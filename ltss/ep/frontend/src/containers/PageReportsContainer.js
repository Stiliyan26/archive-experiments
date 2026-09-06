import React from 'react';
import Select from 'react-select'

import PivotTableUI from 'react-pivottable/PivotTableUI';
import 'react-pivottable/pivottable.css';
import TableRenderers from 'react-pivottable/TableRenderers';
import Plot from 'react-plotly.js';
import createPlotlyRenderers from 'react-pivottable/PlotlyRenderers';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Form, Dropdown, Row, Col } from 'react-bootstrap';

import lodash from 'lodash'
import moment from 'moment'
moment.locale('bg')
import querystring from 'querystring'
import ReactTable from 'react-table-v6'
import DatePicker from 'react-date-picker';
import i18n from 'i18next';
import { withTranslation } from 'react-i18next';

import * as Constants from './../static/constants';
import history from './../scripts/history'
import { isAuthenticated } from './../components/pages/login/Login.js'

import Header from './../components/generic/Header';

import { getExpandedColumns } from './nomenclatures/entityDefinitions.js'
import { resetRESTCallLimit, dispatchCleanRESTData, postRESTData } from './../actions/taskActions';
import { resolveObjectPath } from './../scripts/dataUtils';
import EmbedRetrieveEntityListContainer from './nomenclatures/EmbedRetrieveEntityListContainer'

//create Plotly renderers via dependency injection
const PlotlyRenderers = createPlotlyRenderers(Plot);

var yearStart = moment((new Date("1/1/" + ((new Date()).getFullYear()))).valueOf());
var yearEnd = moment((new Date("12/31/" + ((new Date()).getFullYear()))).valueOf());

const reports = [{
		//with separate visualization function
		label: i18n.t("Reports.IncomesAndExpenditures"),
		value: "incomes_and_expenditures",
	}, {
		//with separate visualization function
		label: i18n.t("Reports.VehiclesProfitPerKm"),
		value: "vehicles_profit_per_km",
	}, {
		label: i18n.t("Reports.UpcomingMaintenance"),
		value: "maintenance_expenditure",
		root: "expenditures",
		expand: [],
		columnOverride: undefined,
		filter: [
			{where: {
				op: "equal",
				operands: ["expenseType.listOptionItemCode", {literal: 1}],
			},},
			{id: "expenseToDate", value: {fromDate: yearStart, toDate: yearEnd}}
		]
	}, {
		//with separate visualization function
		label: i18n.t("Reports.EmployeesIncomesAndExpenditures"),
		value: "employees_incomes_and_expenditures",
//	}, {
//		label: "Коментари",
//		value: "comments",
//		root: "comments",
//		expand: ["task","task.counterParty","task.counterParty.interests","task.counterParty.contacts"],
//		columnOverride: undefined,
//		filter: [{id: "task.dtype", value: "TransportOrder"}],
//	}, {
//		label: "Запитвания от клиенти, публични поръчки и търгове",
//		value: "requestsForOffer",
//		root: "tasks",
//		expand: ["status","counterParty","comments","taskAttachments"],
//		columnOverride: undefined,
//		filter: [{id: "type.code", value: 3}],
//	}, {
//		label: "Оферти към клиенти",
//		value: "offersToClients",
//		root: "offerToClients",
//		expand: ["person","offerLines"],
//		columnOverride: undefined,
//		filter: [],
//	}, {
//		label: "Заявки",
//		value: "orders",
//		root: "importedOrders",
//		expand: ["offer"],
//		columnOverride: {
//			id: {show: false},
//			createdBy: {show: false},
//			createdDate: {show: false},
//			lastModifiedBy: {show: false},
//			lastModifiedDate: {show: false},
//			"foreignId": {show: false},
//			"foreignDeleted": {show: false},
//			"compId": {show: false},
//			"updateCountAsBigInt": {show: false},
//			"offer.vatPercent": {show: false},
//			"offer.discountPercent": {show: false},
//			"offer.discountCondition": {show: false},
//			"offer.deliveryTerms": {show: false},
//			"offer.notes": {show: false},
//			"offer.foreignId": {show: false},
//		},
//		filter: [{id: "importedExpeditionLists.foreignDeleted", value: false},{id: "importedInvoices.foreignDeleted", value: false}],
//	}, { //from=Article&select=Article,sum(Article.plannedIncomeOrExpenses.ammount),count(Article.plannedIncomeOrExpenses.task.id)
//		label: "Планирани артикули в задачите",
//		value: "ammountsPlannedInTasksByArticles",
//		root: "articles",
//		filter: [],
//		columns: [
//					{
//						Header: "Артикул",
//						accessor: 'name',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Общо кол-во",
//						accessor: "sum(Article.plannedIncomeOrExpenses.ammount)",
//						isAggregate: true,
//						filterable: false,
//						sortable: false,
//						aggregation: {
//							op: "sum",
//							operands: ["plannedIncomeOrExpenses.ammount"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					},{
//						Header: "Бр. задачи",
//						accessor: "count(Article.plannedIncomeOrExpenses.task.id)",
//						isAggregate: true,
//						filterable: false,
//						sortable: false,
//						aggregation: {
//							op: "count",
//							operands: ["plannedIncomeOrExpenses.task.id"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	}, {
//		label: "Суми по възложени задачи на потребители",
//		value: "ammountsPlannedInTasksBySecUsers",
//		root: "secUsers",
//		filter: [{id: "assignedTasks.plannedIncomeOrExpenses.article.dtype", value: "Currency"}],
//		columns: [
//					{
//						Header: "Потребител",
//						accessor: 'fullName',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Бр. задачи",
//						accessor: "count(SecUser.assignedTasks.id)",
//						isAggregate: true,
//						filterable: false,
//						sortable: false,
//						aggregation: {
//							op: "count",
//							operands: ["assignedTasks.id"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					},{
//						Header: "Валута",
//						accessor: "assignedTasks.plannedIncomeOrExpenses.article.name",
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Общо суми",
//						accessor: "sum(SecUser.assignedTasks.plannedIncomeOrExpenses.ammount)",
//						isAggregate: true, //TODO make aggregates sortable
//						filterable: false,
//						sortable: false,
//						aggregation: {
//							op: "sum",
//							operands: ["assignedTasks.plannedIncomeOrExpenses.ammount"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	}, {
//		label: "Суми по задачи към контрагенти",
//		value: "ammountsPlannedInTasksByCounterParty",
//		root: "legalPersons",
//		filter: [{id: "tasks.plannedIncomeOrExpenses.article.dtype", value: "Currency"}],
//		columns: [
//					{
//						Header: "Наименование",
//						accessor: 'name',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Бр. задачи",
//						accessor: "count(LegalPerson.tasks.id)",
//						isAggregate: true, //TODO make aggregates sortable
//						filterable: false,
//						sortable: false,
//						aggregation: {
//							op: "count",
//							operands: ["tasks.id"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					},{
//						Header: "Валута",
//						accessor: "tasks.plannedIncomeOrExpenses.article.name",
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Общо суми",
//						accessor: "sum(LegalPerson.tasks.plannedIncomeOrExpenses.ammount)",
//						isAggregate: true, //TODO make aggregates sortable
//						filterable: false,
//						sortable: false,
//						aggregation: {
//							op: "sum",
//							operands: ["tasks.plannedIncomeOrExpenses.ammount"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	}, {
//		label: "Предаден отпадък за текуща година",
//		value: "processedWaste",
//		root: "wastes",
//		excludeRootEntity: true,
//		sort: [{id:'name', value: 'desc'}],
//		filter: [{id: "processedDate", value: {fromDate: yearStart, toDate: yearEnd}}],
//		columns: [
//					{
//						Header: "Наименование",
//						accessor: 'costCenter.name',
//						isSpecific: true,
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "ЕИК",
//						accessor: "costCenter.eik",
//						isSpecific: true,
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Основание за притежание",
//						accessor: "costCenter.possessionReason",
//						isSpecific: true,
//						fluidSize: 2,
//						dataType: "TEXT",
//					},
//					{
//						Header: "Количество",
//						accessor: "sum(Waste.processedAmount)",
//						isAggregate: true, //TODO make aggregates sortable
//						filterable: false,
//						sortable: false,
//						aggregation: {
//							op: "sum",
//							operands: ["processedAmount"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	}, {
//		label: "Количества отпадък за текуща година",
//		value: "wasteQuantity"
//	}, {
//		label: "Таблица за отчитане явяването и неявяването на работа",
//		value: "timeSheetsPerDay"
	}
];
//Page: can be used as a landing page
//Container: redux container class
class PageReportsContainer extends React.Component {

	constructor(props){
		super(props);
		this.state = {
			timeSheetsPerDayMonth: moment(),
			pivotProps: undefined,
			vehicleReportOption: "profitPerKm",
			vehicleFromDate: yearStart.toISOString(),
			vehicleToDate: yearEnd.toISOString(),
			employeeFromDate: yearStart.toISOString(),
			employeeToDate: yearEnd.toISOString(),
			profitFromDate: yearStart.toISOString(),
			profitToDate: yearEnd.toISOString(),
		}
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		} else {
			this.retrieveData();
		}
	}

	componentDidUpdate(prevProps, prevState) {
		this.retrieveData();
	}

	retrieveData() {
		if(this.props.match.params.selectedReport == 'wasteQuantity'
			&& !(this.props.wasteQuantity instanceof Array)
			&& !(this.props.wasteQuantity && this.props.wasteQuantity[Constants.PATH_FOR_LOADING] instanceof Promise)
			&& !(this.props.wasteQuantity instanceof Error)
		) {
			this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/reports/wasteQuantity",
						data: {}
					},
					'reports.wasteQuantity',
					'PageReportsContainer.retrieveData'
				);
		}
		if(this.props.match.params.selectedReport == 'timeSheetsPerDay'
			&& !(this.props.timeSheetsPerDay)
			&& !(this.props.timeSheetsPerDay && this.props.timeSheetsPerDay[Constants.PATH_FOR_LOADING] instanceof Promise)
			&& !(this.props.timeSheetsPerDay instanceof Error)
		) {
			this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/reports/timeSheetsPerDay/"+this.state.timeSheetsPerDayMonth.clone().startOf('month').format("DDMMYYYY")+"/"+this.state.timeSheetsPerDayMonth.clone().endOf('month').format("DDMMYYYY"),
						data: {}
					},
					'reports.timeSheetsPerDay',
					'PageReportsContainer.retrieveData'
				);
		}
	}

	renderTimeSheetsPerDay() {
		if(this.props.timeSheetsPerDay) {
			if(this.props.timeSheetsPerDay[Constants.PATH_FOR_LOADING] instanceof Promise) {
				return <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else {
				let reportData = [];
				if(this.props.timeSheetsPerDay instanceof Array) {
					reportData = this.props.timeSheetsPerDay.map((elem) => ({
						employeeName: elem[0] || this.props.t("Reports.NotEmployee"),
						jobPositionName: elem[1] || this.props.t("Reports.NotEmployee"),
						companyDepartmentName: elem[2] || this.props.t("Reports.NotEmployee"),
						timeSheetItemTypeCode: elem[3] || 0,
						fromTime: elem[4] || 0,
						totalTime: elem[5]/3600 || 0
					}));
				}
				let columns = [{
						Header: this.props.t("Employee.employeeName"),
						accessor: 'employeeName'
					},{
						Header: this.props.t("JobPosition._className"),
						accessor: 'jobPositionName',
						aggregate: vals => vals[0],
					},{
						Header: this.props.t("CompanyDepartment._className"),
						accessor: 'companyDepartmentName',
						aggregate: vals => vals[0],
					},
				]
				let dates = Array.from(new Set(reportData.map((elem) => (elem.fromTime)))).sort();
				let currentDate = this.state.timeSheetsPerDayMonth.clone().startOf('month');
				const endDate = this.state.timeSheetsPerDayMonth.clone().endOf('month');
				while(currentDate < endDate) {
					const dateText = currentDate.format("YYYY-MM-DD");
					columns.push({
						Header: currentDate.format("DD.MM.YYYY"),
						id: currentDate.format("DD.MM.YYYY"),
						accessor: d => dateText == d.fromTime ? d.totalTime : 0,
						aggregate: vals => lodash.round(lodash.sum(vals),3),
					});
					currentDate.add(1, "days");
				};
				columns = columns.concat([{
						Header: this.props.t("Reports.totalTime"),
						accessor: 'totalTime',
						aggregate: vals => lodash.round(lodash.sum(vals),3),
					},{
						Header: this.props.t("Reports.code9overtime"),
						accessor: 'code9overtime',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == 9)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},{
						Header: this.props.t("Reports.code0work"),
						accessor: 'code0work',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == undefined || row._original.timeSheetItemTypeCode == 0)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},{
						Header: this.props.t("Reports.code1paidLeave"),
						accessor: 'code1paidLeave',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == 1)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},{
						Header: this.props.t("Reports.code2maternity"),
						accessor: 'code2maternity',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == 2)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},{
						Header: this.props.t("Reports.code3sickLeave"),
						accessor: 'code3sickLeave',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == 3)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},{
						Header: this.props.t("Reports.code10govDuty"),
						accessor: 'code10govDuty',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == 10)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},{
						Header: this.props.t("Reports.code4compensation"),
						accessor: 'code4compensation',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == 4)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},{
						Header: this.props.t("Reports.code5noShow"),
						accessor: 'code5noShow',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == 5)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},{
						Header: this.props.t("Reports.code6holiday"),
						accessor: 'code6holiday',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == 6)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},{
						Header: this.props.t("Reports.code7unpaidLeave"),
						accessor: 'code7unpaidLeave',
						aggregate: (vals,rows) => {
							return rows.filter((row) => row._original.timeSheetItemTypeCode == 7)
								.reduce((acc,row) => acc = acc + row._original.totalTime,0);
						},
					},
				]);
				//TODO params in URL
				return <div className='page-body'>
						<DatePicker
							selected={this.state.timeSheetsPerDayMonth}
							placeholderText={this.props.t("Reports.ChooseDate")}
							locale="bg-bg"
							timeFormat={"HH:mm"}
							dateFormat={"DD.MM.YYYY"}
							onChange={(e) => {
								resetRESTCallLimit();
								this.setState({timeSheetsPerDayMonth: e});
								this.props.actions.dispatchCleanRESTData('reports.timeSheetsPerDay');
							}}
						/>
						{this.props.t("Reports.ForMonth")}{this.state.timeSheetsPerDayMonth.format("MMMM YYYY")}
						<ReactTable
							className='clients-table align-center-table -striped -highlight'
							data={reportData}
							columns={columns}
							pivotBy={["employeeName"]}
							defaultPageSize={10}
							showPagination={true}
						/>
					</div>;
			}
		}
		return undefined;
	}

	renderWasteQuantity() {
		if(this.props.wasteQuantity) {
			if(this.props.wasteQuantity[Constants.PATH_FOR_LOADING] instanceof Promise) {
				return <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else {
				let wasteQuantitiesData = [];
				if(this.props.wasteQuantity instanceof Array) {
					wasteQuantitiesData = this.props.wasteQuantity.map((elem) => ({
						prevPresent: elem[0] || 0,
						currGenerated: elem[1] || 0,
						currProcessed: elem[2] || 0,
						currPresent: elem[3] || 0
					}));
				}
				return <div className='page-body'>
						<ReactTable
							className='clients-table align-center-table -striped -highlight'
							data={wasteQuantitiesData}
							columns={[{
									Header: this.props.t("Reports.prevPresent"),
									accessor: 'prevPresent'
								},{
									Header: this.props.t("Reports.treatedTonnes"),
									Cell: props => <span>0</span>
								},{
									Header: this.props.t("Reports.treatmentDesc"),
									Cell: props => <span>0</span>
								},{
									Header: this.props.t("Reports.currGenerated"),
									accessor: 'currGenerated'
								},{
									Header: this.props.t("Reports.currProcessed"),
									accessor: 'currProcessed'
								},{
									Header: this.props.t("Reports.currPresent"),
									accessor: 'currPresent'
								},{
									Header: this.props.t("Reports.storageTime"),
									Cell: props => <span>0</span>
								},
							]}
							defaultPageSize={10}
							showPagination={true}
						/>
					</div>;
			}
		}
		return undefined;
	}
	
	renderVehiclesProfitPerKm() {
		let columns = getExpandedColumns("vehicles", 
				["allocationProxies.consumerAllocationRecords.allocationProducer","allocationProxies.consumerAllocationRecords.allocationProducer.attachable","allocationProxies.consumerAllocationRecords.allocationProducer.article.articlePriceRates"], 
				{});
		let incomeColumns = getExpandedColumns("vehicles", 
				["transportsForTractors","transportsForTractors.invoiceRows","transportsForTractors.invoiceRows.invoice","transportsForTractors.invoiceRows.invoice.invoiceCurrency.articlePriceRates"], 
				{});
//		incomeColumns.push({
//						Header: "Общо Км",
//						accessor: "sumKm",
//						isAggregate: true,
//						filterable: true,
//						sortable: false,
//						aggregation: {
//							op: "sum",
//							operands: ["transportsForTractors.distanceKm"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					})
		let tempData = [];
		if(this.props.data 
				&& this.props.data.expenditures
				&& this.props.data.expenditures.pageData
				&& this.props.data.expenditures.pageData._embedded
				&& this.props.data.expenditures.pageData._embedded.vehicles) 
		{
			this.props.data.expenditures.pageData._embedded.vehicles.forEach((item,index) => {
				if(item.allocationProxies.consumerAllocationRecords.allocationProducer.quantityToAllocate !== undefined) {
					tempData.push({name: item.licensePlate, date: item.allocationProxies.consumerAllocationRecords.allocationProducer.attachable.expenseDate, income: 0, expenditure: -item.allocationProxies.consumerAllocationRecords.allocationProducer.quantityToAllocate*(item.allocationProxies.consumerAllocationRecords && item.allocationProxies.consumerAllocationRecords.allocationProducer && item.allocationProxies.consumerAllocationRecords.allocationProducer.article && item.allocationProxies.consumerAllocationRecords.allocationProducer.article.articlePriceRates ? item.allocationProxies.consumerAllocationRecords.allocationProducer.article.articlePriceRates.price : 1), distance: 0});
				}
			});
		}
		if(this.props.data 
				&& this.props.data.incomes
				&& this.props.data.incomes.pageData
				&& this.props.data.incomes.pageData._embedded
				&& this.props.data.incomes.pageData._embedded.vehicles) 
		{
			this.props.data.incomes.pageData._embedded.vehicles.forEach((item,index) => {
				if(item.transportsForTractors.invoiceRows.priceRate !== undefined) {
					tempData.push({name: item.licensePlate, date: item.transportsForTractors.invoiceRows.invoice.invoiceDate, income: item.transportsForTractors.invoiceRows.priceRate*(item.transportsForTractors && item.transportsForTractors.invoiceRows && item.transportsForTractors.invoiceRows.invoice && item.transportsForTractors.invoiceRows.invoice.invoiceCurrency && item.transportsForTractors.invoiceRows.invoice.invoiceCurrency.articlePriceRates ? item.transportsForTractors.invoiceRows.invoice.invoiceCurrency.articlePriceRates.price : 0), expenditure: 0, distance: item.transportsForTractors.distanceKm});
				}
			});
		}
		tempData.sort((a, b) => moment(a.date) - moment(b.date));
		let expenditureAgg = {};
		let incomeAgg = {};
		let kmAgg = {};
		let scatterData = [];
		tempData.forEach((item,itemIndex) => {
			let traceIndex = scatterData.findIndex(trace => trace.name == item.name);
			if(traceIndex == -1) {
				expenditureAgg[item.name] = 0;
				incomeAgg[item.name] = 0;
				kmAgg[item.name] = 0;
				scatterData.push({type: "scatter", 
					name: item.name,
					x: scatterData.length > 0 ? scatterData[0].x.slice() : [], 
					y: scatterData.length > 0 ? Array(scatterData[0].x.length).fill(null) : []
				});
			}
			expenditureAgg[item.name] = expenditureAgg[item.name] + item.expenditure;
			incomeAgg[item.name] = incomeAgg[item.name] + item.income;
			kmAgg[item.name] = kmAgg[item.name] + item.distance;
			scatterData.forEach((trace) => {
				let ammount = 0;
				switch(this.state.vehicleReportOption) {
					case "profitPerKm": 
						ammount = (kmAgg[trace.name] != 0 ? ((expenditureAgg[trace.name]+incomeAgg[trace.name])/kmAgg[trace.name]) : null);
						break;
					case "expenditurePerKm":
						ammount = (kmAgg[trace.name] != 0 ? ((expenditureAgg[trace.name])/kmAgg[trace.name]) : null);
						break;
					case "incomePerKm": 
						ammount = (kmAgg[trace.name] != 0 ? ((incomeAgg[trace.name])/kmAgg[trace.name]) : null);
						break;
					case "km": 
						ammount = kmAgg[trace.name];
						break;
					case "profit": 
						ammount = (expenditureAgg[trace.name]+incomeAgg[trace.name]);
						break;
					case "expenditure":
						ammount = (expenditureAgg[trace.name]);
						break;
					case "income": 
						ammount = (incomeAgg[trace.name]);
						break;
				}
				let date = moment(item.date).format("DD.MM.YYYY");
				if(trace.x[trace.x.length-1] != date) {
					trace.x.push(date);
					trace.y.push(ammount);
				} else {
					trace.y[trace.x.length-1] = ammount;
				}
			});
		});
		let barData = scatterData.map(trace => {
			return {type: "bar", 
				name: trace.name,
				x: trace.x.slice(),
				y: trace.y.map((item,itemIndex) => {
					return item - (itemIndex > 0 ? trace.y[itemIndex-1] : 0);
				}),
			};
		});
		let allData = scatterData.concat(barData);
		let body = <div>
				<Form>
					<Form.Row>
						<Col>
							<Form.Group controlId="reports.selectLines">
								<Form.Label>{this.props.t("Reports.Show")}: </Form.Label>
								<Form.Control as="select" onChange={(e) => {resetRESTCallLimit(); this.setState({vehicleReportOption: e.target.value});}}>
									<option value="profitPerKm">{this.props.t("Reports.profitPerKm")}</option>
									<option value="expenditurePerKm">{this.props.t("Reports.expenditurePerKm")}</option>
									<option value="incomePerKm">{this.props.t("Reports.incomePerKm")}</option>
									<option value="km">{this.props.t("Reports.km")}</option>
									<option value="profit">{this.props.t("Reports.profit")}</option>
									<option value="expenditure">{this.props.t("Reports.expenditure")}</option>
									<option value="income">{this.props.t("Reports.income")}</option>
								</Form.Control>
							</Form.Group>
						</Col>
						<Col>
							<Form.Group controlId="reports.fromDate">
								<Form.Label>{this.props.t("Reports.FromDate")}: </Form.Label>
								<DatePicker
									className="col-sm-12 form-control"
									value={this.state.vehicleFromDate ? new Date(this.state.vehicleFromDate) : undefined}
									locale="bg-BG"
									showLeadingZeros={true}
									calendarIcon={null}
									onChange={(e) => {resetRESTCallLimit(); this.setState({vehicleFromDate: e ? e.toISOString() : e});}} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
								/>
							</Form.Group>
						</Col>
						<Col>
							<Form.Group controlId="reports.toDate">
								<Form.Label>{this.props.t("Reports.ToDate")}: </Form.Label>
								<DatePicker
									className="col-sm-12 form-control"
									value={this.state.vehicleToDate ? new Date(this.state.vehicleToDate) : undefined}
									locale="bg-BG"
									showLeadingZeros={true}
									calendarIcon={null}
									onChange={(e) => {resetRESTCallLimit(); this.setState({vehicleToDate: e ? e.toISOString() : e});}} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
								/>
							</Form.Group>
						</Col>
					</Form.Row>
				</Form>
				<Plot
					data={allData}
					layout={ {autosize: true} }
				/>
				<EmbedRetrieveEntityListContainer
					key={this.props.match.params.selectedReport+"_expenditures"}
					title={this.props.t("Reports.VehiclesExpenditures")}
					icon={undefined}
					columns={columns}
					componentPath={"reports.vehicles_profit_per_km.expenditures"}
					retrieveType={"vehicles"}
					expanded={false}
					asTable={true}
					editable={false}
					excludeRootEntity={false}
					defaultSort={undefined}
					defaultPageSize={1000}
					defaultFilter={[
						{where: {
								op: "equal",
								operands: ["vehicleType.listOptionItemCode", {literal: 1}],
							},
						},
						{id: "allocationProxies.consumerAllocationRecords.allocationProducer.attachable.expenseDate",value: {fromDate: moment(this.state.vehicleFromDate), toDate: moment(this.state.vehicleToDate)}}]}
				/>
				<EmbedRetrieveEntityListContainer
					key={this.props.match.params.selectedReport+"_invoices"}
					title={this.props.t("Reports.VehiclesIncomes")}
					icon={undefined}
					columns={incomeColumns}
					componentPath={"reports.vehicles_profit_per_km.incomes"}
					retrieveType={"vehicles"}
					expanded={false}
					asTable={true}
					editable={false}
					excludeRootEntity={false}
					defaultSort={undefined}
					defaultPageSize={1000}
					defaultFilter={[
						{where: {
								op: "equal",
								operands: ["vehicleType.listOptionItemCode", {literal: 1}],
							},
						},
						{id: "transportsForTractors.invoiceRows.invoice.invoiceDate",value: {fromDate: moment(this.state.vehicleFromDate), toDate: moment(this.state.vehicleToDate)}}]}
				/>
			</div>;
		return body;
	}
	
	renderEmployeesIncomesAndExpenditures() {
		let columns = getExpandedColumns("employees", 
				["allocationProxies.consumerAllocationRecords.allocationProducer","allocationProxies.consumerAllocationRecords.allocationProducer.attachable","allocationProxies.consumerAllocationRecords.allocationProducer.article.articlePriceRates"], 
				{});
		let incomeColumns = getExpandedColumns("employees", 
				["transportsForDrivers.invoiceRows","transportsForDrivers.invoiceRows.invoice","transportsForDrivers.invoiceRows.invoice.invoiceCurrency.articlePriceRates"], 
				{});
		let tempData = [];
		if(this.props.data 
				&& this.props.data.expenditures
				&& this.props.data.expenditures.pageData
				&& this.props.data.expenditures.pageData._embedded
				&& this.props.data.expenditures.pageData._embedded.employees) 
		{
			this.props.data.expenditures.pageData._embedded.employees.forEach((item,index) => {
				if(item.allocationProxies.consumerAllocationRecords.allocationProducer.quantityToAllocate !== undefined) {
					tempData.push({name: item.employeeName, date: item.allocationProxies.consumerAllocationRecords.allocationProducer.attachable.expenseDate, ammount: -item.allocationProxies.consumerAllocationRecords.allocationProducer.quantityToAllocate*(item.allocationProxies.consumerAllocationRecords && item.allocationProxies.consumerAllocationRecords.allocationProducer && item.allocationProxies.consumerAllocationRecords.allocationProducer.article && item.allocationProxies.consumerAllocationRecords.allocationProducer.article.articlePriceRates ? item.allocationProxies.consumerAllocationRecords.allocationProducer.article.articlePriceRates.price : 1)});
				}
			});
		}
		if(this.props.data 
				&& this.props.data.incomes
				&& this.props.data.incomes.pageData
				&& this.props.data.incomes.pageData._embedded
				&& this.props.data.incomes.pageData._embedded.employees) 
		{
			this.props.data.incomes.pageData._embedded.employees.forEach((item,index) => {
				if(item.transportsForDrivers.invoiceRows.priceRate !== undefined) {
					tempData.push({name: item.employeeName, date: item.transportsForDrivers.invoiceRows.invoice.invoiceDate, ammount: item.transportsForDrivers.invoiceRows.priceRate*(item.transportsForDrivers && item.transportsForDrivers.invoiceRows && item.transportsForDrivers.invoiceRows.invoice && item.transportsForDrivers.invoiceRows.invoice.invoiceCurrency && item.transportsForDrivers.invoiceRows.invoice.invoiceCurrency.articlePriceRates ? item.transportsForDrivers.invoiceRows.invoice.invoiceCurrency.articlePriceRates.price : 0)});
				}
			});
		}
		tempData.sort((a, b) => moment(a.date) - moment(b.date));
		let chartData = []; //{type: 'bar', x: [], y: []};
		let lineData = []; //{type: 'scatter', x: [], y: []};
		let lineAgg = {};
		tempData.forEach((item,itemIndex) => {
			let traceIndex = chartData.findIndex(trace => trace.name == item.name);
			if(traceIndex == -1) {
				chartData.push({type: 'bar', 
						name: item.name,
						x: chartData.length > 0 ? chartData[0].x.slice() : [], 
						y: chartData.length > 0 ? Array(chartData[0].x.length).fill(0) : []
					});
			}
			chartData.forEach((trace) => {
				let ammount = trace.name == item.name ? item.ammount : 0;
				let date = moment(item.date).format("DD.MM.YYYY");
				let barIndex = trace.x.findIndex(xitem => xitem == date);
				if(barIndex == -1) {
					trace.x.push(date);
					trace.y.push(ammount);
				} else {
					trace.y[barIndex] = trace.y[barIndex] + ammount;
				}
			});
			
			traceIndex = lineData.findIndex(trace => trace.name == item.name);
			if(traceIndex == -1) {
				lineData.push({type: 'scatter', 
						name: item.name,
						x: lineData.length > 0 ? lineData[0].x.slice() : [], 
						y: lineData.length > 0 ? Array(lineData[0].x.length).fill(0) : []
					});
				lineAgg[item.name] = 0;
			}
			lineAgg[item.name] = lineAgg[item.name] + item.ammount;
			lineData.forEach((trace) => {
				let date = moment(item.date).format("DD.MM.YYYY");
				if(trace.x[trace.x.length-1] != date) {
					trace.x.push(date);
					trace.y.push(lineAgg[trace.name]);
				} else {
					trace.y[trace.x.length-1] = lineAgg[trace.name];
				}
			});
		});
		let allData = chartData.concat(lineData);
		let body = <div>
				<Form>
					<Form.Row>
						<Col>
							<Form.Group controlId="reports.fromDate">
								<Form.Label>{this.props.t("Reports.FromDate")}: </Form.Label>
								<DatePicker
									className="col-sm-12 form-control"
									value={this.state.employeeFromDate ? new Date(this.state.employeeFromDate) : undefined}
									locale="bg-BG"
									showLeadingZeros={true}
									calendarIcon={null}
									onChange={(e) => {resetRESTCallLimit(); this.setState({employeeFromDate: e ? e.toISOString() : e});}} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
								/>
							</Form.Group>
						</Col>
						<Col>
							<Form.Group controlId="reports.toDate">
								<Form.Label>{this.props.t("Reports.ToDate")}: </Form.Label>
								<DatePicker
									className="col-sm-12 form-control"
									value={this.state.employeeToDate ? new Date(this.state.employeeToDate) : undefined}
									locale="bg-BG"
									showLeadingZeros={true}
									calendarIcon={null}
									onChange={(e) => {resetRESTCallLimit(); this.setState({employeeToDate: e ? e.toISOString() : e});}} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
								/>
							</Form.Group>
						</Col>
					</Form.Row>
				</Form>
				<Plot
					data={allData}
					layout={ {autosize: true} }
				/>
				<EmbedRetrieveEntityListContainer
					key={this.props.match.params.selectedReport+"_expenditures"}
					title={this.props.t("Reports.EmployeesExpenditures")}
					icon={undefined}
					columns={columns}
					componentPath={"reports.employees_incomes_and_expenditures.expenditures"}
					retrieveType={"employees"}
					defaultFilter={[{id: "allocationProxies.consumerAllocationRecords.allocationProducer.attachable.expenseDate",value: {fromDate: moment(this.state.employeeFromDate), toDate: moment(this.state.employeeToDate)}}]}
					expanded={false}
					asTable={true}
					editable={false}
					excludeRootEntity={false}
					defaultSort={undefined}
					defaultPageSize={1000}
				/>
				<EmbedRetrieveEntityListContainer
					key={this.props.match.params.selectedReport+"_invoices"}
					title={this.props.t("Reports.EmployeesIncomes")}
					icon={undefined}
					columns={incomeColumns}
					componentPath={"reports.employees_incomes_and_expenditures.incomes"}
					retrieveType={"employees"}
					defaultFilter={[{id: "transportsForDrivers.invoiceRows.invoice.invoiceDate",value: {fromDate: moment(this.state.employeeFromDate), toDate: moment(this.state.employeeToDate)}}]}
					expanded={false}
					asTable={true}
					editable={false}
					excludeRootEntity={false}
					defaultSort={undefined}
					defaultPageSize={1000}
				/>
			</div>;
		return body;
	}
	
	renderIncomesAndExpenditures() {
		let expenditureColumns = getExpandedColumns("expenditures", 
				["attachableRevenuesAndExpenses","attachableRevenuesAndExpenses.article.articlePriceRates"], 
				{});
		let invoiceColumns = getExpandedColumns("invoices", 
				["invoiceCurrency.articlePriceRates"], 
				{});
		let tempData = [];
		if(this.props.data 
				&& this.props.data.expenditures
				&& this.props.data.expenditures.pageData
				&& this.props.data.expenditures.pageData._embedded
				&& this.props.data.expenditures.pageData._embedded.expenditures) 
		{
			this.props.data.expenditures.pageData._embedded.expenditures.forEach((item,index) => {
				if(item.attachableRevenuesAndExpenses.ammount !== undefined) {
					tempData.push({name: item.expenseType ? item.expenseType.listOptionItemName : "n/a", date: item.expenseDate, ammount: -item.attachableRevenuesAndExpenses.ammount*(item.attachableRevenuesAndExpenses && item.attachableRevenuesAndExpenses.article && item.attachableRevenuesAndExpenses.article.articlePriceRates ? item.attachableRevenuesAndExpenses.article.articlePriceRates.price : 1)});
				}
			});
		}
		if(this.props.data 
				&& this.props.data.invoices
				&& this.props.data.invoices.pageData
				&& this.props.data.invoices.pageData._embedded
				&& this.props.data.invoices.pageData._embedded.invoices) 
		{
			this.props.data.invoices.pageData._embedded.invoices.forEach((item,index) => {
				if(item.totalAmount !== undefined) {
					tempData.push({name: this.props.t("Reports.Invoiced"), date: item.invoiceDate, ammount: item.totalAmount*(item.invoiceCurrency && item.invoiceCurrency.articlePriceRates ? item.invoiceCurrency.articlePriceRates.price : 0)});
				}
			});
		}
		tempData.sort((a, b) => moment(a.date) - moment(b.date));
		let lineData = [{type: 'scatter', name: this.props.t("Reports.Profit/Loss"), x: [], y: []}]; //{type: 'scatter', x: [], y: []};
		let lineAgg = 0;
		let chartData = []; //{type: 'bar', x: [], y: []};
		tempData.forEach((item,index) => {
			let date = moment(item.date).format("DD.MM.YYYY");
			lineAgg = lineAgg + item.ammount;
			let barIndex = lineData[0].x.findIndex(xitem => xitem == date);
			if(barIndex == -1) {
				lineData[0].x.push(date);
				lineData[0].y.push(lineAgg);
			} else {
				lineData[0].y[barIndex] = lineAgg;
			}
			let traceIndex = chartData.findIndex(trace => trace.name == item.name);
			if(traceIndex == -1) {
				chartData.push({type: 'bar', 
						name: item.name,
						x: [], 
						y: []});
			}
		});
		tempData.forEach((item,index) => {
			chartData.forEach((trace) => {
				let date = moment(item.date).format("DD.MM.YYYY");
				let ammount = trace.name == item.name ? item.ammount : 0;
				let barIndex = trace.x.findIndex(xitem => xitem == date);
				if(barIndex == -1) {
					trace.x.push(date);
					trace.y.push(ammount);
				} else {
					trace.y[barIndex] = trace.y[barIndex] + ammount;
				}
			});
		});
		let allData = chartData.concat(lineData);
		let body = <div>
				<Form>
					<Form.Row>
						<Col>
							<Form.Group controlId="reports.fromDate">
								<Form.Label>{this.props.t("Reports.FromDate")}: </Form.Label>
								<DatePicker
									className="col-sm-12 form-control"
									value={this.state.profitFromDate ? new Date(this.state.profitFromDate) : undefined}
									locale="bg-BG"
									showLeadingZeros={true}
									calendarIcon={null}
									onChange={(e) => {resetRESTCallLimit(); this.setState({profitFromDate: e ? e.toISOString() : e});}} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
								/>
							</Form.Group>
						</Col>
						<Col>
							<Form.Group controlId="reports.toDate">
								<Form.Label>{this.props.t("Reports.ToDate")}: </Form.Label>
								<DatePicker
									className="col-sm-12 form-control"
									value={this.state.profitToDate ? new Date(this.state.profitToDate) : undefined}
									locale="bg-BG"
									showLeadingZeros={true}
									calendarIcon={null}
									onChange={(e) => {resetRESTCallLimit(); this.setState({profitToDate: e ? e.toISOString() : e});}} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
								/>
							</Form.Group>
						</Col>
					</Form.Row>
				</Form>
				<Plot
					data={allData}
					layout={ {autosize: true} }
				/>
				<EmbedRetrieveEntityListContainer
					key={this.props.match.params.selectedReport+"_expenditures"}
					title={this.props.t("Reports.expenditure")}
					icon={undefined}
					columns={expenditureColumns}
					componentPath={"reports.incomes_and_expenditures.expenditures"}
					retrieveType={"expenditures"}
					defaultFilter={[{id: "expenseDate",value: {fromDate: moment(this.state.profitFromDate), toDate: moment(this.state.profitToDate)}}]}
					expanded={false}
					asTable={true}
					editable={false}
					excludeRootEntity={false}
					defaultSort={undefined}
					defaultPageSize={1000}
				/>
				<EmbedRetrieveEntityListContainer
					key={this.props.match.params.selectedReport+"_invoices"}
					title={this.props.t("Reports.Invoiced")}
					icon={undefined}
					columns={invoiceColumns}
					componentPath={"reports.incomes_and_expenditures.invoices"}
					retrieveType={"invoices"}
					defaultFilter={[{id: "invoiceDate",value: {fromDate: moment(this.state.profitFromDate), toDate: moment(this.state.profitToDate)}}]}
					expanded={false}
					asTable={true}
					editable={false}
					excludeRootEntity={false}
					defaultSort={undefined}
					defaultPageSize={1000}
				/>
			</div>;
		return body;
	}

	render() {
		const report = reports.find((item) => item.value == this.props.match.params.selectedReport);
		let body;
		let pivot;
		if(this.props.match.params.selectedReport == 'wasteQuantity'){
			body = this.renderWasteQuantity();
		}
		else if(this.props.match.params.selectedReport == 'timeSheetsPerDay'){
			body = this.renderTimeSheetsPerDay();
		}
		else if(this.props.match.params.selectedReport == 'employees_incomes_and_expenditures'){
			body = this.renderEmployeesIncomesAndExpenditures();
		}
		else if(this.props.match.params.selectedReport == 'incomes_and_expenditures'){
			body = this.renderIncomesAndExpenditures();
		}
		else if(this.props.match.params.selectedReport == 'vehicles_profit_per_km'){
			body = this.renderVehiclesProfitPerKm();
		}
		else if(report) {
			let columns = report.columns;
			if(columns == undefined) {
				columns = getExpandedColumns(report.root, report.expand, report.columnOverride);
			}
			body = report.body;
			if(body === undefined) {
				body = <EmbedRetrieveEntityListContainer
						key={this.props.match.params.selectedReport}
						title={report.label}
						icon={report.icon}
						columns={columns}
						componentPath={"reports."+this.props.match.params.selectedReport}
						retrieveType={report.root}
						defaultFilter={report.filter}
						expanded={true}
						asTable={true}
						editable={false}
						excludeRootEntity={report.excludeRootEntity}
						defaultSort={report.sort}
					/>;
			}
//			pivot = <PivotTableUI
//					data={this.props.pivot_data}
//					onChange={s => this.setState({pivotProps: s})}
//					{...this.state.pivotProps}
//					renderers={Object.assign({}, TableRenderers, PlotlyRenderers)}
//					report={report.value}
//				/>;
			pivot = null;
		}
		return (
			<div className="reports-wrapper">
				<Header text={this.props.t("Reports.title")} class='page-header text-align-center no-margin' />
				<div className='page-body'>
					<div className="col-sm-12 form-group">
						<Select
							className="col-sm-12 no-padding"
							name="form-field-name"
							options={reports}
							onChange={(e) => {resetRESTCallLimit(); history.push('/reports/'+e.value);}}
							value={report}
							placeholder={this.props.t("Reports.ChooseReport")}
						/>
					</div>
					<div className='col-sm-12'>
						{body}
					</div>
					<div className='col-sm-12'>
						{pivot}
					</div>
				</div>
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath("reports."+ownProps.match.params.selectedReport,state.rest); //rest because of fetchREST
	const report = reports.find((item) => item.value == ownProps.match.params.selectedReport);
	let pivot_data = data && data.pageData && data.pageData._embedded && data.pageData._embedded[report.root] instanceof Array ? data.pageData._embedded[report.root] : [];
	const wasteQuantity = state.rest.reports ? state.rest.reports.wasteQuantity : undefined;
	const timeSheetsPerDay = state.rest.reports ? state.rest.reports.timeSheetsPerDay : undefined;
	return {
		auth: state.auth,
		wasteQuantity,
		timeSheetsPerDay,
		data: data,
		pivot_data: pivot_data,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchCleanRESTData, postRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageReportsContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
