import React from "react";

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
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"

import { getEntityDefinition } from "../nomenclatures/entityDefinitions.js"
import { resolveObjectPath } from "../../scripts/dataUtils";

const LOGIN = "/login"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const C_RESERVE_QUANTITIES = "cReserveQuantities"
const C_BLOCKED_QUANTITIES = "cBlockedQuantities"
const C_DELIVERY_DETAILS = "cDeliveryDetails"
const C_SALE_DETAILS = "cSaleDetails"
const DOT_C_RESERVE_QUANTITIES = ".cReserveQuantities"
const DOT_C_BLOCKED_QUANTITIES = ".cBlockedQuantities"
const DOT_C_DELIVERY_DETAILS = ".cDeliveryDetails"
const DOT_C_SALE_DETAILS = ".cSaleDetails"
const STK_ID = "stkId"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_BODY = "page-body"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const SLASH = "/"
const SLASH_ADD = "/add"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const DOT = "."
const EQUAL = "equal"
const ID = "id"
const C_STOCKS = "cStocks"
const C_STOCK_DOT_CLASS_NAME = "CStock._className"
const C_STOCKS_VIEW = "cStocksView"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewCStockContainer extends React.Component {	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const cReserveQuantitiesDef = getEntityDefinition(C_RESERVE_QUANTITIES,{stkId: {show: false}});
				const cBlockedQuantitiesDef = getEntityDefinition(C_BLOCKED_QUANTITIES,{stkId: {show: false}});
				const cDeliveryDetailsDef = getEntityDefinition(C_DELIVERY_DETAILS,{stkId: {show: false}});
				const cSaleDetailsDef = getEntityDefinition(C_SALE_DETAILS,{stkId: {show: false}});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={cReserveQuantitiesDef.label}
							icon={cReserveQuantitiesDef.icon}
							columns={cReserveQuantitiesDef.columns}
							componentPath={this.props.componentPath+DOT_C_RESERVE_QUANTITIES}
							retrieveType={C_RESERVE_QUANTITIES}
							parentHref={this.props.href}
							parentAttr={STK_ID}
							parentData={this.props.data}
							asTable = {true}
							creatable={false}
							editable={false}
						/>
						<EmbedRetrieveEntityListContainer
							title={cBlockedQuantitiesDef.label}
							icon={cBlockedQuantitiesDef.icon}
							columns={cBlockedQuantitiesDef.columns}
							componentPath={this.props.componentPath+DOT_C_BLOCKED_QUANTITIES}
							retrieveType={C_BLOCKED_QUANTITIES}
							parentHref={this.props.href}
							parentAttr={STK_ID}
							parentData={this.props.data}
							asTable = {true}
							creatable={false}
							editable={false}
						/>
						<EmbedRetrieveEntityListContainer
							title={cDeliveryDetailsDef.label}
							icon={cDeliveryDetailsDef.icon}
							columns={cDeliveryDetailsDef.columns}
							componentPath={this.props.componentPath+DOT_C_DELIVERY_DETAILS}
							retrieveType={C_DELIVERY_DETAILS}
							asTable = {true}
							creatable={false}
							editable={false}
							defaultFilter = {[
								{where: {
									op: EQUAL,
									operands: [ID,{literal: this.props.data.ddlId.id}],
								},}
							]}
						/>
						<EmbedRetrieveEntityListContainer
							title={cSaleDetailsDef.label}
							icon={cSaleDetailsDef.icon}
							columns={cSaleDetailsDef.columns}
							componentPath={this.props.componentPath+DOT_C_SALE_DETAILS}
							retrieveType={C_SALE_DETAILS}
							parentHref={this.props.href}
							parentAttr={STK_ID}
							parentData={this.props.data}
							asTable = {true}
							creatable={false}
							editable={false}
						/>
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+DOT_CHANGE_LOG} retrieveType={this.props.entityName}/>
					</div>;
			}
		}
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
					entityName={C_STOCKS}
					headerText={this.props.t(C_STOCK_DOT_CLASS_NAME)}
					creatable={false}
					expanded={true}
				/>;
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = C_STOCKS;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = C_STOCKS_VIEW;
	let viewData = resolveObjectPath(componentPath,state.rest);
	let data = undefined;
	if(viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded 
			&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
		data = viewData[entityDef.className]._embedded[entityName][0];
	}
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t(C_STOCK_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewCStockContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
