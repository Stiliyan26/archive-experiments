import React from 'react';
import Modal from 'react-responsive-modal';
import Switch from 'rc-switch';
import ReactToPrint from "react-to-print";

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Container, Row, Card, ButtonGroup, Button } from 'react-bootstrap';

import { isAuthenticated } from './../../components/pages/login/Login.js'
import * as Constants from './../../static/constants';

import EmbedEntityContainer from './EmbedEntityContainer'
import InvoiceTemplate from './../../components/shared/InvoiceTemplate'

import { getEntityDefinition } from './../nomenclatures/entityDefinitions.js'
import { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

class EmbedInvoiceContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			isModalPrintOpen: false,
			printLanguage: 'bg',
			originalOrCopy: 'orig',
		};
	}
	
	getPrintModal(formData, _printData) {
		return <Modal center open={this.state.isModalPrintOpen} onClose={() => {this.setState({ isModalPrintOpen: false });}} showCloseIcon={true}>
				<Container>
					<Row className="form-group">
						<Button variant={(_printData instanceof Array ? "success" : "warning")} disabled={!(_printData instanceof Array)}
							onClick={_printData instanceof Array ? () => {
								resetRESTCallLimit();
								if(_printData instanceof Array){
									// timeout set to 1000ms in order to be sure that the shadow table has rendered before the print is triggered
									setTimeout(() => {
										this.printTriggerWrapper.handlePrint()
									}, 1000);
								}
							} : undefined}
						>
							<FontAwesomeIcon icon="print"/>
							&nbsp;{ _printData instanceof Array ? "Печат" : "Изчакване на данните..." }
						</Button>
					</Row>
					<Row className='form-group'>
						<div className="col-sm-6">Език:</div>
						<div className="col-sm-6">
							<Switch
								id="language"
								onChange={(e) => {
									this.setState({
										printLanguage: e ? 'en' : 'bg'
									})}}
								checkedChildren={'EN'}
								unCheckedChildren={'BG'}
							/>
						</div>
						<div className="col-sm-6">Оригинал:</div>
						<div className="col-sm-6"><input 
							type="checkbox" 
							checked={this.state.originalOrCopy == 'orig'} 
							onChange={(e) => {
								this.setState({
									originalOrCopy: e.target.checked ? 'orig' : 'copy'
								})}}/>
						</div>
					</Row>
				
					<InvoiceTemplate
						className="invisible-table"
						formData={formData}
						formDetailsData={_printData}
						ref={(el) => (this.tableRef = el)}
						lang={this.state.printLanguage}
						originalOrCopy={this.state.originalOrCopy}
					/>
					<ReactToPrint
							ref={(el) => (this.printTriggerWrapper = el)}
							// the trigger below contains a HIDDEN button which gets 'pushed' indirectly by the 'enablePrint' func
							trigger={() => <Button disabled={!formData} ref={(el) => (this.printTrigger = el)} size="lg" className='hidden'><FontAwesomeIcon icon='print'/></Button>}
							content={() => this.tableRef }
							pageStyle="
							table { width: 100% }
							.header {width: 40%}
							.header-divider {width: 20%}
							.text-align-center { text-align: -webkit-center; }
							.text-align-right { text-align: -webkit-right; }
							.text-align-left { text-align: -webkit-left; }
							.pull-right { float: right !important }
							@page { size: auto;  margin: 10mm; }
							@media print { body { -webkit-print-color-adjust: exact; } }
							.invisible-table .offer-table th,
							.invisible-table .offer-table td:not(.no-border) {border: 1px solid black}
							.invisible-table .offer-table th { background-color: #BFBFBF }
							.totalOfferPrice { background-color: #FFFF00 } "
							copyStyles={false}
					/>
				</Container>
			</Modal>
		;
	}

	fetchLines(){
		let checkInterval = setInterval(() => {
			let invoice = this.props.data;
			if(invoice){
				clearInterval(checkInterval)
				this.props.actions.fetchRESTFollow(
					{
						url: `${API_URL}/reports/builder/1?from=InvoiceRow&select=InvoiceRow,InvoiceRow.article,InvoiceRow.transport,InvoiceRow.transport.tractorUnit,InvoiceRow.transport.trailer&InvoiceRow.invoice.id=${invoice.id}&page=0&size=5000`,
					},
					this.props.componentPath+"._printData",
					response => {
						let data = response.data;
						if(!data._embedded){
							throw new Error('Грешка при извличане на данните.')
						}
						let lines = data._embedded.hashMaps.map((hashMap) => {
							let line = hashMap.InvoiceRow;
							line.article = hashMap['InvoiceRow.article'];
							line.transport = hashMap['InvoiceRow.transport'];
							if(line.transport) {
								line.transport.tractorUnit = hashMap['InvoiceRow.transport.tractorUnit'];
								line.transport.trailer = hashMap['InvoiceRow.transport.trailer'];
							}
							return line;
						})
						return lines
					},
					'EmbedInvoiceContainer.fetchLines',
				)
			}
		}, 200)
	}

	render() {
		let modal = this.getPrintModal(this.props.data, this.props._printData);
		return (
			<EmbedEntityContainer
				componentPath={this.props.componentPath+"."+this.props.entityDef.className}
				retrieve_id={this.props.retrieve_id}
				entityName={this.props.entityName}
				headerText={this.props.headerText}
				creatable={true}
				expanded={this.props.expanded}
				onBeforeChange={this.props.onBeforeChange}
				onChange={this.props.onChange}
				onPrint={(e) => {
					e.stopPropagation(); 
					resetRESTCallLimit(); 
					this.fetchLines();
					this.setState({ isModalPrintOpen: true }); 
				}}
			>
			{modal}
			</EmbedEntityContainer>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let viewData = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	const entityName = "invoices";
	const entityDef = getEntityDefinition(entityName,undefined);
	let data = undefined;
	if(viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded 
			&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
		data = viewData[entityDef.className]._embedded[entityName][0];
	}
	return {
		auth: state.auth,
		//storage
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		error: data instanceof Error ? data : (data && data[Constants.PATH_FOR_ERROR] instanceof Error ? data[Constants.PATH_FOR_ERROR] : undefined),
		componentPath: ownProps.componentPath,
		_printData: data ? data._printData : undefined,
		//data
		retrieve_id: ownProps.retrieve_id,
		//UI
		onBeforeChange: ownProps.onBeforeChange ? ownProps.onBeforeChange : () => {},
		onChange: ownProps.onChange ? ownProps.onChange : () => {},
		headerText: 'Фактура',
		expanded: ownProps.expanded,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedInvoiceContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
