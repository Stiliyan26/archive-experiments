import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

import { getEntityDefinition, getExpandedColumns } from '../nomenclatures/entityDefinitions.js'

const LOGIN = "/login"

//Page: can be used as a landing page 
//Table: presents table of the objects
//Container: redux container class
class PageTableScheduleContainer extends React.Component {

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push(LOGIN)
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
						componentPath = {this.props.componentPath} //existing path in redux store where we put data
						columns = {columns}
						title={this.props.t("Schedule._className_plural")}
						icon={this.props.icon}
						expanded={true}
						asTable={true}
						editable={false}
						hasRowSelecting={true}
						selectedRowsColumns={this.props.selectedRowsColumns}
					/>
				</div>
			</div>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	const entityDef = getEntityDefinition("schedules",undefined);
	const componentPath = "PageTable"+entityDef.className;
	const title = ownProps.t("Schedule._className_plural");
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: "schedules",
		entityDef: entityDef,
		expand: ownProps.expand ? ownProps.expand : [],
		title: title,
		icon: entityDef.icon ? entityDef.icon : "list",
		hasRowSelecting: ownProps.hasRowSelecting,
		selectedRowsColumns: ownProps.selectedRowsColumns,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, {  }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableScheduleContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
