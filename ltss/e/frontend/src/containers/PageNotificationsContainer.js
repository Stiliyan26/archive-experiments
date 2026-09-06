import React from 'react';
import querystring from 'querystring'
import moment from 'moment';
moment.locale('bg')

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Button, Alert } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import * as Constants from './../static/constants';
import history from './../scripts/history'
import { isAuthenticated } from './../components/pages/login/Login.js'

import Header from './../components/generic/Header'
import FieldTimestampContainer from './fields/FieldTimestampContainer'
import FieldUnitContainer from './fields/FieldUnitContainer'

import { fetchRESTFollow, dispatchEditRESTData, resetRESTCallLimit } from './../actions/taskActions';

//Page: can be used as a landing page
//Container: redux container class
class PageNotificationsContainer extends React.Component {
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		} else {
			this.retrieveData();
		}
	}
	
	retrieveData(){
		if(this.props.auth.username !== undefined) {
			let fetchPromiseWrapper = {};
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/builder/1",
					params: {
						from: "Task",
						select: "Task",
						"assigned.name": this.props.auth.username,
						page: 0,
						size: 1,
					}
				},
				"notificationsSpecial.assignedTasks",
				response => ({result: response && response.data && response.data.page ? response.data.page.totalElements : undefined }),
				'PageNotificationsContainer.retrieveData',
				{},
				fetchPromiseWrapper
			);
		}
		//TODO after previous is done
		if(this.props.auth.username !== undefined) {
			let fetchPromiseWrapper = {};
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/builder/1",
					params: {
						from: "Task",
						select: "Task",
						"assigned.name": this.props.auth.username,
						deadline: ["1900/01/01 00:00:00 +0000",moment().endOf('week').format("YYYY/MM/DD HH:mm:ss ZZ")],
						page: 0,
						size: 1,
					},
					paramsSerializer: function(params) {
						//needed for the from-to dates
						return querystring.stringify(params)
					}
				},
				"notificationsSpecial.deadlineTasks",
				response => ({result: response && response.data && response.data.page ? response.data.page.totalElements : undefined }),
				'PageNotificationsContainer.retrieveData',
				{},
				fetchPromiseWrapper
			);
			fetchPromiseWrapper.promise.then((response) => {
				if(response[0].value.result > 0) {
					let newData = this.props.data;
					newData.push({
						timestamp: moment(),
						error: new Error(),
						isChecked: false,
						message: <div style={{display: 'inline-block'}}>
							{this.props.t("Notifications.AssignedTasksCloseToDeadline", {assignedTasksCloseToDeadline: response[0].value.result})}
						</div>
					});
					this.props.actions.dispatchEditRESTData("notifications", newData)
				}
			});
		}
	}

	render() {
		let body = this.props.data.map((elem,index) => {
			let notification = "";
			if(elem[Constants.PATH_FOR_ERROR] instanceof Error) {
				notification =
					<Alert variant="danger">
						{elem.isChecked ? "" : <Button variant="outline-dark" style={{display: 'inline-block'}} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.props.actions.dispatchEditRESTData("notifications."+index+".isChecked",true);}}><FontAwesomeIcon icon="check"/></Button>}
						&nbsp;
						{elem.message}
					</Alert>;
			} else {
				notification =
					<Alert variant="info">
						{elem.isChecked ? "" : <Button variant="outline-dark" style={{display: 'inline-block'}} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.props.actions.dispatchEditRESTData("notifications."+index+".isChecked",true);}}><FontAwesomeIcon icon="check"/></Button>}
						&nbsp;
						{elem.message}
					</Alert>;
			}
			return <div key={"notification"+index}>
				<FieldTimestampContainer
					componentPath={'notifications.'+index+'.timestamp'} //existing path in redux store where we put data
				/>
				{notification}
			</div>;
		});
		body = body.reverse();
		if(body.length == 0) {
			body = <h2>{this.props.t("Notifications.NoNotifications")}</h2>;
		}
		return (
			<div className="page-body-wrapper">
				<Header text={this.props.t("Notifications.Notifications")} class='page-header text-align-center no-margin' />
				<div className='page-body'>
					{body}
					{this.props.assignedTasks > 0 ?
						<div key={"notificationsSpecial.assignedTasks"}>
							<Alert variant="info">
								{this.props.t("Notifications.AssignedTasks", {assignedTasks: this.props.assignedTasks})}
							</Alert>
						</div>
						: ""
					}
				</div>
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let data = state.rest.notifications;
	data = data instanceof Array ? data : [];
	return {
		auth: state.auth,
		data: data,
		notCheckedCount: data.filter(elem => !elem.isChecked).length,
		//special
		assignedTasks: state.rest.notificationsSpecial && state.rest.notificationsSpecial.assignedTasks ? state.rest.notificationsSpecial.assignedTasks.result : undefined,
		deadlineTasks: state.rest.notificationsSpecial && state.rest.notificationsSpecial.deadlineTasks ? state.rest.notificationsSpecial.deadlineTasks.result : undefined,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageNotificationsContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
