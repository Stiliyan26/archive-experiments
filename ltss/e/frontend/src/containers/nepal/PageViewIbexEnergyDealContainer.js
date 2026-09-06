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
import {Alert, Button, Form} from "react-bootstrap";
import axios from "axios";
import {dispatchEditRESTData} from "../../actions/taskActions";
import {PATH_FOR_ERROR} from "../../static/constants";

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
const ENTITY_NAME = "ibexEnergyDeals"
const ENTITY_NAME_DOT_CLASS_NAME = "IbexEnergyDeal._className"
const ENTITY_NAME_VIEW = "ibexEnergyDealsView"
const REPORTS_ENERGY_DISTRIBUTION_CAMEL_PROCESS = "/reports/energyDistributionCamelProcess/"
const GET = "get"
const POST = "post"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const ERROR = "Error."
const IBEX_ENERGY_DEAL_DOT_DISTRIBUTE_THE_DEAL = "IbexEnergyDeal.distributeTheDeal"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewIbexEnergyDealContainer extends React.Component {
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	constructor(props){
		super(props);
		this.state = {
			warningMessage: ""
		}
	}
	
	energyDistribution(id){
		console.log("energyDistribution")
		console.log(id)
		console.log("URL: " + API_URL+REPORTS_ENERGY_DISTRIBUTION_CAMEL_PROCESS)
		//let formData = new FormData();
		//formData.append('ibexEnergyDealIdList', id);
		let formData = [id];
		axios({
			method: POST,
			url: API_URL + REPORTS_ENERGY_DISTRIBUTION_CAMEL_PROCESS,
			headers: {Authorization: sessionStorage[X_AUTH_TOKEN]},
			data: formData
		}).then(response => {
			let data = response.data;
			this.validateData(data)
			console.log(data);
			this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: data});
		//	this.props.actions.dispatchEditRESTData(this.props.componentPath+".energyDistributionResult",data);
			console.log("PageTableScheduleContainer.axios.then", data[0].Warnings);
		}).catch(error => {
			console.log(error)
			this.props.actions.dispatchEditRESTData(this.props.componentPath,{...this.props.data, pageData: undefined, result: error});
			console.log("PageTableScheduleContainer.axios.error", error)
		});
	}
	
	validateData(data){
		if (data[0].Warnings != undefined){
			this.setState({warningMessage : ERROR + data[0].Warnings})
		} else {
			this.setState({ warningMessage : ""})
		}
	}
	
	render() {
		let body = EMPTY; 
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const ibexEnergyDealLegsDef = getEntityDefinition("ibexEnergyDealLegs",{ibexEnergyDeal: {show: false}});
				body = <div className={PAGE_BODY}>
						<EmbedRetrieveEntityListContainer
							title={ibexEnergyDealLegsDef.label}
							icon={ibexEnergyDealLegsDef.icon}
							columns={ibexEnergyDealLegsDef.columns}
							componentPath={this.props.componentPath+".ibexEnergyDealLegs"}
							retrieveType="ibexEnergyDealLegs"
							parentHref={this.props.href}
							parentAttr="ibexEnergyDeal"
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
					entityName={ENTITY_NAME}
					headerText={this.props.t(ENTITY_NAME_DOT_CLASS_NAME)}
					creatable={false}
					expanded={true}
				>
					<Button onClick={() => {this.energyDistribution((this.props.match.params.entity_id ? this.props.match.params.entity_id : 0) )}}>
						{this.props.t(IBEX_ENERGY_DEAL_DOT_DISTRIBUTE_THE_DEAL)}
					</Button>
					{this.state.warningMessage ?
						<Alert variant="danger"><FontAwesomeIcon icon="ban"/> {this.props.t(this.state.warningMessage)}</Alert>
						: ""}
				</EmbedEntityContainer>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = ENTITY_NAME;
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = ENTITY_NAME_VIEW;
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
		headerText: ownProps.t(ENTITY_NAME_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewIbexEnergyDealContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
