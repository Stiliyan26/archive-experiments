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
const REPORT_PAR_BALANCE_BY_MONTHS = "/reports/parBalanceByMonths/"
const REPORT_PAR_BALANCE_BY_MONTHS2 = "/reports/parBalanceByMonths2/"
const REPORT_PAR_BALANCE_BY_MONTHS_NULL = "/reports/parBalanceByMonthsNull/"
const SLASH = "/"
const GET = "get"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const DOT_RESULT_DOT = ".result."
const DOT_RESULT = ".result"
const ERROR_MESSAGE = "Reports.ChooseAllFields"
const HEADER_COA_CODE = "Код сметка"
const ACCESSOR_COA_CODE = "coa_code"
const NULL = "null"
const PIVOT_VALUE_COA_CODE = "Няма"
const PIVOT_VALUE_COA_CODE_MESSAGE = "Общо за сметка "
const PIVOT_VALUE_COA_CODE_TOTAL = "ОБЩО:"
const HEADER_COA_NAME = "Сметка"
const ACCESSOR_COA_NAME = "coa_name"
const HEADER_PAR_VAT_NO = "Ид. №"
const ACCESSOR_PAR_VAT_NO = "par_vat_no"
const HEADER_PAR_NAME = "Контрагент"
const ACCESSOR_PAR_NAME = "par_name"
const HEADER_F_PAR_DT_AMOUNT = "Начално салдо ДЕБИТ"
const ACCESSOR_F_PAR_DT_AMOUNT = "f_par_dt_amount"
const HEADER_F_PAR_CT_AMOUNT = "Начално салдо КРЕДИТ"
const ACCESSOR_F_PAR_CT_AMOUNT = "f_par_ct_amount"
const HEADER_O_PAR_DT_AMOUNT = "Оборот ДЕБИТ"
const ACCESSOR_O_PAR_DT_AMOUNT = "o_par_dt_amount"
const HEADER_O_PAR_CT_AMOUNT = "Оборот КРЕДИТ"
const ACCESSOR_O_PAR_CT_AMOUNT = "o_par_ct_amount"
const HEADER_S_PAR_DT_AMOUNT = "Крайно салдо ДЕБИТ"
const ACCESSOR_S_PAR_DT_AMOUNT = "s_par_dt_amount"
const HEADER_S_PAR_CT_AMOUNT = "Крайно салдо КРЕДИТ"
const ACCESSOR_S_PAR_CT_AMOUNT = "s_par_ct_amount"
const CONSOLIDATE_MESSAGE_FIRST = "ОБОРОТНА ВЕДОМОСТ ПО КОНТРАГЕНТИ"
const CONSOLIDATE_MESSAGE_SECOND = "КОНСОЛИДИРАНА ОБОРОТНА ВЕДОМОСТ ПО КОНТРАГЕНТИ"
const HEADER_TEXT = "Оборотна ведомост по контрагент"
const COL_SM_3 = "col-sm-3"
const COL_MD_5_MX_AUTO_FONT_WEIGHT_BOLD = "col-md-5 mx-auto font-weight-bold"
const COL_MD_2_MX_AUTO = "col-md-2 mx-auto"
const COL_SM_12_ALIGN_CENTER_TABLE = "col-sm-12 align-center-table"
const COL_SM_12 = "col-sm-12"
const CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHT = "clients-table align-center-table -striped -highlight"
const REPORTS_WRAPPER = "reports-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const PAGE_BODY = "page-body"
const COL_SM_12_FORM_GROUP = "col-sm-12 form-group"
const REPORTS_YEAR = "reports.Year"
const T_REPORTS_YEAR = "Reports.Year"
const COL_SM_12_FORM_CONTROL = "col-sm-12 form-control"
const NUMBER = "number"
const CHOOSE_MONTH = "Choose month..."
const REPORTS_FROM_MONTH = "reports.fromMonth"
const T_REPORTS_FROM_MONTH = "Reports.FromMonth"
const REPORTS_TO_MONTH = "reports.toMonth"
const T_REPORTS_TO_MONTH = "Reports.ToMonth"
const REPORTS_DOT_PARAM_CHART_ACCOUNT = "reports.paramChartAccount"
const CHART_ACCOUNT = "ChartAccount"
const REPORTS_DOT_PARAM_C_C_PARTNER = "reports.paramCCPartner"
const C_C_PARTNER = "CCPartner"
const F_CHART_ACCOUNT_LABEL = "Reports.Account"
const PARAM_CHART_ACCOUNT = ".paramChartAccount"
const F_CHART_ACCOUNTS = "fChartAccounts"
const NAME = "name"
const DASH = " - "
const U_1F194 = "\u{1F194}"
const CODE = "code"
const C_CC_PARTNER_LABEL = "Контрагент"
const PARAM_CC_PARTNER = ".paramCCPartner"
const C_CC_PARTNERS = "cCcPartners"
const REFERENCE_FIRST = "Справка по сметка"
const REFERENCE_SECOND = "Справка по ПРЦ"
const REFERENCE_THIRD = "Справка със Салдо"
const EMPTY = ""
const PIVOT_BY_COA_CODE = "coa_code"
const COMPONENT_PATH_REPORTS_PAR_BALANCE_BY_MONTHS = "reports.parBalanceByMonths"

//Page: can be used as a landing page
//Container: redux container class
class PageReportParBalanceByMonthsContainer extends React.Component {
	
	constructor(props){
		super(props);
		this.state = {
			fromDate: FROM_DATE_00,
			toDate: months[currentMonth].value,
			pConsolidated: CONSOLIDATED_N,
			pYear: currentYear
		}
	}
	
	retrieveData(pFromDate, pToDate, pICoaId, pIParId,pYear) {
		let outCode = OUT_CODE_01;
		let pSConsolidated = CONSOLIDATED_N;
		if(pICoaId && pFromDate && pToDate && pIParId && pYear) {
			import("moment").then(moment => { //TODO we get an error, when we replace the literal("moment") with a constant
				let fromDate = pYear + pFromDate;
				let toDate = pYear + pToDate;
				let promise = axios({
					method: GET,
					url: API_URL+REPORT_PAR_BALANCE_BY_MONTHS + outCode + SLASH + fromDate + SLASH + toDate + SLASH + pICoaId + SLASH + pSConsolidated + SLASH + pIParId,
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: data});
				}).catch(error => {
					console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: error});
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_RESULT_DOT+Constants.PATH_FOR_LOADING, promise);
			});
		} else {
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_RESULT,new Error(this.props.t(ERROR_MESSAGE)));
		}
	}
	
	retrieveDataSec(pFromDate, pToDate, pICoaId, pIParId, pYear) {
		let outCode = OUT_CODE_01;
		let pSConsolidated = CONSOLIDATED_N;
		if(pICoaId && pFromDate && pToDate && pIParId && pYear) {
			import("moment").then(moment => { //TODO we get an error, when we replace the literal("moment") with a constant
				let fromDate = pYear + pFromDate;
				let toDate = pYear + pToDate;
				let promise = axios({
					method: GET,
					url: API_URL+REPORT_PAR_BALANCE_BY_MONTHS2 + outCode + SLASH + fromDate + SLASH + toDate + SLASH + pICoaId + SLASH + pSConsolidated + SLASH + pIParId,
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: data});
				}).catch(error => {
					console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: error});
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_RESULT_DOT+Constants.PATH_FOR_LOADING, promise);
			});
		} else {
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_RESULT,new Error(this.props.t(ERROR_MESSAGE)));
		}
	}
	
	retrieveDataNull(pFromDate, pToDate, pICoaId, pIParId, pYear) {
		let outCode = OUT_CODE_01;
		let pSConsolidated = CONSOLIDATED_N;
		if(pICoaId && pFromDate && pToDate && pIParId && pYear) {
			import("moment").then(moment => { //TODO we get an error, when we replace the literal("moment") with a constant
				let fromDate = pYear + pFromDate;
				let toDate = pYear + pToDate;
				let promise = axios({
					method: GET,
					url: API_URL+REPORT_PAR_BALANCE_BY_MONTHS_NULL + outCode + SLASH + fromDate + SLASH + toDate + SLASH + pICoaId + SLASH + pSConsolidated + SLASH + pIParId,
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: data});
				}).catch(error => {
					console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: error});
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_RESULT_DOT+Constants.PATH_FOR_LOADING, promise);
			});
		} else {
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_RESULT,new Error(this.props.t(ERROR_MESSAGE)));
		}
	}
	
	render() {
		let columns = [
			{
				Header: HEADER_COA_CODE,
				accessor: ACCESSOR_COA_CODE,
				PivotValue: (props) => {
					return props.value === NULL ? PIVOT_VALUE_COA_CODE : <span><b><i>{PIVOT_VALUE_COA_CODE_MESSAGE + props.value}</i></b></span>;
				},
				Footer: props => {
					return <span><b><i>{PIVOT_VALUE_COA_CODE_TOTAL}</i></b></span>;
				},
			},
			{
				Header: HEADER_COA_NAME,
				accessor: ACCESSOR_COA_NAME,
				aggregate: vals => EMPTY,
			},
			{
				Header: HEADER_PAR_VAT_NO,
				accessor: ACCESSOR_PAR_VAT_NO,
				aggregate: vals => EMPTY,
			},
			{
				Header: HEADER_PAR_NAME,
				accessor: ACCESSOR_PAR_NAME,
				aggregate: vals => EMPTY,
			},
			{
				Header: HEADER_F_PAR_DT_AMOUNT,
				accessor: ACCESSOR_F_PAR_DT_AMOUNT,
				aggregate: vals => lodash.round(lodash.sum(vals),3),
				Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
				Footer: props => {
					let vals = props.data.map((rowData) => (rowData.f_par_dt_amount));
					return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
				},
			},
			{
				Header: HEADER_F_PAR_CT_AMOUNT,
				accessor: ACCESSOR_F_PAR_CT_AMOUNT,
				aggregate: vals => lodash.round(lodash.sum(vals),3),
				Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
				Footer: props => {
					let vals = props.data.map((rowData) => (rowData.f_par_ct_amount));
					return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
				},
			},
			{
				Header: HEADER_O_PAR_DT_AMOUNT,
				accessor: ACCESSOR_O_PAR_DT_AMOUNT,
				aggregate: vals => lodash.round(lodash.sum(vals),3),
				Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
				Footer: props => {
					let vals = props.data.map((rowData) => (rowData.o_par_dt_amount));
					return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
				},
			},
			{
				Header: HEADER_O_PAR_CT_AMOUNT,
				accessor: ACCESSOR_O_PAR_CT_AMOUNT,
				aggregate: vals => lodash.round(lodash.sum(vals),3),
				Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
				Footer: props => {
					let vals = props.data.map((rowData) => (rowData.o_par_ct_amount));
					return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
				},
			},
			{
				Header: HEADER_S_PAR_DT_AMOUNT,
				accessor: ACCESSOR_S_PAR_DT_AMOUNT,
				aggregate: vals => lodash.round(lodash.sum(vals),3),
				Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
				Footer: props => {
					let vals = props.data.map((rowData) => (rowData.s_par_dt_amount));
					return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
				},
			},
			{
				Header: HEADER_S_PAR_CT_AMOUNT,
				accessor: ACCESSOR_S_PAR_CT_AMOUNT,
				aggregate: vals => lodash.round(lodash.sum(vals),3),
				Aggregated: row => {return <span><b><i>{row.value}</i></b></span>;},
				Footer: props => {
					let vals = props.data.map((rowData) => (rowData.s_par_ct_amount));
					return <span><b><i>{lodash.round(lodash.sum(vals),3)}</i></b></span>;
				},
			},
		];
		
		let consolidate = this.state.pConsolidated && this.state.pConsolidated === CONSOLIDATED_N ? CONSOLIDATE_MESSAGE_FIRST : CONSOLIDATE_MESSAGE_SECOND;
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
										<Form.Label>{this.props.t(F_CHART_ACCOUNT_LABEL)}: </Form.Label>
										<FieldSelectContainer
											id={CHART_ACCOUNT}
											componentPath={this.props.componentPath+PARAM_CHART_ACCOUNT} //existing path in redux store where we put data
											listType={F_CHART_ACCOUNTS}
											listAttr={NAME}
											listDisplayFn={((item, constraints) => {
													return (item.code && item.name ? item.code+DASH+item.name : (item.name ? item.name : (item.code ? item.code : U_1F194+item.id)))
												})}
											listLookupColumns={[CODE,NAME]}
											editable={true}
											creatable={false}
										/>
									</Form.Group>
								</Col>
								<Col>
									<Form.Group controlId={REPORTS_DOT_PARAM_C_C_PARTNER}>
										<Form.Label>{this.props.t(C_CC_PARTNER_LABEL)}: </Form.Label>
										<FieldSelectContainer
											id={C_C_PARTNER}
											componentPath={this.props.componentPath+PARAM_CC_PARTNER} //existing path in redux store where we put data
											listType={C_CC_PARTNERS}
											listAttr={NAME}
											editable={true}
											creatable={false}
										/>
									</Form.Group>
								</Col>
							</Form.Row>
							<Form.Row>
								<div className={COL_SM_3}>
									<EmbedRestCallButton  componentPath={this.props.componentPath+DOT_RESULT} onClick={(e) => {
										this.retrieveData(this.state.fromDate, this.state.toDate, this.props.data && this.props.data.paramChartAccount ? this.props.data.paramChartAccount.id : undefined, this.props.data && this.props.data.paramCCPartner ? this.props.data.paramCCPartner.id : undefined, this.state.pYear);
									}}>{this.props.t(REFERENCE_FIRST)}</EmbedRestCallButton>
								</div>
								<div className={COL_SM_3}>
									<EmbedRestCallButton  componentPath={this.props.componentPath+DOT_RESULT} onClick={(e) => {
										this.retrieveDataSec(this.state.fromDate, this.state.toDate, this.props.data && this.props.data.paramChartAccount ? this.props.data.paramChartAccount.id : undefined, this.props.data && this.props.data.paramCCPartner ? this.props.data.paramCCPartner.id : undefined, this.state.pYear);
									}}>{this.props.t(REFERENCE_SECOND)}</EmbedRestCallButton>
								</div>
								<div className={COL_SM_3}>
									<EmbedRestCallButton  componentPath={this.props.componentPath+DOT_RESULT} onClick={(e) => {
										this.retrieveDataNull(this.state.fromDate, this.state.toDate, this.props.data && this.props.data.paramChartAccount ? this.props.data.paramChartAccount.id : undefined, this.props.data && this.props.data.paramCCPartner ? this.props.data.paramCCPartner.id : undefined, this.state.pYear);
									}}>{this.props.t(REFERENCE_THIRD)}</EmbedRestCallButton>
								</div>
							</Form.Row>
						</Form>
					</div>
					<div className={COL_MD_5_MX_AUTO_FONT_WEIGHT_BOLD}>
						{consolidate ? consolidate : EMPTY}
					</div>
					<div className={COL_MD_2_MX_AUTO}>
						{reportData && reportData[0] ? reportData[0].out_name : EMPTY}
					</div>
					<div className={COL_SM_12_ALIGN_CENTER_TABLE}>
						{reportData && reportData[0] ? reportData[0].period : EMPTY}
					</div>
					<div className={COL_SM_12}>
						{reportData && reportData[0] ? reportData[0].param_coa : EMPTY}
					</div>
					<div className={COL_SM_12}>
						{reportData && reportData[0] ? reportData[0].param_par : EMPTY}
					</div>
					<div className={COL_SM_12}>
						<ReactTable
							className={CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPPED_HIGHLIGHT}
							data={reportData}
							columns={columns}
							defaultPageSize={10}
							showPagination={true}
							pivotBy={[PIVOT_BY_COA_CODE]}
						/>
					</div>
				</div>
			</div>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let componentPath = COMPONENT_PATH_REPORTS_PAR_BALANCE_BY_MONTHS;
	let data = resolveObjectPath(componentPath,state.rest); //rest because of fetchREST
	return {
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageReportParBalanceByMonthsContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
