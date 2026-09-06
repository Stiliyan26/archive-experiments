import React from "react";

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

const LOGIN = "/login"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2_X = "2x"
const PAGE_BODY = "page-body"
const F_CT_BATCH_TYPE_RULES = "fCtBatchTypeRules"
const COA_ID_CT = "coaIdCt"
const COA_ID_DT = "coaIdDt"
const F_CT_JTE_DEFAULTS = "fCtJteDefaults"
const COA_CT = "coaCt"
const COA_DT = "coaDt"
const CHANGE_LOG = ".changelog"
const CLASS_NAME = "._className"
const LENDING = " - Кредитиране"
const COA_ID_CT_F_CT_BATCH_TYPE_RULES = ".coaIdCtfCtBatchTypeRules"
const DEBITING = " - Дебитиране"
const COA_ID_CT_F_DT_BATCH_TYPE_RULES = ".coaIdDtfCtBatchTypeRules"
const COA_CT_F_CT_JTE_DEFAULTS = ".coaCtFCtJteDefaults"
const F_CT_JTE_DEFAULTS_P = ".fCtJteDefaults"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const SLASH = "/"
const DOT = "."
const SLASH_ADD = "/add"
const F_CHART_ACCOUNTS = "fChartAccounts"
const CHART_ACCOUNTS_VIEW = "chartAccountsView"
const F_CHART_ACCOUNT_CLASS_NAME = "FChartAccount._className"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewFChartAccountContainer extends React.Component {	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2_X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const fCtBatchTypeRulesDef = getEntityDefinition(F_CT_BATCH_TYPE_RULES, {});
				const fCtJteDefaultsDef = getEntityDefinition(F_CT_JTE_DEFAULTS, {});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={i18n.t(fCtBatchTypeRulesDef.className+CLASS_NAME)+LENDING}
							icon={fCtBatchTypeRulesDef.icon}
							columns={fCtBatchTypeRulesDef.columns}
							componentPath={this.props.componentPath+COA_ID_CT_F_CT_BATCH_TYPE_RULES}
							retrieveType={F_CT_BATCH_TYPE_RULES}
							parentHref={this.props.href}
							parentAttr={COA_ID_CT}
							parentData={this.props.data}
							asTable={true}
							expanded={false}
	
						 />
						<EmbedRetrieveEntityListContainer
							title={i18n.t(fCtBatchTypeRulesDef.className+CLASS_NAME)+DEBITING}
							icon={fCtBatchTypeRulesDef.icon}
							columns={fCtBatchTypeRulesDef.columns}
							componentPath={this.props.componentPath+COA_ID_CT_F_DT_BATCH_TYPE_RULES}
							retrieveType={F_CT_BATCH_TYPE_RULES}
							parentHref={this.props.href}
							parentAttr={COA_ID_DT}
							parentData={this.props.data}
							asTable={true}
							expanded={false}
	
						 />
						<EmbedRetrieveEntityListContainer
							title={i18n.t(fCtJteDefaultsDef.className+CLASS_NAME)+LENDING}
							icon={fCtJteDefaultsDef.icon}
							columns={fCtJteDefaultsDef.columns}
							componentPath={this.props.componentPath+COA_CT_F_CT_JTE_DEFAULTS}
							retrieveType={F_CT_JTE_DEFAULTS}
							parentHref={this.props.href}
							parentAttr={COA_CT}
							parentData={this.props.data}
							asTable={true}
							expanded={false}
	
						 />
						<EmbedRetrieveEntityListContainer
							title={i18n.t(fCtJteDefaultsDef.className+CLASS_NAME)+DEBITING}
							icon={fCtJteDefaultsDef.icon}
							columns={fCtJteDefaultsDef.columns}
							componentPath={this.props.componentPath+F_CT_JTE_DEFAULTS_P}
							retrieveType={F_CT_JTE_DEFAULTS}
							parentHref={this.props.href}
							parentAttr={COA_DT}
							parentData={this.props.data}
							asTable={true}
							expanded={false}
	
						 />
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+CHANGE_LOG} retrieveType={this.props.entityName}/>
					</div>;
			}
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
					expanded={true}
				/>;
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = F_CHART_ACCOUNTS;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = CHART_ACCOUNTS_VIEW;
	let viewData = resolveObjectPath(componentPath,state.rest);
	let data = undefined;
	if(viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded 
			&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
		data = viewData[entityDef.className]._embedded[entityName][0];
	}
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t(F_CHART_ACCOUNT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewFChartAccountContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
