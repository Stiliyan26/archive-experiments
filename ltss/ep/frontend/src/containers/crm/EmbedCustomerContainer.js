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
import FieldBooleanContainer from './../fields/FieldBooleanContainer'
import FieldNomenclatureSelectContainer from './../fields/FieldNomenclatureSelectContainer'
import FieldSelectOrEditContainer from './../fields/FieldSelectOrEditContainer'

import { builderDataToProjection, getEntityDefinition } from './../nomenclatures/entityDefinitions.js'

import EmbedRetrieveEntityListContainer from './../nomenclatures/EmbedRetrieveEntityListContainer'

import { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, deleteREST, resetRESTCallLimit } from './../../actions/taskActions';
import { showModal, hideModal } from './../../actions/modal'
import { resolveObjectPath } from './../../scripts/dataUtils';

class EmbedCustomerContainer extends React.Component {
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
		if(this.props && !this.props.error && !this.props.loading && this.props.personHref && this.props.componentPath
				&& (!this.props.data || this.props.partnerId != curr_partnerId)) {
			if(!this.props.data) console.log('EmbedRetrieveEntityListContainer retrieve: No data!');
			if(curr_partnerId != this.props.partnerId) console.log('EmbedRetrieveEntityListContainer retrieve: Diff curr_partnerId!');
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/builder/1?from=Customer&select=Customer,Customer.person.id,Customer.stage,Customer.assignedSales,Customer.direction,Customer.area,Customer.business,Customer.generalCategory,Customer.segment&page=0&size=5&sort=Customer.id%2Casc&Customer.person.id="+this.props.partnerId,
				},
				this.props.componentPath,
				(response) => {
					let newData = response.data;
					//transform data to "repository response"-like
					builderDataToProjection(newData,"customers");
					newData = newData._embedded.customers[0];
					newData._partnerId = newData.person.id;
					if(newData == undefined) {
						newData = new Error(this.props.t("Error.CouldNotFindRecord") + this.props.t("Customer.IsClient"));
					}
					return newData;
				},
				'EmbedCustomerContainer.retrieveData',
				{}
			);
		}
	}

	saveData() {
		//check data availability
		if(this.props.customerData) {
			if(this.props.customerData.id == undefined) {
				this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/customers/",
						data: {...this.props.customerData,person: this.props.personHref} //add the link in case of saving new items
					},
					this.props.componentPath,
					'EmbedCustomerContainer.saveData'
				);
			} else {
				this.props.actions.patchRESTData(
						{
							method: 'patch',
							url: API_URL+"/customers/"+this.props.customerData.id,
							data: this.props.customerData,
							headers: {'Content-Type': 'application/merge-patch+json'}
						},
						this.props.componentPath,
						response => (response.data),
						(data) => {},
						'EmbedCustomerContainer.saveData'
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
		this.props.actions.dispatchEditRESTData(this.props.componentPath,{});
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
						url: API_URL+"/customers/"+this.props.customerData.id,
					},
					this.props.componentPath,
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
				const clientInterestsDef = getEntityDefinition("clientInterests",{legalPerson: {show: false}});
				body =
					<div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-6">
								<FieldBooleanContainer id="client"
									componentPath={this.props.componentPath+'.client'} //existing path in redux store where we put data
									editable={this.state.editable}
									loading={this.props.loading}
								/>
								<label htmlFor="client">{this.props.t("Customer.IsClient")}</label>
							</div>
							<div className="col-sm-6">
								<FieldBooleanContainer id="unsubscribed"
									componentPath={this.props.componentPath+'.unsubscribed'} //existing path in redux store where we put data
									editable={this.state.editable}
									loading={this.props.loading}
								/>
								<label htmlFor="unsubscribed">{this.props.t("Customer.unsubscribed")}</label>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-6">
								<label className="contracts-add-form-label">{this.props.t("Customer.stage")}</label>
								<FieldNomenclatureSelectContainer
									href={this.props.stageHref} //REST URI to load from
									componentPath={this.props.componentPath+'.stage'} //existing path in redux store where we put data
									nomenclatureKey={'salesStages'}
									editable={this.state.editable}
									loading={this.props.loading}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.stage.href',href);}}
								/>
							</div>
							<div className="col-sm-6">
								<label className="contracts-add-form-label">{this.props.t("Customer.assignedSales")}</label>
								<FieldNomenclatureSelectContainer
									href={this.props.assignedSalesHref} //REST URI to load from
									componentPath={this.props.componentPath+'.assignedSales'} //existing path in redux store where we put data
									nomenclatureKey={'secUsers'}
									displayAttr={"fullName"}
									editable={this.state.editable}
									loading={this.props.loading}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.assignedSales.href',href);}}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-6">
								<label className="contracts-add-form-label">{this.props.t("Customer.direction")} <span className="text-red">*</span></label>
								<FieldNomenclatureSelectContainer
									href={this.props.directionHref} //REST URI to load from
									componentPath={this.props.componentPath+'.direction'} //existing path in redux store where we put data
									nomenclatureKey={'directionCategories'}
									editable={this.state.editable}
									loading={this.props.loading}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.direction.href',href);}}
								/>
							</div>
							<div className="col-sm-6">
								<label className="contracts-add-form-label">{this.props.t("Customer.area")} <span className="text-red">*</span></label>
								<FieldNomenclatureSelectContainer
									href={this.props.areaHref} //REST URI to load from
									componentPath={this.props.componentPath+'.area'} //existing path in redux store where we put data
									nomenclatureKey={'areaCategories'}
									editable={this.state.editable}
									loading={this.props.loading}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.area.href',href);}}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-6">
								<label className="contracts-add-form-label">{this.props.t("Customer.business")} <span className="text-red">*</span></label>
								<FieldNomenclatureSelectContainer
									href={this.props.businessHref} //REST URI to load from
									componentPath={this.props.componentPath+'.business'} //existing path in redux store where we put data
									nomenclatureKey={'businessCategories'}
									editable={this.state.editable}
									loading={this.props.loading}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.business.href',href);}}
								/>
							</div>
							<div className="col-sm-6">
								<label className="contracts-add-form-label">{this.props.t("Customer.generalCategory")} <span className="text-red">*</span></label>
								<FieldNomenclatureSelectContainer
									href={this.props.generalCategoryHref} //REST URI to load from
									componentPath={this.props.componentPath+'.generalCategory'} //existing path in redux store where we put data
									nomenclatureKey={'generalCategories'}
									editable={this.state.editable}
									loading={this.props.loading}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.generalCategory.href',href);}}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-6">
								<label className="contracts-add-form-label">{this.props.t("Customer.segment")}</label>
								<FieldSelectOrEditContainer
									componentPath={this.props.componentPath+'.segment'} //existing path in redux store where we put data
									listReportEntity={"Customer"}
									listReportAttr={"segment"}
									editable={this.state.editable}
									loading={this.props.loading}
									onChange={(href) => {}}
								/>
							</div>
						</div>
						<EmbedRetrieveEntityListContainer
							title={clientInterestsDef.label}
							icon={clientInterestsDef.icon}
							columns={clientInterestsDef.columns}
							componentPath={this.props.componentPath+'.clientInterests'}
							retrieveType="clientInterests"
							parentHref={this.props.href}
							parentAttr="customer"
							parentData={this.props.customerData}
						/>
					</div>;
			}
		}
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						<ButtonGroup>
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>
								<FontAwesomeIcon icon="star"/>&nbsp;
								{this.props.id == undefined ? this.props.t("Customer.NotClient") : this.props.t("Customer.IsClient")}&nbsp;
								<FontAwesomeIcon icon="caret-square-down"/>
							</Button>
							{this.props.customerData == undefined ?
								<Button variant="outline-dark" title={this.props.t("ReactTable.New")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.addData();}}><FontAwesomeIcon icon="plus"/></Button>
								: (this.state.editable ?
										<Button variant="outline-dark" title={this.props.t("ReactTable.Save")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.saveData();}}><FontAwesomeIcon icon="save"/></Button>
										: <Button variant="outline-dark" title={this.props.t("ReactTable.Edit")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.editData();}}><FontAwesomeIcon icon="edit"/></Button>)
							}
							<Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
							{this.props.customerData != undefined ? <Button variant="outline-dark" title={this.props.t("ReactTable.Delete")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.deleteData();}}><FontAwesomeIcon icon="trash-alt"/></Button> : undefined}
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
	const customerData = data;
	return {
		auth: state.auth,
		//storage
		data: data,
		customerData: customerData,
		componentPath: ownProps.componentPath,
		//data
		loading: ownProps.loading != undefined ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		error: data instanceof Error ? data : undefined,
		href: customerData && customerData._links && customerData._links.self ? customerData._links.self.href : undefined,
		personHref: ownProps.href || (customerData._links && customerData._links.person ? customerData._links.person.href : undefined),
		partnerId: ownProps.partnerId,
		id: customerData ? customerData.id : undefined,
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedCustomerContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
