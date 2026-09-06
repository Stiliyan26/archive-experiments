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
import i18n from "i18next";
import EmbedRetrieveEntityListContainer from "../../nomenclatures/EmbedRetrieveEntityListContainer";

const LOGIN = "/login"
const EMPTY = ""
const DOT = "."
const SPINNER = "spinner"
const SIZE_2_X = "2x"
const F_CT_BATCH_TYPE_RULES = "fCtBatchTypeRules"
const F_CT_BATCH_TTE_TYPES = "fCtBatchTteTypes"
const F_CT_BATCH_TTE_LINKS = "fCtBatchTteLinks"
const TTE_ID = "tteId"
const PAGE_BODY = "page-body"
const CLASS_NAME = "._className"
const F_CT_BATCH_TYPE_RULES_P = ".fCtBatchTypeRules"
const F_CT_BATCH_TTE_TYPES_P = ".fCtBatchTteTypes"
const F_CT_JTE_DEFAULTS_P = ".fCtJteDefaults"
const CHANGE_LOG = ".changelog"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const SLASH = "/"
const SLASH_ADD = "/add"
const F_CT_TRANSITION_TYPES = "fCtTransitionTypes"
const F_CT_TRANSITION_TYPES_VIEW = "fCtTransitionTypesView"
const F_CT_TRANSITION_TYPE_CLASS_NAME = "FCtTransitionType._className"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewCtTransitionTypeContainer extends React.Component {
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
				const fCtBatchTypeRulesDef = getEntityDefinition(F_CT_BATCH_TYPE_RULES, {tteId: {show: false}});
				const ctBatchTteTypesDef = getEntityDefinition(F_CT_BATCH_TTE_TYPES, {tteId: {show: false}});
				const ctBatchTteLinksDef = getEntityDefinition(F_CT_BATCH_TTE_LINKS, {tteId: {show: false},ideId: {show: false},status: {show: false}});
				body = <div className={PAGE_BODY}>
					<EmbedRetrieveEntityListContainer
						title={i18n.t(fCtBatchTypeRulesDef.className+CLASS_NAME)}
						icon={fCtBatchTypeRulesDef.icon}
						columns={fCtBatchTypeRulesDef.columns}
						componentPath={this.props.componentPath+F_CT_BATCH_TYPE_RULES_P}
						retrieveType={F_CT_BATCH_TYPE_RULES}
						parentHref={this.props.href}
						parentAttr={TTE_ID}
						parentData={this.props.data}
						asTable={true}
						expanded={true}
					/>
					<EmbedRetrieveEntityListContainer
						title={i18n.t(ctBatchTteTypesDef.className+CLASS_NAME)}
						icon={ctBatchTteTypesDef.icon}
						columns={ctBatchTteTypesDef.columns}
						componentPath={this.props.componentPath+F_CT_BATCH_TTE_TYPES_P}
						retrieveType={F_CT_BATCH_TTE_TYPES}
						parentHref={this.props.href}
						parentAttr={TTE_ID}
						parentData={this.props.data}
						asTable={true}
						expanded={true}
					/>
					<EmbedRetrieveEntityListContainer
						title={i18n.t(ctBatchTteLinksDef.className+CLASS_NAME)}
						icon={ctBatchTteLinksDef.icon}
						columns={ctBatchTteLinksDef.columns}
						componentPath={this.props.componentPath+F_CT_JTE_DEFAULTS_P}
						retrieveType={F_CT_BATCH_TTE_LINKS}
						parentHref={this.props.href}
						parentAttr={TTE_ID}
						parentData={this.props.data}
						asTable={true}
						expanded={true}
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
						if(!this.props.entityDef.pageURL) {console.error({PAGE_URL_IS_MISSING_FOR_ENTITY}+this.props.entityDef.className);}
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
	const entityName = F_CT_TRANSITION_TYPES;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = F_CT_TRANSITION_TYPES_VIEW;
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
		headerText: ownProps.t(F_CT_TRANSITION_TYPE_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewCtTransitionTypeContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
