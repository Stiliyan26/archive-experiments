import React from "react";
import { Button, Form } from "react-bootstrap";
import { patchRESTData, dispatchEditRESTData, resetRESTCallLimit, fetchRESTFollow } from "../../../actions/taskActions";

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome"
import { withTranslation } from "react-i18next";

import * as Constants from "../../../static/constants";
import history from "../../../scripts/history"
import { isAuthenticated } from "../../../components/pages/login/Login.js"

import Header from "../../../components/generic/Header"
import EmbedEntityContainer from "../../embeds/EmbedEntityContainer"
import EmbedChangeHistory from "../../embeds/EmbedChangeHistory"

import { getEntityDefinition } from "../../nomenclatures/entityDefinitions.js"
import { getLoiByCode, resolveObjectPath } from "../../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../../nomenclatures/EmbedRetrieveEntityListContainer"
import i18n from "i18next";
import { showModal, hideModal } from "../../../actions/modal"

const LOGIN = "/login"
const F_PT_JOURNAL_MODAL_CONFIRM_BOOKING = "FPtJournal._modalConfirmBooking"
const F_PT_JOURNAL_MODAL_AMOUNTS_DIFF = "FPtJournal._modalAmountsDiff"
const EQUAL_SLASH_EQUAL = "=/="
const SPACE = " "
const CONFIRMATION_REQUIRED = "ConfirmationRequired"
const YES = "Yes"
const NO = "No"
const LOI_PT_JOURNAL_STATUSES = "loiPtJournalStatuses"
const F_PT_JOURNAL_MODAL_CONFIRM_REJECT = "FPtJournal._modalConfirmReject"
const PAGE_VIEW_F_PT_JOURNAL_CONTAINER_SAVE_DATA = "PageViewFPtJournalContainer.saveData"
const AXIOS = "axios"
const EMPTY = ""
const DOT = "."
const SPINNER = "spinner"
const SIZE_2_X = "2x"
const POST = "post"
const REPORTS_ACCOUNTING_AGGREGATE_JOURNALS_UI = "/reports/accountingAggregateJournalsUi/"
const PATCH = "patch"
const APPLICATION_MERGE_PATCH_JSON = "application/merge-patch+json"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const DOT_ACCOUNTING_AGGREGATE_JOURNALS_UI = ".accountingAggregateJournalsUi"
const DOT_ACCOUNTING_AGGREGATE_JOURNALS_UI_DOT = ".accountingAggregateJournalsUi."
const F_PT_JOURNAL_MODAL_CONFIRM_STORNO = "FPtJournal._modalConfirmStorno"
const REPORTS_STORNO_JOURNAL_UI = "/reports/stornoJournalUi/"
const ZERO = "0"
const DOT_STORNO = ".storno"
const DOT_STORNO_DOT = ".storno."
const F_PT_POSTINGS = "fPtPostings"
const DOT_F_PT_POSTINGS = ".fPtPostings"
const JOL_ID = "jolId"
const PAGE_BODY = "page-body"
const CLASS_NAME = "._className"
const CHANGE_LOG = ".changelog"
const CLASS_NAME_M_2 = "m-2"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const OUT_LINE_DARK = "outline-dark"
const F_PT_JOURNAL_ACTION_BOOK = "FPtJournal._actionBook"
const F_PT_JOURNAL_ACTION_STORNO = "FPtJournal._actionStorno"
const F_PT_JOURNAL_ACTION_REJECT = "FPtJournal._actionReject"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const SLASH = "/"
const SLASH_ADD = "/add"
const BAH_ID = "bahId"
const F_PT_JOURNALS = "fPtJournals"
const PAGE_VIEW_F_PT_JOURNAL_CONTAINER = "PageViewFPtJournalContainer"
const DOT_EMBEDDED_DOT = "._embedded."
const DOT_ZERO = ".0"
const F_PT_JOURNAL_CLASS_NAME = "FPtJournal._className"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewFPtJournalContainer extends React.Component {
	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	setForBooking() {
		//активен само ако е записан реда и статусът е "C" или "V", проверява дали сумата на amount_currency е равна на bah_amount_cuy - "Общата сума на транзакциите e различна от сумата на документа! Желаете ли да продължите?", пита "Сигурни ли сте, че искате да осчетоводите избраната сч. статия ?", сменя status на "V" и записва.
		//console.log("setForBooking",this.props.data);
		let modalBodyText = this.props.t(F_PT_JOURNAL_MODAL_CONFIRM_BOOKING);
		let amountCurrency = this.props.data && this.props.data.amountCurrency ? this.props.data.amountCurrency : 0;
		let bahAmountCurrency = this.props.data.bahId && this.props.data.bahId.amountCurrency ? this.props.data.bahId.amountCurrency : 0;
		if(amountCurrency != bahAmountCurrency) {
			modalBodyText = this.props.t(F_PT_JOURNAL_MODAL_AMOUNTS_DIFF)+amountCurrency+EQUAL_SLASH_EQUAL+bahAmountCurrency+SPACE+modalBodyText;
		}
		this.props.actions.showModal({
			title: this.props.t(CONFIRMATION_REQUIRED),
			body: modalBodyText,
			acceptLabel: this.props.t(YES),
			acceptCallback: () => {
				getLoiByCode(LOI_PT_JOURNAL_STATUSES,2,this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
					.then((statusV) => {
						let dataToSave = this.props.data;
						dataToSave.status = statusV;
						dataToSave._links = {...dataToSave._links,
							status: {href: statusV._links.self.href},
						}
						this.saveData(dataToSave);
					});
					
				this.props.actions.hideModal()
			},
			refuseLabel: this.props.t(NO),
			refuseCallback: () => {
			
				this.props.actions.hideModal()
			},
		});
	}
	
	setRejected() {
		//активен само ако реда е записан, jolStatus е "C" или "S". Пита "Сигурни ли сте, че искате да откажете избраната сч. статия ?". Сменя status на "R" и записва.
		//console.log("setForBooking",this.props.data);
		let modalBodyText = this.props.t(F_PT_JOURNAL_MODAL_CONFIRM_REJECT);
		this.props.actions.showModal({
			title: this.props.t(CONFIRMATION_REQUIRED),
			body: modalBodyText,
			acceptLabel: this.props.t(YES),
			acceptCallback: () => {
				getLoiByCode(LOI_PT_JOURNAL_STATUSES,4,this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
					.then((statusV) => {
						let dataToSave = this.props.data;
						dataToSave.status = statusV;
						dataToSave._links = {...dataToSave._links,
							status: {href: statusV._links.self.href},
						}
						this.saveData(dataToSave);
					});
					
				this.props.actions.hideModal()
			},
			refuseLabel: this.props.t(NO),
			refuseCallback: () => {
			
				this.props.actions.hideModal()
			},
		});
	}

	saveData(dataToSave) {
		//check data availability
		let promiseWrapper = {};
		if(dataToSave) {
			if(dataToSave.id == undefined) {
				this.props.actions.postRESTData(
						{
							method: POST,
							url: API_URL+SLASH+this.props.entityName+SLASH,
							data: dataToSave
						},
						this.props.dataComponentPath,
						PAGE_VIEW_F_PT_JOURNAL_CONTAINER_SAVE_DATA,
						response => (response.data),
						(data) => {
							//console.log("postRESTData onchange");
							//this.props.onChange(data);
							//if(this.state.editable) this.setState({ editable: false });
							//this.props.onCommitChange();
						},
						(data) => {
							//this.props.onBeforeChange(data);
						},
						undefined,
						promiseWrapper
					);
			} else {
				this.props.actions.patchRESTData(
						{
							method: PATCH,
							url: API_URL+SLASH+this.props.entityName+SLASH+dataToSave.id,
							data: dataToSave,
							headers: {"Content-Type": APPLICATION_MERGE_PATCH_JSON} //TODO the literal "Content-Type" cannot be replaced by a constant because it does not work
						},
						this.props.dataComponentPath,
						response => (response.data),
						(data) => {
							//if(this.state.editable) this.setState({ editable: false });
							//this.props.onCommitChange();
						},
						PAGE_VIEW_F_PT_JOURNAL_CONTAINER_SAVE_DATA,
						promiseWrapper
					);
			}
		}
		//call account recalculation
		promiseWrapper.promise.then(() =>
			import(AXIOS).then(axios => {
				let promise = axios({
					method: POST,
					url: API_URL+REPORTS_ACCOUNTING_AGGREGATE_JOURNALS_UI + this.props.data.id,
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_ACCOUNTING_AGGREGATE_JOURNALS_UI,data);
				}).catch(error => {
					console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_ACCOUNTING_AGGREGATE_JOURNALS_UI,error);
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_ACCOUNTING_AGGREGATE_JOURNALS_UI_DOT+Constants.PATH_FOR_LOADING, promise);
			})
		);
	}
	
	storno() {
		//активен само ако реда е записан, jolStatus е "P" и jolStornoId == null. Пита "Сигурни ли сте, че искате да сторнирате избраната сч. статия?". Извиква "select storno_journal_ui(?,?,?);" с параметри jolId, null, null.
		if(this.props.data && this.props.data.id) {
			this.props.actions.showModal({
				title: this.props.t(CONFIRMATION_REQUIRED),
				body: this.props.t(F_PT_JOURNAL_MODAL_CONFIRM_STORNO),
				acceptLabel: this.props.t(YES),
				acceptCallback: () => {
					import(AXIOS).then(axios => {
						let promise = axios({
							method: POST,
							url: API_URL+REPORTS_STORNO_JOURNAL_UI + this.props.data.id + SLASH + ZERO,
							headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
						}).then(response => {
							let data = response.data;
							console.log(data);
							this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_STORNO,data);
						}).catch(error => {
							console.log(error)
							this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_STORNO,error);
						});
						this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_STORNO_DOT+Constants.PATH_FOR_LOADING, promise);
					});
					
					this.props.actions.hideModal()
				},
				refuseLabel: this.props.t(NO),
				refuseCallback: () => {
				
					this.props.actions.hideModal()
				},
			});
		}
	}
	
	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2_X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const fPtPostingsDef = getEntityDefinition(F_PT_POSTINGS, {jolId: {show: false},amountCurrency: {doCalcOnChange: true},amount: {isReadOnly: true},postDate: {show: false},outCode: {show: false},coaStatus: {show: true},ccStatus: {show: false}});
				body = <div className={PAGE_BODY}>
					<EmbedRetrieveEntityListContainer
						title={i18n.t(fPtPostingsDef.className+CLASS_NAME)}
						icon={fPtPostingsDef.icon}
						columns={fPtPostingsDef.columns}
						componentPath={this.props.componentPath+DOT_F_PT_POSTINGS}
						retrieveType={F_PT_POSTINGS}
						parentHref={this.props.href}
						parentAttr={JOL_ID}
						parentData={this.props.data}
						asTable={true}
						expanded={true}
					>
						<Form><Form.Row>
							<Form.Group className={CLASS_NAME_M_2}>
							</Form.Group>
						</Form.Row></Form>
					</EmbedRetrieveEntityListContainer>
					<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+CHANGE_LOG} retrieveType={this.props.entityName}/>
				</div>;
			}
		}
		let button = EMPTY;
		if(this.props.data && this.props.data.id) {
			button = <Form><Form.Row>
				<Form.Group className={CLASS_NAME_M_2}>
					<Button disabled={!(this.props.data && this.props.data.status && (this.props.data.status.listOptionItemCode == 1 || this.props.data.status.listOptionItemCode == 2))} variant={OUT_LINE_DARK} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.setForBooking();}}>{this.props.t(F_PT_JOURNAL_ACTION_BOOK)}</Button>
					<Button disabled={!(this.props.data && this.props.data.status && this.props.data.status.listOptionItemCode == 3 && !(this.props.data.stornoJolId && this.props.data.stornoJolId.id))} variant={OUT_LINE_DARK} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.storno();}}>{this.props.t(F_PT_JOURNAL_ACTION_STORNO)}</Button>
					<Button disabled={!(this.props.data && this.props.data.status && (this.props.data.status.listOptionItemCode == 1 || this.props.data.status.listOptionItemCode == 4))} variant={OUT_LINE_DARK} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.storno();}}>{this.props.t(F_PT_JOURNAL_ACTION_REJECT)}</Button>
				</Form.Group>
			</Form.Row></Form>
		}
		let columnOverride = {};
		columnOverride = {...columnOverride,
				bahId: {show: false},
				journalNo: {isReadOnly: true},
				status: {isReadOnly: true},
				stornoJolId: {isReadOnly: true},
				outCode: {show: false},
				ccStatus: {show: false},
			};
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={this.props.headerText} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<EmbedEntityContainer
					componentPath={this.props.componentPath+DOT+this.props.entityDef.className}
					retrieve_id={this.props.match.params.entity_id}
					onBeforeChange={(data) => {
						if(!this.props.entityDef.pageURL) {console.error(PAGE_URL_IS_MISSING_FOR_ENTITY+this.props.entityDef.className);}
						if(data.id) {
							history.push(this.props.entityDef.pageURL+SLASH+data.id);
						} else {
							history.push(this.props.entityDef.pageURL+SLASH_ADD);
						}}}
					entityName={this.props.entityName}
					headerText={this.props.headerText}
					creatable={false}
					expanded={true}
					columnOverride={columnOverride}
					expandColumns={[BAH_ID]}
				>
					{button}
				</EmbedEntityContainer>
				{body}
			</div>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = F_PT_JOURNALS;
	const entityDef = getEntityDefinition(entityName, undefined);
	const componentPath = PAGE_VIEW_F_PT_JOURNAL_CONTAINER;
	const dataComponentPath = componentPath+DOT+entityDef.className+DOT_EMBEDDED_DOT+entityName+DOT_ZERO;
	let viewData = resolveObjectPath(componentPath,state.rest);
	let data = undefined;
	if(viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded
		&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
		data = viewData[entityDef.className]._embedded[entityName][0];
	}
	return {
		auth: state.auth,
		componentPath: componentPath,
		dataComponentPath: dataComponentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t(F_PT_JOURNAL_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {patchRESTData, dispatchEditRESTData, resetRESTCallLimit, fetchRESTFollow, showModal, hideModal}), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewFPtJournalContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
