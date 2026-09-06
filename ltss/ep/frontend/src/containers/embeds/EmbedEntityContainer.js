import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Accordion, Card, ButtonGroup, Button, Alert } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import history from './../../scripts/history'
import { ContextAwareToggle } from './../../scripts/util'
import { isAuthenticated } from './../../components/pages/login/Login.js'
import RetrieveDataContainer from '../nomenclatures/RetrieveDataContainer';

import { builderDataToProjection, getEntityDefinition, getEntityForm, builderURLFromColumns, getExpandedColumns } from '../nomenclatures/entityDefinitions.js'
import { dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, deleteREST, resetRESTCallLimit } from './../../actions/taskActions';
import { showModal, hideModal } from './../../actions/modal'
import { resolveObjectPath, extractErrorMessage } from './../../scripts/dataUtils';
import { hasPermission } from './../InfoBarContainer';

class EmbedEntityContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			editable: false,
			open: this.props.expanded !== undefined ? this.props.expanded : false,
		};
	}
	
	onChildUpdate(componentPath, value, doCalcOnChange) {
		//console.log("onChildUpdate");
		if(componentPath !== undefined && value !== undefined
			&& componentPath.startsWith(this.props.dataComponentPath) 
			&& value != resolveObjectPath(componentPath.substring( this.props.dataComponentPath.length+1 ),this.props.data)
		) {
			//console.log(value, resolveObjectPath(componentPath.substring( this.props.dataComponentPath.length+1 ),this.props.data));
			this.props.actions.dispatchEditRESTData(componentPath,value);
			this.setState({needUpdate: this.state.needUpdate+1});
			if( doCalcOnChange ) {
				this.calcData(this.props.data);
			}
		}
	}

	refreshData() {
		this.props.actions.dispatchCleanRESTData(this.props.componentPath);
		if(this.state.editable) this.setState({ editable: false });
	}

	retrieveData() {
		//console.log("retrieveData",this.props.loading,this.props.error,this.props.retrieve_id,this.props.data);
		//if loading, no need to retrieve again
		if(!this.props.loading && !(this.props.data instanceof Error)) {
			if(this.props.retrieve_id == 'add') { 
				if(!this.state.editable) this.setState({ editable: true });
				//if there is no data node, create it; or if there is old data, clear it
				if(!this.props.data || this.props.data.id) {
					this.addData();
				}
			}
		}
	}
	
	calcData(dataForCalc) {
		//console.log("calcData");
		return this.props.setDefaults(dataForCalc).then((dataForCalc) => {
			dataForCalc.calculateOnly = true;
			//transient fields don't work with this version of Spring for PATCH, they work only for POST
			this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/"+this.props.entityName+"/",
						data: dataForCalc
					},
					this.props.dataComponentPath,
					'EmbedEntityContainer.calcData',
					response => {
						return response.data;
					},
					(data) => {
						this.props.onChange(data);
						if(this.state.editable) this.setState({ editable: false });
					}
				);
		});
	}

	saveData() {
		//check data availability
		if(this.props.data) {
			this.props.onConfirmSave(this.props.data).then(() => {
				let confirmedData = this.props.data;
				if(confirmedData.id == undefined) {
					this.props.actions.postRESTData(
							{
								method: 'post',
								url: API_URL+"/"+this.props.entityName+"/",
								data: confirmedData
							},
							this.props.dataComponentPath,
							'EmbedEntityContainer.saveData',
							response => (response.data),
							(data) => {
								//console.log("postRESTData onchange");
								this.props.onChange(data);
								if(this.state.editable) this.setState({ editable: false });
								this.props.onCommitChange();
							},
							(data) => {
								this.props.onBeforeChange(data);
							}
						);
				} else {
					this.props.actions.patchRESTData(
							{
								method: 'patch',
								url: API_URL+"/"+this.props.entityName+"/"+confirmedData.id,
								data: confirmedData,
								headers: {'Content-Type': 'application/merge-patch+json'}
							},
							this.props.dataComponentPath,
							response => (response.data),
							(data) => {
								if(this.state.editable) this.setState({ editable: false });
								this.props.onCommitChange();
							},
							'EmbedEntityContainer.saveData'
						);
				}
			});
		}
	}

	editData() {
		if(this.state.editable) {
			this.refreshData();
		}
		this.setState({ editable: !this.state.editable });
	}

	addData() {
		//console.log("addData");
		this.props.actions.dispatchEditRESTData(this.props.componentPath+"._embedded."+this.props.entityName+".0"+"."+Constants.PATH_FOR_LOADING,this.calcData({}) );
		this.setState({ editable: true });
		this.props.onChange({});
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
						url: API_URL+"/"+this.props.entityName+"/"+this.props.data.id,
					},
					this.props.dataComponentPath,
					promiseWrapper
				);
				promiseWrapper.promise.then((response) => {
					if( response instanceof Error || (response && response[Constants.PATH_FOR_ERROR] instanceof Error) ) {
						//handle error in render
					} else {
						this.refreshData();
						this.props.onCommitChange();
						//on success go to add new entity page
						if(!this.props.entityDef.pageURL) {console.error("pageURL is missing for entity "+this.props.entityDef.className);}
						history.push(this.props.entityDef.pageURL+'/add');
					}
				});
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
				body = getEntityForm(this.props.entityName, this.props.columnOverride, this.props.dataComponentPath, this.state.editable, 
						this.props.loading, 
						this.props.data, 
						(componentPath, value, doCalcOnChange) => {this.onChildUpdate(componentPath, value, doCalcOnChange);}
					);
			}
		}
		let errorMessage = extractErrorMessage(this.props.error, this.props.entityDef);

		console.log("EmbedEntityCOntainer -> this.props.editable: ", this.props.editable)
		
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						{this.props.data == undefined && this.props.retrieve_id && this.props.viewData && this.props.viewData.page && this.props.viewData.page.totalElements == 0 ?
							<Alert variant="danger"><FontAwesomeIcon icon="ban"/> {this.props.t("Error.CouldNotFindRecord") + this.props.headerText}</Alert>
							: ""}
						{this.props.error ?
							<Alert variant="danger"><FontAwesomeIcon icon="ban"/> {errorMessage}</Alert>
							: ""}
						<Button variant="outline-dark" title={this.props.t(this.state.open ? "ReactTable.Collapse" : "ReactTable.Expand")} onClick={() => this.setState({ open: !this.state.open })}>{this.props.headerText}&nbsp;<FontAwesomeIcon icon={this.state.open ? "caret-square-up" : "caret-square-down"}/></Button>
						<ButtonGroup>
							{this.props.creatable ?
									<Button variant="outline-dark" title={this.props.t("ReactTable.New")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.addData();}}><FontAwesomeIcon icon="plus"/></Button>
									: undefined
							}
							{this.props.editable ? 
									(this.state.editable ?
										<Button variant="outline-dark" title={this.props.t("ReactTable.Save")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.saveData();}}><FontAwesomeIcon icon="save"/></Button>
										: <Button variant="outline-dark" title={this.props.t("ReactTable.Edit")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.editData();}}><FontAwesomeIcon icon="edit"/></Button>)
									: undefined
							}
										{this.props.editable && this.state.editable ?
											<Button variant="outline-dark" disabled={!this.props.data} title={this.props.t("ReactTable.Calculate")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.calcData(this.props.data);}}><FontAwesomeIcon icon="calculator"/></Button>
											: undefined
										}
							{this.props.retrieve_id == "add" ?
									''
									: <Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
							}
							{this.props.deleteable && this.props.data != undefined && this.props.retrieve_id != "add" ? 
									<Button variant="outline-dark" title={this.props.t("ReactTable.Delete")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.deleteData();}}><FontAwesomeIcon icon="trash-alt"/></Button>
									: undefined
							}
						</ButtonGroup>
						<ButtonGroup>
							{this.props.onPrint != undefined ? 
									<Button variant="outline-dark" disabled={!this.props.data} title={this.props.t("Print")} onClick={this.props.onPrint}><FontAwesomeIcon icon='print'/></Button>
									: undefined
							}
						</ButtonGroup>
					</Card.Header>
					<Accordion.Collapse eventKey="0"><Card.Body>
						{this.props.retrieve_id != 'add' ?
							<RetrieveDataContainer
								retrieveType={this.props.entityName}
								expandColumns={this.props.expandColumns}
								columns={this.props.columns}
								excludeRootEntity={false}
								loading={this.props.loading}
								componentPath={this.props.componentPath}
								onAfterRetrieve={this.props.onAfterRetrieve}
								
								defaultFilter={[{id: "id", value: this.props.retrieve_id}]}
							/>
							: ""
						}
						{this.props.placeholderBeforeBody(this.props,this.state)}
						{body}
						{this.props.children}
					</Card.Body></Accordion.Collapse>
				</Card>
			</Accordion>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let viewData = resolveObjectPath(ownProps.componentPath,state.rest); 
	const dataComponentPath = ownProps.componentPath+"._embedded."+ownProps.entityName+".0";
	let data = resolveObjectPath(dataComponentPath,state.rest); //rest because of fetchREST
	const entityDef = getEntityDefinition(ownProps.entityName,ownProps.columnOverride,ownProps.entityOverride);
	const permCreate = hasPermission(state.rest.currentPermissions, "ROLE_POST_"+ownProps.entityName);
	const permEdit = hasPermission(state.rest.currentPermissions, "ROLE_PATCH_"+ownProps.entityName);
	const permDelete = hasPermission(state.rest.currentPermissions, "ROLE_DELETE_"+ownProps.entityName);
	const filteredColumns = getExpandedColumns(ownProps.entityName, ownProps.expandColumns, ownProps.columnOverride).filter((col) => (col.isAggregate == undefined || col.isAggregate == false) && (col.show != false));
	
	// console.log("ownProps.entityName: ", ownProps.entityName)
	// if(state.rest.currentPermissions && state.rest.currentPermissions._embedded && state.rest.currentPermissions._embedded.secPermissions ) {
	// 	console.log("EmbedEntityCOntainer -> state.rest.currentPermissions: ", state.rest.currentPermissions._embedded.secPermissions.filter((e) => {
	// 		return e.code.endsWith(ownProps.entityName)
	// 	}))
	// }
	
	return {
		auth: state.auth,
		//storage
		entityName: ownProps.entityName,
		entityDef: entityDef,
		data: data,
		viewData: viewData,
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		error: data instanceof Error ? data : (data && data[Constants.PATH_FOR_ERROR] instanceof Error ? data[Constants.PATH_FOR_ERROR] : undefined),
		componentPath: ownProps.componentPath,
		dataComponentPath: dataComponentPath,
		onAfterRetrieve: ownProps.onAfterRetrieve ? ownProps.onAfterRetrieve : (() => {}),
		//data
		retrieve_id: ownProps.retrieve_id,
		parentHref: ownProps.parentHref,
		parentAccessor: ownProps.parentAccessor,
		setDefaults: ownProps.setDefaults ? ownProps.setDefaults : ((data) => Promise.resolve(data)),
		columns: filteredColumns,
		//UI
		onChange: ownProps.onChange ? ownProps.onChange : () => {},
		onCommitChange: ownProps.onCommitChange ? ownProps.onCommitChange : (()=>{}),
		onBeforeChange: ownProps.onBeforeChange ? ownProps.onBeforeChange : () => {},
		onConfirmSave: ownProps.onConfirmSave ? ownProps.onConfirmSave : (data) => {return Promise.resolve(data);},
		headerText: ownProps.headerText,
		expanded: ownProps.expanded,
		creatable: permCreate ? (ownProps.creatable != undefined ? ownProps.creatable : true) : false,
		editable: permEdit ? (ownProps.editable !== undefined ? ownProps.editable : true) : false,
		deleteable: permDelete ? (ownProps.deleteable !== undefined ? ownProps.deleteable : true) : false,
		onPrint: ownProps.onPrint ? ownProps.onPrint : undefined,
		placeholderBeforeBody: ownProps.placeholderBeforeBody instanceof Function ? ownProps.placeholderBeforeBody : (()=>{}),
		columnOverride: ownProps.columnOverride,
		entityOverride: ownProps.entityOverride,
		expandColumns: ownProps.expandColumns,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, deleteREST, showModal, hideModal }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedEntityContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
