import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { ButtonGroup, Button } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants';
import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import Board from '../kanban/Board'

import { getEntityDefinition, builderDataToProjection } from '../nomenclatures/entityDefinitions.js'
import { fetchRESTFollow, dispatchEditRESTData, dispatchCleanRESTData, resetRESTCallLimit, patchRESTData } from '../../actions/taskActions';

//https://react-beautiful-dnd.netlify.com/?selectedKind=board&selectedStory=simple&full=0&addons=1&stories=1&panelRight=0&addonPanel=storybook%2Factions%2Factions-panel
//Page: can be used as a landing page
//Container: redux container class
class PageKanbanTaskAssigned extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			updated: 0
		}
	}

	refreshData() {
		this.props.actions.dispatchCleanRESTData("kanbanTaskAssigned");
	}

	saveData() {
		//check data availability
		if(this.props.lists && this.props.lists.columns instanceof Array) {
			let modified = this.props.lists.columns.reduce((accColumns,currColumn,columnIndex) => {
				//get this column modified
				const columnModified = currColumn.cards instanceof Array ?
					currColumn.cards.reduce((acc,curr,index) => {
						if(currColumn && currColumn._links && currColumn._links.self && currColumn._links.self.href
								&& !(curr.assigned && curr.assigned._links && curr.assigned._links.self && curr.assigned._links.self.href
										&& curr.assigned._links.self.href.replace(/{.+}/,"") == currColumn._links.self.href.replace(/{.+}/,""))
									){
							acc.push({columnId: columnIndex, cardId: index,id: curr.id, assigned: currColumn._links.self.href});
						}
						return acc;
					},[])
					: [];
				//add them to the list
				return columnModified ? accColumns.concat(columnModified) : accColumns;
			},new Array());
//			console.log(modified);
			modified.forEach((elem) => this.props.actions.patchRESTData(
						{
							method: 'patch',
							url: API_URL+"/tasks/"+elem.id,
							data: {assigned: elem.assigned},
							headers: {'Content-Type': 'application/merge-patch+json'}
						},
						"kanbanTaskAssigned.lists.cards."+elem.columnId+"."+elem.cardId,
						response => (response.data),
						(data) => {},
						'PageKanbanTaskAssigned.saveData'
					)
			);
		}
	}

	retrieveData() {
		//load columns (secUsers) first
		//TODO get their full list (paging!)
		if(!(this.props.lists && this.props.lists.columns && (this.props.lists.columns[Constants.PATH_FOR_LOADING] || this.props.lists.columns instanceof Array))) {
			let fetchPromiseWrapper = {};
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/"+"secUsers"+"?size=1000",
				},
				"kanbanTaskAssigned.lists.columns",
				response => (response.data._embedded["secUsers"]),
				'PageKanbanTaskAssigned.retrieveData',
				{},
				fetchPromiseWrapper
			);
			fetchPromiseWrapper.promise.then((mergeObj) => {
				let response = mergeObj[0].value;
				let secUsers = response.map((column,index) => {let result = column; result.cards = []; return result;});
				secUsers.unshift({id:0,name:this.props.t("Kanban.Unassigned"),cards: []});
				//TODO Fix paging (not all items may be retrieved) like in FieldTask
				if(response) {
					this.props.actions.fetchRESTFollow(
						{
							url: API_URL+"/"+"reports/builder/1?from=Task&select=Task,Task.status,Task.type,Task.counterParty,Task.createdBy,Task.assigned&size=1000&Task.status.terminal=false",
						},
						"kanbanTaskAssigned.lists.columns",
						response => {
							let newData = response.data;
							//transform data to "repository response"-like
							builderDataToProjection(newData,"tasks");
							let cards = newData._embedded["tasks"].reduce(function(columns, card) {
								const assignedId = card && card.assigned ? card.assigned.id : 0;
								const columnIndex = columns.findIndex((elem) => ((elem.id ? elem.id : 0) == assignedId));
								if(columnIndex >= 0) {
									columns[columnIndex].cards.push(card);
								} else {
									console.error("Cannot find the assigned secUser in the list of secUsers!");
								}
								return columns;
							}, secUsers);
							return cards;
						},
						'PageKanbanTaskAssigned.retrieveData',
					);
				}
			});
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
		if(this.props.columns instanceof Array && this.props.columns[0] && this.props.columns[0].cards instanceof Array) {
			return (
				<div className="page-body-wrapper">
					<Header text={this.props.t("Kanban.title")} class='page-header text-align-center no-margin' />
					<ButtonGroup>
						<Button variant="outline-dark" title={this.props.t("ReactTable.Save")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.saveData();}}><FontAwesomeIcon icon="save"/></Button>
						<Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
					</ButtonGroup>
					<Board
						columns={this.props.columns}
						componentPath={"kanbanTaskAssigned.lists.columns"} //existing path in redux store where we put data
						onReorder={(data) => {
							this.props.actions.dispatchEditRESTData("kanbanTaskAssigned.lists.columns",data);
							this.setState({updated: this.state.updated+1});
						}}
					/>
				</div>
			);
		} else {
			return null;
		}
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const data = state.rest.kanbanTaskAssigned;
	let columns = data && data.lists && data.lists.columns instanceof Array ? data.lists.columns : undefined;
	return {
		auth: state.auth,
		data: data,
		lists: data ? data.lists : undefined,
		columns: columns,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData, dispatchCleanRESTData, resetRESTCallLimit, patchRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageKanbanTaskAssigned);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
