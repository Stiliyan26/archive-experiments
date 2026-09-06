import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'

import { getEntityDefinition, getExpandedColumns } from '../nomenclatures/entityDefinitions.js'
import { resolveObjectPath } from '../../scripts/dataUtils';
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//Container: redux container class
class PageTransportsReportContainer extends React.Component {	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}
	
	render() {
		let report = {
				label: this.props.t("TransportsReport.title"),
				value: "transports",
				root: "transports",
				expand: [
					"relationsFromTask.transportOrder.importCustomsAgent",
					"invoiceRows.invoice.invoiceNum",
					"invoiceRows.invoice.invoiceDate",
				],
				columnOverride: {
					createdBy: {show: false},
					createdDate: {show: false},
					lastModifiedBy: {show: false},
					lastModifiedDate: {show: false},
					vehicle: {show: false},
					driver: {show: false},
					transportType : {show: false},
					unloadingDate: {show: false},
					sendDate: {show: false},
					receiveDate: {show: false},
					paymentDate: {show: false},
					isPaid: {show: false},
					confirmation: {show: false},
				},
				filter: [],
				columns: []
			};
		let columns = getExpandedColumns(report.root, report.expand, report.columnOverride);
		let body = <EmbedRetrieveEntityListContainer
					key={"scheduleReport"}
					title={report.label}
					icon={report.icon}
					columns={columns}
					componentPath={this.props.componentPath}
					retrieveType={report.root}
					defaultFilter={report.filter}
					expanded={true}
					asTable={true}
					editable={false}
					excludeRootEntity={report.excludeRootEntity}
					defaultSort={report.sort}
				/>;
		return (
			<div>
				<Header text={this.props.headerText} class='page-header text-align-center no-margin' />
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = "transports";
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = "scheduleReport";
	let viewData = resolveObjectPath(componentPath,state.rest);
	const data = viewData ? viewData[entityDef.className] : undefined;
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t("TransportsReport.title_short"),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTransportsReportContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
