import React from 'react';
import CreatableSelect from 'react-select/lib/Creatable';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import { builderDataToProjection, getEntityDefinition } from '../nomenclatures/entityDefinitions';
import { fetchRESTFollow, dispatchEditRESTData, postRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

import CommonFieldContainer from './CommonFieldContainer';

//redux container class
class FieldMultiSelectOrEditContainer extends React.Component {

	retrieveData() {
		if(this.props.listType && this.props.editable 
				&& !this.props.items && !this.props.itemsLoading && !this.props.itemsError) {
			const def = getEntityDefinition(this.props.listType);
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/builder/1",
					params: {
						...this.props.listFilter,
						from: def.className,
						select: def.className,
						size: 1000,
					}
				},
				"multiselectLists."+this.props.listType,
				(response) => {
					let newData = response.data;
					//transform data to "repository response"-like
					builderDataToProjection(newData,this.props.listType);
					return newData;
				},
				'FieldMultiSelectOrEditContainer.retrieveData',
			);
		}
		if(this.props.href && this.props.componentPath && this.props.listType && !this.props.data) {
			this.props.actions.fetchRESTFollow(
				{
					url: this.props.href,
					params: {
						size: 1000,
					}
				},
				this.props.componentPath,
				(response) => {
					let newData = response.data;
					//transform data to "repository response"-like
					//builderDataToProjection(newData,this.props.listType);
					return newData._embedded[this.props.listType];
				},
				'FieldMultiSelectOrEditContainer.retrieveData',
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
		//console.log('createItem',newValue);
		//add new options to the list
		if(newValue) {
			let componentPath = "";
			if(this.props.items instanceof Array && this.props.items.length > 0) {
				if(!this.props.items.some((item) => item[this.props.listAttr] == newValue)) { //if it is new item, not in the list
					componentPath = "multiselectLists."+this.props.listType+"._embedded."+this.props.listType+"."+this.props.items.length;
					//this.props.actions.dispatchEditRESTData("multiselectLists."+this.props.listType+"._embedded."+this.props.listType+"."+this.props.items.length,{[this.props.listAttr]: newValue});
				}
			} else {
				componentPath = "multiselectLists."+this.props.listType+"._embedded."+this.props.listType;
				this.props.actions.dispatchEditRESTData("multiselectLists."+this.props.listType+"._embedded."+this.props.listType,[{[this.props.listAttr]: newValue}]);
			}
			this.props.actions.postRESTData(
				{
					method: 'post',
					url: API_URL+"/"+this.props.listType,
					data: {[this.props.listAttr]: newValue}
				},
				componentPath,
				'FieldMultiSelectOrEditContainer.createItem',
				undefined,
				(data) => {
					//console.log('after_post',newValue,data,dataItems);
					dataItems.push({value: data.id, label: newValue, index: dataItems.length, object: data});
					this.setValue(onChange, dataItems);
				}
			);
		}
	}
	
	setValue(onChange, newValue, actionMeta) {
		//console.log('onChange',newValue,actionMeta);
		if(onChange) {
			if(newValue instanceof Array) {
				onChange(newValue.map((item) => item.object));
			} else {
				onChange(newValue ? [{[this.props.listAttr]: newValue}] : undefined);
			}
		}
	}
	
	render() {
		//TODO better display when not editable
		return <CommonFieldContainer
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
						let dataItems = data instanceof Array ? 
								data.map((item, index) => ({
									value: item.id,
									label: item[this.props.listAttr],
									index: index,
									object: item,
								}))
								: [];
						let options = this.props.items instanceof Array ? 
								this.props.items.map((item, index) => ({
									value: item.id,
									label: item[this.props.listAttr],
									index: index,
									object: item,
								}))
								: undefined;
						//console.log('render',dataItems,options);
						return <CreatableSelect
								value={dataItems}
								isMulti
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
								noOptionsMessage={(inputValue) => (this.props.itemsError ? this.props.t("Error loading options") : this.props.t("NoOptions"))}
								isLoading={this.props.itemsLoading}
								isDisabled={!this.props.editable}
								placeholder={this.props.t("Choose or type...")}
								loadingMessage={() => (this.props.t("Loading..."))}
								formatCreateLabel={(inputValue) => (this.props.t("Create")+" "+inputValue)}
							/>;
				}}
			/>;
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	let items = undefined;
	if(state.rest.multiselectLists && state.rest.multiselectLists[ownProps.listType] && state.rest.multiselectLists[ownProps.listType]._embedded) {
		//handle case with class hierarchy with single table strategy
		//put all subclasses in one array
		items = [];
		for(let key in state.rest.multiselectLists[ownProps.listType]._embedded) {
			if(key != "hashMaps" && key != "summaries") { //exclude report builder raw results
				items = items.concat(state.rest.multiselectLists[ownProps.listType]._embedded[key]);
			}
		}
		items = items.filter((elem) => (elem[ownProps.listAttr] != undefined && elem[ownProps.listAttr] != null && elem[ownProps.listAttr].length > 0));
	}
	return {
		//storage
		data: data,
		componentPath: ownProps.componentPath,
		href: ownProps.href,
		//data
		listType: ownProps.listType,
		listAttr: ownProps.listAttr,
		listFilter: ownProps.listFilter ? ownProps.listFilter : {},
		items: items,
		itemsLoading: state.rest.multiselectLists && state.rest.multiselectLists[ownProps.listType] ? state.rest.multiselectLists[ownProps.listType][Constants.PATH_FOR_LOADING] : undefined,
		itemsError: state.rest.multiselectLists && state.rest.multiselectLists[ownProps.listType] instanceof Error ? state.rest.multiselectLists[ownProps.listType] : undefined,
		//UI
		editable: ownProps.editable,
		creatable: ownProps.creatable !== undefined ? ownProps.creatable : true,
		onChange: ownProps.onChange
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData, postRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldMultiSelectOrEditContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
