import React from "react";
import { Form } from "react-bootstrap";
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
import EmbedRestCallButton from "../embeds/EmbedRestCallButton"

import { getEntityDefinition } from "../nomenclatures/entityDefinitions.js"
import { resolveObjectPath } from "../../scripts/dataUtils";
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"
import i18n from "i18next";

const LOGIN = "/login"
const POST = "post"
const SLASH_REPORTS_SLASH_MAKE_REQUEST_SLASH = "/reports/makeRequest/"
const SLASH_ADMIN1_SLASH_TESTDATA = "/admin1/testdata"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const DOT_MAKE_REQUEST = ".makeRequest"
const SLASH_REPORTS_SLASH_MAKE_SALE_SLASH = "/reports/makeSale/"
const SLASH_ADMIN1_SLASH_REMARK= "/admin1/remark"
const DOT_MAKE_SALE = ".makeSale"
const EMPTY = ""
const SPINNER = "spinner"
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
const DOT_CLASS_NAME_SERVICE = "._className_service"
const DOT_OFFER_DETAILS_DOT_SERVICE = ".offerDetails.service"
const SEE_ID = "seeId"
const DOT_CLASS_NAME_PLURAL = "._className_plural"
const DOT_C_RESERVE_QUANTITIES = ".cReserveQuantities"
const DOT_C_SALE = ".cSale"
const DOT_CHANGE_LOG = ".changelog"
const M_2 = "m-2"
const MAKE_REQUEST = "makeRequest"
const FINANCE_TRANSLATION_DOT_CHG_STATUS = "FinanceTranslation.chgStatus"
const MAKE_SALE = "makeSale"
const C_OFFER_DOT_CREATE_SALE = "COffer._createSale"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const SLASH = "/"
const SLASH_ADD = "/add"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const DOT = "."
const NOT_EQUAL = "notEqual"
const PARTNER_TYPE_DOT_LIST_OPTION_ITEM_CODE = "partnerType.listOptionItemCode"
const C_OFFERS = "cOffers"
const C_OFFER_DOT_CLASS_NAME = "COffer._className"
const OFFER_VIEW = "offerView"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewOfferContainer extends React.Component {	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	makeRequest(){
		if(this.props.data.status && this.props.data.status.listOptionItemCode == 2){
			axios({
				method: POST,
				url: API_URL+SLASH_REPORTS_SLASH_MAKE_REQUEST_SLASH + this.props.data.id + SLASH_ADMIN1_SLASH_TESTDATA,
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
	
	makeSale(){
		if(this.props.data.status && (this.props.data.status.listOptionItemCode == 1 || this.props.data.status.listOptionItemCode == 3)){
			axios({
				method: POST,
				url: API_URL+SLASH_REPORTS_SLASH_MAKE_SALE_SLASH + this.props.data.id + SLASH_ADMIN1_SLASH_REMARK,
				headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
			}).then(response => {
				//console.log(response.data)
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_MAKE_SALE,response.data);
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className,undefined);
			}).catch(error => {
				console.error(error)
				this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT_MAKE_SALE,error);
			});
		}
	}

	render() {
		let body = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const offerDetailsGoodsDef = getEntityDefinition(C_OFFER_DETAILS, {ofrId: {show: false}, seeId: {show: false}, price2: {show: false}, price3: {show: false}, price4: {show: false}, price5: {show: false}, sdlQuantity: {isReadOnly: true}});
				const offerDetailsServiceDef = getEntityDefinition(C_OFFER_DETAILS, {ofrId: {show: false}, godId: {show: false}, price2: {show: false}, price3: {show: false}, price4: {show: false}, price5: {show: false}, sdlQuantity: {show: false}});
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
							asTable={true}
							expanded={true}
							defaultFilter={[
								{where: {
									op: IS_NOT_NULL,
									operands: [GOD_ID],
								},}
							]}
							onCommitChange={(data) => {this.props.actions.dispatchEditRESTData(this.props.componentPath+DOT+this.props.entityDef.className,undefined);}}
						/>
						<EmbedRetrieveEntityListContainer
							title= {i18n.t(offerDetailsServiceDef.className+DOT_CLASS_NAME_SERVICE)}//"Offer Details / Service"//{offerDetailsDef.label}
							icon={offerDetailsServiceDef.icon}
							columns={offerDetailsServiceDef.columns}
							componentPath={this.props.componentPath+DOT_OFFER_DETAILS_DOT_SERVICE}
							retrieveType={C_OFFER_DETAILS}
							parentHref={this.props.href}
							parentAttr={OFR_ID}
							parentData={this.props.data}
							asTable = {true}
							expanded={true}
							defaultFilter = {[
								{where: {
									op: IS_NOT_NULL,
									operands: [SEE_ID],
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
		if(this.props.data && this.props.data.id) {
			button = <Form><Form.Row>
						<Form.Group className={M_2}>
								{this.props.data.status && this.props.data.status.listOptionItemCode == 2 ?
								<EmbedRestCallButton componentPath={this.props.componentPath+DOT+MAKE_REQUEST} onClick={(e) => {this.makeRequest();}}>{this.props.t(FINANCE_TRANSLATION_DOT_CHG_STATUS)}</EmbedRestCallButton>
								: EMPTY}
								{this.props.data.status && (this.props.data.status.listOptionItemCode == 1/*RA*/ || this.props.data.status.listOptionItemCode == 3/*PI*/) ?
								<EmbedRestCallButton componentPath={this.props.componentPath+DOT+MAKE_SALE} onClick={(e) => {this.makeSale();}}>{this.props.t(C_OFFER_DOT_CREATE_SALE)}</EmbedRestCallButton>
								: EMPTY}
						</Form.Group>
					</Form.Row></Form>
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
					entityName={C_OFFERS}
					headerText={this.props.t(C_OFFER_DOT_CLASS_NAME)}
					creatable={false}
					expanded={true}
					columnOverride={{proformNumber: {show: false}, proformDate: {show: false}, outId: {show: false}, advanceTotal: {show: false}, 
						partner: {optionFilter:[{where: {
							op: NOT_EQUAL,
							operands: [PARTNER_TYPE_DOT_LIST_OPTION_ITEM_CODE, {literal: 2}],
						}}]},
					}}
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
	const entityDef = getEntityDefinition(entityName, undefined);
	const componentPath = OFFER_VIEW;
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
		headerText: ownProps.t(C_OFFER_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {dispatchEditRESTData, resetRESTCallLimit}), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewOfferContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
