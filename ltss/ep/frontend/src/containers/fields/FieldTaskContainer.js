import React from 'react';
import Select from 'react-select'
import { Link } from 'react-router-dom'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import { fetchRESTFollow, dispatchEditRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

import CommonFieldContainer from './CommonFieldContainer';

class FieldTaskContainer extends React.Component {

	retrieveData(filter) {
		const curr_href = this.props.data && this.props.data._retrieveHref ? this.props.data._retrieveHref : this.props.href;
		const self_href = this.props.data && this.props.data._links && this.props.data._links.self ? this.props.data._links.self.href : this.props.href;
		if(this.props.href && this.props.componentPath && !this.props.loading
				&& (!this.props.data 
					|| this.props.href.replace(/{.+}/,"") != curr_href.replace(/{.+}/,"")
					&& this.props.href.replace(/{.+}/,"") != self_href.replace(/{.+}/,"")
				)
			) {
			let fetchPromiseWrapper = {};
			this.props.actions.fetchRESTFollow(
				{
					url: this.props.href,
				},
				this.props.componentPath,
				response => ({
					...response.data,
					_retrieveHref: this.props.href
				}),
				'FieldTaskContainer.retrieveData',
				{},
				fetchPromiseWrapper
			);
			fetchPromiseWrapper.promise.then((mergeObj) => {
				let response = mergeObj[0].value;
				if(response && response._links && response._links.self && response._links.self.href) this.props.onChangeF(response._links.self && response._links.self.href);
			});
		} else {
			if(this.props.componentPath && this.props.editable && (!this.props._items || this.props._filter != filter)) {
				this.props.actions.fetchRESTFollow(
					{
						url: API_URL+"/tasks",
						params: { ...filter }
					},
					this.props.componentPath+'._search',
					response => ({
						_items: response.data._embedded.tasks,
						_filter: filter
					}),
					'FieldTaskContainer.retrieveData',
				);
			}
		}
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
						let options = this.props._items instanceof Array ? 
								this.props._items.map((item, index) => ({
									value: item._links.self.href,
									label: item.title,
									index: index
								}))
								: undefined
						return <div className="col-sm-12 form-group">
								<div className="col-sm-2">
									<input className="form-control" type="number" title={this.props.t("CommonRecord.id")}
										value={this.props.id} 
										onChange={(e) => {
											this.props.actions.dispatchEditRESTData(this.props.componentPath,undefined);
											this.props.onChangeF(API_URL+"/tasks/"+e.target.value);
										}}
									/>
								</div>
								<div className="col-sm-10">
									<Select
										value={this.props.href}
										options={options}
										onChange={(e) => {
											this.props.actions.dispatchEditRESTData(this.props.componentPath,e ? ({...this.props._items[e.index], _search: data._search}) : undefined);
											this.props.onChangeF(e.value);
										}}
										onInputChange={(inputValue) => { 
											if(inputValue != '') {
												this.retrieveData( {title: inputValue} );
											}
											return inputValue;
										}}
										placeholder={this.props.t("Choose...")}
									/>
								</div>
							</div>;
					} else {
						return <Link to={'/tasks/'+this.props.id}>{this.props.title}</Link>;
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
		loading: ownProps.loading || (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		href: ownProps.href || (data && data._links && data._links.self ? data._links.self.href.replace(/{.+}/,"") : undefined),
		title: data ? data.title : undefined,
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldTaskContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
