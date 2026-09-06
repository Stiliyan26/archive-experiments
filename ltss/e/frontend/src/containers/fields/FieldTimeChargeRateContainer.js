import React from 'react';
import url from 'url'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import { fetchRESTFollow, dispatchEditRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

import CommonFieldContainer from './CommonFieldContainer';

//redux container class
class FieldTimeChargeRateContainer extends React.Component {

	retrieveData() {
		const resourceParam = this.props.data && this.props.data.id !== undefined ? this.props.data.id : this.props.resourceId;
		if(this.props.resourceId && this.props.componentPath && !this.props.loading
				&& (!this.props.data || resourceParam != this.props.resourceId)) {
			if(!this.props.data) console.log('FieldTimeChargeRateContainer retrieve: No data!');
			if(resourceParam != this.props.resourceId) console.log('FieldTimeChargeRateContainer retrieve: resourceParam != this.props.resourceId!',resourceParam, this.props.resourceId);
			let fetchPromiseWrapper = {};
			console.error("Change this implementation to call report builder");
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/timeChargeRates",
					params: {"resource.id": this.props.resourceId}
				},
				this.props.componentPath,
				response => ( response.data ),
				'FieldTimeChargeRateContainer.retrieveData',
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
						//TODO manual selection from the available prices?
					} else {
						let fieldText = "";
						if(!editable) {
							fieldText = data && data._embedded && data._embedded[this.props.nomenclatureKey]
										&& data._embedded[this.props.nomenclatureKey][this.props.selectedIndex]
										? data._embedded[this.props.nomenclatureKey][this.props.selectedIndex][this.props.displayAttr]
										: this.props.t("--none--");
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
		nomenclatureKey: ownProps.nomenclatureKey,
		items: data && data._embedded ? data._embedded.timeChargeRates : undefined,
		loading: ownProps.loading != undefined ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		//data
		resourceId: ownProps.resourceId,
		selectedIndex: 0,
		//UI
		displayAttr: ownProps.displayAttr ? ownProps.displayAttr : 'chargeRatePerHour',
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldTimeChargeRateContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
