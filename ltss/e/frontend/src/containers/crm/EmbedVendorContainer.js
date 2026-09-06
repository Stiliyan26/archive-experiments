import React from 'react';
import url from 'url'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Accordion, Card, ButtonGroup, Button } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import history from './../../scripts/history'
import { ContextAwareToggle } from './../../scripts/util'
import { isAuthenticated } from './../../components/pages/login/Login.js'

import FieldTextContainer from './../fields/FieldTextContainer'
import { getEntityDefinition } from './../nomenclatures/entityDefinitions.js'

import { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, deleteREST, resetRESTCallLimit } from './../../actions/taskActions';
import { showModal, hideModal } from './../../actions/modal'
import { resolveObjectPath } from './../../scripts/dataUtils';

class EmbedVendorContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			open: this.props.expanded !== undefined ? this.props.expanded : false,
			editable: false
		};
	}

	refreshData() {
		this.props.actions.dispatchCleanRESTData(this.props.componentPath);
		if(this.state.editable) this.setState({ editable: false });
	}

	retrieveData() {
		//get the param used for the data in the redux state
		const curr_partnerId = this.props.data && this.props.data._partnerId ? this.props.data._partnerId : this.props.partnerId;
		if(this.props && !this.props.error && !this.props.loading && this.props.href && this.props.componentPath
				&& (!this.props.data || this.props.partnerId != curr_partnerId)) {
			if(!this.props.data) console.log('EmbedRetrieveEntityListContainer retrieve: No data!');
			if(curr_partnerId != this.props.partnerId) console.log('EmbedRetrieveEntityListContainer retrieve: Diff curr_partnerId!');
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/builder/1?from=Vendor&select=Vendor&page=0&size=5&sort=Vendor.id%2Casc&Vendor.person.id="+this.props.partnerId,
				},
				this.props.componentPath,
				response => {return {
						...response.data,
						_partnerId: this.props.partnerId
					};},
				'EmbedVendorContainer.retrieveData',
			);
		}
	}

	saveData() {
		//check data availability
		if(this.props.vendorData) {
			if(this.props.vendorData.id == undefined) {
				this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/vendors/",
						data: {...this.props.vendorData,person: this.props.href} //add the link in case of saving new items
					},
					this.props.componentPath+'._embedded.vendors.0',
					'EmbedVendorContainer.saveData'
				);
			} else {
				this.props.actions.patchRESTData(
						{
							method: 'patch',
							url: API_URL+"/vendors/"+this.props.vendorData.id,
							data: this.props.vendorData,
							headers: {'Content-Type': 'application/merge-patch+json'}
						},
						this.props.componentPath+'._embedded.vendors.0',
						response => (response.data),
						(data) => {},
						'EmbedVendorContainer.saveData'
					);
			}
		}
		if(this.state.editable) this.setState({ editable: false });
	}

	editData() {
		if(this.state.editable) {
			this.refreshData();
		}
		this.setState({ editable: !this.state.editable });
	}

	addData() {
		this.props.actions.dispatchEditRESTData(this.props.componentPath+'._embedded.vendors.0',{});
		this.setState({ editable: true, open: true });
	}

	deleteData() {
		this.props.actions.showModal({
			title: this.props.t("ConfirmationRequired"),
			body: this.props.t("AreYouSureYouWantToDelete"),
			acceptLabel: this.props.t("Yes"),
			acceptCallback: () => {
				let promiseWrapper = {};
				this.props.actions.deleteREST({
						method: 'delete',
						url: API_URL+"/vendors/"+this.props.vendorData.id,
					},
					this.props.componentPath+'._embedded.vendors.0',
					promiseWrapper
				);
				promiseWrapper.promise.then((response) => {this.refreshData();});
				this.props.actions.hideModal()
			},
			refuseLabel: this.props.t("No"),
			refuseCallback: () => {
				this.props.actions.hideModal()
			},
		})
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		} else {
			this.retrieveData();
		}
	}

	componentDidUpdate(prevProps, prevState) {
		this.retrieveData();
	}

	render() {
		let body = '';
		if(this.props.data) {
			if(this.props.loading) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else {
				body =
					<div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-6">
								<label className="contracts-add-form-label">{this.props.t("Vendor.vendorCategory")}</label>
								<FieldTextContainer
									componentPath={this.props.componentPath+'._embedded.hashMaps.0.Vendor'+'.vendorCategory'} //existing path in redux store where we put data
									editable={this.state.editable}
									loading={this.props.loading}
									onChange={(value) => {}}
								/>
							</div>
							<div className="col-sm-6">
								<label className="contracts-add-form-label">{this.props.t("Vendor.paymentMethod")}</label>
								<FieldTextContainer
									componentPath={this.props.componentPath+'._embedded.hashMaps.0.Vendor'+'.paymentMethod'} //existing path in redux store where we put data
									editable={this.state.editable}
									loading={this.props.loading}
									onChange={(value) => {}}
								/>
							</div>
						</div>
					</div>;
			}
		}
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						<ButtonGroup>
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>
								<FontAwesomeIcon icon="truck"/>&nbsp;
								{this.props.vendorData == undefined ? this.props.t("Vendor.NotVendor") : this.props.t("Vendor.IsVendor")}&nbsp;
								<FontAwesomeIcon icon="caret-square-down"/>
							</Button>
							{this.props.vendorData == undefined ?
								<Button variant="outline-dark" title={this.props.t("ReactTable.New")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.addData();}}><FontAwesomeIcon icon="plus"/></Button>
								: (this.state.editable ?
										<Button variant="outline-dark" title={this.props.t("ReactTable.Save")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.saveData();}}><FontAwesomeIcon icon="save"/></Button>
										: <Button variant="outline-dark" title={this.props.t("ReactTable.Edit")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.editData();}}><FontAwesomeIcon icon="edit"/></Button>)
							}
							<Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
							{this.props.vendorData != undefined ? <Button variant="outline-dark" title={this.props.t("ReactTable.Delete")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.deleteData();}}><FontAwesomeIcon icon="trash-alt"/></Button> : undefined}
						</ButtonGroup>
					</Card.Header>
					<Accordion.Collapse eventKey="0"><Card.Body>
						{body}
					</Card.Body></Accordion.Collapse>
				</Card>
			</Accordion>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	const vendorData = data && data._embedded && data._embedded.hashMaps && data._embedded.hashMaps[0] ? data._embedded.hashMaps[0].Vendor : undefined;
	return {
		auth: state.auth,
		//storage
		data: data,
		vendorData: vendorData,
		componentPath: ownProps.componentPath,
		//data
		loading: ownProps.loading != undefined ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		error: data instanceof Error ? data : undefined,
		href: ownProps.href || (vendorData._links && vendorData._links.person ? vendorData._links.person.href : undefined),
		partnerId: ownProps.partnerId,
		id: vendorData ? vendorData.id : undefined,
		//UI
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, deleteREST, showModal, hideModal }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedVendorContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
