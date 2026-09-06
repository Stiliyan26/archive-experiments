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
import { resolveObjectPath } from "../../scripts/dataUtils";

import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"

const LOGIN = "/login"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const C_DELIVERY_GOOD_MAPS = "cDeliveryGoodMaps"
const C_PRICE_LISTS = "cPriceLists"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_BODY = "page-body"
const DOT_C_DELIVERY_GOOD_MAPS = ".cDeliveryGoodMaps"
const GOD_ID = "godId"
const DOT_C_PRICE_LISTS_DEF = ".cPriceListsDef"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const SLASH = "/"
const SLASH_ADD = "/add"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const DOT = "."
const C_GOODSES = "cGoodses"
const C_GOODS_CLASS_NAME = "CGoods._className"
const C_GOODS_VIEW = "cGoodsView"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewCGoodsContainer extends React.Component {	
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
				const cDeliveryGoodMapsDef = getEntityDefinition(C_DELIVERY_GOOD_MAPS,{godId: {show: false}});
				const cPriceListsDef = getEntityDefinition(C_PRICE_LISTS,{godId: {show: false}});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={cDeliveryGoodMapsDef.label}
							icon={cDeliveryGoodMapsDef.icon}
							columns={cDeliveryGoodMapsDef.columns}
							componentPath={this.props.componentPath+DOT_C_DELIVERY_GOOD_MAPS}
							retrieveType={C_DELIVERY_GOOD_MAPS}
							parentHref={this.props.href}
							parentAttr={GOD_ID}
							parentData={this.props.data}
							expanded={true}
						/>
						<EmbedRetrieveEntityListContainer
							title={cPriceListsDef.label}
							icon={cPriceListsDef.icon}
							columns={cPriceListsDef.columns}
							componentPath={this.props.componentPath+DOT_C_PRICE_LISTS_DEF}
							retrieveType={C_PRICE_LISTS}
							parentHref={this.props.href}
							parentAttr={GOD_ID}
							parentData={this.props.data}
							expanded={true}
							asTable={true}
						/>
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+DOT_CHANGE_LOG} retrieveType={this.props.entityName}/>
					</div>
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
					entityName={C_GOODSES}
					headerText={this.props.t(C_GOODS_CLASS_NAME)}
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
	const entityName = C_GOODSES;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = C_GOODS_VIEW;
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
		headerText: ownProps.t(C_GOODS_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewCGoodsContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
