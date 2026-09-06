import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import CommonFieldContainer from './CommonFieldContainer';

class FieldTextContainer extends React.Component {
	
	render() {
		return <CommonFieldContainer
				auth={this.props.auth}
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
					if(editable == true) {
						return <input id={this.props.componentPath} className="form-control" type="text" maxLength="255"
								value={data} 
								onChange={(e) => onChange(e.target.value)}
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldTextContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
