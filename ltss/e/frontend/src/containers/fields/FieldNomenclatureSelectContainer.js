import React from 'react';
import { Link } from 'react-router-dom'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import Select from 'react-select'
import CommonFieldContainer from './CommonFieldContainer';

import * as Constants from './../../static/constants';
import { fetchRESTFollow, dispatchEditRESTData } from './../../actions/taskActions';
import { resolveObjectParentPath } from './../../scripts/dataUtils';

//redux container class
class FieldNomenclatureSelectContainer extends React.Component {

	retrieveData() {
		const retrieve_href = this.props.data && this.props.data._retrieveHref ? this.props.data._retrieveHref : this.props.href;
		if(this.props.href && this.props.componentPath //we have the needed parameters
				&& !this.props.loading && !this.props.error
				&& (this.props.data === undefined || this.props.href != retrieve_href)) {
			//if(this.props.data === undefined) console.log('nomenclature retrieve no data',this.props.componentPath);
			//if(this.props.href != retrieve_href) console.log('nomenclature retrieve diff href',this.props.href, retrieve_href);
			let fetchPromiseWrapper = {};
			//TODO check if this should be done everywhere
			let fetch_href = this.props.href;
			this.props.actions.fetchRESTFollow(
				{
					url: fetch_href,
				},
				this.props.componentPath,
				response => ({
					...response.data,
					_retrieveHref: fetch_href
				}), //here we shouldn't have this.props.href, because props may change till the REST response comes
				'FieldNomenclatureSelectContainer.retrieveData',
				{},
				fetchPromiseWrapper
			);
			if(fetchPromiseWrapper.promise) { //happens when call limit is hit on multi-row update
				fetchPromiseWrapper.promise.then((mergeObj) => {
					let response = mergeObj && mergeObj[0] ? mergeObj[0].value : undefined;
					this.props.onChange(response && response._links ? response._links.self.href : undefined,"retrieveData");
				});
			}
		}
		//retrieve all pages of nomenclature
		if(this.props.nomenclatureKey && this.props.editable && !this.props.items && !this.props.itemsLoading && !this.props.itemsError) {
			//console.error("fetchAll call",this.props.nomenclatureKey,this.props.editable,this.props.items,this.props.itemsLoading,this.props.itemsError);
			this.fetchAll(0, []);
		} else {
			//console.error("fetchAll skip",this.props.nomenclatureKey,this.props.editable,this.props.items,this.props.itemsLoading,this.props.itemsError);
		}
	}
	
	fetchAll(startPage, accumulatedData) {
		let fetchPromiseWrapper = {};
		this.props.actions.fetchRESTFollow(
			{
				url: API_URL+"/"+this.props.nomenclatureKey,
				params: { size: 1000, page: startPage },
			},
			"nomenclaturesCurrentPage."+this.props.nomenclatureKey,
			response => {
				//console.error("fetchAll response",response.data);
				if(response.data._embedded && response.data._embedded[this.props.nomenclatureKey]) {
					let newData = accumulatedData.concat(response.data._embedded[this.props.nomenclatureKey]);
					this.props.actions.dispatchEditRESTData("nomenclatures."+this.props.nomenclatureKey,newData);
				}
				return response.data;
			},
			'FieldNomenclatureSelectContainer.fetchAll',
			{},
			fetchPromiseWrapper
		);
		if(fetchPromiseWrapper.promise) { //happens when call limit is hit on multi-row update
			fetchPromiseWrapper.promise.then((response) => {
				let totalPages = response && response[0] && response[0].value && response[0].value.page ? response[0].value.page.totalPages : 0;
				let newData = accumulatedData.concat(response[0].value._embedded[this.props.nomenclatureKey]);
				if(totalPages > startPage+1) {
					//console.error("fetchNext",totalPages,newData);
					this.fetchAll(startPage+1, newData);
				}
			});
		}
	}

	componentDidMount(){
		this.retrieveData();
	}

	componentDidUpdate(prevProps, prevState) {
		this.retrieveData();
	}

	render() {
		return <CommonFieldContainer
				auth={this.props.auth}
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
					if(editable == true) {
//						if(this.props.items == undefined || this.props.items.loading instanceof Promise) {
//							return <FontAwesomeIcon icon="spinner" spin/>;
//						}
						let options = this.props.items instanceof Array ?
								this.props.items.map((item, index) => ({
									value: item._links.self.href,
									label: this.props.displayFn(item),
									index: index
								}))
								: undefined;
						let selectedItem = undefined;
						if(data) {
							if(data._links && data._links.self) {
								selectedItem = {
										value: data._links.self.href,
										label: this.props.displayFn(data)
									};
							} else {//if there is no link, find it by ID
								if(data.id !== undefined && this.props.items instanceof Array) {
									selectedItem = {
											value: this.props.items[this.props.items.findIndex((item) => item.id == data.id)]._links.self.href,
											label: this.props.displayFn(this.props.items[this.props.items.findIndex((item) => item.id == data.id)])
										};
								} else {
									if(data !== undefined && this.props.items instanceof Array) {
										let findItemIndex = this.props.items.findIndex((item) => (item[this.props.displayAttr] == data || item._links.self.href == data));
										if(findItemIndex >= 0) {
											selectedItem = {
													value: this.props.items[findItemIndex]._links.self.href,
													label: this.props.displayFn(this.props.items[findItemIndex])
												};
											this.props.actions.dispatchEditRESTData(this.props.componentPath,this.props.items[findItemIndex]);
											this.props.onChange(selectedItem.value,"render");
										}
									}
								}
							}
						}
						//fix problem with {?projection} postfix
						if(selectedItem && selectedItem.value) {
							selectedItem.value = selectedItem.value.replace(/{.+}/,"");
						}
						return <Select
								id={this.props.id}
								value={selectedItem}
								options={options}
								onChange={(e) => {
									if(e == undefined) {
										this.props.actions.dispatchEditRESTData(this.props.componentPath,null);
										this.props.onChange(null,"select");
									} else {
										this.props.actions.dispatchEditRESTData(this.props.componentPath,this.props.items[e.index]);
										this.props.onChange(e.value,"select");
									}
								}}
								placeholder={this.props.t("Choose...")}
								loadingMessage={() => (this.props.t("Loading..."))}
								noOptionsMessage={(inputValue) => (this.props.itemsError ? this.props.t("Error loading options") : this.props.t("NoOptions"))}
								isLoading={this.props.itemsLoading}
								isClearable={this.props.isClearable}
								classNamePrefix="react-select"
								menuPortalTarget={document.body}
							/>
					} else {
						let fieldText = "";
						if(!editable && this.props.displayFn(data) != null) {
							fieldText = this.props.displayFn(data);
						}
						if(this.props.pageURL !== undefined && this.props.data && this.props.data.id !== undefined) {
							if(editable == undefined) {
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
							if(editable == undefined) {
								return <div style={{display: 'inline-block'}}>{fieldText}</div>;
							} else {
								return <input id={this.props.componentPath} className="form-control" type="text"
											value={fieldText}
											disabled={true}
										/>;
							}
						}
					}
					return undefined;
				}}
			/>;
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let values = resolveObjectParentPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	let data = values.value;
	let items = undefined;
	if(state.rest.nomenclatures && state.rest.nomenclatures[ownProps.nomenclatureKey]) {
		//handle case with class hierarchy with single table strategy
		//put all subclasses in one array
		items = [];
		for(let key in state.rest.nomenclatures[ownProps.nomenclatureKey]) {
			if(key != "hashMaps") { //exclude report builder raw results
				items = items.concat(state.rest.nomenclatures[ownProps.nomenclatureKey][key]);
			}
		}
	}
	if( items != undefined && ownProps.filter instanceof Function ) {
		items = items.filter(ownProps.filter);
	}
	let itemsLoading = state.rest.nomenclaturesCurrentPage && state.rest.nomenclaturesCurrentPage[ownProps.nomenclatureKey] ? state.rest.nomenclaturesCurrentPage[ownProps.nomenclatureKey][Constants.PATH_FOR_LOADING] : undefined;
	return {
		auth: state.auth,
		//storage
		data: data,
		loading: ownProps.loading ? ownProps.loading : (values.parentValue ? (data ? data[Constants.PATH_FOR_LOADING] : undefined) : true), //if parent data is not there, probably it is loading
		error: ownProps.error ? ownProps.error : (data instanceof Error ? data : undefined),
		componentPath: ownProps.componentPath,
		nomenclatureKey: ownProps.nomenclatureKey,
		items: items,
		itemsLoading: itemsLoading,
		itemsError: state.rest.nomenclaturesCurrentPage && state.rest.nomenclaturesCurrentPage[ownProps.nomenclatureKey] instanceof Error ? state.rest.nomenclaturesCurrentPage[ownProps.nomenclatureKey] : undefined,
		filter: ownProps.filter,
		//data
		href: ownProps.href || (data && data._links && data._links.self ? data._links.self.href : undefined),
		//UI
		displayAttr: ownProps.displayAttr ? ownProps.displayAttr : 'name',
		displayFn: ownProps.displayFn instanceof Function ? ownProps.displayFn : ((item)=>{return item[ownProps.displayAttr ? ownProps.displayAttr : 'name'];}),
		editable: ownProps.editable,
		isClearable: ownProps.isClearable,
		onChange: ownProps.onChange ? ownProps.onChange : (x) => (x),
		pageURL: ownProps.pageURL,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldNomenclatureSelectContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
