import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { Button, Tab, Tabs, Alert, Card, Form, Dropdown } from 'react-bootstrap';
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
import JSZip from "jszip";

import './../customTab.css';
import './../customAlert.css';

import { X_AUTH_TOKEN, COMPARISON_OPERATORS } from "../constants/selfieConstants.js";
import { selfieEntities } from "../constants/selfieEntities.js";
import { LOGIN, DOWNLOAD_PDF_ZIP, AGREEMENT_TYPES_FETCH, ACCOUNTING_PERIOD_FETCH } from "../constants/selfiePaths.js";



// Page: can be used as a landing page
// Table: presents table of the objects
// Container: redux container class
class PageTableElectricityInvoiceReferencesContainer extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            showAlert: false,
            alertMessage: "",
            alertSuccess: false,

            showDocumentTypeDropdown: false,
            showAgreementTypeDropdown: false,

            agreementTypes: [],

            toggledDocumentTypesOptions: [],
            toggledAgreementTypesOptions: [],

            selectedDocumentTypesState: [],
            selectedAgreementTypesState: [],
            selectedTaxEventDatePeriodFromState: new Date(),
            selectedTaxEventDatePeriodToState: new Date(new Date().setDate(new Date().getDate() - 1)),
        }
    }

    componentDidMount() {
        if (!isAuthenticated(this.props.auth)) {
            history.push(LOGIN);
        }

        this.fetchAgreementTypes();
    }

    filterHandler() {
        const periodFromStr = this.refs.filterPeriodFrom.value;
        const periodToStr = this.refs.filterPeriodTo.value;

        const documentTypesArr = this.state.toggledDocumentTypesOptions;
        const agreementTypesArr = this.state.toggledAgreementTypesOptions;

        if (
            periodFromStr === "" ||
            periodToStr === "" ||
            documentTypesArr.length === 0 ||
            agreementTypesArr.length === 0
        ) {
            return this.setState({ showAlert: true, alertMessage: `${this.props.t('ErrorMessages.AllFieldsMustBeFilled')}` });
        }

        this.setState((prevState) => {
            return {
                ...prevState,
                selectedDocumentTypesState: documentTypesArr,
                selectedAgreementTypesState: agreementTypesArr,
                selectedTaxEventDatePeriodFromState: new Date(periodFromStr),
                selectedTaxEventDatePeriodToState: new Date(periodToStr)
            };
        });
    }

    downloadDocumentsHandler() {
        this.filterHandler(); // refresh the state, in case the user has changed some of the values, so the table is refreshed  

        const periodFromDate = this.state.selectedTaxEventDatePeriodFromState;
        const periodFromStr = periodFromDate.toISOString().slice(0, 10);

        const periodToDate = this.state.selectedTaxEventDatePeriodToState;
        const periodToStr = periodToDate.toISOString().slice(0, 10);

        const documentTypesArr = this.state.selectedDocumentTypesState;
        const documentTypesStr = documentTypesArr.join(",");

        const agreementTypesArr = this.state.selectedAgreementTypesState;
        const agreementTypesStr = agreementTypesArr.join(",");

        if (
            periodFromStr === "" ||
            periodToStr === "" ||
            documentTypesArr.length === 0 ||
            agreementTypesArr.length === 0
        ) {
            return this.setState({ showAlert: true, alertMessage: `${this.props.t('ErrorMessages.AllFieldsMustBeFilled')}` });
        }

        const selectedRows = this.props.data.selectedRows;
        if (selectedRows === undefined) {
            return this.setState({ showAlert: true, alertMessage: `${this.props.t('ErrorMessages.NoEntriesWereSelected')}` });
        }

        const accessPointsArr = Array.from(this.props.data.selectedRows.map.values()).map((obj) => obj.reportingPointOwn);
        const accessPointsStr = accessPointsArr.join(",");

        const url = DOWNLOAD_PDF_ZIP(API_URL, periodFromStr, periodToStr, documentTypesStr, agreementTypesStr, accessPointsStr);

        axios({
            method: "GET",
            url: url,
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] },
            responseType: 'arraybuffer'
        })
            .then(async (response) => {
                const directoryHandle = await window.showDirectoryPicker();

                const zipFile = await JSZip.loadAsync(response.data);

                const extractPromises = Object.keys(zipFile.files).map(async (filename) => {
                    const fileContent = await zipFile.files[filename].async('blob');

                    const fileHandle = await directoryHandle.getFileHandle(filename, { create: true });
                    const writable = await fileHandle.createWritable();
                    await writable.write(fileContent);
                    await writable.close();
                });

                await Promise.all(extractPromises);

                this.setState({ alertSuccess: true, alertMessage: `${this.props.t('Selfie.FilesSuccessfullyDownloaded')}` });

                // this.props.actions.dispatchEditRESTData(this.props.componentPath, undefined);
            })
            .catch(error => console.log("Error Filtering Electricity Invoices: ", error));
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

    selectDropdownDocumentTypeOptions(optionValue) {
        if (this.state.toggledDocumentTypesOptions.includes(optionValue)) {
            this.setState((prevState) => {
                return {
                    ...prevState,
                    toggledDocumentTypesOptions: prevState.toggledDocumentTypesOptions.filter((item) => item !== optionValue),
                };
            });

        } else {
            this.setState((prevState) => {
                return {
                    ...prevState,
                    toggledDocumentTypesOptions: [...prevState.toggledDocumentTypesOptions, optionValue],
                };
            });
        }
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

    toggleDocumentTypeDropdown() {
        this.setState((prevState) => ({
            showDocumentTypeDropdown: !prevState.showDocumentTypeDropdown,
        }));
    }

    toggleAgreementTypeDropdown() {
        this.setState((prevState) => ({
            showAgreementTypeDropdown: !prevState.showAgreementTypeDropdown,
        }));
    }

    render() {
        let columns = getEntityDefinition(this.props.entityName, {
            isValid: { show: "hidden" },
            hasDbFile: { show: "hidden" }
        }).columns;

        // console.log("this.props: ", this.props);

        let notification = "";
        if (this.state.showAlert == true) {
            notification =
                <Alert className="pulsating-red-border" onClose={() => this.setState({ showAlert: false })} dismissible>
                    <Alert.Heading>Грешка</Alert.Heading>
                    {this.state.alertMessage}
                </Alert>
        }

        let notification1 = "";
        if (this.state.alertSuccess == true) {
            notification1 =
                <Alert className="pulsating-green-border" onClose={() => this.setState({ alertSuccess: false })} dismissible>
                    <Alert.Heading>{this.state.alertMessage}</Alert.Heading>
                </Alert>
        }

        return (
            <div className="page-body-wrapper">
                {notification}
                {notification1}
                <NewHeader text={this.props.t(`${selfieEntities.Reference.className}._className_plural`)} auth={this.props.auth.userAuthenticated} />

                <div className='page-body'>
                    <div className='my-custom-tabs'>

                        {/* Filter for Electricity Invoices */}
                        <Card style={{ marginBottom: "2rem", boxShadow: "0px 4px 10px rgba(0, 0, 0, 0.1)", borderRadius: "0" }} className="h-100 ">
                            <Card.Header style={{ color: 'red' }}>{this.props.t("Selfie.FilterElectricityInvoices")}</Card.Header>

                            <Card.Body>
                                <Form.Row style={{ gap: "2.5rem", display: "flex", height: "auto", height: "20vh"}}>
                                    <Form.Group>
                                        <Form.Label>{this.props.t("Selfie.TaxEventDateFrom")}:</Form.Label>
                                        <Form.Control type="date" required ref="filterPeriodFrom" />
                                    </Form.Group>

                                    <Form.Group>
                                        <Form.Label>{this.props.t("Selfie.TaxEventDateTo")}:</Form.Label>
                                        <Form.Control type="date" required ref="filterPeriodTo" />
                                    </Form.Group>

                                    <Form.Group>
                                        <Form.Label>{this.props.t("ElectricityInvoice.loiDocumentType")}:</Form.Label>
                                        <Dropdown
                                            show={this.state.showDocumentTypeDropdown}
                                        >
                                            <Dropdown.Toggle
                                                variant="danger"
                                                id='dropdown-style'
                                                style={{ fontWeight: "300" }}
                                                onClick={() => this.toggleDocumentTypeDropdown()}
                                            >
                                                {this.state.showDocumentTypeDropdown ? this.props.t("CloseOptions") : this.props.t("SelectOptions")}
                                            </Dropdown.Toggle>

                                            <Dropdown.Menu>
                                                {['Invoice', 'Debit Note', 'Credit Note'].map((option, index) => (
                                                    <Dropdown.Item
                                                        key={index + 1}
                                                        as="button"
                                                        onMouseDown={(e) => {
                                                            e.preventDefault();
                                                            this.selectDropdownDocumentTypeOptions(index + 1);
                                                        }}
                                                        active={this.state.toggledDocumentTypesOptions.includes(index + 1)}
                                                    >
                                                        {option}
                                                    </Dropdown.Item>
                                                ))}
                                            </Dropdown.Menu>
                                        </Dropdown>
                                        <p style={{ marginTop: "1.6vh", color: "grey", fontSize: "0.9vw", width: "11vw", height: "9vh" }}>
                                            {
                                                this.state.toggledDocumentTypesOptions.length > 0 ?
                                                    this.state.toggledDocumentTypesOptions.map(row => ['Invoice', 'Debit Note', 'Credit Note'][row - 1]).join(", ") :
                                                    this.props.t("Selfie.NoDocumentsSelected")
                                            }
                                        </p>
                                    </Form.Group>

                                    <Form.Group>
                                        <Form.Label>{this.props.t("ElectricityInvoice.agreementType")}:</Form.Label>
                                        <Dropdown
                                            show={this.state.showAgreementTypeDropdown}
                                        >
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
                                </Form.Row>

                                <Form.Row style={{ display: "flex", justifyContent: "flex-start", gap: "2.5rem", display: "flex", alignItems: "center" }}>
                                    <button className="button" onClick={() => { this.filterHandler(); }}>
                                        {this.props.t("Selfie.Filter")}
                                    </button>

                                    {/* Download Electricity Invoices Documents Action Button */}
                                    <button className="button" onClick={() => { this.downloadDocumentsHandler(); }} >
                                        {this.props.t("Selfie.DownloadDocuments")}
                                    </button>
                                </Form.Row>
                            </Card.Body>
                        </Card>

                        <EmbedRetrieveEntityListContainer
                            retrieveType={this.props.entityName}
                            componentPath={this.props.componentPath} //existing path in redux store where we put data
                            columns={columns}
                            title={this.props.t(`${selfieEntities.Reference.className}._className_plural`)}
                            icon={this.props.icon}
                            expanded={true}
                            asTable={true}
                            // editable={true}
                            selectedRowsColumns={this.props.selectedRowsColumns}
                            hasRowSelecting={true}
                            defaultFilter={
                                [
                                    {
                                        where: {
                                            op: "and",
                                            operands: [
                                                {
                                                    op: "isNotNull",
                                                    operands: ["isValid"]
                                                },
                                                {
                                                    op: "isTrue",
                                                    operands: ["isValid"]
                                                }
                                            ]
                                        }
                                    },
                                    {
                                        where: {
                                            op: "and",
                                            operands: [
                                                {
                                                    op: "isNotNull",
                                                    operands: ["hasDbFile"]
                                                },
                                                {
                                                    op: "isTrue",
                                                    operands: ["hasDbFile"]
                                                }
                                            ]
                                        }
                                    },
                                    {
                                        where: {
                                            op: COMPARISON_OPERATORS.GREATER_OR_EQUAL,
                                            operands: [
                                                "taxEventDate",
                                                { op: "localDateLiteral", operands: [{ literal: (this.state.selectedTaxEventDatePeriodFromState).toISOString().slice(0, 10) }] }
                                            ]
                                        }
                                    },
                                    {
                                        where: {
                                            op: COMPARISON_OPERATORS.LOWER_OR_EQUAL,
                                            operands: [
                                                "taxEventDate",
                                                { op: "localDateLiteral", operands: [{ literal: (this.state.selectedTaxEventDatePeriodToState).toISOString().slice(0, 10) }] }
                                            ]
                                        }
                                    },
                                    {
                                        where: this.state.selectedAgreementTypesState.length > 0
                                            ? {
                                                op: "and",
                                                operands: [
                                                    { op: "isNotNull", operands: ["agreementType.code"] },
                                                    {
                                                        op: "like",
                                                        operands: [
                                                            { literal: "," + this.state.selectedAgreementTypesState.join(",") + "," },
                                                            {
                                                                op: "concat",
                                                                operands: [
                                                                    { literal: "%," },
                                                                    {
                                                                        op: "concat",
                                                                        operands: [
                                                                            "agreementType.code",
                                                                            { literal: ",%" }
                                                                        ]
                                                                    }
                                                                ]
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                            :
                                            undefined
                                    },
                                    {
                                        where: this.state.selectedDocumentTypesState.length > 0
                                            ? {
                                                op: "and",
                                                operands: [
                                                    { op: "isNotNull", operands: ["loiDocumentType.listOptionItemCode"] },
                                                    {
                                                        op: "like",
                                                        operands: [
                                                            { literal: "," + this.state.selectedDocumentTypesState.join(",") + "," },
                                                            {
                                                                op: "concat",
                                                                operands: [
                                                                    { literal: "%," },
                                                                    {
                                                                        op: "concat",
                                                                        operands: [
                                                                            "loiDocumentType.listOptionItemCode",
                                                                            { literal: ",%" }
                                                                        ]
                                                                    }
                                                                ]
                                                            }
                                                        ]
                                                    }
                                                ]
                                            }
                                            :
                                            undefined
                                    }
                                ]
                            }
                        />
                    </div>
                </div>
            </div >
        );
    }
}

// redux mapping of props
function mapStateToProps(state, ownProps) {
    const entityDef = getEntityDefinition(selfieEntities.ElectricityInvoice.pluralCamelCase, undefined);
    const componentPath = "PageTable" + entityDef.className;
    const title = ownProps.t(`${selfieEntities.ElectricityInvoice.className}._className_plural`);
    let data = resolveObjectPath(componentPath, state.rest);

    return {
        auth: state.auth,
        componentPath: componentPath,
        entityName: selfieEntities.ElectricityInvoice.pluralCamelCase,
        entityDef: entityDef,
        data: data,
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableElectricityInvoiceReferencesContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
