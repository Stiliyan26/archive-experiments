import React from "react";
import { Button, Form } from "react-bootstrap";
import { fetchRESTFollow, dispatchEditRESTData, patchRESTData, resetRESTCallLimit, postRESTData } from "../../../actions/taskActions";

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

const LOGIN = "/login"
const EMPTY = ""
const DOT = "."
const SPINNER = "spinner"
const SIZE_2_X = "2x"
const LOI_BRE_TRANSITION_RESULTS = "loiBreTransitionResults"
const POST = "post"
const PAGE_VIEW_F_BRE_TRANSITION_CONTAINER_SAVE_DATA = "PageViewFBreTransitionContainer.saveData"
const PATCH = "patch"
const APPLICATION_MERGE_PATCH_JSON = "application/merge-patch+json"
const F_CT_BATCH_TYPE_RULES = "fCtBatchTypeRules"
const F_CT_BATCH_TTE_TYPES = "fCtBatchTteTypes"
const PAGE_BODY = "page-body"
const CLASS_NAME = "._className"
const F_CT_BATCH_TYPE_RULES_P = ".fCtBatchTypeRules"
const F_CT_BATCH_TTE_TYPES_P = ".fCtBatchTteTypes"
const CHANGE_LOG = ".changelog"
const CLASS_NAME_M_2 = "m-2"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const OUT_LINE_DARK = "outline-dark"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const SLASH = "/"
const SLASH_ADD = "/add"
const EQUAL = "="
const LESS_THAN_OR_EQUAL = "<="
const MORE_THAN_OR_EQUAL = ">="
const OUT_CODE_ID= "outCode.id"
const ACTIVE_FROM_DATE= "activeFromDate"
const ACTIVE_TO_DATE= "activeToDate"
const LOCAL_DATE_LITERAL= "localDateLiteral"
const DEPENDENCE_TYPE_LIST_OPTION_ITEM_CODE= "dependenceType.listOptionItemCode"
const IS_NULL = "isNull"
const DEPENDENCE_TYPE = "dependenceType"
const DEPENDENCE_ID = "dependenceId"
const TTE_ID_ID = "tteId.id"
const F_BRE_TRANSITION_ACTION_SET_FOR_PROCESSING = "FBreTransition._actionSetForProcessing"
const F_BRE_TRANSITION_ACTION_SET_FOR_REJECT = "FBreTransition._actionSetForReject"
const F_BRE_TRANSITIONS = "fBreTransitions"
const PAGE_VIEW_F_BRE_TRANSITION_CONTAINER = "PageViewFBreTransitionContainer"
const EMBEDDED_P = "._embedded."
const DOT_ZERO_P = ".0"
const F_BRE_TRANSITION_CLASS_NAME = "FBreTransition._className"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewFBreTransitionContainer extends React.Component {
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	setForProcessing() {
		let dataToSave = this.props.data;
		getLoiByCode(LOI_BRE_TRANSITION_RESULTS,-1,this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
				.then((resultWaiting) => {
					dataToSave.result = resultWaiting;
					dataToSave._links = {...dataToSave._links,
							result: {href: resultWaiting._links.self.href},
						};
					this.saveData(dataToSave);
				});
	}
	
	setForReject() {
		let dataToSave = this.props.data;
		getLoiByCode(LOI_BRE_TRANSITION_RESULTS,103,this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
				.then((resultRejected) => {
					dataToSave.result = resultRejected;
					dataToSave._links = {...dataToSave._links,
							result: {href: resultWaiting._links.self.href},
						};
					this.saveData(dataToSave);
				});
	}

	saveData(dataToSave) {
		//check data availability
		if(dataToSave) {
			if(dataToSave.id == undefined) {
				this.props.actions.postRESTData(
						{
							method: POST,
							url: API_URL+SLASH+this.props.entityName+SLASH,
							data: dataToSave
						},
						this.props.dataComponentPath,
						PAGE_VIEW_F_BRE_TRANSITION_CONTAINER_SAVE_DATA,
						response => (response.data),
						(data) => {
							//console.log("postRESTData onchange");
							//this.props.onChange(data);
							//if(this.state.editable) this.setState({ editable: false });
							//this.props.onCommitChange();
						},
						(data) => {
							//this.props.onBeforeChange(data);
						}
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
						PAGE_VIEW_F_BRE_TRANSITION_CONTAINER_SAVE_DATA
					);
			}
		}
	}

	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2_X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const fCtBatchTypeRulesDef = getEntityDefinition(F_CT_BATCH_TYPE_RULES, {});
				const fCtBatchTteTypesDef = getEntityDefinition(F_CT_BATCH_TTE_TYPES, {});
				body = <div className={PAGE_BODY}>
					<EmbedRetrieveEntityListContainer
						title={i18n.t(fCtBatchTypeRulesDef.className+CLASS_NAME)}
						icon={fCtBatchTypeRulesDef.icon}
						columns={fCtBatchTypeRulesDef.columns}
						componentPath={this.props.componentPath+F_CT_BATCH_TYPE_RULES_P}
						retrieveType={F_CT_BATCH_TYPE_RULES}
						asTable={true}
						expanded={true}
						defaultFilter={this.props.data && this.props.data.outCode && this.props.data.tteCode ?
						[
							{where: {op: EQUAL, operands: [OUT_CODE_ID, {literal: this.props.data.outCode.id}]}},
							{where: {op: EQUAL, operands: [OUT_CODE_ID, {literal: this.props.data.tteCode.id}]}},
							{where: {op: LESS_THAN_OR_EQUAL, operands: [ACTIVE_FROM_DATE, {op: LOCAL_DATE_LITERAL, operands: [{literal: (new Date(this.props.data.postDate)).toISOString().slice(0,10)}]}]}},
							{where: {op: MORE_THAN_OR_EQUAL, operands: [ACTIVE_TO_DATE, {op: LOCAL_DATE_LITERAL, operands: [{literal: (new Date(this.props.data.postDate)).toISOString().slice(0,10)}]}]}},
							{where: (this.props.data.dependenceType && this.props.data.dependenceType.listOptionItemCode ?
									{op: EQUAL, operands: [DEPENDENCE_TYPE_LIST_OPTION_ITEM_CODE, {literal: this.props.data.dependenceType.listOptionItemCode}]}
									: {op: IS_NULL, operands: [DEPENDENCE_TYPE]}
								)},
							{where: (this.props.data.dependenceId == undefined ?
									{op: IS_NULL, operands: [DEPENDENCE_ID]}
									: {op: EQUAL, operands: [DEPENDENCE_ID, {literal: this.props.data.dependenceId}]}
								)},
						]
						: undefined }
						onAddData={(newItem) => {
							//console.log(this.props.data.tteId);
							newItem.outCode = this.props.data.outCode;
							newItem._links = {...newItem._links, outCode: this.props.data.outCode._links.self};
							newItem.tteId = this.props.data.tteCode;
							newItem._links = {...newItem._links, tteId: this.props.data.tteCode._links.self};
							return newItem;
						}}
					>
						<Form><Form.Row>
							<Form.Group className={CLASS_NAME_M_2}>
							</Form.Group>
						</Form.Row></Form>
					</EmbedRetrieveEntityListContainer>
					<EmbedRetrieveEntityListContainer
						title={i18n.t(fCtBatchTteTypesDef.className+CLASS_NAME)}
						icon={fCtBatchTteTypesDef.icon}
						columns={fCtBatchTteTypesDef.columns}
						componentPath={this.props.componentPath+F_CT_BATCH_TTE_TYPES_P}
						retrieveType={F_CT_BATCH_TTE_TYPES}
						asTable={true}
						expanded={true}
						defaultFilter={this.props.data && this.props.data.outCode && this.props.data.tteCode ?
						[
							{where: {op: EQUAL, operands: [OUT_CODE_ID, {literal: this.props.data.outCode.id}]}},
							{where: {op: EQUAL, operands: [TTE_ID_ID, {literal: this.props.data.tteCode.id}]}},
						]
						: undefined }
					/>
					<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+CHANGE_LOG} retrieveType={this.props.entityName}/>
				</div>;
			}
		}
		let button = EMPTY;
		if(this.props.data && this.props.data.id) {
			button = <Form><Form.Row>
				<Form.Group className={CLASS_NAME_M_2}>
					<Button variant={OUT_LINE_DARK} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.setForProcessing();}} disabled={this.props.data && this.props.data.result == 0}>{this.props.t(F_BRE_TRANSITION_ACTION_SET_FOR_PROCESSING)}</Button>
					<Button variant={OUT_LINE_DARK} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.setForReject();}} disabled={this.props.data && this.props.data.result == 0}>{this.props.t(F_BRE_TRANSITION_ACTION_SET_FOR_REJECT)}</Button>
				</Form.Group>
			</Form.Row></Form>
		}
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
					editable={false}
					deleteable={false}
					expanded={true}
					columnOverride={{
						postDate: {show: false},
						module: {show: false},
						amountOutstanding: {show: false},
						cuyCode: {show: false},
						cuyRate: {show: false},
						dueDate: {show: false},
						ideCode: {show: false},
						addRefNo: {show: false},
						addRefDate: {show: false},
						advance: {show: false},
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
	const entityName = F_BRE_TRANSITIONS;
	const entityDef = getEntityDefinition(entityName, undefined);
	const componentPath = PAGE_VIEW_F_BRE_TRANSITION_CONTAINER;
	const dataComponentPath = componentPath+DOT+entityDef.className+EMBEDDED_P+entityName+DOT_ZERO_P;
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
		headerText: ownProps.t(F_BRE_TRANSITION_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {fetchRESTFollow, postRESTData, dispatchEditRESTData, patchRESTData, resetRESTCallLimit}), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewFBreTransitionContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
