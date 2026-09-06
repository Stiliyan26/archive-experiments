import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { Button } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

import { getEntityDefinition, getExpandedColumns } from '../nomenclatures/entityDefinitions.js'
import { resolveObjectPath } from '../../scripts/dataUtils';
import { patchRESTMultiData } from '../../actions/taskActions';

//Page: can be used as a landing page
//Table: presents table of the objects
//Container: redux container class
class PageTableInvoicesContainer extends React.Component {

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}
	
	exportToMicroinvest() {
		//mark invoices as sent
		//check data availability
		if(this.props.exportToMicroInvestData && this.props.exportToMicroInvestData.selectedRows && this.props.exportToMicroInvestData.selectedRows.map) {
			let patches = [];
			let _fieldPath = "isExported"
			let editData = true;
			this.props.exportToMicroInvestData.selectedRows.map.forEach((invoice) => {
				patches.push({
					restConfig: {
						method: 'patch',
						url: API_URL+"/"+this.props.entityName+"/"+invoice.id,
						data: [{ "op": "add", "path": "/"+_fieldPath, "value": editData }],
						headers: {'Content-Type': 'application/json-patch+json'}
					},
					statePath: this.props.componentPath+"._exportToMicroInvest._updated."+invoice.id,
					mapping: response => (response.data),
					callback: (data) => {}
				});
			});
			this.props.actions.patchRESTMultiData(patches,(responses) => {
				import("moment").then(moment => {
					var xmlDoc = document.implementation.createDocument("", "", null);
					var TransferData = xmlDoc.createElement("TransferData");
					TransferData.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
					TransferData.setAttribute("xmlns:xsd","http://www.w3.org/2001/XMLSchema");
					TransferData.setAttribute("xmlns","urn:Transfer");
					//xmlns:xsi= xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="urn:Transfer"
					xmlDoc.appendChild(TransferData);
					var Accountings = xmlDoc.createElement("Accountings");
					TransferData.appendChild(Accountings);
					if(responses) {
						responses.forEach((response) => {
							let invoice = this.props.exportToMicroInvestData.selectedRows.map.get(response.data.id);
							var Accounting = xmlDoc.createElement("Accounting");
							Accounting.setAttribute("AccountingDate",moment(invoice.invoiceDate).format("YYYY-MM-DD"));
							Accounting.setAttribute("ViesMonth",moment(invoice.invoiceDate).format("YYYY-MM-DD"));
							Accounting.setAttribute("DueDate",moment(invoice.invoiceDate).format("YYYY-MM-DD"));
							Accounting.setAttribute("Term","Продажби");
							Accounting.setAttribute("Reference",invoice.invoiceDescription);
							Accountings.appendChild(Accounting);
							var Document = xmlDoc.createElement("Document");
							Document.setAttribute("DocumentType","1"); //DocumentType="3" credit note??
							Document.setAttribute("Number",invoice.invoiceNum);
							Document.setAttribute("Date",moment(invoice.invoiceDate).format("YYYY-MM-DD"));
							Accounting.appendChild(Document);
							var Company = xmlDoc.createElement("Company");
							Company.setAttribute("Name",invoice.invoiceCounterParty.name);
							Company.setAttribute("ContactName",invoice.invoiceCounterParty.mol);
							Company.setAttribute("Bulstat",invoice.invoiceCounterParty.eik);
							Company.setAttribute("VatNumber",invoice.invoiceCounterParty.vatNumber);
							Accounting.appendChild(Company);
							var Address = xmlDoc.createElement("Address");
							Address.setAttribute("Location",invoice.invoiceCounterParty.address);
							Address.setAttribute("City",invoice.invoiceCounterParty.city);
							Company.appendChild(Address);
							var AccountingDetails = xmlDoc.createElement("AccountingDetails");
							Accounting.appendChild(AccountingDetails);
							var AccountingDetail1 = xmlDoc.createElement("AccountingDetail");
							AccountingDetail1.setAttribute("AccountNumber","411/1");
							AccountingDetail1.setAttribute("Amount",invoice.totalAmount);
							AccountingDetail1.setAttribute("Direction","Debit");
							AccountingDetail1.setAttribute("VatTerm","7");
							AccountingDetails.appendChild(AccountingDetail1);
							var AccountingDetail2 = xmlDoc.createElement("AccountingDetail");
							AccountingDetail2.setAttribute("AccountNumber","453/2");
							AccountingDetail2.setAttribute("Amount",invoice.taxAmount);
							AccountingDetail2.setAttribute("Direction","Credit");
							AccountingDetail2.setAttribute("VatTerm","7");
							AccountingDetails.appendChild(AccountingDetail2);
							var AccountingDetail3 = xmlDoc.createElement("AccountingDetail");
							AccountingDetail3.setAttribute("AccountNumber","703/1");
							AccountingDetail3.setAttribute("Amount",invoice.taxBaseAmount);
							AccountingDetail3.setAttribute("Direction","Credit");
							AccountingDetail3.setAttribute("VatTerm","7");
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
					var element = document.createElement('a');
					element.setAttribute('href', 'data:text/xml;charset=utf-8,' + encodeURIComponent(xmlString));
					element.setAttribute('download', "exportToMicroinvest.xml");
					element.style.display = 'none';
					document.body.appendChild(element);
					element.click();
					document.body.removeChild(element);
				});
			},'PageTableInvoicesContainer.exportToMicroinvest');
		}
	}

	render() {
		let columns = getExpandedColumns(this.props.entityName, this.props.expand);
		return (
			<div className="page-body-wrapper">
				<Header text={this.props.title} class='page-header text-align-center no-margin' />
				<div className='page-body'>
					<EmbedRetrieveEntityListContainer
						retrieveType = {this.props.entityName}
						componentPath = {this.props.componentPath+"._exportToMicroInvest"} //existing path in redux store where we put data
						columns = {columns}
						title={this.props.t("Invoice.ExportToMicroInvest")}
						icon={this.props.icon}
						expanded={false}
						asTable={true}
						editable={false}
						hasRowSelecting={true}
						selectedRowsColumns={this.props.selectedRowsColumns}
						defaultFilter={[{id: "isIssued", value: true},{id: "isExported", value: false}]}
					>
						<Button onClick={() => {this.exportToMicroinvest();}}>{this.props.t("Export")}</Button>
					</EmbedRetrieveEntityListContainer>
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
	const entityDef = getEntityDefinition("invoices",undefined);
	const componentPath = "PageTable"+entityDef.className;
	const title = ownProps.t("Invoice._className_plural");
	let exportToMicroInvestData = resolveObjectPath(componentPath+"._exportToMicroInvest",state.rest); //rest because of fetchREST
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: "invoices",
		entityDef: entityDef,
		expand: ownProps.expand ? ownProps.expand : [],
		title: title,
		icon: entityDef.icon ? entityDef.icon : "list",
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableInvoicesContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
