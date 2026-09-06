import React from 'react';
import moment from 'moment'
moment.locale('bg')
import Modal from 'react-responsive-modal';
import ReactToPrint from "react-to-print";

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Accordion, Card, ButtonGroup, Button, Alert } from 'react-bootstrap';

import Switch from 'rc-switch';
// import "rc-switch/dist/switch.css"

import * as Constants from './../../static/constants';
import history from './../../scripts/history'
import { ContextAwareToggle } from './../../scripts/util'
import { isAuthenticated } from './../../components/pages/login/Login.js'

import FieldNomenclatureSelectContainer from './../fields/FieldNomenclatureSelectContainer'
import FieldTextContainer from './../fields/FieldTextContainer'
import FieldTextareaContainer from './../fields/FieldTextareaContainer'
import FieldTimestampContainer from './../fields/FieldTimestampContainer'
import FieldUnitContainer from './../fields/FieldUnitContainer'
import OfferTemplate from './../../components/shared/OfferTemplate'

import { builderDataToProjection, getEntityDefinition } from './../nomenclatures/entityDefinitions.js'
import { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath, extractErrorMessage } from './../../scripts/dataUtils';

class EmbedOfferToClientContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			editable: false,
			isModalPrintOpen: false,
			printLanguage: 'bg',
			hasShowDiscounts: false,
			open: this.props.expanded !== undefined ? this.props.expanded : false,
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
				const urlWithoutId = API_URL+"/reports/builder/1?from=OfferToClient&select=OfferToClient,OfferToClient.currency,OfferToClient.person,OfferToClient.company&OfferToClient.id=";
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
							builderDataToProjection(newData,"offerToClients");
							newData = newData._embedded.offerToClients[0];
							if(newData == undefined) {
								newData = new Error("Офертата не е намерена");
							}
							return newData;
						},
						'EmbedOfferToClientContainer.retrieveData',
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
							url: API_URL+"/offerToClients/",
							data: this.props.data
						},
						this.props.componentPath,
						'EmbedOfferToClientContainer.saveData',
						undefined,
						(data) => {this.props.onChange(data);}
					);
			} else {
				this.props.actions.patchRESTData(
						{
							method: 'patch',
							url: API_URL+"/offerToClients/"+this.props.data.id,
							data: this.props.data,
							headers: {'Content-Type': 'application/merge-patch+json'}
						},
						this.props.componentPath,
						response => (response.data),
						(data) => {},
						'EmbedOfferToClientContainer.saveData'
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
		this.props.actions.dispatchEditRESTData(this.props.componentPath,{
			vatPercent: 20,
			validToDate: moment().add(1, 'months').toISOString(),
		});
		this.setState({ editable: true });
		this.props.onChange({});
	}

	cloneData() {
		if(this.props.data.id == undefined) {
			return;
		}
		//TODO modal confirmation
		let newData = Object.assign({},this.props.data);
		newData._links = Object.assign({},this.props.data._links);
		const oldId = this.props.data.id;
		newData.originalOffer = this.props.data._links.self.href;
		newData._links.originalOffer.href = this.props.data._links.self.href;
		delete newData.id;
		delete newData._links.self;
		delete newData.offerCode;
		delete newData.name;
		delete newData.revision;
		delete newData.foreignId;
		let promiseWrapper = {};
		if(newData) {
			this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/offerToClients/",
						data: newData
					},
					this.props.componentPath,
					'EmbedOfferToClientContainer.cloneData',
					undefined,
					(data) => {this.props.onChange(data);},
					undefined,
					promiseWrapper
				);
		}
		promiseWrapper.promise.then((newOfferResponse) => {
			//get the offer lines
			let fetchPromiseWrapper = {};
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/builder/1",
					params: {
						"from": "OfferLine",
						"select": "OfferLine,OfferLine.article",
						"OfferLine.offerToClient.id": oldId,
					}
				},
				this.props.componentPath+"._additionalActionsData.offerLines",
				response => (response.data),
				'EmbedOfferToClientContainer.cloneData',
				{},
				fetchPromiseWrapper
			);
			fetchPromiseWrapper.promise
				.then((response) => {
					let offerLines = response[0].value;
					//transform data to "repository response"-like
					builderDataToProjection(offerLines,"offerLines");
					offerLines = offerLines._embedded.offerLines;
					//save the task
					offerLines.forEach((offerLine,index) => {
						delete offerLine.id;
						delete offerLine._links.self;
						offerLine.offerToClient = newOfferResponse.data._links.self.href;
						offerLine._links.offerToClient.href = newOfferResponse.data._links.self.href;
						let postPromiseWrapper = {};
						this.props.actions.postRESTData(
								{
									method: 'post',
									url: API_URL+"/offerLines/",
									data: offerLine
								},
								this.props.componentPath+'._additionalActionsData.offerLines._embedded.offerLines.'+index,
								'EmbedOfferToClientContainer.cloneData',
								undefined,
								undefined,
								undefined,
								postPromiseWrapper
						);
						postPromiseWrapper.promise
							.then((response) => {
								if(response instanceof Error) {
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._additionalActionsData.errors.offerLines."+index,{error: response.toString(),response: response});
								}
							});
					});
				}
			);
		});
		this.setState({ editable: true });
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		} else {
			this.retrieveData();
		}
	}

	componentDidUpdate(prevProps, prevState) {
		const { currentOfferLines } = this.props

		this.retrieveData();
	}

	fetchOfferLines(){
		let self = this
		let checkInterval = setInterval(() => {
			let offer = self.props.data
			if(offer){
				clearInterval(checkInterval)
				self.props.actions.fetchRESTFollow(
					{
						url: `${API_URL}/reports/builder/1?from=OfferLine&select=OfferLine,OfferLine.article&OfferLine.offerToClient.id=${offer.id}&page=0&size=5000`,
					},
					'currentOfferLines',
					response => {
						let data = response.data;
						if(!data._embedded){
							throw new Error('Not authorized to print an offer, please log out and log in again.')
						}
						let offerLines = data._embedded.hashMaps.map((hashMap) => {
							let offerLine = hashMap.OfferLine
							offerLine.article = hashMap['OfferLine.article']
							return offerLine
						})
						return offerLines
					},
					'EmbedOfferToClientContainer.fetchOfferLines',
				)
			}
		}, 200)
	}
	
	changeLanguage(value) {
		this.setState({
			printLanguage: value ? 'en' : 'bg'
		})
	}
	
	sendOrder() {
		if(this.props.data && this.props.data.id !== undefined) {
			let fetchPromiseWrapper = {};
			this.props.actions.fetchRESTFollow(
					{
						url: API_URL+"/wsproxy/SetOrderForSell/"+this.props.data.id,
					},
					this.props.componentPath+".foreignId",
					response => (response.data),
					'EmbedOfferToClientContainer.sendOrder',
					{},
					fetchPromiseWrapper
				);
			//fetchPromiseWrapper.promise.then((response) => {this.saveData();});
		}
	}
	
	render() {
		const { data, currentOfferLines } = this.props
		let foreignId = (this.props.foreignId instanceof Error ? this.props.foreignId.response.data : this.props.foreignId);
		let foreignIdError = undefined;
		if(foreignId && foreignId.error == "notNull"){
			foreignIdError = "Липсват данни! Попълнете липсващото поле: ";
			switch(foreignId.field) {
				case "OfferToClient.company.code":
					foreignIdError = foreignIdError + "Код на фирма в настройките за многофирмена работа";
					break;
				case "OfferToClient.offerCode":
					foreignIdError = foreignIdError + "Номер на оферта";
					break;
				case "OfferToClient.lastModifiedDate":
					foreignIdError = foreignIdError + "Дата на оферта";
					break;
				case "OfferToClient.company.storeId":
					foreignIdError = foreignIdError + "Код на склад в настройките за многофирмена работа";
					break;
				case "OfferToClient.person.importedLegalPerson":
					foreignIdError = foreignIdError + "Контрагент в счетоводството";
					break;
				case "OfferToClient.vatPercent":
					foreignIdError = foreignIdError + "ДДС %";
					break;
				case "OfferLine.article.foreignId":
					foreignIdError = foreignIdError + "Код на артикул в счетоводството";
					break;
				case "OfferLine.ammount":
					foreignIdError = foreignIdError + "Сума на реда";
					break;
				case "OfferLine.article.measureForeignId":
					foreignIdError = foreignIdError + "Код на мярка в счетоводството";
					break;
				case "OfferLine.price":
					foreignIdError = foreignIdError + "Цена на реда";
					break;
				case "OfferToClient.currency.foreignId":
					foreignIdError = foreignIdError + "Код на валутата в счетоводството";
					break;
				default:
					foreignIdError = foreignIdError + foreignId.field;
			}
		}
		if(foreignId && foreignId.errorCode != undefined) {
			foreignIdError = "Грешка при приемане на заявката: "+foreignId.errorCode+" "+foreignId.errorMessage;
		}
		let body = '';
		if(data) {
			if(this.props.loading instanceof Promise) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else if(!data.id || data.id == parseInt(this.props.retrieve_id,10)) {
				body =
					<div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-4">
								<label className="contracts-add-form-label">Номер</label>
								<FieldTextContainer
									componentPath={this.props.componentPath+'.offerCode'}
									editable={false}
								/>
							</div>
							<div className="col-sm-4">
								<label className="contracts-add-form-label">Заглавие</label>
								<FieldTextContainer
									componentPath={this.props.componentPath+'.name'}
									editable={this.state.editable}
								/>
							</div>
							<div className="col-sm-4">
								<label className="contracts-add-form-label">Контрагент <span className="text-red">*</span></label>
								<FieldNomenclatureSelectContainer
									href={this.props.personHref} //REST URI to load from
									componentPath={this.props.componentPath+'.person'} //existing path in redux store where we put data
									nomenclatureKey={'legalPersons'}
									editable={this.state.editable}
									pageURL={"/legalPersons"}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.person.href',href);}}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-2">
								<label className="contracts-add-form-label">Ревизия</label>
								<FieldUnitContainer
									componentPath={this.props.componentPath+'.revision'}
									editable={false}
								/>
							</div>
							<div className="col-sm-4">
								<label className="contracts-add-form-label">Оригинална оферта</label>
								<FieldNomenclatureSelectContainer
									href={this.props.originalOfferHref} //REST URI to load from
									componentPath={this.props.componentPath+'.originalOffer'} //existing path in redux store where we put data
									nomenclatureKey={'offerToClients'}
									editable={false}
									pageURL={"/offerToClients"}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.originalOffer.href',href);}}
								/>
							</div>
							<div className="col-sm-3">
								{!this.state.editable
									? <Button variant="outline-dark" onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.cloneData();}}>Нова ревизия</Button>
									: undefined
								}
							</div>
							<div className="col-sm-3">
								{foreignId != undefined 
									? (foreignIdError != undefined
											? <Alert variant="danger">{foreignIdError}</Alert> 
											: "Изпратена заявка с идентификатор " + foreignId)
									:<Button variant="outline-dark" onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.sendOrder();}}>Изпрати заявка</Button>
								}
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-4">
								<label className="contracts-add-form-label">Банкова сметка</label>
								<FieldNomenclatureSelectContainer
									href={this.props.bankAccountHref} //REST URI to load from
									componentPath={this.props.componentPath+'.bankAccount'} //existing path in redux store where we put data
									nomenclatureKey={'bankAccounts'}
									displayAttr={"description"}
									editable={this.state.editable}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.bankAccount.href',href);}}
								/>
							</div>
							<div className="col-sm-2">
								<label className="contracts-add-form-label">Валута <span className="text-red">*</span></label>
								<FieldNomenclatureSelectContainer
									href={this.props.currencyHref} //REST URI to load from
									componentPath={this.props.componentPath+'.currency'} //existing path in redux store where we put data
									nomenclatureKey={'currencies'}
									editable={this.state.editable}
									onChange={(href) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+'._links.currency.href',href);}}
								/>
							</div>
							<div className="col-sm-2">
								<label className="contracts-add-form-label">ДДС %</label>
								<FieldUnitContainer
									componentPath={this.props.componentPath+'.vatPercent'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
							<div className="col-sm-2">
								<label className="contracts-add-form-label">Търговска отстъпка %</label>
								<FieldUnitContainer
									componentPath={this.props.componentPath+'.discountPercent'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
							<div className="col-sm-2">
								<label className="contracts-add-form-label">Краен срок на офертата</label>
								<FieldTimestampContainer
									componentPath={this.props.componentPath+'.validToDate'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Условия за отстъпка</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.discountCondition'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Условия на доставка</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.deliveryTerms'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Гаранционен срок</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.guaranteeTerms'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-12">
								<label className="contracts-add-form-label">Забележки</label>
								<FieldTextareaContainer
									componentPath={this.props.componentPath+'.notes'} //existing path in redux store where we put data
									editable={this.state.editable}
								/>
							</div>
						</div>
					</div>;
			}
		}
		//TODO add button to clone the offer (not revision)
		let errorMessage = extractErrorMessage(this.props.error, getEntityDefinition("offerToClients"));
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						{this.props.error ?
							<Alert variant="danger">Грешка: {errorMessage}</Alert>
							: ""}
						<ButtonGroup>
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>Оферта към клиент</Button>
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
							<Button variant="outline-dark" disabled={!data} title={this.props.t("Print")} onClick={(e) => {
								e.stopPropagation(); 
								resetRESTCallLimit(); 
								this.fetchOfferLines();
								this.setState({ isModalPrintOpen: true }); 
							}}><FontAwesomeIcon icon='print'/></Button>
						</ButtonGroup>
					</Card.Header>
					<Accordion.Collapse eventKey="0"><Card.Body>
						{body}
					</Card.Body></Accordion.Collapse>
					<Modal open={this.state.isModalPrintOpen} onClose={() => {this.setState({ isModalPrintOpen: false });}} showCloseIcon={true}>
						<div className="col-sm-6 form-group">
							<Button variant={(currentOfferLines instanceof Array ? "success" : "warning")} disabled={(currentOfferLines instanceof Array)}
								onClick={currentOfferLines instanceof Array ? () => {
									resetRESTCallLimit();
									if(currentOfferLines instanceof Array){
										// timeout set to 1000ms in order to be sure that the shadow table has rendered before the print is triggered
										setTimeout(() => {
											this.printTriggerWrapper.handlePrint()
										}, 1000);
									}
								} : undefined}
							>
								<FontAwesomeIcon icon="print"/>
								&nbsp;{ currentOfferLines instanceof Array ? this.props.t("Print") : "Изчакване на данните..." }
							</Button>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-6">Език:</div>
							<div className="col-sm-6">
								<Switch
									id="language"
									onChange={(e) => this.changeLanguage(e)}
									checkedChildren={'EN'}
									unCheckedChildren={'BG'}
								/>
							</div>
						</div>
						<div className='col-sm-12 form-group'>
							<div className="col-sm-6">Покажи отстъпките:</div>
							<div className="col-sm-6">
								<input id="hasShowDiscounts" type="checkbox" 
									checked={this.state.hasShowDiscounts} 
									onChange={(e) => this.setState({ hasShowDiscounts: e.target.checked })}
								/>
							</div>
						</div>
					
						<OfferTemplate
							className="invisible-table"
							offer={data}
							offerLines={currentOfferLines}
							ref={(el) => (this.tableRef = el)}
							lang={this.state.printLanguage}
							hasShowDiscounts={this.state.hasShowDiscounts}
							companyCodeArg={data && data.company ? data.company.code : 0}
						/>
						<ReactToPrint
								ref={(el) => (this.printTriggerWrapper = el)}
								// the trigger below contains a HIDDEN button which gets 'pushed' indirectly by the 'enablePrint' func
								trigger={() => <Button disabled={!data} ref={(el) => (this.printTrigger = el)} size="lg" className='hidden'><FontAwesomeIcon icon='print'/></Button>}
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
					</Modal>
				</Card>
			</Accordion>

		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST

	if(data && !data.processedBy){
		data.processedBy = resolveObjectPath("currentUser._embedded.secUsers.0.fullName",state.rest);
	}
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
		bankAccountHref: data && data._links && data._links.bankAccount ? data._links.bankAccount.href : undefined,
		currencyHref: data && data._links && data._links.currency ? data._links.currency.href : undefined,
		originalOfferHref: data && data._links && data._links.originalOffer ? data._links.originalOffer.href : undefined,
		foreignId: data && data.foreignId ? data.foreignId : undefined,
		//UI
		creatable: ownProps.creatable != undefined ? ownProps.creatable : true,
		onChange: ownProps.onChange ? ownProps.onChange : () => {},
		currentOfferLines: state.rest.currentOfferLines
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedOfferToClientContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
