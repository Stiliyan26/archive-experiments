import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import history from '../../../scripts/history.js'
import { isAuthenticated } from '../../../components/pages/login/Login.js';

import NewHeader from '../../../components/generic/NewHeader.js';
import EmbedRetrieveEntityListContainer from '../../nomenclatures/EmbedRetrieveEntityListContainer.js';

import { getEntityDefinition, getExpandedColumns } from '../../nomenclatures/entityDefinitions.js';
import { resolveObjectPath } from '../../../scripts/dataUtils.js';

import axios from 'axios';
import { LOGIN, CREATE_NEW_ELECTRICITY_INVOICE_NOTE } from "../constants/selfiePaths.js";
import { X_AUTH_TOKEN } from "../constants/selfieConstants.js";


// Page: can be used as a landing page 
// Table: presents table of the objects
// Container: redux container class
class PageTableInvoiceCorrectionContainer extends React.Component {
	componentDidMount() {
		if (!isAuthenticated(this.props.auth)) {
			history.push(LOGIN);
		}
	}

	createNewDocument(electricityInvoiceId, totalQuantity, priceInLevs) {
		let loiDocumentTypeId = 3;

		axios({
			method: "post",
			url: CREATE_NEW_ELECTRICITY_INVOICE_NOTE(API_URL, electricityInvoiceId, totalQuantity, priceInLevs, loiDocumentTypeId),
			// url: API_URL + `/reports/sumKwh/${newFromDate}/${newToDate}`,
			headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
		})
			.then(res => {
				let data = res.data;
				console.log("data response: ", data);
			})
			.catch(error => {
				console.log("PageTableInvoiceCorrectionContainer.axios.error" + error)
			});
	}

	generateCreditNote() {
		const selectedRows = this.props.data.selectedRows;

		if (selectedRows === undefined) {
			// return this.setState({ showAlert: true, alertMessage: `${this.props.t('ErrorMessages.NoEntriesWereSelected')}` });
			console.log("There are no selected invoices");

		} else {
			const selectedRows = this.props.data.selectedRows.map;
			// console.log("selectedRows: ", selectedRows);

			const testArray = Array.from(selectedRows.values()).map((obj) => obj);
			// console.log("testArray: ", testArray);

			for (let i = 0; i < testArray.length; i++) {
				console.log(testArray[i]);

				let electricityInvoiceId = testArray[i].id;
				let totalQuantity = testArray[i].totalQuantity;
				let priceInLevs = testArray[i].priceInLevs;

				console.log("electricityInvoiceId : ", electricityInvoiceId);
				console.log("totalQuantity : ", totalQuantity);
				console.log("priceInLevs : ", priceInLevs);

				this.createNewDocument(electricityInvoiceId, totalQuantity, priceInLevs);
			}
		}
	}

	render() {
		// let columns = getEntityDefinition(this.props.entityName, undefined).columns; //{ month: { isReadOnly: true }, code: { isReadOnly: true }}).columns;
		let columns = getExpandedColumns(this.props.entityName, this.props.expand);
		console.log("this.props: ", this.props);
		console.log("this: ", this);

		return (
			<div className="page-body-wrapper">
				{/* <NewHeader text={this.props.title} auth={this.props.auth.userAuthenticated} /> */}

				<div className='page-body'>
					<div style={{ display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", marginBottom: "0.5rem" }}>
						<button className="button" style={{ margin: "0.5rem" }} onClick={() => { this.generateCreditNote(); }}>
							{this.props.t("Selfie.GenerateCreditNote")}
						</button>
					</div>

					<EmbedRetrieveEntityListContainer
						retrieveType={this.props.entityName}
						componentPath={this.props.componentPath} //existing path in redux store where we put data
						columns={columns}
						title={this.props.t("ElectricityInvoice.invoiceCorrection")}
						icon={this.props.icon}
						expanded={true}
						asTable={true}
						editable={true}
						creatable={false}
						hasRowSelecting={true}
						selectedRowsColumns={this.props.selectedRowsColumns}
						defaultFilter={[{ id: "isValid", value: true }, { id: "hasDbFile", value: true }]}
					/>
				</div>
			</div>
		);
	}
}

// redux mapping of props
function mapStateToProps(state, ownProps) {
	const entityDef = getEntityDefinition("electricityInvoices", undefined);
	const componentPath = "PageTableInvoiceCorrectionContainer" + entityDef.className;
	const title = ownProps.t("ElectricityInvoice.invoiceCorrection");
	const data = resolveObjectPath(componentPath, state.rest)
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: "electricityInvoices",
		entityDef: entityDef,
		expand: ownProps.expand ? ownProps.expand : [],
		title: title,
		icon: entityDef.icon ? entityDef.icon : "list",
		data: data,
		hasRowSelecting: ownProps.hasRowSelecting,
		selectedRowsColumns: ownProps.selectedRowsColumns,
	};
}

// redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {}), dispatch)
	};
}

// export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableInvoiceCorrectionContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
