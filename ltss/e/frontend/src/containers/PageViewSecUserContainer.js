import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import * as Constants from './../static/constants';
import history from './../scripts/history'
import { isAuthenticated } from './../components/pages/login/Login.js'

import Header from './../components/generic/Header'
import EmbedEntityContainer from './embeds/EmbedEntityContainer'
import EmbedChangeHistory from './embeds/EmbedChangeHistory'

import { getEntityDefinition, getExpandedColumns } from './nomenclatures/entityDefinitions.js'
import { resolveObjectPath } from './../scripts/dataUtils';
import EmbedRetrieveEntityListContainer from './nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewSecUserContainer extends React.Component {	
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
			} else if(this.props.id && this.props.id == parseInt(this.props.userId,10)) {
				const secUserRolesDef = getEntityDefinition("secUserRoles",{user: {show: false}});
				let permissionsColumns = getExpandedColumns("secUserRoles", 
						["role.permissions"], 
						{user: {show: false},});
				body = <div className='page-body'>
						<EmbedRetrieveEntityListContainer
							title={secUserRolesDef.label_plural}
							icon={secUserRolesDef.icon}
							expanded={true}
							columns={secUserRolesDef.columns}
							componentPath={this.props.componentPath+"._secUserRoles"}
							retrieveType="secUserRoles"
							parentHref={this.props.href}
							parentAttr="user"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={"Права за достъп"}
							columns={permissionsColumns}
							componentPath="secUser.permissionsReport"
							retrieveType="secUserRoles"
							asTable={true}
							editable={false}
							expanded={true}
							defaultFilter={[
								{where: {
										op: "equal",
										operands: ["user.id", {literal: this.props.userId}],
									},
								}]}
							/>
					</div>;
			}
		}
		return (
			<div className="page-body-wrapper">
				<Header text={this.props.headerText} class='page-header text-align-center no-margin' />
				<EmbedEntityContainer
					componentPath={this.props.componentPath+"."+this.props.entityDef.className}
					retrieve_id={this.props.userId}
					onBeforeChange={(data) => {
						if(!this.props.entityDef.pageURL) {console.error("pageURL is missing for entity "+this.props.entityDef.className);}
						if(data.id) {
							history.push(this.props.entityDef.pageURL+'/'+data.id);
						} else {
							history.push(this.props.entityDef.pageURL+'/add');
						}}}
					entityName={"secUsers"}
					headerText={this.props.t("SecUser._className")}
					creatable={true}
					expanded={true}
				/>;
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = "secUsers";
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = "secUserView";
	let viewData = resolveObjectPath(componentPath,state.rest);
	let data = undefined;
	if(viewData && viewData[entityDef.className] && viewData[entityDef.className]._embedded 
			&& viewData[entityDef.className]._embedded[entityName] instanceof Array) {
		data = viewData[entityDef.className]._embedded[entityName][0];
	}
	let userId = ownProps.match && ownProps.match.params && ownProps.match.params.entity_id ? ownProps.match.params.entity_id 
			: (state.rest.currentUser && state.rest.currentUser._embedded && state.rest.currentUser._embedded.secUsers[0] ? state.rest.currentUser._embedded.secUsers[0].id : undefined);
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		userId: userId,
		//UI
		headerText: ownProps.t("SecUser._className"),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewSecUserContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
