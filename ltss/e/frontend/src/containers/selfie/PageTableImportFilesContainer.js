import React from "react";
import { Container, Button, Form, Alert, ProgressBar, Spinner, Card, Tab, Tabs } from "react-bootstrap";
import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";


import { withTranslation } from "react-i18next";
import { resolveObjectPath } from '../../scripts/dataUtils.js';

import history from "../../scripts/history.js";
import { isAuthenticated } from "../../components/pages/login/Login.js";
import { dispatchEditRESTData, fetchRESTFollow, patchRESTMultiData, patchRESTData } from './../../actions/taskActions';

import PageTableImportValueContainer from "./importValueRelated/PageTableImportValueContainer.js";
import PageTableImportQuantityContainer from "./importQuantityRelated/PageTableImportQuantityContainer.js";
import PageTableImportValueAndQuantityContainer from "./importValueAndQuantityRelated/PageTableImportValueAndQuantityContainer.js";

import { selfieEntities } from "./constants/selfieEntities.js";
import { IMPORT_FILES_SUBMENUS_OPTIONS } from "./constants/selfieConstants.js";
import { LOGIN } from "./constants/selfiePaths.js";

import XLSX from 'xlsx';

import NewHeader from '../../components/generic/NewHeader.js';
import './customTab.css';
import './customAlert.css';


class PageTableImportFilesContainer extends React.Component {

    componentDidMount() {
        if (!isAuthenticated(this.props.auth)) {
            history.push(LOGIN);
        }
    }

    constructor(props) {
        super(props);

        this.setAlertSuccess = this.setAlertSuccess.bind(this);
        this.setAlertError = this.setAlertError.bind(this);
        this.state = {
            tabKey: IMPORT_FILES_SUBMENUS_OPTIONS.DEFAULT_TAB_KEY_IMPORT_VALUES,
            alertError: false,
            alertSuccess: false,
            documentName: '',
            errorArray: []
        };
    }

    setAlertSuccess() {
        console.log("PageTableImportFilesContainer, alertSuccess: true: ", this.state.alertSuccess);
        this.setState({ alertSuccess: true });
    }

    setAlertError(data, document) {
        console.log("PageTableImportFilesContainer, alertError: true: ", this.state.alertError);
        console.log("data: ", data);
        console.log("document: ", document);
        this.setState({ alertError: true });
        this.setState({ documentName: document });
        this.setState({ errorArray: data });
    }

    getExcelBase64() {
        let data = []; // matrix
        let header = [this.props.t("Selfie.Row"), this.props.t("Error.Error")]; // header row of the Excel file
        data.push(header);

        for (let i = 0; i < this.state.errorArray.length; i++) { // iterating and adding the rows of errors to the matrix
            let innerArray = [];
            innerArray.push(this.state.errorArray[i].row);
            innerArray.push(this.state.errorArray[i].message);
            data.push(innerArray);
        }

        const workbook = XLSX.utils.book_new();
        const worksheet1 = XLSX.utils.aoa_to_sheet(data);

        const colWidths = data[0] // calculating the width of every single column
            .map((_, colIndex) => {
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
        let notification = "";
        if (this.state.alertError == true) {
            notification =
                <Alert className="pulsating-red-border" onClose={() => this.setState({ alertError: false })} dismissible>
                    <Alert.Heading>{this.props.t("Error.Error")}</Alert.Heading>
                    {this.props.t("Selfie.InExcel")} {' ' + this.state.documentName + ' '} {this.props.t("Selfie.ThereIs")}
                    <Alert.Link style={{ color: "red" }} onClick={() => { this.getExcelBase64(); }}>{` ${this.props.t('Selfie.IncorrectData')}`}</Alert.Link>.
                </Alert>
        }

        let notification1 = "";
        if (this.state.alertSuccess == true) {
            notification1 =
                <Alert className="pulsating-green-border" onClose={() => this.setState({ alertSuccess: false })} dismissible>
                    <Alert.Heading>{this.props.t("Selfie.FileSuccessfullyUploaded")}</Alert.Heading>
                    {/* В ексел {' ' + this.props.title + ' '} има
                            <Alert.Link onClick={() => { this.getExcelBase64() }}>{' некоректни данни'}</Alert.Link>. */}
                </Alert>
        }

        return (
            <div className='page-body'>
                {notification}
                {notification1}
                <NewHeader text={this.props.t("Import.titlePlural")} auth={this.props.auth.userAuthenticated} />

                <div className='my-custom-tabs'>
                    <Tabs
                        id="controlled-tab-example"
                        activeKey={this.state.tabKey}
                        onSelect={(k) => this.setState({ tabKey: k })}
                        justify
                    >
                        <Tab eventKey={IMPORT_FILES_SUBMENUS_OPTIONS.DEFAULT_TAB_KEY_IMPORT_VALUES} tabClassName="my-tab-title" title={this.props.t(`${selfieEntities.ImportValue.className}._className_plural`)}>
                            <div>
                                <PageTableImportValueContainer setAlertSuccess={this.setAlertSuccess} setAlertError={this.setAlertError} />
                            </div>
                        </Tab>

                        <Tab eventKey={IMPORT_FILES_SUBMENUS_OPTIONS.TAB_KEY_IMPORT_QUANTITIES} tabClassName="my-tab-title" title={this.props.t(`${selfieEntities.ImportQuantity.className}._className_plural`)} >
                            <div>
                                <PageTableImportQuantityContainer setAlertSuccess={this.setAlertSuccess} setAlertError={this.setAlertError} />
                            </div>
                        </Tab>

                        <Tab eventKey={IMPORT_FILES_SUBMENUS_OPTIONS.TAB_KEY_IMPORT_VALUE_AND_QUANTITIES} tabClassName="my-tab-title" title={this.props.t(`${selfieEntities.ImportValueAndQuantity.className}._className_plural`)} >
                            <div>
                                <PageTableImportValueAndQuantityContainer setAlertSuccess={this.setAlertSuccess} setAlertError={this.setAlertError} />
                            </div>
                        </Tab>
                    </Tabs>
                </div>
            </div>
        )
    }
}

// redux mapping
function mapStateToProps(state, ownProps) {
    // const entityName = "incomes";
    // const entityDef = getEntityDefinition(entityName,undefined);
    const componentPath = "tableImportFilesContainer";
    // let viewData = resolveObjectPath(componentPath, state.rest);
    // let data = undefined;
    // if (viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded 
    // 		&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
    // 	data = viewData[entityDef.className]._embedded[entityName][0];
    // }
    return {
        auth: state.auth,
        componentPath: componentPath,
        // entityName: entityName,
        // entityDef: entityDef,
        // data: viewData,
        // href: data && data._links && data._links.self ? data._links.self.href : undefined,
        // id: data && data.id ? data.id : undefined,
        // //UI
        title: ownProps.t(`${selfieEntities.ImportValueAndQuantity.className}._className_plural`),
    };
}

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Object.assign({}, {}), dispatch)
    };
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableImportFilesContainer);
const WithRouterComponent = withRouter(ConnectComponent);
// export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);