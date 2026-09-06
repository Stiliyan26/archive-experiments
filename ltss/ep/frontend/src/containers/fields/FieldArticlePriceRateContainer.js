import React from 'react';
import url from 'url'
import querystring from 'querystring'
import moment from 'moment'
moment.locale('bg')

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import { fetchRESTFollow, dispatchEditRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';
import { builderDataToProjection } from '../nomenclatures/entityDefinitions.js'

import CommonFieldContainer from './CommonFieldContainer';

//redux container class
class FieldArticlePriceRateContainer extends React.Component {

	retrieveData() {
		const retrieve_id = this.props.data && this.props.data._retrieveArticleId ? this.props.data._retrieveArticleId : this.props.articleId;
		if(this.props.articleId && this.props.componentPath && !this.props.loading
				&& (!this.props.data || retrieve_id != this.props.articleId)) {
			if(!this.props.data) console.log("FieldArticlePriceRateContainer.retrieveData !this.props.data",this.props.data);
			if(retrieve_id != this.props.articleId) console.log("FieldArticlePriceRateContainer.retrieveData retrieve_id != this.props.articleId",retrieve_id,this.props.articleId);
			let fetchPromiseWrapper = {};
			let fetch_id = this.props.articleId;
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/builder/1",
					params: {
						page: 0,
						size: 1,
						from: "ArticlePriceRate",
						select: "ArticlePriceRate,ArticlePriceRate.article,ArticlePriceRate.currency",
						"ArticlePriceRate.article.id": fetch_id,
						"ArticlePriceRate.validFromDate": [moment().set('year', 1900).format("YYYY/MM/DD HH:mm:ss ZZ"),moment().format("YYYY/MM/DD HH:mm:ss ZZ")],
						"ArticlePriceRate.validToDate": [moment().format("YYYY/MM/DD HH:mm:ss ZZ"),moment().set('year', 3000).format("YYYY/MM/DD HH:mm:ss ZZ")],
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
					builderDataToProjection(newData,"articlePriceRates");
					newData = (newData && newData._embedded && newData._embedded.articlePriceRates instanceof Array ? newData._embedded.articlePriceRates[0] : {_retrieveArticleId: fetch_id});
					if(newData) {
						newData._retrieveArticleId = fetch_id;
					} else {
						newData = {_retrieveArticleId: fetch_id};
					}
					return newData;
				},
				'FieldArticlePriceRateContainer.retrieveData',
				{},
				fetchPromiseWrapper
			);
			fetchPromiseWrapper.promise.then((response) => {this.props.onChange();});
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
						//TODO manual selection of price?
					} else {
						let fieldText = "";
						if(!editable) {
							fieldText = data && data.price ? data.price+(data && data.currency ? " "+data.currency.name : "") : this.props.t("--none--");
						}
						if(editable == undefined) {
							return <div style={{display: 'inline-block'}}>{fieldText}</div>;
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
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	return {
		auth: state.auth,
		//storage
		data: data,
		componentPath: ownProps.componentPath,
		loading: ownProps.loading != undefined ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		//data
		articleId: ownProps.articleId,
		//UI
		editable: ownProps.editable,
		onChange: ownProps.onChange ? ownProps.onChange : (x) => (x)
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldArticlePriceRateContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
