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

import { getEntityDefinition, getExpandedColumns } from "../nomenclatures/entityDefinitions.js"
import { resetRESTCallLimit, dispatchEditRESTData } from "../../actions/taskActions";
import { resolveObjectPath } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"
import EmbedRestCallButton from "../embeds/EmbedRestCallButton";

const LOGIN = "/login"
const ID_TEXT = "id: "
const OUT_CODE_TEXT = "outCode: "
const POST = "post"
const REPORTS_MODIFY_STOCKS = "/reports/modifyStocks/"
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
const DOT_EMBEDDED_DOT = "._embedded."
const DOT_ZERO = ".0"
const ID_AND_OUT_CODE_ARE_REQUIRED = "ID and outCode are required"
const REPORTS_BLOCK_STOCKS = "/reports/blockStocks/"
const DOT_BLOCK_STOCKS = ".blockStocks"
const COPY_TO_GOODS_COMPONENT_PATH = ".cDeliveryDetailsGoods.pageData._embedded.cDeliveryDetails"
const THERE_ARE_NO_SELECTED_ROWS = "There are no selected rows"
const C_DELIVERY_DETAILS = "cDeliveryDetails"
const EQUAL = "equal"
const C_DELIVERY_GOOD_MAPS_DOT_PAR_ID_DOT_ID = "cDeliveryGoodMaps.parId.id"
const C_ORDER_DETAILS = "cOrderDetails"
const ORR_ID = "orrId"
const HEADER_REMAINING_QUANTITY = "Остатък"
const REMAINING_QUANTITY = "remainingQuantity"
const UNIT = "UNIT"
const FORM_CONTROL = "form-control"
const TEXT = "text"
const ORR_ID_DOT_PAR_ID_DOT_ID = "orrId.parId.id"
const ORR_ID_DOT_STATUS_DOT_LIST_OPTION_ITEM_CODE = "orrId.status.listOptionItemCode"
const GT = "gt"
const QUANTITY_CONFIRM = "quantityConfirm"
const DDL_QUANTITY = "ddlQuantity"
const NOT_EQUAL = "notEqual"
const ID = "id"
const TITLE_C_ORDER_DETAILS = "Поръчани стоки"
const DOT_C_ORDER_DETAILS = ".cOrderDetails"
const C_DELIVERY_DOT_COPY_TO_GOODS = "CDelivery._copyToGoods"
const TITLE_DELIVERY_DETAILS_GOODS = "Стоки"
const DOT_C_DELIVERY_DETAILS_GOODS = ".cDeliveryDetailsGoods"
const DEY_ID = "deyId"
const IS_NOT_NULL = "isNotNull"
const GOD_ID = "godId"
const ON_COMMIT_CHANGE = "onCommitChange"
const DELETE = "delete"
const DOT_C_DELIVERY_DETAILS_GOODS_DOT = ".cDeliveryDetailsGoods."
const TITLE_C_DELIVERY_DETAILS_SERVICE = "Услуги"
const DOT_C_DELIVERY_DETAILS_SERVICE = ".cDeliveryDetailsService"
const SEE_ID = "seeId"
const C_DELIVERIES = "cDeliveries"
const TYPE_DOC = "typeDoc"
const C_DELIVERY_DOT_CLASS_NAME = "CDelivery._className"
const HIDDEN = "hidden"
const GET = "get"
const SLASH_REPORTS_SLASH_BUILDER_SLASH_ONE = "/reports/builder/1"
const C_PMT_CURRENCY_RATE = "CPmtCurrencyRate"
const WHERE_FIRST_MESSAGE = "and(and(equal(CPmtCurrencyRate.cuyCode.code;stringLiteral("
const WHERE_SECOND_MESSAGE = "));le(CPmtCurrencyRate.dateFrom;localDateLiteral("
const YYYY_MM_DD = "YYYY-MM-DD"
const WHERE_THIRD_MESSAGE = ")));gt(CPmtCurrencyRate.dateTo;localDateLiteral("
const WHERE_FOURTH_MESSAGE = ")))"
const DOT_EXCHANGE_RATE = ".exchangeRate"
const DOT_C_ORDER_DETAILS_DOT_SELECTED_ROWS_DOT_MAP= ".cOrderDetails.selectedRows.map"
const C_DELIVERY_DOT_MODIFY_STOCKS= "CDelivery._modifyStocks"
const OPENING_BRACKET = " ("
const CLOSING_BRACKET = ")"
const C_DELIVERY_DOT_BLOCK_STOCKS = "CDelivery._blockStocks"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewCDeliveryContainer extends React.Component {	
	componentDidMount() {
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	modifyStocks() {
		if(this.props.data && this.props.data.id && this.props.data.outCode && this.props.data.outCode.id) {
			console.log(ID_TEXT + this.props.data.id)
			console.log(OUT_CODE_TEXT + this.props.data.outCode.id)
			axios({
				method: POST,
				url: API_URL+REPORTS_MODIFY_STOCKS + this.props.data.id + SLASH + this.props.data.outCode.id,
				headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
			}).then(response => {
				console.log(response.data)
				//this.props.actions.dispatchEditRESTData(this.props.componentPath+".modifyStocks",response.data);
				this.props.actions.dispatchEditRESTData(this.props.componentPath,{modifyStocks: response.data});
			}).catch(error => {
				console.log(error)
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className+DOT_EMBEDDED_DOT+this.props.entityName+DOT_ZERO+DOT+Constants.PATH_FOR_ERROR,error);
			});
		} else {
			console.error(ID_AND_OUT_CODE_ARE_REQUIRED);
		}
	}
	
	blockStocks() {
		if(this.props.data && this.props.data.id && this.props.data.outCode && this.props.data.outCode.id) {
			console.log(ID_TEXT + this.props.data.id)
			console.log(OUT_CODE_TEXT + this.props.data.outCode.id)
			axios({
				method: POST,
				url: API_URL+REPORTS_BLOCK_STOCKS + this.props.data.id + SLASH + this.props.data.outCode.id,
				headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
			}).then(response => {
				console.log(response.data)
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_BLOCK_STOCKS,response.data);
			}).catch(error => {
				console.log(error)
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_BLOCK_STOCKS,error);
			});
		} else {
			console.error(ID_AND_OUT_CODE_ARE_REQUIRED);
		}
	}
	
	copyToGoods() {
		if(this.props.viewData && this.props.viewData.cOrderDetails && this.props.viewData.cOrderDetails.selectedRows && this.props.viewData.cOrderDetails.selectedRows.map) {
			let newArray = [];
			if(this.props.viewData && this.props.viewData.cDeliveryDetailsGoods && this.props.viewData.cDeliveryDetailsGoods.pageData && 
					this.props.viewData.cDeliveryDetailsGoods.pageData._embedded && this.props.viewData.cDeliveryDetailsGoods.pageData._embedded.cDeliveryDetails instanceof Array) {
				newArray = this.props.viewData.cDeliveryDetailsGoods.pageData._embedded.cDeliveryDetails;
			}
			this.props.viewData.cOrderDetails.selectedRows.map.forEach((ordDet, index) => {
				if( this.props.viewData.cDeliveryDetailsGoods.pageData._embedded.cDeliveryDetails.findIndex((elem) => (elem.orlId.id == ordDet.id)) == -1 ) {
					newArray.push({
						orlId: ordDet,
						godId: ordDet.godId,
						meeId: ordDet.meeId,
						price: (ordDet.priceConfirm > 0 ? ordDet.priceConfirm : ordDet.price),
						quantity: ordDet.quantityConfirm - ordDet.ddlQuantity,//ordDet.quantity,
						vat: this.props.data.vat,
						_editable: true,
						_links: {
								orlId: {
									href: ordDet._links.self.href,
								},
								godId: {
									href: ordDet.godId._links.self.href,
								},
								meeId: {
									href: ordDet.meeId._links.self.href,
								},
							}
					});
				}
			});
			this.props.actions.dispatchEditRESTData(this.props.componentPath+COPY_TO_GOODS_COMPONENT_PATH,newArray);
		} else {
			console.warn(THERE_ARE_NO_SELECTED_ROWS);
		}
	}

	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const cDeliveryDetailsServiceDef = getEntityDefinition(C_DELIVERY_DETAILS, {deyId: {show:false},valuePrice: {show:false},ddlId: {show:false},orlId: {show:false},stkId: {show:false},varId: {show:false},godId: {show: false},batch: {show: false},serialNumber: {show: false},expiry: {show: false},costBase: {show: false},cost: {show: false}});
				const cDeliveryDetailsGoodsColumns = getExpandedColumns(C_DELIVERY_DETAILS, [], {
					stocks: {show:true},
					deyId: {show:false},valuePrice: {show:false},ddlId: {show:false},orlId: {isReadOnly:true},stkId: {show:false},
					varId: {show:false},seeId: {show: false},
					godId: (this.props.data && this.props.data.parId && this.props.data.parId.id ? {
						optionFilter: [{where: {
								op: EQUAL,
								operands: [C_DELIVERY_GOOD_MAPS_DOT_PAR_ID_DOT_ID, {literal: this.props.data.parId.id}],
							},
						}],
					} : {})
				});
				const cOrderDetailsColumns = getExpandedColumns(C_ORDER_DETAILS,[ORR_ID],{});
				cOrderDetailsColumns.splice(1,0,{
						Header: HEADER_REMAINING_QUANTITY,
						accessor: REMAINING_QUANTITY,
						fluidSize: 2,
						dataType: UNIT,
						isCalculated: true,
						filterable: false,
						sortable: false,
						Cell: (props) => <input className={FORM_CONTROL} type={TEXT} value={props.original.quantityConfirm - props.original.ddlQuantity} disabled={true}/>
					});
				let cOrderDetailsFilter = [];
				if(this.props.data && this.props.data.parId && this.props.data.parId.id) {
					cOrderDetailsFilter = cOrderDetailsFilter.concat([
							{where: {
								op: EQUAL,
								operands: [ORR_ID_DOT_PAR_ID_DOT_ID,{literal: this.props.data.parId.id}],
							}},
							{where: {
								op: EQUAL,
								operands: [ORR_ID_DOT_STATUS_DOT_LIST_OPTION_ITEM_CODE,{literal: 2}], //CF
							}},
							{where: {
								op: GT,
								operands: [QUANTITY_CONFIRM,DDL_QUANTITY],
							}},
						]);
				}
				if(this.props.viewData && this.props.viewData.cDeliveryDetailsGoods
						&& this.props.viewData.cDeliveryDetailsGoods.pageData
						&& this.props.viewData.cDeliveryDetailsGoods.pageData._embedded
						&& this.props.viewData.cDeliveryDetailsGoods.pageData._embedded.cDeliveryDetails instanceof Array) {
					this.props.viewData.cDeliveryDetailsGoods.pageData._embedded.cDeliveryDetails.forEach((elem) => {
						if(elem && elem.orlId && elem.orlId.id) {
							//console.log("filter",elem.orlId.id);
							cOrderDetailsFilter.push(
								{where: {
									op: NOT_EQUAL,
									operands: [ID,{literal: elem.orlId.id}],
								}}
							);
						}
					});
				}
				body = <div className={PAGE_BODY}>
						{this.props.id && this.props.data && this.props.data.typeDoc && (this.props.data.typeDoc.listOptionItemCode == 11 /*ISR*/)?
							<EmbedRetrieveEntityListContainer
								title={TITLE_C_ORDER_DETAILS}
								columns={cOrderDetailsColumns}
								componentPath={this.props.componentPath+DOT_C_ORDER_DETAILS}
								retrieveType={C_ORDER_DETAILS}
								asTable={true}
								expanded={this.props.cOrderDetailsData && this.props.cOrderDetailsData.pageData && this.props.cOrderDetailsData.pageData.page && this.props.cOrderDetailsData.pageData.page.totalElements > 0
									&& (this.props.totalForStock == 0 || this.props.addedToStock < this.props.totalForStock)}
								editable={false}
								hasRowSelecting={true}
								defaultFilter={cOrderDetailsFilter}
							>
								{this.props.id ?
									<Form><Form.Row>
										<Form.Group className={M_2}>
											<Button variant={OUTLINE_DARK} onClick={(e) => {
												e.stopPropagation();
												resetRESTCallLimit((this.props.viewData && this.props.viewData.cOrderDetails && this.props.viewData.cOrderDetails.selectedRows && this.props.viewData.cOrderDetails.selectedRows.map) ? this.props.viewData.cOrderDetails.selectedRows.map.size*4 : 100);
												this.copyToGoods();
											}}>{this.props.t(C_DELIVERY_DOT_COPY_TO_GOODS)}</Button>
										</Form.Group>
									</Form.Row></Form>
									: EMPTY
								}
							</EmbedRetrieveEntityListContainer>
							: EMPTY
						}
						<EmbedRetrieveEntityListContainer
							title={TITLE_DELIVERY_DETAILS_GOODS}
							columns={cDeliveryDetailsGoodsColumns}
							componentPath={this.props.componentPath+DOT_C_DELIVERY_DETAILS_GOODS}
							retrieveType={C_DELIVERY_DETAILS}
							parentHref={this.props.href}
							parentAttr={DEY_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
							defaultFilter = {[
								{where: {
									op: IS_NOT_NULL,
									operands: [GOD_ID],
								},}
							]}
							onCommitChange={(data,index,eventName) => {
								console.log(ON_COMMIT_CHANGE,index,data,eventName);
								if( eventName != DELETE ) {
									this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_C_DELIVERY_DETAILS_GOODS_DOT+index, data);
								}
							}}
						/>
						<EmbedRetrieveEntityListContainer
							title={TITLE_C_DELIVERY_DETAILS_SERVICE}
							icon={cDeliveryDetailsServiceDef.icon}
							columns={cDeliveryDetailsServiceDef.columns}
							componentPath={this.props.componentPath+DOT_C_DELIVERY_DETAILS_SERVICE}
							retrieveType={C_DELIVERY_DETAILS}
							parentHref={this.props.href}
							parentAttr={DEY_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
							defaultFilter = {[
								{where: {
									op: IS_NOT_NULL,
									operands: [SEE_ID],
								},}
							]}
							onCommitChange={(data) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_C_DELIVERY_DETAILS_GOODS,undefined);}}
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
					entityName={C_DELIVERIES}
					expandColumns={[TYPE_DOC]}
					headerText={this.props.t(C_DELIVERY_DOT_CLASS_NAME)}
					creatable={false}
					expanded={true}
					columnOverride={{
						costMethod: {show: false},paymentType: {show: false},datePayment: {show: false},discount: {show: false},vat: {show: false},sum: {show: false},oblSum: {show: false},danOsnova: {show: false},endSum: {show: false},vatSum: {show: false},vatTo: {show: false},
						outId: {show: false},status: {show: false},dlyId: {show: false},posted: {show: false},cost: {show: false},
						typeDoc: {show: HIDDEN},
						currency: {onChange: (href) => {
								axios({
									method: GET,
									url: API_URL+SLASH_REPORTS_SLASH_BUILDER_SLASH_ONE,
									params: {
										from: C_PMT_CURRENCY_RATE,
										select: C_PMT_CURRENCY_RATE,
										where: WHERE_FIRST_MESSAGE+(this.props.data && this.props.data.currency ? this.props.data.currency.code : 0)+WHERE_SECOND_MESSAGE+moment().format(YYYY_MM_DD)+WHERE_THIRD_MESSAGE+moment().format(YYYY_MM_DD)+WHERE_FOURTH_MESSAGE,
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
					onAfterRetrieve={(response) => {
							this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_C_ORDER_DETAILS_DOT_SELECTED_ROWS_DOT_MAP,undefined);
						}}
					onCommitChange={(data) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_C_DELIVERY_DETAILS_GOODS,undefined);}}
				>
				{this.props.id ?
					<Form><Form.Row>
						<Form.Group className={M_2}>
							<EmbedRestCallButton componentPath={this.props.componentPath+DOT_BLOCK_STOCKS} onClick={(e) => {this.modifyStocks();}} disabled={this.props.addedToStock >= this.props.totalForStock}>{this.props.t(C_DELIVERY_DOT_MODIFY_STOCKS)+OPENING_BRACKET+(this.props.addedToStock)+SLASH+(this.props.totalForStock)+CLOSING_BRACKET}</EmbedRestCallButton>
						</Form.Group>
						<Form.Group className={M_2}>
							<EmbedRestCallButton componentPath={this.props.componentPath+DOT_BLOCK_STOCKS} onClick={(e) => {this.blockStocks();}} disabled={this.props.addedToStock >= this.props.totalForStock}>{this.props.t(C_DELIVERY_DOT_BLOCK_STOCKS)}</EmbedRestCallButton>
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
	const entityName = C_DELIVERIES;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = C_DELIVERIES;
	let viewData = resolveObjectPath(componentPath,state.rest);
	let data = undefined;
	if(viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded 
			&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
		data = viewData[entityDef.className]._embedded[entityName][0];
	}
	let addedToStock;
	let totalForStock;
	if(viewData && viewData.cDeliveryDetailsGoods && viewData.cDeliveryDetailsGoods.pageData 
			&& viewData.cDeliveryDetailsGoods.pageData._embedded 
			&& viewData.cDeliveryDetailsGoods.pageData._embedded.cDeliveryDetails instanceof Array) {
		addedToStock = viewData.cDeliveryDetailsGoods.pageData._embedded.cDeliveryDetails.reduce((totalCount, current) => {
			return totalCount + (current.stocks && 
				(
					current.stocks._embedded && (current.stocks._embedded.cStocks instanceof Array) && (current.stocks._embedded.cStocks.length > 0)
					|| current.stocks.id != undefined
				) 
				? 1 
				: 0
			);
		}, 0);
		totalForStock = viewData.cDeliveryDetailsGoods.pageData._embedded.cDeliveryDetails.length;
	}
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		viewData: viewData,
		cOrderDetailsData: viewData ? viewData.cOrderDetails : undefined,
		//UI
		headerText: ownProps.t(C_DELIVERY_DOT_CLASS_NAME),
		addedToStock: addedToStock,
		totalForStock: totalForStock,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { resetRESTCallLimit, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewCDeliveryContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
