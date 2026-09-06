import React from 'react';
import CreatableSelect from 'react-select/lib/Creatable';
import Select,{ Async } from 'react-select'
import { Link } from 'react-router-dom'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import { builderDataToProjection, getExpandedColumns } from '../nomenclatures/entityDefinitions';
import { fetchRESTFollow, dispatchEditRESTData, postRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

import CommonFieldContainer from './CommonFieldContainer';
import RetrieveDataContainer from '../nomenclatures/RetrieveDataContainer';

//redux container class
class FieldSelectContainer extends React.Component {

	constructor(...args) {
		super(...args);
		this.state = {
			userFilter: '',
		};
	}

	retrieveData() {
		if(this.props.href && this.props.componentPath && this.props.listType && !this.props.data) {
			this.props.actions.fetchRESTFollow(
				{
					url: this.props.href,
					params: {
						size: this.props.listMax,
					}
				},
				this.props.componentPath,
				data => ({...data.data,_retrieveHref: this.props.href}),
				'FieldSelectContainer.retrieveData',
				{}
			);
		}
	}

	componentDidMount(){
		this.retrieveData();
	}
	
	componentDidUpdate(prevProps, prevState) {
		this.retrieveData();
	}
	
	createItem(newValue,dataItems,onChange) {
		//add new options to the list
		if(newValue) {
			let componentPath = "";
			if(this.props.items instanceof Array && this.props.items.length > 0) {
				if(!this.props.items.some((item) => item[this.props.listAttr] == newValue)) { //if it is new item, not in the list
					componentPath = Constants.PATH_FOR_OPTIONSLIST+"."+this.props.componentPath+"._embedded."+this.props.listType+"."+this.props.items.length;
					//this.props.actions.dispatchEditRESTData(Constants.PATH_FOR_OPTIONSLIST+"."+this.props.componentPath+"._embedded."+this.props.listType+"."+this.props.items.length,{[this.props.listAttr]: newValue});
				}
			} else {
				componentPath = Constants.PATH_FOR_OPTIONSLIST+"."+this.props.componentPath+"._embedded."+this.props.listType;
				this.props.actions.dispatchEditRESTData(Constants.PATH_FOR_OPTIONSLIST+"."+this.props.componentPath+"._embedded."+this.props.listType,[{[this.props.listAttr]: newValue}]);
			}
			this.props.actions.postRESTData(
				{
					method: 'post',
					url: API_URL+"/"+this.props.listType,
					data: {[this.props.listAttr]: newValue}
				},
				componentPath,
				'FieldSelectContainer.createItem',
				undefined,
				(data) => {
					dataItems.push({value: data.id, label: newValue, index: dataItems.length, object: data});
					this.setValue(onChange, dataItems);
				}
			);
		}
	}
	
	setValue(onChange, newValue, actionMeta) {
		if(onChange) {
			if(newValue instanceof Array) {
				onChange(newValue.map((item) => item.object));
			} else if(newValue) {
				onChange(newValue.object);
			} else {
				onChange(newValue);
			}
		}
	}
	
	render() {
		let combinedFilter = this.props.listFilter.slice();
		if(this.state.userFilter != "" && this.props.listLookupColumns instanceof Array) {
			let columnFilters;
			this.props.listLookupColumns.forEach((lookupColumn, index) => {
				if(index == 0) { //"like(upper("+entityDefinition.className+"."+curr.id+");upper(%"+curr.value+"%))"
					columnFilters = {op: "like",operands: [{op: "upper", operands: [lookupColumn]},{op: "upper", operands: [{literal: "%"+this.state.userFilter+"%"}]}]};
				} else {
					columnFilters = {op: "or", operands: [columnFilters,{op: "like",operands: [{op: "upper", operands: [lookupColumn]},{op: "upper", operands: [{literal: "%"+this.state.userFilter+"%"}]}]}]};
				}
			});
			combinedFilter.push({where: columnFilters});
		}
		//TODO better display when not editable
		if(this.props.editable == true) {
			//optimize to get only the needed columns
			let retrieveColumns = getExpandedColumns(this.props.listType, this.props.listExpandedColumns, undefined);	//filter out the columns not needed for display or required for expansion
			retrieveColumns = retrieveColumns.filter((column) => (this.props.listLookupColumns.length > 0 ? this.props.listLookupColumns.includes(column.accessor) : column.entityType == undefined));
			return <RetrieveDataContainer
					componentPath={Constants.PATH_FOR_OPTIONSLIST+"."+this.props.componentPath+".pageData"}
					columns={retrieveColumns}
					defaultPageSize={this.props.listMax}
					loading={this.props.itemsLoading}
					retrieveType={this.props.listType}
					defaultSort={undefined}
					defaultFilter={combinedFilter}
					expandColumns={this.props.listExpandedColumns}
				>
					<CommonFieldContainer
						componentPath={this.props.componentPath}
						editable={this.props.editable}
						loading={this.props.loading}
						onChange={this.props.onChange}
						renderF={(editable,data,onChange) => {
								let dataItems = data instanceof Array ? 
										data.map((item, index) => ({
											value: item.id,
											label: this.props.listDisplayFn(item),
											index: index,
											object: item,
										}))
										: {
											value: data.id,
											label: data.id ? this.props.listDisplayFn(data) : this.props.placeholder,
											index: 0,
											object: data,
										};
								let options = this.props.items instanceof Array ? 
										this.props.items.map((item, index) => ({
											value: item.id,
											label: this.props.listDisplayFn(item),
											index: index,
											object: item,
										}))
										: undefined;
								return <Select
										value={dataItems}
										//isMulti
										options={options}
										onCreateOption={(newStringOption) => {
											this.createItem(newStringOption,dataItems,onChange);
										}}
										onChange={(newValue, actionMeta) => {
											this.setValue(onChange, newValue, actionMeta);
										}}
										isValidNewOption={(inputValue, selectValue, selectOptions) => {
											if(this.props.creatable) {
												let attr = this.props.listAttr;
												return !(!inputValue || selectValue.some(function (option) {
													return inputValue.toLowerCase() === option.object[attr].toLowerCase();
												}) || selectOptions.some(function (option) {
													return inputValue.toLowerCase() === option.object[attr].toLowerCase();
												}));
											} else {
												return false;
											}
										}}
										isLoading={this.props.itemsLoading}
										isDisabled={!this.props.editable}
										isClearable={this.props.isClearable}
										noOptionsMessage={(inputValue) => (this.props.itemsError ? this.props.t("Error loading options") : this.props.t("NoOptions"))}
										placeholder={this.props.placeholder}
										loadingMessage={() => (this.props.t("Loading..."))}
										formatCreateLabel={(inputValue) => (this.props.t("Create")+" "+inputValue)}
										classNamePrefix="react-select"
										menuPortalTarget={document.body}
										onInputChange={(inputValue) => { 
											if(inputValue != '') {
												this.setState({userFilter: inputValue});
											}
											return inputValue;
										}}
										cache = {false}
									/>;
						}}
					/>
				</RetrieveDataContainer>;
		} else {
			let fieldText = "";
			if(!this.props.editable && this.props.data && this.props.listDisplayFn(this.props.data) !== null) {
				fieldText = this.props.listDisplayFn(this.props.data);
			}
			if(this.props.pageURL !== undefined && this.props.data && this.props.data.id !== undefined) {
				if(this.props.editable == undefined) {
					return <Link to={this.props.pageURL+"/"+this.props.data.id}>
								<div style={{display: 'inline-block'}}>{fieldText}</div>
							</Link>;
				} else {
					//override the 'disabled' pointer
					const style = {
							cursor: "pointer",
						};
					return <Link to={this.props.pageURL+"/"+this.props.data.id}>
								<input id={this.props.componentPath} className="form-control" type="text" style={style}
									value={fieldText}
									disabled={true}
								/>
							</Link>;
				}
			} else {
				if(this.props.editable == undefined) {
					return <div style={{display: 'inline-block'}}>{fieldText}</div>;
				} else {
					return <input id={this.props.componentPath} className="form-control" type="text"
								value={(this.props.data instanceof Error && this.props.data.response && this.props.data.response.status == 404) ? this.props.t("--none--") : fieldText}
								disabled={true}
							/>;
				}
			}
		}
		return undefined;
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	let items = undefined;
	let listDisplayFn = ownProps.listDisplayFn instanceof Function ? ownProps.listDisplayFn : ((item)=>item[ownProps.listAttr]);
	let listData = resolveObjectPath(Constants.PATH_FOR_OPTIONSLIST+"."+ownProps.componentPath+".pageData",state.rest); //rest because of fetchREST
	if(listData && listData._embedded) {
		//handle case with class hierarchy with single table strategy
		//put all subclasses in one array
		items = [];
		for(let key in listData._embedded) {
			if(key != "hashMaps" && key != "summaries") { //exclude report builder raw results
				items = items.concat(listData._embedded[key]);
			}
		}
		items = items.filter((elem) => {
				const displayText = listDisplayFn(elem);
				return (displayText != undefined && displayText != null && String(displayText).length > 0);
			});
	}
	let listLookupColumns = ownProps.listLookupColumns ? ownProps.listLookupColumns : [ownProps.listAttr];
	return {
		//storage
		data: data,
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		componentPath: ownProps.componentPath,
		href: ownProps.href,
		//data
		listData: listData,
		listType: ownProps.listType,
		listAttr: ownProps.listAttr,
		listDisplayFn: listDisplayFn,
		listExpandedColumns: ownProps.listExpandedColumns ? ownProps.listExpandedColumns : listLookupColumns,
		listLookupColumns: listLookupColumns,
		listFilter: ownProps.listFilter ? ownProps.listFilter : [{}],
		listMax: ownProps.listMax ? ownProps.listMax : 20,
		items: items,
		itemsLoading: listData ? listData[Constants.PATH_FOR_LOADING] : undefined,
		itemsError: listData instanceof Error ? listData : undefined,
		//UI
		editable: ownProps.editable,
		creatable: ownProps.creatable !== undefined ? ownProps.creatable : true,
		onChange: ownProps.onChange,
		pageURL: ownProps.pageURL,
		isClearable: ownProps.isClearable,
		placeholder: ownProps.placeholder ? ownProps.placeholder : ownProps.t("Choose..."),
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData, postRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldSelectContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
