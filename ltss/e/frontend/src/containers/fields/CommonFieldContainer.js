import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import * as Constants from './../../static/constants';
import { dispatchEditRESTData } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

class CommonFieldContainer extends React.Component {

	render() {
		if(this.props.loading instanceof Promise) {
			return <FontAwesomeIcon icon="spinner" spin/>;
		}
		if(this.props.data instanceof Error) {
			if(this.props.data.response && this.props.data.response.status == 404) {
				if(this.props.editable == false) {
					return <input id={this.props.componentPath} className="form-control" type="text"
							value={this.props.t("--none--")}
							disabled={true}
						/>;
				}
				if(this.props.editable == undefined) {
					return <div style={{display: 'inline-block'}}>{this.props.t("--none--")}</div>;
				}
			} else {
				return <FontAwesomeIcon icon="exclamation-circle" className=''/>;
			}
		}
		let result;
		if(this.props.renderF) {
			result = this.props.renderF(this.props.editable,this.props.data ? this.props.data : "", //for controlled input you shouldn't have undefined value
					(value) => {this.props.actions.dispatchEditRESTData(this.props.componentPath,value);this.props.onChange(value);}
				);
		}
		if(result == undefined) { //if render function doesn't cover the case
			if(this.props.data == null || this.props.data == undefined) {
				if(this.props.editable == false) {
					return <input id={this.props.componentPath} className="form-control" type="text"
							value={this.props.t("--none--")}
							disabled={true}
						/>;
				}
				if(this.props.editable == undefined) {
					return <div style={{display: 'inline-block'}}>{this.props.t("--none--")}</div>;
				}
			}
			if(this.props.editable == false) {
				return <input id={this.props.componentPath} className="form-control" type="text"
						value={this.props.data}
						disabled={true}
					/>;
			}
			if(this.props.editable == undefined) {
				return <div style={{display: 'inline-block'}}>{this.props.data}</div>;
			}
		}
		return result;
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	return {
		auth: state.auth,
		data: data,
		componentPath: ownProps.componentPath,
		editable: ownProps.editable,
		loading: ownProps.loading != undefined ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		onChange: ownProps.onChange ? ownProps.onChange : (x) => (x),
		renderF: ownProps.renderF,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(CommonFieldContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
