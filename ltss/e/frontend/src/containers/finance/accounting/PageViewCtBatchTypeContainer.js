import React from "react";
import { Form } from "react-bootstrap";
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

const LOGIN = "/login"
const EMPTY = ""
const DOT = "."
const SPINNER = "spinner"
const SIZE_2_X = "2x"
const PAGE_BODY = "page-body"
const F_CT_BATCH_JTE_DEFAULTS = "fCtBatchJteDefaults"
const F_CT_BATCH_JTE_DEFAULTS_P = ".fCtBatchJteDefaults"
const BTE_ID = "bteId"
const CHANGE_LOG = ".changelog"
const CLASS_NAME = "._className"
const CLASS_NAME_M_2 = "m-2"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const SLASH = "/"
const SLASH_ADD = "/add"
const F_CT_BATCH_TYPES = "fCtBatchTypes"
const PAGE_VIEW_CT_BATCH_TYPE_CONTAINER = "PageViewCtBatchTypeContainer"
const F_BATCH_TYPE_CLASS_NAME = "FCtBatchType._className"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewCtBatchTypeContainer extends React.Component {
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
				const batchJteDefaultsDef = getEntityDefinition(F_CT_BATCH_JTE_DEFAULTS, {bteId: {show: false}});
				body = <div className={PAGE_BODY}>
					<EmbedRetrieveEntityListContainer
						title={i18n.t(batchJteDefaultsDef.className+CLASS_NAME)}
						icon={batchJteDefaultsDef.icon}
						columns={batchJteDefaultsDef.columns}
						componentPath={this.props.componentPath+F_CT_BATCH_JTE_DEFAULTS_P}
						retrieveType={F_CT_BATCH_JTE_DEFAULTS}
						parentHref={this.props.href}
						parentAttr={BTE_ID}
						parentData={this.props.data}
						asTable={true}
						expanded={true}

					 />
					<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+CHANGE_LOG} retrieveType={this.props.entityName}/>
				</div>;
			}
		}
		let button = EMPTY;
		if(this.props.data && this.props.data.id) {
			button = <Form><Form.Row>
				<Form.Group className={CLASS_NAME_M_2}>
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
					expanded={true}
					columnOverride={{side: {show: false},}}
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
	const entityName = F_CT_BATCH_TYPES;
	const entityDef = getEntityDefinition(entityName, undefined);
	const componentPath = PAGE_VIEW_CT_BATCH_TYPE_CONTAINER;
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
		headerText: ownProps.t(F_BATCH_TYPE_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {dispatchEditRESTData, resetRESTCallLimit}), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewCtBatchTypeContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
