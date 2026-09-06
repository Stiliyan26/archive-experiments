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
import EmbedEmployeeAttestationContainer from '../embeds/EmbedEmployeeAttestationContainer'
import EmbedChangeHistory from '../embeds/EmbedChangeHistory'

import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewEmployeeAttestationContainer extends React.Component {
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
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.employeeAttestations_id,10)) {
				body = <EmbedChangeHistory entity_id={this.props.id} componentPath='employeeAttestationView.changelog' retrieveType="employeeAttestations"/>;
			}
		}
		return (
			<div>
				<Header text={this.props.t("EmployeeAttestation._className")} class='page-header text-align-center no-margin' />
				<EmbedEmployeeAttestationContainer
						retrieve_id={this.props.match.params.employeeAttestations_id}
						componentPath='employeeAttestationView.employeeAttestation'
						onChange={(data) => {
							if(data.id) {
								history.push('/employeeAttestations/'+data.id);
							} else {
								history.push('/employeeAttestations/add');
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
	const data = state.rest.employeeAttestationView ? state.rest.employeeAttestationView.employeeAttestation : undefined;
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

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewEmployeeAttestationContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
