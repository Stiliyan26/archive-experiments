import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter	} from 'react-router-dom'
import { withTranslation } from 'react-i18next';
import axios from 'axios';
import moment from 'moment';

import { Card, ListGroup, Form, FormControl, InputGroup, Button } from 'react-bootstrap';
import Select from 'react-select';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import history from './../scripts/history'
import { resolveObjectPath, getEntityFromURL } from './../scripts/dataUtils';
import { isAuthenticated } from './../components/pages/login/Login.js'
import { dispatchEditRESTData, postRESTData, patchRESTData, deleteREST, resetRESTCallLimit } from './../actions/taskActions';

import { getEntityDefinition, getExpandedColumns } from './nomenclatures/entityDefinitions.js'

import Header from './../components/generic/Header'
import RetrieveDataContainer from './nomenclatures/RetrieveDataContainer'
import EmbedRetrieveEntityListContainer from './nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//Container: redux container class
class PageAllocationContainer extends React.Component {	
	constructor(...args) {
		super(...args);
		this.state = {
			selectedConsumer: {},
			selectedProducer: {},
			selectedAllocation: {},
			selectedAmount: 0,
			showExhaustedProducers: false,
			showExhaustedConsumers: false,
		};
	}
	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}

	saveData(consumer, producer, amount, allocationRecord) {
		let result = Promise.resolve(false);
		//check data availability
		if(this.props.allocationType && consumer.allocationProxies.id && producer.allocationProxies.id) {
			if(allocationRecord == undefined || allocationRecord.id == undefined) {
				let data = {
					allocatedQuantity: amount,
					allocationConsumer: "dummy",
					allocationProducer: "dummy",
					_links: {
						allocationConsumer: {
							href: consumer.allocationProxies._links.self.href,
						},
						allocationProducer: {
							href: producer.allocationProxies._links.self.href,
						},
					}
				};
				this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/allocationRecords",
						data: data
					},
					this.props.componentPath+".allocationRecords",
					'PageAllocationContainer.saveData'
				);
			} else {
				this.props.actions.patchRESTData(
						{
							method: 'patch',
							url: API_URL+"/allocationRecords/"+allocationRecord.id,
							data: {
								allocatedQuantity: amount,
								allocationConsumer: "dummy",
								allocationProducer: "dummy",
								_links: {
									allocationConsumer: {
										href: consumer.allocationProxies._links.self.href,
									},
									allocationProducer: {
										href: producer.allocationProxies._links.self.href,
									},
								},
							},
							headers: {'Content-Type': 'application/merge-patch+json'}
						},
						this.props.componentPath+".allocationRecords",
						response => (undefined),
						(data) => {
						}, //force refresh
						'PageAllocationContainer.saveData'
					);
			}
		}
		return result;
	}

	deleteData(allocation) {
		let promiseWrapper = {};
		this.props.actions.deleteREST({
				method: 'delete',
				url: API_URL+"/allocationRecords/"+allocation.id,
			},
			this.props.componentPath+".allocationRecords",
			promiseWrapper
		);
		promiseWrapper.promise.then((response) => {
			//force refresh
			this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
		});
	}
	
	autoAllocation() {
		let paymentInformation = [];
		if(this.props.producerList instanceof Array) {
			this.props.producerList.forEach((producer) => {
				if(producer.attachableRevenuesAndExpenses) {
					paymentInformation.push({
						"ownerIBAN": "",
						"corespondentIBAN": "",
						"amount": producer.attachableRevenuesAndExpenses.ammount,
						"transactionSide": "C",
						"transactionDate": moment(producer.incomeDate).format("DD.MM.YYYY"),
						"ownerAccountCurrency": producer.attachableRevenuesAndExpenses.article.name,
						"transactionType": "",
						"corespondentName": producer.incomePayer ? producer.incomePayer.name : "",
						"description": producer.name,
						"possibleDocumentNumber": producer.incomeDesc,
					});
				}
			});
		}
		let paymentElements = [];
		//this works only for invoices!
		if(this.props.consumerList instanceof Array) {
			this.props.consumerList.forEach((consumer) => {
				paymentElements.push({
					"ownerIBAN": "",
					"corespondentIBAN": "",
					"amount": consumer.totalAmount,
					"dueDate": moment(consumer.invoiceDate).format("DD.MM.YYYY"),
					"transactionSide": "C",
					"corespondentName": consumer.invoiceCounterParty ? consumer.invoiceCounterParty.name : "",
					"documentNumber": consumer.invoiceCode,
					"ownerAccountCurrency": consumer.invoiceCurrency.name,
				});
			});
		}
		let data = {
				"paymentInformation": paymentInformation,
				"paymentElements": paymentElements,
				"projectName": "LOGISTICS"
			};
		let promise = axios({
				method: 'post',
				url: API_URL+"/service/paymentMatcher",
				headers: {Authorization: sessionStorage["X-AUTH-TOKEN"]},
				data: data
			}).then(response => {
			if(response.data instanceof Array) {
				let promiseArray = [];
				response.data.forEach((payment) => {
					let producerMatched = this.props.producerList.find((producer) => {
						return producer.attachableRevenuesAndExpenses.ammount == payment.amount
							&& producer.incomeDate == payment.transactionDate
							&& producer.attachableRevenuesAndExpenses.article.name == payment.ownerAccountCurrency
							&& (producer.incomePayer ? producer.incomePayer.name : "") == payment.corespondentName
							&& producer.name == payment.description
							&& producer.incomeDesc == payment.possibleDocumentNumber;
					});
					if(producerMatched) {
						console.log("producerMatched",producerMatched,payment);
						let sortedMatches = payment.matches.sort((a,b) => a.matchPercent - b.matchPercent);
						sortedMatches.forEach((match) => {
							console.log("match",match);
							let consumerMatched = this.props.consumerList.find((consumer) => {
								return consumer.totalAmount == match.amount
									&& consumer.invoiceDate == match.dueDate
									&& (consumer.invoiceCounterParty ? consumer.invoiceCounterParty.name : "") == match.corespondentName
									&& consumer.invoiceCode == match.documentNumber
									&& consumer.invoiceCurrency.name == match.ownerAccountCurrency;
							});
							if(consumerMatched) {
								//prepare the data for the allocation record
								let foundAllocation = this.props.allocatedList.find((item) => (item.allocationConsumer && item.allocationConsumer.id == consumerMatched.id && item.allocationProducer && producerMatched.attachableRevenuesAndExpenses && item.allocationProducer.id == producerMatched.attachableRevenuesAndExpenses.id));
								let amount = 0;
									let consumerAmount = consumerMatched.quantityToAllocate == -1 ? undefined : consumerMatched.quantityToAllocate - consumerMatched.totalAllocatedQuantity;
									let producerAmount = (producerMatched.attachableRevenuesAndExpenses ? producerMatched.attachableRevenuesAndExpenses.ammount : 0) - producerMatched.totalAllocatedQuantity;
									if((consumerAmount == undefined || producerAmount < consumerAmount)) {
										amount = producerAmount;
									} else {
										amount = consumerAmount;
									}
								if(amount != 0) {
									consumerMatched.totalAllocatedQuantity = consumerMatched.totalAllocatedQuantity + amount;
									producerMatched.totalAllocatedQuantity = producerMatched.totalAllocatedQuantity + amount;
									//save the generated allocation
									let finalAmount = amount;
									if(foundAllocation && foundAllocation.ammount) {
										finalAmount = amount + foundAllocation.ammount;
									}
									console.log("to save",consumerMatched, producerMatched, finalAmount, foundAllocation);
									promiseArray.push(this.saveData(consumerMatched, producerMatched, finalAmount, foundAllocation ? foundAllocation : {}));
								}
							}
						});
					}
				});
				Promise.all(promiseArray)
				.then((responses) => {
					//force refresh
					this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
				});
			}
		})
		.catch(error => {
			console.error(error);
			//this.props.actions.dispatchEditRESTData(this.props.componentPath, error);
		});
	}

	handleSelect(allocation, producer, consumer) {
		if(allocation) {
			let allocationConsumer = undefined;
			if(this.props.consumerList) {
				allocationConsumer = this.props.consumerList.find((consumerItem) => (allocation && allocation.allocationConsumer && consumerItem.allocationProxies.id == allocation.allocationConsumer.id));
			}
			let allocationProducer = undefined;
			if(this.props.producerList) {
				allocationProducer = this.props.producerList.find((producerItem) => (allocation && allocation.allocationProducer && producerItem.allocationProxies.id == allocation.allocationProducer.id));
			}
			this.setState({
				selectedConsumer: allocationConsumer ? allocationConsumer : {},
				selectedProducer: allocationProducer ? allocationProducer : {},
				selectedAllocation: allocation,
				selectedAmount: allocation.allocatedQuantity ? allocation.allocatedQuantity : 0,
			});
			return;
		}
		if(producer && producer.allocationProxies && consumer && consumer.allocationProxies) {
			//console.log("handleSelect",allocation, producer, consumer);
			let foundAllocation = this.props.allocatedList.find((item) => (item.allocationConsumer && item.allocationConsumer.id == consumer.allocationProxies.id && item.allocationProducer && producer && item.allocationProducer.id == producer.allocationProxies.id));
			let amount = 0;
			if(foundAllocation && foundAllocation.allocatedQuantity) {
				amount = foundAllocation.allocatedQuantity
			} else {
				let consumerAmount = consumer.allocationProxies.quantityToAllocate == -1 ? undefined : consumer.allocationProxies.quantityToAllocate - consumer.allocationProxies.totalAllocatedQuantity;
				let producerAmount = (producer ? producer.allocationProxies.quantityToAllocate : 0) - producer.allocationProxies.totalAllocatedQuantity;
				if((consumerAmount == undefined || producerAmount < consumerAmount)) {
					amount = producerAmount;
				} else {
					amount = consumerAmount;
				}
			}
			this.setState({
				selectedAllocation: foundAllocation ? foundAllocation : {},
				selectedAmount: amount ? amount : 0,
			});
		}
		this.setState({
			selectedConsumer: consumer,
			selectedProducer: producer,
		});
	}
	
	render() {
		let allocationRecordsColumns = getExpandedColumns("allocationRecords",["allocationProducer.allocationType"/*,"allocationProducer.allocationOrigin","allocationConsumer.allocationOrigin"*/],{});
		let allocationRecordsFilter = [{where: {
												op: "equal",
												operands: ["allocationProducer.allocationType.id",{literal: this.props.allocationType ? this.props.allocationType.id : undefined}],
											},
										}];
		if(this.state.selectedProducer && this.state.selectedProducer.allocationProxies && this.state.selectedProducer.allocationProxies.id) {
			allocationRecordsFilter.push({where: {
												op: "equal",
												operands: ["allocationProducer.id",{literal: this.state.selectedProducer.allocationProxies.id}],
											},
										});
		}
		if(this.state.selectedConsumer && this.state.selectedConsumer.allocationProxies && this.state.selectedConsumer.allocationProxies.id) {
			allocationRecordsFilter.push({where: {
												op: "equal",
												operands: ["allocationConsumer.id",{literal: this.state.selectedConsumer.allocationProxies.id}],
											},
										});
		}
		
		let producersFilter = [];
		if(this.props.allocationType) {
			producersFilter.push({where: {op: "equal", operands: ["allocationProxies.allocationType", {literal: this.props.allocationType.id}]}});
		}
		if(this.state.selectedAllocation.allocationProducer) {
			producersFilter.push({where: {op: "equal", operands: ["allocationProxies.id", {literal: this.state.selectedAllocation.allocationProducer.id}]}});
		}
		//TODO these filters are specific for invoices, change them into parameters of the allocation type 
		if(this.props.allocationTypeConsumerArticle && this.state.selectedConsumer.id) {
			producersFilter.push({where: {op: "equal", operands: ["article.id", {literal: resolveObjectPath(this.props.allocationTypeConsumerArticle,this.state.selectedConsumer)}]}});
		}
		//filter for matching the counterparty
		if(this.state.selectedConsumer.id && this.state.selectedConsumer.invoiceCounterParty) {
			producersFilter.push({where: {op: "equal", operands: ["incomePayer.id", {literal: this.state.selectedConsumer.invoiceCounterParty.id}]}});
		}
		
		if(!this.state.showExhaustedProducers) {
			producersFilter.push({where: 
						{op: "isFalse", operands: ["allocationProxies.fullyAllocated"]}
			});
		}
		
		let consumersFilter = [];
		if(this.props.allocationType) {
			consumersFilter.push({where: {op: "equal", operands: ["allocationProxies.allocationType", {literal: this.props.allocationType.id}]}});
		}
		if(this.state.selectedAllocation.allocationConsumer) {
			consumersFilter.push({where: {op: "equal", operands: ["allocationProxies.id", {literal: this.state.selectedAllocation.allocationConsumer.id}]}});
		}
		//TODO these filters are specific for invoices, change them into parameters of the allocation type 
		//filter for matching the counterparty
		if(this.state.selectedProducer.id && this.state.selectedProducer.incomePayer) {
			consumersFilter.push({where: {op: "equal", operands: ["invoiceCounterParty.id", {literal: this.state.selectedProducer.incomePayer.id}]}});
		}
		
		if(!this.state.showExhaustedConsumers) {
			consumersFilter.push({where: 
						{op: "isFalse", operands: ["allocationProxies.fullyAllocated"]}
				});
		}
		
		let consumersColumns = this.props.allocationType ? getExpandedColumns(this.props.allocationType.consumer,["allocationProxies"],{}) : [];
		if( this.props.allocationType && consumersColumns.findIndex((column)=>(column._entityType == "allocationProxies")) < 0 ) {
			console.error("Missing allocationProxies column in consumer entity definition", this.props.allocationType.consumer, consumersColumns);
		}
		let producersColumns = this.props.allocationType ? getExpandedColumns(this.props.allocationType.producer,["allocationProxies"],{}) : [];
		if( this.props.allocationType && producersColumns.findIndex((column)=>(column._entityType == "allocationProxies")) < 0 ) {
			console.error("Missing allocationProxies column in producer entity definition", this.props.allocationType.producer, producersColumns);
		}
		let consumerDef = this.props.allocationType ? getEntityDefinition(this.props.allocationType.consumer) : undefined;
		let producerDef = this.props.allocationType ? getEntityDefinition(this.props.allocationType.producer) : undefined;
		let consumerListItems = this.props.consumerList instanceof Array ? 
				this.props.consumerList.map((consumer, index) => <ListGroup.Item action 
						key={"allocation_consumer_"+index}
						variant={this.state.selectedConsumer.id == consumer.id ? "primary" : undefined}
						onClick={() => {resetRESTCallLimit(); 
							if(this.state.selectedConsumer.id != consumer.id) {
								this.handleSelect(undefined, this.state.selectedProducer, consumer);
							} else {
								this.setState({selectedConsumer: {},selectedAllocation: {}});
							}
						}}
						title={consumerDef.displayFn(consumer)}
					>
						<InputGroup size="sm">
							<InputGroup.Prepend>
								<Button variant="outline-secondary" title={this.props.t("AllocationProxy.fullyAllocated")} onClick={(event) => {event.stopPropagation(); this.deleteData(item);}}><FontAwesomeIcon icon={consumer.allocationProxies.fullyAllocated ? "check" : "times"}/></Button>
							</InputGroup.Prepend>
							<InputGroup.Text>
								{consumer.allocationProxies.totalAllocatedQuantity}/{consumer.allocationProxies.quantityToAllocate != -1 ? consumer.allocationProxies.quantityToAllocate : "\u221E"}
							</InputGroup.Text>
							<InputGroup.Text>
								{consumerDef.displayFn(consumer,{maxChars: 200})}
							</InputGroup.Text>
						</InputGroup>
					</ListGroup.Item>
				)
				: <FontAwesomeIcon icon="spinner" spin/>;
		let producerListItems = this.props.producerList instanceof Array ? 
				this.props.producerList.map((producerItem, index) => <ListGroup.Item action 
						key={"allocation_producer_"+index}
						variant={this.state.selectedProducer.id == producerItem.id ? "primary" : undefined}
						onClick={() => {resetRESTCallLimit(); 
							if(this.state.selectedProducer.id != producerItem.id) {
								this.handleSelect(undefined, producerItem, this.state.selectedConsumer);
							} else {
								this.setState({selectedProducer: {},selectedAllocation: {}});
							}
						}}
						title={producerDef.displayFn(producerItem)}
					>
						<InputGroup size="sm">
							<InputGroup.Prepend>
								<Button variant="outline-secondary" title={this.props.t("AllocationProxy.fullyAllocated")} onClick={(event) => {event.stopPropagation(); this.deleteData(item);}}><FontAwesomeIcon icon={producerItem.allocationProxies.fullyAllocated ? "check" : "times"}/></Button>
							</InputGroup.Prepend>
							<InputGroup.Text>
								{producerItem.allocationProxies.totalAllocatedQuantity}/{producerItem.allocationProxies.quantityToAllocate != -1 ? producerItem.allocationProxies.quantityToAllocate : "\u221E"}
							</InputGroup.Text>
							<InputGroup.Text>
								{producerDef.displayFn(producerItem,{maxChars: 200})}
							</InputGroup.Text>
						</InputGroup>
					</ListGroup.Item>
				)
				: <FontAwesomeIcon icon="spinner" spin/>;
		let allocatedListItems = this.props.allocatedList instanceof Array ? 
				this.props.allocatedList.map((item, index) => {
					let producerColumn = getEntityDefinition(this.props.allocationType.producer);
					let consumerColumn = getEntityDefinition(this.props.allocationType.consumer);
					return <ListGroup.Item action
							key={"allocation_record_"+index}
							variant={this.state.selectedAllocation.id == item.id ? "primary" : undefined}
							onClick={() => {resetRESTCallLimit(); 
								if(this.state.selectedAllocation.id != item.id) {
									this.handleSelect(item);
								} else {
									this.handleSelect({});
								}
							}}
						>
							<InputGroup size="sm">
								<InputGroup.Prepend>
									<InputGroup.Text title={producerColumn.displayFn(item.allocationProducer.allocationOrigin)}>{producerColumn.displayFn(item.allocationProducer.allocationOrigin,{maxChars: 200})}</InputGroup.Text>
								</InputGroup.Prepend>
								<InputGroup.Text>{item.allocatedQuantity}</InputGroup.Text>
								<InputGroup.Append>
									<InputGroup.Text title={consumerColumn.displayFn(item.allocationConsumer.allocationOrigin)}>{consumerColumn.displayFn(item.allocationConsumer.allocationOrigin,{maxChars: 200})}</InputGroup.Text>
								</InputGroup.Append>
								<InputGroup.Append>
									<Button variant="outline-secondary" title={this.props.t("ReactTable.Delete")} onClick={(event) => {event.stopPropagation(); this.deleteData(item);}}><FontAwesomeIcon icon="trash-alt"/></Button>
								</InputGroup.Append>
							</InputGroup>
						</ListGroup.Item>;
				})
				: <FontAwesomeIcon icon="spinner" spin/>;
		let producerText = this.state.selectedProducer.id
				? getEntityDefinition(getEntityFromURL(this.state.selectedProducer._links.self.href)).displayFn(this.state.selectedProducer,{maxChars: 200})
				: "";
		let producerTextAlt = this.state.selectedProducer.id
				? getEntityDefinition(getEntityFromURL(this.state.selectedProducer._links.self.href)).displayFn(this.state.selectedProducer)
				: "";
		let consumerText = this.state.selectedConsumer.id 
				? getEntityDefinition(getEntityFromURL(this.state.selectedConsumer._links.self.href)).displayFn(this.state.selectedConsumer,{maxChars: 200})
				: "";
		let consumerTextAlt = this.state.selectedConsumer.id 
				? getEntityDefinition(getEntityFromURL(this.state.selectedConsumer._links.self.href)).displayFn(this.state.selectedConsumer)
				: "";
		return (
			<div>
				<Header text={this.props.headerText} class='page-header text-align-center no-margin' />
				<Card>
					<Card.Header>
						<label htmlFor="select_allocationType">{this.props.t("Allocation.AllocationType")}</label>
						<Select
							id="select_allocationType"
							name="select_allocationType"
							options={this.props.allocationTypeOptions}
							onChange={(e) => {
								resetRESTCallLimit(); 
								history.push("/allocation/"+e.value);
								this.setState({selectedConsumer: {}, selectedProducer: {}, selectedAllocation: {}, selectedAmount: 0});
							}}
							value={this.props.selectedAllocationTypeOption}
							placeholder={this.props.t("Allocation.ChooseAllocationType")}
							menuPortalTarget={document.body}
						/>
					</Card.Header>
				</Card>
				{this.props.allocationType ?
					<Card>
						<EmbedRetrieveEntityListContainer
							componentPath={this.props.componentPath+".producers"}
							loading={false}
							retrieveType={this.props.allocationType ? this.props.allocationType.producer : ""}
							columns={producersColumns}
							title={this.props.allocationType && producerDef ? producerDef.label_plural : ""}//{"Разпределяне от"}
							expanded={false}
							asTable={true}
							editable={false}
							defaultFilter={producersFilter}
							defaultPageSize={1000}
						/>
						<EmbedRetrieveEntityListContainer
							componentPath={this.props.componentPath+".consumers"}
							loading={false}
							retrieveType={this.props.allocationType ? this.props.allocationType.consumer : ""}
							columns={consumersColumns}
							title={this.props.allocationType && consumerDef ? consumerDef.label_plural : ""}//{"Разпределяне към"}
							expanded={false}
							asTable={true}
							editable={false}
							defaultFilter={consumersFilter}
							defaultPageSize={1000}
						/>
						<EmbedRetrieveEntityListContainer
							componentPath={this.props.componentPath+".allocationRecords"}
							loading={false}
							retrieveType={"allocationRecords"}
							columns={allocationRecordsColumns}
							defaultFilter={allocationRecordsFilter}
							title={this.props.t("AllocationRecord._className_plural")}
							expanded={false}
							asTable={true}
							editable={false}
							onAfterRetrieve={() => {this.handleSelect(undefined, this.state.selectedProducer, this.state.selectedConsumer)}}
							defaultPageSize={20}
						/>
						<Card.Header>
							{this.props.allocationType && this.props.allocationType.consumer == "invoices" ?
								<Button
									variant={"success"}
									onClick={() => {this.autoAllocation();}}
									disabled={false}
								>
									{this.props.t("Allocation.AutoAllocate")}
								</Button>
							: ""}
									<Form>
										<InputGroup>
											<InputGroup.Prepend>
												<InputGroup.Text title={producerTextAlt}>{producerText}</InputGroup.Text>
											</InputGroup.Prepend>
											<FormControl type="number" 
												value={this.state.selectedAmount}
												onChange={(e)=>{this.setState({selectedAmount: e.target.value})}}
											/>
											<InputGroup.Append>
												<InputGroup.Text title={consumerTextAlt}>{consumerText}</InputGroup.Text>
											</InputGroup.Append>
										</InputGroup>
										<Button block
											variant={(this.props.allocationType && this.state.selectedConsumer.id && this.state.selectedProducer.id) ? "success" : "warning"}
											onClick={() => {
												resetRESTCallLimit();
												this.saveData(this.state.selectedConsumer, this.state.selectedProducer, this.state.selectedAmount, this.state.selectedAllocation);
												//force refresh
												this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
											}}
											disabled={!(this.props.allocationType && this.state.selectedConsumer.id && this.state.selectedProducer.id)}
										>
											{this.props.t("Allocation.Allocate")}
										</Button>
									</Form>
						</Card.Header>
						<Card.Body>
							<div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gridGap: 20 }}>
								<div>
									<h3>{this.props.allocationType && producerDef ? producerDef.label_plural : ""}</h3>
									<div className='custom-control custom-switch'>
										<input
											type='checkbox'
											className='custom-control-input'
											id='showExhaustedProducersSwitch'
											checked={this.state.showExhaustedProducers}
											onChange={(e) => {
												resetRESTCallLimit(); 
												this.setState({showExhaustedProducers: !this.state.showExhaustedProducers});
											}}
										/>
										<label className='custom-control-label' htmlFor='showExhaustedProducersSwitch'>{this.props.t("Allocation.showExhaustedProducers")}</label>
									</div>
								</div>
								<div>
								</div>
								<div>
									<h3>{this.props.allocationType && consumerDef ? consumerDef.label_plural : ""}</h3>
									<div className='custom-control custom-switch'>
										<input
											type='checkbox'
											className='custom-control-input'
											id='showExhaustedConsumersSwitch'
											checked={this.state.showExhaustedConsumers}
											onChange={(e) => {
												resetRESTCallLimit(); 
												this.setState({showExhaustedConsumers: !this.state.showExhaustedConsumers});
											}}
										/>
										<label className='custom-control-label' htmlFor='showExhaustedConsumersSwitch'>{this.props.t("Allocation.showExhaustedConsumers")}</label>
									</div>
								</div>
								<ListGroup>{producerListItems}</ListGroup>
								<ListGroup>{allocatedListItems}</ListGroup>
								<ListGroup>{consumerListItems}</ListGroup>
							</div>
						</Card.Body>
					</Card>
				: ""}
				<RetrieveDataContainer
					componentPath={this.props.componentPath+".allocationTypes"+".pageData"}
					loading={false}
					retrieveType={"allocationTypes"}
					expandColumns={undefined}
					defaultPageSize={20}
					defaultSort={[{id: "name", desc: false}]}
					defaultFilter={[{}]}
				/>
			</div>
			);
	}
}
//TODO why duplicate rows after second import of payments

//redux mapping
function mapStateToProps(state,ownProps) {
	const componentPath = "allocation."+ownProps.match.params.selected;
	const data = resolveObjectPath(componentPath,state.rest);
	const allocationTypesList = data && data.allocationTypes && data.allocationTypes.pageData && data.allocationTypes.pageData._embedded ?	data.allocationTypes.pageData._embedded.allocationTypes : undefined;
	const allocationTypeOptions = allocationTypesList instanceof Array ?
		allocationTypesList.map((item, index) => ({
			value: item.id,
			label: item["name"],
			index: index
		}))
		: undefined;
	const selectedAllocationTypeOption = allocationTypeOptions ? allocationTypeOptions.find((item) => item.value == ownProps.match.params.selected) : undefined;
	const allocationType = allocationTypesList ? allocationTypesList.find((item) => item.id == ownProps.match.params.selected) : undefined;
	let allocationTypeConsumerArticle = undefined;
	if(allocationType && allocationType.consumer == "invoices") {
		allocationTypeConsumerArticle = "invoiceCurrency.id";
	}
	const consumerList = allocationType && data && data.consumers && data.consumers.pageData && data.consumers.pageData._embedded ? data.consumers.pageData._embedded[allocationType.consumer] : undefined;
	const producerList = allocationType && data && data.producers && data.producers.pageData && data.producers.pageData._embedded ? data.producers.pageData._embedded[allocationType.producer] : undefined;
	const allocatedList = data && data.allocationRecords && data.allocationRecords.pageData && data.allocationRecords.pageData._embedded ? data.allocationRecords.pageData._embedded.allocationRecords : undefined;
	return {
		auth: state.auth,
		componentPath: componentPath,
		data: data,
		//UI
		headerText: ownProps.t("Allocation.title"),
		//refresh-related
		selectedAllocationTypeOption: selectedAllocationTypeOption,
		allocationTypeOptions: allocationTypeOptions,
		allocationTypesList: allocationTypesList,
		allocationType: allocationType,
		allocationTypeConsumerArticle: allocationTypeConsumerArticle,
		consumerList: consumerList,
		producerList: producerList,
		allocatedList: allocatedList,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData, postRESTData, patchRESTData, deleteREST }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageAllocationContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
