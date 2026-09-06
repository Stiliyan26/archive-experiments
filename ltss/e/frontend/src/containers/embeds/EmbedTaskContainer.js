import React from 'react';
import Modal from 'react-responsive-modal';
import moment from 'moment'
moment.locale('bg')

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

import FieldNomenclatureSelectContainer from './../fields/FieldNomenclatureSelectContainer'
import FieldTextContainer from './../fields/FieldTextContainer'
import FieldTextareaContainer from './../fields/FieldTextareaContainer'
import FieldTimestampContainer from './../fields/FieldTimestampContainer'
import EmbedRetrieveEntityListContainer from './../nomenclatures/EmbedRetrieveEntityListContainer'
import EmbedEntityListOrTableContainer from './../nomenclatures/EmbedEntityListOrTableContainer'
import EmbedOfferToClientContainer from './EmbedOfferToClientContainer'

import { builderDataToProjection, getExpandedColumns, getEntityDefinition } from './../nomenclatures/entityDefinitions.js'
import { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath, getNomenclatureByCode, extractErrorMessage } from './../../scripts/dataUtils';

class EmbedTaskContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			editable: false,
			open: this.props.expanded !== undefined ? this.props.expanded : false,
			isModalACLOpen: false,
			isModalTypeSpecificOpen: false,
			typeSpecificStage: 1,
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
				const urlWithoutId = API_URL+"/reports/builder/1?from=Task&select=Task,Task.status,Task.type,Task.counterParty,Task.createdBy,Task.assigned&Task.id=";
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
							builderDataToProjection(newData,"tasks");
							newData = newData._embedded.tasks[0];
							if(newData == undefined) {
								newData = new Error(this.props.t("Error.TaskNotFound"));
							}
							return newData;
						},
						'EmbedTaskContainer.retrieveData',
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
							url: API_URL+"/tasks/",
							data: this.props.data
						},
						this.props.componentPath,
						'EmbedTaskContainer.saveData',
						response => {this.props.onChange(response.data); return (response.data);}
					);
			} else {
				this.props.actions.patchRESTData(
						{
							method: 'patch',
							url: API_URL+"/tasks/"+this.props.data.id,
							data: this.props.data,
							headers: {'Content-Type': 'application/merge-patch+json'}
						},
						this.props.componentPath,
						response => (response.data),
						(data) => {},
						'EmbedTaskContainer.saveData'
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

	cloneData() {
		let newData = Object.assign({},this.props.data);
		delete newData.id;
		delete newData._links.self;
		//TODO check if other manipulations are needed, e.g. change status to Planned
		this.props.actions.dispatchEditRESTData(this.props.componentPath,newData);
		this.setState({ editable: true });
		this.props.onChange(newData);
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

	createMarketingCampaignSubtasks() {
		//clean the state
		this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.createdSubTasks",undefined);
		//console.log('selectedRows',this.props.data._additionalActionsData.legalPersons.selectedRows);
		if(this.props.data._additionalActionsData
				&& this.props.data._additionalActionsData.legalPersons
				&& this.props.data._additionalActionsData.legalPersons.selectedRows
				&& this.props.data._additionalActionsData.legalPersons.selectedRows.map instanceof Map) {
			//find for code 2 TASK_STATUS_ASSIGNED
			getNomenclatureByCode('taskStatuses',2,this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
				.then((statusAssigned) => {
					//find for code 2 TASK_TYPE_CLIENT_COMMUNICATION
					getNomenclatureByCode('taskTypes',2,this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
						.then((typeClientComm) => {
							let createdSubTasks = [];
							this.props.data._additionalActionsData.legalPersons.selectedRows.map.forEach((value,key) => {
								createdSubTasks.push({
										counterParty: value,
										title: this.props.data.title+" - "+value.name,
										description: this.props.data.description,
										assigned: this.props.data.assigned,
										deadline: this.props.data.deadline,
										status: statusAssigned,
										type: typeClientComm,
										watchers: [this.props.data.assigned._links.self.href],
										_links: {
											counterParty: {href: value._links.self.href},
											assigned: {href: this.props.data.assigned._links.self.href},
											status: {href: statusAssigned._links.self.href},
											type: {href: typeClientComm._links.self.href}
										}
									});
							});
							this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.createdSubTasks._embedded.tasks",createdSubTasks);
						});
				});
		}
	}

	addSubtaskRelation(relationType,index) {
		const subtask = this.props.data._additionalActionsData.createdSubTasks._embedded.tasks[index];
		let postPromiseWrapper = {};
		this.props.actions.postRESTData(
			{
				method: 'post',
				url: API_URL+"/taskRelations/",
				data: {
					relation: 'dummyWillBeChangedBypostRESTData',
					fromTask: 'dummyWillBeChangedBypostRESTData',
					toTask: 'dummyWillBeChangedBypostRESTData',
					_links: {
						relation: {href: relationType._links.self.href},
						fromTask: {href: this.props.data._links.self.href},
						toTask: {href: subtask._links.self.href}
					}
				}
			},
			this.props.componentPath+'._additionalActionsData.createdSubTasks._embedded.tasks.'+index+'.relations',
			'EmbedTaskContainer.addSubtaskRelation',
			response => (response.data),
			undefined,
			undefined,
			postPromiseWrapper
		);
		postPromiseWrapper.promise
			.then((response) => {
				if(response instanceof Error) {
					this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.errors.tasks."+index+".relations",{error: response.toString(),response: response});
				}
			});
	}

	copyAttachmentsToSubtask(parentAttachments,index) {
		//console.log('copyAttachmentsToSubtask',parentAttachments);
		const subtask = this.props.data._additionalActionsData.createdSubTasks._embedded.tasks[index];
		parentAttachments.forEach((taskAttachment,attachmentIndex) => {
			const attachmentHref = taskAttachment.attachment._links.self.href;
			let postPromiseWrapper = {};
			this.props.actions.postRESTData(
				{
					method: 'post',
					url: API_URL+"/taskAttachments/",
					data: {
						description: taskAttachment.description,
						task: 'dummyWillBeChangedBypostRESTData',
						attachment: 'dummyWillBeChangedBypostRESTData',
						_links: {
							task: {href: subtask._links.self.href},
							attachment: {href: attachmentHref}
						}
					}
				},
				this.props.componentPath+'._additionalActionsData.createdSubTasks._embedded.tasks.'+index+'.attachments.'+attachmentIndex,
				'EmbedTaskContainer.copyAttachmentsToSubtask',
				undefined,
				undefined,
				undefined,
				postPromiseWrapper
			);
			postPromiseWrapper.promise
				.then((response) => {
					if(response instanceof Error) {
						this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.errors.tasks."+index+".attachments."+attachmentIndex,{error: response.toString(),response: response});
					}
				});
		});
	}

	saveMarketingCampaignSubtasks(createdSubTasks) {
		//find for code 2 TASK_RELATION_TYPE_SUBTASK
		getNomenclatureByCode('taskRelationTypes', 2, this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
			.then((typeRelationSubtask) => {
				//get the parent attachments
				let fetchPromiseWrapper = {};
				this.props.actions.fetchRESTFollow(
					{
						url: API_URL+"/taskAttachments/search/findByTask",
						params: {
							task: this.props.data._links.self.href
						}
					},
					this.props.componentPath+"._additionalActionsData.attachments",
					response => ( response.data._embedded.taskAttachments ),
					'EmbedTaskContainer.saveMarketingCampaignSubtasks',
					{attachment: "attachment"},
					fetchPromiseWrapper
				);
				fetchPromiseWrapper.promise
					.then((mergeObj) => {
						let response = mergeObj[0].value;
						if(response instanceof Error) {
							this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.errors.attachments",{error: response.toString(),response: response});
						} else {
							//save the task
							createdSubTasks.forEach((createdSubtask,index) => {
								let postPromiseWrapper = {};
								this.props.actions.postRESTData(
										{
											method: 'post',
											url: API_URL+"/tasks/",
											data: createdSubtask
										},
										this.props.componentPath+'._additionalActionsData.createdSubTasks._embedded.tasks.'+index,
										'EmbedTaskContainer.saveMarketingCampaignSubtasks',
										undefined,
										(data) => {this.addSubtaskRelation(typeRelationSubtask,index); this.copyAttachmentsToSubtask(this.props.data._additionalActionsData.attachments,index);},
										undefined,
										postPromiseWrapper
								);
								postPromiseWrapper.promise
									.then((response) => {
										if(response instanceof Error) {
											this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.errors.tasks."+index,{error: response.toString(),response: response});
										}
									});
							});
						}
					}
				);
			});
	}

	copyPlanToOfferLines() {
		//get the parent attachments
		let fetchPromiseWrapper = {};
		this.props.actions.fetchRESTFollow(
			{
				url: API_URL+"/reports/builder/1",
				params: {
					"from": "PlannedIncomeOrExpense",
					"select": "PlannedIncomeOrExpense,PlannedIncomeOrExpense.article",
					"task.id": this.props.data.id
				}
			},
			this.props.componentPath+"._generatedOfferLines._planned",
			response => ( response.data._embedded.hashMaps ),
			'EmbedTaskContainer.copyPlanToOfferLines',
			{},
			fetchPromiseWrapper
		);
		fetchPromiseWrapper.promise
			.then((mergeObj) => {
				//generate lines
				if(mergeObj[0]) {
					mergeObj[0].value.forEach((plan,index) => {
						if(plan.PlannedIncomeOrExpense.ammount < 0) { //only for expenses, the articles that will go out of warehouse
							let postPromiseWrapper = {};
							this.props.actions.postRESTData(
								{
									method: 'post',
									url: API_URL+"/offerLines/",
									data: {
										offerToClient: this.props.data._generatedOffer._links.self.href,
										article: plan["PlannedIncomeOrExpense.article"]._links.self.href,
										ammount: -plan.PlannedIncomeOrExpense.ammount,
									}
								},
								this.props.componentPath+'._generatedOfferLines.offerLines.'+index,
								'EmbedTaskContainer.copyPlanToOfferLines',
								undefined,
								undefined,
								undefined,
								postPromiseWrapper
							);
						}
					});
				}
				//attach the offer
				let postPromiseWrapper = {};
				this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/taskAttachments/",
						data: {
							description: this.props.t("Task.CreatedOfferDesc"),
							task: 'dummyWillBeChangedBypostRESTData',
							attachment: 'dummyWillBeChangedBypostRESTData',
							_links: {
								task: {href: this.props.data._links.self.href},
								attachment: {href: this.props.data._generatedOffer._links.self.href}
							}
						}
					},
					this.props.componentPath+'._generatedOffer.attachment',
					'EmbedTaskContainer.copyPlanToOfferLines',
					undefined,
					undefined,
					undefined,
					postPromiseWrapper
				);
				postPromiseWrapper.promise
					.then((response) => {
						if(response instanceof Error) {
							this.props.actions.dispatchEditRESTData(this.props.componentPath+"._generatedOffer.attachment.errors",{error: response.toString(),response: response});
						}
					});
			}
		);
	}
	
	createAttestationCampaignSubtasks() {
		//clean the state
		this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.createdSubTasks",undefined);
		//console.log('selectedRows',this.props.data._additionalActionsData.employees.selectedRows);
		if(this.props.data._additionalActionsData
				&& this.props.data._additionalActionsData.employees
				&& this.props.data._additionalActionsData.employees.selectedRows
				&& this.props.data._additionalActionsData.employees.selectedRows.map instanceof Map) {
			//find for code 1 TASK_STATUS_PLANNED
			getNomenclatureByCode('taskStatuses',1,this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
				.then((statusPlanned) => {
					//find for code 11 TASK_TYPE_EMPLOYEE_CAREER_PLAN
					getNomenclatureByCode('taskTypes',11,this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
						.then((typeEmployeeCareerPlan) => {
							let createdSubTasks = [];
							this.props.data._additionalActionsData.employees.selectedRows.map.forEach((value,key) => {
								let newTask = {
										title: this.props.data.title+" - "+value.name,
										description: this.props.data.description,
										deadline: this.props.data.deadline,
										status: statusPlanned,
										type: typeEmployeeCareerPlan,
										_links: {
											status: {href: statusPlanned._links.self.href},
											type: {href: typeEmployeeCareerPlan._links.self.href}
										}
									};
								let watchers = [];
								if(this.props.data && this.props.data.assigned && this.props.data.assigned._links && this.props.data.assigned._links.self && this.props.data.assigned._links.self.href) {
									newTask.assigned = this.props.data.assigned;
									newTask._links.assigned = {href: this.props.data.assigned._links.self.href};
									watchers.push(this.props.data.assigned._links.self.href);
								}
								if(value && value._links && value._links.self && value._links.self.href) {
									watchers.push(value._links.self.href);
								}
								newTask.watchers = watchers;
								createdSubTasks.push(newTask);
							});
							this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.createdSubTasks._embedded.tasks",createdSubTasks);
						});
				});
		}
	}

	saveAttestationCampaignSubtasks(createdSubTasks) {
		//find for code 2 TASK_RELATION_TYPE_SUBTASK
		getNomenclatureByCode('taskRelationTypes', 2, this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
			.then((typeRelationSubtask) => {
				//save the task
				createdSubTasks.forEach((createdSubtask,index) => {
					let postPromiseWrapper = {};
					this.props.actions.postRESTData(
							{
								method: 'post',
								url: API_URL+"/tasks/",
								data: createdSubtask
							},
							this.props.componentPath+'._additionalActionsData.createdSubTasks._embedded.tasks.'+index,
							'EmbedTaskContainer.saveAttestationCampaignSubtasks',
							undefined,
							(data) => {this.addSubtaskRelation(typeRelationSubtask,index);},
							undefined,
							postPromiseWrapper
					);
					postPromiseWrapper.promise
						.then((response) => {
							if(response instanceof Error) {
								this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.errors.tasks."+index,{error: response.toString(),response: response});
							}
						});
				});
			});
	}

	getAdditionalActions() {
		let buttons = [];
		//common
		if(!this.state.editable) {
			buttons.push(<div key="CloneTask" style={{display: 'inline-block'}}>
					<Button variant="outline-dark" onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.cloneData();}}>
						<FontAwesomeIcon icon="copy"/>&nbsp;
						{this.props.t("Task.CloneTask")}
					</Button></div>);
		}
		//type specific
		if(this.props.data && this.props.data.id && this.props.data.type) {
			let typeSpecific = {};
			let panel = '';
			if(this.props.data.type.code == 1) {
				//TASK_TYPE_MARKETING_CAMPAIGN
				if(this.state.typeSpecificStage == 1) {
					const report = {
							label: this.props.t("LegalPerson._className_plural"),
							value: "legalPersons",
							root: "legalPersons",
							expand: ["interests","contacts"],
						};
					let columns = getExpandedColumns(report.root, report.expand);
					panel = <Card><Card.Body>
								<EmbedRetrieveEntityListContainer
									retrieveType = "legalPersons"
									componentPath = {this.props.componentPath+"._additionalActionsData.legalPersons"} //existing path in redux store where we put data
									columns = {columns}
									hasRowSelecting = {true}
									selectedRowsColumns = {[{
											Header: this.props.t("CommonRecord.id"),
											accessor: 'id',
											width: 50
										}, {
											Header: this.props.t("LegalPerson.name"),
											accessor: 'name'
										}]}
									title={this.props.t("LegalPerson._className_plural")}
									expanded={true}
									asTable={true}
									editable={false}
								/>
								<ButtonGroup>
									<Button variant="outline-dark" onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.setState({ typeSpecificStage: 2 }); this.createMarketingCampaignSubtasks();}}>
										{this.props.t("Task.CreateSubtasksForTheSelectedClients")}
									</Button>
								</ButtonGroup>
							</Card.Body></Card>
				}
				if(this.state.typeSpecificStage == 2) {
					const createdSubTasks = (this.props.data
							&& this.props.data._additionalActionsData
							&& this.props.data._additionalActionsData.createdSubTasks
							&& this.props.data._additionalActionsData.createdSubTasks._embedded ? this.props.data._additionalActionsData.createdSubTasks._embedded.tasks : undefined);
					const isCreateSubtasksReady = (createdSubTasks instanceof Array); //TODO check if they are already created
					panel = <Card><Card.Body>
								<EmbedEntityListOrTableContainer
									componentPath = {this.props.componentPath+"._additionalActionsData.createdSubTasks._embedded.tasks"} //redux state path to data array for refresh, new and param to children
									data = {createdSubTasks ? createdSubTasks : []} //array with the data
									loading = {!isCreateSubtasksReady} //Is parent loading? Then wait before retrieving!
									columns = {getEntityDefinition("tasks")}
									asTable = {this.state.asTable}
									editable={false}
								/>
								{this.props.data
									&& this.props.data._additionalActionsData
									&& this.props.data._additionalActionsData.errors ? <pre>{JSON.stringify(this.props.data._additionalActionsData.errors, null, 2) }</pre> : ''}
								<ButtonGroup>
									<Button variant="outline-dark"
										disabled={(isCreateSubtasksReady)}
										onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.saveMarketingCampaignSubtasks(createdSubTasks);}}>
										{this.props.t("Task.SaveCreatedSubtasks")}
									</Button>
								</ButtonGroup>
							</Card.Body></Card>
				}
				typeSpecific.key = "CreateSubtasks";
				typeSpecific.title = this.props.t("Task.CreateSubtasksForClients");
				typeSpecific.onInit = (e) => {};
			} else if(this.props.data.type.code == 3) {
				//TASK_TYPE_REQUEST_FOR_OFFER
				if(this.state.typeSpecificStage == 1) {
					panel = <EmbedOfferToClientContainer
								retrieve_id={this.props.data._generatedOffer && this.props.data._generatedOffer.id !== undefined ? this.props.data._generatedOffer.id : "add"}
								componentPath={this.props.componentPath+"._generatedOffer"}
								onChange={() => {
									if(this.props.data._generatedOffer && this.props.data._generatedOffer.id !== undefined) {
										this.setState({ isModalTypeSpecificOpen: false });
										this.copyPlanToOfferLines();
									}
								}}
							/>;
				}
				typeSpecific.key = "CreateOffer";
				typeSpecific.title = this.props.t("Task.CreateOffer");
				typeSpecific.onInit = (e) => {
						this.props.actions.dispatchEditRESTData(
								this.props.componentPath+"._generatedOffer",
								{
									name: this.props.t("Task.CreatedOfferName") + this.props.data.id,
									person: this.props.data.counterParty,
									validToDate: moment().add(1, 'months').toISOString(),//.format("YYYY/MM/DD HH:mm:ss ZZ"),
									_links: {
										person: {
											self: {
												href: this.props.data._links.counterParty.href,
											},
										},
									},
								}
						);
					};
			} else if(this.props.data.type.code == 10) {
				//TASK_TYPE_ATTESTATION_CAMPAIGN
				if(this.state.typeSpecificStage == 1) {
					const report = {
							label: this.props.t("Employee._className_plural"),
							value: "employees",
							root: "employees",
							expand: ["department","department.parentDepartment"],
						};
					let columns = getExpandedColumns(report.root, report.expand);
					panel = <Card><Card.Body>
								<EmbedRetrieveEntityListContainer
									retrieveType = {report.root}
									componentPath = {this.props.componentPath+"._additionalActionsData."+report.root} //existing path in redux store where we put data
									columns = {columns}
									hasRowSelecting = {true}
									selectedRowsColumns = {[{
											Header: this.props.t("CommonRecord.id"),
											accessor: 'id',
											width: 50
										}, {
											Header: this.props.t("Employee.name"),
											accessor: 'name'
										}]}
									title={report.label}
									expanded={true}
									asTable={true}
									editable={false}
								/>
								<ButtonGroup>
									<Button variant="outline-dark" onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.setState({ typeSpecificStage: 2 }); this.createAttestationCampaignSubtasks();}}>
										{this.props.t("Task.CreateSubtasksForTheSelectedEmployees")}
									</Button>
								</ButtonGroup>
							</Card.Body></Card>
				}
				if(this.state.typeSpecificStage == 2) {
					const createdSubTasks = (this.props.data
							&& this.props.data._additionalActionsData
							&& this.props.data._additionalActionsData.createdSubTasks
							&& this.props.data._additionalActionsData.createdSubTasks._embedded ? this.props.data._additionalActionsData.createdSubTasks._embedded.tasks : undefined);
					const isCreateSubtasksReady = (createdSubTasks instanceof Array); //TODO check if they are already created
					panel = <Card><Card.Body>
								<EmbedEntityListOrTableContainer
									componentPath = {this.props.componentPath+"._additionalActionsData.createdSubTasks._embedded.tasks"} //redux state path to data array for refresh, new and param to children
									data = {createdSubTasks ? createdSubTasks : []} //array with the data
									loading = {!isCreateSubtasksReady} //Is parent loading? Then wait before retrieving!
									columns = {getEntityDefinition("tasks")}
									asTable = {this.state.asTable}
									editable={false}
								/>
								{this.props.data
									&& this.props.data._additionalActionsData
									&& this.props.data._additionalActionsData.errors ? <pre>{JSON.stringify(this.props.data._additionalActionsData.errors, null, 2) }</pre> : ''}
								<ButtonGroup>
									<Button variant="outline-dark"
										disabled={(isCreateSubtasksReady)}
										onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.saveAttestationCampaignSubtasks(createdSubTasks);}}>
										{this.props.t("Task.SaveCreatedSubtasks")}
									</Button>
								</ButtonGroup>
							</Card.Body></Card>
				}
				typeSpecific.key = "CreateAttestationPlans";
				typeSpecific.title = this.props.t("Task.CreateAttestationPlans");
				typeSpecific.onInit = (e) => {};
			}
			if(typeSpecific.key) {
				buttons.push(<div key={typeSpecific.key} style={{display: 'inline-block'}}>
						<Button variant="outline-dark" onClick={(e) => {
								e.stopPropagation();
								typeSpecific.onInit(e);
								this.setState({ isModalTypeSpecificOpen: true });
							}}>
							<FontAwesomeIcon icon="magic"/>&nbsp;
							{typeSpecific.title}
						</Button>
						<Modal open={this.state.isModalTypeSpecificOpen} onClose={() => {this.setState({ isModalTypeSpecificOpen: false, typeSpecificStage: 1 });}}>
							{panel}
						</Modal>
					</div>);
			}
		}
		if(buttons.length == 0) {
			return '';
		} else {
			return <Card key="additionalActions"><Card.Body><ButtonGroup>{buttons}</ButtonGroup></Card.Body></Card>;
		}
	}

	render() {
		let body = '';
		if(this.props.data) {
			if(this.props.loading instanceof Promise) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else if(!this.props.data.id || this.props.data.id == parseInt(this.props.retrieve_id,10)) {
				body = <div>
					<div className='col-sm-12 form-group'>
						<div className="col-sm-4"><label className="contracts-add-form-label">{this.props.t("Task.status")}</label>
						<FieldNomenclatureSelectContainer
							id={'task-add-select-status'}
							href={this.props.statusHref}
							componentPath={this.props.componentPath+'.status'} //existing path in redux store where we put data
							nomenclatureKey={'taskStatuses'}
							editable={this.state.editable}
							onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.status.href',href);}}
						/></div>
						<div className="col-sm-4"><label className="contracts-add-form-label">{this.props.t("Task.type")}</label>
						<FieldNomenclatureSelectContainer
							id={'task-add-select-type'}
							href={this.props.typeHref}
							componentPath={this.props.componentPath+'.type'} //existing path in redux store where we put data
							nomenclatureKey={'taskTypes'}
							editable={this.state.editable}
							onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.type.href',href);}}
						/></div>
						<div className="col-sm-4"><label className="contracts-add-form-label">{this.props.t("Task.priority")}</label>
						<FieldNomenclatureSelectContainer
							id={'task-add-select-type'}
							href={this.props.priorityHref}
							componentPath={this.props.componentPath+'.priority'} //existing path in redux store where we put data
							nomenclatureKey={'taskPriorities'}
							editable={this.state.editable}
							onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.priority.href',href);}}
						/></div>
					</div>
					<div className='col-sm-12 form-group'>
						<div className="col-sm-12"><label className="contracts-add-form-label">{this.props.t("Task.title")}</label>
						<FieldTextContainer
							componentPath={this.props.componentPath+'.title'} //existing path in redux store where we put data
							editable={this.state.editable}
						/></div>
					</div>
					<div className='col-sm-12 form-group'>
						<div className="col-sm-12"><label className="contracts-add-form-label">{this.props.t("Task.description")}</label>
						<FieldTextareaContainer
							componentPath={this.props.componentPath+'.description'} //existing path in redux store where we put data
							editable={this.state.editable}
						/></div>
					</div>
					<div className='col-sm-12 form-group'>
						<div className="col-sm-6"><label className="contracts-add-form-label">{this.props.t("Task.counterParty")}</label>
						<FieldNomenclatureSelectContainer
							id={'task-add-select-counterParty'}
							href={this.props.counterPartyHref}
							componentPath={this.props.componentPath+'.counterParty'} //existing path in redux store where we put data
							editable={this.state.editable}
							nomenclatureKey={"legalPersons"}
							displayAttr={"name"}
							pageURL={"/legalPersons"}
							onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.counterParty.href',href);}}
						/></div>
						<div className="col-sm-6"><label className="contracts-add-form-label">{this.props.t("CommonRecord.createdBy")}</label>
						<FieldNomenclatureSelectContainer
							id={'task-add-select-createdBy'}
							href={this.props.createdByHref}
							componentPath={this.props.componentPath+'.createdBy'} //existing path in redux store where we put data
							nomenclatureKey={'secUsers'}
							displayAttr={"fullName"}
							editable={false}
						/></div>
					</div>
					<div className='col-sm-12 form-group'>
						<div className="col-sm-6"><label className="contracts-add-form-label">{this.props.t("Task.assigned")}</label>
						<FieldNomenclatureSelectContainer
							id={'task-add-select-assigned'}
							href={this.props.assignedHref}
							componentPath={this.props.componentPath+'.assigned'} //existing path in redux store where we put data
							nomenclatureKey={'secUsers'}
							displayAttr={"fullName"}
							editable={this.state.editable}
							onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.assigned.href',href);}}
						/></div>
						<div className="col-sm-6"><label className="contracts-add-form-label">{this.props.t("CommonRecord.createdDate")}</label>
						<FieldTimestampContainer
							componentPath={this.props.componentPath+'.createdDate'} //existing path in redux store where we put data
							editable={false}
						/></div>
					</div>
					<div className='col-sm-12 form-group'>
						<div className="col-sm-6"><label className="contracts-add-form-label">{this.props.t("Task.deadline")}</label>
							<FieldTimestampContainer
							componentPath={this.props.componentPath+'.deadline'} //existing path in redux store where we put data
							editable={this.state.editable}
						/></div>
						<div className="col-sm-6"><label className="contracts-add-form-label">{this.props.t("CommonRecord.lastModifiedDate")}</label>
						<FieldTimestampContainer
							componentPath={this.props.componentPath+'.lastModifiedDate'} //existing path in redux store where we put data
							editable={false}
						/></div>
					</div></div>;
			}
		}
		const additionalActions = this.getAdditionalActions();
		let errorMessage = extractErrorMessage(this.props.error, getEntityDefinition("tasks"));
		const aclDef = getEntityDefinition("accessControls",{commonRecordId: {show: false}}); //TODO put info if the ACL makes the entity public or private
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						{this.props.error ?
							<Alert variant="danger"><FontAwesomeIcon icon="ban"/> {errorMessage}</Alert>
							: ""}
						<ButtonGroup key="taskButtons">
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>{this.props.t("Task._className")}</Button>
							<Button variant="outline-dark" title={this.props.t("ReactTable.New")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.addData();}}><FontAwesomeIcon icon="plus"/></Button>
							{this.state.editable ?
									<Button variant="outline-dark" title={this.props.t("ReactTable.Save")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.saveData();}}><FontAwesomeIcon icon="save"/></Button>
									: <Button variant="outline-dark" title={this.props.t("ReactTable.Edit")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.editData();}}><FontAwesomeIcon icon="edit"/></Button>
							}
							{this.props.retrieve_id == "add" ?
									'' //TODO in the future may be needed a button to load defaults or make calculations?
									: <Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
							}
							<Button variant="outline-dark" title={this.props.t("ReactTable.ACL")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.setState({ isModalACLOpen: true });}}><FontAwesomeIcon icon="lock"/></Button>
							<Modal open={this.state.isModalACLOpen} onClose={() => {this.setState({ isModalACLOpen: false });}} showCloseIcon={true}>
								<EmbedRetrieveEntityListContainer
									title={aclDef.label}
									icon={aclDef.icon}
									columns={aclDef.columns}
									componentPath={this.props.componentPath+".acl"}
									retrieveType="accessControls"
									defaultFilter={[{id: "commonRecordId", value: this.props.data ? this.props.data.id : undefined}]}
									onAddData={(newItem) => {
										if(newItem.commonRecordId == undefined && this.props.data) {
											newItem.commonRecordId = this.props.data.id;
										}
										return newItem;
									}}
									expanded={true}
									asTable={true}
								/>
							</Modal>
						</ButtonGroup>
					</Card.Header>
					<Accordion.Collapse eventKey="0"><Card.Body>
						{body}
						{additionalActions}
					</Card.Body></Accordion.Collapse>
				</Card>
			</Accordion>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	return {
		auth: state.auth,
		//storage
		rest: state.rest,
		data: data,
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		error: data instanceof Error ? data : (data && data[Constants.PATH_FOR_ERROR] instanceof Error ? data[Constants.PATH_FOR_ERROR] : undefined),
		componentPath: ownProps.componentPath,
		//data
		retrieve_id: ownProps.retrieve_id,
		statusHref: data && data._links && data._links.status ? data._links.status.href : undefined,
		typeHref: data && data._links && data._links.type ? data._links.type.href : undefined,
		priorityHref: data && data._links && data._links.priority ? data._links.priority.href : undefined,
		createdByHref: data && data._links && data._links.createdBy ? data._links.createdBy.href : undefined,
		assignedHref: data && data._links && data._links.assigned ? data._links.assigned.href : undefined,
		counterPartyHref: data && data._links && data._links.counterParty ? data._links.counterParty.href : undefined,
		watchersHref: data && data._links && data._links.watchers ? data._links.watchers.href : undefined,
		//UI
		onChange: ownProps.onChange ? ownProps.onChange : () => {}
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedTaskContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
