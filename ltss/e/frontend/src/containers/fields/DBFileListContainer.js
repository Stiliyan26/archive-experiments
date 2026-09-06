import React from 'react';
import { Alert } from 'react-bootstrap';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants';
import history from './../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import FieldTimestampContainer from './FieldTimestampContainer'
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

import { postRESTData, dispatchEditRESTData } from '../../actions/taskActions';
import { resolveObjectPath, extractErrorMessage } from './../../scripts/dataUtils';

import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'

class DBFileListContainer extends React.Component {
	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		} else {
			if(this.props.selectedItem != this.props.selectedAttachable) {
				this.props.onSelectF(this.props.selectedItem);
			}
		}
	}
	
	componentDidUpdate(prevProps, prevState) {
		if(this.props.selectedItem != this.props.selectedAttachable) {
			this.props.onSelectF(this.props.selectedItem);
		}
	}

	handleFileUpload({ target }) {
		const files = target.files;
		const file = files[0];
		let self = this;

		var reader = new FileReader();
		reader.onload = (function(){
			var dataURL = reader.result.substr(reader.result.indexOf(',') + 1);; //here we get the file in base64
			self.props.actions.postRESTData(
				{
					method: 'post',
					url: API_URL+'/dBFiles',
					data: {content: dataURL, name: file.name, contentType: file.type}
				},
				this.props.componentPath + ".uploadedFile",
				'DBFileListContainer.handleFileUpload',
				undefined,
				(data) => {
					if(!data){
						
					}
					this.props.onFileUpload(data.id)
				}
			);
		}).bind(this);
		reader.readAsDataURL(file)
	}
	
	render() {
		let errorMessage = extractErrorMessage(this.props.error, getEntityDefinition("dBFiles"));
		return (
			<div>
				{this.props.error ?
					<Alert variant="danger"><FontAwesomeIcon icon="ban"/> {errorMessage}</Alert>
					: ""}
				<EmbedRetrieveEntityListContainer
					retrieveType = "dBFiles"
					componentPath = {this.props.componentPath} //existing path in redux store where we put data
					columns = {getEntityDefinition("dBFiles").columns}
					selectedRowsColumns = {[{
							Header: this.props.t("CommonRecord.id"),
							accessor: 'id',
							width: 50
						}, {
							Header: this.props.t("DBFile.name"),
							accessor: 'name'
						}]}
					hasRowSelecting = {true}
					title={this.props.t("DBFile._className_plural")}
					expanded={true}
					asTable={true}
					editable={false}
					addSelectedRows = {this.props.uploadedFile && !this.props.uploadedFile[Constants.PATH_FOR_LOADING] ? this.props.uploadedFile : undefined} //make the uploaded file selected
					afterAddSelectedRowsF = {() => {this.props.actions.dispatchEditRESTData(this.props.componentPath+".uploadedFile",undefined);}}
					isMultiSelect = {false}
				/>
				<label className="btn btn-default col-sm-12" htmlFor="file">
					<FontAwesomeIcon icon="upload"/>
					&nbsp;{ this.props.fileToUpload ? this.props.fileToUpload.name : this.props.t("DBFile.ChooseFile") }
				</label>
				<input type="file" name="file" id="file" className="inputfile" onChange={(ev) => this.handleFileUpload(ev)} />
			</div>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
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
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(DBFileListContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
