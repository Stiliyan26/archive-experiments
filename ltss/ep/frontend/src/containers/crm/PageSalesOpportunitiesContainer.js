import React from 'react';
import ReactTable from 'react-table-v6'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter, Link } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants';
import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'

import { postRESTData } from '../../actions/taskActions';

//Page: can be used as a landing page
//Container: redux container class
class PageSalesOpportunitiesContainer extends React.Component {	
	retrieveData() {
		if(!this.props.data) {
			this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/reports/requestOfferContractOpportunityAnalysis",
						data: {fromDate: "2017-01-01"}
					},
					'salesOpportunities',
					'PageSalesOpportunitiesContainer.retrieveData'
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
		let body = '';
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else {
				let requestOfferContractOpportunityAnalysisData = [];
				if(this.props.data instanceof Array) {
					requestOfferContractOpportunityAnalysisData = this.props.data.map((elem) => ({
						request_task_id: elem[0],
						request_task_title: elem[1],
						request_task_status_name: elem[2],
						request_task_status_terminal: elem[3],
						request_task_plan_sum: elem[4],
						request_counter_party: elem[5],
						request_assigned: elem[6],
						offer_task_id: elem[7],
						offer_task_title: elem[8],
						offer_task_status_name: elem[9],
						offer_task_status_terminal: elem[10],
						offer_task_plan_sum: elem[11],
						offer_counter_party: elem[12],
						offer_assigned: elem[13],
						contract_task_id: elem[14],
						contract_task_title: elem[15],
						contract_task_status_name: elem[16],
						contract_task_status_terminal: elem[17],
						contract_task_plan_sum: elem[18],
						contract_counter_party: elem[19],
						contract_assigned: elem[20],
						counter_party: elem[19] || elem[12] || elem[5] || this.props.t("SalesOpportunities.N/A"),
						assigned: (elem[20] != null) ?
							elem[20]
							: (elem[13] != null ?
								elem[13]
								: (elem[6] != null ?
									elem[6]
									: this.props.t("SalesOpportunities.N/A")
								)
							)
					})); 
				}
				//console.log('render',this.props.data,requestOfferContractOpportunityAnalysisData);
				body = <div className='page-body'>
						<ReactTable
							className='clients-table align-center-table -striped -highlight'
							data={requestOfferContractOpportunityAnalysisData}
							columns={[
								{
									Header: this.props.t("SalesOpportunities.Status"),
									Cell: props => {
										let opportunity_status = this.props.t("SalesOpportunities.N/A");
										if(props.original.request_task_id != null
												&& props.request_task_status_terminal == false
												&& props.original.offer_task_id == null) {
											opportunity_status = this.props.t("SalesOpportunities.Identified");
										}
										if(props.original.request_task_id != null
												&& props.request_task_status_terminal == true
												&& props.original.offer_task_id == null) {
											opportunity_status = this.props.t("SalesOpportunities.Missed");
										}
										if(props.original.offer_task_id != null) {
											opportunity_status = this.props.t("SalesOpportunities.Offer");
										}
										if(props.original.offer_task_id != null
												&& props.offer_task_status_terminal == true
												&& props.original.contract_task_id == null) {
											opportunity_status = this.props.t("SalesOpportunities.Lost");
										}
										if(props.original.contract_task_id != null) {
											opportunity_status = this.props.t("SalesOpportunities.Won");
										}
										return <div>{opportunity_status}</div>
									}
								},{
									Header: this.props.t("SalesOpportunities.RequestTask"),
									accessor: 'request_task_id',
									Cell: props => <Link to={'/tasks/'+props.value}>{props.original.request_task_title}</Link>
								},{
									Header: this.props.t("SalesOpportunities.OfferTask"),
									accessor: 'offer_task_id',
									Cell: props => <Link to={'/tasks/'+props.value}>{props.original.offer_task_title}</Link>
								},{
									Header: this.props.t("SalesOpportunities.ContractTask"),
									accessor: 'contract_task_id',
									Cell: props => <Link to={'/tasks/'+props.value}>{props.original.contract_task_title}</Link>
								},{
									Header: this.props.t("SalesOpportunities.PlannedAmount"),
									accessor: 'plan_sum',
									filterable: false,
									Cell: props => {
										let plan_sum = 0;
										if(props.original.contract_task_plan_sum != null) {
											plan_sum = props.original.contract_task_plan_sum;
										} else if(props.original.offer_task_plan_sum != null) {
											plan_sum = props.original.offer_task_plan_sum;
										} else if(props.original.request_task_plan_sum != null) {
											plan_sum = props.original.request_task_plan_sum;
										}
										return <div>{plan_sum}</div>
									}
								},{
									Header: this.props.t("SalesOpportunities.counterParty"),
									accessor: 'counter_party'
								},{
									Header: this.props.t("SalesOpportunities.assigned"),
									accessor: 'assigned'
								}
							]}
							defaultPageSize={10}
							showPagination={true}
						/>
					</div>;
			}
		}
		return (
			<div className="page-body-wrapper">
				<Header text={this.props.t("SalesOpportunities.title")} class='page-header text-align-center no-margin' />
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const data = state.rest.salesOpportunities;
	return {
		auth: state.auth,
		data: data
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { postRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageSalesOpportunitiesContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
