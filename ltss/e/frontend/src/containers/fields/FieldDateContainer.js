import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import DatePicker from 'react-date-picker';
import 'react-datepicker/dist/react-datepicker.css';
import moment from 'moment'
moment.locale('bg')
import { Form, FormControl } from 'react-bootstrap';

import CommonFieldContainer from './CommonFieldContainer';

class FieldDateContainer extends React.Component {

	render() {
		return <CommonFieldContainer
				auth={this.props.auth}
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
					if(editable == undefined) {
						return <div style={{display: 'inline-block'}}>{data ? (new Date(data)).toLocaleDateString('bg-BG') : "--.--.---- г."}</div>;
					}
					if(editable == true) {
//						return <DatePicker
//								className="col-sm-12 form-control"
//								selected={data ? moment(data) : undefined}
//								placeholderText="Избери дата.."
//								locale="bg-BG"
//								timeFormat={this.props.showTimeSelect ? "HH:mm" : undefined}
//								//timeIntervals={1}
//								dateFormat={this.props.showTimeSelect ? "DD.MM.YYYY HH:mm" : "DD.MM.YYYY"}
//								onChange={(e) => {
//									e.seconds(0)
//									e.millisecond(0)
//									return onChange(e ? e.toISOString() : e)}
//								} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
//								showTimeSelect={this.props.showTimeSelect}
//							/>;
						return <DatePicker
							className="col-sm-12 form-control"
							value={data ? new Date(data) : undefined}
							locale="bg-BG"
							showLeadingZeros={true}
							calendarIcon={null}
							onChange={(e) => {
									//send date as UTC with no time zone (+0) to avoid date change because of the time zones
									let dt = new Date(0,0,0,0,0,0,0);
									dt.setUTCFullYear(e.getFullYear(), e.getMonth(), e.getDate());
									dt.setUTCHours(0);
									//console.log("dates:",e.toISOString(),dt.toISOString());
									//return onChange(e ? e.toISOString() : e);
									return onChange(dt ? dt.toISOString() : dt);
								}
							} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
						/>;
					}
					if(editable == false) {
						//return <div className="col-sm-12 form-control" disabled style={{display: 'inline-block'}}>{data ? (new Date(data)).toLocaleString('bg-BG') : "--.--.---- г."}</div>;
						return <Form.Control type="text" className="col-sm-12" disabled style={{display: 'inline-block'}} value={data ? (new Date(data)).toLocaleDateString('bg-BG') : "--.--.---- г."}/>;
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldDateContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;
