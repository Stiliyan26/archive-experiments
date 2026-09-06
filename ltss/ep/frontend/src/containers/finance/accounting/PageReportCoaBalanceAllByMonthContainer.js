import React from "react";

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { withTranslation } from "react-i18next";

import { resolveObjectPath } from "../../../scripts/dataUtils";

import Header from "../../../components/generic/Header";
import { Form, Col } from "react-bootstrap";
import axios from "axios";
import * as Constants from "../../../static/constants";
import FieldSelectContainer from "../../fields/FieldSelectContainer"
import { dispatchEditRESTData } from "../../../actions/taskActions";
import ReactTable from "react-table-v6"
import lodash from "lodash"
import Select from "react-select";
import EmbedRestCallButton from "../../embeds/EmbedRestCallButton";


const currentYear = new Date().getFullYear();
const currentMonth = new Date().getMonth() + 1;
const months = [
	{label: "Начало на годината", value: "00"},
	{label: "Януари", value: "01"},
	{label: "Февруари", value: "02"},
	{label: "Март", value: "03"},
	{label: "Април", value: "04"},
	{label: "Май", value: "05"},
	{label: "Юни", value: "06"},
	{label: "Юли", value: "07"},
	{label: "Август", value: "08"},
	{label: "Септември", value: "09"},
	{label: "Октомври", value: "10"},
	{label: "Ноември", value: "11"},
	{label: "Декември", value: "12"},
	{label: "Годишно приключване", value: "13"}
]
const FROM_DATE_00 = "00"
const OUT_CODE_01 = "01"
const CONSOLIDATED_N = "N"
const REPORT_COA_BALANCE_ALL_BY_MONTH = "/reports/coaBalanceAllByMonth/"
const SLASH = "/"
const GET = "get"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const RESULT_P = ".result."
const DOT_RESULT = ".result"
const EMPTY = ""
const ERROR_MESSAGE = "Reports.ChooseType"
const HEADER_COA_PARENT_CODE = "Надсметка"
const ACCESSOR_COA_PARENT_CODE = "coa_parent_code"
const COA_PARENT_CODE = "coa_parent_code"
const PIVOT_VALUE_COA_PARENT_CODE = "Няма"
const NULL = "null"
const HEADER_COA_CODE = "Сметка"
const ACCESSOR_COA_CODE = "coa_code"
const HEADER_COA_NAME = "Описание"
const ACCESSOR_COA_NAME = "coa_name"
const COA_NAME_TEXT = "Общо за група от сметки "
const HEADER_F_DT_AMOUNT = "Начално салдо ДЕБИТ"
const ACCESSOR_F_DT_AMOUNT = "f_dt_amount"
const HEADER_F_CT_AMOUNT = "Начално салдо КРЕДИТ"
const ACCESSOR_F_CT_AMOUNT = "f_ct_amount"
const HEADER_O_DT_AMOUNT = "Оборот ДЕБИТ"
const ACCESSOR_O_DT_AMOUNT = "o_dt_amount"
const HEADER_O_CT_AMOUNT = "Оборот КРЕДИТ"
const ACCESSOR_O_CT_AMOUNT = "o_ct_amount"
const HEADER_S_DT_AMOUNT = "Крайно салдо ДЕБИТ"
const ACCESSOR_S_DT_AMOUNT = "s_dt_amount"
const HEADER_S_CT_AMOUNT = "Крайно салдо КРЕДИТ"
const ACCESSOR_S_CT_AMOUNT = "s_ct_amount"
const REPORTS_WRAPPER = "reports-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const PAGE_BODY = "page-body"
const COL_SM_12_FORM_GROUP = "col-sm-12 form-group"
const REPORTS_YEAR = "reports.Year"
const T_REPORTS_YEAR = "Reports.Year"
const COL_SM_12_FORM_CONTROL = "col-sm-12 form-control"
const NUMBER = "number"
const REPORTS_FROM_MONTH = "reports.fromMonth"
const T_REPORTS_FROM_MONTH = "Reports.FromMonth"
const REPORTS_TO_MONTH = "reports.toMonth"
const T_REPORTS_TO_MONTH = "Reports.ToMonth"
const REPORTS_DOT_PARAM_CHART_ACCOUNT = "reports.paramChartAccount"
const REPORTS_TYPE = "Reports.Type"
const CHART_ACCOUNT = "ChartAccount"
const COL_SM_3 = "col-sm-3"
const COL_SM_12 = "col-sm-12"
const CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHT = "clients-table align-center-table -striped -highlight"
const HEADER_TEXT = "Оборотна ведомост"
const CHOOSE_MONTH = "Choose month..."
const REFERENCE = "Справка"
const PARAM_CHART_ACCOUNT = ".paramChartAccount"
const LOI_TYPE_OF_FINANCIAL_ACCOUNTS = "loiTypeOfFinancialAccounts"
const LIST_OPTION_ITEM_NAME = "listOptionItemName"
const REPORTS_COA_BALANCE_BY_MONTHS = "reports.coaBalanceAllByMonth"


//Page: can be used as a landing page
//Container: redux container class
class PageReportCoaBalanceAllByMonthContainer extends React.Component {

	constructor(props){
		super(props);
		this.state = {
			fromDate: FROM_DATE_00,
			toDate: months[currentMonth].value.toString(),
			pYear: currentYear,
		}
	}
	
	retrieveData(pFromDate, pToDate, pSCoaType, pYear) {
		let outCode = OUT_CODE_01;
		let pSConsolidated = CONSOLIDATED_N;
		if(pSCoaType && pFromDate && pToDate && pYear) {
			import("moment").then(moment => {  //TODO we get an error, when we replace the literal("moment") with a constant
				let fromDate = pYear + pFromDate;
				let toDate = pYear + pToDate;
				let promise = axios({
					method: GET,
					url: API_URL+REPORT_COA_BALANCE_ALL_BY_MONTH + outCode + SLASH + fromDate + SLASH + toDate + SLASH + pSConsolidated + SLASH + pSCoaType,
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: data});
				}).catch(error => {
					console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: error});
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+ RESULT_P +Constants.PATH_FOR_LOADING, promise);
			});
		} else {
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_RESULT,new Error(this.props.t(ERROR_MESSAGE)));
		}
	}
	
	render() {
		let columns = [
					{
						Header: HEADER_COA_PARENT_CODE,
						accessor: ACCESSOR_COA_PARENT_CODE,
						PivotValue: (props) => {
							return props.value == NULL ? PIVOT_VALUE_COA_PARENT_CODE : props.value;
						},
					},
					{
						Header: HEADER_COA_CODE,
						accessor: ACCESSOR_COA_CODE,
						aggregate: vals => EMPTY,
					},
					{
						Header: HEADER_COA_NAME,
						accessor: ACCESSOR_COA_NAME,
						aggregate: (values, rows) => (COA_NAME_TEXT+rows[0].coa_parent_code),
						Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
					},
					{
						Header: HEADER_F_DT_AMOUNT,
						accessor: ACCESSOR_F_DT_AMOUNT,
						aggregate: vals => lodash.round(lodash.sum(vals),3),
						Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
						Footer: props => {
							let vals = props.data.map((rowData) => (rowData.f_dt_amount));
							return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
						},
					},
					{
						Header: HEADER_F_CT_AMOUNT,
						accessor: ACCESSOR_F_CT_AMOUNT,
						aggregate: vals => lodash.round(lodash.sum(vals),3),
						Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
						Footer: props => {
							let vals = props.data.map((rowData) => (rowData.f_ct_amount));
							return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
						},
					},
					{
						Header: HEADER_O_DT_AMOUNT,
						accessor: ACCESSOR_O_DT_AMOUNT,
						aggregate: vals => lodash.round(lodash.sum(vals),3),
						Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
						Footer: props => {
							let vals = props.data.map((rowData) => (rowData.o_dt_amount));
							return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
						},
					},
					{
						Header: HEADER_O_CT_AMOUNT,
						accessor: ACCESSOR_O_CT_AMOUNT,
						aggregate: vals => lodash.round(lodash.sum(vals),3),
						Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
						Footer: props => {
							let vals = props.data.map((rowData) => (rowData.o_ct_amount));
							return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
						},
					},
					{
						Header: HEADER_S_DT_AMOUNT,
						accessor: ACCESSOR_S_DT_AMOUNT,
						aggregate: vals => lodash.round(lodash.sum(vals),3),
						Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
						Footer: props => {
							let vals = props.data.map((rowData) => (rowData.s_dt_amount));
							return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
						},
					},
					{
						Header: HEADER_S_CT_AMOUNT,
						accessor: ACCESSOR_S_CT_AMOUNT,
						aggregate: vals => lodash.round(lodash.sum(vals),3),
						Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
						Footer: props => {
							let vals = props.data.map((rowData) => (rowData.s_ct_amount));
							return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
						},
					},
			];
		let reportData = this.props.data && this.props.data.result instanceof Array ? this.props.data.result : undefined;
		return (
			<div className={REPORTS_WRAPPER}>
				<Header text={this.props.t(HEADER_TEXT)} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<div className={PAGE_BODY}>
					<div className={COL_SM_12_FORM_GROUP}>
						<Form>
							<Form.Row>
								<Col>
									<Form.Group controlId={REPORTS_YEAR}>
										<Form.Label>{this.props.t(T_REPORTS_YEAR)}: </Form.Label>
										<div>
											<input
												className={COL_SM_12_FORM_CONTROL}
												type={NUMBER}
												defaultValue={currentYear}
												onChange={(e) => {this.setState({pYear: e.target.value });}}
											/>
										</div>
									</Form.Group>
								</Col>
								<Col>
									<Form.Group controlId={REPORTS_FROM_MONTH}>
										<Form.Label>{this.props.t(T_REPORTS_FROM_MONTH)}: </Form.Label>
										<Select options={months}
												placeholder={this.props.t(CHOOSE_MONTH)}
												defaultValue={months[0]}
												onChange={(e) => {this.setState({fromDate: e.value});}}
										/>
									</Form.Group>
								</Col>
								<Col>
									<Form.Group controlId={REPORTS_TO_MONTH}>
										<Form.Label>{this.props.t(T_REPORTS_TO_MONTH)}: </Form.Label>
										<Select options={months}
												placeholder={this.props.t(CHOOSE_MONTH)}
												defaultValue={months[currentMonth]}
												onChange={(e) => {this.setState({toDate: e.value});}}
												
										/>
									</Form.Group>
								</Col>
								<Col>
									<Form.Group controlId={REPORTS_DOT_PARAM_CHART_ACCOUNT}>
										<Form.Label>{this.props.t(REPORTS_TYPE)}: </Form.Label>
										<FieldSelectContainer 
											id={CHART_ACCOUNT}
											componentPath={this.props.componentPath+PARAM_CHART_ACCOUNT} //existing path in redux store where we put data
											listType={LOI_TYPE_OF_FINANCIAL_ACCOUNTS}
											listAttr={LIST_OPTION_ITEM_NAME}
											editable={true}
											creatable={false}
										/>
									</Form.Group>
								</Col>
							</Form.Row>
							<Form.Row>
								<div className={COL_SM_3}>
									<EmbedRestCallButton componentPath={this.props.componentPath+DOT_RESULT} onClick={(e) => {
											this.retrieveData(this.state.fromDate, this.state.toDate, this.props.data ? this.props.data.paramChartAccount.listOptionItemCode : undefined, this.state.pYear);
										}}>{this.props.t(REFERENCE)}</EmbedRestCallButton>
								</div>
							</Form.Row>
						</Form>
					</div>
					<div className={COL_SM_12}>
						<ReactTable
							className={CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHT}
							data={reportData}
							columns={columns}
							defaultPageSize={10}
							showPagination={true}
							pivotBy={[COA_PARENT_CODE]}
						/>
					</div>
				</div>
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let componentPath = REPORTS_COA_BALANCE_BY_MONTHS;
	let data = resolveObjectPath(componentPath,state.rest); //rest because of fetchREST
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageReportCoaBalanceAllByMonthContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
