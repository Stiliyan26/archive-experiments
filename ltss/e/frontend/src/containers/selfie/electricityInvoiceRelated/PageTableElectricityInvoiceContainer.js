import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { Button, Tab, Tabs, Alert, Card, Form, Dropdown } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';
import axios from "axios";
import Plot from 'react-plotly.js';

import history from '../../../scripts/history.js';
import { isAuthenticated } from '../../../components/pages/login/Login.js';

import NewHeader from '../../../components/generic/NewHeader.js';
import EmbedRetrieveEntityListContainer from '../../nomenclatures/EmbedRetrieveEntityListContainer.js';

import { getEntityDefinition, getExpandedColumns } from '../../nomenclatures/entityDefinitions.js';
import { resolveObjectPath } from '../../../scripts/dataUtils.js';
import { patchRESTMultiData } from '../../../actions/taskActions.js';
import NepalDBFileContainer from '../../nepal/NepalDBFileContainer.js';
import { dispatchEditRESTData } from "../../../actions/taskActions.js";
import JSZip from "jszip";
import XLSX from 'xlsx';

import '../customTab.css';
import '../customAlert.css';

import { X_AUTH_TOKEN, X_AUTH_KEY, ELECTRICITY_INVOICE_SUBMENUS_OPTIONS, DOCUMENT_TYPE_COLORS, COLOR_MAPPINGS } from "../constants/selfieConstants.js";
import { getLastDayOfMonthStr } from "../constants/selfieUtilFunctions.js";
import { selfieEntities } from "../constants/selfieEntities.js";
import { getCurrentDate, formatMonthStr } from "../constants/selfieUtilFunctions.js";
import { GENERATE_PDF_DOCUMENTS, POPULATE_ELECTRICITY_INVOICE, LOGIN, ACCOUNTING_PERIOD_FETCH, ELECTRICITY_INVOICES_FETCH, AGREEMENT_TYPES_FETCH, FETCH_IMPORT_VALUES_BY_FILTER_PERIOD, FETCH_IMPORT_QUANTITIES_BY_FILTER_PERIOD, FETCH_IMPORT_VALUES_QUANTITIES_BY_FILTER_PERIOD } from "../constants/selfiePaths.js";
import PageTableInvoiceCorrectionContainer from "./PageTableInvoiceCorrectionContainer.js";


// Page: can be used as a landing page
// Table: presents table of the objects
// Container: redux container class
class PageTableElectricityInvoiceContainer extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            monthIndex: 0,

            // Error vars
            showAlert: false,
            alertMessageType: "",
            showPreRequestAlert: false,
            alertSuccess: false,

            preRequestAlertMessage: "",
            errorArray: [],

            // Fetch Dsiplay data vars
            numberOfInvoices: 0,
            numberOfDebitInvoices: 0,
            numberOfCreditInvoices: 0,
            selectedDocumentType: 1,
            agreementTypes: [],
            powerPlants: [],
            importReportingPoints: new Set(),

            // Agreement Type related
            toggledAgreementTypesOptions: [],
            toggledPowerPlantOptions: [],
            showAgreementTypeDropdown: false,
            showPowerPlantDropdown: false
        }
    }

    componentDidMount() {
        if (!isAuthenticated(this.props.auth)) {
            history.push(LOGIN);
        }

        this.getAccountingPeriod();
        this.fetchDisplayDataElectricityInvoices();
        this.fetchAgreementTypes();
    }

    populateElectricityInvoice() {
        const periodFromStr = this.refs.periodFrom.value;
        const periodToStr = this.refs.periodTo.value;
        const taxEventDateStr = this.refs.taxEventDate.value;
        const taxEventDate = new Date(taxEventDateStr);
        const documentTypeCode = this.state.selectedDocumentType;

        const agreementTypes = this.state.toggledAgreementTypesOptions;
        const powerPlants = this.state.toggledPowerPlantOptions;

        console.log("agreementTypes:", agreementTypes);
        console.log("powerPlants:", powerPlants);

        // checking for empty values
        if (periodFromStr === "" || periodToStr === "") {
            return this.setState({ showPreRequestAlert: true, preRequestAlertMessage: `${this.props.t('ErrorMessages.PeriodFromAndPeriodToCannotBeEmpty')}` });
        }

        if (agreementTypes.length === 0) {
            return this.setState({ showPreRequestAlert: true, preRequestAlertMessage: `${this.props.t('Selfie.NoAgreementsSelected')}!` });
        }

        if (powerPlants.length === 0) {
            return this.setState({ showPreRequestAlert: true, preRequestAlertMessage: `${this.props.t('Selfie.NoPowerPlantsSelected')}!` });
        }

        const getPeriodFromMonthIndex = new Date(periodFromStr).getMonth() + 1;
        const getPeriodtoMonthIndex = new Date(periodToStr).getMonth() + 1;

        console.log("populateElectricityInvoice -> getPeriodFromMonthIndex: ", getPeriodFromMonthIndex);
        console.log("populateElectricityInvoice -> getPeriodtoMonthIndex: ", getPeriodtoMonthIndex);

        // pre request validations
        if (getPeriodFromMonthIndex != this.state.monthIndex) {
            this.setState({ showPreRequestAlert: true, preRequestAlertMessage: `${this.props.t('ErrorMessages.PeriodFromNotInAccountingPeriod')}` });

        } else if (getPeriodtoMonthIndex != this.state.monthIndex) {
            this.setState({ showPreRequestAlert: true, preRequestAlertMessage: `${this.props.t('ErrorMessages.PeriodToNotInAccountingPeriod')}` });

        } else if (new Date(periodFromStr) > new Date(periodToStr)) {
            this.setState({ showPreRequestAlert: true, preRequestAlertMessage: `${this.props.t('ErrorMessages.InvalidPeriodFromPeriodTo')}` });

        } else {
            const fiveDaysAgo = new Date();
            fiveDaysAgo.setDate(fiveDaysAgo.getDate() - 6);

            if (taxEventDate < fiveDaysAgo) {
                return this.setState({ showPreRequestAlert: true, preRequestAlertMessage: `${this.props.t('ErrorMessages.TaxEventCannotBeMoreThan5DaysBefore')}` });
            }

            this.setState({ uploadProducedEnergyList: true });

            axios({
                method: "POST",
                url: API_URL + POPULATE_ELECTRICITY_INVOICE,
                data: {
                    periodFrom: periodFromStr,
                    periodTo: periodToStr,
                    taxEventDate: taxEventDateStr,
                    documentTypeCode: documentTypeCode,
                    agreementTypeCodes: agreementTypes,
                    powerPlantIds: powerPlants
                },
                headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
            })
                .then((res) => {
                    let data = res.data;
                    console.log('PageTableElectricityInvoiceContainer->populateElectricityInvoice()->data: ', data);

                    if (data.length !== 0 && !data.every(obj => Object.keys(obj).length === 0)) {
                        data
                            .forEach((element) => {
                                console.log("element: ", element);

                                let objectKey = Object.keys(element)[0];
                                if (objectKey !== undefined) {
                                    console.log("objectKey: ", objectKey);

                                    let objectValue = element[`${objectKey}`][0];
                                    console.log("objectValue: ", objectValue);

                                    let errorObject = {
                                        row: objectKey.toString(),
                                        message: objectValue.toString()
                                    }

                                    this.setState((prevState) => ({
                                        errorArray: [...prevState.errorArray, errorObject]
                                    }));
                                }
                            });

                        this.setState({ alertMessageType: "Populate", showAlert: true });
                    }

                    // Force refresh
                    this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
                    this.fetchDisplayDataElectricityInvoices();
                })
                .catch(error => {
                    console.log("PageTableElectricityInvoiceContainer.axios.error", error);
                });
        }
    }

    generatePDFdocuments() {
        this.setState({ uploadProducedEnergyList: true });

        const url = API_URL + GENERATE_PDF_DOCUMENTS;

        axios({
            method: "GET",
            url: url,
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then((res) => {
                console.log("Success Response: ", res);

                let data = res.data;

                if (Object.keys(data).length != 0) {
                    this.setState((prevState) => ({
                        ...prevState,
                        errorArray: [] // clean up the error state
                    }));

                    Object.keys(data)
                        .forEach(invalidRow => {
                            console.log("Invalid Row: " + invalidRow);

                            data[invalidRow]
                                .forEach((errorMessage) => {

                                    console.log(invalidRow + ": " + this.props.t(`${selfieEntities.ErrorMessages.pluralPascalCase}.${errorMessage}`));
                                    // console.log(invalidRow + ": " + errorMessage);

                                    let errorObject = {
                                        row: invalidRow,
                                        message: this.props.t(`${selfieEntities.ErrorMessages.pluralPascalCase}.${errorMessage}`)
                                    };

                                    this.setState((prevState) => ({
                                        errorArray: [...prevState.errorArray, errorObject]
                                    }));
                                });
                        });

                    this.setState({ alertMessageType: "GenerateDocuments", showAlert: true });

                } else {
                    this.setState((prevState) => ({
                        ...prevState,
                        alertSuccess: true,
                    }));
                }

                // Force refresh
                this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
            })
            .catch(error => console.log("Error Generating the PDF documents: ", error));
    }

    getAccountingPeriod() {
        axios({
            method: "get",
            url: API_URL + ACCOUNTING_PERIOD_FETCH,
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(res => {
                let data = res.data._embedded.hashMaps;
                let index = data[0]["AccountingPeriod"].code;

                this.setState({ monthIndex: index });

                console.log("getAccountingPeriod() -> this.state.monthIndex: ", this.state.monthIndex);

                this.fetchPowerPlants(); // After successfully getting the accounting period -> fetch by filtering the imports and retrieve their power plants
            })
            .catch(error => {
                console.log("PageTableElectricityInvoiceContainer.axios.error" + error);
            });
    }

    fetchAgreementTypes() {
        axios({
            method: "GET",
            url: API_URL + AGREEMENT_TYPES_FETCH,
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(res => {
                // Safely access and map the data
                const agreementTypes = res.data._embedded.hashMaps.map(item => {
                    const { id, code, text } = item.AgreementType;
                    return { id, code, text };
                });

                this.setState((prevState) => { return { ...prevState, agreementTypes: agreementTypes }; });
            })
            .catch(error => {
                console.log("Error fetching the Agreement Types: ", error);
            });
    }

    toggleAgreementTypeDropdown() {
        this.setState((prevState) => ({
            showAgreementTypeDropdown: !prevState.showAgreementTypeDropdown,
        }));
    }

    selectDropdownAgreementTypeOptions(optionValue) {
        if (this.state.toggledAgreementTypesOptions.includes(optionValue)) {
            this.setState((prevState) => {
                return {
                    ...prevState,
                    toggledAgreementTypesOptions: prevState.toggledAgreementTypesOptions.filter((item) => item !== optionValue),
                };
            });

        } else {
            this.setState((prevState) => {
                return {
                    ...prevState,
                    toggledAgreementTypesOptions: [...prevState.toggledAgreementTypesOptions, optionValue],
                };
            });
        }
    }

    fetchPowerPlants() {
        const currentDate = new Date();
        const isPeriodToMonthSmallerOrEqualToAccountingPeriodMonth = currentDate.getMonth() + 1 < Number(this.state.monthIndex);
        
        // Check whether the period is in the Future (if it is, get the previous year)
        const periodFromDateStr = `${currentDate.getFullYear() - (isPeriodToMonthSmallerOrEqualToAccountingPeriodMonth ? 1 : 0)}-${formatMonthStr(this.state.monthIndex)}-01`;
        const periodToDateStr = `${currentDate.getFullYear() - (isPeriodToMonthSmallerOrEqualToAccountingPeriodMonth ? 1 : 0)}-${formatMonthStr(this.state.monthIndex)}-${getLastDayOfMonthStr(this.state.monthIndex)}`;
        
        axios({
            method: "GET",
            url: API_URL + FETCH_IMPORT_VALUES_BY_FILTER_PERIOD(periodFromDateStr, periodToDateStr),
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(res => {
                if (res.data.hasOwnProperty("_embedded")) {
                    const data = res.data._embedded.hashMaps;

                    if (data.length !== 0) {
                        const uniqueImportValuesPowerPlants = data
                            .filter(importValObj => importValObj["ImportValue.powerPlant"] != null)
                            .map(importValObj => importValObj["ImportValue.powerPlant"])
                            .filter((obj, index, self) =>
                                index === self.findIndex(o => o.id === obj.id)); // removing duplicate PowerPlants from this import type fetch

                        // console.log("uniqueImportValuesPowerPlants", uniqueImportValuesPowerPlants);

                        this.setState((prevState) => ({
                            ...prevState,
                            powerPlants: [
                                ...prevState.powerPlants,
                                ...uniqueImportValuesPowerPlants.filter(powerPlant => !prevState.powerPlants.map(p => p.id).includes(powerPlant.id))
                            ]
                        })); // adding only non-existing PowerPlants
                    }
                }
            })
            .catch(error => {
                console.log("PageTableElectricityInvoiceContainer.axios.error" + error);
            });


        axios({
            method: "GET",
            url: API_URL + FETCH_IMPORT_QUANTITIES_BY_FILTER_PERIOD(periodFromDateStr, periodToDateStr),
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(res => {
                if (res.data.hasOwnProperty("_embedded")) {
                    const data = res.data._embedded.hashMaps;

                    if (data.length !== 0) {
                        const uniqueImportQuantitiesPowerPlants = data
                            .filter(importQuaObj => importQuaObj["ImportQuantity.powerPlant"] != null)
                            .map(importQuaObj => importQuaObj["ImportQuantity.powerPlant"])
                            .filter((obj, index, self) =>
                                index === self.findIndex(o => o.id === obj.id)); // removing duplicate PowerPlants from this import type fetch

                        // console.log("uniqueImportQuantitiesPowerPlants", uniqueImportQuantitiesPowerPlants);

                        this.setState((prevState) => ({
                            ...prevState,
                            powerPlants: [
                                ...prevState.powerPlants,
                                ...uniqueImportQuantitiesPowerPlants.filter(powerPlant => !prevState.powerPlants.map(p => p.id).includes(powerPlant.id))
                            ]
                        })); // adding only non-existing PowerPlants
                    }
                }
            })
            .catch(error => {
                console.log("PageTableElectricityInvoiceContainer.axios.error" + error);
            });


        axios({
            method: "GET",
            url: API_URL + FETCH_IMPORT_VALUES_QUANTITIES_BY_FILTER_PERIOD(periodFromDateStr, periodToDateStr),
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(res => {
                if (res.data.hasOwnProperty("_embedded")) {
                    const data = res.data._embedded.hashMaps;
                    console.log("Data: ", data);

                    if (data.length !== 0) {
                        const uniqueImportValuesAndQuantitiesPowerPlants = data
                            .filter(importValAndQuaObj => importValAndQuaObj["ImportValueAndQuantity.powerPlant"] != null)
                            .map(importValAndQuaObj => importValAndQuaObj["ImportValueAndQuantity.powerPlant"])
                            .filter((obj, index, self) =>
                                index === self.findIndex(o => o.id === obj.id)); // removing duplicate PowerPlants from this import type fetch

                        // console.log("uniqueImportValuesAndQuantitiesPowerPlants", uniqueImportValuesAndQuantitiesPowerPlants);

                        this.setState((prevState) => ({
                            ...prevState,
                            powerPlants: [
                                ...prevState.powerPlants,
                                ...uniqueImportValuesAndQuantitiesPowerPlants.filter(powerPlant => !prevState.powerPlants.map(p => p.id).includes(powerPlant.id))
                            ]
                        })); // adding only non-existing PowerPlants
                    }
                }
            })
            .catch(error => {
                console.log("PageTableElectricityInvoiceContainer.axios.error" + error);
            });
    }

    togglePowerPlantDropdown() {
        this.setState((prevState) => ({
            showPowerPlantDropdown: !prevState.showPowerPlantDropdown,
        }));
    }

    selectDropdownPowerPlantOptions(optionValue) {
        if (this.state.toggledPowerPlantOptions.includes(optionValue)) {
            this.setState((prevState) => {
                return {
                    ...prevState,
                    toggledPowerPlantOptions: prevState.toggledPowerPlantOptions.filter((item) => item !== optionValue),
                };
            });

        } else {
            this.setState((prevState) => {
                return {
                    ...prevState,
                    toggledPowerPlantOptions: [...prevState.toggledPowerPlantOptions, optionValue],
                };
            });
        }
    }

    fetchDisplayDataElectricityInvoices() {
        axios({
            method: "get",
            url: API_URL + ELECTRICITY_INVOICES_FETCH,
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(res => {
                if (res.data.hasOwnProperty("_embedded")) { // case where there are entries
                    let data = res.data._embedded.hashMaps;

                    if (data.length != 0) {
                        this.setState((prevState) => ({
                            ...prevState,
                            numberOfInvoices: 0,
                            numberOfDebitInvoices: 0,
                            numberOfCreditInvoices: 0,
                        }));

                        data
                            .forEach(element => {
                                let documentTypeCode = element["ElectricityInvoice.loiDocumentType"].listOptionItemCode;
                                console.log("documentTypeCode: ", documentTypeCode);

                                if (documentTypeCode == 1) {
                                    this.setState((prevState) => ({
                                        ...prevState,
                                        numberOfInvoices: prevState.numberOfInvoices + 1
                                    }));

                                } else if (documentTypeCode == 2) {
                                    this.setState((prevState) => ({
                                        ...prevState,
                                        numberOfDebitInvoices: prevState.numberOfDebitInvoices + 1
                                    }));

                                } else if (documentTypeCode == 3) {
                                    this.setState((prevState) => ({
                                        ...prevState,
                                        numberOfCreditInvoices: prevState.numberOfCreditInvoices + 1
                                    }));
                                }
                            });
                    }
                }
            })
            .catch(error => {
                console.log("PageTableElectricityInvoiceContainer.axios.error" + error);
            });
    }

    getDocumentTypesPieChartData() {
        const data = [{
            marker: { colors: DOCUMENT_TYPE_COLORS },
            values: [this.state.numberOfInvoices, this.state.numberOfDebitInvoices, this.state.numberOfCreditInvoices],
            labels: [this.props.t("Selfie.DocumentTypeInvoice"), this.props.t("Selfie.DocumentTypeDebitNote"), this.props.t("Selfie.DocumentTypeCreditNote")],
            // texttemplate: "%{value} MWh",
            textposition: "outside",
            hole: .4,
            type: 'pie'
        }];

        return data;
    }

    getExcelBase64() {
        let data = []; // matrix
        let header = [
            this.props.t("ElectricityInvoice.reportingPointOwn"),
            this.props.t("ElectricityInvoice.periodFrom"),
            this.props.t("ElectricityInvoice.periodTo"),
            this.props.t("ElectricityInvoice.loiDocumentType"),
            this.props.t("Error.Error")
        ]; // header row of the Excel file
        data.push(header);

        for (let i = 0; i < this.state.errorArray.length; i++) { // iterating and adding the rows of errors to the matrix
            const electricityInvoiceRow = this.state.errorArray[i].row.split("-");
            let innerArray = [electricityInvoiceRow[0], electricityInvoiceRow[1], electricityInvoiceRow[2], electricityInvoiceRow[3]];
            innerArray.push(this.state.errorArray[i].message);
            data.push(innerArray);
        }

        const workbook = XLSX.utils.book_new();
        const worksheet1 = XLSX.utils.aoa_to_sheet(data);

        const colWidths = data[0]
            .map((_, colIndex) => { // calculating the width of every single column
                const maxLength = data
                    .reduce((max, row) => {
                        const cellValue = row[colIndex] != null ? row[colIndex].toString() : "";

                        return Math.max(max, cellValue.length);
                    }, 0);

                return { wch: maxLength + 2 };
            });

        worksheet1["!cols"] = colWidths; // setting the width of each column

        const SHEET_NAME = "Sheet1";
        XLSX.utils.book_append_sheet(workbook, worksheet1, SHEET_NAME);

        this.setState({ errorArray: [] });

        const excelBuffer = XLSX.write(workbook, { bookType: "xlsx", type: "array" });
        const blob = new Blob([excelBuffer], { type: "application/octet-stream" }); // creating a blob of the Excel file 

        const link = document.createElement("a");
        link.href = URL.createObjectURL(blob);

        const EXCEL_FILE_NAME = "ErrorMessages";
        link.download = `${EXCEL_FILE_NAME}.xlsx`;
        link.click(); // downloading the file for the client
    }

    render() {
        // Columns for the populate sub-view
        let populateColumns = getEntityDefinition(this.props.entityName, {
            hasDbFile: { show: "hidden" },
            pdfDocument: { show: "hidden" },
            invoiceNumber: { show: "hidden" },
            invoiceCorrection: { show: "hidden" }
        }).columns;

        // Columns for the other two sub-views
        let columns = getEntityDefinition(this.props.entityName, {
            isValid: { show: "hidden" },
            hasDbFile: { show: "hidden" },
            invoiceCorrection: { show: "hidden" }
        }).columns;

        // console.log("this.props: ", this.props);

        let notification1 = "";
        if (this.state.showPreRequestAlert == true) {
            notification1 =
                <Alert className="pulsating-red-border" onClose={() => this.setState({ showPreRequestAlert: false })} dismissible>
                    <Alert.Heading>{this.props.t("Error.Error")}</Alert.Heading>
                    {this.state.preRequestAlertMessage}
                </Alert>
        }

        let notification2 = "";
        if (this.state.showAlert == true) {
            notification2 =
                <Alert className="pulsating-red-border" onClose={() => this.setState({ showAlert: false })} dismissible>
                    <Alert.Heading>{this.props.t("Error.Error")}</Alert.Heading>
                    <Alert.Link style={{ color: "red" }} onClick={() => { this.getExcelBase64(); }}>{`${this.props.t('ErrorMessages.SomeEntries')}`}</Alert.Link> {this.state.alertMessageType === "Populate" ? this.props.t("ErrorMessages.AreNotPopulatedSuccessfully") : this.props.t("ErrorMessages.AreNotGeneratedSuccessfully")}.
                </Alert>
        }

        let notification3 = "";
        if (this.state.alertSuccess == true) {
            notification3 =
                <Alert className="pulsating-green-border" onClose={() => this.setState({ alertSuccess: false })} dismissible>
                    <Alert.Heading>{this.props.t("GenerateFilesWasSuccessful")}</Alert.Heading>
                </Alert>
        }

        return (
            <div className="page-body-wrapper">
                {notification1}
                {notification2}
                {notification3}
                <NewHeader text={this.props.title} auth={this.props.auth.userAuthenticated} />

                <div className='page-body'>

                    <div className='my-custom-tabs'>

                        <Tabs
                            id="controlled-tab-example"
                            className="mb-3"
                            justify>


                            {/* Populate Tab*/}
                            <Tab
                                eventKey={ELECTRICITY_INVOICE_SUBMENUS_OPTIONS.TAB_KEY_POPULATE_ELECTRICITY_INVOICE}
                                title={this.props.t("Selfie.PopulateElectricityInvoice")}
                                unmountOnExit
                            >
                                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gridGap: "1px", marginTop: "1.5%", marginBottom: "1%" }}>
                                    <Plot
                                        data={this.getDocumentTypesPieChartData()}
                                        layout={{
                                            // title: "Document Type Distribution",
                                            height: 300,
                                            width: 400,
                                            // responsive: true,
                                            margin: { "t": 10, "b": 10, "l": 50, "r": 30 },
                                            padding: { "l": 10 },
                                            legend: {
                                                x: 1,
                                                y: 0
                                            },
                                            paper_bgcolor: COLOR_MAPPINGS.BACKGROUND,
                                            annotations: [{ showarrow: false, text: '' }]
                                        }}
                                    />

                                    <div>
                                        <Card>
                                            <Card.Header>{this.props.t("Selfie.DocumentGenerationPanel")}</Card.Header>

                                            {/* Filters for the generated Electricity Invoices */}
                                            <Card.Body>
                                                <Form.Row style={{ display: "flex", justifyContent: "space-between" }}>
                                                    <Form.Group>
                                                        <Form.Label>{this.props.t("ElectricityInvoice.periodFrom")}:</Form.Label>
                                                        <Form.Control type="date" required ref="periodFrom" />
                                                    </Form.Group>
                                                    <Form.Group>
                                                        <Form.Label>{this.props.t("ElectricityInvoice.periodTo")}:</Form.Label>
                                                        <Form.Control type="date" required ref="periodTo" />
                                                    </Form.Group>
                                                    <Form.Group>
                                                        <Form.Label>{this.props.t("Selfie.TaxEventDate")}:</Form.Label>
                                                        <Form.Control type="date" required ref="taxEventDate" defaultValue={getCurrentDate()} />
                                                    </Form.Group>
                                                    <Form.Group style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                                                        <Form.Label>{this.props.t("ElectricityInvoice.loiDocumentType")}:</Form.Label>

                                                        <Dropdown>
                                                            <Dropdown.Toggle variant="danger" id='dropdown-style' style={{ fontWeight: "300" }}>
                                                                {this.state.selectedDocumentType === 1 ? this.props.t("Selfie.DocumentTypeInvoice") :
                                                                    (this.state.selectedDocumentType === 2 ? this.props.t("Selfie.DocumentTypeDebitNote") :
                                                                        this.props.t("Selfie.DocumentTypeCreditNote"))
                                                                }
                                                            </Dropdown.Toggle>

                                                            <Dropdown.Menu>
                                                                {
                                                                    [
                                                                        { value: 1, title: this.props.t("Selfie.DocumentTypeInvoice") },
                                                                        { value: 2, title: this.props.t("Selfie.DocumentTypeDebitNote") },
                                                                        { value: 3, title: this.props.t("Selfie.DocumentTypeCreditNote") },
                                                                    ]
                                                                        .map(obj => (
                                                                            <Dropdown.Item
                                                                                key={obj.value}
                                                                                as="button"
                                                                                onClick={() => this.setState({ selectedDocumentType: obj.value })}
                                                                            >
                                                                                {obj.title}
                                                                            </Dropdown.Item>
                                                                        ))
                                                                }
                                                            </Dropdown.Menu>
                                                        </Dropdown>
                                                    </Form.Group>
                                                    <Form.Group style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                                                        <Form.Label>{this.props.t("ElectricityInvoice.agreementType")}:</Form.Label>
                                                        <Dropdown show={this.state.showAgreementTypeDropdown}>
                                                            <Dropdown.Toggle
                                                                variant="danger"
                                                                id='dropdown-style'
                                                                style={{ fontWeight: "300" }}
                                                                onClick={() => this.toggleAgreementTypeDropdown()}
                                                            >
                                                                {this.state.showAgreementTypeDropdown ? this.props.t("CloseOptions") : this.props.t("SelectOptions")}
                                                            </Dropdown.Toggle>

                                                            <Dropdown.Menu>
                                                                {this.state.agreementTypes.map((optionObj) => (
                                                                    <Dropdown.Item
                                                                        key={optionObj.code}
                                                                        as="button"
                                                                        onMouseDown={(e) => {
                                                                            e.preventDefault();
                                                                            this.selectDropdownAgreementTypeOptions(optionObj.code);
                                                                        }}
                                                                        active={
                                                                            this.state.toggledAgreementTypesOptions.includes(optionObj.code)}
                                                                    >
                                                                        {optionObj.text}
                                                                    </Dropdown.Item>
                                                                ))}
                                                            </Dropdown.Menu>
                                                        </Dropdown>
                                                        <p style={{ marginTop: "1.6vh", color: "grey", fontSize: "0.9vw", width: "11vw", height: "9vh" }}>
                                                            {
                                                                this.state.toggledAgreementTypesOptions.length > 0 ?
                                                                    this.state.toggledAgreementTypesOptions.map(row => this.state.agreementTypes.find(a => a.code == row).text).join(", ") :
                                                                    this.props.t("Selfie.NoAgreementsSelected")
                                                            }
                                                        </p>
                                                    </Form.Group>
                                                    <Form.Group style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                                                        <Form.Label>{this.props.t("PowerPlant._className")}:</Form.Label>
                                                        <Dropdown show={this.state.showPowerPlantDropdown}>
                                                            <Dropdown.Toggle
                                                                variant="danger"
                                                                id='dropdown-style'
                                                                style={{ fontWeight: "300" }}
                                                                onClick={() => this.togglePowerPlantDropdown()}
                                                            >
                                                                {this.state.showPowerPlantDropdown ? this.props.t("CloseOptions") : this.props.t("SelectOptions")}
                                                            </Dropdown.Toggle>

                                                            <Dropdown.Menu>
                                                                {this.state.powerPlants.map((optionObj) => (
                                                                    <Dropdown.Item
                                                                        key={optionObj.id}
                                                                        as="button"
                                                                        onMouseDown={(e) => {
                                                                            e.preventDefault();
                                                                            this.selectDropdownPowerPlantOptions(optionObj.id);
                                                                        }}
                                                                        active={
                                                                            this.state.toggledPowerPlantOptions.includes(optionObj.id)}
                                                                    >
                                                                        {`${optionObj.id}: ${optionObj.name}`}
                                                                    </Dropdown.Item>
                                                                ))}
                                                            </Dropdown.Menu>
                                                        </Dropdown>
                                                        <p style={{ marginTop: "1.6vh", color: "grey", fontSize: "0.9vw", width: "11vw", height: "9vh" }}>
                                                            {
                                                                this.state.toggledPowerPlantOptions.length > 0 ?
                                                                    this.state.toggledPowerPlantOptions.map(row => this.state.powerPlants.find(a => a.id == row).name).join(", ") :
                                                                    this.props.t("Selfie.NoPowerPlantsSelected")
                                                            }
                                                        </p>
                                                    </Form.Group>
                                                </Form.Row>

                                                {/* <Form.Row style={{ gap: "3px" }}>
                                                    <Form.Group>
                                                        <Form.Label>{this.props.t("ElectricityInvoice.loiDocumentType")}:</Form.Label>

                                                        <Dropdown>
                                                            <Dropdown.Toggle variant="danger" id='dropdown-style' style={{ fontWeight: "300" }}>
                                                                {this.state.selectedDocumentType === "" ? this.props.t("SelectOption") :
                                                                    (this.state.selectedDocumentType === "invoice" ? this.props.t("Selfie.DocumentTypeInvoice") :
                                                                        (this.state.selectedDocumentType === "debitNote" ? this.props.t("Selfie.DocumentTypeDebitNote") :
                                                                            this.props.t("Selfie.DocumentTypeCreditNote")))}
                                                            </Dropdown.Toggle>

                                                            <Dropdown.Menu>
                                                                {
                                                                    [
                                                                        { value: "invoice", title: this.props.t("Selfie.DocumentTypeInvoice") },
                                                                        { value: "debitNote", title: this.props.t("Selfie.DocumentTypeDebitNote") },
                                                                        { value: "creditNote", title: this.props.t("Selfie.DocumentTypeCreditNote") },
                                                                    ]
                                                                        .map(obj => (
                                                                            <Dropdown.Item
                                                                                key={obj.value}
                                                                                as="button"
                                                                                onClick={() => this.setState({ selectedDocumentType: obj.value })}
                                                                            >
                                                                                {obj.title}
                                                                            </Dropdown.Item>
                                                                        ))
                                                                }
                                                            </Dropdown.Menu>
                                                        </Dropdown>
                                                    </Form.Group>
                                                </Form.Row> */}
                                            </Card.Body>

                                            <Card.Footer>
                                                {/* Populate Electricity Invoices Action Button */}
                                                <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
                                                    <button className="button" onClick={() => this.populateElectricityInvoice()}>
                                                        {this.props.t("Selfie.PopulateElectricityInvoice")}
                                                    </button>
                                                </div>
                                            </Card.Footer>
                                        </Card>
                                    </div>
                                </div>

                                {/* <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", marginBottom: "0.5rem" }}>
                                    <form style={{ margin: "1rem" }}>
                                        <input style={{ margin: "0.25rem" }} ref="periodFrom" type="date" required />
                                        <input style={{ margin: "0.25rem" }} ref="periodTo" type="date" required />
                                        <input style={{ margin: "0.25rem" }} ref="taxEventDate" type="date" required defaultValue={getCurrentDate()} />
                                        <select style={{ margin: "0.25rem" }} ref="documentType">
                                            <option selected value="invoice">{this.props.t("Selfie.DocumentTypeInvoice")}</option>
                                            <option value="debitNote">{this.props.t("Selfie.DocumentTypeDebitNote")}</option>
                                            <option value="creditNote">{this.props.t("Selfie.DocumentTypeCreditNote")}</option>
                                        </select>
                                    </form>
                                </div> */}

                                {/* Populate Electricity Invoices Action Button */}
                                {/* <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", marginBottom: "0.5rem" }}>
                                    <button className="button" style={{ margin: "0.5rem" }} onClick={() => { this.populateElectricityInvoice() }}>
                                        {this.props.t("Selfie.PopulateElectricityInvoice")}
                                    </button>
                                </div> */}

                                <EmbedRetrieveEntityListContainer
                                    retrieveType={this.props.entityName}
                                    componentPath={this.props.componentPath} //existing path in redux store where we put data
                                    columns={populateColumns}
                                    title={this.props.t(`${selfieEntities.ElectricityInvoice.className}._className_plural`)}
                                    icon={this.props.icon}
                                    expanded={true}
                                    asTable={true}
                                    editable={true}
                                    selectedRowsColumns={this.props.selectedRowsColumns}
                                    defaultFilter={[{ id: "isValid", value: true }]}
                                    onAfterRetrieve={() => { this.fetchDisplayDataElectricityInvoices(); }}
                                    onCommitChange={() => { this.fetchDisplayDataElectricityInvoices(); }}
                                />
                            </Tab>


                            {/* Generate Files Tab */}
                            <Tab
                                eventKey={ELECTRICITY_INVOICE_SUBMENUS_OPTIONS.TAB_KEY_GENERATE_DOCUMENTS}
                                title={this.props.t("Selfie.GeneratePdfDocuments")}
                                unmountOnExit
                            >
                                {/* Generate Files Action Button */}
                                <div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", marginBottom: "0.5rem" }}>
                                    <button className="button" style={{ margin: "0.5rem" }} onClick={() => { this.generatePDFdocuments(); }}>
                                        {this.props.t("Selfie.GeneratePdfDocuments")}
                                    </button>
                                </div>

                                <EmbedRetrieveEntityListContainer
                                    retrieveType={this.props.entityName}
                                    componentPath={this.props.componentPath} //existing path in redux store where we put data
                                    columns={columns}
                                    title={this.props.t(`${selfieEntities.ElectricityInvoice.className}._className_plural`)}
                                    icon={this.props.icon}
                                    expanded={true}
                                    asTable={true}
                                    editable={true}
                                    selectedRowsColumns={this.props.selectedRowsColumns}
                                    defaultFilter={[{ id: "isValid", value: true }, { id: "hasDbFile", value: true }]}
                                />
                            </Tab>


                            {/* Correction Tab */}
                            <Tab
                                eventKey={ELECTRICITY_INVOICE_SUBMENUS_OPTIONS.TAB_KEY_FILTERS}
                                title={this.props.t("ElectricityInvoice.invoiceCorrection")}
                                unmountOnExit
                            >
                                <PageTableInvoiceCorrectionContainer />
                            </Tab>
                        </Tabs>
                    </div>
                </div>
            </div>
        );
    }
}

// redux mapping of props
function mapStateToProps(state, ownProps) {
    const entityDef = getEntityDefinition(selfieEntities.ElectricityInvoice.pluralCamelCase, undefined);
    const componentPath = "PageTable" + entityDef.className;
    const title = ownProps.t(`${selfieEntities.ElectricityInvoice.className}._className_plural`);

    return {
        auth: state.auth,
        componentPath: componentPath,
        entityName: selfieEntities.ElectricityInvoice.pluralCamelCase,
        entityDef: entityDef,
        expand: ownProps.expand ? ownProps.expand : [],
        title: title,
        icon: entityDef.icon ? entityDef.icon : "list",
        hasRowSelecting: ownProps.hasRowSelecting,
        selectedRowsColumns: ownProps.selectedRowsColumns,
    };
}

// redux mapping of actions
function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Object.assign({}, { patchRESTMultiData, dispatchEditRESTData }), dispatch)
    };
}

// export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableElectricityInvoiceContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
