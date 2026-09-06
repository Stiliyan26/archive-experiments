import React, {
	Component,
	PropTypes
} from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import * as authActions from '../actions/auth';
import InfoBar from '../components/shared/InfoBar';
import { withRouter } from 'react-router-dom'

import { builderDataToProjection } from './nomenclatures/entityDefinitions.js'
import { fetchRESTFollow } from '../actions/taskActions';
import { fetchRESTAll } from '../scripts/dataUtils.js';


export function hasPermission(currentPermissions, permissionCode) {
	let permissions = currentPermissions && currentPermissions._embedded && currentPermissions._embedded.secPermissions ? currentPermissions._embedded.secPermissions : undefined;
	//console.warn(' hasPermission ', permissions);
	if(permissions && permissions instanceof Array) {
		return permissions.find(item => item && item.code == permissionCode);
	}
	return undefined;
}

class InfoBarContainer extends Component {
	
	retrieveData() {
		//if loading, no need to retrieve again
		if(!this.props.currentUser && this.props.auth && this.props.auth.username && this.props.auth.userAuthenticated) {
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/builder/1",
					params: {
						from: "SecUser",
						select: "SecUser",
						"name": this.props.auth.username,
						size: 1000,
					},
				},
				"currentUser",
				(response) => {
					let newData = response.data;
					//transform data to "repository response"-like
					builderDataToProjection(newData,"secUsers");
					return newData;
				},
				'InfoBarContainer.retrieveData',
				{}
			);
			let accumulatedData = [];
			fetchRESTAll(
				this.props.actions.fetchRESTFollow,
				{
					url: API_URL+"/reports/builder/1",
					params: {
						from: "SecUserRole",
						select: "SecUserRole.role.permissions.permission",
						"user.name": this.props.auth.username,
					},
				},
				"currentPermissions",
				(response) => {
					let newData = response.data;
					accumulatedData = accumulatedData.concat(newData._embedded.hashMaps.map(item => item["SecUserRole.role.permissions.permission"]));
					newData._embedded.secPermissions = accumulatedData;
					return newData;
				},
				'InfoBarContainer.retrieveData',
				{}
			);
		}
	}

	componentDidMount(){
		this.retrieveData();
	}

	componentDidUpdate(prevProps, prevState) {
		this.retrieveData();
	}
	
	render() {
		const { authActions, home, auth, modal} = this.props;
		return (
				<InfoBar actions={Object.assign({}, authActions)} home={home} auth={auth} modal={modal} />
			);
	}
}

function mapStateToProps(state) {
	const props = { home: state.home, auth: state.auth, modal: state.modal, currentUser: state.rest.currentUser };
	return props;
}
function mapDispatchToProps(dispatch) {
	return {
		authActions: bindActionCreators(authActions, dispatch),
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(InfoBarContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default WithRouterComponent;
