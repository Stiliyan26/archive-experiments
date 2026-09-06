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
import EmbedRestCallButton from "../../embeds/EmbedRestCallButton";

const CONSOLIDATED_N = "N"
const CONSOLIDATED_Y = "Y"
const BTE_SIDE_Y = "S"
const REPORT_UNPAID_DOCS_BY_PAR = "/reports/unpaidDocsByPar/"
const SLASH = "/"
const GET = "get"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const DOT_RESULT_DOT = ".result."
const DOT_RESULT = ".result"
const ERROR_MESSAGE = "Reports.ChooseAllFields"
const HEADER_PAR_CODE_FIRST = "Код на клиента"
const HEADER_PAR_CODE_SECOND = "Код на доставчика"
const ACCESSOR_PAR_CODE = "par_code"
const ROW_MESSAGE_FIRST = "НЕПОГАСЕНИ ДОКУМЕНТИ"
const ROW_MESSAGE_SECOND= "КЪМ ДАТА: "
const ROW_MESSAGE_THIRD = "За Фирма:"
const HEADER_PAR_NAME = "Контрагент"
const ACCESSOR_PAR_NAME = "par_name"
const HEADER_DESCR = "Документ №/Дата"
const ACCESSOR_DESCR = "descr"
const HEADER_AMOUNT = "Обща сума на документа"
const ACCESSOR_AMOUNT = "amount"
const HEADER_DUE_DATE = "Дата  за погасяване"
const ACCESSOR_DUE_DATE = "due_date"
const HEADER_TOTAL = "Обща сума за погасяване"
const ACCESSOR_TOTAL = "total"
const HEADER_OF_COLUMNS = "Просрочени суми"
const HEADER_LESS30 = "до 30 дни"
const ACCESSOR_LESS30 = "less30"
const HEADER_LESS60 = "над 30 дни"
const ACCESSOR_LESS60 = "less60"
const HEADER_LESS120 = "над 60 дни"
const ACCESSOR_LESS120 = "less120"
const HEADER_MORE120 = "над 120 дни"
const ACCESSOR_MORE120 = "more120"
const CONSOLIDATE_MESSAGE_FIRST = "КОНСОЛИДИРАНА СПРАВКА ЗА  "
const CONSOLIDATE_MESSAGE_SECOND = "СПРАВКА ЗА "
const HEADER_TEXT = "Просрочени вземания и задължения"
const REPORTS_WRAPPER = "reports-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const PAGE_BODY = "page-body"
const COL_SM_12_FORM_GROUP = "col-sm-12 form-group"
const REPORTS_DOT_PARAM_BATCH_JTE_DEFAULT_SIDE = "reports.paramBatchJteDefaultSide"
const CHART_ACCOUNT = "ChartAccount"
const BATCH_JTE_DEFAULT_SIDE_LABEL = "Доставки/Продажби"
const PARAM_BATCH_JTE_DEFAULT_SIDE = ".paramBatchJteDefaultSide"
const LOI_BATCH_JTE_DEFAULT_SIDES = "loiBatchJteDefaultSides"
const LIST_OPTION_ITEM_NAME = "listOptionItemName"
const REPORTS_DOT_PARAM_CC_ORGANIZATION_UNIT = "reports.paramCcOrganizationUnit"
const CC_ORGANIZATION_UNIT = "CcOrganizationUnit"
const C_CC_ORGANIZATION_UNIT_LABEL = "Орг. Единица"
const PARAM_C_CC_ORGANIZATION_UNIT = ".paramCcOrganizationUnit"
const LIST_TYPE_C_CC_ORGANIZATION_UNIT = "cCcOrganizationUnits"
const NAME = "name"
const COL_SM_3 = "col-sm-3"
const COL_MD_2_MX_AUTO_FONT_WEIGHT_BOLD = "col-md-2 mx-auto font-weight-bold"
const COL_MD_3_MX_AUTO_FONT_WEIGHT_BOLD = "col-md-3 mx-auto font-weight-bold"
const COL_SM_12_ALIGN_CENTER_TABLE_FONT_WEIGHT_BOLD = "col-sm-12 align-center-table font-weight-bold"
const COL_SM_12_FONT_WEIGHT_BOLD = "col-sm-12 font-weight-bold"
const COL_SM_12 = "col-sm-12"
const CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPED_HIGHLIGHT = "clients-table align-center-table -striped -highlight"
const REFERENCE = "Справка"
const EMPTY = ""
const REPORTS_UNPAID_DOCS_BY_PAR = "reports.unpaidDocsByPar"

//Page: can be used as a landing page
//Container: redux container class
class PageReportUnpaidDocsByParContainer extends React.Component {
	
	constructor(props){
		super(props);
		this.state = {
			pConsolidated: CONSOLIDATED_N,
			pSBteSide: BTE_SIDE_Y
		}
	}
	
	retrieveData(pOutCode, pBteSide) {
		
		let pSConsolidated = CONSOLIDATED_N;
		if(pOutCode && pBteSide) {
			import("moment").then(moment => {  //TODO we get an error, when we replace the literal("moment") with a constant
				let promise = axios({
					method: GET,
					url: API_URL+REPORT_UNPAID_DOCS_BY_PAR + pOutCode + SLASH + pBteSide + SLASH + pSConsolidated,
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
		
		let referenceData = this.state.pConsolidated && this.state.pConsolidated === CONSOLIDATED_Y ? CONSOLIDATE_MESSAGE_FIRST: CONSOLIDATE_MESSAGE_SECOND;
		let reportData = this.props.data && this.props.data.result instanceof Array ? this.props.data.result : undefined;
		return (
			<div className={REPORTS_WRAPPER}>
				<Header text={this.props.t(HEADER_TEXT)} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<div className={PAGE_BODY}>
					<div className={COL_SM_12_FORM_GROUP}>
						<Form>
							<Form.Row>
								<Col>
									<Form.Group controlId={REPORTS_DOT_PARAM_BATCH_JTE_DEFAULT_SIDE}>
										<Form.Label>{this.props.t(BATCH_JTE_DEFAULT_SIDE_LABEL)}: </Form.Label>
										<FieldSelectContainer
											id={CHART_ACCOUNT}
											componentPath={this.props.componentPath+PARAM_BATCH_JTE_DEFAULT_SIDE} //existing path in redux store where we put data
											listType={LOI_BATCH_JTE_DEFAULT_SIDES}
											listAttr={LIST_OPTION_ITEM_NAME}
											editable={true}
											creatable={false}
										/>
									</Form.Group>
								</Col>
								<Col>
									<Form.Group controlId={REPORTS_DOT_PARAM_CC_ORGANIZATION_UNIT}>
										<Form.Label>{this.props.t(C_CC_ORGANIZATION_UNIT_LABEL)}: </Form.Label>
										<FieldSelectContainer
											id={CC_ORGANIZATION_UNIT}
											componentPath={this.props.componentPath+PARAM_C_CC_ORGANIZATION_UNIT} //existing path in redux store where we put data
											listType={LIST_TYPE_C_CC_ORGANIZATION_UNIT}
											listAttr={NAME}
											editable={true}
											creatable={false}
										/>
									</Form.Group>
								</Col>
							</Form.Row>
							<Form.Row>
								<div className={COL_SM_3}>
									<EmbedRestCallButton componentPath={this.props.componentPath+DOT_RESULT} onClick={(e) => {
										this.retrieveData(this.props.data && this.props.data.paramCcOrganizationUnit ? this.props.data.paramCcOrganizationUnit.id : undefined, this.props.data && this.props.data.paramBatchJteDefaultSide ? this.props.data.paramBatchJteDefaultSide.id : undefined);
									}}>{this.props.t(REFERENCE)}</EmbedRestCallButton>
								</div>
							</Form.Row>
						</Form>
					</div>
					<div className={COL_MD_2_MX_AUTO_FONT_WEIGHT_BOLD}>
						{reportData && reportData[0] ? referenceData : EMPTY}
					</div>
					<div className={COL_MD_3_MX_AUTO_FONT_WEIGHT_BOLD}>
						{reportData && reportData[0] ? ROW_MESSAGE_FIRST : EMPTY}
					</div>
					<div className={COL_SM_12_ALIGN_CENTER_TABLE_FONT_WEIGHT_BOLD}>
						{reportData && reportData[0] ? ROW_MESSAGE_SECOND + reportData[0].cur_date : EMPTY}
					</div>
					<div className={COL_SM_12_FONT_WEIGHT_BOLD}>
						{reportData && reportData[0] ? ROW_MESSAGE_THIRD + reportData[0].par_code : EMPTY}
					</div>
					<div className={COL_SM_12}>
						<ReactTable
							className={CLIENTS_TABLE_ALIGN_CENTER_TABLE_STRIPED_HIGHLIGHT}
							data={reportData}
							columns={[
							    {
							    	Header: this.state.pSBteSide && this.state.pSBteSide === BTE_SIDE_Y ? HEADER_PAR_CODE_FIRST : HEADER_PAR_CODE_SECOND,
							    	accessor: ACCESSOR_PAR_CODE,
							    },
							    {
							    	Header: HEADER_PAR_NAME,
							    	accessor: ACCESSOR_PAR_NAME,
							    },
							    {
							    	Header: HEADER_DESCR,
							    	accessor: ACCESSOR_DESCR,
							    },
							    {
							    	Header: HEADER_AMOUNT,
							    	accessor: ACCESSOR_AMOUNT,
							    	aggregate: vals => lodash.round(lodash.sum(vals),3),
							    },
							    {
							    	Header: HEADER_DUE_DATE,
							    	accessor: ACCESSOR_DUE_DATE,
							    	aggregate: vals => lodash.round(lodash.sum(vals),3),
							    	height: 100
							    },
							    {
							    	Header: HEADER_TOTAL,
							    	accessor: ACCESSOR_TOTAL,
							    	aggregate: vals => lodash.round(lodash.sum(vals),3),
							    	height: 100
							    },
							    {
							    	Header: HEADER_OF_COLUMNS,
							    	columns: [
							        {
							        	Header: HEADER_LESS30,
							        	accessor: ACCESSOR_LESS30,
							        	aggregate: vals => lodash.round(lodash.sum(vals),3),
							        },
							        {
							        	Header: HEADER_LESS60,
							        	accessor: ACCESSOR_LESS60,
							        	aggregate: vals => lodash.round(lodash.sum(vals),3),
							        },
							        {
							        	Header: HEADER_LESS120,
							        	accessor: ACCESSOR_LESS120,
							        	aggregate: vals => lodash.round(lodash.sum(vals),3),
							        },
							        {
							        	Header: HEADER_MORE120,
							        	accessor: ACCESSOR_MORE120,
							        	aggregate: vals => lodash.round(lodash.sum(vals),3),
							        }]
							    }
							]}
							defaultPageSize={10}
							showPagination={true}
						/>
					</div>
				</div>
			</div>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let componentPath = REPORTS_UNPAID_DOCS_BY_PAR;
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageReportUnpaidDocsByParContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
