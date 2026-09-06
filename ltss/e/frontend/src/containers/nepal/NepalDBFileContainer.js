import React from 'react';
import { Alert } from 'react-bootstrap';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants.js';
import history from '../../scripts/history.js'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import { postRESTData, dispatchEditRESTData } from '../../actions/taskActions.js';
import { resolveObjectPath, extractErrorMessage } from '../../scripts/dataUtils.js';

import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'

class NepalDBFileContainer extends React.Component {

	componentDidMount() {
		if (!isAuthenticated(this.props.auth)) {
			history.push('/login')
		} else {
			if (this.props.selectedItem != this.props.selectedAttachable) {
				this.props.onSelectF(this.props.selectedItem);
			}
		}
	}

	componentDidUpdate(prevProps, prevState) {
		if (this.props.selectedItem != this.props.selectedAttachable) {
			this.props.onSelectF(this.props.selectedItem);
		}
	}

	handleFileUpload({ target }) {
		const files = target.files;
		const file = files[0];
		let self = this;

		var reader = new FileReader();
		reader.onload = (function () {
			var dataURL = reader.result.substr(reader.result.indexOf(',') + 1);; //here we get the file in base64
			self.props.actions.postRESTData(
				{
					method: 'post',
					url: API_URL + '/dBFiles',
					data: { content: dataURL, name: file.name, contentType: file.type }
				},
				this.props.componentPath + ".uploadedFile",
				'NepalDBFileContainer.handleFileUpload',
				undefined,
				(data) => {
					if (!data) {

					}
					console.log("DBFile")
					console.log(data)
					console.log("Data Id: " + data.id)
					this.props.onFileUpload(data.id)
				}
			);
		}).bind(this);
		reader.readAsDataURL(file)
	}

	getTagId() {
		if(this.props.nepalDbFileInputTagId == null) {
			return "file"
		}
		return this.props.nepalDbFileInputTagId
	}

	render() {
		let errorMessage = extractErrorMessage(this.props.error, getEntityDefinition("dBFiles"));
		let buttonText = this.props.buttonText

		return (
			<div>
				{this.props.error ?
					<Alert variant="danger"><FontAwesomeIcon icon="ban" /> {errorMessage}</Alert>
					: ""}
				<div>
					{/* <label className="btn btn-primary col-sm-12" htmlFor={this.getTagId()}>
						<FontAwesomeIcon icon="upload" />
						&nbsp;{this.props.fileToUpload ? this.props.fileToUpload.name :  buttonText }
					</label> */}
					<label className="button" htmlFor={this.getTagId()}>
						<FontAwesomeIcon icon="upload" />
						&nbsp;{this.props.fileToUpload ? this.props.fileToUpload.name :  buttonText }
					</label>
					<input type="file" name={this.getTagId()} id={this.getTagId()} className="inputfile" onChange={(ev) => this.handleFileUpload(ev)} />
				</div>
			</div>
		);
	}
}

//redux mapping of props
function mapStateToProps(state, ownProps) {
	let data = resolveObjectPath(ownProps.componentPath, state.rest); //rest because of fetchREST
	return {
		auth: state.auth,
		componentPath: ownProps.componentPath,
		error: data instanceof Error ? data : (data && data.uploadedFile instanceof Error ? data.uploadedFile : (data && data.uploadedFile && data.uploadedFile[Constants.PATH_FOR_ERROR] instanceof Error ? data.uploadedFile[Constants.PATH_FOR_ERROR] : undefined)),
		uploadedFile: data ? data.uploadedFile : undefined,
		selectedAttachable: ownProps.selectedAttachable,
		selectedItem: data && data.selectedRows && data.selectedRows.map instanceof Map ? data.selectedRows.map.values().next().value : undefined,
		onSelectF: ownProps.onSelectF,
		onFileUpload: ownProps.onFileUpload
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { postRESTData, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(NepalDBFileContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
