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

const LOGIN = "/login"
const GET = "get"
const REPORTS_GET_ORDER_GOODS = "/reports/getOrderGoods/"
const DOT_MAKE_ORDER_GOODS_DOT = ".makeOrderGoods."
const DOT_MAKE_ORDER_GOODS = ".makeOrderGoods"
const C_ORDER_DETAILS = "cOrderDetails"
const OUT_CODE = "outCode"
const DOT = "."
const C_CC_ORGANIZATION_UNITS = "cCcOrganizationUnits"
const NOMENCLATURES_WRAPPER = "nomenclatures-wrapper"
const RQY_QUANTITY = "rqyQuantity"
const STK_QUANTITY = "stkQuantity"
const TO_ORDER_QUANTITY = "toOrderQuantity"
const QUANTITY_NOT_CONFIRMED = "quantityNotConfirmed"
const QUANTITY_CONFIRM = "quantityConfirm"
const TEXT = "TEXT"
const FILTERED_ENTITY = "FILTERED_ENTITY"
const C_CC_PARTNERS = "cCcPartners"
const GOD_ID = "godId"
const ENTITY = "ENTITY"
const C_GOODSES = "cGoodses"
const C_GOODS = "CGoods"
const GOD_ID_DOT_DEFAULT_VENDOR = "godId.defaultVendor"
const DEFAULT_VENDOR = "defaultVendor"
const ID = "id"
const U_1F194 = "\u{1F194}"
const REPORTS = "Reports"
const UNIT = "UNIT"
const FORM_CONTROL = "form-control"
const HEADER_TEXT = "Стоки за поръчка"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const PAGE_BODY = "page-body"
const COL_SM_12_FORM_GROUP = "col-sm-12 form-group"
const COL_SM_12 = "col-sm-12"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHTS = "clients-table align-center-table -striped -highlight"
const REPORT_GOODS_TO_ORDER = "reportGoodsToOrder"

//Page: can be used as a landing page
//Container: redux container class
class PageReportGoodsToOrderContainer extends React.Component {

	constructor(props){
		super(props);
	}
	
	

	makeOrderGoods(outId){
		axios({
			method: GET,
			url: API_URL+REPORTS_GET_ORDER_GOODS + outId,
			headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
		}).then(response => {
			let data = response.data;
			data = data.map((datarow, index) => ({
				...datarow,
				_componentPath: this.props.componentPath+DOT_MAKE_ORDER_GOODS_DOT+index,
			}));
			//console.log(data);
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_MAKE_ORDER_GOODS,data);
		}).catch(error => {
			//console.log(error)
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_MAKE_ORDER_GOODS,error);
		});
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}

	render() {
		let rootDefinition = getEntityDefinition(C_ORDER_DETAILS, {});
		let tableColumns = [];
		tableColumns.push(getUpdatedColumn(rootDefinition.className, 
			{
				//god_id_id
				accessor: GOD_ID,
				fluidSize: 3,
				dataType: ENTITY,
				entityType: C_GOODSES,
			}, [], C_ORDER_DETAILS));
		tableColumns.push(getUpdatedColumn(C_GOODS,
			{
				//mee_cofficient
				accessor: GOD_ID_DOT_DEFAULT_VENDOR,
				_fieldPath: DEFAULT_VENDOR,
				_fieldRelPath: GOD_ID_DOT_DEFAULT_VENDOR,
				fluidSize: 2,
				dataType: FILTERED_ENTITY,
				entityType: C_CC_PARTNERS,
			}, [], C_ORDER_DETAILS));
		/*
		tableColumns.push(getUpdatedColumn(rootDefinition.className, 
			{
				//mee_id_id
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			}, [], "cOrderDetails"));
		tableColumns.push(getUpdatedColumn("CMeasure", 
			{
				//mee_cofficient
				accessor: "meeId.cofficient",
				_fieldPath: "cofficient",
				_fieldRelPath: "meeId.cofficient",
				fluidSize: 2,
				dataType: "UNIT",
			}, [], "cOrderDetails"));
		*/
		tableColumns.push(getUpdatedColumn(rootDefinition.className, 
			{
				//rqy_quantity
				accessor: RQY_QUANTITY,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_ORDER_DETAILS));
		tableColumns.push(getUpdatedColumn(rootDefinition.className, 
			{
				//stk_quantity
				accessor: STK_QUANTITY,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_ORDER_DETAILS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: TO_ORDER_QUANTITY,
				fluidSize: 2,
				dataType: UNIT,
				isCalculated: true,
				Cell: (props) => <input className={FORM_CONTROL} type={TEXT} value={props.original.rqyQuantity + props.original.stkQuantity - props.original.quantityConfirm} disabled={true}/>
			}, [], C_ORDER_DETAILS));
		tableColumns.push(getUpdatedColumn(REPORTS,
			{
				accessor: QUANTITY_NOT_CONFIRMED,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_ORDER_DETAILS));
		tableColumns.push(getUpdatedColumn(rootDefinition.className, 
			{
				//quantity_confirm
				accessor: QUANTITY_CONFIRM,
				fluidSize: 2,
				dataType: UNIT,
			}, [], C_ORDER_DETAILS));
		let body = <ReactTable
							className={CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHTS}
							defaultPageSize={20}
							minRows={0}
							data={this.props.data.makeOrderGoods}
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
		let column = {
			accessor: OUT_CODE,
			entityType: C_CC_ORGANIZATION_UNITS,
		};
		let entityDefinition =  getCombinedEntityDefinitions()[column.entityType];
		return (
			<div className={NOMENCLATURES_WRAPPER}>
				<Header text={HEADER_TEXT} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<div className={PAGE_BODY}>
					<div className={COL_SM_12_FORM_GROUP}>
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
								if(object && object.id) {
									this.makeOrderGoods(object.id);
								}
							}}
							pageURL={entityDefinition ? entityDefinition.pageURL : undefined}
						/>
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
	let componentPath = REPORT_GOODS_TO_ORDER;
	let data = resolveObjectPath(componentPath,state.rest);
	data = {...data, _componentPath: componentPath, _editable: true};
	return {
		auth: state.auth,
		componentPath: componentPath,
		data: data,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageReportGoodsToOrderContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
