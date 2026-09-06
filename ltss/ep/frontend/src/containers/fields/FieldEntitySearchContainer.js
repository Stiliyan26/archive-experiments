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
import { resolveObjectPath, resolveObjectParentPath } from './../../scripts/dataUtils';
import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'

//redux container class
class FieldEntitySearchContainer extends React.Component {

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
				'FieldEntitySearchContainer.retrieveData',
				{},
				fetchPromiseWrapper
			);
			fetchPromiseWrapper.promise.then((mergeObj) => {
				let response = mergeObj[0].value;
				this.props.onChange(response && response._links ? response._links.self.href : undefined);
			});
		}
		//TODO Fix paging (not all items may be retrieved) like in FieldTask
		if(this.props.entityType && this.props.editable && !this.props.items && !this.props.itemsLoading && !this.props.itemsError) {
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/"+this.props.entityType,
					params: { size: 1000 },
				},
				this.props.entityType,
				response => (response.data),
				'FieldEntitySearchContainer.retrieveData',
			);
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
						let options = this.props.items instanceof Array ? 
								this.props.items.map((item, index) => ({
									value: item._links.self.href,
									label: item[this.props.displayAttr],
									index: index
								}))
								: undefined;
						let selectedItem = undefined;
						if(data) {
							if(data._links && data._links.self) {
								selectedItem = {
										value: data._links.self.href,
										label: data[this.props.displayAttr]
									};
							} else {//if there is no link, find it by ID
								if(data.id !== undefined && this.props.items instanceof Array) {
									selectedItem = {
											value: this.props.items[this.props.items.findIndex((item) => item.id == data.id)]._links.self.href,
											label: this.props.items[this.props.items.findIndex((item) => item.id == data.id)][this.props.displayAttr]
										};
								}
							}
						}
						//fix problem with {?projection} postfix
						if(selectedItem && selectedItem.value) {
							selectedItem.value = selectedItem.value.replace(/{.+}/,"");
						}
						return <Select
								value={selectedItem}
								options={options}
								onChange={(e) => {
									this.props.actions.dispatchEditRESTData(this.props.componentPath,this.props.items[e.index]);
									onChange(e.value);
								}}
								placeholder={this.props.t("Choose...")}
								isLoading={this.props.itemsLoading instanceof Promise}
								menuPortalTarget={document.body}
							/>
					} else {
						let fieldText = data[this.props.displayAttr];
						if(editable == undefined) {
							if(this.props.pageURL !== undefined && data.id !== undefined) {
								return <Link to={this.props.pageURL+"/"+data.id}>{fieldText}</Link>;
							} else {
								return <div style={{display: 'inline-block'}}>{fieldText}</div>;
							}
						} else {
							return <input id={this.props.componentPath} className="form-control" type="text" 
										value={fieldText} 
										disabled={true}
									/>;
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
	let entityDef = undefined;
	if(ownProps.entityType != undefined) {
		entityDef = getEntityDefinition(ownProps.entityType);
	}
	let itemsComponentPath = ownProps.entityType;
	let itemsData = resolveObjectPath(itemsComponentPath,state.rest); //rest because of fetchREST
	let items = undefined;
	if(itemsData && itemsData._embedded) {
		//handle case with class hierarchy with single table strategy
		//put all subclasses in one array
		items = [];
		for(let key in itemsData._embedded) {
			if(key != "hashMaps") { //exclude report builder raw results
				items = items.concat(itemsData._embedded[key]);
			}
		}
	}
	return {
		auth: state.auth,
		//storage
		data: data,
		items: items,
		//retrieval
		error: data instanceof Error ? data : undefined,
		itemsError: itemsData instanceof Error ? itemsData : undefined,
		loading: ownProps.loading ? ownProps.loading : (values.parentValue ? (data ? data[Constants.PATH_FOR_LOADING] : undefined) : true), //if parent data is not there, probably it is loading
		itemsLoading: itemsData ? itemsData[Constants.PATH_FOR_LOADING] : undefined,
		componentPath: ownProps.componentPath,
		entityType: ownProps.entityType,
		//data
		entityDef: entityDef,
		href: ownProps.href || (data && data._links && data._links.self ? data._links.self.href : undefined),
		//UI
		displayAttr: ownProps.displayAttr ? ownProps.displayAttr : (entityDef ? entityDef.displayAttr : 'name'),
		editable: ownProps.editable,
		onChange: ownProps.onChange ? ownProps.onChange : (x) => (x),
		pageURL: ownProps.pageURL != undefined ? ownProps.pageURL : entityDef.pageURL,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldEntitySearchContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
