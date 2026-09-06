import React from "react";
import {Alert, Button, Form} from "react-bootstrap";
import { Link } from 'react-router-dom'
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
import NepalDBFileContainer from "./NepalDBFileContainer";
import PageViewForecastContainer from "./PageViewForecastContainer";
import RetrieveDataContainer from "./../nomenclatures/RetrieveDataContainer";

import Plot from 'react-plotly.js';
import createPlotlyRenderers from 'react-pivottable/PlotlyRenderers';

import { getEntityDefinition } from "../nomenclatures/entityDefinitions.js"
import { dispatchEditRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from "../../scripts/dataUtils";

const LOGIN = "/login"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const PAGE_BODY = "page-body"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const UPLOAD_SCHEDULE_DOT_CLASS_NAME = "UploadSchedule._className"
const COMPONENT_PATH_DASHBOARD = "dashboard"
const CLASS_NAME_M_2 = "m-2"
const POST = "post"
const GET = "get"
const IMPORT_XML = "/reports/importXML/"
const REPORTS_SEND_MAIL = "/reports/sendMail/"
const REPORTS_GET_IBEX_ENERGY_DEAL = "/reports/getIbexEnergyDeal"
const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const ERROR = "Error."

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewUploadScheduleContainer extends React.Component {
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	constructor(props){
		super(props);
		this.state = {
			dbFileId: this,
			warningMessage: "",
			warningMessageForImportXml: ""
		}
	}
	
	validateData(data){
		if (data[0].Warnings != undefined){
			this.setState({warningMessage : ERROR + data[0].Warnings})
		} else {
			this.setState({ warningMessage : ""})
		}
	}
	
	validateDataForImportXml(data){
		if (data[0].Warnings != undefined){
			this.setState({warningMessageForImportXml : ERROR + data[0].Warnings})
		} else {
			this.setState({ warningMessageForImportXml : ""})
		}
	}

	createSchedule(id){
		console.log("PageTableScheduleContainer")
		console.log(id)
		//url = API_URL+IMPORT_XML+id
		console.log("URL: " + API_URL+IMPORT_XML+id)
		axios({
			method: POST,
			url: API_URL+IMPORT_XML+id, 
			headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
		}).then(response => {
			let data = response.data;
			this.validateDataForImportXml(data)
			console.log("PageTableScheduleContainer.axios.then", data);
			//force refresh
			// this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
		}).catch(error => {
			console.log("PageTableScheduleContainer.axios.error", error)
		});
	}
	
	getIbexEnergyDealTest(){
		console.log("getIbexEnergyDealTest")
		console.log("URL: " + API_URL+REPORTS_GET_IBEX_ENERGY_DEAL)
		axios({
			method: POST,
			url: API_URL+REPORTS_GET_IBEX_ENERGY_DEAL,
			headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
		}).then(response => {
			let data = response.data;
			this.validateData(data)
			console.log(data);
			this.props.actions.dispatchEditRESTData(this.props.componentPath+".ibexEnergyDealImportResult",data);
			console.log("PageTableScheduleContainer.axios.then", data);
		}).catch(error => {
			console.log("PageTableScheduleContainer.axios.error", error)
		});
	}
	
	sendMail(id){
	//	console.log("PageTableScheduleContainer")
	//	console.log()
		//url = API_URL+IMPORT_XML+id
		console.log("URL: " + API_URL+ REPORTS_SEND_MAIL + id)
		axios({
			method: POST,
			url: API_URL+ REPORTS_SEND_MAIL + id,
			headers: {Authorization: sessionStorage[X_AUTH_TOKEN]}
		}).then(response => {
			let data = response.data;
		}).catch(error => {
			console.log("PageTableScheduleContainer.axios.error.sendMail", error)
		});
	}

	getChartData() {
		let data = [
			{
				type: 'scatter',
				mode: 'lines+markers',
				marker: {color: 'red'},
				//marker: {color: "#1797FF"},
				x: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24],
				y: [0, 0, 0, 0, 0, 0, 2, 2.5, 3.6, 3.5, 5, 9, 9.1, 12, 12.5, 13.6, 9.5, 15, 9, 8.5, 5.6, 5, 5, 0],
				
			},
			{
				type: 'bar', 
				marker: {color: '#0F65AA'},
				x: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24], 
				y: [0, 0, 0, 0, 0, 0, 2, 2.5, 3.6, 3.5, 5, 9, 9.1, 12, 12.5, 13.6, 9.5, 15, 9, 8.5, 5.6, 5, 5, 0]
			},
		]

		return data
	}
	
	
	render() {
		let dbFileId;
		let body = EMPTY; 
		let ibexEnergyDealsBody = EMPTY;
		let energyDistributionBody = EMPTY;
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				body = <div className={PAGE_BODY} />;
			}
		}
		if(this.props.ibexEnergyDealsData ) {
			ibexEnergyDealsBody = <Form.Group className={CLASS_NAME_M_2}>
					Импортирани са сделки: <Link to={"/ibexEnergyDeals/"+this.props.ibexEnergyDealsData.id}>{this.props.ibexEnergyDealsData.tradeId}</Link>
				</Form.Group>;
		}
		if(this.props.energyDistributionResult && this.props.energyDistributionResult[0]) {
			energyDistributionBody = <Form.Group className={CLASS_NAME_M_2}>
					Сделката е разпределена по графици: {this.props.energyDistributionResult[0].result.map((curr) => <div key ={curr}><Link to={"/schedules/"+curr}>{curr}</Link></div>)}
				</Form.Group>;
		}
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={this.props.headerText} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<div className={PAGE_BODY}>
					<Form>
						<Form.Row>
							<Form.Group className={CLASS_NAME_M_2}>
								<NepalDBFileContainer 
									onFileUpload = {(id) => this.createSchedule(id) & (dbFileId = id)}
									buttonText = {this.props.t("NepalDBFile.schedule")}
									nepalDbFileInputTagId = "fileSchedule"
								/>
								{this.state.warningMessageForImportXml ?
									<Alert variant="danger"><FontAwesomeIcon icon="ban"/> {this.props.t(this.state.warningMessageForImportXml)}</Alert>
									: ""}
							</Form.Group>
							{/* <Form.Group style={{right: "0px"}}>
								<PageViewForecastContainer />
							</Form.Group> */}
						</Form.Row>
						<Form.Row>
							<Form.Group className={CLASS_NAME_M_2}>
								<Button onClick={() => {this.getIbexEnergyDealTest()}}>
									Сделки от IBEX
								</Button>
								{this.state.warningMessage ?
									<Alert variant="danger"><FontAwesomeIcon icon="ban"/> {this.props.t(this.state.warningMessage)}</Alert>
									: ""}
							</Form.Group>
							{ibexEnergyDealsBody}
							<RetrieveDataContainer
								retrieveType={"ibexEnergyDeals"}
								componentPath={this.props.componentPath+".ibexEnergyDeals"}
								defaultFilter={[{id: "id", value: (this.props.ibexEnergyDealImportResult ? this.props.ibexEnergyDealsImportResult : 123)}]}
							/>
						</Form.Row>
						<Form.Row>
							
							{/* <Form.Group className={CLASS_NAME_M_2}>
								<Button onClick={() => {this.sendMail(this.props.energyDistributionResult[0].result)}}>
									{this.props.t("Send")}
								</Button>
							</Form.Group> */}
						</Form.Row>
						<Form.Row>
							{/* <PageViewForecastContainer /> */}
						</Form.Row>
					</Form>

					<Plot 
						data={this.getChartData()}
						layout={ 
							{
								width: 1024, 
								title: "PPS Produced Energy", 
								xaxis:{title: "Hours"}, 
								yaxis:{title: "Produced MHw"},
								plot_bgcolor: "F7F7FF",
								paper_bgcolor: "F7F7FF"
							} 
						}
					/>
					
				</div>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const componentPath = COMPONENT_PATH_DASHBOARD;
	let viewData = resolveObjectPath(componentPath,state.rest);
	let data = viewData;
	let ibexEnergyDealsData = undefined;
	if(data && data.ibexEnergyDeals && data.ibexEnergyDeals._embedded
			&& data.ibexEnergyDeals._embedded.ibexEnergyDeals instanceof Array) {
		ibexEnergyDealsData = data.ibexEnergyDeals._embedded.ibexEnergyDeals[0];
	}
	return {
		auth: state.auth,
		componentPath: componentPath,
		data: data,
		ibexEnergyDealsData: ibexEnergyDealsData,
		ibexEnergyDealImportResult: data ? data.ibexEnergyDealImportResult : undefined,
		energyDistributionResult: data ? data.energyDistributionResult : undefined,
		//UI
		headerText: ownProps.t(UPLOAD_SCHEDULE_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewUploadScheduleContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
