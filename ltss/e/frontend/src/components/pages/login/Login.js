
import React from 'react';

import './selfie.css';

import {
	BrowserRouter as Router,
	Route,
	Redirect,
	withRouter
} from 'react-router-dom'
import { Row, Col, Button } from 'react-bootstrap';
import Header from '../../generic/Header'
import NewHeader from '../../generic/NewHeader'
import history from './../../../scripts/history'
import classNames from 'classnames'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

const CUSTOM_CARD = 'customButton'

export function isAuthenticated(auth) {
	return auth.userAuthenticated && sessionStorage.getItem('X-AUTH-TOKEN')
}

class Login extends React.Component {
	redirectHome() {
		//history.push('/notifications')
		history.push('/dashboardScreen')
	}

	login() {
		this.props.actions.login({ username: this.refs.username.value, password: this.refs.password.value })
	}

	componentDidUpdate() {
		if (isAuthenticated(this.props.auth)) {
			this.redirectHome()
		}
	}

	componentDidMount() {
		if (isAuthenticated(this.props.auth)) {
			this.redirectHome()
		}
	}

	loginKeyPress(e) {
		if (e.charCode == 13) {
			this.login()
		}
	}

	render() {
		let loginBtnContent = this.props.auth.isLoggingIn ?
			(<div>
				{this.props.t("Logging...")}
				<FontAwesomeIcon icon="spinner" spin />
			</div>) : this.props.t("LogIn");

		let messageClasses = classNames({
			"text-align-center": true,
			"login-msg-wrapper": true,
			"fadeout": this.props.auth.message === "",
			"fadein": this.props.auth.message != ""
		})

		return (
			<div className="users-wrapper">

				<div className="clients-header-wrapper" >
					{/* <Header text={this.props.t("LogIn")} class='page-header text-align-center no-margin inline-block col-sm-12' /> */}
					<NewHeader text={this.props.t("LogIn")} auth={this.props.auth.userAuthenticated}  />
				</div>
				<div className="page-body">
					<Row className='form-group'>
						<Col md={{ span: 4, offset: 4 }} lg={{ span: 4, offset: 4 }} className="login-form">
							{this.props.auth.message ?
								<div className={messageClasses}>
									<span className="error-msg"> {this.props.auth.message} </span>
								</div>
								: undefined
							}

							<div class="login-card">
								<form>
									<input ref="username" type="text" placeholder={this.props.t("Username")} onKeyPress={(ev) => this.loginKeyPress(ev)} required />
									<input ref="password" type="password" placeholder={this.props.t("Password")} onKeyPress={(ev) => this.loginKeyPress(ev)} required />
									<button onClick={() => { this.login() }}>{loginBtnContent}</button>
								</form>
							</div>
							{/* <div className="form-group">
								<input ref="username" className="form-control" type="text" placeholder={this.props.t("Username")} onKeyPress={(ev) => this.loginKeyPress(ev)} />
							</div>
							<div className="form-group">
								<input ref="password" className="form-control" type="password" placeholder={this.props.t("Password")} onKeyPress={(ev) => this.loginKeyPress(ev)} />
							</div> */}
							{/* <Button className="col-sm-12" onClick={() => {this.login()}}>{loginBtnContent}</Button> */}
						</Col>
					</Row>
				</div>
			</div>
		)
	}
}

/* export default AuthExample */
Login.displayName = 'Login';
Login.propTypes = {};
Login.defaultProps = {};

export default withTranslation()(Login);
