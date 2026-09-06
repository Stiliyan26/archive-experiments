import React from 'react';
import { Link } from 'react-router-dom'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { ButtonGroup, Button } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants';
import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import EmbedTransportOrderContainer from './EmbedTransportOrderContainer'
import EmbedChangeHistory from '../embeds/EmbedChangeHistory'
import RetrieveDataContainer from '../nomenclatures/RetrieveDataContainer'

import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'
import { dispatchCleanRESTData, dispatchEditRESTData, postRESTData, fetchRESTFollow, resetRESTCallLimit } from '../../actions/taskActions';
import { resolveObjectPath, getNomenclatureByCode } from '../../scripts/dataUtils';
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewTransportOrderContainer extends React.Component {	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}
	
	createTransport(containerData) {
		console.log("Container data: ",containerData);
		//find for code 14 Transport Task
		getNomenclatureByCode('taskTypes', 14, this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
			.then((taskTypeTransport) => {
				if(!taskTypeTransport) {
					this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.errors.transport",{error: "Error.NomenclatureNotFound",response: taskTypeTransport});
					return new Error("Error.NomenclatureNotFound");
				}
				//find for code 2 TASK_RELATION_TYPE_SUBTASK
				getNomenclatureByCode('taskRelationTypes', 2, this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
					.then((typeRelationSubtask) => {
						if(!typeRelationSubtask) {
							this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.errors.transport",{error: "Error.NomenclatureNotFound",response: typeRelationSubtask});
							return new Error("Error.NomenclatureNotFound");
						}
						let postPromiseWrapper = {};
						this.props.actions.postRESTData(
							{
								method: 'post',
								url: API_URL+"/transports/",
								data: {
									orderer: 'dummyWillBeChangedBypostRESTData',
									route: this.props.data.orderRoute,
									contents: this.props.data.orderContents,
									containerNumber: containerData.number,
									containerType: 'dummyWillBeChangedBypostRESTData',
									loadingDate: this.props.data.orderLoadingDate,
									unloadingDate: this.props.data.orderUnloadingDate,
									paymentCurrency: 'dummyWillBeChangedBypostRESTData',
									paymentAmount: parseFloat(this.props.data.orderPaymentAmount),
									type: 'dummyWillBeChangedBypostRESTData',
									_links: {
										orderer: {href: this.props.data && this.props.data.orderOrderer && this.props.data.orderOrderer._links && this.props.data.orderOrderer._links.self ? this.props.data.orderOrderer._links.self.href : undefined},
										containerType: {href: containerData && containerData.type && containerData.type._links && containerData.type._links.self ? containerData.type._links.self.href : undefined},
										paymentCurrency: {href: this.props.data && this.props.data.orderPaymentCurrency && this.props.data.orderPaymentCurrency._links && this.props.data.orderPaymentCurrency._links.self ? this.props.data.orderPaymentCurrency._links.self.href : undefined},
										type: {href: taskTypeTransport && taskTypeTransport._links && taskTypeTransport._links.self ? taskTypeTransport._links.self.href : undefined},
									}
								}
							},
							this.props.componentPath+'._additionalActionsData.createdSubTasks.transport',
							'PageViewTransportOrderContainer.createTransport',
							undefined,
							undefined,
							undefined,
							postPromiseWrapper
						);
						postPromiseWrapper.promise
							.then((responseTransport) => {
								//force refresh of container row
								this.props.actions.dispatchCleanRESTData(containerData._componentPath+"._transport");
								//add relation to the created task
								if(responseTransport instanceof Error) {
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.errors.transport",{error: responseTransport.toString(),response: responseTransport});
									return responseTransport;
								} else {
									let postPromiseWrapperRelation = {};
									this.props.actions.postRESTData(
										{
											method: 'post',
											url: API_URL+"/taskRelations/",
											data: {
												relation: 'dummyWillBeChangedBypostRESTData',
												fromTask: 'dummyWillBeChangedBypostRESTData',
												toTask: 'dummyWillBeChangedBypostRESTData',
												_links: {
													relation: {href: typeRelationSubtask._links.self.href},
													fromTask: {href: this.props.data._links.self.href},
													toTask: {href: responseTransport.data._links.self.href}
												}
											}
										},
										this.props.componentPath+'._additionalActionsData.createdSubTasks.relation',
										'PageViewTransportOrderContainer.createTransport',
										undefined,
										undefined,
										undefined,
										postPromiseWrapperRelation
									);
									return postPromiseWrapperRelation;
								}
							});
					});
			});
	}
	
	render() {
		let body = '';
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				let transportOrderShippingContainersDef = getEntityDefinition("transportOrderShippingContainers",{transportOrder: {show: false}});
				let transportsDef = getEntityDefinition("transports",{});
				transportOrderShippingContainersDef.columns.push({
					Header: this.props.t("Transport._className"),
					accessor: '_transport',
					fluidSize: 1,
					Cell: (props) => {
							//console.warn(props);
							return props.original._editable != true && props.original.number !== undefined ?
								<RetrieveDataContainer
									componentPath={props.original._componentPath+"._transport"+".pageData"}
									retrieveType={"transports"}
									//columns={[{accessor: 'id',}]}
									defaultFilter={[{id: "containerNumber", value: props.original.number}]}
								>
									{props.value !== undefined && props.value.pageData._embedded !== undefined && props.value.pageData._embedded.transports !== undefined && props.value.pageData._embedded.transports[0] !== undefined ? 
										<Link to={transportsDef.pageURL+"/"+props.value.pageData._embedded.transports[0].id}>
											<input id={props.original._componentPath+"._transport"} className="form-control" type="text" style={{cursor: "pointer",}}
												value={props.value.pageData._embedded.transports[0].id}
												disabled={true}
											/>
										</Link>
										: <ButtonGroup>
											<Button variant="outline-dark" onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.createTransport(props.original);}}>
												{this.props.t("TransportOrder.CreateTransport")}
											</Button>
										</ButtonGroup>
									}
								</RetrieveDataContainer>
								: null;
						}
				});
				const taskRelationsFromDef = getEntityDefinition("taskRelations",{
					transportOrder: {show: false},
					relation: {show: false}
				});
				const attachmentDef = getEntityDefinition("taskAttachments",{task: {show: false}});
				const commentsDef = getEntityDefinition("comments",{task: {show: false}});
				body = <div>
						<EmbedRetrieveEntityListContainer
							title={transportOrderShippingContainersDef.label}
							icon={transportOrderShippingContainersDef.icon}
							columns={transportOrderShippingContainersDef.columns}
							componentPath={this.props.componentPath+".transportOrderShippingContainers"}
							retrieveType="transportOrderShippingContainers"
							parentHref={this.props.href}
							parentAttr="transportOrder"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={this.props.t("TransportOrder.relationsFromTask")}
							icon="level-down-alt"
							columns={taskRelationsFromDef.columns}
							componentPath={this.props.componentPath+".relationsFrom"}
							retrieveType="taskRelations"
							parentHref={this.props.href}
							parentAttr="fromTask"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={this.props.t("TransportOrder.taskAttachments")}
							icon={attachmentDef.icon}
							columns={attachmentDef.columns}
							componentPath={this.props.componentPath+".attachments"}
							retrieveType="taskAttachments"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={commentsDef.label}
							icon={commentsDef.icon}
							columns={commentsDef.columns}
							componentPath={this.props.componentPath+".comments"}
							retrieveType="comments"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
							// expanded={true}
						/>
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+".changelog"} retrieveType={this.props.entityName}/>
					</div>;
			}
		}
		return (
			<div >
				<Header text={this.props.headerText} class='page-header text-align-center no-margin' />
				<EmbedTransportOrderContainer 
					retrieve_id={this.props.match.params.entity_id} 
					componentPath={this.props.componentPath+"."+this.props.entityDef.className}
					onBeforeChange={(data) => {
						if(!this.props.entityDef.pageURL) {console.error("pageURL is missing for entity "+this.props.entityDef.className);}
						if(data.id) {
							history.push(this.props.entityDef.pageURL+'/'+data.id);
						} else {
							history.push(this.props.entityDef.pageURL+'/add');
						}}}
					expanded={true}
				/>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = "transportOrders";
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = "transportOrderView";
	let viewData = resolveObjectPath(componentPath,state.rest);
	const data = viewData ? viewData[entityDef.className] : undefined;
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		rest: state.rest,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t("TransportOrder._className"),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchCleanRESTData, dispatchEditRESTData, fetchRESTFollow, postRESTData }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewTransportOrderContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
