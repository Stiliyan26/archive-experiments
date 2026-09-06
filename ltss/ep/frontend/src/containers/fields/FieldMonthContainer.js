import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import DatePicker from 'react-datepicker'
import 'react-datepicker/dist/react-datepicker.css';
import moment from 'moment'
moment.locale('bg')
import { withTranslation } from 'react-i18next';

import CommonFieldContainer from './CommonFieldContainer';

class FieldMonthContainer extends React.Component {

	render() {
		return <CommonFieldContainer
				auth={this.props.auth}
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
					if(editable == undefined) {
						let date = new Date(data)
						return <div style={{display: 'inline-block'}}>{data ? (`${date.getMonth() + 1}.${date.getFullYear()}`) : this.props.t("--none--")}</div>;
					}
					if(editable == true) {
						return <DatePicker
								className="col-sm-12 form-control"
								selected={data ? moment(data) : undefined}
								placeholderText={this.props.t("Choose month...")}
								locale="bg-bg"
								timeFormat={this.props.showTimeSelect ? "HH:mm" : undefined}
								//timeIntervals={15}
								dateFormat={"MM.YYYY"}
								onChange={(e) => onChange(e.toISOString())} //don't use object, because postREST deletes it or fix postREST //use Moment object and format when needed: "YYYY/MM/DD HH:mm:ss ZZ" - java default, "YYYY-MM-DDTHH:mm:ss.SSSZZ" - java ISO, maybe RFC2822???
								showTimeSelect={false}
							/>;
					}
					if(editable == false) {
						return <DatePicker
								className="col-sm-12 form-control"
								selected={data ? moment(data) : undefined}
								placeholderText={this.props.t("Choose date...")}
								locale="bg-bg"
								timeFormat={this.props.showTimeSelect ? "HH:mm" : undefined}
								//timeIntervals={15}
								dateFormat={"MM.YYYY"}
								disabled={true}
								showTimeSelect={false}
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldMonthContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
