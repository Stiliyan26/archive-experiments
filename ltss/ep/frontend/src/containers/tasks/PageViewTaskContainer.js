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
import EmbedTaskContainer from '../embeds/EmbedTaskContainer'
import EmbedPlannedResourcesContainer from '../embeds/EmbedPlannedResourcesContainer'
import EmbedActualResourcesContainer from '../embeds/EmbedActualResourcesContainer'
import EmbedTaskPlannedActualResourcesContainer from '../embeds/EmbedTaskPlannedActualResourcesContainer'
import EmbedChangeHistory from '../embeds/EmbedChangeHistory'
import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewTaskContainer extends React.Component {
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
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.task_id,10)) {
				const taskRelationsFromDef = getEntityDefinition("taskRelations",{fromTask: {show: false}});
				const taskRelationsToDef = getEntityDefinition("taskRelations",{toTask: {show: false}});
				const attachmentDef = getEntityDefinition("taskAttachments",{task: {show: false}});
				const commentsDef = getEntityDefinition("comments",{task: {show: false}});
				const watchersDef = getEntityDefinition("taskWatchers",{task: {show: false}});
				body = <div className='page-body'>
						<EmbedRetrieveEntityListContainer
							title={this.props.t("Task.relationsFromTask")}
							icon="level-down-alt"
							columns={taskRelationsFromDef.columns}
							componentPath="taskView.relationsFrom"
							retrieveType="taskRelations"
							parentHref={this.props.href}
							parentAttr="fromTask"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={this.props.t("Task.relationsToTask")}
							icon="level-up-alt"
							columns={taskRelationsToDef.columns}
							componentPath="taskView.relationsTo"
							retrieveType="taskRelations"
							parentHref={this.props.href}
							parentAttr="toTask"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={attachmentDef.label}
							icon={attachmentDef.icon}
							columns={attachmentDef.columns}
							componentPath="taskView.attachments"
							retrieveType="taskAttachments"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={commentsDef.label}
							icon={commentsDef.icon}
							columns={commentsDef.columns}
							componentPath="taskView.comments"
							retrieveType="comments"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
							// expanded={true}
						/>
						<EmbedPlannedResourcesContainer href={this.props.href} componentPath='taskView.planned'/>
						<EmbedActualResourcesContainer href={this.props.href} componentPath='taskView.actual'/>
						<EmbedTaskPlannedActualResourcesContainer task_id={this.props.id} componentPath='taskView.plan_actual'/>
						<EmbedRetrieveEntityListContainer
							title={watchersDef.label}
							icon={watchersDef.icon}
							columns={watchersDef.columns}
							componentPath="taskView.watchers"
							retrieveType="taskWatchers"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
						/>
						<EmbedChangeHistory entity_id={this.props.id} componentPath='taskView.changelog' retrieveType="tasks"/>
					</div>; //TODO add watchers list
			}
		}
		return (
			<div className="page-body-wrapper">
				<Header text={this.props.t("Task._className")} class='page-header text-align-center no-margin' />
				<EmbedTaskContainer
					retrieve_id={this.props.match.params.task_id}
					componentPath='taskView.task'
					onChange={(data) => {
						if(data.id) {
							history.push('/tasks/'+data.id);
						} else {
							history.push('/tasks/add');
						}}}
					expanded={true}
				/>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const data = state.rest.taskView ? state.rest.taskView.task : undefined;
	return {
		auth: state.auth,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewTaskContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
