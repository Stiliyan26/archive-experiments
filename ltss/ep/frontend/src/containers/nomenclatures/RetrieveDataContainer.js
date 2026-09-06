import React from 'react';
import querystring from 'querystring'
import lodashIsEqual from 'lodash/isEqual'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import * as Constants from './../../static/constants';
import { getExpandedColumns, tableFilterToParam, builderDataToProjection, builderURLFromColumns, getEntityDefinition } from './entityDefinitions.js'
import { fetchRESTFollow, resetRESTCallLimit, dispatchCleanRESTData, dispatchEditRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

//Embed: this is a middle-sized component must be placed inside another component and get its properties from it
//Container: redux container class
class RetrieveDataContainer extends React.Component {

	retrieveData(pageIndex, pageSize, sort, filter, isUpdate) {
		//console.log("RetrieveDataContainer.retrieveData", pageIndex, pageSize, sort, filter, (this.props._retrieveTimeStamp));
		//we should have data about:
		// - what is the current data: this.props.error, this.props.items, this.props.retrievedPageIndex, this.props.retrievedPageSize, this.props.retrievedSort, this.props.retrievedFilter
		// - what is the new required data: pageIndex, pageSize, sort, filter
		// - is it in process of loading: this.props.loading
		let fetch_url = builderURLFromColumns(
				(this.props.retrieveType ? API_URL+"/"+this.props.retrieveType : undefined),
				this.props.retrieveType,
				this.props.columns,
				this.props.excludeRootEntity);
		const retrieve_href = this.props.data && this.props.data._retrieveHref ? this.props.data._retrieveHref : fetch_url; //get the stored retrieval URL, if no such, then get the same as new one in order disable the check below
		const parentAttr = this.props.data && this.props.data._parentAttr ? this.props.data._parentAttr : this.props.parentAttr;
		const parentHref = this.props.data && this.props.data._parentHref ? this.props.data._parentHref : this.props.parentHref;
		if(this.props && !this.props.error && !this.props.loading && fetch_url !== undefined && this.props.componentPath !== undefined
				&& (this.props.items === undefined
						|| fetch_url != retrieve_href
						|| pageIndex != this.props.retrievedPageIndex
						|| pageSize != this.props.retrievedPageSize
						|| ! lodashIsEqual(sort, this.props.retrievedSort)
						|| ! lodashIsEqual(filter, this.props.retrievedFilter)
						|| parentAttr != this.props.parentAttr
						|| parentHref != this.props.parentHref
						|| !isUpdate && ((new Date()) - this.props._retrieveTimeStamp) > this.props.dataRefreshInterval
					)) {
			if(this.props.items === undefined) console.log('RetrieveDataContainer retrieve: No items!');
			if(pageIndex!=this.props.retrievedPageIndex) console.log('RetrieveDataContainer retrieve: Diff pageIndex!',pageIndex,this.props.retrievedPageIndex);
			if(pageSize!=this.props.retrievedPageSize) console.log('RetrieveDataContainer retrieve: Diff pageSize!',pageSize,this.props.retrievedPageSize);
			if(! lodashIsEqual(sort, this.props.retrievedSort)) console.log('RetrieveDataContainer retrieve: Diff sort!');
			if(! lodashIsEqual(filter, this.props.retrievedFilter)) console.log('RetrieveDataContainer retrieve: Diff filter!',filter, this.props.retrievedFilter);
			if(parentAttr != this.props.parentAttr) console.log('RetrieveDataContainer retrieve: Diff parentAttr!');
			if(parentHref != this.props.parentHref) console.log('RetrieveDataContainer retrieve: Diff parentHref!');
			if( !isUpdate && ((new Date()) - this.props._retrieveTimeStamp) > this.props.dataRefreshInterval ) console.log('RetrieveDataContainer retrieve: Data too old!', this.props._retrieveTimeStamp);
			//TODO sort like filter - to use the displayAttr for entities
			let filter_param = tableFilterToParam(filter, this.props.columns, this.props.retrieveType);
			let entityDefinition = getEntityDefinition(this.props.retrieveType);
			if(this.props.parentAttr !== undefined && this.props.parentHref !== undefined) {
				filter_param[(entityDefinition.className ? entityDefinition.className + "." : "")+this.props.parentAttr+".id"] = this.props.parentHref.replace(/.+\/(\d+)/,"$1");
			} else if(this.props.parentAttr !== undefined && this.props.parentData !== undefined && this.props.parentData.id !== undefined) {
				filter_param[(entityDefinition.className ? entityDefinition.className + "." : "")+this.props.parentAttr+".id"] = this.props.parentData.id;
			}
			if(fetch_url != retrieve_href) {
				console.log('RetrieveDataContainer retrieve: Diff query (URL/params)!',fetch_url,retrieve_href);
				//clear stored data because this is a different query
				this.props.actions.dispatchEditRESTData(this.props.componentPath+'.selectedRows.map',undefined);
			}
			let fetchPromiseWrapper = {};
			this.props.actions.fetchRESTFollow(
				{
					url: fetch_url,
					params: {
						...filter_param,
						page: pageIndex,
						size: pageSize,
						sort: entityDefinition.className+"."+sort[0].id+','+(sort[0].desc?'desc':'asc') //TODO implement multi-sort
					},
					paramsSerializer: function(params) {
						//needed for the from-to dates
						return querystring.stringify(params)
					}
				},
				this.props.componentPath,
				(response) => {
					let newData = response.data;
					//transform data to "repository response"-like
					builderDataToProjection(newData,this.props.retrieveType);
					newData._retrieveHref = fetch_url;
					newData._parentAttr = this.props.parentAttr;
					newData._parentHref = this.props.parentHref;
					newData._sort = sort;
					newData._filter = filter;
					newData._retrieveTimeStamp = new Date();
					if(newData && newData._embedded && newData._embedded[this.props.retrieveType] instanceof Array) {
						newData._embedded[this.props.retrieveType] = newData._embedded[this.props.retrieveType].map((item) => {
								let newItem = item;
								newItem._editable = false;
								if(this.props.parentAttr !== undefined && this.props.parentData !== undefined) {
									newItem[this.props.parentAttr] = Object.assign({},this.props.parentData);
								}
								return newItem;
							});
						if(this.props.data && this.props.data._entitiesToAdd) {
							newData._embedded[this.props.retrieveType] = newData._embedded[this.props.retrieveType].concat(this.props.data._entitiesToAdd);
							this.props.actions.dispatchEditRESTData(this.props.componentPath+'._entitiesToAdd',undefined);
						}
					}
					return newData;
				},
				'RetrieveDataContainer.retrieveData',
				{},
				fetchPromiseWrapper
			);
			fetchPromiseWrapper.promise.then((mergeObj) => {
					if(mergeObj instanceof Array && mergeObj[0]) {
						this.props.onAfterRetrieve(mergeObj[0].value);
					} else {
						this.props.onAfterRetrieve(undefined);
					}
				});
		}
	}

	refreshData() {
		//force table refresh
		this.props.actions.dispatchEditRESTData(this.props.componentPath,undefined);
	}

	componentDidMount(){
		//console.log("RetrieveDataContainer.componentDidMount");
		this.retrieveData(this.props.defaultPageIndex,this.props.defaultPageSize,this.props.defaultSort,this.props.defaultFilter,false);
	}

	componentDidUpdate(prevProps, prevState) {
		this.retrieveData(this.props.defaultPageIndex,this.props.defaultPageSize,this.props.defaultSort,this.props.defaultFilter,true);
	}

	render() {
		return (this.props.children ? this.props.children : null);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	const data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
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
	const retrievedPageIndex = page ? page.number : 0;
	const retrievedPageSize = page ? page.size : 5;
	const retrievedSort = (data && data._sort) ? data._sort : [{id: "id", desc: true}];
	const retrievedFilter = (data && data._filter) ? data._filter : [{}];
	const defaultPageIndex = ownProps.defaultPageIndex != undefined ? ownProps.defaultPageIndex : retrievedPageIndex;
	const defaultPageSize = ownProps.defaultPageSize != undefined ? ownProps.defaultPageSize : retrievedPageSize;
	const defaultSort = ownProps.defaultSort ? ownProps.defaultSort : retrievedSort;
	const defaultFilter = ownProps.defaultFilter ? ownProps.defaultFilter : retrievedFilter;
	const columns = ownProps.columns ? ownProps.columns : getExpandedColumns(ownProps.retrieveType, ownProps.expandColumns, undefined);
	return {
		//data
		auth: state.auth,
		data: data, //retrieved data
		items: items, //items array from the retrieved data
		//retrieval
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		_retrieveTimeStamp: (data && data._retrieveTimeStamp ? data._retrieveTimeStamp : undefined),
		dataRefreshInterval: ownProps.dataRefreshInterval ? ownProps.dataRefreshInterval : 2500, //if timeout is too small, there may be a loop
		error: data instanceof Error ? data : undefined,
		componentPath: ownProps.componentPath, //redux state path to store the data
		retrieveType: ownProps.retrieveType, //entity name for retrieval
		columns: columns,
		parentHref: ownProps.parentHref, //href of parent for filter and save purpose
		parentAttr: ownProps.parentAttr, //the attribute name that holds the parent href
		parentData: ownProps.parentData, //already available data of the parent
		onAfterRetrieve: ownProps.onAfterRetrieve ? ownProps.onAfterRetrieve : (() => {}),
		//paging, filtering, sorting
		defaultPageIndex: defaultPageIndex,
		defaultPageSize: defaultPageSize,
		defaultSort: defaultSort,
		defaultFilter: defaultFilter,
		pages: pages,
		retrievedPageIndex: retrievedPageIndex,
		retrievedPageSize: retrievedPageSize,
		retrievedSort: retrievedSort,
		retrievedFilter: retrievedFilter,
		//UI
		expandColumns: ownProps.expandColumns,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
export default connect(mapStateToProps, mapDispatchToProps, null, { withRef: true })(RetrieveDataContainer);
