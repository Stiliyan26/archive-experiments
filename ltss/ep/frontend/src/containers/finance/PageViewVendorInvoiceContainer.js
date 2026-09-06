import React from "react";
import axios from "axios";
import { Form, Button } from "react-bootstrap";
import moment from "moment"
import querystring from "querystring"

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome"
import { withTranslation } from "react-i18next";

import * as Constants from "../../static/constants";
import history from "../../scripts/history"
import { isAuthenticated } from "../../components/pages/login/Login.js"

import Header from "../../components/generic/Header"
import EmbedEntityContainer from "../embeds/EmbedEntityContainer"
import EmbedChangeHistory from "../embeds/EmbedChangeHistory"

import { getEntityDefinition } from "../nomenclatures/entityDefinitions.js"
import { resetRESTCallLimit, dispatchEditRESTData } from "../../actions/taskActions";
import { resolveObjectPath } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"

const LOGIN = "/login"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const VENDOR_INVOICE_ROWS = "vendorInvoiceRows"
const DOT_VENDOR_INVOICE_ROWS = ".vendorInvoiceRows"
const PAGE_BODY = "page-body"
const INVOICE = "invoice"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const DOT = "."
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const SLASH = "/"
const SLASH_ADD = "/add"
const VENDOR_INVOICE_DOT_CLASS_NAME = "VendorInvoice._className"
const GET = "get"
const SLASH_REPORTS_SLASH_BUILDER_SLASH_ONE = "/reports/builder/1"
const C_PMT_CURRENCY_RATE = "CPmtCurrencyRate"
const WHERE_FIRST_MESSAGE = "and(and(equal(CPmtCurrencyRate.cuyCode.code;"
const WHERE_SECOND_MESSAGE = ");le(CPmtCurrencyRate.dateFrom;localDateLiteral("
const YYYY_MM_DD = "YYYY-MM-DD"
const WHERE_THIRD_MESSAGE = ")));gt(CPmtCurrencyRate.dateTo;localDateLiteral("
const WHERE_FOURTH_MESSAGE = ")))"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const DOT_INVOICE_EXCHANGE_RATE = ".invoiceExchangeRate"
const M_2 = "m-2"
const OUTLINE_DARK = "outline-dark"
const VENDOR_INVOICE_DOT_CLASS_NAME_KI = "VendorInvoice._className_KI"
const VENDOR_INVOICES = "vendorInvoices"
const VENDOR_INVOICES_VIEW = "vendorInvoicesView"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewVendorInvoiceContainer extends React.Component {	
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
				const vendorInvoiceRowsDef = getEntityDefinition(VENDOR_INVOICE_ROWS, {invoice: {show: false}});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={vendorInvoiceRowsDef.label}
							icon={vendorInvoiceRowsDef.icon}
							columns={vendorInvoiceRowsDef.columns}
							componentPath={this.props.componentPath+DOT_VENDOR_INVOICE_ROWS}
							retrieveType={VENDOR_INVOICE_ROWS}
							parentHref={this.props.href}
							parentAttr={INVOICE}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
						/>
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+DOT_CHANGE_LOG} retrieveType={this.props.entityName}/>
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
					headerText={this.props.t(VENDOR_INVOICE_DOT_CLASS_NAME)}
					creatable={false}
					expanded={true}
					columnOverride={{
						invoiceCurrency: {onChange: (href) => {
								axios({
									method: GET,
									url: API_URL+SLASH_REPORTS_SLASH_BUILDER_SLASH_ONE,
									params: {
										from: C_PMT_CURRENCY_RATE,
										select: C_PMT_CURRENCY_RATE,
										where: WHERE_FIRST_MESSAGE+this.props.data.invoiceCurrency.code+WHERE_SECOND_MESSAGE+moment().format(YYYY_MM_DD)+WHERE_THIRD_MESSAGE+moment().format(YYYY_MM_DD)+WHERE_FOURTH_MESSAGE,
									},
									paramsSerializer: function(params) {
										//needed for the from-to dates
										return querystring.stringify(params)
									},
									headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
								}).then(response => {
									if(response && response.data && response.data._embedded && response.data._embedded.hashMaps instanceof Array) {
										this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className+DOT_INVOICE_EXCHANGE_RATE
											,response.data._embedded.hashMaps[0].CPmtCurrencyRate.inMainCuy / response.data._embedded.hashMaps[0].CPmtCurrencyRate.unitOfCuy);
											//,undefined); //or clear it so the backend will handle it
									}
								});
							}}
					}}
				>
				{this.props.id && this.props.data && this.props.data.typeDoc && (this.props.data.typeDoc.listOptionItemCode == 13 /*SR*/ || this.props.data.typeDoc.listOptionItemCode == 16 /*RL*/)?
					<Form><Form.Row>
						<Form.Group className={M_2}>
							<Button variant={OUTLINE_DARK} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.createRevertSale();}}>{this.props.t(VENDOR_INVOICE_DOT_CLASS_NAME_KI)}</Button>
						</Form.Group>
					</Form.Row></Form>
					: EMPTY
				}
				</EmbedEntityContainer>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = VENDOR_INVOICES;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = VENDOR_INVOICES_VIEW;
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
		viewData: viewData,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t(VENDOR_INVOICE_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { resetRESTCallLimit, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewVendorInvoiceContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
