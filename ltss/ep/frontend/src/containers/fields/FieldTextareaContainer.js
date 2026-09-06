import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import TextareaAutosize from 'react-autosize-textarea';
import Textarea from 'react-textarea-autosize';

import CommonFieldContainer from './CommonFieldContainer';

class FieldTextareaContainer extends React.Component {

	render() {
		return <CommonFieldContainer
				auth={this.props.auth}
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
					if(editable == true) {
						return <TextareaAutosize
							className="form-control"
							style={{width: '100%', 'verticalAlign': 'top'}}
							value={data}
							onChange={(e) => onChange(e.target.value)}
							maxLength={3000}
						/>
					}
					if(editable == false) {
						return <TextareaAutosize
							className="form-control"
							style={{width: '100%', height: '100px', 'verticalAlign': 'top'}}
							value={data}
							onChange={(e) => onChange(e.target.value)}
							disabled={true}
						/>
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
		onChange: ownProps.onChange ? ownProps.onChange : (x) => (x),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldTextareaContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
