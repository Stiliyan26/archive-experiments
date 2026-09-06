import React from "react";

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome"
import { withTranslation } from "react-i18next";

import * as Constants from "../../static/constants";
import history from "../../scripts/history"
import { isAuthenticated } from "../../components/pages/login/Login.js"

import Header from "../../components/generic/Header"
import EmbedChangeHistory from "../embeds/EmbedChangeHistory"

import { getEntityDefinition } from "../nomenclatures/entityDefinitions.js"
import { dispatchEditRESTData } from "../../actions/taskActions";
import { resolveObjectPath } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"
import EmbedCInvoiceContainer from "../embeds/EmbedCInvoiceContainer";

const LOGIN = "/login"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const C_INVOICE_ROWS = "cInvoiceRows"
const DOT_C_INVOICE_ROWS = ".cInvoiceRows"
const INVOICE = "invoice"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_BODY = "page-body"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const SLASH = "/"
const SLASH_ADD = "/add"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const DOT = "."
const C_INVOICES = "cInvoices"
const INCOME_VIEW = "incomeView"
const C_INVOICE_DOT_CLASS_NAME = "CInvoice._className"


//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewCInvoiceContainer extends React.Component {
	
	onChildUpdate(componentPath,value) {
		//refresh the page data
		this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className,{});
	}
	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const cInvoiceRowsDef = getEntityDefinition(C_INVOICE_ROWS,{invoice: {show: false}});
				body = <div className={PAGE_BODY}>
					<EmbedRetrieveEntityListContainer
						title={cInvoiceRowsDef.label}
						icon={cInvoiceRowsDef.icon}
						expanded={true}
						columns={cInvoiceRowsDef.columns}
						componentPath={this.props.componentPath + DOT_C_INVOICE_ROWS}
						retrieveType={C_INVOICE_ROWS}
						parentHref={this.props.href}
						parentAttr={INVOICE}
						parentData={this.props.data}
						onCommitChange={()=>{this.onChildUpdate();}}
					/>
					<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+DOT_CHANGE_LOG} retrieveType={this.props.entityName}/>
				</div>;
			}
		}
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={this.props.headerText} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<EmbedCInvoiceContainer
					retrieve_id={this.props.match.params.entity_id}
					componentPath={this.props.componentPath+DOT+this.props.entityDef.className}
					onBeforeChange={(data) => {
						if(!this.props.entityDef.pageURL) {console.error(PAGE_URL_IS_MISSING_FOR_ENTITY+this.props.entityDef.className);}
						if(data.id) {
							history.push(this.props.entityDef.pageURL+SLASH+data.id);
						} else {
							history.push(this.props.entityDef.pageURL+SLASH_ADD);
						}}}
					expanded={true}
				/>
				{body}
			</div>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = C_INVOICES;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = INCOME_VIEW;
	let viewData = resolveObjectPath(componentPath,state.rest);
	const data = viewData ? viewData[entityDef.className] : undefined;
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t(C_INVOICE_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewCInvoiceContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
