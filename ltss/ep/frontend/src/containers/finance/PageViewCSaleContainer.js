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
import { resetRESTCallLimit, dispatchEditRESTData, fetchRESTFollow } from "../../actions/taskActions";
import { resolveObjectPath, getNomenclatureByCriteria } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"

const LOGIN = "/login"
const LOI_TYPE_DOCS = "loiTypeDocs"
const POSTED_N = "N"
const REVERT_SALE_VIEW_DOT = "RevertSaleView."
const REVERT_SALE_VIEW_DOT_C_SALE_DETAILS_GOODS_DOT_PAGE_DATA_DOT_ENTITIES_TO_ADD = "RevertSaleView.cSaleDetailsGoods.pageData._entitiesToAdd"
const SLASH_REVERT_SALES_SLASH_ADD = "/revertSales/add"
const C_SALE_DETAILS = "cSaleDetails"
const TITLE_C_SALE_DETAILS_GOODS = "Стоки"
const DOT_C_SALE_DETAILS_GOODS = ".cSaleDetailsGoods"
const SAE_ID = "saeId"
const STK_ID = "stkId"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_BODY = "page-body"
const M_2 = "m-2"
const OUTLINE_DARK = "outline-dark"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const SLASH = "/"
const SLASH_ADD = "/add"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const DOT = "."
const C_SALE_DOT_CLASS_NAME = "CSale._className"
const DOT_EMBEDDED_DOT = "._embedded."
const DOT_ZERO = ".0"
const C_SALE_DOT_CLASS_NAME_KI = "CSale._className_KI"
const IS_NOT_NULL = "isNotNull"
const TITLE_C_SALE_DETAILS_SERVICE = "Услуги"
const DOT_C_SALE_DETAILS_SERVICE = ".cSaleDetailsService"
const SEE_ID = "seeId"
const GET = "get"
const SLASH_REPORTS_SLASH_BUILDER_SLASH_ONE = "/reports/builder/1"
const C_PMT_CURRENCY_RATE = "CPmtCurrencyRate"
const WHERE_FIRST_MESSAGE = "and(and(equal(CPmtCurrencyRate.cuyCode.code;"
const WHERE_SECOND_MESSAGE = ");le(CPmtCurrencyRate.dateFrom;localDateLiteral("
const YYYY_MM_DD = "YYYY-MM-DD"
const WHERE_THIRD_MESSAGE = ")));gt(CPmtCurrencyRate.dateTo;localDateLiteral("
const WHERE_FOURTH_MESSAGE = ")))"
const DOT_EXCHANGE_RATE = ".exchangeRate"
const C_SALES= "cSales"
const C_SALES_VIEW= "cSalesView"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewCSaleContainer extends React.Component {	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	createRevertSale() {
		getNomenclatureByCriteria(LOI_TYPE_DOCS, (elem) => (elem.listOptionItemCode == 5 /*KI*/), this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
			.then((typeDocKI) => {
				let revertData = Object.assign({},this.props.data,{
						id: undefined, 
						saeId: Object.assign({},this.props.data),
						saleDate: new Date(),
						datePayment: new Date(),
						typeDoc: typeDocKI,
						documentNumber: undefined,
						posted: POSTED_N
					});
				revertData._links.saeId.href = this.props.data._links.self.href;
				let detailsData = [];
				if(this.props.viewData && this.props.viewData.cSaleDetailsGoods && this.props.viewData.cSaleDetailsGoods.pageData
						 && this.props.viewData.cSaleDetailsGoods.pageData._embedded&& this.props.viewData.cSaleDetailsGoods.pageData._embedded.cSaleDetails instanceof Array) {
					detailsData = this.props.viewData.cSaleDetailsGoods.pageData._embedded.cSaleDetails.map((detail) => {
						let detailData = {
							...detail,
							id: undefined,
							sdlId: Object.assign({},detail),
							saeId: undefined,
							_editable: true,
						};
						detailData._links.sdlId.href = detail._links.self.href;
						return detailData;
					});
				}
				this.props.actions.dispatchEditRESTData(REVERT_SALE_VIEW_DOT+this.props.entityDef.className,revertData);
				this.props.actions.dispatchEditRESTData(REVERT_SALE_VIEW_DOT_C_SALE_DETAILS_GOODS_DOT_PAGE_DATA_DOT_ENTITIES_TO_ADD,detailsData);
				history.push(SLASH_REVERT_SALES_SLASH_ADD);
			});
	}

	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const cSaleDetailsServiceDef = getEntityDefinition(C_SALE_DETAILS, {stkId: {show: false},batch: {show: false},serialNumber: {show: false}});
				const cSaleDetailsGoodsDef = getEntityDefinition(C_SALE_DETAILS, {
						seeId: {show: false},
						currency: {show: false},
						priceVat: {show: false},
						rateExchange: {show: false},
						sdlId: {show: false},
						saeId: {show: false},
						cost: {show: false},
					});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={TITLE_C_SALE_DETAILS_GOODS}
							icon={cSaleDetailsGoodsDef.icon}
							columns={cSaleDetailsGoodsDef.columns}
							componentPath={this.props.componentPath+DOT_C_SALE_DETAILS_GOODS}
							retrieveType={C_SALE_DETAILS}
							parentHref={this.props.href}
							parentAttr={SAE_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
							defaultFilter = {[
								{where: {
									op: IS_NOT_NULL,
									operands: [STK_ID],
								},}
							]}
						/>
						<EmbedRetrieveEntityListContainer
							title={TITLE_C_SALE_DETAILS_SERVICE}
							icon={cSaleDetailsServiceDef.icon}
							columns={cSaleDetailsServiceDef.columns}
							componentPath={this.props.componentPath+DOT_C_SALE_DETAILS_SERVICE}
							retrieveType={C_SALE_DETAILS}
							parentHref={this.props.href}
							parentAttr={SAE_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
							defaultFilter = {[
								{where: {
									op: IS_NOT_NULL,
									operands: [SEE_ID],
								},}
							]}
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
					headerText={this.props.t(C_SALE_DOT_CLASS_NAME)}
					creatable={false}
					expanded={true}
					columnOverride={{placeDeals: {show: false}, saeId: {show: false}, status: {show: false}, tdtId: {show: false}, vatto: {show: false}, outId: {show: false}, oldTypeDoc: {show: false}, ofrId: {show: false}, posted: {show: false}, advanceUsed: {show: false}, cost: {show: false}, totalPayed: {show: false}, payed: {show: false},
						currency: {onChange: (href) => {
								axios({
									method: GET,
									url: API_URL+SLASH_REPORTS_SLASH_BUILDER_SLASH_ONE,
									params: {
										from: C_PMT_CURRENCY_RATE,
										select: C_PMT_CURRENCY_RATE,
										where: WHERE_FIRST_MESSAGE+this.props.data.currency.code+WHERE_SECOND_MESSAGE+moment().format(YYYY_MM_DD)+WHERE_THIRD_MESSAGE+moment().format(YYYY_MM_DD)+WHERE_FOURTH_MESSAGE,
									},
									paramsSerializer: function(params) {
										//needed for the from-to dates
										return querystring.stringify(params)
									},
									headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
								}).then(response => {
									if(response && response.data && response.data._embedded && response.data._embedded.hashMaps instanceof Array) {
										this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className+DOT_EMBEDDED_DOT+this.props.entityName+DOT_ZERO+DOT_EXCHANGE_RATE
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
							<Button variant={OUTLINE_DARK} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.createRevertSale();}}>{this.props.t(C_SALE_DOT_CLASS_NAME_KI)}</Button>
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
	const entityName = C_SALES;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = C_SALES_VIEW;
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
		headerText: ownProps.t(C_SALE_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { resetRESTCallLimit, dispatchEditRESTData, fetchRESTFollow }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewCSaleContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
