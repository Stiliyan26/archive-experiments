import React, {
	Component,
	PropTypes
} from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import { withTranslation } from 'react-i18next';
import history from './../../scripts/history';
import './NewHeaderStyle.css';
import * as authActions from './../../actions/auth';
import { Dropdown } from 'react-bootstrap';
import { builderDataToProjection } from '../../containers/nomenclatures/entityDefinitions.js';
import { fetchRESTFollow } from '../../actions/taskActions';
import { fetchRESTAll } from '../../scripts/dataUtils.js';
import Announcement from '../shared/Announcement.js';
// import { builderDataToProjection } from './nomenclatures/entityDefinitions.js'
// import { fetchRESTFollow } from '../actions/taskActions';
// import { fetchRESTAll } from '../scripts/dataUtils.js';


export function hasPermission(currentPermissions, permissionCode) {
	let permissions = currentPermissions && currentPermissions._embedded && currentPermissions._embedded.secPermissions ? currentPermissions._embedded.secPermissions : undefined;
	//console.warn(' hasPermission ', permissions);
	if (permissions && permissions instanceof Array) {
		return permissions.find(item => item && item.code == permissionCode);
	}
	return undefined;
}

class NewHeader extends React.Component {
	retrieveData() {
		//if loading, no need to retrieve again
		if (!this.props.currentUser && this.props.auth && this.props.auth.username && this.props.auth.userAuthenticated) {
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL + "/reports/builder/1",
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
					builderDataToProjection(newData, "secUsers");
					return newData;
				},
				'NewHeader.retrieveData',
				{}
			);
			let accumulatedData = [];
			fetchRESTAll(
				this.props.actions.fetchRESTFollow,
				{
					url: API_URL + "/reports/builder/1",
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
				'NewHeader.retrieveData',
				{}
			);
		}
	}

	componentDidMount() {
		this.retrieveData();
	}

	componentDidUpdate(prevProps, prevState) {
		this.retrieveData();
	}

	logout() {
		authActions.logout();
	}

	navigateTo(endPoint) {
		history.push(endPoint);
	}

	render() {
		// console.log("NewHeader -> this.props.auth.userAuthenticated ->: ", this.props)
		// console.log("NewHeader -> Authenticated ->: ", authActions)
		const { modal } = this.props;

		document.title = this.props.t("ModuleName") + " " + this.props.text;

		const endPoints = {
			user: '/secUsers',
			login: '/login',
			import: '/import',
			nomenclatures: '/nomenclatures',
			secRoles: '/secRoles',
		};

		return (

			<header className="header">
				<div className="user-info">
					{this.props.text}
				</div>
				<div className="header-buttons">
					{/* <div class="button"><span>Change Language</span></div> */}
					<div className="button-container">
						<Dropdown
						>
							<Dropdown.Toggle
								variant="danger"
								id='dropdown-style'
							>
								{this.props.t("ChangeLanguage")}
							</Dropdown.Toggle>

							<Dropdown.Menu>
									<Dropdown.Item
										key="en"
										as="button"
										onClick={() => { this.props.i18n.changeLanguage("en"); history.go(0); }}
									>
										{this.props.t("English")}
									</Dropdown.Item>

									<Dropdown.Item
										key="bg"
										as="button"
										onClick={() => { this.props.i18n.changeLanguage("bg"); history.go(0); }}
									>
										{this.props.t("Български")}
									</Dropdown.Item>
							</Dropdown.Menu>
						</Dropdown>

						{/* <div className="customDropdown">
							<button>{this.props.t("ChangeLanguage")}</button>
							<div className="customDropdown-content">
								<a onClick={() => { this.props.i18n.changeLanguage("en"); history.go(0); }}>{this.props.t("English")}</a>
								<a onClick={() => { this.props.i18n.changeLanguage("bg"); history.go(0); }}>{this.props.t("Български")}</a>
							</div>
						</div> 
						*/}
					</div>
					<div className="button" onClick={() => {
						if (this.props.auth.userAuthenticated) {
							this.props.auth.userAuthenticated = false;
							this.logout();
						} else {
							this.navigateTo(endPoints.login);
						}
					}}>
						<span>
							{(() => {
								if (this.props.auth.userAuthenticated) {
									return <span> {this.props.t("LogOut")} </span>
								}
								else {
									return <span> {this.props.t("LogIn")} </span>
								}
							}
							)()}
						</span>
					</div>
					{/* <div class="button"><span>Log Out</span></div> */}
					<Announcement
						visible={modal.visible}
						title={modal.title}
						message={modal.body}
						acceptLabel={modal.acceptLabel}
						acceptCallback={modal.acceptCallback}
						refuseLabel={modal.refuseLabel}
						refuseCallback={modal.refuseCallback}
					/>
				</div>
			</header>
		);
	}
}

NewHeader.displayName = 'GenericHeader';
NewHeader.propTypes = {};
NewHeader.defaultProps = {};

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

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(NewHeader);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);

//export default withTranslation()(NewHeader);