import React from "react";
import { Button, Form } from "react-bootstrap";
import { dispatchEditRESTData, resetRESTCallLimit } from "../../../actions/taskActions";

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
import { resolveObjectPath } from "../../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../../nomenclatures/EmbedRetrieveEntityListContainer"
import i18n from "i18next";
import { showModal, hideModal } from "../../../actions/modal"
import EmbedRestCallButton from "../../embeds/EmbedRestCallButton";

const LOGIN = "/login"
const CONFIRMATION_REQUIRED = "ConfirmationRequired"
const F_PT_BATCH_MODAL_READ_ONLY_AMOUNT_OUTSTANDING = "FPtBatch._modalReadOnlyAmountOutstanding"
const YES = "Yes"
const NO = "No"
const POST = "post"
const AXIOS = "axios"
const REPORTS_BAH_ENFORCE_RULE_UI = "/reports/bahEnforceRulesUi/"
const ZERO = "0"
const ONE = "1"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const DOT_BAH_ENFORCE_RULES_UI = ".bahEnforceRulesUi"
const DOT_BAH_ENFORCE_RULES_UI_DOT = ".bahEnforceRulesUi."
const REPORTS_CREATE_DEFAULT_JOURNALS = "/reports/createDefaultJournals/"
const Y = "Y"
const DOT_CREATE_DEFAULT_JOURNALS = ".createDefaultJournals"
const DOT_CREATE_DEFAULT_JOURNALS_DOT = ".createDefaultJournals."
const EMPTY = ""
const DOT = "."
const SPINNER = "spinner"
const SIZE_2_X = "2x"
const F_PT_BATCH_CC_DETAILS = "fPtBatchCcDetails"
const F_PT_JOURNALS = "fPtJournals"
const DOT_F_PT_BATCH_CC_DETAILS = ".fPtBatchCcDetails"
const BAH_ID = "bahId"
const DOT_RESULT = ".result"
const F_PT_BATCH_ACTION_APPLY_RULE = "FPtBatch._actionApplyRule"
const DOT_F_PT_JOURNALS = ".fPtJournals"
const PAGE_BODY = "page-body"
const DOT_CLASS_NAME = "._className"
const CHANGE_LOG = ".changelog"
const CLASS_NAME_M_2 = "m-2"
const TRUE = "true"
const OUTLINE_DARK = "outline-dark"
const F_PT_BATCH_ACTION_PAY_CASH = "FPtBatch._actionPayCash"
const F_PT_BATCH_ACTION_EDIT_AMOUNT_OUTSTANDING = "FPtBatch._actionEditAmountOutstanding"
const F_PT_BATCH_ACTION_CREATE_DEFAULT_JOURNALS = "FPtBatch._actionCreateDefaultJournals"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const SLASH = "/"
const SLASH_ADD = "/add"
const F_PT_BATCH_MODAL_ACCEPT_DATE_INCONSISTENCY = "FPtBatch._modalAcceptDateInconsistency"
const F_PT_BATCHES = "fPtBatches"
const PAGE_VIEW_PT_BATCH_CONTAINER = "PageViewPtBatchContainer"
const F_PT_BATCH_CLASS_NAME = "FPtBatch._className"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewPtBatchContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			isReadOnlyAmountOutstanding: true,
		};
	}
	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	payCash() {
		//TODO: отваря като модален прозорец "cash-payment-batch.zul" с параметър Id-то на batch-а. Не е активен ако amount_outstanding_currency закръглено до стотинка е 0 или bteId.type = "P".
	}
	
	editAmountOutstanding() {
		//TODO: първо пита след което прави четири полета read-write.
		this.props.actions.showModal({
			title: this.props.t(CONFIRMATION_REQUIRED),
			body: this.props.t(F_PT_BATCH_MODAL_READ_ONLY_AMOUNT_OUTSTANDING),
			acceptLabel: this.props.t(YES),
			acceptCallback: () => {
				this.setState({isReadOnlyAmountOutstanding: false});

				this.props.actions.hideModal()
			},
			refuseLabel: this.props.t(NO),
			refuseCallback: () => {
			
				this.props.actions.hideModal()
			},
		});
	}
	
	applyRule() {
		if(this.props.data && this.props.data.id) {
			import(AXIOS).then(axios => {
				let promise = axios({
					method: POST,
					url: API_URL+REPORTS_BAH_ENFORCE_RULE_UI + this.props.data.id + SLASH + ZERO + SLASH + ONE,
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_BAH_ENFORCE_RULES_UI,data);
				}).catch(error => {
					console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_BAH_ENFORCE_RULES_UI,error);
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_BAH_ENFORCE_RULES_UI_DOT+Constants.PATH_FOR_LOADING, promise);
			});
		}
	}
	
	createDefaultJournals() {
		//TODO:  вижда се само ако няма редове в долния панел. Стартира "sql: select create_default_journals(<<bah.bah_tab_id>>::integer, "Y") ;".
		if(this.props.data && this.props.data.id) {
			import(AXIOS).then(axios => {
				let promise = axios({
					method: POST,
					url: API_URL+REPORTS_CREATE_DEFAULT_JOURNALS + this.props.data.id + SLASH + Y,
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_CREATE_DEFAULT_JOURNALS,data);
				}).catch(error => {
					console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_CREATE_DEFAULT_JOURNALS,error);
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_CREATE_DEFAULT_JOURNALS_DOT+Constants.PATH_FOR_LOADING, promise);
			});
		}
	}
	
	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2_X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const batchCcDetailsDef = getEntityDefinition(F_PT_BATCH_CC_DETAILS, {bahId: {show: false}});
				const fPtJournalDef = getEntityDefinition(F_PT_JOURNALS, {bahId: {show: false}});
				body = <div className={PAGE_BODY}>
					<EmbedRetrieveEntityListContainer
						title={i18n.t(batchCcDetailsDef.className+DOT_CLASS_NAME)}
						icon={batchCcDetailsDef.icon}
						columns={batchCcDetailsDef.columns}
						componentPath={this.props.componentPath+DOT_F_PT_BATCH_CC_DETAILS}
						retrieveType={F_PT_BATCH_CC_DETAILS}
						parentHref={this.props.href}
						parentAttr={BAH_ID}
						parentData={this.props.data}
						asTable={true}
						expanded={true}
					>
						<Form><Form.Row>
							<Form.Group className={CLASS_NAME_M_2}>
								<EmbedRestCallButton componentPath={this.props.componentPath+DOT_RESULT} onClick={(e) => {this.applyRule();}}>{this.props.t(F_PT_BATCH_ACTION_APPLY_RULE)}</EmbedRestCallButton>
							</Form.Group>
						</Form.Row></Form>
					</EmbedRetrieveEntityListContainer>
					<EmbedRetrieveEntityListContainer
						title={i18n.t(fPtJournalDef.className+DOT_CLASS_NAME)}
						icon={fPtJournalDef.icon}
						columns={fPtJournalDef.columns}
						componentPath={this.props.componentPath+DOT_F_PT_JOURNALS}
						retrieveType={F_PT_JOURNALS}
						parentHref={this.props.href}
						parentAttr={BAH_ID}
						parentData={this.props.data}
						asTable={true}
						expanded={true}
					>
						<Form><Form.Row>
							<Form.Group className={CLASS_NAME_M_2}>
								<EmbedRestCallButton disabled={this.props.fPtJournalsData !== undefined} componentPath={this.props.componentPath+DOT_RESULT} onClick={(e) => {this.createDefaultJournals();}}>{this.props.t(F_PT_BATCH_ACTION_CREATE_DEFAULT_JOURNALS)}</EmbedRestCallButton>
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
					<Button disabled={TRUE} variant={OUTLINE_DARK} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.payCash();}}>{this.props.t(F_PT_BATCH_ACTION_PAY_CASH)}</Button>
					<Button variant={OUTLINE_DARK} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.editAmountOutstanding();}}>{this.props.t(F_PT_BATCH_ACTION_EDIT_AMOUNT_OUTSTANDING)}</Button>
				</Form.Group>
			</Form.Row></Form>
		}
		let columnOverride = {};
		if(this.props.data && this.props.data.bteId && this.props.data.bteId.isDds == true) {
			columnOverride = {
					ideId: {isReadOnly: false},
					corrRefNo: {isReadOnly: false},
					salePeriod: {isReadOnly: false},
					purchasePeriod: {isReadOnly: false},
				}
		} else {
			columnOverride = {
					ideId: {isReadOnly: true},
					corrRefNo: {isReadOnly: true},
					salePeriod: {isReadOnly: true},
					purchasePeriod: {isReadOnly: true},
				}
		}
		columnOverride = {...columnOverride,
				cuyUnit: {isReadOnly: true},
				amountOutstandingCurrency: {isReadOnly: true},
				amount2: {isReadOnly: this.state.isReadOnlyAmountOutstanding},
				amount3: {isReadOnly: this.state.isReadOnlyAmountOutstanding},
				amount: {isReadOnly: this.state.isReadOnlyAmountOutstanding},
				amountOutstanding: {isReadOnly: this.state.isReadOnlyAmountOutstanding},
				module: {show: false},
				dfeId: {show: false},
				dfeId2: {show: false},
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
					onConfirmSave={(data) => {
						let refDate = new Date(data.refDate);
						let postDate = new Date(data.postDate);
						if(refDate.getMonth() != postDate.getMonth() || refDate.getFullYear() != postDate.getFullYear()) {
							let promise = new Promise((resolve, reject) => {
								this.props.actions.showModal({
									title: this.props.t(CONFIRMATION_REQUIRED),
									body: this.props.t(F_PT_BATCH_MODAL_ACCEPT_DATE_INCONSISTENCY),
									acceptLabel: this.props.t(YES),
									acceptCallback: () => {
										resolve(data);
				
										this.props.actions.hideModal()
									},
									refuseLabel: this.props.t(NO),
									refuseCallback: () => {
										reject();
									
										this.props.actions.hideModal()
									},
								});
							});
							return promise;
						} else {
							return Promise.resolve(data);
						}
					}}
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
	const entityName = F_PT_BATCHES;
	const entityDef = getEntityDefinition(entityName, undefined);
	const componentPath = PAGE_VIEW_PT_BATCH_CONTAINER;
	let viewData = resolveObjectPath(componentPath,state.rest);
	let data = undefined;
	if(viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded
		&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
		data = viewData[entityDef.className]._embedded[entityName][0];
	}
	let fPtJournalsData = undefined;
	if(viewData && viewData.fPtJournals && viewData.fPtJournals.pageData && viewData.fPtJournals.pageData._embedded
		&& viewData.fPtJournals.pageData._embedded.fPtJournals instanceof Array) {
		fPtJournalsData = viewData.fPtJournals.pageData._embedded.fPtJournals[0];
	}
	
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		fPtJournalsData: fPtJournalsData,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t(F_PT_BATCH_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {dispatchEditRESTData, resetRESTCallLimit, showModal, hideModal}), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewPtBatchContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
