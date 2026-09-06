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
const PAGE_BODY = "page-body"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const SLASH = "/"
const SLASH_ADD = "/add"
const PAGE_URL_IS_MISSING_FOR_ENTITY = "pageURL is missing for entity "
const DOT = "."
const POWER_PLANT_PROFILES = "powerPlantProfiles"
const POWER_PLANT_PROFILES_DOT_CLASS_NAME = "PowerPlantProfile._className"
const POWER_PLANT_PROFILE_VIEW = "powerPlantProfileView"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewPowerPlantProfileContainer extends React.Component {	
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
				const powerPlantsDef = getEntityDefinition("powerPlants",{powerPlantProfile: {show: false}});
				const quarterOfHours = getEntityDefinition("quarterOfHours",{powerPlantProfile: {show: false}});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={powerPlantsDef.label}
							icon={powerPlantsDef.icon}
							columns={powerPlantsDef.columns}
							componentPath={this.props.componentPath+".powerPlants"}
							retrieveType="powerPlants"
							parentHref={this.props.href}
							parentAttr="powerPlantProfile"
							parentData={this.props.data}
							asTable={true}
							expanded={true}
						/>
						<EmbedRetrieveEntityListContainer
							title={quarterOfHours.label}
							icon={quarterOfHours.icon}
							columns={quarterOfHours.columns}
							componentPath={this.props.componentPath+".quarterOfHours"}
							retrieveType="quarterOfHours"
							parentHref={this.props.href}
							parentAttr="powerPlantProfile"
							parentData={this.props.data}
							asTable={true}
							expanded={true}
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
					entityName={POWER_PLANT_PROFILES}
					headerText={this.props.t(POWER_PLANT_PROFILES_DOT_CLASS_NAME)}
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
	const entityName = POWER_PLANT_PROFILES;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = POWER_PLANT_PROFILE_VIEW;
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
		headerText: ownProps.t(POWER_PLANT_PROFILES_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewPowerPlantProfileContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
