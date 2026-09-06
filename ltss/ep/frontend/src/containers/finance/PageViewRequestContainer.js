import React from "react";
import { ButtonGroup } from "react-bootstrap";
import { dispatchEditRESTData, resetRESTCallLimit } from "./../../actions/taskActions";
import axios from "axios";

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
import { resolveObjectPath } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"
import i18n from "i18next";
import EmbedRestCallButton from "../embeds/EmbedRestCallButton";

const LOGIN = "/login"
const POST = "post"
const SLASH_REPORTS_SLASH_MAKE_REQUEST_SLASH = "/reports/makeRequest/"
const SLASH_ADMIN1_SLASH_TEST_DATA= "/admin1/testdata"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const DOT_MAKE_REQUEST = ".makeRequest"
const EMPTY = ""
const SPINNER = "spinner"
const MAKE_REQUEST = "makeRequest"
const FINANCE_TRANSLATION_DOT_CHG_STATUS = "FinanceTranslation.chgStatus"
const SIZE_2X = "2x"
const C_OFFER_DETAILS = "cOfferDetails"
const C_RESERVE_QUANTITIES = "cReserveQuantities"
const C_SALES = "cSales"
const PAGE_BODY = "page-body"
const DOT_CLASS_NAME_GOODS = "._className_goods"
const DOT_OFFER_DETAILS_DOT_GOODS = ".offerDetails.goods"
const OFR_ID = "ofrId"
const IS_NOT_NULL = "isNotNull"
const GOD_ID = "godId"
const DOT_CLASS_NAME_PLURAL = "._className_plural"
const DOT_C_RESERVE_QUANTITIES = ".cReserveQuantities"
const DOT_C_SALE = ".cSale"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const SLASH_REQUESTS = "/requests"
const SLASH = "/"
const SLASH_ADD = "/add"
const C_OFFERS = "cOffers"
const C_REQUESTS_VIEW = "cRequestsView"
const C_OFFER_DOT_CLASS_NAME_WAREHOUSE = "COffer._className_warehouse"
const FINANCE_TRANSLATION_DOT_WAREHOUSE = "FinanceTranslation.warehouse"
const DOT = "."

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewRequestContainer extends React.Component {
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}

	makeRequest(){
		//console.log("Status: " + this.props.data.status)
		if(this.props.data.status && this.props.data.status.listOptionItemCode == 2){
			axios({
				method: POST,
				url: API_URL+SLASH_REPORTS_SLASH_MAKE_REQUEST_SLASH + this.props.data.id + SLASH_ADMIN1_SLASH_TEST_DATA,
				headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
			}).then(response => {
				//console.log(response.data)
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_MAKE_REQUEST,response.data);
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className,undefined);
			}).catch(error => {
				console.error(error)
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_MAKE_REQUEST,error);
			});
		}
	}
	
	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const offerDetailsGoodsDef = getEntityDefinition(C_OFFER_DETAILS, {ofrId: {show: false}, seeId: {show: false}, price2: {show: false}, price3: {show: false}, price4: {show: false}, price5: {show: false}});
				const reserveQuantitiesDef = getEntityDefinition(C_RESERVE_QUANTITIES, {ofrId: {show: false}});
				const saleDef = getEntityDefinition(C_SALES, {ofrId: {show: false}});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={i18n.t(offerDetailsGoodsDef.className+DOT_CLASS_NAME_GOODS)} //"Offer Details / Goods"//{offerDetailsDef.label}
							icon={offerDetailsGoodsDef.icon}
							columns={offerDetailsGoodsDef.columns}
							componentPath={this.props.componentPath+DOT_OFFER_DETAILS_DOT_GOODS}
							retrieveType={C_OFFER_DETAILS}
							parentHref={this.props.href}
							parentAttr={OFR_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
							defaultFilter = {[
								{where: {
									op: IS_NOT_NULL,
									operands: [GOD_ID],
								},}
							]}
							onCommitChange={(data) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className,undefined);}}
						/>
						<EmbedRetrieveEntityListContainer
							title= {i18n.t(reserveQuantitiesDef.className+DOT_CLASS_NAME_PLURAL)}
							icon={reserveQuantitiesDef.icon}
							columns={reserveQuantitiesDef.columns}
							componentPath={this.props.componentPath+DOT_C_RESERVE_QUANTITIES}
							retrieveType={C_RESERVE_QUANTITIES}
							parentHref={this.props.href}
							parentAttr={OFR_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
						/>
						<EmbedRetrieveEntityListContainer
							title= {i18n.t(saleDef.className+DOT_CLASS_NAME_PLURAL)}
							icon={saleDef.icon}
							columns={saleDef.columns}
							componentPath={this.props.componentPath+DOT_C_SALE}
							retrieveType={C_SALES}
							parentHref={this.props.href}
							parentAttr={OFR_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
						/>
						
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+DOT_CHANGE_LOG} retrieveType={this.props.entityName}/>
					</div>;
			}
		}
		let button = EMPTY;
		if(this.props.data && this.props.data.id && this.props.data.status && this.props.data.status.listOptionItemCode == 2) {
			button = <ButtonGroup>
						<EmbedRestCallButton componentPath={this.props.componentPath+DOT+MAKE_REQUEST} onClick={(e)=> {this.makeRequest();}}>
							{this.props.t(FINANCE_TRANSLATION_DOT_CHG_STATUS)}
						</EmbedRestCallButton>
					</ButtonGroup>
		} 
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={this.props.headerText} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<EmbedEntityContainer
					componentPath={this.props.componentPath+DOT+this.props.entityDef.className}
					retrieve_id={this.props.match.params.entity_id}
					onBeforeChange={(data) => {
						//if(!this.props.entityDef.pageURL) {console.error("pageURL is missing for entity "+this.props.entityDef.className);}
						if(data.id) {
							//history.push(this.props.entityDef.pageURL+"/"+data.id);
							history.push(SLASH_REQUESTS+SLASH+data.id);
						} else {
							//history.push(this.props.entityDef.pageURL+"/add");
							history.push(SLASH_REQUESTS+SLASH_ADD);
						}}}
					entityName={C_OFFERS}
					headerText={this.props.t(C_OFFER_DOT_CLASS_NAME_WAREHOUSE)}
					creatable={false}
					expanded={true}
					columnOverride={{partner: {show: false}, outId:{Header: this.props.t(FINANCE_TRANSLATION_DOT_WAREHOUSE)}, advanceTotal: {show: false}, proformNumber: {show: false}, proformDate: {show: false}}}
				>
					{button}
				</EmbedEntityContainer>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = C_OFFERS;
	const entityDef = getEntityDefinition(entityName, {partner: {show: false},});
	const componentPath = C_REQUESTS_VIEW;
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
		headerText: ownProps.t(C_OFFER_DOT_CLASS_NAME_WAREHOUSE),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {dispatchEditRESTData, resetRESTCallLimit}), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewRequestContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);


// КОД НА СТОКА ДА СЕ ПОКАЗВА В ТАБЛИЦАТА.