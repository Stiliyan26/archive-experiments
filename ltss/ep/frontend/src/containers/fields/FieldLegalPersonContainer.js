import React from 'react';
import { Async } from 'react-select'
import { Link } from 'react-router-dom'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import CommonFieldContainer from './CommonFieldContainer';

import * as Constants from './../../static/constants';
import { fetchRESTFollow, dispatchEditRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

//redux container class
class FieldLegalPersonContainer extends React.Component {

	retrieveData(filter) {
		const retrieve_href = this.props.data && this.props.data._retrieveHref ? this.props.data._retrieveHref : this.props.href;
		if(this.props.href && this.props.componentPath 
				&& (!this.props.data || this.props.href != retrieve_href)) {
			this.props.actions.fetchRESTFollow(
				{
					url: this.props.href,
				},
				this.props.componentPath,
				response => ({
					...response.data,
					_retrieveHref: this.props.href
				}),
				'FieldLegalPersonContainer.retrieveData',
			);
		}
	}
	

	retrieveOptions(input,callback) {
		//TODO optimize with caching?
		let fetchPromiseWrapper = {};
		this.props.actions.fetchRESTFollow({
				url: API_URL+"/legalPersons",
				params: { name: input }
			},
			this.props.componentPath+'._search',
			response => ({
				_items: response.data._embedded.legalPersons,
				_filter: input
			}),
			'FieldLegalPersonContainer.retrieveOptions',
			{},
			fetchPromiseWrapper
		);
		fetchPromiseWrapper.promise
		.then((mergeObj) => {
			let response = mergeObj[0].value;
			let options = response._items instanceof Array ? 
					response._items.map((item, index) => ({
						item: item,
						value: item._links.self.href,
						label: item.name,
						index: index
					}))
					: [];
			callback(null,{ options: options });
		});
	}

	componentDidMount(){
		this.retrieveData();
	}
	
	componentDidUpdate(prevProps, prevState) {
		this.retrieveData(this.props._filter);
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
						let selectedValue = undefined;
						if(data) {
							if(data._links && data._links.self) { 
								selectedValue = data._links.self.href
							} else if(this.props._items instanceof Array) { //if there is no link, find it by ID
								const selectedIndex = this.props._items.findIndex((item) => item.id == this.props.id);
								if(selectedIndex >= 0) {
									selectedValue = this.props._items[selectedIndex]._links.self.href;
								}
							}
						};
						//fix problem with {?projection} postfix
						if(selectedValue) {
							selectedValue = selectedValue.replace(/{.+}/,"");
						}
						return <div className="col-sm-12 form-group">
								<div className="col-sm-2"><input className="form-control" type="number" title={this.props.t("CommonRecord.id")}
									value={this.props.id} 
									onChange={(e) => {
										this.props.actions.dispatchEditRESTData(this.props.componentPath,undefined);
										this.props.onChangeF(API_URL+"/legalPersons/"+e.target.value);
									}}
								/></div>
								<div className="col-sm-10">
									<Async
										value={selectedValue}
										onChange={(e) => {
											this.props.actions.dispatchEditRESTData(this.props.componentPath,e ? ({...e.item, _search: data._search}) : undefined);
											this.props.onChangeF(e ? e.value : undefined);
										}}
										filterOptions = {(options, filter, currentValues) => {
											return options;
										}}
										loadOptions = {(input,callback) => this.retrieveOptions(input,callback)}
										cache = {false}
										placeholder={this.props.t("Choose...")}
									/>
								</div>
							</div>;
					} else {
						return <Link to={'/legalPersons/'+this.props.id}>{this.props.name}</Link>;
					}
					return undefined;
				}}
			/>;
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	return {
		auth: state.auth,
		//storage
		data: data,
		componentPath: ownProps.componentPath,
		//data
		href: ownProps.href || (data && data._links && data._links.self ? data._links.self.href : undefined),
		name: data ? data.name : undefined,
		id: data ? data.id : undefined,
		_items: data && data._search ? (data._search[Constants.PATH_FOR_LOADING] instanceof Promise ? data._search : data._search._items) : undefined,
		//UI
		editable: ownProps.editable,
		_filter: data && data._search && data._search._filter ? data._search._filter : (data ? {id: data.id} : undefined),
		onChangeF: ownProps.onChange
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldLegalPersonContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
