import React from "react";
import axios from "axios";
import { Form, Col, ButtonGroup, Button } from "react-bootstrap";
import ReactTable from "react-table-v6"
import lodash from "lodash"
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome"

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"
import { withTranslation } from "react-i18next";

import history from "../../scripts/history"
import { isAuthenticated } from "../../components/pages/login/Login.js"
import * as Constants from "./../../static/constants";

import Header from "../../components/generic/Header"

import { getCombinedEntityDefinitions, getEntityDefinition, getUpdatedColumn, getExpandedColumns } from "../nomenclatures/entityDefinitions.js"
import { resetRESTCallLimit, dispatchEditRESTData } from "../../actions/taskActions";
import { resolveObjectPath } from "./../../scripts/dataUtils";
import EmbedEntityListOrTableContainer from "../nomenclatures/EmbedEntityListOrTableContainer"

import FieldSelectContainer from "../fields/FieldSelectContainer"
import EmbedRestCallButton from "../embeds/EmbedRestCallButton";

const LOGIN = "/login"
const GET = "get"
const REPORTS_GET_ORDER_GOODS = "/reports/getOrderGoods/"
const DOT_GET_ORDER_GOODS_DOT = ".getOrderGoods."
const DOT_VENDORS_DOT_SELECTED_ROWS_DOT_MAP = ".vendors.selectedRows.map"
const DOT_GET_ORDER_GOODS = ".getOrderGoods"
const SELECTED_VENDOR = "Selected vendor"
const PARTNER_DOT_ID = "partner.Id: "
const C_ORDER_DETAILS = "cOrderDetails"
const HEADER_VENDOR = "Основен доставчик"
const VENDOR = "vendor"
const CHECKBOX = "checkbox"
const CARET_SQUARE_UP = "caret-square-up"
const CARET_SQUARE_DOWN = "caret-square-down"
const OUT_CODE = "outCode"
const C_CC_ORGANIZATION_UNITS = "cCcOrganizationUnits"
const HEADER_MARK_NAME = "Марка"
const ACCESSOR_MARK_NAME = "godId.goodMark.markName"
const COMMA_WITH_SPACE = ", "
const HEADER_GOOD_ID = "Стока"
const ACCESSOR_GOOD_ID = "godId.nameBg"
const HEADER_RQY_QUANTITY = "По заявки"
const ACCESSOR_RQY_QUANTITY = "rqyQuantity"
const HEADER_STK_QUANTITY = "За склад"
const ACCESSOR_STK_QUANTITY = "stkQuantity"
const HEADER_TO_ORDER_QUANTITY = "Общо за поръчване"
const ACCESSOR_TO_ORDER_QUANTITY = "toOrderQuantity"
const HEADER_QUANTITY_NOT_CONFIRMED = "Непотвърдено к-во"
const ACCESSOR_QUANTITY_NOT_CONFIRMED = "quantityNotConfirmed"
const HEADER_QUANTITY_CONFIRM = "Потвърдено к-во"
const ACCESSOR_QUANTITY_CONFIRM = "quantityConfirm"
const NOTIFICATIONS = "Notifications"
const TEXT = "TEXT"
const C_PRICE_LIST = "CPriceList"
const PAR_ID = "parId"
const FILTERED_ENTITY = "FILTERED_ENTITY"
const C_CC_PARTNERS = "cCcPartners"
const GOD_ID = "godId"
const ENTITY = "ENTITY"
const C_GOODSES = "cGoodses"
const ID = "id"
const CREATE = "Създай"
const LOAD_IT = "Зареди"
const U_1F194 = "\u{1F194}"
const POST = "post"
const REPORTS_MAKE_ORDER = "/reports/makeOrder/"
const SLASH_IN_QUOTES = "/"
const DOT_MAKE_ORDER = ".makeOrder"
const DOT_MAKE_ORDER_DOT_C_ORDER_DETAIL_DOT = ".makeOrder.COrderDetail."
const DOT_MAKE_ORDER_DOT_C_ORDER_DETAIL = ".makeOrder.COrderDetail"
const DOT_MAKE_ORDER_DOT_MESSAGE = ".makeOrder.message"
const REPORTS = "Reports"
const SUM = "sum"
const MINUS = "minus"
const DEFAULT_VENDOR_GOODS_RESERVE_QUANTITIES_INITIAL_QUANTITY = "defaultVendorGoods.reserveQuantities.initialQuantity"
const DEFAULT_VENDOR_GOODS_RESERVE_QUANTITIES_QUANTITY = "defaultVendorGoods.reserveQuantities.quantity"
const UNIT = "UNIT"
const C_RESERVE_QUANTITIES = "cReserveQuantities"
const HEADER_TEXT = "Генериране на поръчки за доставчици"
const SYNC = "sync"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const EMPTY = ""
const DOT = "."
const PAGE_BODY = "page-body"
const CLASS_NAME_M_2 = "m-2"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHTS = "clients-table align-center-table -striped -highlight"
const GENERATE_ORDER = "generateOrder"

//Page: can be used as a landing page
//Container: redux container class
class PageGenerateOrderContainer extends React.Component {

	constructor(...args) {
		super(...args);
		this.state = {
			outId: 0,
		};
	}
	
	getOrderGoods(outId){
		let promise = axios({
			method: GET,
			url: API_URL+REPORTS_GET_ORDER_GOODS + outId,
			headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
		}).then(response => {
			let data = response.data;
			data = data.map((datarow, index) => ({
				...datarow,
				vendor: JSON.stringify(datarow.defaultVendor),
				toOrderQuantity: datarow.rqyQuantity + datarow.stkQuantity - datarow.quantityConfirm,
				_componentPath: this.props.componentPath+DOT_GET_ORDER_GOODS_DOT+index,
			}));
			//clear selection
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_VENDORS_DOT_SELECTED_ROWS_DOT_MAP,new Map());
			//console.log(data);
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_GET_ORDER_GOODS,data);
		}).catch(error => {
			//console.log(error)
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_GET_ORDER_GOODS,error);
		});
		this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_GET_ORDER_GOODS_DOT+Constants.PATH_FOR_LOADING, promise);
	}
	
	//https://localhost:8443/api/reports/makeOrder/573/18
	generateOrder(){
		let resultAll = {COrder: [], COrderDetail: [], message: []};
		let errorsAll = [];
		let promiseArray = [];
		//let promise = Promise.resolve(false);
		this.props.selectedRows.forEach((vendor) => {
			console.log(SELECTED_VENDOR,vendor);
			console.log(PARTNER_DOT_ID + vendor.id);
			
		//	promise = promise.then(() => {
		//		return axios({
			promiseArray.push( axios({
					method: POST,
					url: API_URL+REPORTS_MAKE_ORDER + vendor.id + SLASH_IN_QUOTES + this.state.outId,
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let result = response.data;
					if(result.message instanceof Array) {
						result.message = result.message.map((elem) => {return {
							Notifications: elem.message,
							messageCode: elem.messageCode,
							parId: elem.partner, 
							godId: elem.goods,
						}});
					}
					console.log(result);
					resultAll.COrder = resultAll.COrder.concat(result.COrder);
					resultAll.COrderDetail = resultAll.COrderDetail.concat(result.COrderDetail);
					resultAll.message = resultAll.message.concat(result.message);
				}).catch(error => {
					console.log(error);
					errorsAll = errorsAll.concat(error);
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_MAKE_ORDER,errorsAll);
				})
			);
		//	});
		});
		//promise.then(() => {
		let promise = Promise.all(promiseArray).then(() => {
			//save result
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_MAKE_ORDER,resultAll);
		});
		this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_MAKE_ORDER_DOT_C_ORDER_DETAIL_DOT+Constants.PATH_FOR_LOADING,promise);
		return;
	}
	
	selectRow(isSelected,vendor) {
		let selectedRows = this.props.selectedRows;
		//console.log("selectedRows",selectedRows,isSelected,vendor);
		let isChange = false;
		if(!isSelected && selectedRows.has(vendor.id)) {
			selectedRows.delete(vendor.id);
			isChange = true;
		}
		if(isSelected && !selectedRows.has(vendor.id)) {
			const rowDataCopy = Object.assign({},vendor);
			selectedRows.set(vendor.id,rowDataCopy);
			isChange = true;
		}
		if(isChange) {
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_VENDORS_DOT_SELECTED_ROWS_DOT_MAP,selectedRows);
		}
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
		this.getOrderGoods(0);
	}

	render() {
		let column = {
			accessor: OUT_CODE,
			entityType: C_CC_ORGANIZATION_UNITS,
		};
		let entityDefinition =  getCombinedEntityDefinitions()[column.entityType];
		let rootDefinition = getEntityDefinition(C_ORDER_DETAILS, {});//TODO CHECK FOR THE UNUSED VARIABLE
		let tableColumns = [
			{Header: HEADER_VENDOR, accessor: VENDOR,
				Pivot: (props) => {
					//console.log(props);
					if(props.value) {
						let vendor = JSON.parse(props.value);
						return (<div>
							<input type={CHECKBOX} checked={this.props.selectedRows.has(vendor.id)} onClick={(e)=> {e.stopPropagation(); resetRESTCallLimit(); this.selectRow(e.target.checked,vendor);}} id={VENDOR+vendor.id}/>
							<label for={VENDOR+vendor.id}>&nbsp;{vendor.name}</label>
							&nbsp;<FontAwesomeIcon icon={props.isExpanded ? CARET_SQUARE_UP : CARET_SQUARE_DOWN}/>
						</div>);
					}
					return props.value;
				}},
			{Header: HEADER_MARK_NAME, accessor: ACCESSOR_MARK_NAME, aggregate: vals => lodash.uniq(vals).join(COMMA_WITH_SPACE)},
			{Header: HEADER_GOOD_ID, accessor: ACCESSOR_GOOD_ID, aggregate: () => EMPTY},
			{Header: HEADER_RQY_QUANTITY, accessor: ACCESSOR_RQY_QUANTITY, aggregate: vals => lodash.round(lodash.sum(vals),3)},
			{Header: HEADER_STK_QUANTITY, accessor: ACCESSOR_STK_QUANTITY, aggregate: vals => lodash.round(lodash.sum(vals),3)},
			{Header: HEADER_TO_ORDER_QUANTITY, accessor: ACCESSOR_TO_ORDER_QUANTITY, aggregate: vals => lodash.round(lodash.sum(vals),3)},
			{Header: HEADER_QUANTITY_NOT_CONFIRMED, accessor: ACCESSOR_QUANTITY_NOT_CONFIRMED, aggregate: vals => lodash.round(lodash.sum(vals),3)},
			{Header: HEADER_QUANTITY_CONFIRM, accessor: ACCESSOR_QUANTITY_CONFIRM, aggregate: vals => lodash.round(lodash.sum(vals),3)}]
		let detailDefinition = getEntityDefinition(C_ORDER_DETAILS, {});
		let messageColumns = [];
		messageColumns.push(
				getUpdatedColumn(NOTIFICATIONS, {
						accessor: NOTIFICATIONS,
						fluidSize: 3,
						dataType: TEXT,
					}, {}, EMPTY)
			);
		messageColumns.push(
				getUpdatedColumn(C_PRICE_LIST, {
						accessor: PAR_ID,
						fluidSize: 3,
						dataType: FILTERED_ENTITY,
						entityType: C_CC_PARTNERS,
					}, {}, C_CC_PARTNERS)
			);
		messageColumns.push(
				getUpdatedColumn(C_PRICE_LIST, {
						accessor: GOD_ID,
						fluidSize: 3,
						dataType: ENTITY,
						entityType: C_GOODSES
					}, {}, C_GOODSES)
			);
		let body = <div>
					<Form.Row className={CLASS_NAME_M_2}>
						<Col>
							<EmbedEntityListOrTableContainer
								columns={detailDefinition.columns}
								data={this.props.data.makeOrder ? this.props.data.makeOrder.COrderDetail : undefined}
								componentPath={this.props.componentPath+DOT_MAKE_ORDER_DOT_C_ORDER_DETAIL}
								filterable={false}
							/>
						</Col>
					</Form.Row>
					<Form.Row className={CLASS_NAME_M_2}>
						<Col>
							<EmbedEntityListOrTableContainer
								columns={messageColumns}
								data={this.props.data.makeOrder && this.props.data.makeOrder.message instanceof Array ? this.props.data.makeOrder.message : undefined}
								componentPath={this.props.componentPath+DOT_MAKE_ORDER_DOT_MESSAGE}
								filterable={false}
							/>
						</Col>
					</Form.Row>
				</div>;
		let goodsToOrderColumns = getExpandedColumns(C_CC_PARTNERS, [], {});
		goodsToOrderColumns.splice(0,0,
				getUpdatedColumn(REPORTS, {
						accessor: ACCESSOR_TO_ORDER_QUANTITY,
						isAggregate: true,
						filterable: true,
						sortable: false,
						aggregation: {
							op: SUM,
							operands: [{op: MINUS,operands: [DEFAULT_VENDOR_GOODS_RESERVE_QUANTITIES_INITIAL_QUANTITY,DEFAULT_VENDOR_GOODS_RESERVE_QUANTITIES_QUANTITY]}]
						},
						fluidSize: 2,
						dataType: UNIT,
					}, {}, C_RESERVE_QUANTITIES
				)
		);
		let orderGoodsRows = undefined;
		if( this.props.data.getOrderGoods instanceof Array ) {
			orderGoodsRows = this.props.data.getOrderGoods.filter((elem) => (elem.toOrderQuantity > 0));
		}
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={HEADER_TEXT} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<div className={PAGE_BODY}><Form>
					<Form.Row className={CLASS_NAME_M_2}>
						<ButtonGroup as={Col}>
							<EmbedRestCallButton componentPath={this.props.componentPath+DOT_GET_ORDER_GOODS} onClick={(e)=> {this.getOrderGoods(this.state.outId);}}>
								{LOAD_IT} <FontAwesomeIcon icon={SYNC}/>
							</EmbedRestCallButton>
						</ButtonGroup>
					</Form.Row>
					<Form.Row className={CLASS_NAME_M_2}>
						<Col>
							<ReactTable
								className={CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHTS}
								defaultPageSize={20}
								minRows={0}
								data={orderGoodsRows}
								columns={tableColumns}
								pivotBy={[VENDOR]}
								pageSize={20}
								showPagination={true}
	
								//manual // Forces table not to paginate or sort automatically, so we can handle it server-side
								page={this.props.page}
								pages={this.props.pages} // Display the total number of pages
								pageSize={this.props.pageSize}
								sortable={this.props.sortable}
								sorted={this.props.sorted}
								loading={this.props.data.getOrderGoods && this.props.data.getOrderGoods[Constants.PATH_FOR_LOADING] ? true : false} // Display the loading overlay when we need it
								filterable={true}
							/>
						</Col>
					</Form.Row>
					<Form.Row className={CLASS_NAME_M_2}>
						<Col>
								<FieldSelectContainer
									data={this.props.data}
									href={this.props.data && this.props.data._links && this.props.data._links[column.accessor] ? this.props.data._links[column.accessor].href : undefined}
									componentPath={this.props.data._componentPath+DOT+column.accessor} //existing path in redux store where we put data
									listType={column.entityType}
									listAttr={entityDefinition.displayAttr ? entityDefinition.displayAttr : ID}
									listDisplayFn={entityDefinition.displayFn instanceof Function ? entityDefinition.displayFn : ((item) => entityDefinition.displayAttr ? item[entityDefinition.displayAttr] : U_1F194+item[ID])}
									listExpandedColumns={column.expandColumns}
									listFilter={column.optionFilter}
									editable={this.props.data._editable}
									loading={this.props.data[Constants.PATH_FOR_LOADING]}
									creatable={false}
									onChange={(object) => {
										let outId = 0;
										if(object && object.id) {
											outId = object.id;
										}
										this.setState({outId: outId});
									}}
									pageURL={entityDefinition ? entityDefinition.pageURL : undefined}
								/>
						</Col>
						<ButtonGroup as={Col}>
							<EmbedRestCallButton componentPath={this.props.componentPath+DOT_GET_ORDER_GOODS} onClick={(e)=> {this.generateOrder()}}>
								{CREATE}
							</EmbedRestCallButton>
						</ButtonGroup>
					</Form.Row>
					{body}
				</Form></div>
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let componentPath = GENERATE_ORDER;
	let data = resolveObjectPath(componentPath,state.rest);
	data = {...data, _componentPath: componentPath, _editable: true};
	return {
		auth: state.auth,
		componentPath: componentPath,
		data: data,
		selectedRows: (data && data.vendors && data.vendors.selectedRows && data.vendors.selectedRows.map instanceof Map) ? data.vendors.selectedRows.map : new Map(),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData, resetRESTCallLimit }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageGenerateOrderContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
