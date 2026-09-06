import React from 'react';
import { ButtonGroup, Button, Dropdown, DropdownButton, ListGroup, Alert } from 'react-bootstrap';
import Modal from 'react-responsive-modal';
import ReactTable from 'react-table-v6'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import EmbedRetrieveEntityListContainer from './EmbedRetrieveEntityListContainer'
import { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, deleteREST, resetRESTCallLimit } from './../../actions/taskActions';
import { showModal, hideModal } from './../../actions/modal'
import { resolveObjectPath, extractErrorMessage, getMessageFromCode, extractColumnErrorMessage } from './../../scripts/dataUtils';
import { hasPermission } from './../InfoBarContainer';
import { flattenReportColumns, mapReportColumns, getEntityDefinition } from './entityDefinitions.js'

class EmbedEntityListOrTableContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			needUpdate: 1,
			isModalACLOpen: false,
			aclCommonRecordId: undefined,
		};
	}

	onChildUpdate(componentPath,value) {
		if(componentPath) {
			//console.log("EmbedEntityListOrTableContainer.onChildUpdate",componentPath,value);
			this.props.actions.dispatchEditRESTData(componentPath,value);
		}
		this.setState({needUpdate: this.state.needUpdate+1});
		this.props.onChange();
	}

	children_refreshData(children_componentPath, children_data) {
		if(children_data && children_data.id) { //for saved items only set the key to undefined
			let children_href = children_data && children_data.id && children_data._links && children_data._links.self ? children_data._links.self.href : undefined;
			if(children_href && children_componentPath) {
				let fetchPromiseWrapper = {};
				this.props.actions.fetchRESTFollow(
					{
						url: children_href,
						params: {}
					},
					children_componentPath,
					response => ({
						...response.data,
						_editable: false,
					}),
					'EmbedEntityListOrTableContainer.children_refreshData',
					{},
					fetchPromiseWrapper
				);
				fetchPromiseWrapper.promise.then((response) => {this.onChildUpdate();});
			}
		} else { //if it was new item, delete the key
			this.props.actions.dispatchCleanRESTData(children_componentPath);
		}
	}

	children_deleteData(children_componentPath, children_data, index) {
		let promiseWrapper = {};
		this.props.actions.deleteREST({
				method: 'delete',
				url: API_URL+"/"+this.props.retrieveType+"/"+children_data.id,
			},
			children_componentPath,
			promiseWrapper
		);
		promiseWrapper.promise.then((response) => {
			this.onChildUpdate();
			this.props.onCommitChange(response, index, "delete");
		});
		//TODO refresh parent container
	}

	askThenChildrenDeleteData(children_componentPath, children_data, index) {
		this.props.actions.showModal({
			title: this.props.t("ConfirmationRequired"),
			body: this.props.t("AreYouSureYouWantToDelete"),
			acceptLabel: this.props.t("Yes"),
			acceptCallback: () => {
				this.children_deleteData(children_componentPath, children_data, index);
				this.props.actions.hideModal();
			},
			refuseLabel: this.props.t("No"),
			refuseCallback: () => {
				this.props.actions.hideModal();
			},
		})
	}

	askThenChildrenDeleteAllData() {
		this.props.actions.showModal({
			title: this.props.t("ConfirmationRequired"),
			body: this.props.t("AreYouSureYouWantToDeleteAll"),
			acceptLabel: this.props.t("Yes"),
			acceptCallback: () => {
				this.props.data.forEach((elem,index) => {
					this.children_deleteData(this.props.componentPath+"."+index, elem, index);
				});
				this.props.actions.dispatchCleanRESTData(this.props.componentPath);
				this.props.actions.hideModal();
			},
			refuseLabel: this.props.t("No"),
			refuseCallback: () => {
				this.props.actions.hideModal();
			},
		})
	}
	
	topologicalSortHelper(nodes, path, explored, s) {
		explored.add(path);
		// Marks this node as visited and goes on to the nodes
		// that are dependent on this node, the edge is node ----> n
		nodes[path].requiredIds.forEach(n => {
			if (!explored.has(n.path)) {
				this.topologicalSortHelper(nodes, n.path, explored, s);
			}
		});
		// All dependencies are resolved for this node, we can now add
		// This to the stack.
		s.push(nodes[path]);
	}

	topologicalSort(nodes) {
		// Create a Stack to keep track of all elements in sorted order
		let s = [];
		let explored = new Set();

		// For every unvisited node in our graph, call the helper.
		Object.keys(nodes).forEach((key) => {
			let node = nodes[key];
			if (!explored.has(node.path)) {
				this.topologicalSortHelper(nodes, node.path, explored, s);
			}
		});

		return s;
	}

	children_saveData(children_componentPath, children_data, calculateOnly) {
		//console.log("children_saveData",JSON.stringify(children_data));
		//identify the entities in the columns
		let entitiesToSave = {};
		entitiesToSave[children_componentPath] = {entityType: this.props.retrieveType, path: children_componentPath, data: children_data, requiredIds: []};
		this.props.columns.forEach((column) => {
			if(column.columns instanceof Array && column.columns.length > 0) {
				//console.log("column with columns",column);
				let path = children_componentPath + "." + column.columns[0]._parentPath;
				entitiesToSave[path] = 
					{
						entityType: column._entityType, 
						path: path,
						data: resolveObjectPath(column.columns[0]._parentPath,children_data),
						requiredIds: []
					};
				//set the relations between the entities
				if(column._mappedBy) {
					entitiesToSave[path].requiredIds.push({path: path.substring(0,path.lastIndexOf(".")), mappedBy: column._mappedBy});
				} else {
					if(entitiesToSave[path.substring(0,path.lastIndexOf("."))]) {
						entitiesToSave[path.substring(0,path.lastIndexOf("."))].requiredIds.push({path: path, mappedBy: path.substring(path.lastIndexOf(".")+1)});
					} else {
						console.error("Missing entity column for saving",path.substring(0,path.lastIndexOf(".")));
					}
				}
			}
		});
		//sort entities by relation
		let sortedEntitiesToSave = this.topologicalSort(entitiesToSave);
		//console.log("children_saveData entities",this.props.columns, entitiesToSave, sortedEntitiesToSave);
		//save entities in order using recursion
		this.handleSavePromise(children_componentPath, entitiesToSave, sortedEntitiesToSave, 0, calculateOnly);
		return;
	}
	
	handleSavePromise(children_componentPath, entitiesToSave, sortedEntitiesToSave, index, calculateOnly) {
		//console.log("handleSavePromise", children_componentPath, entitiesToSave, sortedEntitiesToSave, index);
		let sortedEntity = sortedEntitiesToSave[index];
		//set the links to the other created entities
		entitiesToSave[sortedEntity.path].requiredIds.forEach((requiredId) => {
			entitiesToSave[sortedEntity.path].data[requiredId.mappedBy] = entitiesToSave[requiredId.path].data._links.self.href;
			entitiesToSave[sortedEntity.path].data._links = {
				...entitiesToSave[sortedEntity.path].data._links,
				[requiredId.mappedBy]: {href: entitiesToSave[requiredId.path].data._links.self.href}
			};
		});
		entitiesToSave[sortedEntity.path].savePromise = this.saveEntity(sortedEntity.entityType, sortedEntity.path, entitiesToSave[sortedEntity.path].data, calculateOnly);
		entitiesToSave[sortedEntity.path].savePromise.then((response) => {
			if(response instanceof Error) {
				this.props.actions.dispatchEditRESTData(children_componentPath + "."+Constants.PATH_FOR_ERROR,response);
				return response;
			}
			//console.log("savePromise",response, entitiesToSave[sortedEntity.path]);
			//get the id in case it is new entity
			entitiesToSave[sortedEntity.path].data.id = response.data.id;
			entitiesToSave[sortedEntity.path].data._links = {
				...entitiesToSave[sortedEntity.path].data._links,
				self: {href: response.data._links.self.href}
			};
			if(sortedEntitiesToSave.length > index + 1) {
				this.handleSavePromise(children_componentPath, entitiesToSave, sortedEntitiesToSave, index + 1, calculateOnly);
			}
			return response;
		});
	}
	
	saveEntity(entityType, children_componentPath, children_data, calculateOnly) {
		let index = children_componentPath.substring(children_componentPath.lastIndexOf(".")+1);
		//check data availability
		let promiseWrapper = {};
		if(children_data) {
			if(children_data.id == undefined || calculateOnly) { //calculateOnly: transient fields don't work with this version of Spring for PATCH, they work only for POST
				let dataWithParent = children_data;
				let nodeHref = this.props.nodeHref || (children_data._links && children_data._links[this.props.nodeType] ? children_data._links[this.props.nodeType].href : undefined);
				if(nodeHref) {
					dataWithParent = {
						...children_data,
						[this.props.nodeType]: nodeHref,
						_links: {
							...children_data._links,
							[this.props.nodeType]: {
								href: nodeHref
							}
						}
					};
				}
				if(calculateOnly) {
					dataWithParent.calculateOnly = true;
				}
				this.props.actions.postRESTData(
					{
						method: 'post',
						url: API_URL+"/"+entityType,
						data: dataWithParent
					},
					children_componentPath,
					'EmbedEntityListOrTableContainer.saveEntity',
					response => ({
						...response.data,
						[this.props.nodeType]: Object.assign({},this.props.nodeData),
						_editable: false,
					}),
					(data) => {
						this.onChildUpdate();
						this.props.onCommitChange(data, index, "post");
					}, //force parent refresh
					undefined,
					promiseWrapper
				);
			} else {
				this.props.actions.patchRESTData(
						{
							method: 'patch',
							url: API_URL+"/"+entityType+"/"+children_data.id,
							data: children_data,
							headers: {'Content-Type': 'application/merge-patch+json'}
						},
						children_componentPath,
						response => ({
							...response.data,
							_editable: false,
						}),
						(data) => {
							this.onChildUpdate();
							this.props.onCommitChange(data, index, "patch");
						}, //force parent refresh
						'EmbedEntityListOrTableContainer.saveEntity',
						promiseWrapper
					);
			}
		}
		return promiseWrapper.promise;
	}

	children_editData(children_componentPath, children_data) {
		if(children_data._editable) {
			this.children_refreshData(children_componentPath, children_data);
		}
		this.onChildUpdate(children_componentPath+'._editable',!children_data._editable);
	}

	getHeaderDropdown(isBtnGroup, column, column_index) {
		if(!isBtnGroup) {
			let actionList = [];
			if(column.actionMenu instanceof Array) {
				actionList = column.actionMenu.map((action, iconIndex) => {
					return <Dropdown.Item key={"menuItem_"+iconIndex+action.icon} id={iconIndex+action.icon} eventKey={iconIndex} onSelect={(eventKey,event) => {event.stopPropagation(); action.onAction(event);}}>
							<FontAwesomeIcon icon={action.icon}/>&nbsp;{action.title}
						</Dropdown.Item>;
				});
			}
			if(column.sortable != false && this.props.sortable != false) { //TODO indicate if sort is active for this column
				actionList.push(<Dropdown.Item key="menuItem_sort_asc" id="sort_asc" eventKey="1" onSelect={(eventKey,event) => {event.stopPropagation(); this.props.onSortedChange([{desc: false, id: column.accessor}],column.accessor,false);}} active={false}>
						<FontAwesomeIcon icon="sort-amount-down-alt"/>&nbsp;{this.props.t("ReactTable.SortAsc")}
					</Dropdown.Item>);
			}
			if(column.sortable != false && this.props.sortable != false) {
				actionList.push(<Dropdown.Item key="menuItem_sort_desc" id="sort_desc" eventKey="2" onSelect={(eventKey,event) => {event.stopPropagation(); this.props.onSortedChange([{desc: true, id: column.accessor}],column.accessor,false);}} active={false}>
					<FontAwesomeIcon icon="sort-amount-up"/>&nbsp;{this.props.t("ReactTable.SortDesc")}
				</Dropdown.Item>);
			}
			if(actionList.length > 0) {
				//if there are actions
				if(column.Header == undefined) console.log('column.Header == undefined',column);
				return <div onClick={e => e.stopPropagation()}><DropdownButton variant="outline-dark"
							id={column.id ? column.id : column.accessor}
							key={column.id ? column.id : column.accessor}
							title={column.Header}>
						{actionList}
					</DropdownButton></div>;
			} else {
				//no actions - just Header
				return <div onClick={e => e.stopPropagation()}><Button variant="outline-dark" id={column.id ? column.id : column.accessor} disabled>{column.Header}</Button></div>;
			}
		} else {
			//if there are actions
			if(column.actionMenu instanceof Array) {
				let actionButtons = column.actionMenu.map((action, iconIndex) => {
					return <Button variant="outline-dark" key={"actionButton_"+iconIndex+action.icon} style={{cursor: "pointer"}} onClick={action.onAction}>
							<FontAwesomeIcon icon={action.icon}/>
						</Button>;
				});
				return <div>{column.Header}{/*column.isRequired ? <span className="text-red">*</span> : undefined*/}&nbsp;{actionButtons}</div>;
			}
			//no actions - just Header
			return (/*column.isRequired ? <div>{column.Header}<span className="text-red">*</span></div> : */column.Header);
		}
	}

	componentDidUpdate(prevProps, prevState) {
		//if there is a row with calculateOnly == true, calculate it (e.g. when adding new rows)
		if(this.props.data instanceof Array) {
			this.props.data.forEach((elem,index) => {
				if(elem.calculateOnly && this.props.data[index][Constants.PATH_FOR_LOADING] == undefined) {
					this.children_saveData(this.props.componentPath+"."+index, elem, true); //not save, this should just calculate
				}
			});
		}
	}

	render() {
		let content = undefined;
		//show error if any
		if(this.props.data instanceof Error) {
			return <Alert variant="danger"><FontAwesomeIcon icon="ban"/> {this.props.data.message}</Alert>;
		}
		let columns = this.props.columns;
		//show controls for editing if needed
		let buttons;
		if(this.props.editable) {
			buttons = [{ fluidSize: 2,
						filterable: false,
						sortable: false,
						width: 150,
						id: "actions",
						Header: this.props.t("ReactTable.Actions"),
						Cell: (props) => {
							let errorMessage = extractErrorMessage(props.original[Constants.PATH_FOR_ERROR], getEntityDefinition(this.props.retrieveType), true);
							if(props.original[Constants.PATH_FOR_LOADING] instanceof Promise) {
								return <FontAwesomeIcon icon="spinner" size="2x" spin/>;
							} else {
								return <div><ButtonGroup variant="outline-dark">
										{props.original._editable ? <Button variant="outline-dark" title={this.props.t("ReactTable.Save")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.children_saveData(this.props.componentPath+"."+props.index, props.original, false);}}><FontAwesomeIcon icon="save"/></Button> : undefined}
										{props.original._editable ? <Button variant="outline-dark" title={this.props.t("ReactTable.Calculate")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.children_saveData(this.props.componentPath+"."+props.index, props.original, true);}}><FontAwesomeIcon icon="calculator"/></Button> : undefined}
										{props.original._editable ? <Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.children_refreshData(this.props.componentPath+"."+props.index, props.original);}}><FontAwesomeIcon icon="sync"/></Button> : undefined}
										{props.original._editable ? undefined : <Button variant="outline-dark" title={this.props.t("ReactTable.Edit")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.children_editData(this.props.componentPath+"."+props.index, props.original);}}><FontAwesomeIcon icon="edit"/></Button>}
										{props.original.id ? <Button variant="outline-dark" title={this.props.t("ReactTable.Delete")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.askThenChildrenDeleteData(this.props.componentPath+"."+props.index, props.original, props.index);}}><FontAwesomeIcon icon="trash-alt"/></Button> : undefined}
										{this.props.aclRestrictable == true ? <Button variant="outline-dark" title={this.props.t("ReactTable.ACL")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.setState({ isModalACLOpen: true, aclCommonRecordId: props.original.id });}}><FontAwesomeIcon icon="lock"/></Button> : undefined}
									</ButtonGroup>
									{errorMessage ?
											<div><small className="text-danger"><FontAwesomeIcon icon="ban"/> {errorMessage}</small></div>
											: undefined}
								</div>;
							}
						},
						actionMenu: [{
								icon: "save",
								title: this.props.t("ReactTable.SaveAll"),
								onAction: (e) => {
									e.stopPropagation(); 
									resetRESTCallLimit(); 
									this.props.data.forEach((elem,index) => {
										if(elem._editable) {
											this.children_saveData(this.props.componentPath+"."+index, elem, false);
										}
									});
								},
							}, {
								icon: "calculator",
								title: this.props.t("ReactTable.Calculate"),
								onAction: (e) => {
									e.stopPropagation(); 
									resetRESTCallLimit(); 
									this.props.data.forEach((elem,index) => {
										if(elem._editable) {
											this.children_saveData(this.props.componentPath+"."+index, elem, true);
										}
									});
								},
							}, {
								icon: "trash-alt",
								title: this.props.t("ReactTable.DeleteAll"),
								onAction: (e) => {
									e.stopPropagation(); 
									resetRESTCallLimit(); 
									this.askThenChildrenDeleteAllData();
								},
							}
						],
					}];
		} else {
			buttons = [];
		}
		columns = buttons.concat(columns);
		//add special attributes to data items
		let column_data = this.props.data instanceof Array ? this.props.data.map((item,index) => {
			let modifiedItem = {
					...item,
					_needUpdate: this.state.needUpdate,
					_asTable: this.props.asTable,
					_componentPath: this.props.componentPath+"."+index,
					_onChildUpdate: (componentPath, value, doCalcOnChange) => {
							this.onChildUpdate(componentPath,value);
							if( doCalcOnChange ) {
								this.children_saveData(this.props.componentPath+"."+index, item, doCalcOnChange);
							}
						},
					[Constants.PATH_FOR_LOADING]: this.props.loading,
				};
			if(this.props.asTable && modifiedItem._editable == false) {
				modifiedItem._editable = undefined
			}
			return modifiedItem;
		}) : [];
		//display each field
		if(this.props.asTable) {
			let tableColumns = mapReportColumns(columns, (column) => {
						let tempCell = column.Cell;
						return ({
							...column, 
							show: (column.show != false && column.show != "hidden"),
							Header: this.getHeaderDropdown(true, column),
							Cell: (props) => {
									if(props.original[Constants.PATH_FOR_ERROR] && tempCell) {
										let errors = extractColumnErrorMessage(props.original[Constants.PATH_FOR_ERROR]);
										let error;
										if(column.accessor && errors instanceof Array) {
											error = errors.find((error) => {
												return column.accessor == error.field
											});
										}
										//console.log("error",error);
										if(error) {
											return <div>{tempCell(props)}<small className="text-danger">{getMessageFromCode(error.error, error.value)}</small></div>;
										} else {
											return tempCell(props);
										}
									} else if(tempCell) {
										return tempCell(props);
									} else {
										return undefined; //"Error: No cell renderer";
									}
								}
							}
						);
					}
				);
			content = <ReactTable
							className='clients-table align-center-table -striped -highlight'
							defaultPageSize={10}
							minRows={0}
							data={column_data}
							columns={tableColumns}

							manual // Forces table not to paginate or sort automatically, so we can handle it server-side
							page={this.props.page}
							pages={this.props.pages} // Display the total number of pages
							pageSize={this.props.pageSize}
							showPagination={false}
							sortable={this.props.sortable}
							sorted={this.props.sorted}
							loading={this.props.loading ? true : false} // Display the loading overlay when we need it
							filterable={this.props.filterable}
							defaultFiltered={this.props.defaultFilter}

							onSortedChange={this.props.onSortedChange}
							onFilteredChange={this.props.onFilteredChange}
						/>
		} else {
			let itemList = undefined; //empty list
			let listHeader = undefined;
			let listFooter = undefined;
			if(this.props.data instanceof Error) {
				itemList = <FontAwesomeIcon key="icon_error" icon="exclamation-circle" size="2x"/>;
			} else {
				if(this.props.loading instanceof Promise) {
					itemList = <FontAwesomeIcon key="icon_loading" icon="spinner" size="2x" spin/>;
				} else {
					if(this.props.data) {
						let flattenedColumns = flattenReportColumns(columns);
						const totalFluidSize = flattenedColumns.reduce((acc,curr) => ((curr.show == false || curr.show == "hidden") ? acc : acc+(curr.fluidSize == undefined ? 1 : curr.fluidSize)),0);
						const useLabels = (totalFluidSize > 12);
						//if(!useLabels) {
							listHeader = flattenedColumns.map((column,column_index) => {
								if(column.show == false || column.show == "hidden") {
									return null;
								}
								//TODO sort on multiple columns
								return <div key={"column_"+column_index} className={"col-sm-"+(column.fluidSize == undefined ? 1 : column.fluidSize)}>
									{ this.getHeaderDropdown(false, column) }
									</div>;
							});
							listHeader = <ListGroup.Item key="listGroupItem_header"><div className="row">{listHeader}</div></ListGroup.Item>;
						//}
						listFooter = flattenedColumns.map((column,column_index) => {
							if(column.show == false || column.show == "hidden") {
								return undefined;
							}
							return <div key={"column_"+column_index} className={"col-sm-"+(column.fluidSize == undefined ? 1 : column.fluidSize)}>
								{ column.Footer instanceof Function ? //show only if needed
									column.Footer({
										data: column_data,
										column: {
											...column,
											_parentPath: column._parentPath,
											_fieldPath: column._fieldPath,
											_fieldRelPath: column._fieldRelPath,
										},
									})
									: undefined
								}
								</div>;
						});
						listFooter = <ListGroup.Item key="listGroupItem_footer"><div className="container-fluid">{listFooter}</div></ListGroup.Item>;
						itemList =
							this.props.data.map((item,index) => {
								let columnsRender = undefined;
								if(item instanceof Error) {
									columnsRender = <FontAwesomeIcon icon="exclamation-circle" size="2x"/>;
								} else {
									if(item[Constants.PATH_FOR_LOADING] instanceof Promise) {
										columnsRender = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
									} else {
										let errors = extractColumnErrorMessage(column_data[index][Constants.PATH_FOR_ERROR]);
										//group each column in rows
										let colFluidSum = 0;
										let rowIndex = 0;
										let groupedColumns = [[]];
										flattenedColumns = flattenedColumns.filter((col) => col.show == undefined || col.show == true);
										for (var i = 0; i < flattenedColumns.length; i++) {
											let column = flattenedColumns[i];
											colFluidSum += column.fluidSize || 1;
											if(colFluidSum > 12){
												colFluidSum = column.fluidSize || 1;
												rowIndex = rowIndex + 1;
												groupedColumns[rowIndex] = [];
											}
											let error;
											if(column.accessor && errors instanceof Array) {
												error = errors.find((error) => {
													return column.accessor == error.field
												});
											}
											groupedColumns[rowIndex].push(
												<div key={"column_"+i} className={"col-sm-"+(column.fluidSize == undefined ? 1 : column.fluidSize)}>
													{useLabels ? <label className="contracts-add-form-label">{column.Header}{/*column.isRequired ? <span className="text-red">*</span> : undefined*/}</label> : undefined}
													{column.Cell instanceof Function ?
															column.Cell({
																original: column_data[index],
																value: resolveObjectPath(column.accessor,column_data[index]),
																index: index,
																column: {
																	...column,
																	_parentPath: column._parentPath,
																	_fieldPath: column._fieldPath,
																	_fieldRelPath: column._fieldRelPath,
																},
																_asTable: false,
															})
															: undefined
													}
													{error ? <small className="text-danger">{getMessageFromCode(error.error, error.value)}</small> : undefined}
												</div>
											)
										}
										columnsRender = groupedColumns.map((colGroup,index) =>
											<div className="row" key={"row_"+index}>
												{colGroup}
											</div>
										)
									}
								}
								return <ListGroup.Item key={"listGroupItem_"+index+"_"+(item && item.id ? item.id : 'add').toString()}>
											<div className="container-fluid">
												{columnsRender}
											</div>
										</ListGroup.Item>;
							});
					}
				}
			}
			content = <ListGroup key="listGroup">
						{listHeader}
						{itemList}
						{listFooter}
					</ListGroup>;
		}
		//get definition for access control table
		const aclDef = getEntityDefinition("accessControls",{commonRecordId: {show: false}}); //TODO put info if the ACL makes the entity public or private
		return <div key="entityListContainer">
				{content}
				{this.props.aclRestrictable == true ?
						<Modal key="modalACL" open={this.state.isModalACLOpen} onClose={() => {this.setState({ isModalACLOpen: false });}} showCloseIcon={true}>
							<EmbedRetrieveEntityListContainer
								title={aclDef.label}
								icon={aclDef.icon}
								columns={aclDef.columns}
								componentPath={this.props.componentPath+".acl"}
								retrieveType="accessControls"
								defaultFilter={[{id: "commonRecordId", value: this.state.aclCommonRecordId}]}
								onAddData={(newItem) => {
									if(newItem.commonRecordId == undefined && this.state.aclCommonRecordId) {
										newItem.commonRecordId = this.state.aclCommonRecordId;
									}
									return newItem;
								}}
								expanded={true}
								asTable={true}
							/>
						</Modal>
					: undefined}
			</div>;
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	const permEdit = hasPermission(state.rest.currentPermissions, "ROLE_PATCH_"+ownProps.retrieveType);
	return {
		auth: state.auth,
		componentPath: ownProps.componentPath, //redux state path to data array for refresh, new and param to children
		data: ownProps.data, //array with the data
		count: ownProps.data instanceof Array ? ownProps.data.length : 0, //to force redux update on list add/remove
		loading: ownProps.data && ownProps.data[Constants.PATH_FOR_LOADING] ? ownProps.data[Constants.PATH_FOR_LOADING] : ownProps.loading, //Is parent loading? Then wait before retrieving!
		columns: ownProps.columns, //columns render function
		retrieveType: ownProps.retrieveType, //type of the root
		nodeType: ownProps.nodeType, //key to the parent for save purpose
		nodeHref: ownProps.nodeHref, //href of parent for save purpose
		nodeData: ownProps.nodeData, //data of parent for attachments
		asTable: ownProps.asTable !== undefined ? ownProps.asTable : true,
		page: ownProps.page ? ownProps.page : undefined,
		pages: ownProps.pages ? ownProps.pages : undefined,
		pageSize: ownProps.pageSize ? ownProps.pageSize : 10,
		sortable: ownProps.sortable,
		sorted: ownProps.sorted ? ownProps.sorted : undefined,
		filterable: ownProps.filterable,
		defaultFilter: ownProps.defaultFilter,
		onChange: ownProps.onChange ? ownProps.onChange : (()=>{}),
		onCommitChange: ownProps.onCommitChange ? ownProps.onCommitChange : (()=>{}),
		onSortedChange: ownProps.onSortedChange ? ownProps.onSortedChange : (()=>{}),
		onFilteredChange: ownProps.onFilteredChange ? ownProps.onFilteredChange : (()=>{}),
		editable: permEdit ? (ownProps.editable !== undefined ? ownProps.editable : true) : false, //if the items can be edited
		aclRestrictable: ownProps.aclRestrictable !== undefined ? ownProps.aclRestrictable : false, //if the items should have button for ACL
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, postRESTData, patchRESTData, deleteREST, showModal, hideModal }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedEntityListOrTableContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
