import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { Button, Alert, Card } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';
import axios from "axios";

import history from '../../../scripts/history';
import { isAuthenticated } from '../../../components/pages/login/Login.js';

import NewHeader from '../../../components/generic/NewHeader';
import EmbedRetrieveEntityListContainer from '../../nomenclatures/EmbedRetrieveEntityListContainer';

import { getEntityDefinition, getExpandedColumns } from '../../nomenclatures/entityDefinitions.js';
import { resolveObjectPath } from '../../../scripts/dataUtils';
import { patchRESTMultiData } from '../../../actions/taskActions';
import NepalDBFileContainer from '../../nepal/NepalDBFileContainer.js';
import { dispatchEditRESTData } from "../../../actions/taskActions.js";

import { X_AUTH_TOKEN, COLOR_MAPPINGS, DOCUMENT_TYPE_COLORS } from "../constants/selfieConstants.js";
import { selfieEntities } from "../constants/selfieEntities.js";
import { REPORTS_IMPORT_VALUES_FILE, LOGIN, IMPORT_VALUES_FETCH, LEGAL_PEOPLE_FETCH, ACCOUNTING_PERIOD_FETCH } from "../constants/selfiePaths.js";

import '../customAlert.css';
// import XLSX from 'xlsx';
import Plot from 'react-plotly.js';


// Page: can be used as a landing page
// Table: presents table of the objects
// Container: redux container class
class PageTableImportValueContainer extends React.Component {
    componentDidMount() {
        if (!isAuthenticated(this.props.auth)) {
            history.push(LOGIN);
        }

        this.fetchDisplayDataImportValues();
    }

    fetchDisplayDataImportValues() {
        axios({
            method: "GET",
            url: API_URL + IMPORT_VALUES_FETCH,
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(res => {
                const accessPointsSet = new Set();
                
                if (res.data.hasOwnProperty("_embedded")) { // case where there are entries
                    const importValuesData = res.data._embedded.hashMaps;

                    // reseting the number of documents
                    this.setState((prevState) => ({
                        ...prevState,
                        numberOfInvoices: 0,
                        numberOfDebitInvoices: 0,
                        numberOfCreditInvoices: 0,
                    }));

                    importValuesData
                        .forEach(importValue => {
                            // add the access point
                            accessPointsSet.add(importValue["ImportValue"].reportingPointOwn);

                            // increment the Document Type
                            if (importValue["ImportValue.loiDocumentType"].listOptionItemName === "Фактура") {
                                this.setState((prevState) => ({
                                    ...prevState,
                                    numberOfInvoices: prevState.numberOfInvoices + 1
                                }));

                            } else if (importValue["ImportValue.loiDocumentType"].listOptionItemName === "Дебитно известие") {
                                this.setState((prevState) => ({
                                    ...prevState,
                                    numberOfDebitInvoices: prevState.numberOfDebitInvoices + 1
                                }));

                            } else {
                                this.setState((prevState) => ({
                                    ...prevState,
                                    numberOfCreditInvoices: prevState.numberOfCreditInvoices + 1
                                }));
                            }
                        });

                    // set the number of access points
                    this.setState((prevState) => ({
                        ...prevState,
                        numberOfReportingPoints: accessPointsSet.size
                    }));

                    // count the number of import values in this accounting period
                    axios({
                        method: "get",
                        url: API_URL + ACCOUNTING_PERIOD_FETCH,
                        headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
                    })
                        .then(res1 => {
                            let accountingPeriodsData = res1.data._embedded.hashMaps;
                            let accountPeriodMonth = accountingPeriodsData[0]["AccountingPeriod"].code;

                            this.setState((prevState) => ({
                                ...prevState,
                                numberOfImportValuesInAccountingPeriod: 0
                            }));

                            importValuesData
                                .forEach(importValue => {
                                    const periodFromMonth = importValue["ImportValue"].periodFrom.split("-")[1];
                                    const periodToMonth = importValue["ImportValue"].periodTo.split("-")[1];

                                    // if the current entry has the same month -> increment the numberOfEntriesInAccountingPeriod
                                    if (
                                        (periodFromMonth == accountPeriodMonth || periodFromMonth == "0" + accountPeriodMonth) &&
                                        (periodToMonth == accountPeriodMonth || periodToMonth == "0" + accountPeriodMonth)
                                    ) {
                                        this.setState((prevState) => ({
                                            ...prevState,
                                            numberOfImportValuesInAccountingPeriod: prevState.numberOfImportValuesInAccountingPeriod + 1
                                        }));
                                    }
                                });
                        })
                        .catch(error => {
                            console.log("PageTableImportValueContainer.axios.error" + error);
                        });
                }
            })
            .catch(error => {
                console.log("PageTableImportValueContainer.axios.error" + error);
            });


        // fetch and set the number of Legal People
        axios({
            method: "GET",
            url: API_URL + LEGAL_PEOPLE_FETCH,
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(res => {
                if (res.data.page.totalElements > 0) {
                    this.setState((prevState) => ({
                        ...prevState,
                        numberOfLegalPeople: res.data._embedded.hashMaps.length
                    }));
                }
            })
            .catch(error => {
                console.log("PageTableImportValueContainer.axios.error" + error);
            });
    }

    constructor(props) {
        super(props);

        this.state = {
            alertUploadHasErrors: false,
            alertWrongFile: false,
            errorArray: [],

            // Fetch Dsiplay data vars
            numberOfInvoices: 0,
            numberOfDebitInvoices: 0,
            numberOfCreditInvoices: 0,
            numberOfReportingPoints: 0,
            numberOfImportValuesInAccountingPeriod: 0,
            numberOfLegalPeople: 0
        };
    }

    importExcelImportValues(id) {
        console.log("Message from import Excel File: " + id);
        console.log("URL: " + API_URL + REPORTS_IMPORT_VALUES_FILE + id);

        axios({
            method: "POST",
            url: API_URL + REPORTS_IMPORT_VALUES_FILE + id,
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(response => {
                let data = response.data;

                console.log("PageTableImportValueContainer.axios.then", data);
                console.log("PageTableImportValueContainer.axios.then -> Object.keys(data).lenght: ", Object.keys(data).length);

                if (Object.keys(data).length != 0) {
                    this.setState({ alertUploadHasErrors: true });

                    Object.keys(data)
                        .forEach(invalidRow => {
                            console.log("Invalid Row: " + invalidRow);

                            data[invalidRow]
                                .forEach((errorMessage) => {

                                    console.log(invalidRow + ": " + this.props.t(`${selfieEntities.ErrorMessages.pluralPascalCase}.${errorMessage}`));

                                    let errorObject = {
                                        row: invalidRow,
                                        message: this.props.t(`${selfieEntities.ErrorMessages.pluralPascalCase}.${errorMessage}`)
                                    };

                                    this.setState((prevState) => ({
                                        errorArray: [...prevState.errorArray, errorObject]
                                    }));
                                });
                        });

                    this.props.setAlertError(this.state.errorArray, this.props.title);

                } else {
                    console.log("this.props: ", this.props);
                    this.props.setAlertSuccess();
                }

                //force refresh
                this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
                this.fetchDisplayDataImportValues();
            })
            .catch(error => {
                console.log("PageTableImportValueContainer.axios.error", error);
            });
    }

    getDocumentDistributionPieChartData() {
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

    // getExcelBase64() {

    //     let data = []
    //     let header = ['Ред', 'Грешка']
    //     data.push(header)

    //     for (let i = 0; i < this.state.errorArray.length; i++) {
    //         let innerArray = []

    //         innerArray.push(this.state.errorArray[i].row)
    //         innerArray.push(this.state.errorArray[i].message)

    //         data.push(innerArray)
    //     }

    //     const wb = XLSX.utils.book_new()

    //     const ws1 = XLSX.utils.aoa_to_sheet(data)
    //     XLSX.utils.book_append_sheet(wb, ws1, "Sheet1")

    //     // return XLSX.write(wb, { type: "base64" });
    //     this.setState({errorArray: []})

    //     const excelBuffer = XLSX.write(wb, { bookType: "xlsx", type: "array" });

    //     const blob = new Blob([excelBuffer], { type: "application/octet-stream" });

    //     const link = document.createElement("a");
    //     link.href = URL.createObjectURL(blob);
    //     link.download = "data.xlsx";
    //     link.click();
    // }

    render() {
        let columns = getExpandedColumns(this.props.entityName, this.props.expand);

        // let notification = "";
        // if (this.state.alertUploadHasErrors == true) {
        //     if (this.state.alertWrongFile == true) {
        //         notification =
        //             <Alert variant="danger" onClose={() => this.setState({ alertUploadHasErrors: false })} dismissible>
        //                 <Alert.Heading>Грешка</Alert.Heading>
        //                 Грешен файл.
        //             </Alert>;
        //     }
        //     notification =
        //         <Alert className="pulsating-red-border" onClose={() => this.setState({ alertUploadHasErrors: false })} dismissible>
        //             <Alert.Heading>Грешка</Alert.Heading>

        //             В ексел {' ' + this.props.title + ' '} има
        //             <Alert.Link onClick={() => { this.getExcelBase64() }}>{' некоректни данни'}</Alert.Link>.
        //         </Alert>;
        // }

        return (
            <div className="page-body-wrapper">
                {/* {notification} */}
                {/* <NewHeader text={this.props.title} auth={this.props.auth.userAuthenticated} /> */}

                <div className="page-body">
                    <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gridGap: "5px", marginTop: "1.5%", marginBottom: "1%" }}>
                        <div style={{ display: "flex", justifyContent: "center" }} >
                            <Plot
                                data={this.getDocumentDistributionPieChartData()}
                                layout={{
                                    // title: "Document Type Distribution",
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
                                // config={{
                                //     responsive: true,
                                //     displayModeBar: true
                                // }}
                            // layout={{
                            //     // title: "Document Type Distribution",
                            //     height: 350,
                            //     width: 450,
                            //     // responsive: true,
                            //     margin: { "t": 10, "b": 10, "l": 50, "r": 30 },
                            //     padding: { "l": 10 },
                            //     legend: {
                            //         x: 1,
                            //         y: 0
                            //     },
                            //     paper_bgcolor: "#e6e6e6",
                            //     annotations: [{ showarrow: false, text: '' }]
                            // }}
                            />
                        </div>

                        <Card style={{ minHeight: "400px" }} className="custom-card h-100 text-center">
                            <Card.Header style={{ color: 'red' }}>{this.props.t("Selfie.ImportValuesStats")}</Card.Header>
                            <Card.Body style={{ color: 'black' }}>
                                <div className="mb-3">
                                    <i className="bi bi-upload" /> <i className="bi bi-file-pdf" />{" "}
                                    <i className="bi bi-pencil" /> <i className="bi bi-envelope" />
                                </div>
                                <ul className="text-start">
                                    <li style={{ textAlign: "left", marginBottom: "0.6rem" }}>{this.props.t("Selfie.NumberOfUniqueReportingPoints")}: {this.state.numberOfReportingPoints}</li>
                                    <li style={{ textAlign: "left", marginBottom: "0.6rem" }}>{this.props.t("Selfie.NumberOfLegalPeople")}: {this.state.numberOfLegalPeople}</li>
                                    <li style={{ textAlign: "left", marginBottom: "0.6rem" }}>{this.props.t("Selfie.NumberOfImportValuesInAccountingPeriod")}: {this.state.numberOfImportValuesInAccountingPeriod}</li>
                                </ul>
                            </Card.Body>
                            <Card.Footer>
                                <NepalDBFileContainer
                                    onFileUpload={(id) => this.importExcelImportValues(id)}
                                    buttonText={this.props.t("Import.ImportFile")}
                                    nepalDbFileInputTagId="fileImportValue"
                                />
                            </Card.Footer>
                        </Card>
                    </div>

                    <EmbedRetrieveEntityListContainer
                        retrieveType={this.props.entityName}
                        componentPath={this.props.componentPath} //existing path in redux store where we put data
                        columns={columns}
                        title={this.props.t(`${selfieEntities.ImportValue.className}._className_plural`)}
                        icon={this.props.icon}
                        expanded={true}
                        asTable={true}
                        editable={true}
                        selectedRowsColumns={this.props.selectedRowsColumns}
                        onAfterRetrieve={() => { this.fetchDisplayDataImportValues(); }}
                        onCommitChange={() => { this.fetchDisplayDataImportValues(); }}
                    >
                    </EmbedRetrieveEntityListContainer>
                </div>
            </div>
        );
    }
}

// redux mapping of props
function mapStateToProps(state, ownProps) {
    const entityDef = getEntityDefinition(selfieEntities.ImportValue.pluralCamelCase, undefined);
    const componentPath = "PageTable" + entityDef.className;
    const title = ownProps.t(`${selfieEntities.ImportValue.className}._className_plural`);

    return {
        auth: state.auth,
        componentPath: componentPath,
        entityName: selfieEntities.ImportValue.pluralCamelCase,
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableImportValueContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
