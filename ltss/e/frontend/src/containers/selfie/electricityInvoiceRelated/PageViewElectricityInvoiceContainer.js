import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import { Alert, Form, Card } from 'react-bootstrap';
import Dropdown from 'react-bootstrap/Dropdown';
import DropdownButton from 'react-bootstrap/DropdownButton';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { withTranslation } from 'react-i18next';

import * as Constants from '../../../static/constants';
import history from '../../../scripts/history';
import { isAuthenticated } from '../../../components/pages/login/Login.js';
import axios from 'axios';

import NewHeader from '../../../components/generic/NewHeader';
import EmbedEntityContainer from '../../embeds/EmbedEntityContainer';
//import EmbedCustomerContainer from './EmbedCustomerContainer';
//import EmbedVendorContainer from './EmbedVendorContainer';
import EmbedChangeHistory from '../../embeds/EmbedChangeHistory';

import { getEntityDefinition } from '../../nomenclatures/entityDefinitions.js';
import { selfieEntities } from "../constants/selfieEntities.js";
import { LOGIN, CREATE_NEW_ELECTRICITY_INVOICE_NOTE } from "../constants/selfiePaths.js";
import EmbedRetrieveEntityListContainer from '../../nomenclatures/EmbedRetrieveEntityListContainer';
import { X_AUTH_TOKEN } from '../constants/selfieConstants.js';


// Page: can be used as a landing page
// View: presents details of object
// Container: redux container class
class PageViewElectricityInvoiceContainer extends React.Component {
    componentDidMount() {
        if (!isAuthenticated(this.props.auth)) {
            history.push(LOGIN);
        }
    }

    constructor(props) {
        super(props);

        this.state = {
            monthIndex: 0,
            showAlert: false,
            alertMessage: "",
            alertSuccess: false,

            totalQuantity: 0,
            priceInLevs: 0,
            documentTypeId: 0,
        }
    }

    createNewDocument() {
        let electricityInvoiceId = this.props.data.id;

        console.log("createNewDocument");
        console.log("Цена: ", this.state.priceInLevs);
        console.log("Стойност: ", this.state.totalQuantity);
        console.log("Тип Документ: ", this.state.documentTypeId);
        console.log("Id на фактурата: ", electricityInvoiceId);

        axios({
            method: "post",
            url: CREATE_NEW_ELECTRICITY_INVOICE_NOTE(API_URL, electricityInvoiceId, this.state.totalQuantity, this.state.priceInLevs, this.state.documentTypeId),
            headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
        })
            .then(res => {
                let data = res.data;
                console.log("data response: ", data);

                this.setState({
                    alertSuccess: true, alertMessage:
                        this.state.documentTypeId == 2 ?
                            this.props.t("Selfie.DebitNoteCreated") :
                            this.props.t("Selfie.CreditNoteCreated")
                });
            })
            .catch(error => {
                console.log("PageViewElectricityInvoiceContainer.axios.error" + error);
            });
    }

    render() {
        let body = '';

        // console.log("PageViewElectricityInvoiceContainer-props: ", this.props)
        if (this.props.data) {
            if (this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
                body = <FontAwesomeIcon icon="spinner" size="2x" spin />;

            } else if (this.props.id && this.props.id == parseInt(this.props.match.params.person_id, 10)) {
                body = <div className='page-body'>
                    <EmbedChangeHistory
                        entity_id={this.props.id}
                        componentPath={`${selfieEntities.ElectricityInvoice.view}.changelog`}
                        retrieveType={selfieEntities.ElectricityInvoice.pluralCamelCase}
                    />
                </div>;
            }
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
                {notification1}
                <NewHeader text={this.props.t("Selfie.ManualEditing")} auth={this.props.auth.userAuthenticated} />

                <Card style={{ marginBottom: "2rem", boxShadow: "0px 4px 10px rgba(0, 0, 0, 0.1)" }}>
                    <Card.Body>
                        <Form.Row style={{display: "flex", alignItems: "center"}}>
                            <Form.Group className={'m-2'}>
                                <Form.Label>{this.props.t("Selfie.Price")}:</Form.Label>
                                <Form.Control type="number" value={this.state.priceInLevs} onChange={(e) => {
                                    this.setState({ priceInLevs: e.target.value });
                                    console.log("Цена: ", e.target.value);
                                }} />
                            </Form.Group>

                            <Form.Group className={'m-2'}>
                                <Form.Label>{this.props.t("Selfie.Value")}:</Form.Label>
                                <Form.Control type="number" value={this.state.totalQuantity} onChange={(e) => {
                                    this.setState({ totalQuantity: e.target.value })
                                    console.log("Стойност: ", e.target.value)
                                }} />
                            </Form.Group>

                            <Form.Group className={'m-2'}>
                                <Form.Label>{this.props.t("ElectricityInvoice.loiDocumentType")}:</Form.Label>

                                <Dropdown>
                                    <Dropdown.Toggle variant="danger" id='dropdown-style' style={{ fontWeight: "300" }}>
                                        {this.state.documentTypeId === 0 ? this.props.t("SelectOption") :
                                            (this.state.documentTypeId === 1 ? this.props.t("Selfie.DocumentTypeInvoice") :
                                                (this.state.documentTypeId === 2 ? this.props.t("Selfie.DocumentTypeDebitNote") :
                                                    this.props.t("Selfie.DocumentTypeCreditNote")))}
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
                                                        onClick={() => this.setState({ documentTypeId: obj.value })}
                                                    >
                                                        {obj.title}
                                                    </Dropdown.Item>
                                                ))
                                        }
                                    </Dropdown.Menu>
                                </Dropdown>
                            </Form.Group>
                        </Form.Row>
                    </Card.Body>

                    <Card.Footer>
                        <Form.Row>
                            <Form.Group className={'m-2'}>
                                <button className="button" onClick={() => this.createNewDocument()}>{this.props.t("Selfie.GenerateDocument")}</button>
                            </Form.Group>
                        </Form.Row>
                    </Card.Footer>
                </Card>


                <EmbedEntityContainer
                    componentPath={`${selfieEntities.ElectricityInvoice.view}.${selfieEntities.ElectricityInvoice.singleCamelCase}`}
                    loading={this.props.loading}
                    onBeforeChange={(data) => {
                        if (data.id) {
                            history.push(`/${selfieEntities.ElectricityInvoice.pluralCamelCase}/` + data.id);
                        } else {
                            history.push(`/${selfieEntities.ElectricityInvoice.pluralCamelCase}/add`);
                        }
                    }}
                    retrieve_id={this.props.match.params.electricityInvoice_id}
                    entityName={selfieEntities.ElectricityInvoice.pluralCamelCase}
                    headerText={this.props.t(`${selfieEntities.ElectricityInvoice.className}._className`)}
                    expanded={true}
                    editable={false}
                    creatable={false}
                    deleteable={false}
                    columnOverride={
                        {
                            invoiceCorrection: { show: false, },
                            hasDbFile: { show: false, },
                            totalSumInEuros: { show: false, },
                            priceInEuros: { show: false, },
                            isValid: { show: false, },
                        }
                    }
                >
                </EmbedEntityContainer>
                {body}
            </div>
        );
    }
}

// redux mapping
function mapStateToProps(state, ownProps) {
    const data = state.rest.electricityInvoiceView && state.rest.electricityInvoiceView.electricityInvoice && state.rest.electricityInvoiceView.electricityInvoice._embedded ? state.rest.electricityInvoiceView.electricityInvoice._embedded.electricityInvoices[0] : undefined;

    return {
        auth: state.auth,
        data: data,
        href: data && data._links && data._links.self ? data._links.self.href : undefined,
        id: data && data.id ? data.id : undefined,
    };
}

// redux mapping
function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Object.assign({}, {}), dispatch)
    };
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewElectricityInvoiceContainer);
const WithRouterComponent = withRouter(ConnectComponent);
// export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
