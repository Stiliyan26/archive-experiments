import React from 'react';
import { Creatable } from 'react-select';

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
class FieldSelectOrEditContainer extends React.Component {

	retrieveData() {
		if(this.props.listReportEntity && this.props.listReportAttr && this.props.editable && !this.props.items) {
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/reports/builder/1",
					params: {
						size: 1000,
						from: this.props.listReportEntity,
						select: this.props.listReportEntity+"."+this.props.listReportAttr,
					}
				},
				"editableLists."+this.props.listReportEntity+"."+this.props.listReportAttr,
				response => {
					let newData = response.data._embedded.hashMaps.map((elem) => elem[this.props.listReportEntity+"."+this.props.listReportAttr]);
					return newData;
				},
				'FieldSelectOrEditContainer.retrieveData',
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
				onChange={this.props.onChangeF}
				renderF={(editable,data,onChange) => {
					if(editable == true) {
						let value = undefined;
						if(data) {
							value = {
								value: data,
								label: data,
							};
						}
						let options = this.props.items instanceof Array ? 
								this.props.items
								.filter((item) => item != undefined && item != null)
								.map((item, index) => ({
									value: item,
									label: item,
									index: index
								}))
								: undefined;
						return <Creatable
								value={value}
								options={options}
								onChange={(e) => {
									if(e) {
										if(this.props.items instanceof Array && this.props.items.length > 0) {
											if(!this.props.items.includes(e.value)) {
												this.props.actions.dispatchEditRESTData("editableLists."+this.props.listReportEntity+"."+this.props.listReportAttr+"."+this.props.items.length,e.value);
											}
										} else {
											this.props.actions.dispatchEditRESTData("editableLists."+this.props.listReportEntity+"."+this.props.listReportAttr,[e.value]);
										}
									}
									if(onChange) {
										onChange(e ? e.value : undefined);
									}
								}}
								isLoading={this.props.items && this.props.items[Constants.PATH_FOR_LOADING] instanceof Promise}
								placeholder={this.props.t("Choose or type...")}
								formatCreateLabel={(value) => (this.props.t("CreateNewOption")+' "'+value+'"')}
							/>
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
		listReportEntity: ownProps.listReportEntity,
		listReportAttr: ownProps.listReportAttr,
		items: state.rest.editableLists && state.rest.editableLists[ownProps.listReportEntity] ? state.rest.editableLists[ownProps.listReportEntity][ownProps.listReportAttr] : undefined,
		//data
		//UI
		editable: ownProps.editable,
		onChangeF: ownProps.onChange
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldSelectOrEditContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
