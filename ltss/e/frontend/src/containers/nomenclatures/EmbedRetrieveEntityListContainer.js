import React from 'react';
import { Accordion, Card, Dropdown, DropdownButton, Alert, ButtonGroup, Button } from 'react-bootstrap';
import querystring from 'querystring'
import { CSVLink, CSVDownload } from 'react-csv';
import ReactToPrint from "react-to-print";
import ReactTable from 'react-table-v6'
import Modal from 'react-responsive-modal';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import lodashIsEqual from 'lodash/isEqual'
import moment from 'moment'
import { withTranslation } from 'react-i18next';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import EmbedEntityListOrTableContainer from './EmbedEntityListOrTableContainer'
import RetrieveDataContainer from './RetrieveDataContainer';
import TablePlain from './../../components/shared/TablePlain'

import * as Constants from './../../static/constants';
import { tableFilterToParam, flattenReportColumns, builderDataToProjection, builderURLFromColumns, mapReportColumns, getEntityDefinition } from './entityDefinitions.js'
import { fetchRESTFollow, resetRESTCallLimit, dispatchCleanRESTData, dispatchEditRESTData, patchRESTMultiData, postRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';
import { ContextAwareToggle } from './../../scripts/util';
import { hasPermission } from './../InfoBarContainer';

//Embed: this is a middle-sized component must be placed inside another component and get its properties from it
//Container: redux container class
class EmbedRetrieveEntityListContainer extends React.Component {
	
	constructor(...args) {
		super(...args);
		this.state = {
			open: this.props.expanded !== undefined ? this.props.expanded : false,
			asTable: this.props.asTable !== undefined ? this.props.asTable : false,
			linkModalIsOpen: false,
			editFieldPanel: <Card/>,
			requiredPageIndex: this.props.defaultPageIndex,
			requiredPageSize: this.props.defaultPageSize,
			requiredSort: this.props.defaultSort,
			requiredFilter: this.props.defaultFilter,
			travHeaders: [[]],
			travData: [[]]
		};
	}

	refreshData() {
		//force table refresh
		if(this.props.error) {
			this.props.actions.dispatchEditRESTData(this.props.componentPath,undefined);
		} else {
			this.props.actions.dispatchEditRESTData(this.props.componentPath+'.pageData',undefined);
		}
	}

	addData(newItem = {}) {
		let newList = this.props.items;
		newItem._editable = true;
		newItem.calculateOnly = true; //force the new row to be calculated
		newItem[this.props.parentAttr] = ((this.props.parentAttr !== undefined && this.props.parentData !== undefined) ? Object.assign({},this.props.parentData) : undefined);
		if(this.props.onAddData instanceof Function) {
			this.props.onAddData(newItem);
		}
		newList.unshift(newItem);
		this.props.actions.dispatchEditRESTData(this.props.itemsComponentPath,newList);
		//TODO find how to focus on the new item
		this.setState({ open: true });
	}

	selectRow(isSelected,rowId,rowData = undefined) {
		let selectedRows = this.props.selectedRows;
		let isChange = false;
		if(!isSelected && selectedRows.has(rowId)) {
			selectedRows.delete(rowId);
			isChange = true;
		}
		if(isSelected && !selectedRows.has(rowId)) {
			const rowDataCopy = Object.assign({},rowData);
			if(this.props.isMultiSelect) {
				selectedRows.set(rowId,rowDataCopy);
			} else {
				selectedRows = new Map();
				selectedRows.set(rowId,rowDataCopy);
			}
			isChange = true;
		}
		if(isChange) {
			this.props.actions.dispatchEditRESTData(this.props.componentPath+'.selectedRows.map',selectedRows);
			this.props.afterAddSelectedRowsF(selectedRows);
		}
	}

	selectAllFilteredRows(isMarked) {
		let fetch_url = builderURLFromColumns(
				(this.props.retrieveType ? API_URL+"/"+this.props.retrieveType : undefined),
				this.props.retrieveType,
				this.props.columns,
				this.props.excludeRootEntity);
		let filter_param = tableFilterToParam(this.state.requiredFilter, this.props.columns, this.props.retrieveType);
		let entityDefinition = getEntityDefinition(this.props.retrieveType);
		if(this.props.parentAttr !== undefined && this.props.parentHref !== undefined) {
			filter_param[(entityDefinition.className ? entityDefinition.className + "." : "")+this.props.parentAttr+".id"] = this.props.parentHref.replace(/.+\/(\d+)/,"$1");
		} else if(this.props.parentAttr !== undefined && this.props.parentData !== undefined && this.props.parentData.id !== undefined) {
			filter_param[(entityDefinition.className ? entityDefinition.className + "." : "")+this.props.parentAttr+".id"] = this.props.parentData.id;
		}
		this.props.actions.fetchRESTFollow(
			{
				url: fetch_url,
				params: {
					...filter_param,
					size: this.props.totalElements
				},
				paramsSerializer: function(params) {
					//needed for the from-to dates
					return querystring.stringify(params)
				}
			},
			this.props.componentPath+'.selectedRows.map',
			(response) => {
				let newData = response.data;
				//transform data to "repository response"-like
				builderDataToProjection(newData,this.props.retrieveType);
				if(newData && newData._embedded && newData._embedded[this.props.retrieveType] instanceof Array) {
					let result = this.props.selectedRows;
					newData._embedded[this.props.retrieveType].forEach((curr) => {
						if(isMarked) {
							result.set(curr.id,curr);
						} else {
							result.delete(curr.id);
						}
					});
					return result;
				}
				return undefined;
			},
			'EmbedRetrieveEntityListContainer.selectAllFilteredRows',
			{}
		);
	}

	editSelected(column,componentPath) {
		let editFieldPanel = 
			<div>
				<ButtonGroup>
					<Button variant="outline-dark"
							onClick={(e) => {
								e.stopPropagation();
								resetRESTCallLimit();
								this.setState({ linkModalIsOpen: false });
								if(column.editableColumn.post) {
									this.postAllData(column.editableColumn.post);
								} else {
									this.saveAllData(column._fieldPath);
								}
							}}>
						<FontAwesomeIcon icon="save"/>&nbsp;
						{this.props.t("ReactTable.EditTheSelectedRows")}
					</Button>
				</ButtonGroup>
				{column.editableColumn.editFieldPanel instanceof Function ?
					column.editableColumn.editFieldPanel(componentPath)
					: <Card>
						<label className="contracts-add-form-label">{column.Header} <span className="text-red">*</span></label>
						{
							column.Cell({
								//original: column_data[index],
								original: {
									_componentPath: componentPath+".edit",
									_editable: true,
									[Constants.PATH_FOR_LOADING]: false,
									_onChildUpdate: (href) => {},
								},
								//value: resolveObjectPath(column.accessor,column_data[index]),
								//index: index,
								column: column,
								_asTable: false,
							})
						}
					</Card>}
				<Card>
					<ReactTable
						className='clients-table align-center-table -striped -highlight'
						data={Array.from(this.props.selectedRows.values())}
						columns={this.props.selectedRowsColumns}
						defaultPageSize={5}
						showPagination={true}
					/>
				</Card>
			</div>;
		this.setState({ linkModalIsOpen: true, editFieldPanel: editFieldPanel });
	}

	saveAllData(_fieldPath) {
		//check data availability
		if(this.props.editData && this.props.editData[_fieldPath]) {
			let patches = [];
			let editData = this.props.editData[_fieldPath];
			this.props.selectedRows.forEach((curr) => {
				patches.push({
					restConfig: {
						method: 'patch',
						url: API_URL+"/"+this.props.retrieveType+"/"+curr.id,
						data: [{ "op": "add", "path": "/"+_fieldPath, "value": editData }],
						headers: {'Content-Type': 'application/json-patch+json'}
					},
					statePath: this.props.componentPath+'.selectedRows.updated.'+curr.id, //TODO curr.id is probably wrong, shouldn't it be the table row number?
					mapping: response => (response.data),
					callback: (data) => {}
				});
			});
			this.props.actions.patchRESTMultiData(patches,(responses) => {
				this.props.onChange();
				this.props.onCommitChange(responses, -1, "saveAll");
				this.refreshData();
			},'EmbedRetrieveEntityListContainer.saveAllData');
		}
	}

	postAllData(post) {
		if(this.props.editData) {
			this.props.selectedRows.forEach((curr) => {
				this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+post.endpoint,
						data: {...this.props.editData,[post.nodeField]: curr._links.self.href}
					},
					this.props.componentPath+'.selectedRows.updated.'+curr.id, //TODO curr.id is probably wrong, shouldn't it be the table row number?
					'EmbedRetrieveEntityListContainer.postAllData',
					undefined,
					(data) => {
						this.props.onChange();
						this.props.onCommitChange(data, -1, "postAll");
					}
				);
			});
		}
	}

	componentDidMount(){
		if(this.props.hasRowSelecting) { //TODO check how this works in every place it is used
			if(this.props.addSelectedRows != undefined && !(this.props.addSelectedRows instanceof Promise)) {
				this.selectRow(true,this.props.addSelectedRows.id,this.props.addSelectedRows);
			}
		}
		this.loadExportData()
	}

	componentDidUpdate(prevProps, prevState) {
		if( !lodashIsEqual(prevProps.defaultFilter, this.props.defaultFilter) ) {
			this.setState({requiredFilter: this.props.defaultFilter});
		}
		if(this.props.hasRowSelecting) { //TODO check how this works in every place it is used
			if(this.props.addSelectedRows != undefined && !(this.props.addSelectedRows instanceof Promise)) {
				this.selectRow(true,this.props.addSelectedRows.id,this.props.addSelectedRows);
			}
		}
		this.loadExportData()
	}

	onPageChange(pageIndex) {
		resetRESTCallLimit();
		this.setState({requiredPageIndex: pageIndex});
	}
	onPageSizeChange(pageSize, pageIndex) {
		resetRESTCallLimit();
		this.setState({requiredPageIndex: pageIndex, requiredPageSize: pageSize});
	}
	onSortedChange(newSorted, column, shiftKey) {
		resetRESTCallLimit();
		this.setState({requiredSort: newSorted});
	}
	onFilteredChange(filtered, column) {
		resetRESTCallLimit();
		//avoid removing additional filters that are not by visible columns
		let newFilter = this.state.requiredFilter.filter((elem) => elem != undefined && elem.id != column.id);
		let columnFilter = filtered.find((elem) => elem.id == column.id);
		if( columnFilter ) {
			newFilter.push(columnFilter);
		}
		this.setState({requiredFilter: newFilter});
	}
	getSafePage (page) {
		if (isNaN(page)) {
			page = this.props.defaultPageIndex;
		}
		return Math.min(Math.max(page, 0), this.props.pages - 1)
	}
	changePage(page) {
		page = this.getSafePage(page)
		if (this.state.requiredPageIndex !== page) {
			this.onPageChange(page)
		}
	}
	applyPage(e) {
		e && e.preventDefault()
		const page = this.state.requiredPageIndex
		this.changePage(page === '' ? this.props.defaultPageIndex : page)
	}

	loadExportData(){
		if(this.tableContainerRef){
			setTimeout(() => {
				let headersEl = $(this.tableContainerRef).find('.ReactTable .rt-table .rt-thead.-header .rt-resizable-header'),
					travHeaders = headersEl.map((index, el) => el.textContent).toArray(),
					trimFirstCol = true
				trimFirstCol = !(travHeaders && travHeaders[0] && travHeaders[0] != '')
				if(trimFirstCol){
					travHeaders.shift()
				}
				let rowsEl = $(this.tableContainerRef).find('.ReactTable .rt-table .rt-tbody .rt-tr-group'),
					travData = []
				rowsEl.each((index, row) => {
					let tds = $(row).find('.rt-td'),
						tdsContent = tds.map((i, td) => {
						return td.textContent || ''
					}).toArray()
					if(trimFirstCol){
						tdsContent.shift()
					}
					travData.push(tdsContent)
				})
				if(JSON.stringify(this.state.travHeaders) != JSON.stringify(travHeaders) || JSON.stringify(this.state.travData) != JSON.stringify(travData)){
					this.setState({
						travHeaders, travData
					})
				}
			}, 500)
		}
	}
	
	copyRowsToClipboard() {
		let copiedRows = undefined;
		if(this.props.hasRowSelecting) {
			copiedRows = Array.from(this.props.selectedRows, ([name, value]) => (value));
		} else if(this.props.items) {
			copiedRows = this.props.items;
		}
		if(copiedRows) {
			this.props.actions.dispatchEditRESTData("_clipboard",copiedRows);
		}
	}
	
	pasteRowsFromClipboard() {
		if(this.props._clipboard) {
			//clean up original data
			let newItems = this.props._clipboard.map((item) => {
				let newItem = {
					...item,
					_editable: true,
				}
				delete newItem.id;
				if(newItem._links) {
					Object.keys(newItem._links).forEach((key) => {
						if(newItem[key] && newItem[key]._links && newItem[key]._links.self && newItem[key]._links.self.href) {
							//console.log("update links",newItem._links[key].href, newItem[key]._links.self.href);
							newItem._links[key].href = newItem[key]._links.self.href;
						}
					});
				}
				if(this.props.parentAttr !== undefined && this.props.parentData !== undefined) {
					newItem[this.props.parentAttr] = Object.assign({},this.props.parentData);
					newItem._links[this.props.parentAttr].href = this.props.parentHref;
				}
				return newItem;
			});
			this.props.actions.dispatchEditRESTData(this.props.itemsComponentPath,newItems);
		}
	}

	render() {
		const header_spinner = (this.props.data instanceof Error) ?
				<FontAwesomeIcon icon="exclamation-circle"/>
			:	((this.props.loading instanceof Promise)?
					<FontAwesomeIcon icon="spinner" spin/>
				:	'');
		let columnsWithActions = this.props.columns;
		if(this.props.columns instanceof Array) {
			columnsWithActions = mapReportColumns(this.props.columns, (column) => ({
				...column,
				_summaryData: this.props.summary,
			}));
		}
		if(this.props.hasRowSelecting) {
			if(this.props.permEdit) {
				columnsWithActions = mapReportColumns(columnsWithActions, (column) => {
						if(column.editableColumn != undefined) {
							let columnWithAction = Object.assign({}, column);
							columnWithAction.actionMenu = [{
								icon: "edit",
								title: this.props.t("ReactTable.EditTheSelectedRows"),
								onAction: (e) => {e.stopPropagation(); resetRESTCallLimit(); this.editSelected(columnWithAction,this.props.componentPath+".selectedRows")},
							}];
							return columnWithAction;
						}
						return column;
					});
			}
			//TODO this is not OK with paging (should count selected rows vs total count
			let isSelectedAllRows = this.props.items && this.props.selectedRows ? this.props.items.every((row) => (this.props.selectedRows.has(row.id))) : true;
			columnsWithActions.unshift({
				Header: (this.props.isMultiSelect ?
							<input type="checkbox" checked={isSelectedAllRows} onChange={(e) => {this.selectAllFilteredRows(e.target.checked);}}/>
							: undefined
						),
				accessor: '__isRowSelected',
				width: 50,
				filterable: false,
				sortable: false,
				Cell: props => <input type="checkbox" checked={this.props.selectedRows.has(props.original.id)} onChange={(e) => {this.selectRow(e.target.checked,props.original.id,props.original);}}/>
			});
		}
		//page size dropdown
		let pageSizesArray = [5,10,20,50];
		if(this.props.defaultPageSize > 0 && pageSizesArray.indexOf(this.props.defaultPageSize) == -1){
			pageSizesArray.push(this.props.defaultPageSize);
			pageSizesArray.sort((a,b) => a - b);
		}
		let pageSizeDropdown = <DropdownButton variant="outline-dark" as={ButtonGroup} id="dropdown_pageSize" title={this.state.requiredPageSize}>
				{pageSizesArray.map((sizeItem, index) => {
					return <Dropdown.Item eventKey={index} onSelect={(eventKey,event) => {this.onPageSizeChange(sizeItem, this.state.requiredPageIndex)}} active={this.state.requiredPageSize == sizeItem} id={"pageSize_"+sizeItem}>{sizeItem} {this.props.t("ReactTable.rowsText")}</Dropdown.Item>;
				})}
			</DropdownButton>;
		return (<Accordion activeKey={this.state.open ? "0" : ""}>
			<div ref={(el) => (this.tableContainerRef = el)}>
			<Card><Card.Header>
				<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>
					<FontAwesomeIcon icon={this.props.icon}/>&nbsp;
					{this.props.title} ({this.props.totalElements} {this.props.t("ReactTable.pcs")}{header_spinner})&nbsp;
					{this.props.t("ReactTable.pageText")}&nbsp;{this.state.requiredPageIndex+1}&nbsp;{this.props.t("ReactTable.ofText")}&nbsp;{this.props.pages}&nbsp;
					<FontAwesomeIcon icon={this.state.open ? "caret-square-up" : "caret-square-down"}/>
				</Button>
				<ButtonGroup>
					{this.props.editable && this.props.creatable ?
							<Button variant="outline-dark" title={this.props.t("ReactTable.New")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.addData();}}>
								<FontAwesomeIcon icon="plus" />
							</Button>
							: undefined
					}
					<Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")+" (последно зареждане: "+moment(this.props._retrieveTimeStamp).fromNow()+")"} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}>
						<FontAwesomeIcon icon="sync"/>
					</Button>
				</ButtonGroup>
				<ButtonGroup>
					{this.state.asTable ?
						<Button variant="outline-dark" title={this.props.t("Print")}>
							<ReactToPrint
								trigger={() => <FontAwesomeIcon icon='print'/>}
								content={() => this.tableRef }
								pageStyle="@page { size: auto;  margin: 0mm; }
								@media print { body { -webkit-print-color-adjust: exact; } }
								.invisible-table * { border: 1px solid #000000; padding: 2px; }"
								copyStyles={false}
							/>
						</Button>
						: undefined
					}
					{this.state.asTable ?
						<Button variant="outline-dark" title={this.props.t("Export")}>
							<CSVLink data={this.state.travData} headers={this.state.travHeaders} filename="export.csv"	>
								<FontAwesomeIcon icon='file-excel'/>
							</CSVLink>
						</Button>
						: undefined
					}
					<TablePlain
						className="invisible-table"
						headers={this.state.travHeaders}
						data={this.state.travData}
						ref={(el) => (this.tableRef = el)}
					/>
					<Button variant="outline-dark" title={this.props.t(this.state.asTable ? "ReactTable.AsList" : "ReactTable.AsTable")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.setState({asTable: !this.state.asTable});}}>
						<FontAwesomeIcon icon={this.state.asTable ? "list" : "table"}/>
					</Button>
				</ButtonGroup>
				<ButtonGroup>
					<Button variant="outline-dark" disabled={this.state.requiredPageIndex <= 0} title={this.props.t("ReactTable.previousText")} onClick={this.state.requiredPageIndex <= 0 ? undefined
							: (e) => {e.stopPropagation(); resetRESTCallLimit(); this.changePage(this.state.requiredPageIndex - 1); }}>
						<FontAwesomeIcon icon="backward"/>
					</Button>
					{pageSizeDropdown}
					<Button variant="outline-dark" disabled={this.state.requiredPageIndex >= this.props.pages - 1} title={this.props.t("ReactTable.nextText")} onClick={this.state.requiredPageIndex >= this.props.pages - 1 ? undefined
							: (e) => {e.stopPropagation(); resetRESTCallLimit(); this.changePage(this.state.requiredPageIndex + 1); }}>
						<FontAwesomeIcon icon="forward"/>
					</Button>
					{this.props.headerStatus}
				</ButtonGroup>
				{this.props.enableCopyPaste ?
					<ButtonGroup>
						<Button variant="outline-dark" disabled={false} title={this.props.t("ReactTable.Copy")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.copyRowsToClipboard(); }}>
							<FontAwesomeIcon icon="copy"/>
						</Button>
						<Button variant="outline-dark" disabled={!this.props._clipboard} title={this.props.t("ReactTable.Paste")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.pasteRowsFromClipboard(); }}>
							<FontAwesomeIcon icon="paste"/>
						</Button>
					</ButtonGroup>
					: undefined
				}
				</Card.Header>
				<Accordion.Collapse eventKey="0"><Card.Body>
					{this.props.error ?
						<Alert variant="danger"><FontAwesomeIcon icon="ban"/> {this.props.error.message}</Alert>
						: <EmbedEntityListOrTableContainer
								componentPath = {this.props.itemsComponentPath} //redux state path to data array for refresh, new and param to children
								data = {this.props.items} //array with the data
								loading = {this.props.loading} //Is parent loading? Then wait before retrieving!
								columns = {columnsWithActions} //columns render function
								retrieveType = {this.props.retrieveType}
								nodeType = {this.props.parentAttr} //key to the parent for save purpose
								nodeHref = {this.props.parentHref} //href of parent for save purpose
								nodeData = {this.props.parentData} //data of parent for the attachments
								asTable = {this.state.asTable}
								page={this.state.requiredPageIndex}
								pages={this.props.pages} // Display the total number of pages
								pageSize={this.state.requiredPageSize}
								sortable={this.props.sortable}
								sorted={this.state.requiredSort}
								filterable={this.props.filterable}
								defaultFilter={this.state.requiredFilter}
								onSortedChange={(newSorted, column, shiftKey) => {this.onSortedChange(newSorted, column, shiftKey);}}
								onFilteredChange={(filtered, column) => {this.onFilteredChange(filtered, column);}}
								editable={this.props.editable}
								aclRestrictable={getEntityDefinition(this.props.retrieveType).aclRestrictable}
								onChange={()=>{this.props.onChange();}}
								onCommitChange={(data,index,eventName)=>{this.props.onCommitChange(data,index,eventName);}}
							/>
					}
					{this.props.children}
					{this.props.hasRowSelecting ?
						<Modal open={this.state.linkModalIsOpen} little onClose={() => {this.setState({ linkModalIsOpen: false });}} showCloseIcon={false}>
							{this.state.editFieldPanel}
						</Modal>
						: undefined
					}
				</Card.Body></Accordion.Collapse>
			</Card>
			<RetrieveDataContainer
					retrieveType={this.props.retrieveType}
					expandColumns={[]}
					columns={this.props.columns}
					excludeRootEntity={this.props.excludeRootEntity}
					loading={this.props.loading}
					componentPath={this.props.componentPath+".pageData"}
					parentHref={this.props.parentHref}
					parentAttr={this.props.parentAttr}
					onAfterRetrieve={this.props.onAfterRetrieve}
					dataRefreshInterval = {this.props.dataRefreshInterval}
					defaultPageIndex={this.state.requiredPageIndex}
					defaultPageSize={this.state.requiredPageSize}
					defaultSort={this.state.requiredSort}
					defaultFilter={this.state.requiredFilter}
			/>
			</div>
		</Accordion>);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	const containerState = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	const _clipboard = state.rest._clipboard;
	//data._entitiesToAdd - used for inserting rows from other components
	const data = containerState ? containerState.pageData : undefined;
	let items = undefined;
	if(data && data._embedded) {
		//handle case with class hierarchy with single table strategy
		//put all subclasses in one array
		items = [];
		for(let key in data._embedded) {
			if(key != "hashMaps" && key != "summaries") { //exclude report builder raw results
				items = items.concat(data._embedded[key]);
			}
		}
	}
	const page = data ? data.page : undefined;
	const pages = page ? page.totalPages : 0;
	const defaultPageIndex = ownProps.defaultPageIndex ? ownProps.defaultPageIndex : 0;
	const defaultPageSize = ownProps.defaultPageSize ? ownProps.defaultPageSize : 20;
	const defaultSort = ownProps.defaultSort ? ownProps.defaultSort : [{id: "id", desc: true}];
	const defaultFilter = ownProps.defaultFilter ? ownProps.defaultFilter : [{}];
	const totalElements = page ? page.totalElements : 0;
	const summary = data && data._embedded && data._embedded.summaries ? data._embedded.summaries[0] : undefined;
	const permCreate = hasPermission(state.rest.currentPermissions, "ROLE_POST_"+ownProps.retrieveType);
	const permEdit = hasPermission(state.rest.currentPermissions, "ROLE_PATCH_"+ownProps.retrieveType);
	return {
		//data
		auth: state.auth,
		data: data, //retrieved data
		count: items ? items.length : 0, //to force redux update on list add/remove, also shown in header
		items: items, //items array from the retrieved data
		summary: summary,
		_clipboard: _clipboard,
		onAddData: ownProps.onAddData,
		onChange: ownProps.onChange ? ownProps.onChange : (()=>{}),
		onCommitChange: ownProps.onCommitChange ? ownProps.onCommitChange : (()=>{}),
		//retrieval
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		_retrieveTimeStamp: (data && data._retrieveTimeStamp ? data._retrieveTimeStamp : undefined),
		onAfterRetrieve: ownProps.onAfterRetrieve ? ownProps.onAfterRetrieve : (() => {}),
		error: data instanceof Error ? data : undefined,
		componentPath: ownProps.componentPath, //redux state path to store the data
		itemsComponentPath: ownProps.componentPath+'.pageData._embedded.'+ownProps.retrieveType,
		parentHref: ownProps.parentHref, //href of parent for filter and save purpose
		parentAttr: ownProps.parentAttr, //the attribute name that holds the parent href
		parentData: ownProps.parentData, //already available data of the parent
		retrieveType: ownProps.retrieveType, //entity name for retrieval
		excludeRootEntity: ownProps.excludeRootEntity,
		//paging, filtering, sorting
		sortable: ownProps.sortable,
		filterable: ownProps.filterable,
		defaultPageIndex: defaultPageIndex,
		defaultPageSize: defaultPageSize,
		defaultSort: defaultSort,
		defaultFilter: defaultFilter,
		pages: pages,
		totalElements: summary ? summary["count(*)"] : totalElements, //because of the extra summaries row, we have to have stupid cases
		dataRefreshInterval: ownProps.dataRefreshInterval,
		//UI
		title: ownProps.title ? ownProps.title : ownProps.t("N/A"), //rendered before the header buttons
		icon: ownProps.icon ? ownProps.icon : "list", //fontawesome rendered in front of the title
		columns: ownProps.columns, //columns render function
		headerStatus: ownProps.headerStatus, //rendered after the header buttons
		expanded: ownProps.expanded, //default state of the panel
		asTable: ownProps.asTable, //default view
		creatable: permCreate ? (ownProps.creatable != undefined ? ownProps.creatable : true) : false,
		permEdit: permEdit,
		editable: permEdit ? (ownProps.editable !== undefined ? ownProps.editable : true) : false, //if the list and its items can be edited
		enableCopyPaste: ownProps.enableCopyPaste != undefined ? ownProps.enableCopyPaste: false,
		//selecting rows
		hasRowSelecting: ownProps.hasRowSelecting == undefined ? false : ownProps.hasRowSelecting,
		selectedRows: (containerState && containerState.selectedRows && containerState.selectedRows.map instanceof Map) ? containerState.selectedRows.map : new Map(),
		selectedRowsCount: (containerState && containerState.selectedRows && containerState.selectedRows.map) ? containerState.selectedRows.map.size : 0, //force update
		editData: (containerState && containerState.selectedRows && containerState.selectedRows.edit) ? containerState.selectedRows.edit : undefined,
		selectedRowsColumns: ownProps.selectedRowsColumns,
		addSelectedRows: ownProps.addSelectedRows, //list of rows that should be initially selected
		afterAddSelectedRowsF: ownProps.afterAddSelectedRowsF instanceof Function ? ownProps.afterAddSelectedRowsF : () => {}, //callback function after each change in selections
		onSelectRowF: ownProps.onSelectRowF,
		isMultiSelect: ownProps.isMultiSelect != undefined ? ownProps.isMultiSelect : true,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, patchRESTMultiData, postRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
export default withTranslation()(connect(mapStateToProps, mapDispatchToProps, null, { withRef: true })(EmbedRetrieveEntityListContainer));
