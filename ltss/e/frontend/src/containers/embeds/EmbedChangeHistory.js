import React from 'react';
import ReactTable from 'react-table-v6'
import lodashIsEqual from 'lodash/isEqual'

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

import FieldTimestampContainer from '../fields/FieldTimestampContainer'

import { getEntityDefinition, getUpdatedColumn } from '../nomenclatures/entityDefinitions.js'
import { fetchRESTFollow, dispatchCleanRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath, reduceCountingPromises } from './../../scripts/dataUtils';

class EmbedChangeHistory extends React.Component {
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
		if(this.props.entity_id && !this.props.error && !this.props.loading && this.props.componentPath &&
				(this.props.items === undefined
					|| this.props.data === undefined
					|| this.props.entity_id != this.props.data.entity_id
			)) {
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/changelog/"+this.props.entityName+"/"+this.props.entity_id,
					params: {}
				},
				this.props.componentPath,
				(response) => {
						//convert response of entity versions array into field changes array
						let changeHistoryData = [];
						if(response.data instanceof Array) {
							for(let logRow = 1; logRow < response.data.length; logRow++) {
								for(let key in response.data[logRow]) {
									if(key != "lastModifiedDate" && key != "lastModifiedBy") { //we don't care about these
										let hasDiff = false;
										if(response.data[logRow][key] && response.data[logRow][key].id) {
											if(response.data[logRow-1][key] && response.data[logRow-1][key].id) {
												//this field is object with ID, so we just compare IDs
												hasDiff = response.data[logRow][key].id != response.data[logRow-1][key].id;
											} else {
												//didn't have ID, now it has
												hasDiff = true;
											}
										} else {
											//compare as values or arrays
											hasDiff = !lodashIsEqual(response.data[logRow][key], response.data[logRow-1][key]);
										}
										if(hasDiff) { //difference is detected
											let changeRow = {
												lastModifiedDate: response.data[logRow].lastModifiedDate,
												lastModifiedBy: response.data[logRow].lastModifiedBy.fullName,
												field: key,
												old: (response.data[logRow-1][key]), //previous entity version
												new: (response.data[logRow][key]), //current entity version
											}
											//add field change
											changeHistoryData.push(changeRow);
										}
									}
								}
							}
						}
						return {items: changeHistoryData, entity_id: this.props.entity_id}
					},
				'EmbedChangeHistory.retrieveData',
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
		//TODO add this embed container to appear by pressing a button against every entity (in lists, tables, views)
		const changeHistoryColumns = [
			{
				Header: this.props.t("ChangeHistory.ChangeTimestamp"),
				accessor: 'lastModifiedDate',
				Cell: (props) => <FieldTimestampContainer
					componentPath={this.props.componentPath+'.items.'+props.index+'.lastModifiedDate'} //existing path in redux store where we put data
				/>
			}, {
				Header: this.props.t("ChangeHistory.ModifiedBy"),
				accessor: 'lastModifiedBy'
			}, {
				Header: this.props.t("ChangeHistory.Column"),
				accessor: 'fieldHeader'
			}, {
				Header: this.props.t("ChangeHistory.OldValue"),
				accessor: 'old',
				Cell: (props) => {
					return props.original.oldCell({
						...props,
						column: {
							...props.column,
							_fieldRelPath: "old",
						},
					});
				}
			}, {
				Header: this.props.t("ChangeHistory.NewValue"),
				accessor: 'new',
				Cell: (props) => {
					return props.original.newCell({
						...props,
						column: {
							...props.column,
							_fieldRelPath: "new",
						},
					});
				}
			}
		];
		let body = (this.props.data instanceof Error) ?
				<FontAwesomeIcon icon="exclamation-circle" className=''/>
			:	((this.props.loading) ?
					<FontAwesomeIcon icon="spinner" spin/>
				:	'');
		if(this.props.items instanceof Array) {
			body = <ReactTable
					className='clients-table align-center-table -striped -highlight'
					data={this.props.items}
					columns={changeHistoryColumns}
					defaultPageSize={5}
					minRows={0}
					showPagination={this.props.items.length>5}
				/>;
		}
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						<ButtonGroup>
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>
								<FontAwesomeIcon icon="history"/>&nbsp;
								{this.props.t("ChangeHistory.ChangeHistory")}&nbsp;
							</Button>
							<Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
						</ButtonGroup>
					</Card.Header>
					<Accordion.Collapse eventKey="0"><Card.Body>
						{body}
					</Card.Body></Accordion.Collapse>
				</Card>
			</Accordion>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchRESTFollow
	const def = getEntityDefinition(ownProps.retrieveType);
	const entityName = def.className;
	let items = data && data.items instanceof Array? data.items.map((item, itemIndex) => {
		const column = def.columns.find((column) => column.accessor == item.field);
		const fakeOldColumn = getUpdatedColumn(entityName, {
			...column,
			accessor: "old",
		});
		const fakeNewColumn = getUpdatedColumn(entityName, {
			...column,
			accessor: "new",
		});
		return {...item,
			fieldHeader: column ? column.Header : item.field,
			oldCell: fakeOldColumn.Cell,
			newCell: fakeNewColumn.Cell,
			_componentPath: ownProps.componentPath+".items."+itemIndex,
			_editable: false,
		};
	}): undefined;
	return {
		auth: state.auth,
		componentPath: ownProps.componentPath,
		data: data,
		items: items,
		error: data instanceof Error ? data : undefined,
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		retrieveType: ownProps.retrieveType,
		entityName: entityName,
		entity_id: ownProps.entity_id,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedChangeHistory);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);

