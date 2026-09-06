import React from 'react';
import { withTranslation } from 'react-i18next';

import history from './../../scripts/history'
import Button from '../generic/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import Announcement from './Announcement'

import { Dropdown } from 'react-bootstrap';

class InfoBar extends React.Component {
	
	logout(){
		this.props.actions.logout();
	}

	navigateTo(endPoint){
		history.push(endPoint)
	}

	render() {
		const { modal } = this.props;
		const endPoints = {
			user: '/secUsers',
			login: '/login',
			import: '/import',
			nomenclatures: '/nomenclatures',
			secRoles: '/secRoles',
		}
		return (
			<div className="info-bar">
				<Dropdown variant="outline-dark">
					<Dropdown.Toggle>
						<FontAwesomeIcon icon="language"/>
					</Dropdown.Toggle>
					<Dropdown.Menu as="ul">
						<Dropdown.Item as="li" className="text-align-right cursor-pointer" onClick={() => {this.props.i18n.changeLanguage("en");history.go(0);}}>
							<span> {this.props.t("English")} </span>
						</Dropdown.Item>
						<Dropdown.Item as="li" className="text-align-right cursor-pointer" onClick={() => {this.props.i18n.changeLanguage("bg");history.go(0);}}>
							<span> {this.props.t("Български")} </span>
						</Dropdown.Item>
					</Dropdown.Menu>
				</Dropdown>
				<Dropdown variant="outline-dark">
					<Dropdown.Toggle>
						<FontAwesomeIcon icon="cog" spin/>
					</Dropdown.Toggle>
					<Dropdown.Menu as="ul">
						<Dropdown.Item as="li" className="text-align-right cursor-pointer" onClick={() => {this.navigateTo(endPoints.user)}}>
							<FontAwesomeIcon icon="user"/>
							<span> {this.props.auth.username || this.props.t("PleaseLogIn")} </span>
						</Dropdown.Item>
						<Dropdown.Item as="li" className="text-align-right cursor-pointer" onClick={() => {this.navigateTo(endPoints.import)}}>
							<span> {this.props.t("Import.title")} </span>
						</Dropdown.Item>
						<Dropdown.Item as="li" className="text-align-right cursor-pointer" onClick={() => {this.navigateTo(endPoints.nomenclatures)}}>
							<span> {this.props.t("Nomenclatures")} </span>
						</Dropdown.Item>
						<Dropdown.Divider as="li"></Dropdown.Divider>
						<Dropdown.Item as="li" className="text-align-right cursor-pointer" 
							onClick={() => {
								if(this.props.auth.userAuthenticated) {
									this.logout();
								} else {
									this.navigateTo(endPoints.login);
								}
							}}
						>
								{(() => {
										if (this.props.auth.userAuthenticated){
												return <span> {this.props.t("LogOut")} </span>
											}
											else{
												return <span> {this.props.t("LogIn")} </span>
											}
										}
								)()}
						</Dropdown.Item>
					</Dropdown.Menu>
				</Dropdown>
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
		);
	}
}

InfoBar.displayName = 'InfoBar';
InfoBar.propTypes = {};
InfoBar.defaultProps = {};

export default withTranslation()(InfoBar);
