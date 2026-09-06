import React from "react";

import axios from "axios";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"
import { Button, Form } from "react-bootstrap";

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
const SCHEDULES = "schedules"
const SCHEDULE_DOT_CLASS_NAME = "Schedule._className"
const SCHEDULE_VIEW = "scheduleView"

const REPORTS_SEND_MAIL = "/reports/sendMail/"
const POST = "post"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewScheduleContainer extends React.Component {
	componentDidMount() {
		if (!isAuthenticated(this.props.auth)) {
			history.push(LOGIN)
		}
	}

	sendMail(){
			console.log("URL: " + API_URL+ REPORTS_SEND_MAIL + this.props.id)
			axios({
				method: POST,
				url: API_URL+ REPORTS_SEND_MAIL + this.props.id,
				headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
			}).then(response => {
				let data = response.data;
				console.log("responce", data)
			}).catch(error => {
				console.log("PageViewScheduleContainer.axios.error.sendMail", error)
			});
		}

	render() {
		let body = EMPTY;
		if (this.props.data) {
			if (this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin />;
			} else if (this.props.id && this.props.id == parseInt(this.props.match.params.entity_id, 10)) {
				const scheduleTimeSeries = getEntityDefinition("scheduleTimeSeries", { schedule: { show: false } });
				//const employeeCompetencesDef = getEntityDefinition("employeeCompetences",{employee: {show: false}});
				body = <div className={PAGE_BODY}>
					<EmbedRetrieveEntityListContainer
						title={scheduleTimeSeries.label}
						icon={scheduleTimeSeries.icon}
						columns={scheduleTimeSeries.columns}
						componentPath={this.props.componentPath + ".scheduleTimeSeries"}
						retrieveType="scheduleTimeSeries"
						parentHref={this.props.href}
						parentAttr="schedule"
						parentData={this.props.data}
						asTable={true}
						expanded={true}
					/>
					{/* <EmbedRetrieveEntityListContainer
							title={employeeCompetencesDef.label}
							icon={employeeCompetencesDef.icon}
							columns={employeeCompetencesDef.columns}
							componentPath={this.props.componentPath+".employeeCompetences"}
							retrieveType="employeeCompetences"
							parentHref={this.props.href}
							parentAttr="employee"
							parentData={this.props.data}
						/> */}
					<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath + DOT_CHANGE_LOG} retrieveType={this.props.entityName} />
				</div>;
			}
		}
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={this.props.headerText} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<EmbedEntityContainer
					componentPath={this.props.componentPath + DOT + this.props.entityDef.className}
					retrieve_id={this.props.match.params.entity_id}
					onBeforeChange={(data) => {
						if (!this.props.entityDef.pageURL) { console.error(PAGE_URL_IS_MISSING_FOR_ENTITY + this.props.entityDef.className); }
						if (data.id) {
							history.push(this.props.entityDef.pageURL + SLASH + data.id);
						} else {
							history.push(this.props.entityDef.pageURL + SLASH_ADD);
						}
					}}
					entityName={SCHEDULES}
					headerText={this.props.t(SCHEDULE_DOT_CLASS_NAME)}
					creatable={false}
					expanded={true}
				>
					<Button onClick={() => { this.sendMail() }}>
						{this.props.t("Send")}
					</Button>
				</EmbedEntityContainer>
				{body}
			</div>
		);
	}
}

//redux mapping
function mapStateToProps(state, ownProps) {
	const entityName = SCHEDULES;
	const entityDef = getEntityDefinition(entityName, undefined);
	const componentPath = SCHEDULE_VIEW;
	let viewData = resolveObjectPath(componentPath, state.rest);
	let data = undefined;
	if (viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded
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
		headerText: ownProps.t(SCHEDULE_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {}), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewScheduleContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
