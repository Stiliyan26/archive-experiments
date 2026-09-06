import React from 'react';
import url from 'url'
import ReactTable from 'react-table-v6'
import { Accordion, Card, ButtonGroup, Button } from 'react-bootstrap';
import lodash from 'lodash'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import * as Constants from './../../static/constants';
import history from './../../scripts/history'
import { ContextAwareToggle } from './../../scripts/util'
import { isAuthenticated } from './../../components/pages/login/Login.js'

import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

import { fetchRESTFollow, dispatchCleanRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath, reduceCountingPromises } from './../../scripts/dataUtils';

class EmbedActualResourcesContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			open: this.props.expanded !== undefined ? this.props.expanded : false,
		};
	}

	refreshData() {
		this.props.actions.dispatchCleanRESTData(this.props.componentPath);
	}

	retrieveData() {
		//get the param used for the data in the redux state
		const curr_href = this.props.data && this.props.data.articles && this.props.data.articles._links ? url.parse(this.props.data.articles._links.self.href,true).query.task : this.props.href;
		if(this.props && !this.props.error && !this.props.loading && this.props.href
				&& (!this.props.data || !this.props.articles || this.props.href != curr_href)) {
			if(!this.props.data) console.log('EmbedActualResourcesContainer retrieve: No data!');
			if(!this.props.articles) console.log('EmbedActualResourcesContainer retrieve: No articles!');
			if(this.props.href != curr_href) console.log('EmbedActualResourcesContainer retrieve: this.props.href != curr_href',this.props.href,curr_href);
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/taskAttachments/search/findByTask",
					params: {
						task: this.props.href
					}
				},
				this.props.componentPath+'.articles',
				"data",
				'EmbedActualResourcesContainer.retrieveData',
				{attachment:
					{
						node: "_embedded.taskAttachments",
						children: {
							attachableRevenuesAndExpenseses: {
								url: API_URL+"/attachableRevenuesAndExpenseses/search/findByAttachable",
								getParams: (key) => ({
									attachable: key._links.self.href
								}),
								mapping: "data._embedded.attachableRevenuesAndExpenseses",
								children:
									{article:
										{articlePriceRate:
											{
												url: API_URL+"/articlePriceRates/search/findByArticle",
												getParams: (key) => ({
													article: key._links.self.href
												}),
												mapping: "data._embedded.articlePriceRates"
											}
										}
									}
							}
						}
					}
				}
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
		let revenuesAndExpensesData = [];
		let totalArticles = {aggregate: 0, promises: 0};
		if(this.props.articles && this.props.articles instanceof Array) {
			revenuesAndExpensesData = this.props.articles.map((curr,index) => ( !curr ? {}
				: curr instanceof Promise ? {
					id: 'Движение No'+index,
					document: 'Зареждане...',
					title: 'Зареждане...',
					price: 0,
					revenueAmmount: 0,
					expenseAmmount: 0
				}
					: {
						id: curr.id,
						document: curr.document,
						title: curr.article ? curr.article.name : 'N/A',
						price: (curr.article && curr.article.articlePriceRate && curr.article.articlePriceRate[0]) ? curr.article.articlePriceRate[0].price : 'N/A',
						revenueAmmount: curr.ammount>0 ? curr.ammount : 0,
						expenseAmmount: curr.ammount<0 ? -curr.ammount : 0
					}
			));
			totalArticles = reduceCountingPromises(
					this.props.articles,
					(total,curr) => total.aggregate + curr.ammount*((curr.article && curr.article.articlePriceRate && curr.article.articlePriceRate[0]) ? curr.article.articlePriceRate[0].price : 'N/A')
				);
		}
		const timeSheetItemsDef = getEntityDefinition("timeSheetItems",{task: {show: false}});
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						<ButtonGroup>
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>
								<FontAwesomeIcon icon="cube"/>&nbsp;
								Изпълнено по задачата (
									<FontAwesomeIcon icon="clock" className='fa-fw'/>{this.props.totalTime},
									<FontAwesomeIcon icon="exchange-alt" className='fa-fw'/>{totalArticles.aggregate}
								)&nbsp;
							</Button>
							<Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
						</ButtonGroup>
					</Card.Header>
					<Accordion.Collapse eventKey="0"><Card.Body>
						<EmbedRetrieveEntityListContainer
							title={timeSheetItemsDef.label}
							icon={timeSheetItemsDef.icon}
							columns={timeSheetItemsDef.columns}
							componentPath={this.props.componentPath+".timeSheetItems"}
							retrieveType="timeSheetItems"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
						/>
						<h4><FontAwesomeIcon icon="exchange-alt"/>&nbsp;Приходи и разходи</h4>
						<ReactTable
							className='clients-table align-center-table -striped -highlight'
							data={revenuesAndExpensesData}
							defaultPageSize={5}
							minRows={0}
							showPagination={revenuesAndExpensesData.length>5}
							columns={[
								{
									Header: 'Документ',
									accessor: 'document'
								}, {
									Header: 'Наименование',
									accessor: 'title'
								}, {
									Header: 'Ед. цена',
									accessor: 'price',
									width: 100
								}, {
									Header: 'Кол-во',
									columns: [{
										Header: 'Приход',
										accessor: 'revenueAmmount',
										width: 100
									}, {
										Header: 'Разход',
										accessor: 'expenseAmmount',
										width: 100
									}]
								}, {
									Header: 'Сума',
									columns: [{
										id: 'revenue_sum',
										Header: 'Приход',
										accessor: d => d.revenueAmmount * d.price,
										aggregate: vals => lodash.round(lodash.sum(vals),3),
										width: 100
									}, {
										id: 'expense_sum',
										Header: 'Разход',
										accessor: d => d.expenseAmmount * d.price,
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
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	let timeSheetItems = data && data.timeSheetItems && data.timeSheetItems._embedded ? data.timeSheetItems._embedded.timeSheetItems : undefined;
	let totalTime = 0;
	if(timeSheetItems && timeSheetItems instanceof Array) {
		totalTime = timeSheetItems.reduce((total,curr) => total
			+ (curr
				? (new Date(curr.toTime) - new Date(curr.fromTime))/1000/60/60
					*(
							curr.resource
							&& curr.resource.timeChargeRates
							&& curr.resource.timeChargeRates._embedded
							&& curr.resource.timeChargeRates._embedded.timeChargeRates
							&& curr.resource.timeChargeRates._embedded.timeChargeRates[0]
						? curr.resource.timeChargeRates._embedded.timeChargeRates[0].chargeRatePerHour
						: 'N/A')
				: 0)
			, 0);
	}
	const articles = (data && data.articles && data.articles._embedded && (data.articles._embedded.taskAttachments instanceof Array) ? data.articles._embedded.taskAttachments.reduce((all,attachmentElem) => all.concat(
			attachmentElem.attachment ?
				(attachmentElem.attachment.attachableRevenuesAndExpenseses instanceof Array ?
					attachmentElem.attachment.attachableRevenuesAndExpenseses.map((moveElem) => ({
						document: attachmentElem.attachment.name,
						ammount: moveElem.ammount,
						article: moveElem.article
					}))
					: [attachmentElem.attachment.attachableRevenuesAndExpenseses]
				)
			: all
		),[]):undefined);
	return {
		auth: state.auth,
		data: data,
		totalTime: totalTime,
		articles: articles,
		componentPath: ownProps.componentPath,
		href: ownProps.href,
		loading: ownProps.loading ? ownProps.loading : (data && data.articles ? data.articles[Constants.PATH_FOR_LOADING] : undefined),
		error: data && data.articles instanceof Error ? data.articles : undefined,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedActualResourcesContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
