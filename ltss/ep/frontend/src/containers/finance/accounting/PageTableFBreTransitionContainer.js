import React from "react";
import axios from "axios";

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"
import {  Card, Form } from "react-bootstrap";
import { withTranslation } from "react-i18next";

import * as Constants from "../../../static/constants";
import history from "../../../scripts/history"
import { isAuthenticated } from "../../../components/pages/login/Login.js"

import Header from "../../../components/generic/Header"
import EmbedRetrieveEntityListContainer from "../../nomenclatures/EmbedRetrieveEntityListContainer"
import FieldDateContainer from "../../fields/FieldDateContainer"

import { getEntityDefinition, getExpandedColumns } from "../../nomenclatures/entityDefinitions.js"
import { resolveObjectPath } from "../../../scripts/dataUtils";
import { dispatchEditRESTData } from "../../../actions/taskActions";
import { showModal, hideModal } from "../../../actions/modal"
import EmbedRestCallButton from "../../embeds/EmbedRestCallButton";

const LOGIN = "/login"
const OUT_CODE_01 = "01"
const DD_MM_YYYY = "DDMMYYYY"
const DSP = "DSP"
const POST = "post"
const REPORTS_POST_TO_BRIDGE = "/reports/postToBridge/"
const DOT_POST_TO_BRIDGE_DOT = ".postToBridge."
const DOT_POST_TO_BRIDGE = ".postToBridge"
const REPORTS_FILL_FROM_TO_DATES = "Reports.FillFromToDates"
const CONFIRMATION_REQUIRED = "ConfirmationRequired"
const F_BRE_TRANSITION_MODAL_BRIDGE_DATA_PROCESS_UI = "FBreTransition._modalBridgeDataProcessUi"
const YES = "Yes"
const REPORTS_BRIDGE_DATA_PROCESS_UI = "/reports/bridgeDataProcessUi/"
const ZERO = "0"
const DOT_BRIDGE_DATA_PROCESS_UI_DOT = ".bridgeDataProcessUi."
const NO = "No"
const SLASH = "/"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const PAGE_BODY = "page-body"
const COL_SM_3 = "col-sm-3"
const F_BRE_TRANSITION_ACTION_POST_TO_BRIDGE = "FBreTransition._actionPostToBridge"
const FROM_DATE = "fromDate"
const REPORTS_FROM_DATE = "Reports.FromDate"
const DOT_FROM_DATE = ".fromDate"
const TO_DATE = "toDate"
const REPORTS_TO_DATE = "Reports.ToDate"
const DOT_TO_DATE = ".toDate"
const TRANSFERRED_DOCUMENTS_MESSAGE = "Прехвърлени документи: "
const EMPTY = ""
const DOT_PAGE_DATA = ".pageData"
const F_BRE_TRANSITION_ACTION_BRIDGE_DATA_PROCESS_UI = "FBreTransition._actionBridgeDataProcessUi"
const PAGE_TABLE = "PageTable"
const LIST = "list"
const PAGE_BODY_WRAPPER = "page-body-wrapper"

//Page: can be used as a landing page
//Table: presents table of the objects
//Container: redux container class
class PageTableFBreTransitionContainer extends React.Component {

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}

	postToBridge(pFromDate, pToDate){
		let outCode = OUT_CODE_01;
		if(outCode && pFromDate && pToDate) {
			import("moment").then(moment => {  //TODO we get an error, when we replace the literal("moment") with a constant
				let fromDate = moment(pFromDate).format(DD_MM_YYYY);
				let toDate = moment(pToDate).format(DD_MM_YYYY);
				let flags = DSP;
				let promise = axios({
					method: POST,
					url: API_URL+REPORTS_POST_TO_BRIDGE + outCode + SLASH + fromDate + SLASH + toDate + SLASH + flags,
					headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
				}).then(response => {
					let data = response.data;
					console.log(data);
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, postToBridge: data});
				}).catch(error => {
					console.log(error)
					this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, postToBridge: error});
				});
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_POST_TO_BRIDGE_DOT+Constants.PATH_FOR_LOADING, promise);
			});
		} else {
			this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_POST_TO_BRIDGE,new Error(this.props.t(REPORTS_FILL_FROM_TO_DATES)));
		}
	}

	bridgeDataProcessUi(){
		this.props.actions.showModal({
			title: this.props.t(CONFIRMATION_REQUIRED),
			body: this.props.t(F_BRE_TRANSITION_MODAL_BRIDGE_DATA_PROCESS_UI),
			acceptLabel: this.props.t(YES),
			acceptCallback: () => {

				let outCode = OUT_CODE_01;
				if(outCode) {
					import("moment").then(moment => {  //TODO we get an error, when we replace the literal("moment") with a constant
						let promise = axios({
							method: POST,
							url: API_URL+REPORTS_BRIDGE_DATA_PROCESS_UI + outCode + SLASH + ZERO,
							headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
						}).then(response => {
							let data = response.data;
							console.log(data);
							this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, bridgeDataProcessUi: data});
						}).catch(error => {
							console.log(error)
							this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, bridgeDataProcessUi: error});
						});
						this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_BRIDGE_DATA_PROCESS_UI_DOT+Constants.PATH_FOR_LOADING, promise);
					});
				}
		
				this.props.actions.hideModal()
			},
			refuseLabel: this.props.t(NO),
			refuseCallback: () => {
				this.props.actions.hideModal()
			},
		})
	}

	render() {
		let columns = getExpandedColumns(this.props.entityName, this.props.expand, this.props.columnOverride);
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={this.props.title} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<div className={PAGE_BODY}>
					<Card><Card.Body>
						<Form><Form.Row>
							<div className={COL_SM_3}>
								<EmbedRestCallButton componentPath={this.props.componentPath+DOT_POST_TO_BRIDGE} onClick={(e) => {this.postToBridge(this.props.data.fromDate, this.props.data.toDate);}}>{this.props.t(F_BRE_TRANSITION_ACTION_POST_TO_BRIDGE)}</EmbedRestCallButton>
							</div>
							<div className={COL_SM_3}>
								<label htmlFor={FROM_DATE}>{this.props.t(REPORTS_FROM_DATE)}</label>
								<FieldDateContainer
									id={FROM_DATE}
									componentPath={this.props.componentPath+DOT_FROM_DATE} //existing path in redux store where we put data
									editable={true}
									loading={this.props.loading}
								/>
							</div>
							<div className={COL_SM_3}>
								<label htmlFor={TO_DATE}>{this.props.t(REPORTS_TO_DATE)}</label>
								<FieldDateContainer
									id={TO_DATE}
									componentPath={this.props.componentPath+DOT_TO_DATE} //existing path in redux store where we put data
									editable={true}
									loading={this.props.loading}
								/>
							</div>
						</Form.Row></Form>
						<label>{ this.props.data && this.props.data.postToBridge ?
							(this.props.data.postToBridge.cnt !== undefined ? TRANSFERRED_DOCUMENTS_MESSAGE+this.props.data.postToBridge.cnt : JSON.stringify(this.props.data.postToBridge))
							: EMPTY}
						</label>
					</Card.Body></Card>
					<EmbedRetrieveEntityListContainer
						key={this.props.componentPath} //help React disambiguate when changing to another table page with this component but with different path
						retrieveType = {this.props.entityName}
						componentPath = {this.props.componentPath} //existing path in redux store where we put data
						columns = {columns}
						title={this.props.title}
						icon={this.props.icon}
						expanded={true}
						asTable={true}
						editable={false}
						hasRowSelecting={this.props.hasRowSelecting}
						selectedRowsColumns={this.props.selectedRowsColumns}
						defaultFilter={this.props.defaultFilter}
					>
						<Form><Form.Row>
							<div className={COL_SM_3}>
								<EmbedRestCallButton componentPath={this.props.componentPath+DOT_PAGE_DATA} onClick={(e) => {this.bridgeDataProcessUi();}}>{this.props.t(F_BRE_TRANSITION_ACTION_BRIDGE_DATA_PROCESS_UI)}</EmbedRestCallButton>
							</div>
						</Form.Row></Form>
					</EmbedRetrieveEntityListContainer>
				</div>
			</div>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	const entityDef = getEntityDefinition(ownProps.entityName,ownProps.columnOverride,ownProps.entityOverride);
	const title = ownProps.title ? ownProps.title : entityDef.label_plural;
	let componentPath = ownProps.componentPath ? ownProps.componentPath : PAGE_TABLE+entityDef.className;
	let data = resolveObjectPath(componentPath,state.rest);
	return {
		auth: state.auth,
		componentPath: componentPath,
		data: data,
		entityName: ownProps.entityName,
		entityDef: entityDef,
		entityOverride: ownProps.entityOverride,
		expand: ownProps.expand ? ownProps.expand : [],
		title: title,
		icon: entityDef.icon ? entityDef.icon : LIST,
		hasRowSelecting: ownProps.hasRowSelecting,
		selectedRowsColumns: ownProps.selectedRowsColumns,
		columnOverride: ownProps.columnOverride,
		defaultFilter: ownProps.defaultFilter
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { showModal, hideModal, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableFBreTransitionContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);