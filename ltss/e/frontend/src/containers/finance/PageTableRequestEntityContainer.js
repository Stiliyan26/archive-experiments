import React from "react";

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"
import { withTranslation } from "react-i18next";
import i18n from "i18next";

import history from "../../scripts/history"
import { isAuthenticated } from "../../components/pages/login/Login.js"

import Header from "../../components/generic/Header"
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"

import { getEntityDefinition, getExpandedColumns } from "../nomenclatures/entityDefinitions.js"


const LOGIN = "/login"
const REQUEST = "/requests"
const FINANCE_TRANSLATION_DOT_WAREHOUSE = "FinanceTranslation.warehouse"
const IS_NOT_NULL = "isNotNull"
const OUT_ID = "outId"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_BODY = "page-body"
const LIST = "list"
const PAGE_TABLE = "PageTable"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"

//Page: can be used as a landing page
//Table: presents table of the objects
//Container: redux container class
class PageTableRequestEntityContainer extends React.Component {

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}

	render() {
		let columns = getExpandedColumns(this.props.entityName, this.props.expand, {numberOfr:{pageURL: REQUEST}, partner:{show:false}, outId:{Header:i18n.t(FINANCE_TRANSLATION_DOT_WAREHOUSE)}});
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={this.props.title} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<div className={PAGE_BODY}>
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
						defaultFilter={[{where: {
							op: IS_NOT_NULL,
							operands: [OUT_ID],
						},}]}
					/>
				</div>
			</div>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	const entityDef = getEntityDefinition(ownProps.entityName, undefined);
	const componentPath = PAGE_TABLE+entityDef.className;
	const title = ownProps.title ? ownProps.title : entityDef.label_plural;
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: ownProps.entityName,
		entityDef: entityDef,
		expand: ownProps.expand ? ownProps.expand : [],
		title: title,
		icon: entityDef.icon ? entityDef.icon : LIST,
		hasRowSelecting: ownProps.hasRowSelecting,
		selectedRowsColumns: ownProps.selectedRowsColumns,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableRequestEntityContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);