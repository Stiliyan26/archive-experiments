import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants';
import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import EmbedEntityContainer from '../embeds/EmbedEntityContainer'
import EmbedChangeHistory from '../embeds/EmbedChangeHistory'

import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'
import { resolveObjectPath } from '../../scripts/dataUtils';
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewIncomeContainer extends React.Component {	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}
	
	render() {
		let body = '';
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const expensesDef = getEntityDefinition("attachableRevenuesAndExpenseses",{attachable: {show: false}});
				const assetAttachmentDef = getEntityDefinition("assetAttachments",{attachmentToAsset: {show: false}});
				const employeeAttachmentDef = getEntityDefinition("employeeAttachments",{attachmentToEmployee: {show: false}});
				const taskAttachmentDef = getEntityDefinition("taskAttachments",{attachment: {show: false}});
				body = <div className='page-body'>
						<EmbedRetrieveEntityListContainer
							title={expensesDef.label}
							icon={expensesDef.icon}
							columns={expensesDef.columns}
							componentPath={this.props.componentPath+".attachableRevenuesAndExpenses"}
							retrieveType="attachableRevenuesAndExpenseses"
							parentHref={this.props.href}
							parentAttr="attachable"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={assetAttachmentDef.label}
							icon={assetAttachmentDef.icon}
							columns={assetAttachmentDef.columns}
							componentPath={this.props.componentPath+".assetAttachments"}
							retrieveType="assetAttachments"
							parentHref={this.props.href}
							parentAttr="attachmentToAsset"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={employeeAttachmentDef.label}
							icon={employeeAttachmentDef.icon}
							columns={employeeAttachmentDef.columns}
							componentPath={this.props.componentPath+".employeeAttachments"}
							retrieveType="employeeAttachments"
							parentHref={this.props.href}
							parentAttr="attachmentToEmployee"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={taskAttachmentDef.label}
							icon={taskAttachmentDef.icon}
							columns={taskAttachmentDef.columns}
							componentPath={this.props.componentPath+".taskAttachments"}
							retrieveType="taskAttachments"
							parentHref={this.props.href}
							parentAttr="attachment"
							parentData={this.props.data}
						/>
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+".changelog"} retrieveType={this.props.entityName}/>
					</div>;
			}
		}
		return (
			<div className="page-body-wrapper">
				<Header text={this.props.headerText} class='page-header text-align-center no-margin' />
				<EmbedEntityContainer
					componentPath={this.props.componentPath+"."+this.props.entityDef.className}
					loading={this.props.loading}
					onBeforeChange={(data) => {
						if(!this.props.entityDef.pageURL) {console.error("pageURL is missing for entity "+this.props.entityDef.className);}
						if(data.id) {
							history.push(this.props.entityDef.pageURL+'/'+data.id);
						} else {
							history.push(this.props.entityDef.pageURL+'/add');
						}}}
					retrieve_id={this.props.match.params.entity_id}
					entityName={"incomes"}
					headerText={this.props.t("Income._className")}
					expanded={true}
					creatable={true}
				/>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = "incomes";
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = "incomeView";
	let viewData = resolveObjectPath(componentPath,state.rest);
	let data = undefined;
	if(viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded 
			&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
		data = viewData[entityDef.className]._embedded[entityName][0];
	}
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t("Income._className"),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewIncomeContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
