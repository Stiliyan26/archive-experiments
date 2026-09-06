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
class PageViewTransportContainer extends React.Component {	
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
				const taskRelationsToDef = getEntityDefinition("taskRelations",{
					relation: {show: false},
					transport: {show: false}
				});
				const attachmentDef = getEntityDefinition("taskAttachments",{task: {show: false}});
				const commentsDef = getEntityDefinition("comments",{task: {show: false}});
				const invoiceRowsDef = getEntityDefinition("invoiceRows",{transport: {show: false}});
				body = <div className='page-body'>
						<EmbedRetrieveEntityListContainer
							title={this.props.t("Transport.relationsToTask")}
							icon="level-up-alt"
							columns={taskRelationsToDef.columns}
							componentPath={this.props.componentPath+".relationsTo"}
							retrieveType="taskRelations"
							parentHref={this.props.href}
							parentAttr="toTask"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={this.props.t("Transport.taskAttachments")}
							icon={attachmentDef.icon}
							columns={attachmentDef.columns}
							componentPath={this.props.componentPath+".attachments"}
							retrieveType="taskAttachments"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={commentsDef.label}
							icon={commentsDef.icon}
							columns={commentsDef.columns}
							componentPath={this.props.componentPath+".comments"}
							retrieveType="comments"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
							// expanded={true}
						/>
						<EmbedRetrieveEntityListContainer
							title={invoiceRowsDef.label}
							icon={invoiceRowsDef.icon}
							editable={false}
							columns={invoiceRowsDef.columns}
							componentPath={this.props.componentPath + ".invoiceRows"}
							retrieveType="invoiceRows"
							parentHref={this.props.href}
							parentAttr="transport"
							parentData={this.props.data}
							onCommitChange={()=>{this.onChildUpdate();}}
						/>
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+".changelog"} retrieveType={this.props.entityName}/>
					</div>;
			}
		}
		return (
			<div className="page-body-wrapper">
				<Header text={this.props.headerText} class='page-header text-align-center no-margin' />
				<EmbedEntityContainer 
					retrieve_id={this.props.match.params.entity_id} 
					componentPath={this.props.componentPath+"."+this.props.entityDef.className}
					onBeforeChange={(data) => {
						if(!this.props.entityDef.pageURL) {console.error("pageURL is missing for entity "+this.props.entityDef.className);}
						if(data.id) {
							history.push(this.props.entityDef.pageURL+'/'+data.id);
						} else {
							history.push(this.props.entityDef.pageURL+'/add');
						}}}
					entityName={"transports"}
					headerText={this.props.t("Transport._className")}
					creatable={true}
					expanded={true}
				/>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = "transports";
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = "transportView";
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
		headerText: ownProps.t("Transport._className"),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewTransportContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
