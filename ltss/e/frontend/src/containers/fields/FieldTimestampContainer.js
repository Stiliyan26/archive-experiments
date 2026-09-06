import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import DateTimePicker from 'react-datetime-picker';
import moment from 'moment'
moment.locale('bg')
import { Form, FormControl } from 'react-bootstrap';

import CommonFieldContainer from './CommonFieldContainer';

class FieldTimestampContainer extends React.Component {

	render() {
		return <CommonFieldContainer
				auth={this.props.auth}
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
					if(editable == undefined) {
						return <div style={{display: 'inline-block'}}>{data ? (new Date(data)).toLocaleString('bg-BG') : "--.--.---- г. --:-- ч."}</div>;
					}
					if(editable == true) {
						return <DateTimePicker
								className="col-sm-12 form-control"
								value={data ? new Date(data) : undefined}
								locale="bg-BG"
								maxDetail={"minute"}
								showLeadingZeros={true}
								//disableClock
								calendarIcon={null}
								onChange={(e) => {
									return onChange(e ? e.toISOString() : e);}
								} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
							/>;
					}
					if(editable == false) {
						//return <div className="col-sm-12 form-control" disabled style={{display: 'inline-block'}}>{data ? (new Date(data)).toLocaleString('bg-BG') : "--.--.---- г. --:-- ч."}</div>;
						return <Form.Control type="text" className="col-sm-12" disabled style={{display: 'inline-block'}} value={data ? (new Date(data)).toLocaleString('bg-BG') : "--.--.---- г. --:-- ч."}/>;
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
		showTimeSelect: ownProps.showTimeSelect,
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldTimestampContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
