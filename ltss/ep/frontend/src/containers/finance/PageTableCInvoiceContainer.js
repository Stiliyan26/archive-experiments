import React from "react";

import { bindActionCreators } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom"
import { withTranslation } from "react-i18next";

import history from "../../scripts/history"
import { isAuthenticated } from "../../components/pages/login/Login.js"

import Header from "../../components/generic/Header"
import EmbedRetrieveEntityListContainer from "../nomenclatures/EmbedRetrieveEntityListContainer"

import { getEntityDefinition, getExpandedColumns } from "../nomenclatures/entityDefinitions.js"
import { resolveObjectPath } from "../../scripts/dataUtils";
import { patchRESTMultiData } from "../../actions/taskActions";

const LOGIN = "/login"
const IS_EXPORTED = "isExported"
const PATCH = "patch"
const ADD = "add"
const APPLICATION_JSON_PATCH_JSON = "application/json-patch+json"
const DOT_EXPORT_TO_MICRO_INVEST_DOT_UPDATED_DOT = "._exportToMicroInvest._updated."
const EMPTY = ""
const TRANSFER_DATA = "TransferData"
const XMLNS_XSI = "xmlns:xsi"
const XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance"
const XMLNS_XSD = "xmlns:xsd"
const XML_SCHEMA = "http://www.w3.org/2001/XMLSchema"
const XMLNS = "xmlns"
const URN_TRANSFER = "urn:Transfer"
const ACCOUNTINGS = "Accountings"
const ACCOUNTING = "Accounting"
const ACCOUNTING_DATE = "AccountingDate"
const YYYY_MM_DD = "YYYY-MM-DD"
const VIES_MONTH = "ViesMonth"
const DUE_DATE = "DueDate"
const TERM = "Term"
const TERM_VALUE = "Продажби"
const REFERENCE = "Reference"
const DOCUMENT = "Document"
const DOCUMENT_TYPE = "DocumentType"
const ONE = "1"
const NUMBER = "Number"
const DATE = "Date"
const COMPANY = "Company"
const NAME = "Name"
const CONTACT_NAME = "ContactName"
const BULSTAT = "Bulstat"
const VAT_NUMBER = "VatNumber"
const ADDRESS = "Address"
const LOCATION = "Location"
const ACCOUNTING_DETAILS = "AccountingDetails"
const ACCOUNTING_DETAIL = "AccountingDetail"
const ACCOUNTING_NUMBER = "AccountNumber"
const ACCOUNTING_NUMBER_VALUE_411_SLASH_1 = "411/1"
const ACCOUNTING_NUMBER_VALUE_453_SLASH_2 = "453/2"
const ACCOUNTING_NUMBER_VALUE_703_SLASH_1 = "703/1"
const AMOUNT = "Amount"
const DIRECTION = "Direction"
const DEBIT = "Debit"
const CREDIT = "Credit"
const VAT_TERM = "VatTerm"
const SEVEN = "7"
const TAG_NAME_A = "a"
const HREF = "href"
const HREF_VALUE = "data:text/xml;charset=utf-8,"
const DOWNLOAD = "download"
const DOWNLOAD_VALUE = "exportToMicroinvest.xml"
const NONE = "none"
const PAGE_TABLE_C_INVOICE_CONTAINER_EXPORT_TO_MICROINVEST = "PageTableCInvoiceContainer.exportToMicroinvest"
const PAGE_BODY_WRAPPER = "page-body-wrapper"
const C_INVOICE_CLASS_NAME_PLURAL = "CInvoice._className_plural"
const DOT_EXPORT_TO_MICRO_INVEST = "._exportToMicroInvest"
const SLASH = "/"
const PAGE_BODY = "page-body"
const C_INVOICES = "cInvoices"
const LIST = "list"
const PAGE_TABLE = "PageTable"
const PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN = "page-header text-align-center no-margin"

//Page: can be used as a landing page
//Table: presents table of the objects
//Container: redux container class
class PageTableCInvoiceContainer extends React.Component {
	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
		}
	}
	
	exportToMicroinvest() {
		//mark invoices as sent
		//check data availability
		if(this.props.exportToMicroInvestData && this.props.exportToMicroInvestData.selectedRows && this.props.exportToMicroInvestData.selectedRows.map) {
			let patches = [];
			let _fieldPath = IS_EXPORTED
			let editData = true;
			this.props.exportToMicroInvestData.selectedRows.map.forEach((invoice) => {
				patches.push({
					restConfig: {
						method: PATCH,
						url: API_URL+SLASH+this.props.entityName+SLASH+invoice.id,
						data: [{ "op": ADD, "path": SLASH+_fieldPath, "value": editData }], //TODO the literals "op", "path" and "value" cannot be replaced by a constant because it does not work
						headers: {"Content-Type": APPLICATION_JSON_PATCH_JSON} //TODO the literal "Content-Type" cannot be replaced by a constant because it does not work
					},
					statePath: this.props.componentPath+DOT_EXPORT_TO_MICRO_INVEST_DOT_UPDATED_DOT+invoice.id,
					mapping: response => (response.data),
					callback: (data) => {}
				});
			});
			this.props.actions.patchRESTMultiData(patches,(responses) => {
				import("moment").then(moment => { //TODO we get an error, when we replace the literal("moment") with a constant
					var xmlDoc = document.implementation.createDocument(EMPTY, EMPTY, null);
					var TransferData = xmlDoc.createElement(TRANSFER_DATA);
					TransferData.setAttribute(XMLNS_XSI,XML_SCHEMA_INSTANCE);
					TransferData.setAttribute(XMLNS_XSD,XML_SCHEMA);
					TransferData.setAttribute(XMLNS,URN_TRANSFER);
					//xmlns:xsi= xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="urn:Transfer"
					xmlDoc.appendChild(TransferData);
					var Accountings = xmlDoc.createElement(ACCOUNTINGS);
					TransferData.appendChild(Accountings);
					if(responses) {
						responses.forEach((response) => {
							let invoice = this.props.exportToMicroInvestData.selectedRows.map.get(response.data.id);
							var Accounting = xmlDoc.createElement(ACCOUNTING);
							Accounting.setAttribute(ACCOUNTING_DATE,moment(invoice.invoiceDate).format(YYYY_MM_DD));
							Accounting.setAttribute(VIES_MONTH,moment(invoice.invoiceDate).format(YYYY_MM_DD));
							Accounting.setAttribute(DUE_DATE,moment(invoice.invoiceDate).format(YYYY_MM_DD));
							Accounting.setAttribute(TERM,TERM_VALUE);
							Accounting.setAttribute(REFERENCE,invoice.invoiceDescription);
							Accountings.appendChild(Accounting);
							var Document = xmlDoc.createElement(DOCUMENT);
							Document.setAttribute(DOCUMENT_TYPE,ONE); //DocumentType="3" credit note??
							Document.setAttribute(NUMBER,invoice.invoiceNum);
							Document.setAttribute(DATE,moment(invoice.invoiceDate).format(YYYY_MM_DD));
							Accounting.appendChild(Document);
							var Company = xmlDoc.createElement(COMPANY);
							Company.setAttribute(NAME,invoice.invoiceCounterParty.name);
							Company.setAttribute(CONTACT_NAME,invoice.invoiceCounterParty.mol);
							Company.setAttribute(BULSTAT,invoice.invoiceCounterParty.bulstat);
							Company.setAttribute(VAT_NUMBER,invoice.invoiceCounterParty.vatNo);
							Accounting.appendChild(Company);
							var Address = xmlDoc.createElement(ADDRESS);
							Address.setAttribute(LOCATION,invoice.invoiceCounterParty.address);
						//	Address.setAttribute("City",invoice.invoiceCounterParty.city); //TODO the field city is not existing in CCcPartner
							Company.appendChild(Address);
							var AccountingDetails = xmlDoc.createElement(ACCOUNTING_DETAILS);
							Accounting.appendChild(AccountingDetails);
							var AccountingDetail1 = xmlDoc.createElement(ACCOUNTING_DETAIL);
							AccountingDetail1.setAttribute(ACCOUNTING_NUMBER,ACCOUNTING_NUMBER_VALUE_411_SLASH_1);
							AccountingDetail1.setAttribute(AMOUNT,invoice.totalAmount);
							AccountingDetail1.setAttribute(DIRECTION,DEBIT);
							AccountingDetail1.setAttribute(VAT_TERM,SEVEN);
							AccountingDetails.appendChild(AccountingDetail1);
							var AccountingDetail2 = xmlDoc.createElement(ACCOUNTING_DETAIL);
							AccountingDetail2.setAttribute(ACCOUNTING_NUMBER,ACCOUNTING_NUMBER_VALUE_453_SLASH_2);
							AccountingDetail2.setAttribute(AMOUNT,invoice.taxAmount);
							AccountingDetail2.setAttribute(DIRECTION,CREDIT);
							AccountingDetail2.setAttribute(VAT_TERM,SEVEN);
							AccountingDetails.appendChild(AccountingDetail2);
							var AccountingDetail3 = xmlDoc.createElement(ACCOUNTING_DETAIL);
							AccountingDetail3.setAttribute(ACCOUNTING_NUMBER,ACCOUNTING_NUMBER_VALUE_703_SLASH_1);
							AccountingDetail3.setAttribute(AMOUNT,invoice.taxBaseAmount);
							AccountingDetail3.setAttribute(DIRECTION,CREDIT);
							AccountingDetail3.setAttribute(VAT_TERM,SEVEN);
							AccountingDetails.appendChild(AccountingDetail3);
							/*
							 * TODO no VAT
		<AccountingDetails>
		<AccountingDetail AccountNumber="411/1" Amount="792.12" Direction="Debit" VatTerm="17"/> ??foreign
		<AccountingDetail AccountNumber="703/1" Amount="792.12" Direction="Credit" VatTerm="17"/>
		</AccountingDetails>
		<AccountingDetails>
		<AccountingDetail AccountNumber="411/1" Amount="430.00" Direction="Debit" VatTerm="12"/> ??not registered
		<AccountingDetail AccountNumber="703/1" Amount="430.00" Direction="Credit" VatTerm="12"/>
		</AccountingDetails>
							 */
						});
					}
					var serializer = new XMLSerializer();
					var xmlString = serializer.serializeToString(xmlDoc);
					//console.log(xmlString);
					//download trick
					var element = document.createElement(TAG_NAME_A);
					element.setAttribute(HREF, HREF_VALUE + encodeURIComponent(xmlString));
					element.setAttribute(DOWNLOAD, DOWNLOAD_VALUE);
					element.style.display = NONE;
					document.body.appendChild(element);
					element.click();
					document.body.removeChild(element);
				});
			},PAGE_TABLE_C_INVOICE_CONTAINER_EXPORT_TO_MICROINVEST);
		}
	}
	
	render() {
		let columns = getExpandedColumns(this.props.entityName, this.props.expand);
		return (
			<div className={PAGE_BODY_WRAPPER}>
				<Header text={this.props.title} class={PAGE_HEADER_TEXT_ALIGN_CENTER_NO_MARGIN} />
				<div className={PAGE_BODY}>
					<EmbedRetrieveEntityListContainer
						key={this.props.componentPath} //help React disambiguate when changing to another table page with this component but with different path
						retrieveType = {this.props.entityName}
						componentPath = {this.props.componentPath} //existing path in redux store where we put data
						columns = {columns}
						title={this.props.title}
						icon={this.props.icon}
						expanded={true}
						asTable={true}
						editable={false}
						hasRowSelecting={this.props.hasRowSelecting}
						selectedRowsColumns={this.props.selectedRowsColumns}
					/>
				</div>
			</div>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	const entityDef = getEntityDefinition(C_INVOICES,undefined);
	const componentPath = PAGE_TABLE+entityDef.className;
	const title = ownProps.t(C_INVOICE_CLASS_NAME_PLURAL);
	let exportToMicroInvestData = resolveObjectPath(componentPath+DOT_EXPORT_TO_MICRO_INVEST,state.rest); //rest because of fetchREST
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: C_INVOICES,
		entityDef: entityDef,
		expand: ownProps.expand ? ownProps.expand : [],
		title: title,
		icon: entityDef.icon ? entityDef.icon : LIST,
		hasRowSelecting: ownProps.hasRowSelecting,
		selectedRowsColumns: ownProps.selectedRowsColumns,
		
		exportToMicroInvestData: exportToMicroInvestData,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { patchRESTMultiData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableCInvoiceContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
