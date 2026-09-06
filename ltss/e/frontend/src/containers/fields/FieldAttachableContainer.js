import React, { Suspense, lazy } from 'react';
import Select from 'react-select'
import Modal from 'react-responsive-modal';
import { Card, ButtonGroup, Button } from 'react-bootstrap';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import CommonFieldContainer from './CommonFieldContainer';
import DBFileListContainer from './DBFileListContainer'
import EmbedOfferToClientContainer from './../embeds/EmbedOfferToClientContainer'
import EmbedEmployeeAttestationContainer from './../embeds/EmbedEmployeeAttestationContainer'
import EmbedRetrieveEntityListContainer from './../nomenclatures/EmbedRetrieveEntityListContainer'
import EmbedEntityContainer from './../embeds/EmbedEntityContainer';

import * as Constants from './../../static/constants';
import { fetchRESTFollow, dispatchEditRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath } from './../../scripts/dataUtils';
import { getEntityDefinition } from './../nomenclatures/entityDefinitions.js'

const PdfPreview = React.lazy(() => import('./PdfPreview'));


//redux container class
class FieldAttachableContainer extends React.Component {
	constructor(...args) {
		super(...args);
		let self = this;
		this.state = {
				isSelectAttachableModalOpen: false,
				isPreviewModalOpen: false,
				selectedAttachableType: 1,
				selectedAttachable: undefined,
			};
		import("sanitize-html").then(sanitizeHtml => {
			self.setState({sanitizeHtml: sanitizeHtml});
		});
	}
	

	getAttachmentTypes() {
		return [
			{
				value: 1,
				label: this.props.t("DBFile._className"),
				apiEndpoint: "/dBFiles",
				displayAttr: "name",
				preview: (data) => {
					if(data === undefined) {
						return "";
					}
					//TODO maybe check content type better: https://stackoverflow.com/questions/18299806/how-to-check-file-mime-type-with-javascript-before-upload
					//TODO https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Complete_list_of_MIME_types
					//TODO preview other file formats: https://www.npmjs.com/package/react-file-viewer
					let contentPreview = this.props.t("This file type cannot be previewed");
					if(data.contentType && data.contentType.toLowerCase().includes("image/")) {
						contentPreview = <img src={"data:"+data.contentType+";base64,"+data.content}/>; //https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/Data_URIs
					}
					if(data.contentType && data.contentType.toLowerCase().includes("text/")) {
						//TODO lazy load sanitizeHtml
						contentPreview = this.state.sanitizeHtml(Buffer.from(data ? data.content : "", "base64").toString("ascii"));//text files only
					}
					if(data.contentType && data.contentType.toLowerCase().includes("application/pdf")) {
						//TODO this is nightmare for big PDF files. Maybe change with server-side rendering (https://github.com/katjas/PDFrenderer), or make it as separate endpoint downloading file and let Chrome handle it
						contentPreview = <Suspense fallback={<div>Loading...</div>}><PdfPreview imageSrc={"data:application/pdf;base64,"+data.content} /></Suspense>;
					}
					return <Card>
							<Card.Header>
								{this.props.t("You can download the file from the link")}:&nbsp;
								<a href={"data:"+data.contentType+";base64,"+data.content} download={data.name}>
									<FontAwesomeIcon icon="download"/>&nbsp;
									{data.name}
								</a>
							</Card.Header>
							<Card.Body>
								{contentPreview}
							</Card.Body>
						</Card>;
				}
			},
			{
				value: 5,
				label: this.props.t("OfferToClient._className"),
				apiEndpoint: "/offerToClients",
				displayAttr: "name",
				preview: (data,componentPath,attachedToObjectData) => {
					let body = '';
					if(data) {
						if(data[Constants.PATH_FOR_LOADING] instanceof Promise) {
							body = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
						} else {
							const offerLinesDef = getEntityDefinition("offerLines",{offerToClient: {show: false}});
							body = <EmbedRetrieveEntityListContainer
										title={offerLinesDef.label}
										icon={offerLinesDef.icon}
										columns={offerLinesDef.columns}
										componentPath={componentPath+".offerLines"}
										retrieveType="offerLines"
										parentHref={data._links.self.href}
										parentAttr="offerToClient"
										parentData={data}
										expanded={true}
										asTable={true}
										creatable={false}
									/>;
						}
					}
					return <div>
								<EmbedOfferToClientContainer
									retrieve_id={data.id}
									componentPath={componentPath}
									creatable={false}
								/>
								{body}
							</div>;
					}
			},
			{
				value: 6,
				label: this.props.t("EmployeeAttestation._className"),
				apiEndpoint: "/employeeAttestations",
				displayAttr: "name",
				preview: (data,componentPath,attachedToObjectData) => {
					return <EmbedEmployeeAttestationContainer
								retrieve_id={data.id}
								componentPath={componentPath}
								creatable={false}
							/>;
					}
			},
			{
				value: 7,
				label: this.props.t("Expenditure._className"),
				apiEndpoint: "/expenditures",
				displayAttr: "expenseDesc",
				preview: (data,componentPath,attachedToObjectData) => {
					return <EmbedEntityContainer
								componentPath={componentPath + "_details"}
								retrieve_id={data.id}
								entityName={"expenditures"}
								headerText={this.props.t("Expenditure._className")}
								creatable={false}
								expanded={true}
							/>
					}
			},
			{
				value: 8,
				label: this.props.t("Income._className"),
				apiEndpoint: "/incomes",
				displayAttr: "incomeDesc",
				preview: (data,componentPath,attachedToObjectData) => {
					return <EmbedEntityContainer
								componentPath={componentPath + "_details"}
								retrieve_id={data.id}
								entityName={"incomes"}
								headerText={this.props.t("Income._className")}
								creatable={false}
								expanded={true}
							/>;
					}
			},
//								<option>EXT_FILE</option>
//								<option>INT_FILE</option>
//								<option>SANTA_REQ_FOR_OFFER</option>
//								<option>SANTA_CONTRACT</option>
//								<option>SANTA_ORDER_TO_VENDOR</option>
//								<option>SANTA_WAREHOUSE_IN</option>
//								<option>SANTA_INVOICE</option>
		];
	}

	retrieveData() {
		const retrieve_href = this.props.data && this.props.data._retrieveHref ? this.props.data._retrieveHref : this.props.href;
		if(this.props && !this.props.error && !this.props.loading 
				&& this.props.href && this.props.componentPath
				&& (this.props.data === undefined || this.props.href != retrieve_href)) {
			this.props.actions.fetchRESTFollow(
				{
					url: this.props.href,
				},
				this.props.componentPath,
				response => {
					let data = response.data;
					this.setState({selectedAttachableType: this.inferAttachableType(data).value});
					return {...data,_retrieveHref: this.props.href};
				},
				'FieldAttachableContainer.retrieveData',
			);
		}
	}

	componentDidMount(){
		this.retrieveData();
	}

	componentDidUpdate(prevProps, prevState) {
		this.retrieveData();
	}

	inferAttachableType(attachable) {
		let result;
		if(attachable && attachable._links && attachable._links.self) {
			result = this.getAttachmentTypes().find((elem) => {
				return attachable._links.self.href.includes(elem.apiEndpoint);
			});
		}
		if(result == undefined) {
			result = this.getAttachmentTypes()[0];
		}
		return result;
	}

	getDisplayLabel(displayAttr){
		let displayAttrArr
		displayAttrArr = displayAttr ? displayAttr.split('.') : ['name']
		let result = this.state.selectedAttachable[displayAttrArr[0]]
		for (var i = 1; i < displayAttrArr.length; i++) {
			result = result[displayAttrArr[i]]
		}
		return result
	}
	
	render() {
		let options = this.getAttachmentTypes().filter((elem) => {
			if(this.props.excludeAttachmentTypes == undefined) {
				return true;
			}
			return !this.props.excludeAttachmentTypes.some((excludeElem) =>
					elem.apiEndpoint.includes(excludeElem)
				);
		});
		let displayAttr
		let selectedOption = options.filter((x) => x.value == this.state.selectedAttachableType)[0]
		if(selectedOption){
			displayAttr = selectedOption.displayAttr || 'name'
		}

		return <CommonFieldContainer
				auth={this.props.auth}
				componentPath={this.props.componentPath}
				editable={this.props.editable}
				loading={this.props.loading}
				onChange={this.props.onChange}
				renderF={(editable,data,onChange) => {
					const attType = this.inferAttachableType(data);
					let fieldText = "";
					if(data != undefined && data != "") {
						fieldText = data[attType.displayAttr];
					} else {
						fieldText = this.props.t("--none--");
					}
					//TODO download or preview
					let previewElement = attType.preview(data,this.props.componentPath,this.props.attachedToObjectData);

					let selectDocumentTypeValue = {
							value: this.state.selectedAttachableType,
							label: this.getAttachmentTypes().find((item) => item.value == this.state.selectedAttachableType).label,
						};
					return <div style={{display: 'inline-block'}}>
							<ButtonGroup>
								{data != undefined && data != "" ? 
									<Button variant="outline-dark" title={this.props.t("Download")} onClick={(e) => {
											e.stopPropagation();
											this.setState({ isPreviewModalOpen: true });
										}}><FontAwesomeIcon icon="download"/></Button>
									: ""}
								{editable ? <Button variant="outline-dark" title={this.props.t("Attach")} onClick={(e) => {
												e.stopPropagation();
												this.setState({ isSelectAttachableModalOpen: true, selectedAttachable: this.props.data });
											}}><FontAwesomeIcon icon="paperclip" /></Button>
										: ''}
							</ButtonGroup>&nbsp;
							{fieldText} ({attType.label})
							<Modal open={this.state.isSelectAttachableModalOpen} onClose={() => {this.setState({ isSelectAttachableModalOpen: false });}} showCloseIcon={false}>


								<ButtonGroup>
									<Button disabled={!this.state.selectedAttachable} className="col-sm-12" variant={(this.state.selectedAttachable ? "success" : "warning")}
										onClick={this.state.selectedAttachable ? () => {
											resetRESTCallLimit();
											this.props.actions.dispatchEditRESTData(this.props.componentPath,this.state.selectedAttachable);
											if(this.props.onChangeF) {
												this.props.onChangeF(this.state.selectedAttachable);
											}
											this.setState({ isSelectAttachableModalOpen: false });
										} : undefined}
									>
										<FontAwesomeIcon icon="paperclip"/>
										&nbsp;{ this.state.selectedAttachable ? this.props.t("Attach") + ": "+ this.getDisplayLabel(displayAttr) : this.props.t("Choose document") }
									</Button>
								</ButtonGroup>


								<label className="contracts-add-form-label">{this.props.t("DocumentType")}:</label>
								<Select
									value={selectDocumentTypeValue}
									options={options}
									onChange={(e) => {
										this.setState({ selectedAttachableType: e.value });
									}}
									placeholder={this.props.t("Choose...")}
								/>
								{this.state.selectedAttachableType == 1 ?
									<DBFileListContainer
										componentPath = {this.props.componentPath + "._dBFiles"} //existing path in redux store where we put data
										selectedAttachable={this.state.selectedAttachable}
										onSelectF={(selectedAttachable) => {
											this.setState({selectedAttachable: selectedAttachable});
										}}
									/> : ''}
								{this.state.selectedAttachableType == 2 ?
									<EmbedRetrieveEntityListContainer
										retrieveType = "mailMessages"
										componentPath = {this.props.componentPath + "._mailMessages"} //existing path in redux store where we put data
										columns = {getEntityDefinition("mailMessages").columns}
										hasRowSelecting = {true}
										title={this.props.t("MailMessage._className")}
										expanded={true}
										asTable={true}
										editable={false}
										addSelectedRows = {this.state.selectedAttachable}
										afterAddSelectedRowsF = {() => {
											let selectedItem = data && data._mailMessages && data._mailMessages.selectedRows && data._mailMessages.selectedRows.map ? data._mailMessages.selectedRows.map.values().next().value : undefined;
											if(selectedItem != this.state.selectedAttachable) {
												this.setState({selectedAttachable: selectedItem});
											}
										}}
										isMultiSelect = {false}
									/> : ''}
								{this.state.selectedAttachableType == 3 ?
									<EmbedRetrieveEntityListContainer
										retrieveType = "mailTemplates"
										componentPath = {this.props.componentPath + "._mailTemplates"} //existing path in redux store where we put data
										columns = {getEntityDefinition("mailTemplates").columns}
										hasRowSelecting = {true}
										title={this.props.t("MailTemplate._className")}
										expanded={true}
										asTable={true}
										editable={false}
										addSelectedRows = {this.state.selectedAttachable}
										afterAddSelectedRowsF = {() => {
											let selectedItem = data && data._mailTemplates && data._mailTemplates.selectedRows && data._mailTemplates.selectedRows.map ? data._mailTemplates.selectedRows.map.values().next().value : undefined;
											if(selectedItem != this.state.selectedAttachable) {
												this.setState({selectedAttachable: selectedItem});
											}
										}}
										isMultiSelect = {false}
									/> : ''}
								{this.state.selectedAttachableType == 4 ?
									<EmbedRetrieveEntityListContainer
										retrieveType = "sendMailMessages"
										componentPath = {this.props.componentPath + "._sendMailMessages"} //existing path in redux store where we put data
										columns = {getEntityDefinition("sendMailMessages").columns}
										hasRowSelecting = {true}
										title={this.props.t("SendMailMessage._className")}
										expanded={true}
										asTable={true}
										editable={false}
										addSelectedRows = {this.state.selectedAttachable}
										afterAddSelectedRowsF = {() => {
											let selectedItem = data && data._sendMailMessages && data._sendMailMessages.selectedRows && data._sendMailMessages.selectedRows.map ? data._sendMailMessages.selectedRows.map.values().next().value : undefined;
											if(selectedItem != this.state.selectedAttachable) {
												this.setState({selectedAttachable: selectedItem});
											}
										}}
										isMultiSelect = {false}
									/> : ''}
								{this.state.selectedAttachableType == 5 ?
									<EmbedRetrieveEntityListContainer
										retrieveType = "offerToClients"
										componentPath = {this.props.componentPath + "._offerToClients"} //existing path in redux store where we put data
										columns = {getEntityDefinition("offerToClients").columns}
										hasRowSelecting = {true}
										title={this.props.t("OfferToClient._className")}
										expanded={true}
										asTable={true}
										editable={false}
										addSelectedRows = {this.state.selectedAttachable}
										afterAddSelectedRowsF = {() => {
											let selectedItem = data && data._offerToClients && data._offerToClients.selectedRows && data._offerToClients.selectedRows.map ? data._offerToClients.selectedRows.map.values().next().value : undefined;
											if(selectedItem != this.state.selectedAttachable) {
												this.setState({selectedAttachable: selectedItem});
											}
										}}
										isMultiSelect = {false}
									/> : ''}
								{this.state.selectedAttachableType == 6 ?
									<EmbedRetrieveEntityListContainer
										retrieveType = "employeeAttestations"
										componentPath = {this.props.componentPath + "._employeeAttestations"} //existing path in redux store where we put data
										columns = {getEntityDefinition("employeeAttestations").columns}
										hasRowSelecting = {true}
										title={this.props.t("EmployeeAttestation._className")}
										expanded={true}
										asTable={true}
										editable={false}
										addSelectedRows = {this.state.selectedAttachable}
										afterAddSelectedRowsF = {() => {
											let selectedItem = data && data._employeeAttestations && data._employeeAttestations.selectedRows && data._employeeAttestations.selectedRows.map ? data._employeeAttestations.selectedRows.map.values().next().value : undefined;
											if(selectedItem != this.state.selectedAttachable) {
												this.setState({selectedAttachable: selectedItem});
											}
										}}
										isMultiSelect = {false}
									/> : ''}
								{this.state.selectedAttachableType == 7 ?
									<EmbedRetrieveEntityListContainer
										retrieveType = "expenditures"
										componentPath = {this.props.componentPath + "._expenditures"} //existing path in redux store where we put data
										columns = {getEntityDefinition("expenditures").columns}
										hasRowSelecting = {true}
										title={this.props.t("Expenditure._className")}
										expanded={true}
										asTable={true}
										editable={false}
										addSelectedRows = {this.state.selectedAttachable}
										afterAddSelectedRowsF = {() => {
											let selectedItem = data && data._expenditures && data._expenditures.selectedRows && data._expenditures.selectedRows.map ? data._expenditures.selectedRows.map.values().next().value : undefined;
											if(selectedItem != this.state.selectedAttachable) {
												this.setState({selectedAttachable: selectedItem});
											}
										}}
										isMultiSelect = {false}
									/> : ''}
								{this.state.selectedAttachableType == 8 ?
									<EmbedRetrieveEntityListContainer
										retrieveType = "incomes"
										componentPath = {this.props.componentPath + "._incomes"} //existing path in redux store where we put data
										columns = {getEntityDefinition("incomes").columns}
										hasRowSelecting = {true}
										title={this.props.t("Income._className")}
										expanded={true}
										asTable={true}
										editable={false}
										addSelectedRows = {this.state.selectedAttachable}
										afterAddSelectedRowsF = {() => {
											let selectedItem = data && data._incomes && data._incomes.selectedRows && data._incomes.selectedRows.map ? data._incomes.selectedRows.map.values().next().value : undefined;
											if(selectedItem != this.state.selectedAttachable) {
												this.setState({selectedAttachable: selectedItem});
											}
										}}
										isMultiSelect = {false}
									/> : ''}
							</Modal>
							<Modal open={this.state.isPreviewModalOpen} onClose={() => {this.setState({ isPreviewModalOpen: false });}} showCloseIcon={false}>
								{previewElement}
							</Modal>
						</div>; //TODO modal should be scrollable - now it closes when you click the scrollbar, because it is outside
				}}
			/>;
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	return {
		auth: state.auth,
		//storage
		data: data,
		componentPath: ownProps.componentPath,
		//retrieval
		loading: ownProps.loading ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		error: data instanceof Error ? data : undefined,
		//data
		href: ownProps.href || (data && data._links && data._links.self ? data._links.self.href : undefined),
		attachedToObjectData: ownProps.attachedToObjectData,
		//UI
		editable: ownProps.editable,
		onChangeF: ownProps.onChangeF,
		excludeAttachmentTypes: ownProps.excludeAttachmentTypes,
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(FieldAttachableContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
