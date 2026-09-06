import React from 'react';
import { Accordion, Card, ButtonGroup, Button, Alert } from 'react-bootstrap';
import Modal from 'react-responsive-modal';
import ReactToPrint from "react-to-print";

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import * as Constants from './../../static/constants';
import history from './../../scripts/history'
import { ContextAwareToggle } from './../../scripts/util'
import { isAuthenticated } from './../../components/pages/login/Login.js'

import FieldNomenclatureSelectContainer from './../fields/FieldNomenclatureSelectContainer'
import FieldTextareaContainer from './../fields/FieldTextareaContainer'
import FieldTimestampContainer from './../fields/FieldTimestampContainer'
import EmbedRetrieveEntityListContainer from './../nomenclatures/EmbedRetrieveEntityListContainer'
import GradeTemplate from './../../components/shared/GradeTemplate'

import { builderDataToProjection, getEntityDefinition } from './../nomenclatures/entityDefinitions.js'
import { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath, extractErrorMessage } from './../../scripts/dataUtils';

class EmbedEmployeeAttestationContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			open: this.props.expanded !== undefined ? this.props.expanded : false,
			editable: false,
			isModalCommentTemplateOpen: false,
		};
	}

	refreshData() {
		this.props.actions.dispatchCleanRESTData(this.props.componentPath);
		if(this.state.editable) this.setState({ editable: false });
	}

	retrieveData() {
		//if loading, no need to retrieve again
		if(!this.props.loading) {
			if(this.props.retrieve_id != 'add') { //isNew = False
				const urlWithoutId = API_URL+"/reports/builder/1?from=EmployeeAttestation&select=EmployeeAttestation,EmployeeAttestation.employee,EmployeeAttestation.employee.position,EmployeeAttestation.employee.company,EmployeeAttestation.employee.department,EmployeeAttestation.employee.department.parentDepartment,EmployeeAttestation.certifier,EmployeeAttestation.watcher&EmployeeAttestation.id=";
				const curr_id = this.props.data && this.props.data.id ?
						(urlWithoutId+this.props.data.id)
						: (this.props.error instanceof Error && this.props.error.config ?
								this.props.error.config.url
								: undefined);
				if(this.props.retrieve_id && this.props.componentPath //hasParams = True
						//&& !this.props.error
						&& (!this.props.data //hasData = False
								//hasData = True, isLoading = False, isDifferent
								|| curr_id != (urlWithoutId+this.props.retrieve_id))) {
					this.props.actions.fetchRESTFollow(
						{
							url: urlWithoutId+this.props.retrieve_id,
						},
						this.props.componentPath,
						(response) => {
							let newData = response.data;
							//transform data to "repository response"-like
							builderDataToProjection(newData,"employeeAttestations");
							newData = newData._embedded.employeeAttestations[0];
							if(newData == undefined) {
								newData = new Error("Оценката не е намерена");
							}
							return newData;
						},
						'EmbedEmployeeAttestationContainer.retrieveData',
						{}
					);
					if(this.state.editable) this.setState({ editable: false });
				}
			} else {
				if(!this.state.editable) this.setState({ editable: true });
				//if there is no data node, create it
				if(!this.props.data || this.props.data.id) {
					this.addData();
				}
			}
		}
	}

	saveData() {
		//check data availability
		if(this.props.data) {
			if(this.props.data.id == undefined) {
				this.props.actions.postRESTData(
						{
							method: 'post',
							url: API_URL+"/employeeAttestations/",
							data: this.props.data
						},
						this.props.componentPath,
						'EmbedEmployeeAttestationContainer.saveData',
						response => (response.data),
						(data) => {this.props.onChange(data);}
					);
			} else {
				this.props.actions.patchRESTData(
						{
							method: 'patch',
							url: API_URL+"/employeeAttestations/"+this.props.data.id,
							data: this.props.data,
							headers: {'Content-Type': 'application/merge-patch+json'}
						},
						this.props.componentPath,
						response => (response.data),
						(data) => {},
						'EmbedEmployeeAttestationContainer.saveData'
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
		this.setState({ editable: true });
		this.props.onChange({});
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		} else {
			this.retrieveData();
		}
	}

	componentDidUpdate(prevProps, prevState) {
		let self = this
		this.retrieveData();
		if(this.state.enablePrint){
			// timeout set to 1000ms in order to be sure that the shadow table has rendered before the print is triggered
			setTimeout(() => {
				self.printTriggerWrapper.handlePrint()
			}, 1000)
			this.setState({
				enablePrint: false
			})
		}
	}

	enablePrint(){
		this.fetchAdditionalPrintData()
		this.setState({
			enablePrint: true
		})
	}

	fetchAdditionalPrintData(){
		let self = this
		let checkInterval = setInterval(() => {
			let employeeAttestation = self.props.data
			if(employeeAttestation){
				clearInterval(checkInterval)
				self.props.actions.fetchRESTFollow(
					{
						url: `${API_URL}/reports/builder/1?from=EmployeeAttestation&select=EmployeeAttestation,EmployeeAttestation.employee,EmployeeAttestation.employee.company,EmployeeAttestation.employee.department,EmployeeAttestation.employee.position&EmployeeAttestation.id=${employeeAttestation.id}&page=0&size=5000`,
					},
					`${self.props.componentPath}.employee`,
					response => {
						let data = response.data;
						if(!data._embedded){
							throw new Error('Not authorized to print an attestation, please log out and log in again.')
						}
						if(data._embedded && data._embedded.hashMaps && data._embedded.hashMaps[0] && data._embedded.hashMaps[0].EmployeeAttestation && data._embedded.hashMaps[0]['EmployeeAttestation.employee']){
							let employee = data._embedded.hashMaps[0]['EmployeeAttestation.employee']
							employee.company = data._embedded.hashMaps[0]['EmployeeAttestation.employee.company']
							employee.department = data._embedded.hashMaps[0]['EmployeeAttestation.employee.department']
							employee.position = data._embedded.hashMaps[0]['EmployeeAttestation.employee.position']
							return employee
						}
					},
					'EmbedEmployeeAttestationContainer.fetchAdditionalPrintData',
					{}
				)
			}
		}, 200)
	}

	render() {
		const { data, employeeCompany, employeeDepartment, employeePosition } = this.props
		let body = '';

		if(data) {
			if(this.props.loading instanceof Promise) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else if(!data.id || data.id == parseInt(this.props.retrieve_id,10)) {
				const commentTemplatessDef = getEntityDefinition("commentTemplates",{});
				body =
					<div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-2">
								<label className="contracts-add-form-label">Служител <span className="text-red">*</span></label>
								<FieldNomenclatureSelectContainer
									href={this.props.employeeHref} //REST URI to load from
									componentPath={this.props.componentPath+'.employee'} //existing path in redux store where we put data
									nomenclatureKey={'employees'}
									editable={this.state.editable}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.employee.href',href);}}
								/>
							</div>
							<div className="col-sm-3">
								<label className="contracts-add-form-label">Оценяващ <span className="text-red">*</span></label>
								<FieldNomenclatureSelectContainer
									href={this.props.certifierHref} //REST URI to load from
									componentPath={this.props.componentPath+'.certifier'} //existing path in redux store where we put data
									nomenclatureKey={'employees'}
									editable={this.state.editable}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.certifier.href',href);}}
								/>
							</div>
							<div className="col-sm-3">
								<label className="contracts-add-form-label">Наблюдаващ <span className="text-red">*</span></label>
								<FieldNomenclatureSelectContainer
									href={this.props.watcherHref} //REST URI to load from
									componentPath={this.props.componentPath+'.watcher'} //existing path in redux store where we put data
									nomenclatureKey={'employees'}
									editable={this.state.editable}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.watcher.href',href);}}
								/>
							</div>
							<div className="col-sm-2">
								<label className="contracts-add-form-label">Оценка за периода от</label>
								<FieldTimestampContainer
									componentPath={this.props.componentPath+'.attestationFromDate'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
							<div className="col-sm-2">
								<label className="contracts-add-form-label">Оценка за периода до</label>
								<FieldTimestampContainer
									componentPath={this.props.componentPath+'.attestationToDate'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Наименование</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.name'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Цели и задачи за служителя</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.employeeGoals'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Оценка</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.attestationText'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-10">
								<label className="contracts-add-form-label">Мотиви</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.motivesText'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
							{this.state.editable
							? <div className="col-sm-2">
								<ButtonGroup>
									<Button variant="outline-dark" title={this.props.t("ReactTable.New")} onClick={(e) => {
											e.stopPropagation();
											resetRESTCallLimit();
											this.setState({ isModalCommentTemplateOpen: true });
										}}><FontAwesomeIcon icon="plus"/>
									</Button>
								</ButtonGroup>
								<Modal open={this.state.isModalCommentTemplateOpen} onClose={() => {this.setState({ isModalCommentTemplateOpen: false });}}>
									<ButtonGroup>
										<Button variant="outline-dark" variant="success" className="col-sm-12"
											onClick={() => {
												resetRESTCallLimit();
												let newText = (this.props.data && this.props.data.motivesText ? this.props.data.motivesText : "");
												if(data && data._commentTemplates && data._commentTemplates.selectedRows && data._commentTemplates.selectedRows.map instanceof Map) {
													data._commentTemplates.selectedRows.map.forEach((value) => {
														newText = (newText ? newText + "\n" : "") + value.text;
													});
												}
												this.props.actions.dispatchEditRESTData(this.props.componentPath+'.motivesText',newText);
												this.setState({ isModalCommentTemplateOpen: false });
											}}
										>
											"Избери мотиви"
										</Button>
									</ButtonGroup>
									<EmbedRetrieveEntityListContainer
										retrieveType = {"commentTemplates"}
										componentPath = {this.props.componentPath+"._commentTemplates"}
										columns = {commentTemplatessDef.columns}
										icon = {commentTemplatessDef.icon}
										hasRowSelecting = {true}
										selectedRowsColumns = {[{
												Header: 'Код',
												accessor: 'id',
												width: 50
											}, {
												Header: 'Име',
												accessor: 'name'
											}]}
										title={"Шаблонни мотиви"}
										expanded={true}
										asTable={true}
										editable={false}
									/>
								</Modal>
							</div>
							: undefined}
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Потенциал за развитие</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.potentialText'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Коментар на служителя</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.employeeCommentText'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Коментар на контролиращия ръководител</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.controllingOfficerCommentText'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
					</div>;
			}
		}
		let errorMessage = extractErrorMessage(this.props.error, getEntityDefinition("employeeAttestations"));
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						{this.props.error ?
							<Alert variant="danger">Грешка: {errorMessage}</Alert>
							: ""}
						<ButtonGroup>
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>Оценка на служител</Button>
							{this.props.creatable ?
									<Button variant="outline-dark" title={this.props.t("ReactTable.New")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.addData();}}><FontAwesomeIcon icon="plus"/></Button>
									: undefined
							}
							{this.state.editable ?
									<Button variant="outline-dark" title={this.props.t("ReactTable.Save")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.saveData();}}><FontAwesomeIcon icon="save"/></Button>
									: <Button variant="outline-dark" title={this.props.t("ReactTable.Edit")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.editData();}}><FontAwesomeIcon icon="edit"/></Button>
							}
							{this.props.retrieve_id == "add" ?
									'' //TODO in the future may be needed a button to load defaults or make calculations?
									: <Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
							}
		
							<Button variant="outline-dark" disabled={!data} title={this.props.t("Print")} onClick={() => this.enablePrint()} ><FontAwesomeIcon icon='print'/></Button>
							<ReactToPrint
									ref={(el) => (this.printTriggerWrapper = el)}
									// the trigger below contains a HIDDEN button which gets 'pushed' indirectly by the 'enablePrint' func
									trigger={() => <Button disabled={!data} ref={(el) => (this.printTrigger = el)} className='hidden'><FontAwesomeIcon icon='print'/></Button>}
									content={() => this.gradeRef }
									pageStyle="
									table { width: 100% }
									.header {width: 40%}
									.header-divider {width: 20%}
									.text-align-right { text-align: -webkit-right; }
									.text-align-left { text-align: -webkit-left; }
									.text-align-center { text-align: -webkit-center }
									.text-bold { font-weight: bold }
									.pull-right { float: right !important }
									@page { size: auto;  margin: 10mm; }
									@media print { body { -webkit-print-color-adjust: exact; } }
									.invisible-table .grade-table { border: 1px solid black }
									.margin-top-10 { margin-top: 10px }
									.margin-top-20 { margin-top: 20px }
									.margin-top-40 { margin-top: 40px }
									.margin-bottom-20 { margin-bottom: 20px }
									.border-row td {
										border-top: 1px solid black;
									}
									.border-row-dashed td {
										border-top: 1px dashed black;
									}
									.first-row td {
										border: 1px solid black;
										border-right: 1px solid black;
										border-left: 1px solid black;
										border-bottom: 1px solid black;
									}
									.border-top-solid td {
										border-top: 1px solid black
									}
									.border-left-solid td {
										border-left: 1px solid black
									}
									.border-right-solid td {
										border-right: 1px solid black
									}
									.border-bottom-dashed td {
										border-dashed: 1px dashed black
									}
		
									.duties-summary { border: 1px solid black; padding: 10px } "
									copyStyles={false}
							/>
						</ButtonGroup>
						<GradeTemplate
								className="invisible-table"
								grade={data}
								employeeCompany={employeeCompany}
								employeeDepartment={employeeDepartment}
								employeePosition={employeePosition}
								ref={(el) => (this.gradeRef = el)}
							/>
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
	let employee = resolveObjectPath(ownProps.componentPath + '.employee',state.rest); //rest because of fetchREST
	return {
		auth: state.auth,
		//storage
		data: data,
		componentPath: ownProps.componentPath,
		//data
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		error: data instanceof Error ? data : (data && data[Constants.PATH_FOR_ERROR] instanceof Error ? data[Constants.PATH_FOR_ERROR] : undefined),
		retrieve_id: ownProps.retrieve_id,
		personHref: data && data._links && data._links.person ? data._links.person.href : undefined,
		employeeHref: data && data._links && data._links.employee ? data._links.employee.href : undefined,
		// employeeCompanyHref: data && data.employee && data.employee._links && data.employee._links.company ? data.employee._links.company.href : undefined,
		// employeeDepartmentHref: data && data.employee && data.employee._links && data.employee._links.department ? data.employee._links.department.href : undefined,
		// employeePositionHref: data && data.employee && data.employee._links && data.employee._links.position ? data.employee._links.position.href : undefined,
		employeeCompany: employee ? employee.company : undefined,
		employeeDepartment: employee ? employee.department : undefined,
		employeePosition: employee ? employee.position : undefined,
		certifierHref: data && data._links && data._links.certifier ? data._links.certifier.href : undefined,
		watcherHref: data && data._links && data._links.watcher ? data._links.watcher.href : undefined,
		//UI
		creatable: ownProps.creatable != undefined ? ownProps.creatable : true,
		onChange: ownProps.onChange ? ownProps.onChange : () => {},
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedEmployeeAttestationContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
