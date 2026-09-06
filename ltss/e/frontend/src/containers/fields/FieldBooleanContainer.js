import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import CommonFieldContainer from './CommonFieldContainer';

class FieldBooleanContainer extends React.Component {
	
	render() {
		return <CommonFieldContainer
				auth={this.props.auth}
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
					if(editable == undefined) {
						return <div style={{display: 'inline-block'}}>{data ? this.props.t("Yes") : this.props.t("No")}</div>;
					}
					if(editable == true) {
						return <input id={this.props.componentPath} type="checkbox" 
								checked={data} 
								onChange={(e) => onChange(e.target.checked)}
							/>;
					}
					if(editable == false) {
						return <input id={this.props.componentPath} type="checkbox"
								checked={data} 
								disabled={true}
							/>;
					}
					return undefined;
				}}
			/>;
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	return {
		auth: state.auth,
		componentPath: ownProps.componentPath,
		editable: ownProps.editable,
		onChange: ownProps.onChange ? ownProps.onChange : (x) => (x)
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldBooleanContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
