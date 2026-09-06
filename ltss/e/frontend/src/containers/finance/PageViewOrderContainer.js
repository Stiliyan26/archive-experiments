import React from "react";
import Modal from "react-responsive-modal";
import { Container, Row, Button, Form } from "react-bootstrap";
import ReactToPrint from "react-to-print";

import { resetRESTCallLimit, fetchRESTFollow, dispatchEditRESTData, patchRESTData } from "./../../actions/taskActions";

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
import OrderTemplate from "./../../components/shared/OrderTemplate"

import { getEntityDefinition } from "../nomenclatures/entityDefinitions.js"
import { resolveObjectPath, getNomenclatureByCriteria } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"
import RetrieveDataContainer from "../nomenclatures/RetrieveDataContainer"
import EmbedRestCallButton from "../embeds/EmbedRestCallButton";

const LOGIN = "/login"
const HEADER_TEXT = "Печатане на документ"
const FORM_GROUP = "form-group"
const SUCCESS = "success"
const WARNING = "warning"
const PRINT = "print"
const PRINT_DATA_FIRST_MESSAGE = "Печат"
const PRINT_DATA_SECOND_MESSAGE = "Изчакване на данните..."
const INVISIBLE_TABLE = "invisible-table"
const LG = "lg"
const HIDDEN = "hidden"
const PAGE_STYLE_SETTINGS = "table { width: 100% }.header {width: 40%}.header-divider {width: 20%}.text-align-center { text-align: -webkit-center; }.text-align-right { text-align: -webkit-right; }.text-align-left { text-align: -webkit-left; }.pull-right { float: right !important }@page { size: auto;  margin: 10mm; }@media print { body { -webkit-print-color-adjust: exact; } }.invisible-table .offer-table th,.invisible-table .offer-table td:not(.no-border) {border: 1px solid black}.invisible-table .offer-table th { background-color: #BFBFBF }.totalOfferPrice { background-color: #FFFF00 } "
const C_CC_PARTNERS = "cCcPartners"
const DOT_OWN_COMPANY_DATA = ".ownCompanyData"
const EQUAL = "equal"
const BULSTAT = "bulstat"
const STRING_LITERAL = "stringLiteral"
const LOI_ORDER_STATUSES = "loiOrderStatuses"
const PATCH = "patch"
const SLASH_C_ORDERS_SLASH = "/cOrders/"
const APPLICATION_SLASH_MERGE_PATCH_JSON = "application/merge-patch+json"
const PAGE_VIEW_ORDER_CONTAINER_DOT_CHANGE_STATUS = "PageViewOrderContainer.changeStatus"
const DOT_LOI_ORDER_STATUSES_DOT_CHANGE_STATUS = ".loiOrderStatuses.changeStatus"
const DETAIL = "detail"
const ORDER_VIEW_DOT = "orderView."
const ORDER_VIEW_DOT_ORDER_DETAILS = "orderView.orderDetails"
const SLASH_ORDER_SLASH_ADD = "/orders/add"
const ORDER_VIEW_DOT_ORDER_DETAILS_DOT_PAGE_DATA_DOT_EMBEDDED_DOT_C_ORDER_DETAILS_DOT = "orderView.orderDetails.pageData._embedded.cOrderDetails."
const C_ORDER_DETAILS = "cOrderDetails"
const DOT_ORDER_DETAILS = ".orderDetails"
const ORR_ID = "orrId"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const PAGE_BODY = "page-body"
const DOT_CHANGE_LOG = ".changelog"
const M_2 = "m-2"
const OUTLINE_DARK = "outline-dark"
const FIRST_BUTTON_LABEL = "Поръчай непотвърдените "
const SECOND_BUTTON_LABEL = "Откажи недоставените "
const THIRD_BUTTON_LABEL = "Поръчай недоставените "
const FINANCE_TRANSLATION_DOT_CHG_STATUS = "FinanceTranslation.chgStatus"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const SLASH = "/"
const SLASH_ADD = "/add"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const DOT = "."
const C_ORDERS = "cOrders"
const C_ORDER_DOT_CLASS_NAME = "COrder._className"
const ORDER_VIEW = "orderView"
const NOT_EQUAL = "notEqual"
const PARTNER_TYPE_DOT_LIST_OPTION_ITEM_CODE = "partnerType.listOptionItemCode"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewOrderContainer extends React.Component {	
	constructor(...args) {
		super(...args);
		this.state = {
			isModalPrintOpen: false,
			//printLanguage: "bg",
			//originalOrCopy: "orig",
		};
	}
	
	getPrintModal(formData, _printData) {
		return <Modal center open={this.state.isModalPrintOpen} onClose={() => {this.setState({ isModalPrintOpen: false });}} showCloseIcon={true}>
				<Container>
					<h1>{HEADER_TEXT}</h1>
					<Row className={FORM_GROUP}>
						<Button variant={(_printData ? SUCCESS : WARNING)} disabled={!(_printData)}
							onClick={_printData ? () => {
								resetRESTCallLimit();
								if(_printData){
									// timeout set to 1000ms in order to be sure that the shadow table has rendered before the print is triggered
									setTimeout(() => {
										this.printTriggerWrapper.handlePrint()
									}, 1000);
								}
							} : undefined}
						>
							<FontAwesomeIcon icon={PRINT}/>
							&nbsp;{ _printData ? PRINT_DATA_FIRST_MESSAGE : PRINT_DATA_SECOND_MESSAGE }
						</Button>
					</Row>
					{/*<Row className="form-group">
						<div className="col-sm-6">Език:</div>
						<div className="col-sm-6">
							<Switch
								id="language"
								onChange={(e) => {
									this.setState({
										printLanguage: e ? "en" : "bg"
									})}}
								checkedChildren={"EN"}
								unCheckedChildren={"BG"}
							/>
						</div>
						<div className="col-sm-6">Оригинал:</div>
						<div className="col-sm-6"><input 
							type="checkbox" 
							checked={this.state.originalOrCopy == "orig"} 
							onChange={(e) => {
								this.setState({
									originalOrCopy: e.target.checked ? "orig" : "copy"
								})}}/>
						</div>
					</Row>
					*/}
					{_printData ? 
						<OrderTemplate
							className={INVISIBLE_TABLE}
							formData={formData}
							formDetailsData={_printData.orderDetailsData}
							ownCompanyData={_printData.ownCompanyData}
							ref={(el) => (this.tableRef = el)}
						/>
					: EMPTY}
					<ReactToPrint
							ref={(el) => (this.printTriggerWrapper = el)}
							// the trigger below contains a HIDDEN button which gets "pushed" indirectly by the "enablePrint" func
							trigger={() => <Button disabled={!formData} ref={(el) => (this.printTrigger = el)} size={LG} className={HIDDEN}><FontAwesomeIcon icon={PRINT}/></Button>}
							content={() => this.tableRef }
							pageStyle={PAGE_STYLE_SETTINGS}
							copyStyles={false}
					/>
					<RetrieveDataContainer
						retrieveType={C_CC_PARTNERS}
						componentPath={this.props.componentPath+DOT_OWN_COMPANY_DATA}
						defaultFilter={[
								{where: {
									op: EQUAL,
									operands: [BULSTAT,{op: STRING_LITERAL, operands: [{literal: MANAGED_COMPANY_EIK}]}],
								},}
							]}
					/>
				</Container>
			</Modal>
		;
	}
	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}

	changeStatus() {
		getNomenclatureByCriteria(LOI_ORDER_STATUSES, (elem) => (elem.listOptionItemCode == 2), this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
			.then((orderStatus) => {
				//console.log("Order Status: ", orderStatus)
				this.props.actions.patchRESTData(
					{
						method: PATCH,
						url: API_URL+SLASH_C_ORDERS_SLASH+this.props.data.id,
						data: {
							status: orderStatus,
							_links: {
								status: {
									href: orderStatus._links.self.href,
								},
							},
						},
						headers: {"Content-Type": APPLICATION_SLASH_MERGE_PATCH_JSON}  //TODO the literal "Content-Type" cannot be replaced by a constant because it does not work
					},
					this.props.componentPath+DOT+this.props.entityDef.className,
					response => (undefined),
					(data) => {
					}, //force refresh
					PAGE_VIEW_ORDER_CONTAINER_DOT_CHANGE_STATUS
				)
			})
			.catch(error => {
				console.log(error)
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_LOI_ORDER_STATUSES_DOT_CHANGE_STATUS, error);
			});
	}
	
	transferToNewOrder(quantityFn) {
		getNomenclatureByCriteria(LOI_ORDER_STATUSES, (elem) => (elem.listOptionItemCode == 1 /*CR*/), this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
			.then((statusCr) => {
				//create new order
				let orderData = Object.assign({},this.props.data,{
						id: undefined, 
						orderNum: undefined,
						dateOrr: new Date().toISOString(),
						status: statusCr,
					});
				//create details for the rows with quantities for transfer
				let detailsData = [];
				if(this.props.viewData && this.props.viewData.orderDetails && this.props.viewData.orderDetails.pageData
						 && this.props.viewData.orderDetails.pageData._embedded&& this.props.viewData.orderDetails.pageData._embedded.cOrderDetails instanceof Array) {
					this.props.viewData.orderDetails.pageData._embedded.cOrderDetails.forEach((detail) => {
						console.log(DETAIL,detail);
						if(quantityFn(detail) > 0) {
							detailsData.push({
								...detail,
								id: undefined,
								quantity: quantityFn(detail),
								quantityConfirm: quantityFn(detail),
								rqyQuantity: 0,
								stkQuantity: 0,
								ddlQuantity: 0,
								plnQuantity: 0,
								cancelledQuantity: 0,
								_editable: true,
							});
						}
					});
				}
				this.props.actions.dispatchEditRESTData(ORDER_VIEW_DOT+this.props.entityDef.className,orderData);
				this.props.actions.dispatchEditRESTData(ORDER_VIEW_DOT_ORDER_DETAILS,{pageData: {_entitiesToAdd: detailsData}});
				history.push(SLASH_ORDER_SLASH_ADD);
			});
	}
	
	cancelNotDelivered() {
		if(this.props.viewData && this.props.viewData.orderDetails && this.props.viewData.orderDetails.pageData
				 && this.props.viewData.orderDetails.pageData._embedded&& this.props.viewData.orderDetails.pageData._embedded.cOrderDetails instanceof Array) {
			this.props.viewData.orderDetails.pageData._embedded.cOrderDetails.forEach((detail,index) => {
				detail._editable = true;
				detail.cancelledQuantity = detail.quantityConfirm - detail.ddlQuantity;
				this.props.actions.dispatchEditRESTData(ORDER_VIEW_DOT_ORDER_DETAILS_DOT_PAGE_DATA_DOT_EMBEDDED_DOT_C_ORDER_DETAILS_DOT+index,detail);
			});
		}
	}

	render() {
		let body = EMPTY;
		if(this.props.data) {
			let notConfirmedFn = ((detail) => detail.quantity - detail.quantityConfirm);
			let notSettledFn = ((detail) => detail.quantityConfirm - detail.ddlQuantity - detail.cancelledQuantity);
			let notDeliveredFn = ((detail) => detail.quantityConfirm - detail.ddlQuantity);
			let notConfirmedCount = 0;
			let notSettledCount = 0;
			let notDeliveredCount = 0;
			if(this.props.viewData && this.props.viewData.orderDetails && this.props.viewData.orderDetails.pageData
						 && this.props.viewData.orderDetails.pageData._embedded&& this.props.viewData.orderDetails.pageData._embedded.cOrderDetails instanceof Array) {
					notConfirmedCount = this.props.viewData.orderDetails.pageData._embedded.cOrderDetails.reduce((acc,detail) => {
						return acc + notConfirmedFn(detail);
					}, 0)
					notSettledCount = this.props.viewData.orderDetails.pageData._embedded.cOrderDetails.reduce((acc,detail) => {
						return acc + notSettledFn(detail);
					}, 0)
					notDeliveredCount = this.props.viewData.orderDetails.pageData._embedded.cOrderDetails.reduce((acc,detail) => {
						return acc + notDeliveredFn(detail);
					}, 0)
			}
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const orderDetailsDef = getEntityDefinition(C_ORDER_DETAILS, {orrId: {show:false}, doiId:{show:false}, ddlQuantity:{isReadOnly:true}, plnQuantity:{show:false}});
				/*
				let orderDetailsColumns = [{
					Header: "За отказ/прехвърляне",
					accessor: "_forTransfer",
					fluidSize: 3,
					dataType: "UNIT",
					Cell: (props) => <FieldUnitContainer
							componentPath={props.original._componentPath+"._forTransfer"} //existing path in redux store where we put data
							editable={true}
							loading={this.props.data[Constants.PATH_FOR_LOADING]}
							onChange={(value) => {}}
						/>
				}].concat(orderDetailsDef.columns);
				*/
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={orderDetailsDef.label}
							icon={orderDetailsDef.icon}
							columns={orderDetailsDef.columns}
							componentPath={this.props.componentPath+DOT_ORDER_DETAILS}
							retrieveType={C_ORDER_DETAILS}
							parentHref={this.props.href}
							parentAttr={ORR_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
						/>
							{this.props.id ?
								<Form><Form.Row>
									<Form.Group className={M_2}>
										<Button variant={OUTLINE_DARK} disabled={notConfirmedCount <= 0} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.transferToNewOrder(notConfirmedFn);}}>{FIRST_BUTTON_LABEL+notConfirmedCount}</Button>
									</Form.Group>
									<Form.Group className={M_2}>
										<Button variant={OUTLINE_DARK} disabled={notDeliveredCount <= 0 || this.props.data.status && this.props.data.status.listOptionItemCode != 2} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.cancelNotDelivered();}}>{SECOND_BUTTON_LABEL+notSettledCount}</Button>
									</Form.Group>
									<Form.Group className={M_2}>
										<Button variant={OUTLINE_DARK} disabled={notDeliveredCount <= 0 || this.props.data.status && this.props.data.status.listOptionItemCode != 3} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.transferToNewOrder(notDeliveredFn);}}>{THIRD_BUTTON_LABEL+notDeliveredCount}</Button>
									</Form.Group>
								</Form.Row></Form>
								: EMPTY
							}
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+DOT_CHANGE_LOG} retrieveType={this.props.entityName}/>
					</div>;
			}
		}
		let button = EMPTY;
		if(this.props.data && this.props.data.id && this.props.data.status && this.props.data.status.listOptionItemCode == 1) {
			button = <Form><Form.Row>
						<Form.Group className={M_2}>
								<EmbedRestCallButton componentPath={this.props.componentPath+DOT_LOI_ORDER_STATUSES_DOT_CHANGE_STATUS} onClick={(e)=> {this.changeStatus();}}>{this.props.t(FINANCE_TRANSLATION_DOT_CHG_STATUS)}</EmbedRestCallButton>
						</Form.Group>
					</Form.Row></Form>
		} 
		let modal = this.getPrintModal(this.props.data, this.props.printData);
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
					entityName={C_ORDERS}
					headerText={this.props.t(C_ORDER_DOT_CLASS_NAME)}
					creatable={false}
					expanded={true}
					columnOverride={{finishDate: {show: false}, 
						parId: {optionFilter:[{where: {
							op: NOT_EQUAL,
							operands: [PARTNER_TYPE_DOT_LIST_OPTION_ITEM_CODE, {literal: 1}],
						}}]},
					}}
					onPrint={(e) => {
						e.stopPropagation(); 
						resetRESTCallLimit(); 
						this.setState({ isModalPrintOpen: true }); 
					}}
				>
					{button}
					{modal}
				</EmbedEntityContainer>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = C_ORDERS;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = ORDER_VIEW;
	let viewData = resolveObjectPath(componentPath,state.rest);
	let data = undefined;
	if(viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded 
			&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
		data = viewData[entityDef.className]._embedded[entityName][0];
	}
	const orderDetailsData = (viewData && viewData.orderDetails && viewData.orderDetails.pageData && viewData.orderDetails.pageData._embedded && viewData.orderDetails.pageData._embedded.cOrderDetails) ? viewData.orderDetails.pageData._embedded.cOrderDetails : undefined;
	const ownCompanyData = (viewData && viewData.ownCompanyData && viewData.ownCompanyData._embedded && viewData.ownCompanyData._embedded.cCcPartners && viewData.ownCompanyData._embedded.cCcPartners[0]) ? viewData.ownCompanyData._embedded.cCcPartners[0] : undefined;
	const printData = (orderDetailsData && ownCompanyData ? {orderDetailsData: orderDetailsData, ownCompanyData: ownCompanyData} : undefined);
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		rest: state.rest,
		viewData: viewData,
		data: data,
		printData: printData,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t(C_ORDER_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { resetRESTCallLimit, fetchRESTFollow, dispatchEditRESTData, patchRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewOrderContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
