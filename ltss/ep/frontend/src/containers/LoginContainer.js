import React, { Component, 	PropTypes } from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import Login from '../components/pages/login/Login';
import {signIn, logout, login } from '../actions/index';


class LoginContainer extends Component {
	render() {
		const {actions, auth} = this.props;
		return (
				<Login actions={Object.assign(actions)} auth={auth}/>
			);
	}
}

function mapStateToProps(state) {
	const props = { auth: state.auth };
	return props;
}
function mapDispatchToProps(dispatch) {
	let actions = {signIn, logout, login};
	return {
			actions: bindActionCreators(actions, dispatch)
		};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(LoginContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default WithRouterComponent;