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

import { getCombinedEntityDefinitions, getEntityDefinition, getUpdatedColumn } from "../nomenclatures/entityDefinitions.js"
import { dispatchEditRESTData } from "../../actions/taskActions";
import { resolveObjectPath } from "./../../scripts/dataUtils";
import FieldSelectContainer from "../fields/FieldSelectContainer"
import FieldDateContainer from "../fields/FieldDateContainer"

const LOGIN = "/login"
const GET = "get"
const REPORTS_GET_STOCKS = "/reports/getStocks/"
const SLASH = "/"
const DD_MM_YYYY = "DDMMYYYY"
const DOT_GET_STOCKS_DOT = ".getStocks."
const DOT_GET_STOCKS = ".getStocks"
const C_STOCKS = "cStocks"
const OUT_CODE = "outCode"
const OUT_CODE_LABEL = "Склад"
const FROM_DATE = "fromDate"
const FROM_DATE_LABEL = "Reports.FromDate"
const TO_DATE_LABEL = "Reports.ToDate"
const TO_DATE = "toDate"
const DOT = "."
const C_CC_ORGANIZATION_UNITS = "cCcOrganizationUnits"
const NOMENCLATURES_WRAPPER = "nomenclatures-wrapper"
const GOD_ID = "godId"
const ENTITY = "ENTITY"
const C_GOODSES = "cGoodses"
const C_GOODS = "CGoods"
const GOD_ID_DOT_MEE_ID = "godId.meeId"
const MEE_ID = "meeId"
const C_MEASURES = "cMeasures"
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
const FREE = "free"
const AVG_PRICE = "avgPrice"
const ID = "id"
const U_1F194 = "\u{1F194}"
const REPORTS = "Reports"
const UNIT = "UNIT"
const HEADER_TEXT = "Складова наличност"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const PAGE_BODY = "page-body"
const COL_SM_12_FORM_GROUP = "col-sm-12 form-group"
const COL_SM_4 = "col-sm-4"
const COL_SM_3 = "col-sm-3"
const COL_SM_12 = "col-sm-12"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHTS = "clients-table align-center-table -striped -highlight"
const REPORT_STOCKS = "reportStocks"

//Page: can be used as a landing page
//Container: redux container class
class PageReportStocksContainer extends React.Component {

	constructor(props){
		super(props);
	}

	getStocks(outCode, fromDate, toDate){
		if(outCode && fromDate && toDate) {
			import("moment").then(moment => { //TODO we get an error, when we replace the literal("moment") with a constant
				let promise = axios({
					method: GET,
					url: API_URL+REPORTS_GET_STOCKS + outCode + SLASH + moment(fromDate).format(DD_MM_YYYY) + SLASH + moment(toDate).format(DD_MM_YYYY),
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					data = data.map((datarow, index) => ({
						...datarow,
						_componentPath: this.props.componentPath+DOT_GET_STOCKS_DOT+index,
					}));
					//console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_GET_STOCKS,data);
				}).catch(error => {
					//console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_GET_STOCKS,error);
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_GET_STOCKS_DOT+Constants.PATH_FOR_LOADING,promise);
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
		let tableColumns = [];
		tableColumns.push(getUpdatedColumn(rootDefinition.className, 
			{
				//god_id_id
				accessor: GOD_ID,
				fluidSize: 3,
				dataType: ENTITY,
				entityType: C_GOODSES,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(C_GOODS,
			{
				//mee_cofficient
				accessor: GOD_ID_DOT_MEE_ID,
				_fieldPath: MEE_ID,
				_fieldRelPath: GOD_ID_DOT_MEE_ID,
				fluidSize: 2,
				dataType: ENTITY,
				entityType: C_MEASURES,
			}, [], C_STOCKS));
		tableColumns.push(getUpdatedColumn(rootDefinition.className, 
			{
				accessor: OUT_CODE,  //TODO ASK FOR THE ENTITY TYPE
				fluidSize: 3,
				dataType: ENTITY,
				entityType: C_MEASURES
			}, [], C_STOCKS));
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
							data={this.props.data.getStocks instanceof Array ? this.props.data.getStocks : undefined}
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
		let fromDateColumn = {
			accessor: FROM_DATE,
		};
		let toDateColumn = {
			accessor: TO_DATE,
		};
		let entityDefinition =  getCombinedEntityDefinitions()[outCodeColumn.entityType];
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
								loading={this.props.loading}
								creatable={false}
								onChange={(object) => {
									if(object && object.id) {
										this.getStocks(object.id, this.props.data[fromDateColumn.accessor], this.props.data[toDateColumn.accessor]);
									}
								}}
								pageURL={entityDefinition ? entityDefinition.pageURL : undefined}
							/>
						</div>
						<div className={COL_SM_3}>
							<label htmlFor={FROM_DATE}>{this.props.t(FROM_DATE_LABEL)}</label>
							<FieldDateContainer
								id={FROM_DATE}
								componentPath={this.props.data._componentPath+DOT+fromDateColumn.accessor} //existing path in redux store where we put data
								editable={true}
								loading={this.props.loading}
								onChange={(value) => {
									if(this.props.data && this.props.data[outCodeColumn.accessor]) {
										this.getStocks(this.props.data[outCodeColumn.accessor].id, value, this.props.data[toDateColumn.accessor]);
									}
								}}
								//showTimeSelect
							/>
						</div>
						<div className={COL_SM_3}>
							<label htmlFor={TO_DATE}>{this.props.t(TO_DATE_LABEL)}</label>
							<FieldDateContainer
								id={TO_DATE}
								componentPath={this.props.data._componentPath+DOT+toDateColumn.accessor} //existing path in redux store where we put data
								editable={true}
								loading={this.props.loading}
								onChange={(value) => {
									if(this.props.data && this.props.data[outCodeColumn.accessor]) {
										this.getStocks(this.props.data[outCodeColumn.accessor].id, this.props.data[fromDateColumn.accessor], value);
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
	let componentPath = REPORT_STOCKS;
	let data = resolveObjectPath(componentPath,state.rest);
	data = {...data, _componentPath: componentPath, _editable: true};
	return {
		auth: state.auth,
		componentPath: componentPath,
		data: data,
		loading: ownProps.loading ? ownProps.loading : (data && data.getStocks ? data.getStocks[Constants.PATH_FOR_LOADING] : undefined),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageReportStocksContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
