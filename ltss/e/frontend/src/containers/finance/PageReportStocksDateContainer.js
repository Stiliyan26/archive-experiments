import React from "react";
import axios from "axios";
import ReactTable from "react-table-v6"

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"
import { withTranslation } from "react-i18next";

import history from "../../scripts/history"
import { isAuthenticated } from "../../components/pages/login/Login.js"
import * as Constants from "./../../static/constants";

import Header from "../../components/generic/Header"

import { getCombinedEntityDefinitions, getEntityDefinition, getUpdatedColumn, getExpandedColumns } from "../nomenclatures/entityDefinitions.js"
import { dispatchEditRESTData } from "../../actions/taskActions";
import { resolveObjectPath } from "./../../scripts/dataUtils";
import FieldSelectContainer from "../fields/FieldSelectContainer"
import FieldDateContainer from "../fields/FieldDateContainer"

const LOGIN = "/login"
const GET = "get"
const REPORTS_GET_STOCKS_DATE = "/reports/getStocksDate/"
const SLASH = "/"
const DD_MM_YYYY = "DDMMYYYY"
const DOT_GET_STOCKS_DATE_DOT = ".getStocksDate."
const DOT_GET_STOCKS_DATE = ".getStocksDate"
const C_STOCKS = "cStocks"
const C_BLOCKED_QUANTITIES = "cBlockedQuantities"
const STK_ID_DOT_GOD_ID_DOT_NAME_BG = "stkId.godId.nameBg"
const STK_ID_DOT_GOD_ID_DOT_CODE = "stkId.godId.code"
const STK_ID_DOT_GOD_ID_DOT_BARCODE = "stkId.godId.barcode"
const STK_ID_DOT_GOD_ID_DOT_GOOD_MARK_DOT_MARK_CODE = "stkId.godId.goodMark.markCode"
const STK_ID_DOT_OUT_CODE_DOT_NAME = "stkId.outCode.name"
const STK_ID_DOT_PAR_ID_DOT_NAME = "stkId.parId.name"
const STK_ID_DOT_GOD_ID_DOT_MEE_ID_DOT_NAME = "stkId.godId.meeId.name"
const OUT_CODE = "outCode"
const PARTNER = "partner"
const PARTNER_LABEL = "Доставчик"
const C_CC_PARTNERS = "cCcPartners"
const OUT_CODE_LABEL = "Склад"
const TO_DATE_LABEL = "Reports.ToDate"
const TO_DATE = "toDate"
const DOT = "."
const C_CC_ORGANIZATION_UNITS = "cCcOrganizationUnits"
const NOMENCLATURES_WRAPPER = "nomenclatures-wrapper"
const AVAILABLE = "available"
const DELIVERED = "delivered"
const SOLD = "sold"
const AVAILABLE_COST = "availableCost"
const DELIVERED_COST = "deliveredCost"
const SOLD_COST = "soldCost"
const DELIVERED_BEFORE = "deliveredBefore"
const SOLD_BEFORE = "soldBefore"
const RESERVE_QUANTITY = "reserveQuantity"
const BLOCKED_QUANTITY = "blockedQuantity"
const CONSIGNMENT_QUANTITY = "consignmentQuantity"
const FREE = "free"
const AVG_PRICE = "avgPrice"
const ID = "id"
const U_1F194 = "\u{1F194}"
const REPORTS = "Reports"
const UNIT = "UNIT"
const HEADER_TEXT = "Складова наличност към дата"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const PAGE_BODY = "page-body"
const COL_SM_12_FORM_GROUP = "col-sm-12 form-group"
const COL_SM_4 = "col-sm-4"
const COL_SM_3 = "col-sm-3"
const COL_SM_12 = "col-sm-12"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHTS = "clients-table align-center-table -striped -highlight"
const REPORT_STOCKS_DATE = "reportStocksDate"

//Page: can be used as a landing page
//Container: redux container class
class PageReportStocksDateContainer extends React.Component {

	constructor(props){
		super(props);
	}

	getStocksDate(outCode, partner, toDate){
		if(outCode && partner && toDate) {
			import("moment").then(moment => {//TODO we get an error, when we replace the literal("moment") with a constant
				let promise = axios({
					method: GET,
					url: API_URL+REPORTS_GET_STOCKS_DATE + outCode + SLASH + partner + SLASH + moment(toDate).format(DD_MM_YYYY),
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					data = data.map((datarow, index) => ({
						...datarow,
						_componentPath: this.props.componentPath+DOT_GET_STOCKS_DATE_DOT+index,
					}));
					//console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_GET_STOCKS_DATE,data);
				}).catch(error => {
					//console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_GET_STOCKS_DATE,error);
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_GET_STOCKS_DATE_DOT+Constants.PATH_FOR_LOADING,promise);
			});
		}
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}

	render() {
		let rootDefinition = getEntityDefinition(C_STOCKS, {});
		//use fake cBlockedQuantities only to get the stocks that is in one of the columns 
		let tableColumns = getExpandedColumns(C_BLOCKED_QUANTITIES, [STK_ID_DOT_GOD_ID_DOT_NAME_BG,STK_ID_DOT_GOD_ID_DOT_CODE,STK_ID_DOT_GOD_ID_DOT_BARCODE,STK_ID_DOT_GOD_ID_DOT_GOOD_MARK_DOT_MARK_CODE,STK_ID_DOT_OUT_CODE_DOT_NAME,STK_ID_DOT_PAR_ID_DOT_NAME,STK_ID_DOT_GOD_ID_DOT_MEE_ID_DOT_NAME], {blkId: {show: false},meeId: {show: false},effectiveDate: {show: false},quantity: {show: false},stkId: {show: false}});
		/*
		tableColumns.push(getUpdatedColumn(rootDefinition.className, 
			{
				//god_id_id
				accessor: "stkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cStocks",
			}, [], "cStocks"));
		*/
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: AVAILABLE,//Наличност
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: DELIVERED,//Доставени
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: SOLD,//Продадени
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: AVAILABLE_COST, //Обща ст-ст
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: DELIVERED_COST,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: SOLD_COST,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: DELIVERED_BEFORE,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: SOLD_BEFORE,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: RESERVE_QUANTITY,//Резервирано к-во
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: BLOCKED_QUANTITY,//Блокирано к-во
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: CONSIGNMENT_QUANTITY,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: FREE,//Свободно
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: AVG_PRICE,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_STOCKS));
		let body = <ReactTable
							className={CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHTS}
							defaultPageSize={20}
							minRows={0}
							data={this.props.data.getStocksDate instanceof Array ? this.props.data.getStocksDate : undefined}
							columns={tableColumns}
							pageSize={20}
							showPagination={true}

							//manual // Forces table not to paginate or sort automatically, so we can handle it server-side
							page={this.props.page}
							pages={this.props.pages} // Display the total number of pages
							pageSize={this.props.pageSize}
							sortable={this.props.sortable}
							sorted={this.props.sorted}
							loading={this.props.loading ? true : false} // Display the loading overlay when we need it
							filterable={true}
						/>;
		let outCodeColumn = {
			accessor: OUT_CODE,
			entityType: C_CC_ORGANIZATION_UNITS,
		};
		let partnerColumn = {
			accessor: PARTNER,
			entityType: C_CC_PARTNERS,
		};
		let toDateColumn = {
			accessor: TO_DATE,
		};
		let entityDefinition =  getCombinedEntityDefinitions()[outCodeColumn.entityType];
		let partnerDef =  getCombinedEntityDefinitions()[partnerColumn.entityType];
		return (
			<div className={NOMENCLATURES_WRAPPER}>
				<Header text={HEADER_TEXT} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<div className={PAGE_BODY}>
					<div className={COL_SM_12_FORM_GROUP}>
						<div className={COL_SM_4}>
							<label htmlFor={OUT_CODE}>{OUT_CODE_LABEL}</label>
							<FieldSelectContainer 
								id={OUT_CODE}
								data={this.props.data}
								href={this.props.data && this.props.data._links && this.props.data._links[outCodeColumn.accessor] ? this.props.data._links[outCodeColumn.accessor].href : undefined}
								componentPath={this.props.data._componentPath+DOT+outCodeColumn.accessor} //existing path in redux store where we put data
								listType={outCodeColumn.entityType}
								listAttr={entityDefinition.displayAttr ? entityDefinition.displayAttr : ID}
								listDisplayFn={entityDefinition.displayFn instanceof Function ? entityDefinition.displayFn : ((item) => entityDefinition.displayAttr ? item[entityDefinition.displayAttr] : U_1F194+item[ID])}
								listExpandedColumns={outCodeColumn.expandColumns}
								listFilter={outCodeColumn.optionFilter}
								editable={this.props.data._editable}
								loading={this.props.loading ? true : false}
								creatable={false}
								onChange={(object) => {
									if(object && object.id && this.props.data && this.props.data[partnerColumn.accessor] && this.props.data[toDateColumn.accessor]) {
										this.getStocksDate(object.id, this.props.data[partnerColumn.accessor].id, this.props.data[toDateColumn.accessor]);
									}
								}}
								pageURL={entityDefinition ? entityDefinition.pageURL : undefined}
							/>
						</div>
						<div className={COL_SM_3}>
							<label htmlFor={PARTNER}>{PARTNER_LABEL}</label>
							<FieldSelectContainer 
								id={PARTNER}
								data={this.props.data}
								href={this.props.data && this.props.data._links && this.props.data._links[partnerColumn.accessor] ? this.props.data._links[partnerColumn.accessor].href : undefined}
								componentPath={this.props.data._componentPath+DOT+partnerColumn.accessor} //existing path in redux store where we put data
								listType={partnerColumn.entityType}
								listAttr={partnerDef.displayAttr ? partnerDef.displayAttr : ID}
								listDisplayFn={partnerDef.displayFn instanceof Function ? partnerDef.displayFn : ((item) => partnerDef.displayAttr ? item[partnerDef.displayAttr] : U_1F194+item[ID])}
								listExpandedColumns={partnerColumn.expandColumns}
								listFilter={partnerColumn.optionFilter}
								editable={this.props.data._editable}
								loading={this.props.loading}
								creatable={false}
								onChange={(object) => {
									if(object && object.id && this.props.data && this.props.data[outCodeColumn.accessor] && this.props.data[toDateColumn.accessor]) {
										this.getStocksDate(this.props.data[outCodeColumn.accessor].id, object.id, this.props.data[toDateColumn.accessor]);
									}
								}}
								pageURL={partnerDef ? partnerDef.pageURL : undefined}
							/>
						</div>
						<div className={COL_SM_3}>
							<label htmlFor={TO_DATE}>{this.props.t(TO_DATE_LABEL)}</label>
							<FieldDateContainer
								id={TO_DATE}
								componentPath={this.props.data._componentPath+DOT+toDateColumn.accessor} //existing path in redux store where we put data
								editable={true}
								loading={this.props.data[Constants.PATH_FOR_LOADING]}
								onChange={(value) => {
									if(this.props.data && this.props.data[outCodeColumn.accessor] && this.props.data[partnerColumn.accessor]) {
										this.getStocksDate(this.props.data[outCodeColumn.accessor].id, this.props.data[partnerColumn.accessor].id, value);
									}
								}}
								//showTimeSelect
							/>
						</div>
					</div>
					<div className={COL_SM_12}>
						{body}
					</div>
				</div>
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let componentPath = REPORT_STOCKS_DATE;
	let data = resolveObjectPath(componentPath,state.rest);
	data = {...data, _componentPath: componentPath, _editable: true};
	return {
		auth: state.auth,
		componentPath: componentPath,
		data: data,
		loading: ownProps.loading ? ownProps.loading : (data && data.getStocksDate ? data.getStocksDate[Constants.PATH_FOR_LOADING] : undefined),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageReportStocksDateContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
