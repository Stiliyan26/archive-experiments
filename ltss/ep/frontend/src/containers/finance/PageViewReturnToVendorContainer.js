import React from "react";
import axios from "axios";
import { Form } from "react-bootstrap";

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

import { getEntityDefinitionExpanded } from "../nomenclatures/entityDefinitions.js"
import { resetRESTCallLimit, dispatchEditRESTData, fetchRESTFollow, postRESTData, patchRESTData } from "../../actions/taskActions";
import { showModal, hideModal } from "../../actions/modal"
import { resolveObjectPath, getNomenclatureByCriteria } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"
import EmbedRestCallButton from "../embeds/EmbedRestCallButton";

const LOGIN = "/login"
const LOI_TYPE_DOCS = "loiTypeDocs"
const C_TYPE_DOCUMENTS = "cTypeDocuments"
const PP = "ПП"
const POST = "post"
const SLASH_C_DELIVERY_DETAILS_SLASH = "/cDeliveryDetails/"
const DOT_GENERATED_DELIVERY_DETAILS_DOT = "._generatedDeliveryDetails."
const SLASH_C_DELIVERIES_SLASH = "/cDeliveries/"
const DUMMY_WILL_BE_CHANGED_BYPOST_REST_DATA = "dummyWillBeChangedBypostRESTData"
const ONE = "1"
const DOT_GENERATED_DELIVERY = "._generatedDelivery"
const DOT_DO_TRANSFER_GOODS = ".doTransferGoods"
const PATCH = "patch"
const STATUS_21 = "21"
const APPLICATION_SLASH_MERGE_PATCH_JSON = "application/merge-patch+json"
const DOT_EMBEDDED_DOT = "._embedded."
const DOT_ZERO = ".0"
const SLASH_REPORTS_SLASH_MODIFY_STOCKS_SLASH = "/reports/modifyStocks/"
const SHOW_MODAL_TITLE = "Прехвърлянето е приключено"
const SHOW_MODAL_BODY = "Създадена е насрещна складова разписка за вписване на стоките с приемо-предавателен протокол. Желаете ли да я прегледате?"
const YES = "Yes"
const NO = "No"
const ID_AND_OUT_CODE_ARE_REQUIRED = "ID and outCode are required"
const DO_TRANSFER_GOODS = "doTransferGoods"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const C_SALE_DETAILS = "cSaleDetails"
const STK_ID_DOT_GOD_ID_DOT_NAME_BG = "stkId.godId.nameBg"
const HIDDEN = "hidden"
const C_GOODS_DOT_CLASS_NAME_PLURAL = "CGoods._className_plural"
const DOT_C_SALE_DETAILS_GOODS = ".cSaleDetailsGoods"
const SAE_ID = "saeId"
const C_SALES = "cSales"
const PAGE_BODY = "page-body"
const IS_NOT_NULL = "isNotNull"
const STK_ID = "stkId"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const EQUAL = "equal"
const OUT_CODE_DOT_ID = "outCode.id"
const GT = "gt"
const QUANTITY = "quantity"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const SLASH = "/"
const SLASH_ADD = "/add"
const TWO = "2"
const C_SALE_DOT_CLASS_NAME_DI = "CSale._className_DI"
const SLASH_RETURN_TO_VENDOR = "/returnToVendor"
const PAR_ID_HEADER = "Доставчик"
const NOT_EQUAL = "notEqual"
const PARTNER_TYPE_DOT_LIST_OPTION_ITEM_CODE = "partnerType.listOptionItemCode"
const M_2 = "m-2"
const BUTTON_LABEL = "Прехвърли стоките"
const RETURN_TO_VENDOR_VIEW = "ReturnToVendorView"
const DOT = "."

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewReturnToVendorContainer extends React.Component {	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	doTransferGoods() {
		//sale_to_delivery
		//insertintodeliveriesfromsales
		//console.log("doTransferGoods");
		if(this.props.data && this.props.data.id && this.props.data.status == TWO
				&& this.props.viewData
				&& this.props.viewData.cSaleDetailsGoods
				&& this.props.viewData.cSaleDetailsGoods.pageData
				&& this.props.viewData.cSaleDetailsGoods.pageData._embedded
				&& this.props.viewData.cSaleDetailsGoods.pageData._embedded.cSaleDetails instanceof Array) {
			let destinationWarehouse = this.props.data.outId;
			getNomenclatureByCriteria(LOI_TYPE_DOCS, (elem) => (elem.listOptionItemCode == 6 /*PZ*/), this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
				.then((typeDocPZ) => {
					getNomenclatureByCriteria(C_TYPE_DOCUMENTS, (elem) => (elem.shortName == PP), this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
						.then((cTypeDocPP) => {
							//console.log("doTransferGoods create delivery");
							//create delivery
							let postPromiseWrapper = {};
							this.props.actions.postRESTData(
								{
									method: POST,
									url: API_URL+SLASH_C_DELIVERIES_SLASH,
									data: {
										id: undefined,
										deyDate: (new Date()).toISOString(),
										typeDoc: DUMMY_WILL_BE_CHANGED_BYPOST_REST_DATA,
										tdtId: DUMMY_WILL_BE_CHANGED_BYPOST_REST_DATA,
										outCode: DUMMY_WILL_BE_CHANGED_BYPOST_REST_DATA,
										outId: DUMMY_WILL_BE_CHANGED_BYPOST_REST_DATA,
										status: ONE,
										groundsNumber: this.props.data.documentNumber,
										groundsDate: this.props.data.saleDate,
										total: this.props.data.total,
										vat: this.props.data.vat,
										currency: this.props.data.currency,
										exchangeRate: this.props.data.exchangeRate,
										vatSum: this.props.data.vatto,
										sum: this.props.data.sum,
										oblSum: this.props.data.oblSum,
										endSum: this.props.data.endSum,
										danOsnova: this.props.data.danOsnova,
										cost: this.props.data.sum,
										_links: {
											typeDoc: {href: typeDocPZ._links.self.href},
											tdtId: {href: cTypeDocPP._links.self.href},
											outCode: {href: destinationWarehouse._links.self.href},
											outId: {href: this.props.data.outCode._links.self.href},
										}
									}
								},
								this.props.componentPath+DOT_GENERATED_DELIVERY,
								this.props.componentPath+DOT_DO_TRANSFER_GOODS,
								response => (response.data),
								undefined,
								undefined,
								postPromiseWrapper
							);
							postPromiseWrapper.promise
								.then((response) => {
									//console.log("doTransferGoods create delivery response",response);
									if(response instanceof Error) {
										this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className+DOT_EMBEDDED_DOT+this.props.entityName+DOT_ZERO+DOT+Constants.PATH_FOR_ERROR,response);
									} else {
										let newDelivery = response.data;
										
										//change sale status
										this.props.actions.patchRESTData(
											{
												method: PATCH,
												url: API_URL+SLASH+this.props.entityName+SLASH+this.props.data.id,
												data: {
													status: STATUS_21,
												},
												headers: {"Content-Type": APPLICATION_SLASH_MERGE_PATCH_JSON} //TODO the literal "Content-Type" cannot be replaced by a constant because it does not work
											},
											this.props.componentPath+DOT+this.props.entityDef.className+DOT_EMBEDDED_DOT+this.props.entityName+DOT_ZERO,
											response => (undefined),
											(data) => {
											}, //force refresh
											this.props.componentPath+DOT_DO_TRANSFER_GOODS
										)
										//console.log("doTransferGoods sale status patched");
										//create delivery details
										let detailPromises = [];
										this.props.viewData.cSaleDetailsGoods.pageData._embedded.cSaleDetails.forEach((detail, index) => {
											//console.log("doTransferGoods create detail",detail);
											let detailPromiseWrapper = {};
											this.props.actions.postRESTData(
												{
													method: POST,
													url: API_URL+SLASH_C_DELIVERY_DETAILS_SLASH,
													data: {
														id: undefined,
														deyId: DUMMY_WILL_BE_CHANGED_BYPOST_REST_DATA,
														//godId: "dummyWillBeChangedBypostRESTData",
														batch: detail.batch,
														serialNumber: detail.serialNumber,
														quantity: detail.quantity,
														meeId: DUMMY_WILL_BE_CHANGED_BYPOST_REST_DATA,
														price: detail.price,
														vat: detail.vat,
														valuePrice: detail.priceVat,
														discount: detail.discount,
														valueAll: detail.totalWhtVat,
														vatAll: detail.totalVat,
														total: detail.total,
														cost: detail.price,
														costBase: 1,
														stkId: DUMMY_WILL_BE_CHANGED_BYPOST_REST_DATA,
														_links: {
															deyId: {href: newDelivery._links.self.href},
															//godId: {href: detail.stkId.godId._links.self.href},
															meeId: {href: detail.meeId._links.self.href},
															stkId: {href: detail.stkId._links.self.href},
														}
													}
												},
												this.props.componentPath+DOT_GENERATED_DELIVERY_DETAILS_DOT+index,
												this.props.componentPath+DOT_DO_TRANSFER_GOODS,
												response => (response.data),
												undefined,
												undefined,
												detailPromiseWrapper
											);
											detailPromises.push(detailPromiseWrapper.promise);
										});
										Promise.all(detailPromises).then(() => {
											//console.log("doTransferGoods modify_stocks",newDelivery,destinationWarehouse);
											//modify_stocks
											if(newDelivery && newDelivery.id && destinationWarehouse && destinationWarehouse.id) {
												axios({
													method: POST,
													url: API_URL+SLASH_REPORTS_SLASH_MODIFY_STOCKS_SLASH + newDelivery.id + SLASH + destinationWarehouse.id,
													headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
												}).then(response => {
													//console.log(response.data)
													//this.props.actions.dispatchEditRESTData(this.props.componentPath+"._modifyStocks",response.data);
													this.props.actions.dispatchEditRESTData(this.props.componentPath,{_modifyStocks: response.data});
													
													this.props.actions.showModal({
														title: SHOW_MODAL_TITLE,
														body: SHOW_MODAL_BODY,
														acceptLabel: this.props.t(YES),
														acceptCallback: () => {
															this.props.actions.hideModal();
															history.push(SLASH_C_DELIVERIES_SLASH+newDelivery.id);
														},
														refuseLabel: this.props.t(NO),
														refuseCallback: () => {
															this.props.actions.hideModal();
														},
													})
												}).catch(error => {
													console.error(error)
													this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className+DOT_EMBEDDED_DOT+this.props.entityName+DOT_ZERO+DOT+Constants.PATH_FOR_ERROR,error);
												});
											} else {
												console.error(ID_AND_OUT_CODE_ARE_REQUIRED);
											}
										});
									}
								}).catch((error) => {
									this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className+DOT_EMBEDDED_DOT+this.props.entityName+DOT_ZERO+DOT+Constants.PATH_FOR_ERROR,error);
								});
						}).catch(error => {
							this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+DO_TRANSFER_GOODS,error);
						});
				}).catch(error => {
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+DO_TRANSFER_GOODS,error);
				});
		}
	}

	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10) && this.props.data.outCode && this.props.data.outCode.id) {
				const cSaleDetailsGoodsDef = getEntityDefinitionExpanded(C_SALE_DETAILS, [STK_ID_DOT_GOD_ID_DOT_NAME_BG], {
						stkId: {
							optionFilter: [{where: {
										op: EQUAL,
										operands: [OUT_CODE_DOT_ID, {literal: this.props.data.outCode.id}],
									},
								},{where: {
										op: GT,
										operands: [QUANTITY, {literal: 0}],
									},
								}
							],
						},
						seeId: {show: false},
						totalWhtVat: {show: false},
						discount: {show: false},
						valDisc: {show: false},
						vat: {show: false},
						totalVat: {show: false},
						total: {show: false},
						currency: {show: false},
						price: {show: false},
						priceVat: {show: false},
						rateExchange: {show: false},
						sdlId: {show: false},
						saeId: {show: false},
						odlId: {show: false},
						cost: {show: false},
						"stkId.godId.nameBg": {show: HIDDEN}, //TODO the literal "stkId.godId.nameBg" cannot be replaced by a constant because it does not work
					});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={this.props.t(C_GOODS_DOT_CLASS_NAME_PLURAL)}
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
					setDefaults={(data) => {
						if(data == undefined || data.typeDoc == undefined || data.typeDoc.listOptionItemCode != 15) {
							return getNomenclatureByCriteria(LOI_TYPE_DOCS, (elem) => (elem.listOptionItemCode == 15 /*DI*/), this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
								.then((typeDocDI) => {
									data.typeDoc = typeDocDI;
									if(data._links == undefined) data._links = {};
									if(data._links.typeDoc == undefined) data._links.typeDoc = {};
									data._links.typeDoc.href = typeDocDI._links.self.href;
									
									data.status = TWO;
									//data.outCode = ??
									return data;
								});
						}
						return Promise.resolve(data);
					}}
					entityName={this.props.entityName}
					headerText={this.props.t(C_SALE_DOT_CLASS_NAME_DI)}
					entityOverride={{pageURL: SLASH_RETURN_TO_VENDOR}}
					creatable={false}
					expanded={true}
					columnOverride={{
						parId: {Header: PAR_ID_HEADER, optionFilter: [{where: {
								op: NOT_EQUAL,
								operands: [PARTNER_TYPE_DOT_LIST_OPTION_ITEM_CODE, {literal: 1 /*CL*/}],
							},}],
						}, 
						//outCode: {show: false},
						documentNumber: {isReadOnly: true}, 
						typeDoc: {isReadOnly: true}, //TODO must to be hidden
						outId: {show: false}, 
						discount: {show: false}, 
						paymentType: {show: false}, 
						datePayment: {show: false}, 
						idtCode: {show: false}, 
						vat: {show: false}, 
						sum: {show: false}, 
						oblSum: {show: false}, 
						danOsnova: {show: false}, 
						endSum: {show: false}, 
						vatSum: {show: false}, 
						total: {show: false}, 
						currency: {show: false}, 
						exchangeRate: {show: false}, 
						placeDeals: {show: false}, 
						saeId: {show: false}, 
						status: {show: false}, tdtId: {show: false}, vatto: {show: false}, oldTypeDoc: {show: false}, ofrId: {show: false}, posted: {show: false}, advanceUsed: {show: false}, cost: {show: false}, totalPayed: {show: false}, payed: {show: false}}}
				>
				{(this.props.id && this.props.data 
						&& this.props.viewData
						&& this.props.viewData.cSaleDetailsGoods
						&& this.props.viewData.cSaleDetailsGoods.pageData
						&& this.props.viewData.cSaleDetailsGoods.pageData._embedded
						&& this.props.viewData.cSaleDetailsGoods.pageData._embedded.cSaleDetails instanceof Array
						&& this.props.viewData.cSaleDetailsGoods.pageData._embedded.cSaleDetails.length > 0
						&& this.props.data.typeDoc && this.props.data.typeDoc.listOptionItemCode == 15 /*DI*/) ?
					<Form><Form.Row>
						<Form.Group className={M_2}>
							<EmbedRestCallButton componentPath={this.props.componentPath+DOT+DO_TRANSFER_GOODS} disabled={!(this.props.data && this.props.data.id && this.props.data.status == TWO)} onClick={(e) => {this.doTransferGoods();}}>{BUTTON_LABEL}</EmbedRestCallButton>
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
	let entityDef = getEntityDefinitionExpanded(entityName,[],undefined);
	entityDef.pageURL = SLASH_RETURN_TO_VENDOR;
	const componentPath = RETURN_TO_VENDOR_VIEW;
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
		headerText: ownProps.t(C_SALE_DOT_CLASS_NAME_DI),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { resetRESTCallLimit, dispatchEditRESTData, fetchRESTFollow, postRESTData, patchRESTData, showModal, hideModal }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewReturnToVendorContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
