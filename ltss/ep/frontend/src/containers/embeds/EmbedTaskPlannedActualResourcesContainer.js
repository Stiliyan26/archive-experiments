import React from 'react';
import ReactTable from 'react-table-v6'
import lodash from 'lodash'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Accordion, Card, ButtonGroup, Button } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import history from './../../scripts/history'
import { ContextAwareToggle } from './../../scripts/util'
import { isAuthenticated } from './../../components/pages/login/Login.js'

import FieldEntitySearchContainer from './../fields/FieldEntitySearchContainer'
import FieldTimeChargeRateContainer from './../fields/FieldTimeChargeRateContainer'
import FieldArticlePriceRateContainer from './../fields/FieldArticlePriceRateContainer'

import { fetchRESTFollow, dispatchCleanRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath, reduceCountingPromises } from './../../scripts/dataUtils';

class EmbedTaskPlannedActualResourcesContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			open: this.props.expanded !== undefined ? this.props.expanded : false,
			needUpdate: 1,
		};
	}

	refreshData() {
		this.props.actions.dispatchCleanRESTData(this.props.componentPath);
	}

	retrieveData() {
		let taskId = this.props.data && this.props.data.time ? this.props.data.time._taskId : this.props.task_id;
		if(this.props.task_id && this.props.componentPath && !this.props.time_error && !this.props.time_loading
				&& (!this.props.time || this.props.task_id != taskId)) {
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/subTasksPlannedTime/"+this.props.task_id,
					params: {}
				},
				this.props.componentPath+'.time',
				(response) => ({data: response.data, _taskId: this.props.task_id}),
				'EmbedTaskPlannedActualResourcesContainer.retrieveData',
				{}
			);
		}
		taskId = this.props.data && this.props.data.articles ? this.props.data.articles._taskId : this.props.task_id;
		if(this.props.task_id && this.props.componentPath && !this.props.articles_error && !this.props.articles_loading
				&& (!this.props.articles || this.props.task_id != taskId)) {
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/subTasksPlannedIncomeOrExpense/"+this.props.task_id,
					params: {}
				},
				this.props.componentPath+'.articles',
				(response) => ({data: response.data, _taskId: this.props.task_id}),
				'EmbedTaskPlannedActualResourcesContainer.retrieveData',
				{}
			);
		}

		taskId = this.props.data && this.props.data.actualTime ? this.props.data.actualTime._taskId : this.props.task_id;
		if(this.props.task_id && this.props.componentPath && !this.props.actualTime_error && !this.props.actualTime_loading
				&& (!this.props.actualTime || this.props.task_id != taskId)) {
			if(!this.props.actualTime) console.log('EmbedTaskPlannedActualResourcesContainer retrieve: No actualTime!');
			if(this.props.task_id != taskId) console.log('EmbedTaskPlannedActualResourcesContainer retrieve: this.props.task_id != taskId',this.props.task_id,taskId);
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/subTasksActualTime/"+this.props.task_id,
					params: {}
				},
				this.props.componentPath+'.actualTime',
				(response) => ({data: response.data, _taskId: this.props.task_id}),
				'EmbedTaskPlannedActualResourcesContainer.retrieveData',
				{}
			);
		}
		taskId = this.props.data && this.props.data.actualArticles ? this.props.data.actualArticles._taskId : this.props.task_id;
		if(this.props.task_id && this.props.componentPath && !this.props.actualArticles_error && !this.props.actualArticles_loading
				&& (!this.props.actualArticles || this.props.task_id != taskId)) {
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/subTasksActualRevenuesAndExpenses/"+this.props.task_id,
					params: {}
				},
				this.props.componentPath+'.actualArticles',
				(response) => ({data: response.data, _taskId: this.props.task_id}),
				'EmbedTaskPlannedActualResourcesContainer.retrieveData',
				{}
			);
		}
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		} else {
			this.retrieveData();
		}
	}

	componentDidUpdate(prevProps, prevState) {
		this.retrieveData();
	}

	render() {
		let resourcesData = [];
		if(this.props.time && this.props.time instanceof Array) {
			resourcesData = this.props.time.map((curr,index) => {
				let actual = 0;
				if(this.props.actualTime instanceof Array) {
					actual = this.props.actualTime.filter((row) => row.resource.name == curr.resource.name && row.task.id == curr.task.id)
								.reduce((acc,curr) => acc+(new Date(curr.toTime) - new Date(curr.fromTime))/1000/60/60,0);
				}
				return {
					title: curr.resource ? curr.resource.fullName : this.props.t("TaskPlannedActualResources.N/A"),
					taskPath: this.props.componentPath+'.time.data.'+index+'.task',
					task: curr.task,
					price: this.props.timeChargeRates 
							&& this.props.timeChargeRates[curr.resource ? curr.resource.id : 0] 
							&& this.props.timeChargeRates[curr.resource ? curr.resource.id : 0]._embedded
							&& this.props.timeChargeRates[curr.resource ? curr.resource.id : 0]._embedded.timeChargeRates
							&& this.props.timeChargeRates[curr.resource ? curr.resource.id : 0]._embedded.timeChargeRates[0]
						? this.props.timeChargeRates[curr.resource ? curr.resource.id : 0]._embedded.timeChargeRates[0].chargeRatePerHour : 0,
					hours: (curr.minutes)/60,
					actualHours: actual,
					resource: curr.resource,
				};
			});
		}
		if(this.props.actualTime instanceof Array) {
			resourcesData = resourcesData.concat(
				this.props.actualTime
				.map((curr,index) => ({...curr,taskPath: this.props.componentPath+'.actualTime.data.'+index+'.task',}))
				.filter((actualTime) =>
					!(
						this.props.time instanceof Array
						&& this.props.time.some(
							(row) => row.resource.name == actualTime.resource.name && row.task.id == actualTime.task.id
						)
					)
				)
				.map((curr,index) => {
					return {
						title: curr.resource ? curr.resource.fullName : this.props.t("TaskPlannedActualResources.N/A"),
						taskPath: curr.taskPath,
						task: curr.task,
						price: this.props.timeChargeRates 
								&& this.props.timeChargeRates[curr.resource ? curr.resource.id : 0] 
								&& this.props.timeChargeRates[curr.resource ? curr.resource.id : 0]._embedded
								&& this.props.timeChargeRates[curr.resource ? curr.resource.id : 0]._embedded.timeChargeRates
								&& this.props.timeChargeRates[curr.resource ? curr.resource.id : 0]._embedded.timeChargeRates[0]
							? this.props.timeChargeRates[curr.resource ? curr.resource.id : 0]._embedded.timeChargeRates[0].chargeRatePerHour : 0,
						hours: 0,
						actualHours: (new Date(curr.toTime) - new Date(curr.fromTime))/1000/60/60,
						resource: curr.resource,
					};
				})
			);
		}

		let revenuesAndExpensesData = [];
		if(this.props.articles && this.props.articles instanceof Array) {
			revenuesAndExpensesData = this.props.articles.map((curr,index) => {
				let actual = 0;
				if(this.props.actualArticles instanceof Array) {
					actual = this.props.actualArticles.filter((row) => row.article.name == curr.article.name && row.task.id == curr.task.id)
								.reduce((acc,curr) => acc+curr.ammount,0);
				}
				return {
					title: curr.article ? curr.article.name : this.props.t("TaskPlannedActualResources.N/A"),
					taskPath: this.props.componentPath+'.articles.data.'+index+'.task',
					task: curr.task,
					price: this.props.articlePriceRates 
							&& this.props.articlePriceRates[curr.article ? curr.article.id : 0] 
						? this.props.articlePriceRates[curr.article ? curr.article.id : 0].price : 0,
					ammount: curr.ammount,
					actualAmmount: actual,
					article: curr.article,
				};
			});
		}
		if(this.props.actualArticles instanceof Array) {
			revenuesAndExpensesData = revenuesAndExpensesData.concat(
				this.props.actualArticles
				.map((curr,index) => ({...curr,taskPath: this.props.componentPath+'.articles.data.'+index+'.task',}))
				.filter((actualArticle) =>
					!(
						this.props.articles instanceof Array
						&& this.props.articles.some(
							(row) => row.article.name == actualArticle.article.name && row.task.id == actualArticle.task.id
						)
					)
				)
				.map((curr,index) => {
					return {
						title: curr.article ? curr.article.name : this.props.t("TaskPlannedActualResources.N/A"),
						taskPath: curr.taskPath,
						task: curr.task,
						price: this.props.articlePriceRates 
								&& this.props.articlePriceRates[curr.article ? curr.article.id : 0] 
							? this.props.articlePriceRates[curr.article ? curr.article.id : 0].price : 0,
						ammount: 0,
						actualAmmount: curr.ammount,
						article: curr.article,
					};
				})
			);
		}
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						<ButtonGroup>
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>
								<FontAwesomeIcon icon="map"/>
								&nbsp;{this.props.t("TaskPlannedActualResources.Title")}
							</Button>
							<Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
						</ButtonGroup>
					</Card.Header>
					<Accordion.Collapse eventKey="0"><Card.Body>
						<h4><FontAwesomeIcon icon="clock"/>&nbsp;{this.props.t("TaskPlannedActualResources.TitleHours")}</h4>
						<ReactTable
							className='clients-table align-center-table -striped -highlight'
							data={resourcesData}
							defaultPageSize={5}
							minRows={0}
							showPagination={resourcesData.length>5}
							loading={this.props.time_loading}
							pivotBy={["title"]}
							columns={[{
									Header: ' ',
									columns: [
									{
										Header: this.props.t("TaskPlannedActualResources.Task"),
										accessor: 'title'
									}, {
										accessor: 'task.title',
										show: false,
									}, {
										Header: this.props.t("TaskPlannedActualResources.Subtask"),
										accessor: 'taskPath',
										Cell: props =>
											<FieldEntitySearchContainer
												href={undefined}
												componentPath={props.value} //existing path in redux store where we put data
												entityType={"tasks"}
												onChange={(href) => {}}
											/>,
										Aggregated: ' '
									}, {
										Header: this.props.t("TaskPlannedActualResources.HourlyRate"),
										accessor: 'price',
										width: 100,
										aggregate: vals => lodash.mean(vals),
										Cell: (props) => {
											let resource = props.aggregated ? props.row._subRows[0]._original.resource : props.original.resource;
											return <FieldTimeChargeRateContainer
													resourceId={resource.id}
													componentPath={this.props.componentPath+".timeChargeRates."+(resource ? resource.id : 0)} //existing path in redux store where we put data
													nomenclatureKey={'timeChargeRates'}
													editable={false}
													loading={!resource || resource[Constants.PATH_FOR_LOADING]}
													onChange={(href) => {this.setState({needUpdate: this.state.needUpdate+1});}}
												/>;
										}
									}]
								}, {
									Header: this.props.t("TaskPlannedActualResources.Planned"),
									columns: [{
										Header: this.props.t("TaskPlannedActualResources.Hours"),
										accessor: 'hours',
										width: 100,
										aggregate: vals => lodash.round(lodash.sum(vals),3)
									}, {
										Header: this.props.t("TaskPlannedActualResources.Sum"),
										id: 'amount',
										accessor: d => d.hours * d.price,
										width: 100,
										aggregate: vals => lodash.round(lodash.sum(vals),3)
									}]
								}, {
									Header: this.props.t("TaskPlannedActualResources.Actual"),
									columns: [{
										Header: this.props.t("TaskPlannedActualResources.Hours"),
										accessor: 'actualHours',
										width: 100,
										aggregate: vals => lodash.round(lodash.sum(vals),3)
									}, {
										Header: this.props.t("TaskPlannedActualResources.Sum"),
										id: 'amountActual',
										accessor: d => d.actualHours * d.price,
										width: 100,
										aggregate: vals => lodash.round(lodash.sum(vals),3)
									}]
								}, {
									Header: this.props.t("TaskPlannedActualResources.Remaining"),
									columns: [{
										Header: this.props.t("TaskPlannedActualResources.Hours"),
										id: 'resultHours',
										accessor: d => d.hours - d.actualHours,
										width: 100,
										aggregate: vals => lodash.round(lodash.sum(vals),3)
									}, {
										Header: this.props.t("TaskPlannedActualResources.Sum"),
										id: 'resultAmount',
										accessor: d => d.hours * d.price - d.actualHours * d.price,
										width: 100,
										aggregate: vals => lodash.round(lodash.sum(vals),3)
									}]
								}
							]}
						/>
						<h4><FontAwesomeIcon icon="exchange-alt"/>&nbsp;{this.props.t("TaskPlannedActualResources.TitleMoney")}</h4>
						<ReactTable
							className='clients-table align-center-table -striped -highlight'
							defaultPageSize={5}
							minRows={0}
							showPagination={revenuesAndExpensesData.length>5}
							data={revenuesAndExpensesData}
							loading={this.props.articles_loading}
							pivotBy={["title"]}
							columns={[{
									Header: ' ',
									columns: [{
										Header: this.props.t("TaskPlannedActualResources.Task"),
										accessor: 'title'
									}, {
										Header: this.props.t("TaskPlannedActualResources.Subtask"),
										accessor: 'taskPath',
										Cell: props =>
											<FieldEntitySearchContainer
												href={undefined}
												componentPath={props.value} //existing path in redux store where we put data
												entityType={"tasks"}
												onChange={(href) => {}}
											/>,
										Aggregated: ' '
									}, {
										Header: this.props.t("TaskPlannedActualResources.Price"),
										accessor: 'price',
										width: 100,
										aggregate: vals => lodash.mean(vals),
										Cell: (props) => {
											let article = props.aggregated ? props.row._subRows[0]._original.article : props.original.article;
											return <FieldArticlePriceRateContainer
													articleId={article.id}
													componentPath={this.props.componentPath+".articlePriceRates."+(article ? article.id : 0)} //existing path in redux store where we put data
													nomenclatureKey={'articlePriceRates'}
													editable={false}
													loading={!article || article[Constants.PATH_FOR_LOADING]}
													onChange={(href) => {this.setState({needUpdate: this.state.needUpdate+1});}}
												/>;
										}
									}]
								}, {
									Header: this.props.t("TaskPlannedActualResources.Planned"),
									columns: [{
										Header: this.props.t("TaskPlannedActualResources.Amount"),
										accessor: 'ammount',
										aggregate: vals => lodash.round(lodash.sum(vals),3),
										width: 100
									}, {
										Header: this.props.t("TaskPlannedActualResources.Sum"),
										id: "planSum",
										accessor: d => d.ammount * d.price,
										aggregate: vals => lodash.round(lodash.sum(vals),3),
										width: 100
									}]
								}, {
									Header: this.props.t("TaskPlannedActualResources.Actual"),
									columns: [{
										Header: this.props.t("TaskPlannedActualResources.Amount"),
										accessor: 'actualAmmount',
										aggregate: vals => lodash.round(lodash.sum(vals),3),
										width: 100
									}, {
										Header: this.props.t("TaskPlannedActualResources.Sum"),
										id: "actualSum",
										accessor: d => d.actualAmmount * d.price,
										aggregate: vals => lodash.round(lodash.sum(vals),3),
										width: 100
									}]
								}, {
									Header: this.props.t("TaskPlannedActualResources.Remaining"),
									columns: [{
										Header: this.props.t("TaskPlannedActualResources.Amount"),
										id: "resultAmmount",
										accessor: d => d.ammount - d.actualAmmount,
										aggregate: vals => lodash.round(lodash.sum(vals),3),
										width: 100
									}, {
										Header: this.props.t("TaskPlannedActualResources.Sum"),
										id: "resultSum",
										accessor: d => (d.ammount - d.actualAmmount) * d.price,
										aggregate: vals => lodash.round(lodash.sum(vals),3),
										width: 100
									}]
								}
							]}
						/>
					</Card.Body></Accordion.Collapse>
				</Card>
			</Accordion>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchRESTFollow
	return {
		auth: state.auth,
		articles: data && data.articles ? data.articles.data : undefined,
		actualArticles: data && data.actualArticles ? data.actualArticles.data : undefined,
		time: data && data.time ? data.time.data : undefined,
		actualTime: data && data.actualTime ? data.actualTime.data : undefined,
		data: data,
		time_loading: ownProps.loading ? ownProps.loading : (data && data.time ? data.time[Constants.PATH_FOR_LOADING] : undefined),
		actualTime_loading: ownProps.loading ? ownProps.loading : (data && data.actualTime ? data.actualTime[Constants.PATH_FOR_LOADING] : undefined),
		articles_loading: ownProps.loading ? ownProps.loading : (data && data.articles ? data.articles[Constants.PATH_FOR_LOADING] : undefined),
		actualArticles_loading: ownProps.loading ? ownProps.loading : (data && data.actualArticles ? data.actualArticles[Constants.PATH_FOR_LOADING] : undefined),
		time_error: data && data.time instanceof Error ? data.time : undefined,
		actualTime_error: data && data.actualTime instanceof Error ? data.actualTime : undefined,
		articles_error: data && data.articles instanceof Error ? data.articles : undefined,
		actualArticles_error: data && data.actualArticles instanceof Error ? data.actualArticles : undefined,
		timeChargeRates: data ? data.timeChargeRates : undefined,
		articlePriceRates: data ? data.articlePriceRates : undefined,
		componentPath: ownProps.componentPath,
		task_id: ownProps.task_id
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedTaskPlannedActualResourcesContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
