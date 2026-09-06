import React from "react";
import { Container, Row, Col, Card, Form } from "react-bootstrap";
import { Link } from 'react-router-dom';
import axios from "axios";
import moment from 'moment';

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { withTranslation } from "react-i18next";

import ReactTable from 'react-table-v6';
import * as Constants from "../../../static/constants.js";
import history from "../../../scripts/history.js";
import { isAuthenticated } from "../../../components/pages/login/Login.js";

// import Header from "../../../components/generic/Header.js";
import NewHeader from "../../../components/generic/NewHeader.js";

import CustomCardComponent from "./CustomCardComponent.js";
import Plot from 'react-plotly.js';

import { dispatchEditRESTData } from '../../../actions/taskActions.js';
import { resolveObjectPath } from "../../../scripts/dataUtils.js";

import './../dashboardScreen/CustomCardComponent.css';
import { LOGIN, ACCOUNTING_PERIOD_FETCH, LEGAL_PEOPLE_FETCH_2, POWER_PLANTS_FETCH, POWER_PLANTS_TYPES_FETCH, ELECTRICITY_INVOICES_FETCH_2 } from "../../../containers/selfie/constants/selfiePaths.js";
import { X_AUTH_TOKEN, COLOR_MAPPINGS, DOCUMENT_TYPES_PIE_CHART_COLORS } from "../../../containers/selfie/constants/selfieConstants.js";
import { getLastDayOfMonthStr, formatMonthStr } from "../../../containers/selfie/constants/selfieUtilFunctions.js";


const SPINNER = "spinner";
const CHECK_SQUARE = "check";
const BAN = "ban";
const SIZE_2X = "2x";
const PAGE_BODY = "page-body";
const PAGE_BODY_WRAPPER = "page-body-wrapper";
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin";
const DASHBOARD_SCREEN_DOT_CLASS_NAME = "DashboardScreen._className";
const DASHBOARD_CHART_PPS_PRODUCED_ENERGY = "DashboardScreen.ppsProducedEnergy";
const DASHBOARD_CHART_HOURS = "DashboardScreen.months";
const DASHBOARD_CHART_PRODUCED_ENERGY = "DashboardScreen.producedMwh";
const DASHBOARD_CARD_HEADER_TEXT_PROTOCOL = "DashboardScreen.protocolLabel";
const DASHBOARD_CARD_BUTTON_LINK_PROTOCOL = "/manufacturerProtocol";
const DASHBOARD_CARD_HEADER_TEXT_SCHEDULE = "DashboardScreen.scheduleLabel";
const DASHBOARD_CARD_BUTTON_LINK_SCHEDULE = "/schedules";
const DASHBOARD_CARD_HEADER_TEXT_LEGAL_PERSON = "DashboardScreen.legalPersonLabel";
const DASHBOARD_CARD_BUTTON_LINK_LEGAL_PERSON = "/legalPersons";
const DASHBOARD_CARD_HEADER_TEXT_MODULE = "DashboardScreen.module";
const COMPONENT_PATH_DASHBOARD_SCREEN = "dashboardScreen";
const CLASS_NAME_M_2 = "m-2";


// Page: can be used as a landing page
// View: presents details of object
// Container: redux container class
class PageViewDashboardScreenContainer extends React.Component {
	componentDidMount() {
		if (!isAuthenticated(this.props.auth)) {
			history.push(LOGIN);
		}

		this.getData();
	}

	constructor(props) {
		super(props);

		this.state = {
			powerPlantTypeLabels: [],
			powerPlantTypeCounter: [],

			numberOfInvoices: [],
			numberOfDebitNotes: [],
			numberOfCreditNotes: [],
			numberOfLegalPerson: 0,
			numberOfPowerPlants: 0,

			accountingMonth: '',
			monthIndex: 0,
			monthDays: [],
			powerPlantUpdateDate: '',
			legalPersonUpdateDate: '',
		}
	}

	getData() {
		let promiseArray = [];

		let accountingPeriod = axios({
			method: "get",
			url: API_URL + ACCOUNTING_PERIOD_FETCH,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		})
			.then(res => {
				let data = res.data._embedded.hashMaps;
				return data;
			})
			.catch(error => {
				console.log("PageViewDashboardScreenContainer.getData().accountingPeriod.axios.error" + error);
			});

		promiseArray.push(accountingPeriod);

		let legalPersons = axios({
			method: "get",
			url: API_URL + LEGAL_PEOPLE_FETCH_2,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		})
			.then(res => {
				let data = res.data._embedded.hashMaps;
				return data;
			})
			.catch(error => {
				console.log("PageViewDashboardScreenContainer.getData().legalPersons.axios.error" + error);
			});

		promiseArray.push(legalPersons);

		let powerPlants = axios({
			method: "get",
			url: API_URL + POWER_PLANTS_FETCH,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		})
			.then(res => {
				let data = res.data._embedded.hashMaps;
				return data;
			})
			.catch(error => {
				console.log("PageViewDashboardScreenContainer.getData().powerPlants.axios.error" + error);
			});

		promiseArray.push(powerPlants);

		let powerPlantTypes = axios({
			method: "get",
			url: API_URL + POWER_PLANTS_TYPES_FETCH,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		})
			.then(res => {
				let data = res.data._embedded.hashMaps;
				return data;
			})
			.catch(error => {
				console.log("PageViewDashboardScreenContainer.getData().powerPlantTypes.axios.error" + error);
			});

		promiseArray.push(powerPlantTypes);


		Promise.all(promiseArray)
			.then((res) => {
				console.log("getData() -> Promise.all -> res: ", res);

				let accountingPeriodArray = res[0];
				let currentMonth = '';
				let currentMonthIndex = 0;

				if (res[0] == undefined) {
					currentMonthIndex = new Date().getMonth() + 1;

				} else {
					currentMonth = accountingPeriodArray[0]["AccountingPeriod"].month;
					currentMonthIndex = accountingPeriodArray[0]["AccountingPeriod"].code;
				}

				// console.log("getData() -> Promise.all -> accountingPeriodArray: ", accountingPeriodArray);
				this.setState((prevState) => ({
					...prevState,
					accountingMonth: currentMonth,
					monthIndex: currentMonthIndex,
				}));
				// console.log("getData() -> Promise.all -> this.state.accountingMonth: ", this.state.accountingMonth);
				// console.log("getData() -> Promise.all -> this.state.monthIndex: ", this.state.monthIndex);

				let legalPersonsArray = res[1];
				// console.log("getData() -> Promise.all -> legalPersonsArray: ", legalPersonsArray);
				let legalPersonModifyOn = this.dateFormatting(legalPersonsArray[0]['LegalPerson'].lastModifiedDate);

				this.setState((prevState) => ({
					...prevState,
					legalPersonUpdateDate: legalPersonModifyOn,
					numberOfLegalPerson: legalPersonsArray.length,
				}));

				let powerPlantsArray = res[2];
				// console.log("getData() -> Promise.all -> powerPlantsArray: ", powerPlantsArray);
				let powerPlantModifyOn = this.dateFormatting(powerPlantsArray[0]['PowerPlant'].lastModifiedDate);

				this.setState((prevState) => ({
					...prevState,
					powerPlantUpdateDate: powerPlantModifyOn,
					numberOfPowerPlants: powerPlantsArray.length,
				}));

				let powerPlantTypes = res[3];
				// console.log("getData() -> Promise.all -> powerPlantTypes: ", powerPlantTypes);
				this.createPieChartData(powerPlantsArray, powerPlantTypes);

				this.getElectricityInvoices();
			});
	}

	getElectricityInvoices() {
		let promiseArray = [];

		let fromDate = `${new Date().getFullYear()}-${formatMonthStr(this.state.monthIndex)}-01`;
		let toDate = `${new Date().getFullYear()}-${formatMonthStr(this.state.monthIndex)}-${getLastDayOfMonthStr(this.state.monthIndex)}`;

		// console.log("getElectricityInvoices() -> fromDate: ", fromDate)
		// console.log("getElectricityInvoices() -> toDate: ", toDate)

		let electricityInvoicesArray = axios({
			method: "get",
			url: API_URL + ELECTRICITY_INVOICES_FETCH_2(fromDate, toDate),
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		})
			.then(res => {
				let data = res.data._embedded.hashMaps;
				return data;

			})
			.catch(error => {
				console.log("PageViewDashboardScreenContainer.getElectricityInvoices().axios.error" + error);
			});

		promiseArray.push(electricityInvoicesArray);

		Promise.all(promiseArray)
			.then((res) => {
				// console.log("getElectricityInvoices() -> Promise.all -> res: ", res);

				let electricityInvoicesArray = res[0];
				// console.log("getElectricityInvoices() -> Promise.all -> electricityInvoicesArray: ", electricityInvoicesArray);

				let daysArray = this.getDaysOfMonth(new Date().getFullYear(), this.state.monthIndex);
				// console.log("getElectricityInvoices() -> Promise.all -> daysArray: ", daysArray);
				
				this.setState((prevState) => ({
					...prevState,
					monthDays: daysArray,
				}));

				this.createColumnChartData(electricityInvoicesArray, daysArray);
			});
	}

	createColumnChartData(electricityInvoicesArray, daysArray) {
		// console.log("createColumnChartData() -> electricityInvoicesArray: ", electricityInvoicesArray);
		// console.log("createColumnChartData() -> daysArray: ", daysArray);

		for (let i = 0; i < daysArray.length; i++) {
			let dayIndex = daysArray[i];

			let invoiceCounter = 0;
			let debitNoteCounter = 0;
			let creditNoteCounter = 0;

			for (let e = 0; e < electricityInvoicesArray.length; e++) {
				let currentDocumentTaxEventDate = new Date(electricityInvoicesArray[e]["ElectricityInvoice"].taxEventDate).getDate();

				if (dayIndex == currentDocumentTaxEventDate) {
					// console.log("createColumnChartData() -> currentDocumentTaxEventDate: ", currentDocumentTaxEventDate)
					let currentDocumentType = electricityInvoicesArray[e]["ElectricityInvoice.loiDocumentType"].listOptionItemCode;

					// currentDocumentType == 1 - Invoice
					// currentDocumentType == 2 - Debit Note
					// currentDocumentType == 3 - Credit Note

					if (currentDocumentType == 1) {
						invoiceCounter++;

					} else if (currentDocumentType == 2) {
						debitNoteCounter++;

					} else if (currentDocumentType == 3) {
						creditNoteCounter++;
					}
				}
			}

			this.setState((prevState) => ({
				numberOfInvoices: [...prevState.numberOfInvoices, invoiceCounter],
				numberOfDebitNotes: [...prevState.numberOfDebitNotes, debitNoteCounter],
				numberOfCreditNotes: [...prevState.numberOfCreditNotes, creditNoteCounter],
			}));
		}

		// console.log("createColumnChartData() -> this.state.numberOfInvoices: ", this.state.numberOfInvoices);
		// console.log("createColumnChartData() -> this.state.numberOfDebitNotes: ", this.state.numberOfDebitNotes);
		// console.log("createColumnChartData() -> this.state.numberOfCreditNotes: ", this.state.numberOfCreditNotes);
	}

	electricityInvoiceChartData() {
		let data = [
			{
				x: this.state.monthDays,
				y: this.state.numberOfInvoices,
				name: this.props.t("Selfie.DocumentTypeInvoice"),
				type: 'bar',
				marker: {
					color: COLOR_MAPPINGS.INVOICE_BAR_CHART,
				},
			},
			{
				x: this.state.monthDays,
				y: this.state.numberOfDebitNotes,
				name: this.props.t("Selfie.DocumentTypeDebitNote"),
				type: 'bar',
				marker: {
					color: COLOR_MAPPINGS.DEBIT_NOTE_BAR_CHART,
				},
			},
			{
				x: this.state.monthDays,
				y: this.state.numberOfCreditNotes,
				name: this.props.t("Selfie.DocumentTypeCreditNote"),
				type: 'bar',
				marker: {
					color: COLOR_MAPPINGS.CREDIT_NOTE_BAR_CHART,
				},
			},
		];

		return data;
	}

	createPieChartData(powerPlantsArray, powerPlantTypes) {
		// console.log("createPieChart() -> powerPlantsArray: ", powerPlantsArray);
		// console.log("createPieChart() -> powerPlantTypes: ", powerPlantTypes);

		for (let i = 0; i < powerPlantTypes.length; i++) {
			let powerPlantTypeLabel = powerPlantTypes[i]["LoiTypeOfPowerPlant"].listOptionItemName;
			let powerPlantTypeCode = powerPlantTypes[i]["LoiTypeOfPowerPlant"].listOptionItemCode;
			let numberOfType = 0;
			
			// console.log(powerPlantTypeLabel);
			// console.log(powerPlantTypeCode);

			this.setState((prevState) => ({
				powerPlantTypeLabels: [...prevState.powerPlantTypeLabels, powerPlantTypeLabel]
			}));

			for (let e = 0; e < powerPlantsArray.length; e++) {
				let powerPlantArrayTypeCode = powerPlantsArray[e]["PowerPlant.type"].listOptionItemCode;

				if (powerPlantArrayTypeCode == powerPlantTypeCode) {
					numberOfType = numberOfType + 1;
				}
			}

			this.setState((prevState) => ({
				powerPlantTypeCounter: [...prevState.powerPlantTypeCounter, numberOfType]
			}));
		}
	}

	documentTypesPieChartData() {
		let colors = DOCUMENT_TYPES_PIE_CHART_COLORS;
		
		const data = [{
			marker: { colors: colors },
			values: this.state.powerPlantTypeCounter,
			labels: this.state.powerPlantTypeLabels,
			// texttemplate: "%{value} MWh",
			textposition: "inside",
			hole: .4,
			type: 'pie'
		}];

		return data;
	}

	dateFormatting(timestamp) {
		const date = new Date(timestamp);
	
		const options = {
			day: '2-digit', month: 'short', year: 'numeric',
			hour: '2-digit', minute: '2-digit', hour12: false,
			timeZone: 'Europe/Sofia'
		};
	
		return new Intl.DateTimeFormat('en-GB', options)
			.format(date)
			.replace(',', '')
			.replace(/(\d{2}) (\w{3}) (\d{4}) (\d{2}):(\d{2})/, '$1-$2-$3, $4:$5');
	}	

	getDaysOfMonth(year, month) {
		const daysInMonth = new Date(year, month, 0).getDate();
		return Array.from({ length: daysInMonth }, (_, i) => (i + 1).toString());
	}

	updateFromCrm() {
		// console.log("updateFromCrm()");
		let promiseArray = [];

		// URLS
		// /legal-people/synchronizations
		// /power-plants/synchronizations
		// /agreements-self-invoicing/synchronizations

		let legalPersonsUpdate = axios({
			method: "post",
			url: API_URL + `/reports/legal-people/synchronizations`,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(res => {

			return res;

		}).catch(error => {
			console.log("PageViewDashboardScreenContainer.updateFromCrm().legalPersonsUpdate.axios.error" + error);
		});

		promiseArray.push(legalPersonsUpdate);

		let powerPlantsUpdate = axios({
			method: "post",
			url: API_URL + `/reports/power-plants/synchronizations`,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(res => {

			return res;

		}).catch(error => {
			console.log("PageViewDashboardScreenContainer.updateFromCrm().powerPlantsUpdate.axios.error" + error);
		});

		promiseArray.push(powerPlantsUpdate);

		Promise.all(promiseArray)
			.then((res) => {
				// console.log("updateFromCrm() -> Promise.all -> res: ", res);
				let legalPersonsUpdate = res[0]
				console.log("updateFromCrm() -> Promise.all -> legalPersonsUpdate: ", legalPersonsUpdate);

				let powerPlantsUpdate = res[1]
				console.log("updateFromCrm() -> Promise.all -> powerPlantsUpdate: ", powerPlantsUpdate);

				axios({
					method: "post",
					url: API_URL + `/reports/agreements-self-invoicing/synchronizations`,
					headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
				}).then(res => {

					console.log("updateFromCrm() -> Promise.all -> agreements-self-invoicing: ", res)

				}).catch(error => {
					console.log("PageViewDashboardScreenContainer.updateFromCrm().Promise.all.agreements-self-invoicing.axios.error" + error);
				});

			});
	}

	render() {
		let body = "";

		if (this.props.data) {
			if (this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin />;

			} else if (this.props.id && this.props.id == parseInt(this.props.match.params.entity_id, 10)) {
				body = <div className={PAGE_BODY} />;
			}
		}

		return (
			<div className={PAGE_BODY_WRAPPER}>
				{/* <Header text={this.props.headerText} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} /> */}
				<NewHeader text={this.props.headerText} auth={this.props.auth.userAuthenticated} />
				
				<div className={PAGE_BODY}>

					<Container className="mt-4">
						<Row className="gy-4">
							<Col xs={12} sm={6} lg={4}>
								<Link to="/electricityInvoices">
									<Card className="custom-card h-100 text-center">
										<Card.Header style={{ color: 'red' }}>{this.props.t("ElectricityInvoice._className_plural")}</Card.Header>
										<Card.Body style={{ color: 'black' }}>
											<div className="mb-3">
												<i className="bi bi-upload" /> <i className="bi bi-file-pdf" />{" "}
												<i className="bi bi-pencil" /> <i className="bi bi-envelope" />
											</div>
											<ul style={{ textAlign: "left" }} className="text-start">
												<li>{this.props.t("Selfie.InvoicingDataProcessing")}.</li>
												<li>{this.props.t("Selfie.GeneratingDocuments")}.</li>
												<li>{this.props.t("Selfie.ManualDocumentEditing")}.</li>
												<li>{this.props.t("Selfie.DownloadingDocuments")}.</li>
											</ul>
										</Card.Body>
									</Card>
								</Link>
							</Col>

							<Col xs={12} sm={6} lg={4}>
								<Link to="/legalPersons">
									<Card className="custom-card h-100 text-center">
										<Card.Header style={{ color: 'red' }}>{this.props.t("Selfie.ManagingRanges")}</Card.Header>
										<Card.Body style={{ color: 'black' }}>
											<div className="mb-3">
												<i className="bi bi-person-check" /> <i className="bi bi-building" />{" "}
												<i className="bi bi-save" />
											</div>
											<ul style={{ textAlign: "left" }} className="text-start">
												<li>{this.props.t("Selfie.ManagingRangesDescFirstPart")}.<br></br>{this.props.t("Selfie.ManagingRangesDescSecondPart")}.</li>
											</ul>
										</Card.Body>
									</Card>
								</Link>
							</Col>

							<Col xs={12} sm={6} lg={4}>
								<Link to="/importSelfieFiles">
									<Card className="custom-card h-100 text-center">
										<Card.Header style={{ color: 'red' }}>{this.props.t("Import.titlePlural")}</Card.Header>
										<Card.Body style={{ color: 'black' }}>
											<div className="mb-3">
												<i className="bi bi-file-earmark-text" /> <i className="bi bi-gear" />{" "}
												<i className="bi bi-envelope" />
											</div>
											<ul style={{ textAlign: "left" }} className="text-start">
												<li>{this.props.t("Selfie.LoadingDataForGeneratingIvoices")}.</li>
												<li>{this.props.t("Selfie.LoadingDataForEditingDocuments")}.</li>
												<li>{this.props.t("Selfie.DeactivatingActiveEntries")}.</li>
											</ul>
										</Card.Body>
									</Card>
								</Link>
							</Col>
						</Row>
					</Container>

					<Container style={{ marginTop: "5%", marginBottom: "1%", width: '100%', height: '400px' }}>
						<Plot
							data={this.electricityInvoiceChartData()}
							layout={{
								title: `${this.props.t('Selfie.GeneratedDocumentsForAccountingPeriod')}: ${this.state.accountingMonth}`,
								barmode: 'stack',
								xaxis: {
									title: this.props.t("Days"),
								},
								yaxis: {
									title: this.props.t("NumberOfDocuments"),
								},
								// height: 400,
								// width: 700,
								margin: {
									l: 50,
									r: 50,
									t: 50,
									b: 50,
								},
								paper_bgcolor: COLOR_MAPPINGS.BACKGROUND,
								plot_bgcolor: COLOR_MAPPINGS.BACKGROUND,
								legend: {
									orientation: 'h',
									x: 0,
									y: 1.08,
									xanchor: 'left',
									yanchor: 'top',

								},
							}}
							config={{
								responsive: true,
								displayModeBar: false
							}}
							style={{ width: '100%', height: '100%' }}
						/>
					</Container>

					<Container style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gridGap: "5px", marginTop: "5%", marginBottom: "1%" }}>
						<Plot
							data={this.documentTypesPieChartData()}
							layout={{
								title: this.props.t("Selfie.PowerPlantTypes"),
								height: 400,
								// width: 450,
								// responsive: true,
								// margin: { "t": 10, "b": 10, "l": 30, "r": 30 },
								margin: {
									l: 0, // Left margin
									r: 0, // Right margin
									t: 50, // Top margin
									b: 0, // Bottom margin
								},
								padding: { "l": 10 },
								// automargin: true,
								legend: {
									orientation: 'h',
									x: 0.5,
									y: -0.2,
									xanchor: 'center',
								},
								paper_bgcolor: COLOR_MAPPINGS.BACKGROUND,
								annotations: [{ showarrow: false, text: '' }],
								transition: {
									duration: 500,
									easing: 'ease-in-out'
								},
							}}
						/>

						<div>
							<Card>
								<Card.Header style={{ display: "flex", justifyContent: "center" }}>{this.props.t("Selfie.LegalPeoplePowerPlants")}</Card.Header>

								<Card.Body style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gridGap: "5px", marginTop: "1.5%", marginBottom: "1%" }}>
									<Form.Row style={{ gap: "1vw" }} >
										<Form.Group >
											<Form.Label>{this.props.t("Selfie.LegalPeople")}: {this.state.numberOfLegalPerson}</Form.Label>
										</Form.Group>
										{/* <Form.Group>
											<Form.Label>{this.props.t("ElectricityInvoice.periodTo")}:</Form.Label>
											<Form.Control type="date" required ref="periodTo" />
										</Form.Group> */}
									</Form.Row>

									<Form.Row style={{ gap: "1vw" }}>
										<Form.Group>
											<Form.Label>{this.props.t("PowerPlant._className_plural")}: {this.state.numberOfPowerPlants}</Form.Label>
										</Form.Group>
									</Form.Row>

									<Form.Row style={{ gap: "1vw" }}>
										<Form.Group>
											<Form.Label>{this.props.t("Selfie.UpdatedOn")}: </Form.Label>
											<Form.Label>{this.state.legalPersonUpdateDate}</Form.Label>
										</Form.Group>
									</Form.Row>

									<Form.Row style={{ gap: "1vw" }}>
										<Form.Group>
											<Form.Label>{this.props.t("Selfie.UpdatedOn")}: </Form.Label>
											<Form.Label>{this.state.powerPlantUpdateDate}</Form.Label>
										</Form.Group>
									</Form.Row>
								</Card.Body>

								<Card.Footer>
									{/* Populate Electricity Invoices Action Button */}
									<div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
										<button className="button" onClick={() => { this.updateFromCrm() }}>
											{this.props.t("Update")}
										</button>
									</div>
								</Card.Footer>
							</Card>
						</div>
					</Container>
				</div>
				{body}
			</div>
		);
	}
}

// redux mapping
function mapStateToProps(state, ownProps) {
	const componentPath = COMPONENT_PATH_DASHBOARD_SCREEN;
	let viewData = resolveObjectPath(componentPath, state.rest);
	let data = viewData;
	return {
		auth: state.auth,
		componentPath: componentPath,
		data: data,
		//UI
		headerText: ownProps.t(DASHBOARD_SCREEN_DOT_CLASS_NAME),
	};
}

// redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

// export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewDashboardScreenContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
