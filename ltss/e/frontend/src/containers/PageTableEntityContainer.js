import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import history from './../scripts/history'
import { isAuthenticated } from './../components/pages/login/Login.js'

import Header from './../components/generic/Header'
import NewHeader from './../components/generic/NewHeader'
import EmbedRetrieveEntityListContainer from './nomenclatures/EmbedRetrieveEntityListContainer'

import { getEntityDefinition, getExpandedColumns } from './nomenclatures/entityDefinitions.js'

//Page: can be used as a landing page
//Table: presents table of the objects
//Container: redux container class
class PageTableEntityContainer extends React.Component {

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}

	render() {
		let columns = getExpandedColumns(this.props.entityName, this.props.expand, this.props.columnOverride);
		return (
			<div className="page-body-wrapper">
				{/* <Header text={this.props.title} class='page-header text-align-center no-margin' /> */}
				<NewHeader text={this.props.title} auth={this.props.auth.userAuthenticated} />
				<div className='page-body'>
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
						defaultFilter={this.props.defaultFilter}
					/>
				</div>
			</div>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	const entityDef = getEntityDefinition(ownProps.entityName,ownProps.columnOverride,ownProps.entityOverride);
	const title = ownProps.title ? ownProps.title : entityDef.label_plural;
	return {
		auth: state.auth,
		componentPath: ownProps.componentPath ? ownProps.componentPath : "PageTable"+entityDef.className,
		entityName: ownProps.entityName,
		entityDef: entityDef,
		entityOverride: ownProps.entityOverride,
		expand: ownProps.expand ? ownProps.expand : [],
		title: title,
		icon: entityDef.icon ? entityDef.icon : "list",
		hasRowSelecting: ownProps.hasRowSelecting,
		selectedRowsColumns: ownProps.selectedRowsColumns,
		columnOverride: ownProps.columnOverride,
		defaultFilter: ownProps.defaultFilter
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableEntityContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);