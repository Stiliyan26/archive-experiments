import React from 'react';
import url from 'url'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Accordion, Card, ButtonGroup, Button } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import { getEntityDefinition } from './../nomenclatures/entityDefinitions.js'
import EmbedRetrieveEntityListContainer from './../nomenclatures/EmbedRetrieveEntityListContainer'

import history from './../../scripts/history'
import { ContextAwareToggle } from './../../scripts/util'
import { isAuthenticated } from './../../components/pages/login/Login.js'

import { dispatchCleanRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

class EmbedPlannedResourcesContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			open: this.props.expanded !== undefined ? this.props.expanded : false,
		};
	}

	refreshData() {
		this.props.actions.dispatchCleanRESTData(this.props.componentPath);
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}

	render() {
		const plannedTimesDef = getEntityDefinition("plannedTimes",{task: {show: false}});
		const plannedIncomeOrExpensesDef = getEntityDefinition("plannedIncomeOrExpenses",{task: {show: false}});
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						<ButtonGroup>
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>
								<FontAwesomeIcon icon="map"/>&nbsp;
								this.props.t("PlannedIncomeOrExpense._className_plural") (
									<FontAwesomeIcon icon="clock" className='fa-fw'/>{this.props.totalTime},
									<FontAwesomeIcon icon="exchange-alt" className='fa-fw'/>{this.props.totalArticles}
								)&nbsp;
							</Button>
							<Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
						</ButtonGroup>
					</Card.Header>
					<Accordion.Collapse eventKey="0"><Card.Body>
						<EmbedRetrieveEntityListContainer
							title={plannedTimesDef.label}
							icon={plannedTimesDef.icon}
							columns={plannedTimesDef.columns}
							componentPath={this.props.componentPath+".plannedTimes"}
							retrieveType="plannedTimes"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
						/>
						<EmbedRetrieveEntityListContainer
							title={plannedIncomeOrExpensesDef.label}
							icon={plannedIncomeOrExpensesDef.icon}
							columns={plannedIncomeOrExpensesDef.columns}
							componentPath={this.props.componentPath+".plannedIncomeOrExpenses"}
							retrieveType="plannedIncomeOrExpenses"
							parentHref={this.props.href}
							parentAttr="task"
							parentData={this.props.data}
						/>
					</Card.Body></Accordion.Collapse>
				</Card>
			</Accordion>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchRES
	let plannedTimes = data && data.plannedTimes && data.plannedTimes._embedded ? data.plannedTimes._embedded.plannedTimes : undefined;
	let totalTime = 0;
	if(plannedTimes && plannedTimes instanceof Array) {
		totalTime = plannedTimes.reduce((total,curr) => total
			+ (curr
				? (curr.minutes)/60
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
	let plannedIncomeOrExpenses = data && data.plannedIncomeOrExpenses && data.plannedIncomeOrExpenses._embedded ? data.plannedIncomeOrExpenses._embedded.plannedIncomeOrExpenses : undefined;
	let totalArticles = 0;
	if(plannedIncomeOrExpenses && plannedIncomeOrExpenses instanceof Array) {
		totalArticles = plannedIncomeOrExpenses.reduce((total,curr) => total
			+ curr.ammount
				*(
						curr.article
						&& curr.article.articlePriceRates
						&& curr.article.articlePriceRates._embedded
						&& curr.article.articlePriceRates._embedded.articlePriceRates
						&& curr.article.articlePriceRates._embedded.articlePriceRates[0]
					? curr.article.articlePriceRates._embedded.articlePriceRates[0].price
					: 'N/A')
			, 0);
	}
	return {
		auth: state.auth,
		data: data,
		plannedTimes: plannedTimes,
		totalTime: totalTime,
		totalArticles: totalArticles,
		articles: data ? data.articles : undefined,
		componentPath: ownProps.componentPath,
		href: ownProps.href
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchCleanRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedPlannedResourcesContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
