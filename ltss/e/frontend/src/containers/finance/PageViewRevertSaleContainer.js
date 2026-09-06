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

import { getEntityDefinition } from "../nomenclatures/entityDefinitions.js"
import { resetRESTCallLimit, dispatchEditRESTData } from "../../actions/taskActions";
import { resolveObjectPath } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"

const LOGIN = "/login"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const C_SALE_DETAILS = "cSaleDetails"
const TITLE_C_SALE_DETAILS = "Стоки"
const DOT_C_SALE_DETAILS_GOODS = ".cSaleDetailsGoods"
const SAE_ID = "saeId"
const C_SALES = "cSales"
const C_SALE_DOT_CLASS_NAME_KI = "CSale._className_KI"
const SLASH_REVERT_SALES = "/revertSales"
const REVERT_SALE_VIEW = "RevertSaleView"
const PAGE_BODY = "page-body"
const IS_NOT_NULL = "isNotNull"
const STK_ID = "stkId"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const SLASH = "/"
const SLASH_ADD = "/add"
const DOT = "."

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewRevertSaleContainer extends React.Component {	
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
				const cSaleDetailsGoodsDef = getEntityDefinition(C_SALE_DETAILS, {
						seeId: {show: false},
						currency: {show: false},
						priceVat: {show: false},
						rateExchange: {show: false},
						sdlId: {show: false},
						saeId: {show: false},
						odlId: {show: false},
						cost: {show: false},
						valDisc: {isReadOnly: true},
						totalWhtVat: {isReadOnly: true},
						totalVat: {isReadOnly: true},
						total: {isReadOnly: true},
						stkId: {isReadOnly: true},
						batch: {isReadOnly: true},
						serialNumber: {isReadOnly: true},
						meeId: {isReadOnly: true},
						price: {isReadOnly: true},
						discount: {isReadOnly: true},
						vat: {isReadOnly: true},
					});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={TITLE_C_SALE_DETAILS}
							icon={cSaleDetailsGoodsDef.icon}
							columns={cSaleDetailsGoodsDef.columns}
							componentPath={this.props.componentPath+DOT_C_SALE_DETAILS_GOODS}
							retrieveType={C_SALE_DETAILS}
							parentHref={this.props.href}
							parentAttr={SAE_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
							defaultFilter = {[
								{where: {
									op: IS_NOT_NULL,
									operands: [STK_ID],
								},}
							]}
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
					entityName={C_SALES}
					headerText={this.props.t(C_SALE_DOT_CLASS_NAME_KI)}
					creatable={false}
					expanded={true}
					entityOverride={{pageURL: SLASH_REVERT_SALES}}
					columnOverride={{
						documentNumber: {isReadOnly: true},
						parId: {isReadOnly: true},
						discount: {isReadOnly: true},
						vat: {isReadOnly: true},
						sum: {isReadOnly: true},
						total: {isReadOnly: true},
						oblSum: {isReadOnly: true},
						endSum: {isReadOnly: true},
						danOsnova: {isReadOnly: true},
						currency: {isReadOnly: true},
						exchangeRate: {isReadOnly: true},
						typeDoc: {isReadOnly: true},
						vatSum: {isReadOnly: true},
						outCode: {isReadOnly: true},
						saeId: {show: true, isReadOnly: true}, 
						placeDeals: {show: false}, 
						status: {show: false}, 
						tdtId: {show: false}, 
						vatto: {show: false}, 
						outId: {show: false}, 
						oldTypeDoc: {show: false}, 
						ofrId: {show: false}, 
						posted: {show: false}, 
						advanceUsed: {show: false}, 
						cost: {show: false}, 
						totalPayed: {show: false}, 
						payed: {show: false},
					}}
				>
				</EmbedEntityContainer>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = C_SALES;
	let entityDef = getEntityDefinition(entityName,undefined);
	entityDef.pageURL = SLASH_REVERT_SALES;
	const componentPath = REVERT_SALE_VIEW;
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
		viewData: viewData,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t(C_SALE_DOT_CLASS_NAME_KI),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { resetRESTCallLimit, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewRevertSaleContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
