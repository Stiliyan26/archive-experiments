import React from "react";
import './dashboardScreen/CustomCardComponent.css'
import { Container, Button, Form, Alert, ProgressBar, Spinner, Card, Tab, Tabs } from "react-bootstrap";
import ReactTable from 'react-table-v6'
import Modal from 'react-responsive-modal';
import axios from "axios";
import ReactToPrint from "react-to-print";
import NepalDBFileContainer from "./NepalDBFileContainer.js";
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Plot from 'react-plotly.js';

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"
import moment from 'moment';

import { FontAwesomeIcon } from "@fortawesome/react-fontawesome"
import { withTranslation } from "react-i18next";

import * as Constants from "../../static/constants";
import history from "../../scripts/history"
import { isAuthenticated } from "../../components/pages/login/Login.js"
import { dispatchEditRESTData, fetchRESTFollow, patchRESTMultiData, patchRESTData } from './../../actions/taskActions';

import Header from "../../components/generic/Header"
import ManufacturerProtocolPDF from "./ManufacturerProtocolPDF.js"
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"
import EmbedChangeHistory from "../embeds/EmbedChangeHistory"

import { getEntityDefinition, builderDataToProjection } from "../nomenclatures/entityDefinitions.js"
import { resolveObjectPath, getLoiByCode } from "../../scripts/dataUtils";

import XLSX from 'xlsx'
import { getPdfBase64, downloadPdfZip } from "./PdfMakeContentManufacturerProtocol.js";


const LOGIN = "/login"
const EMPTY = ""
const SPINNER = "spinner"
const SIZE_2X = "2x"
const PAGE_BODY = "page-body"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const DOT_CHANGE_LOG = ".changelog"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"
const INTERVALS = "intervals"
const MANUFACTURER_PROTOCOL_DOT_CLASS_NAME = "ManufacturerProtocol._className"
const MANUFACTURER_PROTOCOL_VIEW = "manufacturerProtocolView"
const CLASS_NAME_M_2 = "m-2"

const X_AUTH_TOKEN = "X-AUTH-TOKEN"
const POST = "post"
const GET = "get"
const REPORTS_IMPORT_XLSX_PRODUCED_SCHEDULE = "/reports/importXLSXProducedSchedule/"
const REPORTS_IMPORT_XLSX_METER_READING = "/reports/importXLSXMeterReading/"
const REPORTS_IMPORT_XLSX_IBEX_PRICE = "/reports/importIbexPriceDAM/"
const REPORTS_AUCTION_PRICES = "/reports/getAuctionPrices"

const DEFAULT_TAB_KEY_STATUS = "status"
const TAB_KEY_GENERATE = "generate"
const TAB_KEY_UPLOAD = "upload"

const DASHBOARD_CHART_PLOT_COLOR = "#F7F7FF"

const dropdownValues = [
	{
		index: 1,
		label: "Методология",
		isVisible: false
	},
	{
		index: 2,
		label: "Избери период",
		isVisible: true
	}
]

var detailEpData = [];
var detailTableData = [];

const columns = [{
	Header: 'Съобщение',
	accessor: 'message'
}, {
	Header: 'ИТН / Обект',
	accessor: 'itn'
}, {
	Header: 'Произход',
	accessor: 'origin'
}, {
	Header: 'Дата',
	accessor: 'date'
}]

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewManufacturerProtocolContainer extends React.Component {

	componentDidMount() {

		if (!isAuthenticated(this.props.auth)) {
			history.push(LOGIN)
		}

		this.definingTimePeriod()
	}

	constructor(props) {
		super(props);
		this.state = {
			tableData: [],

			warningArray: [],
			fromDate: '',
			toDate: '',
			currentDate: new Date(),

			isAlert: false,
			alertMessage: "",
			isDropdownVisible: false,
			dropdownSelectedItn: "",
			indexedPriceArray: [],
			objectList: [],
			createdProtocolsArray: [],

			getObjectList: false,
			getPriceList: false,
			getProductionList: false,
			getMeterList: false,
			calculating: false,

			//Tabs and Charts states
			isTabDisabled: false,
			tabKey: DEFAULT_TAB_KEY_STATUS,
			generatedProtocols: 0,
			generatedProtocolsProduced: 0,
			generatedProtocolsMeasured: 0,
			nonGeneratedProtocols: 0,
			nonGeneratedProtocolsProduced: 0,
			nonGeneratedProtocolsMeasured: 0,
			numberOfLegalPersons: 0,
			numberOfObjects: 0,
			uploadedPriseList: false,
			uploadedMeterList: false,
			uploadedProductionList: false,
			createdProtocols: 0,

			allToBeCreated: 0,

			//Upload States
			uploadPriceList: false,
			uploadProducedEnergyList: false,
			uploadMeterEnergyList: false,

			objectDropdownList: []
		};

		this.meterEnergyPages = 0
		this.producedEnergyPages = 0
		this.priceListPages = 0
		this.objectListPages = 0
	}

	// ----------------------------------------------------------- START FUNCTIONS ---------------------------------------------------

	definingTimePeriod() {

		this.setState({ isTabDisabled: true, tabKey: DEFAULT_TAB_KEY_STATUS })

		let currentDay = new Date(this.state.currentDate).getDate()
		let currentMonth = new Date(this.state.currentDate).getMonth() + 1
		let currentYear = new Date(this.state.currentDate).getFullYear()
		let previousYear = new Date(this.state.currentDate).getFullYear() - 1

		let rangeFromDate = ''
		let rangeToDate = ''

		if (currentDay <= 10) {
			if (currentMonth == 1) {
				rangeFromDate = moment(new Date(previousYear + "-12" + "-01")).format("YYYY-MM-DD")
				rangeToDate = moment(new Date(previousYear + "-12" + "-" + this.getLastDayOfMonth(12))).format("YYYY-MM-DD")
			} else {
				rangeFromDate = moment(new Date(currentYear + "-" + (currentMonth - 1) + "-01")).format("YYYY-MM-DD")
				rangeToDate = moment(new Date(currentYear + "-" + (currentMonth - 1) + "-" + this.getLastDayOfMonth(currentMonth - 1))).format("YYYY-MM-DD")
			}
		} else {
			rangeFromDate = moment(new Date(currentYear + "-" + currentMonth + "-01")).format("YYYY-MM-DD")
			rangeToDate = moment(new Date(currentYear + "-" + currentMonth + "-" + currentDay)).format("YYYY-MM-DD")
		}

		this.setState({ fromDate: rangeFromDate, toDate: rangeToDate }, () => {
			console.log("definingTimePeriod() -> this.state.fromDate: ", this.state.fromDate)
			console.log("definingTimePeriod() -> this.state.toDate: ", this.state.toDate)

			this.getDataFromDatabase()
		})
	}

	getDataFromDatabase() {
		let newFromDate = this.returnStateDate(this.state.fromDate, false)
		let newToDate = this.returnStateDate(this.state.toDate, true)

		let promiseArray = []

		const getData = axios({
			method: "get",
			url: API_URL + `/reports/sumKwh/${newFromDate}/${newToDate}`,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(res => {
			let data = res.data
			return data
		}).catch(error => {
			console.log("PageViewManufacturerProtocolContainer.axios.error" + error)
		});

		const createdProtocols = axios({
			method: "get",
			url: API_URL + `/reports/builder/1?from=PowerPlantProtocol&select=PowerPlantProtocol.owner,PowerPlantProtocol.powerPlant,PowerPlantProtocol.loiContractQuantity,PowerPlantProtocol.loiContractPrice,PowerPlantProtocol.loiProtocolLineCount,PowerPlantProtocol.loiProtocolStatus,PowerPlantProtocol.sendMailMessage,PowerPlantProtocol&page=0&size=1000&sort=PowerPlantProtocol.id%2Cdesc`,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(res => {
			// console.log("getDataFromDatabase() -> createdProtocols-axios-res: ", res)
			let data = res.data._embedded.hashMaps
			return data
		}).catch(error => {
			console.log("PageViewManufacturerProtocolContainer.axios.error" + error)
			let data = []
			return data
		});

		const objectList = axios({
			method: "get",
			url: API_URL + `/reports/builder/1?from=PowerPlant&select=PowerPlant.type,PowerPlant.grid,PowerPlant.powerPlantProfile,PowerPlant.owner,PowerPlant.contractStatus,PowerPlant.loiContractQuantity,PowerPlant.loiContractPrice,PowerPlant.loiContractFee,PowerPlant.loiProtocolCountPerMonth,PowerPlant.loiProtocolLineCount,PowerPlant&page=0&size=1000&sort=PowerPlant.id%2Cdesc`,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(res => {
			let data = res.data._embedded.hashMaps;
			return data
		}).catch(error => {
			console.log("PageViewManufacturerProtocolContainer.axios.error" + error)
		});

		promiseArray.push(getData)
		promiseArray.push(createdProtocols)
		promiseArray.push(objectList)

		Promise.all(promiseArray)
			.then((res) => {

				let getProtocolsData = res[0]
				if (getProtocolsData.length > 0) {
					detailEpData = getProtocolsData
					this.setState({ uploadedMeterList: true, uploadedProductionList: true, uploadedPriseList: true })
				}

				let createdProtocolsData = res[1]
				this.setState({ createdProtocolsArray: createdProtocolsData })

				let objectsData = res[2]
				let legalPersons = []
				for (let i = 0; i < objectsData.length; i++) {
					let uniqueLegalPerson = objectsData[i]["PowerPlant.owner"].id
					if (legalPersons.length == 0) {
						legalPersons.push(uniqueLegalPerson)
					} else {
						let isExist = false
						for (let e = 0; e < legalPersons.length; e++) {
							if (legalPersons[e] == uniqueLegalPerson) {
								isExist = true
								break
							}
						}

						if (!isExist) {
							legalPersons.push(uniqueLegalPerson)
						}
					}
				}

				this.setState({
					objectList: objectsData,
					objectDropdownList: objectsData,
					numberOfLegalPersons: legalPersons.length,
					numberOfObejcts: objectsData.length
				}, () => {
					console.log("getDataFromDatabase() -> detailEpData: ", detailEpData)
					console.log("getDataFromDatabase() -> this.state.uploadedMeterList: ", this.state.uploadedMeterList)
					console.log("getDataFromDatabase() -> this.state.uploadedProductionList: ", this.state.uploadedProductionList)
					console.log("getDataFromDatabase() -> this.state.uploadedPriseList: ", this.state.uploadedPriseList)
					console.log("getDataFromDatabase() -> this.state.createdProtocolsArray: ", this.state.createdProtocolsArray)
					console.log("getDataFromDatabase() -> this.state.objectList: ", this.state.objectList)
					console.log("getDataFromDatabase() -> this.state.objectDropdownList: ", this.state.objectDropdownList)
					console.log("getDataFromDatabase() -> this.state.numberOfLegalPersons: ", this.state.numberOfLegalPersons)
					console.log("getDataFromDatabase() -> this.state.numberOfObejcts: ", this.state.numberOfObejcts)

					this.defineObjectDateRangeForProtocols()
				})
			})
	}

	async defineObjectDateRangeForProtocols() {
		let protocolsArray = []

		const date1 = new Date(this.state.toDate);
		const date2 = new Date(this.state.fromDate);
		const timeDiff = Math.abs(date2 - date1);
		const dayDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24)) + 1;

		for (let i = 0; i < this.state.objectList.length; i++) {

			let currentMonth = new Date(this.state.fromDate).getMonth() + 1
			let currentYear = new Date(this.state.fromDate).getFullYear()

			let protocolCountPerMonthCode = this.state.objectList[i]['PowerPlant.loiProtocolCountPerMonth'].listOptionItemCode
			//protocolCountPerMonthCode 1: За цял месец
			//protocolCountPerMonthCode 2: За 15 дни
			//protocolCountPerMonthCode 3: за 10 дни

			if (dayDiff > 9 && dayDiff <= 15) {
				if (protocolCountPerMonthCode == 3) {
					let currentObject = Object.assign({}, this.state.objectList[i])

					currentObject.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-1',
						toDate: currentYear + '-' + currentMonth + '-10'
					}

					protocolsArray.push(currentObject)
				}
			} else if (dayDiff > 15 && dayDiff <= 20) {
				if (protocolCountPerMonthCode == 3) {
					let currentObject = Object.assign({}, this.state.objectList[i])

					currentObject.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-1',
						toDate: currentYear + '-' + currentMonth + '-10'
					}

					protocolsArray.push(currentObject)
				} else if (protocolCountPerMonthCode == 2) {
					let currentObject = Object.assign({}, this.state.objectList[i])

					currentObject.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-1',
						toDate: currentYear + '-' + currentMonth + '-15'
					}

					protocolsArray.push(currentObject)
				}
			} else if (dayDiff > 21 && dayDiff <= 25) {
				if (protocolCountPerMonthCode == 3) {
					let currentObject1 = Object.assign({}, this.state.objectList[i])

					currentObject1.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-1',
						toDate: currentYear + '-' + currentMonth + '-10'
					}

					protocolsArray.push(currentObject1)

					let currentObject2 = Object.assign({}, this.state.objectList[i])

					currentObject2.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-11',
						toDate: currentYear + '-' + currentMonth + '-20'
					}

					protocolsArray.push(currentObject2)
				} else if (protocolCountPerMonthCode == 2) {
					let currentObject = Object.assign({}, this.state.objectList[i])

					currentObject.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-1',
						toDate: currentYear + '-' + currentMonth + '-15'
					}

					protocolsArray.push(currentObject)
				}

			} else if (dayDiff > 25 && dayDiff <= 40) {

				if (protocolCountPerMonthCode == 3) {
					let newCurrentObject1 = Object.assign({}, this.state.objectList[i])

					newCurrentObject1.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-1',
						toDate: currentYear + '-' + currentMonth + '-10'
					}

					protocolsArray.push(newCurrentObject1)

					let newCurrentObject2 = Object.assign({}, this.state.objectList[i])

					newCurrentObject2.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-11',
						toDate: currentYear + '-' + currentMonth + '-20'
					}

					protocolsArray.push(newCurrentObject2)

					let newCurrentObject3 = Object.assign({}, this.state.objectList[i])

					newCurrentObject3.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-21',
						toDate: currentYear + '-' + currentMonth + '-' + this.getLastDayOfMonth(currentMonth) // '30'
					}

					protocolsArray.push(newCurrentObject3)

				} else if (protocolCountPerMonthCode == 2) {
					let newCurrentObject1 = Object.assign({}, this.state.objectList[i])

					newCurrentObject1.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-1',
						toDate: currentYear + '-' + currentMonth + '-15'
					}

					protocolsArray.push(newCurrentObject1)

					let newCurrentObject2 = Object.assign({}, this.state.objectList[i])

					newCurrentObject2.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-16',
						toDate: currentYear + '-' + currentMonth + '-' + this.getLastDayOfMonth(currentMonth)//'-30'
					}

					protocolsArray.push(newCurrentObject2)
				} else if (protocolCountPerMonthCode == 1) {
					let newCurrentObject1 = Object.assign({}, this.state.objectList[i])

					newCurrentObject1.dateRange = {
						fromDate: currentYear + '-' + currentMonth + '-1',
						toDate: currentYear + '-' + currentMonth + '-' + this.getLastDayOfMonth(currentMonth)//'-31'
					}

					protocolsArray.push(newCurrentObject1)
				}

			}
		}

		this.setState({ objectList: protocolsArray }, () => {
			console.log("defineObjectDateRangeForProtocols() -> this.state.objectList: ", this.state.objectList)

			this.defineStatisticsForProtocols()
		})
	}

	defineStatisticsForProtocols() {

		let objectArray = this.state.objectList

		let generatedProtocols_var = 0
		let generatedProtocolsProduced_var = 0
		let generatedProtocolsMeasured_var = 0
		let nonGeneratedProtocols_var = 0
		let nonGeneratedProtocolsProduced_var = 0
		let nonGeneratedProtocolsMeasured_var = 0

		for (let i = 0; i < this.state.createdProtocolsArray.length; i++) {
			let createdProtocols_itn = this.state.createdProtocolsArray[i]["PowerPlantProtocol.powerPlant"].identificationNumber
			let createdProtocols_fromDate = new Date(this.state.createdProtocolsArray[i]["PowerPlantProtocol"].dateFrom).getDate() + "-" + (new Date(this.state.createdProtocolsArray[i]["PowerPlantProtocol"].dateFrom).getMonth() + 1) + "-" + new Date(this.state.createdProtocolsArray[i]["PowerPlantProtocol"].dateFrom).getFullYear()
			let createdProtocols_toDate = new Date(this.state.createdProtocolsArray[i]["PowerPlantProtocol"].dateTo).getDate() + "-" + (new Date(this.state.createdProtocolsArray[i]["PowerPlantProtocol"].dateTo).getMonth() + 1) + "-" + new Date(this.state.createdProtocolsArray[i]["PowerPlantProtocol"].dateTo).getFullYear()
			let createdprotocolType = this.state.createdProtocolsArray[i]["PowerPlantProtocol.loiContractQuantity"].listOptionItemCode
			// protocolType
			// 1 - Количество по електромер
			// 2 - Количество по график

			for (let e = 0; e < objectArray.length; e++) {
				let toBeCreatedProtocols_itn = objectArray[e]["PowerPlant"].identificationNumber
				let toBeCreatedProtocols_fromDate = new Date(objectArray[e]["dateRange"].fromDate).getDate() + "-" + (new Date(objectArray[e]["dateRange"].fromDate).getMonth() + 1) + "-" + new Date(objectArray[e]["dateRange"].fromDate).getFullYear()
				let toBeCreatedProtocols_toDate = new Date(objectArray[e]["dateRange"].toDate).getDate() + "-" + (new Date(objectArray[e]["dateRange"].toDate).getMonth() + 1) + "-" + new Date(objectArray[e]["dateRange"].toDate).getFullYear()

				if (createdProtocols_itn == toBeCreatedProtocols_itn && createdProtocols_fromDate == toBeCreatedProtocols_fromDate && createdProtocols_toDate == toBeCreatedProtocols_toDate) {
					generatedProtocols_var = generatedProtocols_var + 1

					if (createdprotocolType == 1) {
						generatedProtocolsMeasured_var = generatedProtocolsMeasured_var + 1
					} else if (createdprotocolType == 2) {
						generatedProtocolsProduced_var = generatedProtocolsProduced_var + 1
					}
					objectArray.splice(e, 1)
					break
				}
			}
		}

		for (let e = 0; e < objectArray.length; e++) {
			let protocolType = objectArray[e]["PowerPlant.loiContractQuantity"].listOptionItemCode

			nonGeneratedProtocols_var = nonGeneratedProtocols_var + 1
			if (protocolType == 1) {
				nonGeneratedProtocolsMeasured_var = nonGeneratedProtocolsMeasured_var + 1
			} else if (protocolType == 2) {
				nonGeneratedProtocolsProduced_var = nonGeneratedProtocolsProduced_var + 1
			}
		}

		this.setState({
			objectList: objectArray,
			generatedProtocols: generatedProtocols_var,
			generatedProtocolsProduced: generatedProtocolsProduced_var,
			generatedProtocolsMeasured: generatedProtocolsMeasured_var,
			nonGeneratedProtocols: nonGeneratedProtocols_var,
			nonGeneratedProtocolsProduced: nonGeneratedProtocolsProduced_var,
			nonGeneratedProtocolsMeasured: nonGeneratedProtocolsMeasured_var,
			allToBeCreated: (nonGeneratedProtocols_var + generatedProtocols_var),
			isTabDisabled: false
		}, () => {
			console.log("defineStatisticsForProtocols() -> this.state.objectList: ", this.state.objectList)
			console.log("defineStatisticsForProtocols() -> this.state.generatedProtocols: ", this.state.generatedProtocols)
			console.log("defineStatisticsForProtocols() -> this.state.generatedProtocolsProduced: ", this.state.generatedProtocolsProduced)
			console.log("defineStatisticsForProtocols() -> this.state.generatedProtocolsMeasured: ", this.state.generatedProtocolsMeasured)
			console.log("defineStatisticsForProtocols() -> this.state.nonGeneratedProtocols: ", this.state.nonGeneratedProtocols)
			console.log("defineStatisticsForProtocols() -> this.state.nonGeneratedProtocolsProduced: ", this.state.nonGeneratedProtocolsProduced)
			console.log("defineStatisticsForProtocols() -> this.state.nonGeneratedProtocolsMeasured: ", this.state.nonGeneratedProtocolsMeasured)
		})
	}

	// -------------------------------------------------------------------------------------------------------------------------------

	// ------------------------------------------------------------ NEW FUNCTIONS ----------------------------------------------------
	
	createProtocolDetailData() {

		this.setState({ getObjectList: true, getMeterList: true, getProductionList: true, getPriceList: true, calculating: true })

		this.getNotifications()

		for (var i = 0; i <= this.state.objectList.length; i++) {

			if (this.state.objectList[i] != undefined) {

				let fromDate = this.state.objectList[i]['dateRange'].fromDate
				let toDate = this.state.objectList[i]['dateRange'].toDate

				let itn = this.state.objectList[i]['PowerPlant'].identificationNumber
				//console.log("createDetailTableData itn: ", itn)

				let contractStatusCode = this.state.objectList[i]['PowerPlant.contractStatus'].listOptionItemCode
				let contractEndDate = this.state.objectList[i]['PowerPlant'].term

				let protocolTypeCode = this.state.objectList[i]['PowerPlant.loiContractQuantity'].listOptionItemCode
				let protocolRowsCode = this.state.objectList[i]['PowerPlant.loiProtocolLineCount'].listOptionItemCode

				let protocolPriceTypeCode = this.state.objectList[i]['PowerPlant.loiContractPrice'].listOptionItemCode
				let protocolFixedPriceValue = this.state.objectList[i]['PowerPlant'].value

				let protocolTaxTypeCode = this.state.objectList[i]['PowerPlant.loiContractFee'].listOptionItemCode
				let protocolTaxValue = this.state.objectList[i]['PowerPlant'].valueSec

				let protocolMinPriceMWh = this.state.objectList[i]['PowerPlant'].minPriceMWh

				if (this.validateMethodology(contractStatusCode, contractEndDate, protocolTypeCode, protocolRowsCode, protocolPriceTypeCode, protocolFixedPriceValue, protocolTaxTypeCode, protocolTaxValue, itn)) {
					this.constructDetailData(protocolTypeCode, protocolPriceTypeCode, protocolFixedPriceValue, protocolTaxValue, protocolTaxTypeCode, itn, fromDate, toDate)
				}
			}
		}
		console.log('createProtocolDetailData -> detailTableData: ', detailTableData)
		this.setState({ getObjectList: true, getMeterList: true, getProductionList: true, getPriceList: true }, () => {
			this.createProtocols()
		})
	}

	constructDetailData(protocolTypeCode, protocolPriceTypeCode, protocolFixedPriceValue, protocolTaxValue, protocolTaxTypeCode, itn, fromDate, toDate) {

		fromDate = moment.utc(fromDate)
		toDate = moment.utc(toDate).add(1, 'day')

		if (protocolTypeCode == 1) {
			//protocolTypeCode 1: Количество по електромер
			//protocolTypeCode 2: Количество по график

			if (protocolPriceTypeCode == 1) {
				//protocolPriceTypeCode 1: Фиксирана цена
				//protocolPriceTypeCode 2: Цена IBEX

				for (var i = 0; i < detailEpData.length; i++) {
					// console.log("fromDate: ", fromDate)
					// console.log("new Date(detailEpData[i].startts): ", new Date(detailEpData[i].startts))
					if (detailEpData[i].identification_number == itn && moment.utc(detailEpData[i].startts) >= fromDate && moment.utc(detailEpData[i].startts) < toDate) {

						let hour = new Date(detailEpData[i].endts).getUTCHours() == 0 ? 24 : new Date(detailEpData[i].endts).getUTCHours()//new Date(detailEpData[i].endts).getHours();//new Date(detailEpData[i].endts).getHours() == 0 ? 24 : new Date(detailEpData[i].endts).getHours()
						// console.log("detailEpData[i].endts: ", detailEpData[i].endts)
						// console.log("index: ", i, " / hour: ", hour)

						let date = this.returnDate(detailEpData[i].endts, hour)//new Date(detailEpData[i].endts).getFullYear() + '-' + (new Date(detailEpData[i].endts).getMonth() + 1) + '-' + new Date(detailEpData[i].endts).getDate();//this.returnDate(detailEpData[i].endts, hour) //producedEnergyArray[i]["PowerPlantProducedSchedule"].endTS.substring(0, 10)
						//console.log("date ", date)

						let producedQty_MWh = detailEpData[i].prod_quantity_kwh
						//console.log("producedQty_MWh ", producedQty_MWh)

						let measuredQty_MWh = detailEpData[i].met_quantity_kwh
						//console.log("measuredQty_MWh ", measuredQty_MWh)

						let protocolExpense = this.getExpense(protocolFixedPriceValue, measuredQty_MWh, protocolTaxValue, protocolTaxTypeCode, protocolTypeCode)
						//console.log("protocolExpense", protocolExpense)

						let detailDataTableItem = {
							itn: itn,
							date: date,
							hour: hour,
							qty_MWh_produced: producedQty_MWh,
							qty_MWh_measured: measuredQty_MWh,
							price: protocolFixedPriceValue,
							expense: protocolExpense
						}
						detailTableData = [...detailTableData, detailDataTableItem];
					}
				}

			} else if (protocolPriceTypeCode == 2) {

				for (var i = 0; i < detailEpData.length; i++) {
					if (detailEpData[i].identification_number == itn && moment.utc(detailEpData[i].startts) >= fromDate && moment.utc(detailEpData[i].startts) < toDate) {

						let hour = new Date(detailEpData[i].endts).getUTCHours() == 0 ? 24 : new Date(detailEpData[i].endts).getUTCHours()//new Date(detailEpData[i].endts).getHours();//new Date(detailEpData[i].endts).getHours() == 0 ? 24 : new Date(detailEpData[i].endts).getHours()
						// console.log("detailEpData[i].endts: ", detailEpData[i].endts)
						// console.log("index: ", i, " / hour: ", hour)

						let date = this.returnDate(detailEpData[i].endts, hour)//new Date(detailEpData[i].endts).getFullYear() + '-' + (new Date(detailEpData[i].endts).getMonth() + 1) + '-' + new Date(detailEpData[i].endts).getDate() //this.returnDate(detailEpData[i].endts, hour) //producedEnergyArray[i]["PowerPlantProducedSchedule"].endTS.substring(0, 10)
						//console.log("date ", date)

						let producedQty_MWh = detailEpData[i].prod_quantity_kwh
						//console.log("producedQty_MWh ", producedQty_MWh)

						let measuredQty_MWh = detailEpData[i].met_quantity_kwh
						//console.log("measuredQty_MWh ", measuredQty_MWh)

						let protocolPrice = detailEpData[i].pricebgn
						// 		//console.log("protocolPrice ", protocolPrice)

						let protocolExpense = this.getExpense(protocolPrice, measuredQty_MWh, protocolTaxValue, protocolTaxTypeCode, protocolTypeCode)
						//console.log("protocolExpense", protocolExpense)

						let detailDataTableItem = {
							itn: itn,
							date: date,
							hour: hour,
							qty_MWh_produced: producedQty_MWh,
							qty_MWh_measured: measuredQty_MWh,
							price: protocolPrice,
							expense: protocolExpense
						}
						detailTableData = [...detailTableData, detailDataTableItem];
					}

				}
			}

		} else if (protocolTypeCode == 2) {

			if (protocolPriceTypeCode == 1) {
				//protocolPriceTypeCode 1: Фиксирана цена
				//protocolPriceTypeCode 2: Цена IBEX

				for (var i = 0; i < detailEpData.length; i++) {
					if (detailEpData[i].identification_number == itn && moment.utc(detailEpData[i].startts) >= fromDate && moment.utc(detailEpData[i].startts) < toDate) {

						let hour = new Date(detailEpData[i].endts).getUTCHours() == 0 ? 24 : new Date(detailEpData[i].endts).getUTCHours()//new Date(detailEpData[i].endts).getHours();//new Date(detailEpData[i].endts).getHours() == 0 ? 24 : new Date(detailEpData[i].endts).getHours()
						// console.log("detailEpData[i].endts: ", detailEpData[i].endts)
						// console.log("index: ", i, " / hour: ", hour)

						let date = this.returnDate(detailEpData[i].endts, hour)//new Date(detailEpData[i].endts).getFullYear() + '-' + (new Date(detailEpData[i].endts).getMonth() + 1) + '-' + new Date(detailEpData[i].endts).getDate()//this.returnDate(detailEpData[i].endts, hour) //producedEnergyArray[i]["PowerPlantProducedSchedule"].endTS.substring(0, 10)
						//console.log("date ", date)

						let producedQty_MWh = detailEpData[i].prod_quantity_kwh
						//console.log("producedQty_MWh ", producedQty_MWh)

						let protocolExpense = this.getExpense(protocolFixedPriceValue, producedQty_MWh, protocolTaxValue, protocolTaxTypeCode, protocolTypeCode)
						//console.log("protocolExpense", protocolExpense)

						let detailDataTableItem = {
							itn: itn,
							date: date,
							hour: hour,
							qty_MWh_produced: producedQty_MWh,
							qty_MWh_measured: 0,
							price: protocolFixedPriceValue,
							expense: protocolExpense
						}
						detailTableData = [...detailTableData, detailDataTableItem];
					}
				}

			} else if (protocolPriceTypeCode == 2) {

				for (var i = 0; i < detailEpData.length; i++) {
					if (detailEpData[i].identification_number == itn && moment.utc(detailEpData[i].startts) >= fromDate && moment.utc(detailEpData[i].startts) < toDate) {

						let hour = new Date(detailEpData[i].endts).getUTCHours() == 0 ? 24 : new Date(detailEpData[i].endts).getUTCHours()//new Date(detailEpData[i].endts).getHours();//new Date(detailEpData[i].endts).getHours() == 0 ? 24 : new Date(detailEpData[i].endts).getHours()
						// console.log("detailEpData[i].endts: ", detailEpData[i].endts)
						// console.log("index: ", i, " / hour: ", hour)

						let date = this.returnDate(detailEpData[i].endts, hour)//new Date(detailEpData[i].endts).getFullYear() + '-' + (new Date(detailEpData[i].endts).getMonth() + 1) + '-' + new Date(detailEpData[i].endts).getDate();//this.returnDate(detailEpData[i].endts, hour) //producedEnergyArray[i]["PowerPlantProducedSchedule"].endTS.substring(0, 10)
						//console.log("date ", date)

						let producedQty_MWh = detailEpData[i].prod_quantity_kwh
						//console.log("producedQty_MWh ", producedQty_MWh)

						let protocolPrice = detailEpData[i].pricebgn
						// 		//console.log("protocolPrice ", protocolPrice)

						let protocolExpense = this.getExpense(protocolPrice, producedQty_MWh, protocolTaxValue, protocolTaxTypeCode, protocolTypeCode)
						//console.log("protocolExpense", protocolExpense)

						let detailDataTableItem = {
							itn: itn,
							date: date,
							hour: hour,
							qty_MWh_produced: producedQty_MWh,
							qty_MWh_measured: 0,
							price: protocolPrice,
							expense: protocolExpense
						}
						// console.log("Item to add: ", detailDataTableItem)
						// console.log("index: ", i)
						detailTableData = [...detailTableData, detailDataTableItem];
					}
				}
			}
		}

		//console.log("constructDetailData() -> detailTableData: ", detailTableData)
	}

	async createProtocols() {
		//console.log("Start createProtocols", Date.now());
		//console.log("createProtocols() => detailTableData : ", detailTableData)
		this.setState({ getObjectList: false, getMeterList: false, getProductionList: false, getPriceList: false })

		let newList = []

		for (var i = 0; i <= this.state.objectList.length; i++) {

			if (this.state.isDropdownVisible == true && this.state.dropdownSelectedItn != "") {

				if (this.state.objectList[i] != undefined && this.state.objectList[i]['PowerPlant'].identificationNumber == this.state.dropdownSelectedItn) {

					// let fromDateRange = this.state.objectList[i]['dateRange'].fromDate
					// let toDateRange = this.state.objectList[i]['dateRange'].toDate
					let fromDateRange = new Date(this.state.fromDate)
					let toDateRange = new Date(this.state.toDate)
					toDateRange.setDate(toDateRange.getDate() + 1)

					let fromDate = this.state.objectList[i]['dateRange'].fromDate
					let toDate = this.state.objectList[i]['dateRange'].toDate

					let itn = this.state.objectList[i]['PowerPlant'].identificationNumber
					let name = this.state.objectList[i]['PowerPlant'].name
					let protocolTypeCode = this.state.objectList[i]['PowerPlant.loiContractQuantity'].listOptionItemCode
					let protocolRowsCode = this.state.objectList[i]['PowerPlant.loiProtocolLineCount'].listOptionItemCode
					let protocolPriceTypeCode = this.state.objectList[i]['PowerPlant.loiContractPrice'].listOptionItemCode
					let protocolFixedPriceValue = this.state.objectList[i]['PowerPlant'].value
					let protocolTaxTypeCode = this.state.objectList[i]['PowerPlant.loiContractFee'].listOptionItemCode
					let protocolTaxValue = this.state.objectList[i]['PowerPlant'].valueSec
					let protocolMinPriceMWh = this.state.objectList[i]['PowerPlant'].minPriceMWh
					let numberOfRows = this.state.objectList[i]["PowerPlant.loiProtocolLineCount"].listOptionItemCode == 1 ? 1 : 3

					let owner = this.state.objectList[i]["PowerPlant.owner"]
					let powerPlant = this.state.objectList[i]["PowerPlant"]
					let loiContractQuantity = this.state.objectList[i]["PowerPlant.loiContractQuantity"]
					let loiContractPrice = this.state.objectList[i]["PowerPlant.loiContractPrice"]
					let loiProtocolLineCount = this.state.objectList[i]["PowerPlant.loiProtocolLineCount"]
					let loiProtocolStatus = await getLoiByCode("loiProtocolStatuses", 2, this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)

					let dataTableItem = {
						//common protocol details
						fromDate: fromDate, //this.state.fromDate,
						toDate: toDate, //this.state.toDate,
						protocolCreationDate: this.getProtocolCreationDate(toDate),
						itn: itn,
						name: name,
						pricePerMwh: 0,
						//one row protocol details
						energyForInvoice: 0,
						priceWithoutVat: 0,
						//three row protocol pdf detalis
						scheduledEnergy: 0,
						scheduledPriceWithoutVat: 0,
						balancedEnergy: 0,
						balancedPriceWithoutVat: 0,
						totalEnergyForInvoice: 0,
						totalPriceWithoutVat: 0,
						//three row protocol excel detalis
						contractMwhQty: 0,
						contractExpense: 0,
						contractExpensePerMwh: 0,
						contractIncome: 0,
						contractIncomePerMwh: 0,
						checkingQuantity_1: 0,
						checkingQuantity_2: 0,
						differenceQuantity: 0,
						checkingIncome_1: 0,
						checkingIncome_2: 0,
						differenceIncome: 0
					}

					//console.log("async createProtocols() => dataTableItem: ", dataTableItem)

					if (protocolTypeCode == 1) {
						//protocolTypeCode 1: Количество по електромер
						//protocolTypeCode 2: Количество по график

						if (protocolTaxTypeCode == 1) {
							//protocolTaxTypeCode 1: Такса/процент
							//protocolTaxTypeCode 2: Фиксирана такса
							//protocolTaxTypeCode 3: Няма такса

							for (var e = 0; e < detailTableData.length; e++) {
								if (detailTableData[e].itn == itn && new Date(detailTableData[e].date) >= fromDateRange && new Date(detailTableData[e].date) <= toDateRange) {

									dataTableItem.contractMwhQty = dataTableItem.contractMwhQty + detailTableData[e].qty_MWh_measured
									dataTableItem.contractExpense = dataTableItem.contractExpense + detailTableData[e].expense
									dataTableItem.contractIncome = dataTableItem.contractIncome + (detailTableData[e].qty_MWh_measured * detailTableData[e].price)
									dataTableItem.scheduledEnergy = dataTableItem.scheduledEnergy + detailTableData[e].qty_MWh_produced
								}
							}

							let contractMwhQty = dataTableItem.contractMwhQty / 1000 //this.round(((dataTableItem.contractMwhQty / 1000) * 100) / 100, 4)
							let contractExpense = dataTableItem.contractExpense >= (contractMwhQty * protocolMinPriceMWh) ? dataTableItem.contractExpense : (contractMwhQty * protocolMinPriceMWh) //dataTableItem.contractExpense >= (contractMwhQty * protocolMinPriceMWh) ? this.round(((dataTableItem.contractExpense) * 100) / 100, 2) : this.round(((contractMwhQty * protocolMinPriceMWh) * 100) / 100, 2)
							let contractExpensePerMwh = dataTableItem.contractExpense / dataTableItem.contractMwhQty //this.round(((dataTableItem.contractExpense / dataTableItem.contractMwhQty) * 100) / 100, 2)
							let contractIncome = (dataTableItem.contractIncome / 1000) - contractExpense //this.round((((dataTableItem.contractIncome / 1000) - contractExpense) * 100) / 100, 2)
							let contractIncomePerMwh = contractIncome / contractMwhQty //this.round(((contractIncome / contractMwhQty) * 100) / 100, 2)
							let scheduledEnergy = dataTableItem.scheduledEnergy / 1000 //this.round(((dataTableItem.scheduledEnergy / 1000) * 100) / 100, 4)
							let pricePerMwh = contractIncomePerMwh //this.round(((contractIncomePerMwh) * 100) / 100, 2)
							let scheduledPriceWithoutVat = scheduledEnergy * pricePerMwh //this.round(((scheduledEnergy * pricePerMwh) * 100) / 100, 2)
							let balancedEnergy = contractMwhQty - scheduledEnergy //this.round(((contractMwhQty - scheduledEnergy) * 100) / 100, 4)
							let balancedPriceWithoutVat = balancedEnergy * pricePerMwh //this.round(((balancedEnergy * pricePerMwh) * 100) / 100, 2)
							let totalEnergyForInvoice = scheduledEnergy + balancedEnergy //this.round(((scheduledEnergy + balancedEnergy) * 100) / 100, 2)
							let totalPriceWithoutVat = scheduledPriceWithoutVat + balancedPriceWithoutVat //this.round(((scheduledPriceWithoutVat + balancedPriceWithoutVat) * 100) / 100, 2)
							let checkingQuantity_1 = contractMwhQty //this.round(((contractMwhQty) * 100) / 100, 4)
							let checkingQuantity_2 = scheduledEnergy + balancedEnergy //this.round(((scheduledEnergy + balancedEnergy) * 100) / 100, 4)
							let differenceQuantity = checkingQuantity_1 - checkingQuantity_2 //this.round(((checkingQuantity_1 - checkingQuantity_2) * 100) / 100, 4)
							let checkingIncome_1 = contractIncome //this.round(((contractIncome) * 100) / 100, 2)
							let checkingIncome_2 = scheduledPriceWithoutVat + balancedPriceWithoutVat //this.round(((scheduledPriceWithoutVat + balancedPriceWithoutVat) * 100) / 100, 2)
							let differenceIncome = checkingIncome_1 - checkingIncome_2 //this.round(((checkingIncome_1 - checkingIncome_2) * 100) / 100, 2)

							dataTableItem.contractMwhQty = this.formatNumbers(contractMwhQty.toFixed(4))
							dataTableItem.contractExpense = this.formatNumbers(contractExpense.toFixed(2))
							dataTableItem.contractExpensePerMwh = this.formatNumbers(contractExpensePerMwh.toFixed(2))
							dataTableItem.contractIncome = this.formatNumbers(contractIncome.toFixed(2))
							dataTableItem.contractIncomePerMwh = this.formatNumbers(contractIncomePerMwh.toFixed(2))
							dataTableItem.scheduledEnergy = this.formatNumbers(scheduledEnergy.toFixed(4))
							dataTableItem.pricePerMwh = this.formatNumbers(pricePerMwh.toFixed(2))
							dataTableItem.scheduledPriceWithoutVat = this.formatNumbers(scheduledPriceWithoutVat.toFixed(2))
							dataTableItem.balancedEnergy = this.formatNumbers(balancedEnergy.toFixed(4))
							dataTableItem.balancedPriceWithoutVat = this.formatNumbers(balancedPriceWithoutVat.toFixed(2))
							dataTableItem.totalEnergyForInvoice = this.formatNumbers(totalEnergyForInvoice.toFixed(4))
							dataTableItem.totalPriceWithoutVat = this.formatNumbers(totalPriceWithoutVat.toFixed(2))
							dataTableItem.checkingQuantity_1 = this.formatNumbers(checkingQuantity_1.toFixed(4))
							dataTableItem.checkingQuantity_2 = this.formatNumbers(checkingQuantity_2.toFixed(4))
							dataTableItem.differenceQuantity = this.formatNumbers(differenceQuantity.toFixed(4))
							dataTableItem.checkingIncome_1 = this.formatNumbers(checkingIncome_1.toFixed(2))
							dataTableItem.checkingIncome_2 = this.formatNumbers(checkingIncome_2.toFixed(2))
							dataTableItem.differenceIncome = this.formatNumbers(differenceIncome.toFixed(2))

						} else {
							for (var e = 0; e < detailTableData.length; e++) {
								if (detailTableData[e].itn == itn && new Date(detailTableData[e].date) >= fromDateRange && new Date(detailTableData[e].date) <= toDateRange) {

									dataTableItem.energyForInvoice = dataTableItem.energyForInvoice + detailTableData[e].qty_MWh_measured
									dataTableItem.priceWithoutVat = dataTableItem.priceWithoutVat + (detailTableData[e].qty_MWh_measured * detailTableData[e].price)
									dataTableItem.pricePerMwh = 0.0

								}
							}

							let currentTax = protocolTaxValue == 0 ? 1 : protocolTaxValue

							let energyForInvoice = dataTableItem.energyForInvoice / 1000 //this.round(((dataTableItem.energyForInvoice / 1000) * 100) / 100, 4)
							let priceWithoutVat = (dataTableItem.priceWithoutVat / 1000) - (energyForInvoice * currentTax)  //this.round(((dataTableItem.priceWithoutVat / 1000 - (energyForInvoice * currentTax)) * 100) / 100, 2)
							let pricePerMwh = priceWithoutVat / energyForInvoice //this.round(((priceWithoutVat / energyForInvoice) * 100) / 100, 2)

							dataTableItem.energyForInvoice = this.formatNumbers(energyForInvoice.toFixed(4))
							dataTableItem.priceWithoutVat = this.formatNumbers(priceWithoutVat.toFixed(2))
							dataTableItem.pricePerMwh = this.formatNumbers(pricePerMwh.toFixed(2))
						}

					} else if (protocolTypeCode == 2) {

						if (protocolTaxTypeCode == 1) {
							//protocolTaxTypeCode 1: Такса/процент
							//protocolTaxTypeCode 2: Фиксирана такса
							//protocolTaxTypeCode 3: Няма такса
							for (var e = 0; e < detailTableData.length; e++) {
								if (detailTableData[e].itn == itn && new Date(detailTableData[e].date) >= fromDateRange && new Date(detailTableData[e].date) <= toDateRange) {

									dataTableItem.energyForInvoice = dataTableItem.energyForInvoice + detailTableData[e].qty_MWh_produced
									dataTableItem.priceWithoutVat = dataTableItem.priceWithoutVat + (detailTableData[e].qty_MWh_produced * detailTableData[e].expense)
									dataTableItem.pricePerMwh = 0.0

								}
							}

							let energyForInvoice = dataTableItem.energyForInvoice / 1000 //this.round(((dataTableItem.energyForInvoice / 1000) * 100) / 100, 4)
							let priceWithoutVat = dataTableItem.priceWithoutVat / 1000 //this.round(((dataTableItem.priceWithoutVat / 1000) * 100) / 100, 2)
							let pricePerMwh = priceWithoutVat / energyForInvoice //this.round(((priceWithoutVat / energyForInvoice) * 100) / 100, 2)

							dataTableItem.energyForInvoice = this.formatNumbers(energyForInvoice.toFixed(4))
							dataTableItem.priceWithoutVat = this.formatNumbers(priceWithoutVat.toFixed(2))
							dataTableItem.pricePerMwh = this.formatNumbers(pricePerMwh.toFixed(2))
						} else {

							for (var e = 0; e < detailTableData.length; e++) {
								if (detailTableData[e].itn == itn && new Date(detailTableData[e].date) >= fromDateRange && new Date(detailTableData[e].date) <= toDateRange) {

									dataTableItem.energyForInvoice = dataTableItem.energyForInvoice + detailTableData[e].qty_MWh_produced
									dataTableItem.priceWithoutVat = dataTableItem.priceWithoutVat + (detailTableData[e].qty_MWh_produced * detailTableData[e].price)
									dataTableItem.pricePerMwh = 0.0

								}
							}

							let currentTax = protocolTaxValue == 0 ? 1 : protocolTaxValue

							let energyForInvoice = dataTableItem.energyForInvoice / 1000 //this.round(((dataTableItem.energyForInvoice / 1000) * 100) / 100, 4)
							let priceWithoutVat = (dataTableItem.priceWithoutVat / 1000) - (energyForInvoice * currentTax) //this.round(((dataTableItem.priceWithoutVat / 1000 - (energyForInvoice * currentTax)) * 100) / 100, 2)
							let pricePerMwh = priceWithoutVat / energyForInvoice //this.round(((priceWithoutVat / energyForInvoice) * 100) / 100, 2)

							dataTableItem.energyForInvoice = this.formatNumbers(energyForInvoice.toFixed(4))
							dataTableItem.priceWithoutVat = this.formatNumbers(priceWithoutVat.toFixed(2))
							dataTableItem.pricePerMwh = this.formatNumbers(pricePerMwh.toFixed(2))

						}
					}

					//console.log("async createProtocols() => dataTableItem: ", dataTableItem)
					const pdfBase64 = await getPdfBase64(dataTableItem, numberOfRows)
					const xlsBase64 = this.getExcelBase64(dataTableItem, protocolMinPriceMWh, protocolTaxValue, protocolTypeCode, protocolTaxTypeCode, fromDateRange, toDateRange);

					let newItem = {}
					newItem._editable = true;
					newItem._getPdfFile = this.getPdfFile
					newItem.pdfFile = pdfBase64
					newItem.xlsxFile = xlsBase64;
					newItem.dateFrom = moment(new Date(fromDate)).format("YYYY-MM-DDTHH:mm:ss")//this.returnStateDate(fromDate, false)//new Date(fromDate)//this.state.fromDate
					newItem.dateTo = moment(new Date(toDate)).format("YYYY-MM-DDTHH:mm:ss")//new Date(toDate)//this.state.toDate
					newItem.owner = owner

					newItem._links = {
						...newItem._links,
						owner: { href: owner._links.self.href },
					};

					newItem.powerPlant = powerPlant
					newItem._links = {
						...newItem._links,
						powerPlant: { href: powerPlant._links.self.href },
					};

					newItem.loiContractQuantity = loiContractQuantity;
					newItem._links = {
						...newItem._links,
						loiContractQuantity: { href: loiContractQuantity._links.self.href },
					};

					newItem.loiContractPrice = loiContractPrice;
					newItem._links = {
						...newItem._links,
						loiContractPrice: { href: loiContractPrice._links.self.href },
					};

					newItem.loiProtocolLineCount = loiProtocolLineCount;
					newItem._links = {
						...newItem._links,
						loiProtocolLineCount: { href: loiProtocolLineCount._links.self.href },
					};

					newItem.loiProtocolStatus = loiProtocolStatus
					newItem._links = {
						...newItem._links,
						loiProtocolStatus: { href: newItem.loiProtocolStatus._links.self.href },
					};

					newItem.sendMailMessage = null

					newList.push(newItem)
				}

			} else if (this.state.isDropdownVisible == false && this.state.dropdownSelectedItn == "") {

				if (this.state.objectList[i] != undefined) {

					let fromDateRange = this.state.objectList[i]['dateRange'].fromDate
					let toDateRange = this.state.objectList[i]['dateRange'].toDate
					fromDateRange = new Date(fromDateRange)
					toDateRange = new Date(toDateRange)
					toDateRange.setDate(toDateRange.getDate() + 1)

					let fromDate = this.state.objectList[i]['dateRange'].fromDate
					let toDate = this.state.objectList[i]['dateRange'].toDate

					let itn = this.state.objectList[i]['PowerPlant'].identificationNumber
					let name = this.state.objectList[i]['PowerPlant'].name
					let protocolTypeCode = this.state.objectList[i]['PowerPlant.loiContractQuantity'].listOptionItemCode
					let protocolRowsCode = this.state.objectList[i]['PowerPlant.loiProtocolLineCount'].listOptionItemCode
					let protocolPriceTypeCode = this.state.objectList[i]['PowerPlant.loiContractPrice'].listOptionItemCode
					let protocolFixedPriceValue = this.state.objectList[i]['PowerPlant'].value
					let protocolTaxTypeCode = this.state.objectList[i]['PowerPlant.loiContractFee'].listOptionItemCode
					let protocolTaxValue = this.state.objectList[i]['PowerPlant'].valueSec
					let protocolMinPriceMWh = this.state.objectList[i]['PowerPlant'].minPriceMWh
					let numberOfRows = this.state.objectList[i]["PowerPlant.loiProtocolLineCount"].listOptionItemCode == 1 ? 1 : 3

					let owner = this.state.objectList[i]["PowerPlant.owner"]
					let powerPlant = this.state.objectList[i]["PowerPlant"]
					let loiContractQuantity = this.state.objectList[i]["PowerPlant.loiContractQuantity"]
					let loiContractPrice = this.state.objectList[i]["PowerPlant.loiContractPrice"]
					let loiProtocolLineCount = this.state.objectList[i]["PowerPlant.loiProtocolLineCount"]
					let loiProtocolStatus = await getLoiByCode("loiProtocolStatuses", 2, this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)

					let dataTableItem = {
						//common protocol details
						fromDate: fromDate, //this.state.fromDate,
						toDate: toDate, //this.state.toDate,
						protocolCreationDate: this.getProtocolCreationDate(toDate),
						itn: itn,
						name: name,
						pricePerMwh: 0,
						//one row protocol details
						energyForInvoice: 0,
						priceWithoutVat: 0,
						//three row protocol pdf detalis
						scheduledEnergy: 0,
						scheduledPriceWithoutVat: 0,
						balancedEnergy: 0,
						balancedPriceWithoutVat: 0,
						totalEnergyForInvoice: 0,
						totalPriceWithoutVat: 0,
						//three row protocol excel detalis
						contractMwhQty: 0,
						contractExpense: 0,
						contractExpensePerMwh: 0,
						contractIncome: 0,
						contractIncomePerMwh: 0,
						checkingQuantity_1: 0,
						checkingQuantity_2: 0,
						differenceQuantity: 0,
						checkingIncome_1: 0,
						checkingIncome_2: 0,
						differenceIncome: 0
					}

					//console.log("async createProtocols() => dataTableItem: ", dataTableItem)

					if (protocolTypeCode == 1) {
						//protocolTypeCode 1: Количество по електромер
						//protocolTypeCode 2: Количество по график

						if (protocolTaxTypeCode == 1) {
							//protocolTaxTypeCode 1: Такса/процент
							//protocolTaxTypeCode 2: Фиксирана такса
							//protocolTaxTypeCode 3: Няма такса

							for (var e = 0; e < detailTableData.length; e++) {
								if (detailTableData[e].itn == itn && new Date(detailTableData[e].date) >= fromDateRange && new Date(detailTableData[e].date) <= toDateRange) {

									dataTableItem.contractMwhQty = dataTableItem.contractMwhQty + detailTableData[e].qty_MWh_measured
									dataTableItem.contractExpense = dataTableItem.contractExpense + detailTableData[e].expense
									dataTableItem.contractIncome = dataTableItem.contractIncome + (detailTableData[e].qty_MWh_measured * detailTableData[e].price)
									dataTableItem.scheduledEnergy = dataTableItem.scheduledEnergy + detailTableData[e].qty_MWh_produced
								}
							}

							let contractMwhQty = dataTableItem.contractMwhQty / 1000 //this.round(((dataTableItem.contractMwhQty / 1000) * 100) / 100, 4)
							let contractExpense = dataTableItem.contractExpense >= (contractMwhQty * protocolMinPriceMWh) ? dataTableItem.contractExpense : (contractMwhQty * protocolMinPriceMWh) //dataTableItem.contractExpense >= (contractMwhQty * protocolMinPriceMWh) ? this.round(((dataTableItem.contractExpense) * 100) / 100, 2) : this.round(((contractMwhQty * protocolMinPriceMWh) * 100) / 100, 2)
							let contractExpensePerMwh = dataTableItem.contractExpense / dataTableItem.contractMwhQty //this.round(((dataTableItem.contractExpense / dataTableItem.contractMwhQty) * 100) / 100, 2)
							let contractIncome = (dataTableItem.contractIncome / 1000) - contractExpense //this.round((((dataTableItem.contractIncome / 1000) - contractExpense) * 100) / 100, 2)
							let contractIncomePerMwh = contractIncome / contractMwhQty //this.round(((contractIncome / contractMwhQty) * 100) / 100, 2)
							let scheduledEnergy = dataTableItem.scheduledEnergy / 1000 //this.round(((dataTableItem.scheduledEnergy / 1000) * 100) / 100, 4)
							let pricePerMwh = contractIncomePerMwh //this.round(((contractIncomePerMwh) * 100) / 100, 2)
							let scheduledPriceWithoutVat = scheduledEnergy * pricePerMwh //this.round(((scheduledEnergy * pricePerMwh) * 100) / 100, 2)
							let balancedEnergy = contractMwhQty - scheduledEnergy //this.round(((contractMwhQty - scheduledEnergy) * 100) / 100, 4)
							let balancedPriceWithoutVat = balancedEnergy * pricePerMwh //this.round(((balancedEnergy * pricePerMwh) * 100) / 100, 2)
							let totalEnergyForInvoice = scheduledEnergy + balancedEnergy //this.round(((scheduledEnergy + balancedEnergy) * 100) / 100, 2)
							let totalPriceWithoutVat = scheduledPriceWithoutVat + balancedPriceWithoutVat //this.round(((scheduledPriceWithoutVat + balancedPriceWithoutVat) * 100) / 100, 2)
							let checkingQuantity_1 = contractMwhQty //this.round(((contractMwhQty) * 100) / 100, 4)
							let checkingQuantity_2 = scheduledEnergy + balancedEnergy //this.round(((scheduledEnergy + balancedEnergy) * 100) / 100, 4)
							let differenceQuantity = checkingQuantity_1 - checkingQuantity_2 //this.round(((checkingQuantity_1 - checkingQuantity_2) * 100) / 100, 4)
							let checkingIncome_1 = contractIncome //this.round(((contractIncome) * 100) / 100, 2)
							let checkingIncome_2 = scheduledPriceWithoutVat + balancedPriceWithoutVat //this.round(((scheduledPriceWithoutVat + balancedPriceWithoutVat) * 100) / 100, 2)
							let differenceIncome = checkingIncome_1 - checkingIncome_2 //this.round(((checkingIncome_1 - checkingIncome_2) * 100) / 100, 2)

							dataTableItem.contractMwhQty = this.formatNumbers(contractMwhQty.toFixed(4))
							dataTableItem.contractExpense = this.formatNumbers(contractExpense.toFixed(2))
							dataTableItem.contractExpensePerMwh = this.formatNumbers(contractExpensePerMwh.toFixed(2))
							dataTableItem.contractIncome = this.formatNumbers(contractIncome.toFixed(2))
							dataTableItem.contractIncomePerMwh = this.formatNumbers(contractIncomePerMwh.toFixed(2))
							dataTableItem.scheduledEnergy = this.formatNumbers(scheduledEnergy.toFixed(4))
							dataTableItem.pricePerMwh = this.formatNumbers(pricePerMwh.toFixed(2))
							dataTableItem.scheduledPriceWithoutVat = this.formatNumbers(scheduledPriceWithoutVat.toFixed(2))
							dataTableItem.balancedEnergy = this.formatNumbers(balancedEnergy.toFixed(4))
							dataTableItem.balancedPriceWithoutVat = this.formatNumbers(balancedPriceWithoutVat.toFixed(2))
							dataTableItem.totalEnergyForInvoice = this.formatNumbers(totalEnergyForInvoice.toFixed(4))
							dataTableItem.totalPriceWithoutVat = this.formatNumbers(totalPriceWithoutVat.toFixed(2))
							dataTableItem.checkingQuantity_1 = this.formatNumbers(checkingQuantity_1.toFixed(4))
							dataTableItem.checkingQuantity_2 = this.formatNumbers(checkingQuantity_2.toFixed(4))
							dataTableItem.differenceQuantity = this.formatNumbers(differenceQuantity.toFixed(4))
							dataTableItem.checkingIncome_1 = this.formatNumbers(checkingIncome_1.toFixed(2))
							dataTableItem.checkingIncome_2 = this.formatNumbers(checkingIncome_2.toFixed(2))
							dataTableItem.differenceIncome = this.formatNumbers(differenceIncome.toFixed(2))

						} else {
							for (var e = 0; e < detailTableData.length; e++) {
								if (detailTableData[e].itn == itn && new Date(detailTableData[e].date) >= fromDateRange && new Date(detailTableData[e].date) <= toDateRange) {

									dataTableItem.energyForInvoice = dataTableItem.energyForInvoice + detailTableData[e].qty_MWh_measured
									dataTableItem.priceWithoutVat = dataTableItem.priceWithoutVat + (detailTableData[e].qty_MWh_measured * detailTableData[e].price)
									dataTableItem.pricePerMwh = 0.0

								}
							}

							let currentTax = protocolTaxValue == 0 ? 1 : protocolTaxValue

							let energyForInvoice = dataTableItem.energyForInvoice / 1000 //this.round(((dataTableItem.energyForInvoice / 1000) * 100) / 100, 4)
							let priceWithoutVat = (dataTableItem.priceWithoutVat / 1000) - (energyForInvoice * currentTax)  //this.round(((dataTableItem.priceWithoutVat / 1000 - (energyForInvoice * currentTax)) * 100) / 100, 2)
							let pricePerMwh = priceWithoutVat / energyForInvoice //this.round(((priceWithoutVat / energyForInvoice) * 100) / 100, 2)

							dataTableItem.energyForInvoice = this.formatNumbers(energyForInvoice.toFixed(4))
							dataTableItem.priceWithoutVat = this.formatNumbers(priceWithoutVat.toFixed(2))
							dataTableItem.pricePerMwh = this.formatNumbers(pricePerMwh.toFixed(2))
						}

					} else if (protocolTypeCode == 2) {

						if (protocolTaxTypeCode == 1) {
							//protocolTaxTypeCode 1: Такса/процент
							//protocolTaxTypeCode 2: Фиксирана такса
							//protocolTaxTypeCode 3: Няма такса
							for (var e = 0; e < detailTableData.length; e++) {
								if (detailTableData[e].itn == itn && new Date(detailTableData[e].date) >= fromDateRange && new Date(detailTableData[e].date) <= toDateRange) {

									dataTableItem.energyForInvoice = dataTableItem.energyForInvoice + detailTableData[e].qty_MWh_produced
									dataTableItem.priceWithoutVat = dataTableItem.priceWithoutVat + (detailTableData[e].qty_MWh_produced * detailTableData[e].expense)
									dataTableItem.pricePerMwh = 0.0

								}
							}

							let energyForInvoice = dataTableItem.energyForInvoice / 1000 //this.round(((dataTableItem.energyForInvoice / 1000) * 100) / 100, 4)
							let priceWithoutVat = dataTableItem.priceWithoutVat / 1000 //this.round(((dataTableItem.priceWithoutVat / 1000) * 100) / 100, 2)
							let pricePerMwh = priceWithoutVat / energyForInvoice //this.round(((priceWithoutVat / energyForInvoice) * 100) / 100, 2)

							dataTableItem.energyForInvoice = this.formatNumbers(energyForInvoice.toFixed(4))
							dataTableItem.priceWithoutVat = this.formatNumbers(priceWithoutVat.toFixed(2))
							dataTableItem.pricePerMwh = this.formatNumbers(pricePerMwh.toFixed(2))
						} else {

							for (var e = 0; e < detailTableData.length; e++) {
								if (detailTableData[e].itn == itn && new Date(detailTableData[e].date) >= fromDateRange && new Date(detailTableData[e].date) <= toDateRange) {

									dataTableItem.energyForInvoice = dataTableItem.energyForInvoice + detailTableData[e].qty_MWh_produced
									dataTableItem.priceWithoutVat = dataTableItem.priceWithoutVat + (detailTableData[e].qty_MWh_produced * detailTableData[e].price)
									dataTableItem.pricePerMwh = 0.0

								}
							}

							let currentTax = protocolTaxValue == 0 ? 1 : protocolTaxValue

							let energyForInvoice = dataTableItem.energyForInvoice / 1000 //this.round(((dataTableItem.energyForInvoice / 1000) * 100) / 100, 4)
							let priceWithoutVat = (dataTableItem.priceWithoutVat / 1000) - (energyForInvoice * currentTax) //this.round(((dataTableItem.priceWithoutVat / 1000 - (energyForInvoice * currentTax)) * 100) / 100, 2)
							let pricePerMwh = priceWithoutVat / energyForInvoice //this.round(((priceWithoutVat / energyForInvoice) * 100) / 100, 2)

							dataTableItem.energyForInvoice = this.formatNumbers(energyForInvoice.toFixed(4))
							dataTableItem.priceWithoutVat = this.formatNumbers(priceWithoutVat.toFixed(2))
							dataTableItem.pricePerMwh = this.formatNumbers(pricePerMwh.toFixed(2))

						}
					}

					//console.log("async createProtocols() => dataTableItem: ", dataTableItem)
					const pdfBase64 = await getPdfBase64(dataTableItem, numberOfRows)
					const xlsBase64 = this.getExcelBase64(dataTableItem, protocolMinPriceMWh, protocolTaxValue, protocolTypeCode, protocolTaxTypeCode, fromDateRange, toDateRange);

					let newItem = {}
					newItem._editable = true;
					newItem._getPdfFile = this.getPdfFile
					newItem.pdfFile = pdfBase64
					newItem.xlsxFile = xlsBase64;
					newItem.dateFrom = moment(new Date(fromDate)).format("YYYY-MM-DDTHH:mm:ss")//this.returnStateDate(fromDate, false)//new Date(fromDate)//this.state.fromDate
					newItem.dateTo = moment(new Date(toDate)).format("YYYY-MM-DDTHH:mm:ss")//new Date(toDate)//this.state.toDate
					newItem.owner = owner

					newItem._links = {
						...newItem._links,
						owner: { href: owner._links.self.href },
					};

					newItem.powerPlant = powerPlant
					newItem._links = {
						...newItem._links,
						powerPlant: { href: powerPlant._links.self.href },
					};

					newItem.loiContractQuantity = loiContractQuantity;
					newItem._links = {
						...newItem._links,
						loiContractQuantity: { href: loiContractQuantity._links.self.href },
					};

					newItem.loiContractPrice = loiContractPrice;
					newItem._links = {
						...newItem._links,
						loiContractPrice: { href: loiContractPrice._links.self.href },
					};

					newItem.loiProtocolLineCount = loiProtocolLineCount;
					newItem._links = {
						...newItem._links,
						loiProtocolLineCount: { href: loiProtocolLineCount._links.self.href },
					};

					newItem.loiProtocolStatus = loiProtocolStatus
					newItem._links = {
						...newItem._links,
						loiProtocolStatus: { href: newItem.loiProtocolStatus._links.self.href },
					};

					newItem.sendMailMessage = null

					newList.push(newItem)
					this.setState({ createdProtocols: ( i + 1 )})
				}
			}
		}

		this.setState({ calculating: false })
		this.addData(newList)
	}

	async downloadPdfZip() {
		//get all PDFs that are generated status
		var protocols = [];
		for (var i = 0; i < this.props.data.length; i++) {
			const protocol = this.props.data[i];
			if (protocol.pdfFile && protocol.loiProtocolStatus && protocol.loiProtocolStatus.listOptionItemCode == 2) {
				//convert from base64 to blob
				const contentType = "application/octet-stream";
				const base64Response = await fetch("data:" + contentType + ";base64," + protocol.pdfFile);
				const blob = await base64Response.blob();
				protocols.push({ rowIndex: i, id: protocol.id, fileName: protocol.id + '_Протокол_' + protocol.powerPlant.identificationNumber + '.pdf', blob: blob });
			}
		};
		//zip them and download file
		downloadPdfZip(protocols);
		//change status to downloaded
		var loiProtocolStatus = await getLoiByCode("loiProtocolStatuses", 3, this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData);
		//use patch to avoid transfer of all files
		let patches = [];
		protocols.forEach((protocol) => {
			patches.push({
				restConfig: {
					method: 'patch',
					url: API_URL + "/" + this.props.entityName + "/" + protocol.id,
					data: [{ "op": "add", "path": "/loiProtocolStatus", "value": loiProtocolStatus }],
					headers: { 'Content-Type': 'application/json-patch+json' }
				},
				statePath: this.props.componentPath + '.pageData._embedded.' + this.props.entityName + '.' + protocol.rowIndex,
				mapping: response => (response.data),
				callback: (data) => { }
			});
		});
		this.props.actions.patchRESTMultiData(patches, (responses) => { }, 'PageViewManufacturerProtocolContainer.downloadPdfZip');
	}

	async handleFileUpload(fileList) {
		let fileResults = [];
		const frPromises = fileList.map((file) =>
			new Promise((resolve, reject) => {
				const fr = new FileReader();
				fr.onload = () => resolve({ file: file, reader: fr });
				fr.onerror = (err) => reject(err);
				fr.readAsDataURL(file);
			})
		);

		try {
			fileResults = await Promise.all(frPromises);
		} catch (err) {
			console.error(err);
			return;
		}

		var loiProtocolStatus = await getLoiByCode("loiProtocolStatuses", 4, this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData);

		for (const fileResult of fileResults) {
			const { file, reader } = fileResult;

			const fileId = file.name.slice(0, file.name.indexOf("_"));
			//find the row with this protocol
			var protocolRow = this.props.data.findIndex((protocol) => protocol.id == fileId);
			var protocolId = -1;
			var data = undefined;
			if (protocolRow == -1) {
				//load the missing protocol
				protocolId = fileId;
				let fetchPromiseWrapper = {};
				this.props.actions.fetchRESTFollow(
					{
						url: API_URL + "/reports/builder/1",
						params: {
							"from": this.props.entityDef.className,
							"select": this.props.entityDef.className,
							[this.props.entityDef.className + ".id"]: fileId,
						}
					},
					this.props.componentPath + '.tempFetchResponse',
					response => {
						let newData = response.data;
						//transform data to "repository response"-like
						builderDataToProjection(newData, this.props.entityName);
						newData = newData._embedded[this.props.entityName][0];
						if (newData == undefined) {
							newData = new Error(this.props.entityName + " not found");
						}

						data = newData;

						return newData;
					},
					"PageViewManufacturerProtocolContainer.handleFileUpload",
					{},
					fetchPromiseWrapper
				);
				await fetchPromiseWrapper.promise;
			} else {
				//get the id of the protocol
				protocolId = this.props.data[protocolRow].id;
				data = this.props.data[protocolRow];
			}

			var dataURL = reader.result.substr(reader.result.indexOf(',') + 1); //here we get the file in base64

			data.loiProtocolStatus = loiProtocolStatus;
			data.pdfFile = dataURL;

			this.props.actions.patchRESTData(
				{
					method: 'patch',
					url: API_URL + "/" + this.props.entityName + "/" + protocolId,
					data: data,
					headers: { 'Content-Type': 'application/merge-patch+json' }
				},
				//this.props.componentPath+'.pageData._embedded.'+this.props.entityName+'.'+protocolRow,
				this.props.componentPath + '.tempFetchResponse',
				response => (response.data),
				(data) => { },
				"PageViewManufacturerProtocolContainer.handleFileUpload"
			);
		};
		//force refresh
		this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
	}

	getExcelBase64(dataTableItem, protocolMinPriceMWh, tax, protocolTypeCode, protocolTaxTypeCode, fromDate, toDate) {

		let data = []
		let data2 = []

		if (protocolTypeCode == 1) {
			//protocolTypeCode 1: Количество по електромер
			//protocolTypeCode 2: Количество по график

			if (protocolTaxTypeCode == 1) {
				//protocolTaxTypeCode 1: Такса/процент
				//protocolTaxTypeCode 2: Фиксирана такса
				//protocolTaxTypeCode 3: Няма такса

				let header = ['Дата', 'Ден', 'Час ЕЕТ', `${dataTableItem.itn} / График, kWh`, `${dataTableItem.itn} / Енергия, kWh`, 'Цена Д-1 БНЕБ, лв', 'Разход, лв.']
				data.push(header)

				for (let i = 0; i < detailTableData.length; i++) {
					if (detailTableData[i].itn == dataTableItem.itn && moment.utc(detailTableData[i].date) >= fromDate && moment.utc(detailTableData[i].date) <= toDate) {
						let innerArray = []
						let day = new Date(detailTableData[i].date).getDate()
						innerArray.push(detailTableData[i].date)
						innerArray.push(day)
						innerArray.push(detailTableData[i].hour)
						innerArray.push(detailTableData[i].qty_MWh_produced)
						innerArray.push(detailTableData[i].qty_MWh_measured)
						innerArray.push(detailTableData[i].price)
						innerArray.push(detailTableData[i].expense)
						//console.log("Inner Array", innerArray)
						data.push(innerArray)
					}
				}
				
				let quantity = parseFloat(dataTableItem.scheduledEnergy) + parseFloat(dataTableItem.balancedEnergy)
				let quantityDiff = (parseFloat(dataTableItem.contractMwhQty) - (parseFloat(dataTableItem.scheduledEnergy) + parseFloat(dataTableItem.balancedEnergy)))
				let income = parseFloat(dataTableItem.scheduledPriceWithoutVat) + parseFloat(dataTableItem.balancedPriceWithoutVat)
				let incomeDiff = (parseFloat(dataTableItem.contractIncome) - (parseFloat(dataTableItem.scheduledPriceWithoutVat) + parseFloat(dataTableItem.balancedPriceWithoutVat)))

				data2 = [
					["Изупуване по електромер"],
					["Такса, лв.", tax + " %"],
					["Не по-малко от, лв./MWh", protocolMinPriceMWh + " лв."],
					[""],
					["", "Условия по договора", ""],
					[""],
					["* Разходи на обекта"],
					["Количество, MWh", "Разход, лв.", "Разход, лв./MWh"],
					[dataTableItem.contractMwhQty, dataTableItem.contractExpense, dataTableItem.contractExpensePerMwh],
					[""],
					["* Приходи на обекта"],
					["Количество, MWh", "Приход, лв.", "Приход, лв./MWh"],
					[dataTableItem.contractMwhQty, dataTableItem.contractIncome, dataTableItem.contractIncomePerMwh],
					[""],
					["", "Издаден протокол", ""],
					[""],
					["*График по СПЦ сметната по електромер"],
					["Количество, MWh", "Приход, лв.", "Цена, лв./MWh"],
					[dataTableItem.scheduledEnergy, dataTableItem.scheduledPriceWithoutVat, dataTableItem.contractIncomePerMwh],
					[""],
					["*Излишък/недостиг по СПЦ сметната по електромер"],
					["Количество, MWh", "Приход/Разход, лв.", "Цена, лв./MWh"],
					[dataTableItem.balancedEnergy, dataTableItem.balancedPriceWithoutVat, dataTableItem.contractIncomePerMwh],
					[""],
					[""],
					["Проверка"],
					[""],
					["Количество, MWh", dataTableItem.contractMwhQty],
					["Количество, MWh", quantity ],
					["Разлика, MWh", quantityDiff ],
					//this.round((((dataTableItem.contractIncome - (dataTableItem.scheduledPriceWithoutVat + dataTableItem.balancedPriceWithoutVat))) * 100) / 100, 3)
					[""],
					["Приход, лв.", dataTableItem.contractIncome + " лв."],
					["Приход, лв.", income  + " лв."], //this.round(((dataTableItem.scheduledPriceWithoutVat + dataTableItem.balancedPriceWithoutVat) * 100) / 100, 3) 
					["Разлика, MWh", incomeDiff + " лв."]
				]
			} else {
				let header = ['Дата', 'Ден', 'Час ЕЕТ', dataTableItem.itn, 'Цена Д-1 БНЕБ, лв']
				data.push(header)

				for (let i = 0; i < detailTableData.length; i++) {
					if (detailTableData[i].itn == dataTableItem.itn && moment.utc(detailTableData[i].date) >= fromDate && moment.utc(detailTableData[i].date) <= toDate) {
						let innerArray = []
						let day = new Date(detailTableData[i].date).getDate()
						innerArray.push(detailTableData[i].date)
						innerArray.push(day)
						innerArray.push(detailTableData[i].hour)
						innerArray.push(detailTableData[i].qty_MWh_measured)
						innerArray.push(detailTableData[i].price)
						//console.log("Inner Array", innerArray)
						data.push(innerArray)
					}
				}

				data2 = [
					["*График, kWh", "Такса, лв.", tax],
					[],
					[],
					["Централа", "Количество, MWh", "Приход, лв", "Цена, лв/MWh"],
					[dataTableItem.itn, dataTableItem.energyForInvoice, dataTableItem.priceWithoutVat, dataTableItem.pricePerMwh]
				]
			}

		} else if (protocolTypeCode == 2) {

			if (protocolTaxTypeCode == 1) {
				//protocolTaxTypeCode 1: Такса/процент
				//protocolTaxTypeCode 2: Фиксирана такса
				//protocolTaxTypeCode 3: Няма такса

				let header = ['Дата', 'Ден', 'Час ЕЕТ', dataTableItem.itn, 'Цена Д-1 БНЕБ, лв', 'Разход, лв.']
				data.push(header)

				for (let i = 0; i < detailTableData.length; i++) {
					if (detailTableData[i].itn == dataTableItem.itn && moment.utc(detailTableData[i].date) >= fromDate && moment.utc(detailTableData[i].date) <= toDate) {
						//console.log("detailTableData[i]: ", detailTableData[i])
						let innerArray = []
						let day = new Date(detailTableData[i].date).getDate()
						innerArray.push(detailTableData[i].date)
						innerArray.push(day)
						innerArray.push(detailTableData[i].hour)
						innerArray.push(detailTableData[i].qty_MWh_produced)
						innerArray.push(detailTableData[i].price)
						innerArray.push(detailTableData[i].expense)
						//console.log("Inner Array", innerArray)
						data.push(innerArray)
					}
				}
			} else {

				let header = ['Дата', 'Ден', 'Час ЕЕТ', dataTableItem.itn, 'Цена Д-1 БНЕБ, лв']
				data.push(header)

				//console.log("detailTableData: ", detailTableData)
				for (let i = 0; i < detailTableData.length; i++) {
					if (detailTableData[i].itn == dataTableItem.itn && moment.utc(detailTableData[i].date) >= fromDate && moment.utc(detailTableData[i].date) <= toDate) {
						//console.log("detailTableData[i]: ", detailTableData[i], " / index: ", i)
						let innerArray = []
						let day = new Date(detailTableData[i].date).getDate()
						innerArray.push(detailTableData[i].date)
						innerArray.push(day)
						innerArray.push(detailTableData[i].hour)
						innerArray.push(detailTableData[i].qty_MWh_produced)
						innerArray.push(detailTableData[i].price)
						//console.log("Inner Array", innerArray)
						data.push(innerArray)
					}
				}
			}

			data2 = [
				["*График, kWh", "Такса, лв.", tax],
				[],
				[],
				["Централа", "Количество, MWh", "Приход, лв", "Цена, лв/MWh"],
				[dataTableItem.itn, dataTableItem.energyForInvoice, dataTableItem.priceWithoutVat, dataTableItem.pricePerMwh]
			]
		}

		// console.log("dataarray:", data)
		data.sort((a, b) => {
			const dayA = a[1]
			const dayB = b[1]
			const hourA = a[2]
			const hourB = b[2]

			// console.log("dayA: ", dayA)
			// console.log("dayB: ", dayB)
			// console.log("hourA: ", hourA)
			// console.log("hourB: ", hourB)
			if (dayA == "Ден") {
				return -1
			}
			if (dayB == "Ден") {
				return 1
			}
			if (dayA < dayB) {
				return -1
			}
			if (dayA > dayB) {
				return 1
			}
			if (hourA < hourB) {
				return -1
			}
			if (hourA > hourB) {
				return 1
			}
			return 0
		})

		const wb = XLSX.utils.book_new()

		const ws1 = XLSX.utils.aoa_to_sheet(data)
		XLSX.utils.book_append_sheet(wb, ws1, "Sheet1")

		const ws2 = XLSX.utils.aoa_to_sheet(data2)
		XLSX.utils.book_append_sheet(wb, ws2, "Sheet2")

		return XLSX.write(wb, { type: "base64" });
	}

	// -------------------------------------------------------------------------------------------------------------------------------

	// ---------------------------------------------------------------- UTILS --------------------------------------------------------

	formatNumbers(number) {

		let splitNumber = number.toString().split(".")

		let result = []
		let index = 0

		for (let i = splitNumber[0].length - 1; i >= 0; i--) {

			if (index == 3) {
				index = 0
				result.push(' ')
			}
			result.push(splitNumber[0][i])
			index = index + 1
		}

		result.reverse()
		let returnResult = result.join('').concat('.', splitNumber[1])

		return returnResult
	}

	getLastDayOfMonth(currentMonth) {

		if (currentMonth == 1 || currentMonth == 3 || currentMonth == 5 || currentMonth == 7 || currentMonth == 8 || currentMonth == 10 || currentMonth == 12) {
			return 31
		} else if (currentMonth == 4 || currentMonth == 6 || currentMonth == 9 || currentMonth == 11) {
			return 30
		} else if (currentMonth == 2) {
			let currentYear = new Date().getFullYear()
			let lastDayOfFebruary = new Date(currentYear + '-2-29').getDate()

			if (lastDayOfFebruary == 29) {
				return 29
			}
			return 28
		}
	}

	returnDate(date, hour) {
		if (hour == 24) {
			let newDate = new Date(date)
			newDate.setDate(newDate.getDate() - 1)
			return moment(newDate).format("YYYY-MM-DDTHH:mm:ss").substring(0, 10)
		}

		return date.substring(0, 10)
	}

	addData(newList) {

		this.props.actions.dispatchEditRESTData(this.props.componentPath + ".pageData._embedded.powerPlantProtocols", newList);
	}

	getProtocolCreationDate(toDate) {

		let currentYear = new Date(toDate).getFullYear()
		let currentMonth = new Date(toDate).getMonth() + 1
		let currentDay = new Date(toDate).getDate()

		let dayOfWeek = new Date(toDate).getDay()

		if (dayOfWeek == 0) {
			currentDay = currentDay + 1

			if (currentDay == 31 && currentMonth == 12) {
				return "02-01-" + (currentYear + 1)
			}

			let newDate = new Date(currentYear + "-" + currentMonth + "-" + currentDay)

			currentYear = new Date(newDate).getFullYear()
			currentMonth = new Date(newDate).getMonth() + 1
			currentDay = new Date(newDate).getDate()

			return currentDay + "-" + currentMonth + "-" + currentYear

		} else if (dayOfWeek == 6) {
			currentDay = currentDay + 2

			if (currentDay == 31 && currentMonth == 12) {
				return "02-01-" + (currentYear + 1)
			}

			let newDate = new Date(currentYear + "-" + currentMonth + "-" + currentDay)

			currentYear = new Date(newDate).getFullYear()
			currentMonth = new Date(newDate).getMonth() + 1
			currentDay = new Date(newDate).getDate()

			return currentDay + "-" + currentMonth + "-" + currentYear

		} else if (currentDay == 31 && currentMonth == 12) {

			return "02-01-" + (currentYear + 1)
		}

		return currentDay + "-" + currentMonth + "-" + currentYear
		
	}

	returnStateDate(date, plus) {

		if (plus == true) {
			let newDate = new Date(date)
			newDate = newDate.setDate(newDate.getDate() + 1)
			return moment(newDate).format("YYYY-MM-DDTHH:mm:ss").substring(0, 10)
		} else {
			let newDate = new Date(date)
			newDate = newDate.setDate(newDate.getDate() - 1)
			return moment(newDate).format("YYYY-MM-DDTHH:mm:ss").substring(0, 10)
		}
	}

	getExpense(price, qty_MWh, protocolTaxValue, protocolTaxTypeCode, protocolTypeCode) {
		if (protocolTaxTypeCode == 1) {
			if (protocolTypeCode == 1) {
				//protocolTypeCode 1: Количество по електромер
				//protocolTypeCode 2: Количество по график
				return this.round((((price * qty_MWh * protocolTaxValue / 1000) / 100) * 10) / 10, 2)
			} else {
				return this.round((((price - ((price * protocolTaxValue) / 100))) * 10) / 10, 2)
			}

		}
		return 0
	}

	round(n, dp) {
		const h = +('1'.padEnd(dp + 1, '0')) // 10 or 100 or 1000 or etc
		return Math.round(n * h) / h
	}

	// -------------------------------------------------------------------------------------------------------------------------------

	// ===================================================== IMPORTS ====================================================================
	importXLSXProducedSchedule(id) {
		console.log("importXLSXProducedSchedule")
		console.log(id)
		console.log("URL: " + API_URL + REPORTS_IMPORT_XLSX_PRODUCED_SCHEDULE + id)
		this.setState({ uploadProducedEnergyList: true })
		axios({
			method: POST,
			url: API_URL + REPORTS_IMPORT_XLSX_PRODUCED_SCHEDULE + id,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(response => {
			let data = response.data;
			console.log("PageTableScheduleContainer.axios.then", data);
			this.setState({ uploadProducedEnergyList: false })
			//force refresh
			// this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
		}).catch(error => {
			console.log("PageTableScheduleContainer.axios.error", error)
		});
	}

	importXLSXMeterReading(id) {
		console.log("importXLSXMeterReading")
		console.log(id)
		console.log("URL: " + API_URL + REPORTS_IMPORT_XLSX_METER_READING + id)
		this.setState({ uploadMeterEnergyList: true })
		axios({
			method: POST,
			url: API_URL + REPORTS_IMPORT_XLSX_METER_READING + id,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(response => {
			let data = response.data;
			console.log("PageTableScheduleContainer.axios.then", data);
			this.setState({ uploadMeterEnergyList: false })
			//force refresh
			// this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
		}).catch(error => {
			console.log("PageTableScheduleContainer.axios.error", error)
		});
	}

	importXLSXIbexPrice(id) {
		console.log("importXLSXIbexPrice")
		console.log(id)
		console.log("URL: " + API_URL + REPORTS_IMPORT_XLSX_IBEX_PRICE + id)
		this.setState({ uploadPriceList: true })
		axios({
			method: POST,
			url: API_URL + REPORTS_IMPORT_XLSX_IBEX_PRICE + id,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(response => {
			let data = response.data;
			console.log("PageTableScheduleContainer.axios.then", data);
			this.setState({ uploadPriceList: false })
			//force refresh
			// this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
		}).catch(error => {
			console.log("PageTableScheduleContainer.axios.error", error)
		});
	}

	auctionPrices() {
		console.log("REPORTS_AUCTION_PRICES")
		console.log("URL: " + API_URL + REPORTS_AUCTION_PRICES)
		this.setState({ uploadPriceList: true })
		axios({
			method: GET,
			url: API_URL + REPORTS_AUCTION_PRICES,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(response => {
			let data = response.data;
			console.log("PageTableScheduleContainer.axios.then", data);
			this.setState({ uploadPriceList: false })
			//force refresh
			// this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
		}).catch(error => {
			console.log("PageTableScheduleContainer.axios.error", error)
		});
	}

	//================================================================================================================================

	//================================================= VALIDATION FUNCTIONS ============================================================
	
	validateDateInputs() {
		if (this.state.toDate < this.state.fromDate) {
			this.setState({ isAlert: true, alertMessage: "'До Дата', не може да бъде по-малко от 'От Дата'" })
			return false
		}

		return true
	}

	validateMethodology(
		contractStatusCode,
		contractEndDate,
		protocolTypeCode,
		protocolRowsCode,
		protocolPriceTypeCode,
		protocolFixedPriceValue,
		protocolTaxTypeCode,
		protocolTaxValue, itn
	) {

		let currentDate = new Date()
		let protocolContractEndDate = new Date(contractEndDate)

		if ((contractStatusCode == 2 && protocolContractEndDate > currentDate) || (contractStatusCode == 3 && protocolContractEndDate < currentDate)) {
			//contractStatusCode 1: Не подписан
			//contractStatusCode 2: Подписан
			//contractStatusCode 3: Очаква се анекс

			//console.log("contractStatusCode true ", itn)
			if ((protocolTypeCode == 2 && protocolRowsCode == 1) || (protocolTypeCode == 1 && protocolRowsCode == 1) || (protocolTypeCode == 1 && protocolRowsCode == 3)) {
				//protocolTypeCode 1: Количество по електромер
				//protocolTypeCode 2: Количество по график
				//protocolRowsCode 1: Протокол с един ред
				//protocolRowsCode 3: Протокол с три реда

				//console.log("protocolTypeCode true ", itn)
				if (protocolPriceTypeCode = 2 || (protocolPriceTypeCode == 1 && protocolFixedPriceValue > 0)) {
					//protocolPriceTypeCode 1: Фиксирана цена
					//protocolPriceTypeCode 2: Цена IBEX

					//console.log("protocolPriceTypeCode true ", itn)
					if (protocolTaxTypeCode = 3 || (protocolTaxTypeCode == 2 && protocolTaxValue > 0) || (protocolTaxTypeCode == 1 && protocolTaxValue > 0)) {
						//protocolTaxTypeCode 1: Такса/процент
						//protocolTaxTypeCode 2: Фиксирана такса
						//protocolTaxTypeCode 3: Няма такса

						//console.log("protocolTaxTypeCode true ", itn)
						return true
					}
				}
			}
		}
		//console.log("false")
		return false
	}

	getNotifications() {

		axios({
			method: GET,
			url: API_URL + `/reports/builder/1?from=Notification&select=Notification&page=0&size=1000&sort=Notification.id%2Cdesc`,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		}).then(response => {
			let data = response.data._embedded.hashMaps;
			//console.log("getNotifications(): ", data);

			let arr = []

			for (var i = 0; i < data.length; i++) {

				let message = data[i]['Notification'].message
				let date = data[i]['Notification'].messageDateTime
				let itn = data[i]['Notification'].identificationFirst
				let origin = data[i]['Notification'].identificationSecond

				let objectData = {
					message: message,
					itn: itn,
					origin: this.props.t(`ManufacturerProtocol.${origin}`),
					date: date
				}
				arr.push(objectData)
			}

			this.setState({ warningArray: arr })

		}).catch(error => {
			console.log("PageViewManufacturerProtocolContainer.getNotifications().axios.error", error)
		});

	}

	//================================================================================================================================

	//==================================================== CHARTS FUNCTIONS ==========================================================================
	getPieChartData() {
		//let colors = ['green', 'red']//['green', 'lime', 'red', 'yellow']
		let colors = ['#0F65AA', 'orange']


		var data = [{
			marker: { colors: colors },
			values: [this.state.generatedProtocols, this.state.nonGeneratedProtocols],//[this.state.generatedProtocolsProduced, this.state.generatedProtocolsMeasured, this.state.nonGeneratedProtocolsProduced, this.state.nonGeneratedProtocolsMeasured],
			labels: ['Генерирани', 'Не генерирани'], //['Генерирани Произведена', 'Генерирани Измерена','Не генерирани Произведена', 'Не генерирани Измерена'],
			texttemplate: "%{value} бр.",
			textposition: "inside",
			insidetextorientation: "radial",
			hole: .3,
			type: 'pie',
		}]

		return data
	}

	getDropdownValues(value) {
		if (value == false) {
			this.setState({ dropdownSelectedItn: "" })
		}
		this.setState({ isDropdownVisible: value })
	}

	//===================================================================================================================================

	render() {
		let body = EMPTY;
		//console.log("DATA: ", this.props)
		//console.log("Array: ", this.state.tableData)
		if (this.props.data) {
			if (this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon={SPINNER} size={SIZE_2X} spin />;
			} else if (this.props.id && this.props.id == parseInt(this.props.match.params.entity_id, 10)) {
				// const powerPlantProtocols = getEntityDefinition("powerPlantProtocols");
				// body = <div className={PAGE_BODY}>
				// 	{console.log("POWER PLANT PROTOCOLS: ", powerPlantProtocols)}
				// 	<EmbedRetrieveEntityListContainer
				// 		title={powerPlantProtocols.label}
				// 		icon={powerPlantProtocols.icon}
				// 		columns={powerPlantProtocols.columns}
				// 		componentPath={this.props.componentPath + ".powerPlantProtocols"}
				// 		retrieveType="powerPlantProtocols"
				// 		parentHref={this.props.href}
				// 		parentAttr="powerPlantProtocols"
				// 		parentData={this.props.data}
				// 		asTable={true}
				// 		expanded={true}
				// 	/>
				// 	<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath + DOT_CHANGE_LOG} retrieveType={this.props.entityName} />
				// </div>;
			}
		}
		const powerPlantProtocols = getEntityDefinition("powerPlantProtocols");
		//console.log("powerPlantProtocols: ", powerPlantProtocols)
		body = <div className={PAGE_BODY}>
			{/* {console.log("POWER PLANT PROTOCOLS: ", powerPlantProtocols)} */}
			<EmbedRetrieveEntityListContainer
				title={powerPlantProtocols.label}
				icon={powerPlantProtocols.icon}
				columns={powerPlantProtocols.columns}
				componentPath={this.props.componentPath}
				retrieveType="powerPlantProtocols"
				parentHref={this.props.href}
				parentAttr="powerPlantProtocols"
				parentData={this.props.data}
				asTable={true}
				expanded={true}
				dataRefreshInterval={10000000}
			/>
			<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath + DOT_CHANGE_LOG} retrieveType={this.props.entityName} />
		</div>;

		let dateContainer = EMPTY
		let dropdownContainer = EMPTY
		if (this.state.isDropdownVisible) {
			dateContainer = <Form.Row>

				<Form.Group className={CLASS_NAME_M_2}>
					<Form.Label>{this.props.t("ManufacturerProtocol.fromDate")}</Form.Label>
					<Form.Control type="date" value={this.state.fromDate} onChange={(e) => {
						this.setState({ fromDate: e.target.value })
						console.log(this.state.fromDate)
					}} />
				</Form.Group>
				<Form.Group className={CLASS_NAME_M_2}>
					<Form.Label>{this.props.t("ManufacturerProtocol.toDate")}</Form.Label>
					<Form.Control type="date" value={this.state.toDate} onChange={(e) => {
						this.setState({ toDate: e.target.value })
						console.log(this.state.toDate)
					}} />
				</Form.Group>

			</Form.Row>
			dropdownContainer = <Form.Row>
				<Form.Group className={CLASS_NAME_M_2}>
					<DropdownButton id="dropdown-basic-button" title={this.state.dropdownSelectedItn == "" ? "Избери Обект" : this.state.dropdownSelectedItn} onSelect={(itn) => { this.setState({ dropdownSelectedItn: itn }) }}>
						<div style={{ maxHeight: "350px", overflowY: "auto" }}>
							{this.state.objectDropdownList.map((choise) =>
								<Dropdown.Item eventKey={choise['PowerPlant'].identificationNumber} key={choise['PowerPlant'].id} >
									{choise['PowerPlant'].name}
								</Dropdown.Item>
							)}
						</div>
					</DropdownButton>
				</Form.Group>
			</Form.Row>
		} else {
			dateContainer = EMPTY
			dropdownContainer = EMPTY
		}

		let loadingContainer = EMPTY
		if (this.state.getObjectList == false && this.state.getPriceList == false && this.state.getProductionList == false && this.state.getMeterList == false && this.state.calculating == false) {
			loadingContainer = EMPTY
		} else {
			loadingContainer = <div>
				<div style={{ margin: "20px", display: "grid", gridTemplateColumns: "1fr 1fr 1fr 1fr 1fr", gridGap: "25px" }}>
					<div>
						<div>Обекти</div>
						{this.state.getObjectList ? <Spinner animation="border" variant="primary" /> : <FontAwesomeIcon size="2x" icon="check" style={{ color: "green" }} />}

					</div>
					<div>
						<div>Произведена Енергия</div>
						{this.state.getProductionList ? <Spinner animation="border" variant="primary" /> : <FontAwesomeIcon size="2x" icon="check" style={{ color: "green" }} />}
					</div>
					<div>
						<div>Измерена енергия</div>
						{this.state.getMeterList ? <Spinner animation="border" variant="primary" /> : <FontAwesomeIcon size="2x" icon="check" style={{ color: "green" }} />}
					</div>
					<div>
						<div>Цени</div>
						{this.state.getPriceList ? <Spinner animation="border" variant="primary" /> : <FontAwesomeIcon size="2x" icon="check" style={{ color: "green" }} />}
					</div>
					<div>
						<div>Калкулация</div>
						{this.state.calculating ? <Spinner animation="border" variant="primary" /> : <FontAwesomeIcon size="2x" icon="check" style={{ color: "green" }} />}
					</div>
				</div>
				<ProgressBar animated now={this.state.createdProtocols == 0 ? 0 : (this.state.createdProtocols / this.state.allToBeCreated) * 100} />
			</div>
		}
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={this.props.headerText} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				{this.state.isAlert ? <Alert variant={"warning"} onClose={() => this.setState({ isAlert: false, alertMessage: "" })} dismissible>{this.state.alertMessage}</Alert> : ""}
				<div className={PAGE_BODY}>

					<Form>
						<Form.Row>

							<Form.Group className={CLASS_NAME_M_2}>
								<Form.Label>{this.props.t("ManufacturerProtocol.fromDate")}</Form.Label>
								<Form.Control type="date" value={this.state.currentDate} onChange={(e) => { this.setState({ currentDate: e.target.value }) }} />
							</Form.Group>
							<Button onClick={() => { this.definingTimePeriod() }}>Generate</Button>

						</Form.Row>
					</Form>

					<Tabs
						id="controlled-tab-example"
						activeKey={this.state.tabKey}
						onSelect={(k) => this.setState({ tabKey: k })}
						className="mb-3"
						justify
					>

						<Tab eventKey={DEFAULT_TAB_KEY_STATUS} title="Статус">
							<div style={{ margin: "20px", display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gridGap: "25px" }}>
								<Card style={{ bordeWidth: "2px", borderColor: "#04365F", position: "absolut", width: "100%", height: "100%", backfaceVisibility: "hidden" }}>
									{/* <Card.Header style={{ fontWeight: "bold", textAlign: "center", backgroundColor: "#04365F", color: "white" }}> */}
									<Card.Header style={{ fontWeight: "bold", textAlign: "center", backgroundColor: "#F7F7FF" }}>
										{'Протоколи ' + this.state.fromDate + " / " + this.state.toDate}
									</Card.Header>
									{/* <Card.Body style={{ color: "black", backgroundColor: "#F7F7FF" }}> */}
									<Card.Body style={{ color: "white", backgroundColor: "#04365F" }}>

										<b>Генерирани протоколи</b>
										<div>
											Произведена енергия: {this.state.generatedProtocolsProduced} бр.
										</div>
										<div>
											Измерена енергия: {this.state.generatedProtocolsMeasured} бр.
										</div>
										<p />
										<b>Не генеринани протоколи</b>
										<div>
											Произведена енергия: {this.state.nonGeneratedProtocolsProduced} бр.
										</div>
										<div>
											Измерена енергия: {this.state.nonGeneratedProtocolsMeasured} бр.
										</div>

									</Card.Body>
								</Card>
								<Plot
									data={this.getPieChartData()}
									layout={
										{
											//title: 'Протоколи ' + this.state.fromDate + " / " + this.state.toDate,
											height: 300,
											width: 400,
											margin: { "t": 10, "b": 10, "l": 20, "r": 30 },
											paper_bgcolor: DASHBOARD_CHART_PLOT_COLOR,
											legend: {
												x: 1,
												y: 0
											},
											annotations: [{ showarrow: false, text: this.state.allToBeCreated + " бр." }]
										}
									}
								/>
								<Card style={{ bordeWidth: "2px", borderColor: "#04365F", position: "absolut", width: "100%", height: "100%", backfaceVisibility: "hidden" }}>
									{/* <Card.Header style={{ fontWeight: "bold", textAlign: "center", backgroundColor: "#04365F", color: "white" }}> */}
									<Card.Header style={{ fontWeight: "bold", textAlign: "center", backgroundColor: "#F7F7FF" }}>
										{'Протоколи ' + this.state.fromDate + " / " + this.state.toDate}
									</Card.Header>
									{/* <Card.Body style={{ color: "black", backgroundColor: "#F7F7FF" }}> */}
									<Card.Body style={{ color: "white", backgroundColor: "#04365F" }}>

										<div>
											Контрагенти: {this.state.numberOfLegalPersons} бр.
										</div>
										<div>
											Обекти: {this.state.numberOfObejcts} бр.
										</div>

										<p />
										<div>
											Заредени файлове за цени: {this.state.uploadedPriseList ? 'Да' : 'Не'}
										</div>
										<div>
											Заредени файлове за произведена енергия: {this.state.uploadedProductionList ? 'Да' : 'Не'}
										</div>
										<div>
											Заредени файлове за измерена енергия: {this.state.uploadedMeterList ? 'Да' : 'Не'}
										</div>

									</Card.Body>
								</Card>
							</div>
						</Tab>
						<Tab eventKey={TAB_KEY_GENERATE} title="Генериране и изпращане на протоколи" disabled={this.state.isTabDisabled}>
							<div style={{ margin: "20px", display: "grid", gridTemplateColumns: "1fr 1fr", gridGap: "25px" }}>
								<div>
									<Form>
										<Form.Row>
											<Form.Group className={CLASS_NAME_M_2}>
												<DropdownButton id="dropdown-basic-button" title={!this.state.isDropdownVisible ? "Методология" : "Избери период"}>
													{dropdownValues.map((choise) =>
														<Dropdown.Item key={choise.index} onSelect={() => this.getDropdownValues(choise.isVisible)}>
															{choise.label}
														</Dropdown.Item>
													)}
												</DropdownButton>
											</Form.Group>
											<Form.Group className={CLASS_NAME_M_2}>
												<Button onClick={() => { this.createProtocolDetailData() /*this.getMeterData(this.meterEnergyPages)*/ /*this.getDataOnClick()*/ }}>
													{this.props.t("ManufacturerProtocol.generate")}
												</Button>
											</Form.Group>
										</Form.Row>
										{dateContainer}
										{dropdownContainer}
									</Form>
								</div>
								<div>
									<Form>
										<Form.Row>
											<Form.Group className={CLASS_NAME_M_2}>
												<Button onClick={() => {
													this.downloadPdfZip();
												}}>
													Изтегли протоколите
												</Button>
											</Form.Group>
											<Form.Group className={CLASS_NAME_M_2}>
												<div>
													<label className="btn btn-primary col-sm-12" htmlFor="uploadSigned">
														<FontAwesomeIcon icon="upload" />
														&nbsp;Зареди подписани протоколи
													</label>
													<input type="file" multiple name="uploadSigned" id="uploadSigned" className="inputfile" onChange={(evt) => this.handleFileUpload([...evt.target.files])} />
												</div>
											</Form.Group>
										</Form.Row>
									</Form>
								</div>
							</div>
							{loadingContainer}
						</Tab>
						<Tab eventKey={TAB_KEY_UPLOAD} title="Зареждане на данни" disabled={this.state.isTabDisabled}>
							<div style={{ padding: "20px", display: "grid", gridTemplateColumns: "1fr 1fr", gridGap: "25px" }}>
								<div style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gridGap: "25px" }}>
									<div>
										<NepalDBFileContainer
											onFileUpload={(id) => this.importXLSXProducedSchedule(id)}
											buttonText={this.props.t("NepalDBFile.manufacturer")}
											nepalDbFileInputTagId="fileProtocol"
										/>
									</div>
									<div />
									<div>
										<NepalDBFileContainer
											onFileUpload={(id) => this.importXLSXMeterReading(id)}
											buttonText={this.props.t("NepalDBFile.meter")}
											nepalDbFileInputTagId="fileMeter"
										/>
									</div>
									<div />
									<div>
										<NepalDBFileContainer
											onFileUpload={(id) => this.importXLSXIbexPrice(id)}
											buttonText={this.props.t("NepalDBFile.price")}
											nepalDbFileInputTagId="filePrice"
										/>
									</div>
									<div />
									<Button onClick={() => { this.auctionPrices() }}>
										Автоматично зареждане на цени от борсата
									</Button>
								</div>
								<div>
									<Card style={{ bordeWidth: "2px", borderColor: "#04365F", position: "absolut", width: "100%", height: "100%", backfaceVisibility: "hidden" }}>
										<Card.Header style={{ fontWeight: "bold", textAlign: "center", backgroundColor: "#04365F", color: "white" }}>
											Info Header
										</Card.Header>
										<Card.Body style={{ color: "black", backgroundColor: "#F7F7FF" }}>
											<div style={{ margin: "20px", display: "grid", gridTemplateColumns: "3fr 1fr", gridGap: "25px" }}>
												<div>Зареждане на данни по график</div>
												<div>
													{this.state.uploadProducedEnergyList ? <Spinner animation="border" variant="primary" /> : <FontAwesomeIcon size="2x" icon="check" style={{ color: "green" }} />}
												</div>
											</div>
											<div style={{ margin: "20px", display: "grid", gridTemplateColumns: "3fr 1fr", gridGap: "25px" }}>
												<div>Зареждане на данни по електромер</div>
												<div>
													{this.state.uploadMeterEnergyList ? <Spinner animation="border" variant="primary" /> : <FontAwesomeIcon size="2x" icon="check" style={{ color: "green" }} />}
												</div>
											</div>
											<div style={{ margin: "20px", display: "grid", gridTemplateColumns: "3fr 1fr", gridGap: "25px" }}>
												<div>Зареждане на Цени</div>
												<div>
													{this.state.uploadPriceList ? <Spinner animation="border" variant="primary" /> : <FontAwesomeIcon size="2x" icon="check" style={{ color: "green" }} />}
												</div>
											</div>
										</Card.Body>
									</Card>
								</div>
							</div>
						</Tab>
					</Tabs>

					{body}

					<div>
						<div>
							<ReactTable
								data={this.state.warningArray}
								columns={columns}
							/>
						</div>
					</div>
				</div>
			</div>
		);
	}
}

//redux mapping
function mapStateToProps(state, ownProps) {
	const entityName = "powerPlantProtocols";
	const entityDef = getEntityDefinition(entityName, undefined);
	const componentPath = "powerPlantProtocols";
	let viewData = resolveObjectPath(componentPath, state.rest);
	let data = undefined;
	if (viewData && viewData.pageData && viewData.pageData._embedded
		&& viewData.pageData._embedded[entityName] instanceof Array) {
		data = viewData.pageData._embedded[entityName];
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
		headerText: ownProps.t(MANUFACTURER_PROTOCOL_DOT_CLASS_NAME),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData, fetchRESTFollow, patchRESTMultiData, patchRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewManufacturerProtocolContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
