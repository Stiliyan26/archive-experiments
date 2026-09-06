import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import Calendar from 'react-big-calendar';
import "react-big-calendar/lib/css/react-big-calendar.css";
import moment from 'moment';
moment.locale('bg')

import history from './../scripts/history'
import { isAuthenticated } from './../components/pages/login/Login.js'

import Header from './../components/generic/Header'
import Select from './OverflowSelect'

import { getEntityDefinition } from './nomenclatures/entityDefinitions.js'
import { resetRESTCallLimit, fetchRESTFollow, dispatchCleanRESTData } from './../actions/taskActions';
import EmbedRetrieveEntityListContainer from './nomenclatures/EmbedRetrieveEntityListContainer'

Calendar.setLocalizer(Calendar.momentLocalizer(moment));
let formats = {
	agendaDateFormat: 'dddd (DD.MM)',
	dayHeaderFormat: 'dddd (DD.MM)',
	dayFormat: 'dddd (DD.MM)',
	weekdayFormat: 'dddd'
}

//Page: can be used as a landing page
//Container: redux container class
class PageCalendarContainer extends React.Component {

	constructor(props){
		super(props);
		this.state = {
			selectedTask: undefined,
			selectedEmployee: undefined
		}
	}

	fetchTasks(){
		this.props.actions.fetchRESTFollow(
			{
				url: API_URL+"/reports/builder/1?from=Task&select=Task&page=0&size=50000&sort=Task.id%2Casc"
			},
			'tasks',
			response => ( response.data ),
			'PageCalendarContainer.fetchTasks',
		);
	}
	fetchEmployees(){
		this.props.actions.fetchRESTFollow(
			{
				url: API_URL+"/reports/builder/1?from=SecUser&select=SecUser&page=0&size=50000&sort=SecUser.id%2Casc"
			},
			'employees',
			response => ( response.data ),
			'PageCalendarContainer.fetchEmployees',
		);
	}
	fetchTimesheets(){
		const { selectedTask, selectedEmployee } = this.state
		let url = `${API_URL}/reports/builder/1?from=TimeSheetItem&select=TimeSheetItem.task,TimeSheetItem.resource,TimeSheetItem.resource.timeChargeRates,TimeSheetItem`
		let selectedTaskId = selectedTask && selectedTask.value && selectedTask.value.id ? selectedTask.value.id : undefined
		let selectedEmployeeId = selectedEmployee && selectedEmployee.value && selectedEmployee.value.id ? selectedEmployee.value.id : undefined
		if(selectedTaskId || selectedEmployeeId){
			if(selectedTaskId){
				url += `&TimeSheetItem.task.id=${selectedTaskId}`
			}
			if(selectedEmployeeId){
				url += `&TimeSheetItem.resource.id=${selectedEmployeeId}`
			}
			url += '&page=0&size=50000&sort=TimeSheetItem.id%2Casc'
		}

		this.props.actions.fetchRESTFollow({
				url
			},
			'timeSheetItems',
			response => ( response.data ),
			'PageCalendarContainer.fetchTimesheets',
		);
	}
	onChange(entityType, ev){
		console.warn('entityType: ', entityType)
		console.warn('ev: ', ev)
		this.setState({
			[entityType]: ev
		}, () => {
			this.props.actions.dispatchCleanRESTData('timeSheetItems')
			this.fetchTimesheets()
		})
	}
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		} else {
			this.fetchTasks()
			this.fetchEmployees()
		}
	}
	mapTimeSheetItems(timeSheetItems){
		let self = this
		if(timeSheetItems && timeSheetItems._embedded && (self.state.selectedTask || self.state.selectedEmployee)){
			let buffer = timeSheetItems._embedded.hashMaps.map((item) => {
				return {
					timeSheet: item.TimeSheetItem,
					resource: item['TimeSheetItem.resource'],
					task: item['TimeSheetItem.task']
				}
			})
			return buffer.map((item) => {
				let eventTitle
				if(self.state.selectedTask && self.state.selectedEmployee){
					eventTitle = item.timeSheet.description || 'без коментар'
				} else if(self.state.selectedTask){
					eventTitle = `${item.resource ? item.resource.fullName : 'неизвестен'} | ${item.timeSheet.description || 'без коментар'}`
				} else if(self.state.selectedEmployee){
					eventTitle = `${item.task.title} | ${item.timeSheet.description || 'без коментар'}`
				}
				return {
					start: new Date(item.timeSheet.fromTime),
					end: new Date(item.timeSheet.toTime),
					title: eventTitle
				}
			})
		} else{
			return []
		}
	}

	render() {
		const { tasks, employees, timeSheetItems } = this.props
		const { selectedTask, selectedEmployee } = this.state
		let timeSheetEvents = this.mapTimeSheetItems(timeSheetItems)
		let employeeOptions = []
		let	taskOptions = []
		if(tasks && tasks._embedded && tasks._embedded.hashMaps){
			let taskList = tasks._embedded.hashMaps.map((task) => task.Task)
			for(let task of taskList) {
				if(task.title){
					taskOptions.push({label: task.title, value: task});
				}
			}
		}
		if(employees && employees._embedded && employees._embedded.hashMaps){
			let employeeList = employees._embedded.hashMaps.map((employee) => employee.SecUser)
			for(let employee of employeeList) {
				if(employee.fullName){
					employeeOptions.push({label: employee.fullName, value: employee});
				}
			}
		}

		return (
			<div className="calendar-wrapper">
				<Header text='Календар' class='page-header text-align-center no-margin' />
				<div className='page-body padding-top-20'>
					<div className="col-sm-12 form-group">
						<div className="col-sm-6 form-group z-index-5">
							<FontAwesomeIcon icon="tasks" size="2x" className='text-align-left col-sm-1 col-sm-offset-1'/>
							<Select
								className="col-sm-9 col-sm-offset-1 no-padding"
								name="form-field-name"
								options={taskOptions}
								onChange={(e) => { this.onChange('selectedTask', e)}}
								value={selectedTask}
								placeholder='Изберете задача'
								formats={formats}
								isClearable={true}
							/>
						</div>
						<div className="col-sm-6 form-group z-index-5">
							<FontAwesomeIcon icon="user" size="2x" className='text-align-left col-sm-1 col-sm-offset-1'/>
							<Select
								className="col-sm-9 col-sm-offset-1 no-padding"
								name="form-field-name"
								options={employeeOptions}
								onChange={(e) => { this.onChange('selectedEmployee', e)}}
								value={selectedEmployee}
								placeholder='Изберете служител'
								isClearable={true}
							/>
						</div>
					</div>

					<div className='col-sm-12'>
						<Calendar
							className='timesheet-calendar'
							defaultDate={new Date()}
							defaultView="month"
							events={timeSheetEvents}
							style={{ height: "85vh" }}
							messages={{
								today: 'днес',
								previous: 'предишен',
								next: 'следващ',
								month: 'месец',
								week: 'седмица',
								day: 'ден',
								agenda: 'детайли',
								date: 'дата',
								time: 'час',
								event: 'наименование'
							}}
							formats={formats}
						/>
					</div>
				</div>
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	return {
		auth: state.auth,
		tasks: state.rest.tasks,
		employees: state.rest.employees,
		timeSheetItems: state.rest.timeSheetItems
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageCalendarContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
