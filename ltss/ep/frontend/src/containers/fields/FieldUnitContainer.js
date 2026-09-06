import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import CommonFieldContainer from './CommonFieldContainer';

class FieldUnitContainer extends React.Component {
	
	render() {
		return <CommonFieldContainer
				auth={this.props.auth}
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
					if(editable == undefined) {
						return <div style={{display: 'inline-block'}}>{this.props.convertFromDataF(data)}</div>;
					}
					if(editable == true) {
						return <input id={this.props.componentPath} className="form-control" type="number" 
								value={this.props.convertFromDataF(data)} 
								onChange={(e) => onChange(this.props.convertToDataF(e.target.value))}
							/>;
					}
					if(editable == false) {
						return <input id={this.props.componentPath} className="form-control" type="number" 
								value={this.props.convertFromDataF(data)} 
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
		convertFromDataF: ownProps.convertFromDataF ? ownProps.convertFromDataF : (x) => (x),
		convertToDataF: ownProps.convertToDataF ? ownProps.convertToDataF : (x) => (x), 
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldUnitContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
