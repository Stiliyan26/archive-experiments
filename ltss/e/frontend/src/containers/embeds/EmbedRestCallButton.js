import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import { Alert, Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import * as Constants from './../../static/constants';
import { resetRESTCallLimit } from '../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';

class EmbedRestCallButton extends React.Component {

	render() {
		let errorMessage = undefined;
		if(this.props.data) {
			if(this.props.data instanceof Error) {
				errorMessage = this.props.data.message;
			} else if(this.props.data.errors instanceof Array && this.props.data.errors.length > 0) {
				errorMessage = this.props.data.errors[0];
				if(this.props.data.errors[0] instanceof Error) {
					errorMessage = this.props.data.errors[0].message;
				}
			} else if(this.props.data.response && this.props.data.response.data && this.props.data.response.data.message) {
				errorMessage = this.props.data.response.data.message;
			}
		}
		let statusIcon = (this.props.data instanceof Error || this.props.data && this.props.data.errors instanceof Array && this.props.data.errors.length > 0) ?
				<FontAwesomeIcon icon="exclamation-circle" className=''/>
				:	((this.props.loading) ?
						<FontAwesomeIcon icon="spinner" spin/>
					:	'');
		//if(this.props.data instanceof Error) {
		//	return <Alert variant="danger"><FontAwesomeIcon icon="ban"/> {this.props.data.message}</Alert>;
		//}
		return <div>
				<div><Button {...this.props} >{statusIcon}{this.props.children}</Button></div>
				<div><small className="text-danger">{errorMessage}</small></div>
			</div>;
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest);
	return {
		data: data,
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		onClick: (e) => {e.stopPropagation(); resetRESTCallLimit(); ownProps.onClick(e);},
		variant: ownProps.variant ? ownProps.variant : "outline-dark",
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { resetRESTCallLimit }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedRestCallButton);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
