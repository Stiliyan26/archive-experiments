import React from 'react';
import axios from 'axios';
import moment from 'moment';
moment.locale('bg')

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Form } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import EmbedEntityContainer from '../embeds/EmbedEntityContainer'

import * as Constants from '../../static/constants';
import { builderDataToProjection, getEntityDefinition, getEntityForm, builderURLFromColumns } from '../nomenclatures/entityDefinitions.js'
import { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData, resetRESTCallLimit } from '../../actions/taskActions';
import { resolveObjectPath } from '../../scripts/dataUtils';

class EmbedTransportOrderContainer extends React.Component {
	
	handleFileUpload({ target }, fileType) {
		const files = target.files;
		const file = files[0];
		let self = this;

		var reader = new FileReader();
		reader.onload = (function(){
			const FormData = require('form-data');
			const form = new FormData();
			form.append('file', file);
			form.append('doctype', fileType);
			let promise = axios({
					method: 'post',
					url: API_URL+"/service/orderProcessing",
					headers: {Authorization: sessionStorage["X-AUTH-TOKEN"]},
					data: form
				}).then(response => {
				if(response && response.data) {
					console.log("Order import data",response);
					if(fileType == 1) {
						let importedData = {
							...this.props.data,
							[Constants.PATH_FOR_LOADING]: undefined,
							orderDate: moment().format(),
							//transporter
							//orderOrderer
							//sender
							//senderContact
							//orderRoute
							loadingPoint: response.data.port_delivery, //"GR-54001 THESSALONIKI THESSALONIKI TERMINAL THESSALONIKI TERMINAL"
							//orderLoadingDate: moment(response.data.cutoff_date).format("YYYY/MM/DD HH:mm:ss ZZ"), //"15-Jan-2021 00:00:00"
							//exportCustoms
							//exportCustomsAgent
							cargo: response.data.container,
							orderContents: response.data.goods, //"002003 PATIO FURNITURE/SOY PROTEIN ISOLATE GS5100/N/M"
							weight: parseFloat(response.data.weight), //"12126"
							//loadingWarehouseRef
							receiver: response.data.recipient,
							//receiverContact
							unloadingPoint: response.data.recipient, //:"BG-1303 SOFIA VENIX BG LTD ULBREGALNITSA 109 Appointment Time From: 19-Jan-2021 08:00:00 To: 19-Jan-2021 08:00:00"
							orderUnloadingDate: moment(response.data.date_delivery).isValid() ? moment(response.data.date_delivery).format() : undefined, //"19-Jan-2021 08:00:00"
							//importCustoms
							//importCustomsAgent
							emptiesReturnPoint: response.data.place_empty_container, //:"GR-54001 THESSALONIKI THESSALONIKI TERMINAL THESSALONIKI TERMINAL"
							//orderPaymentCurrency
							//orderPaymentAmount
							//payer
							//paymentDetails
							notes: response.data.remark, //"T1, MITNICA SOFIA ZAPAD - MILEV - 0888 477 030, RAZTOVARVA V DRAGICHEVO - BELCHINSKA - 0898 568809, TOCHEN ADRES SHTE PODADE BELCHINSKAKASA BAZA"
							//"order_no":"72309653"
							//"reference_id":"Truck plate no:"
							//"reference_value":"E3918"
							//"haulage_mode":"BGCAR TRANSLOGIC"
							//"principal":"MSL"
							//"eta":"210115"
							//"place":"SOFIA/BULGARIA"
							//"plr":"QINGDAO/CHINA"
							//"previous_port":"GRPIRTM/PIRAEUS TERMINAL/GREECE"
							//"port_discharge":"GRSLKTM THESSALONIKI TERMINAL"
							//"transport_by":"Sofia"
							//"transport_mode":"Truck"
							//"binumber":"204787048"
							//"booking_reference":"204787048"
							//"packages":"480"
							//"voyage":"STEEN"
						};
						this.props.actions.dispatchEditRESTData(this.props.dataComponentPath,importedData);
						//add container
//						"container":"GLDU5527757"
//						"seal":"CN1376307"
//						"type":"DRY"
//						"size":"20"
//						"tare":"2230"
						console.error("Change this implementation to call report builder");
						this.props.actions.fetchRESTFollow(
								{
									url: `${API_URL}/legalPersons?page=0&size=1&eik=DK53139655`,
								},
								this.props.componentPath+".orderOrderer",
								(response) => {
									let newData = response.data;
									//transform data to "repository response"-like
									builderDataToProjection(newData,"legalPersons");
									newData = newData._embedded.legalPersons[0];
									//if(newData) {newData._retrieveHref = fetch_url;};
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._links.orderOrderer.self.href",newData._links.self.href);
									if(newData == undefined) {
										newData = new Error(this.props.t("Error.CouldNotFindRecord")+this.props.headerText);
									}
									return newData;
								},
								'EmbedTransportOrderContainer.handleFileUpload',
								{}
							);
						console.error("Change this implementation to call report builder");
						this.props.actions.fetchRESTFollow(
								{
									url: `${API_URL}/legalPersons?page=0&size=1&eik=DK53139655`,
								},
								this.props.componentPath+".sender",
								(response) => {
									let newData = response.data;
									//transform data to "repository response"-like
									builderDataToProjection(newData,"legalPersons");
									newData = newData._embedded.legalPersons[0];
									//if(newData) {newData._retrieveHref = fetch_url;};
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._links.sender.self.href",newData._links.self.href);
									if(newData == undefined) {
										newData = new Error(this.props.t("Error.CouldNotFindRecord")+this.props.headerText);
									}
									return newData;
								},
								'EmbedTransportOrderContainer.handleFileUpload',
								{}
							);
					} else if(fileType == 2) {
						let importedData = {
								...this.props.data,
								[Constants.PATH_FOR_LOADING]: undefined,
								orderDate: moment(response.data.date, "DD.MM.YYYY").isValid() ? moment(response.data.date, "DD.MM.YYYY").format() : undefined, //"17.09.2020"//"2017-11-01T10:02:26.731+00:00"
								//transporter
								//orderOrderer 
								//sender
								senderContact: response.data.from+" "+response.data.email, //"Мария Минковска" "m.minkovska@bonmar.bg"
								//orderRoute
								//loadingPoint
								orderLoadingDate: moment(response.data.date_departure, "DD.MM.YYYY").isValid() ? moment(response.data.date_departure, "DD.MM.YYYY").format() : undefined, //"22.09.2020"
								//exportCustoms
								//exportCustomsAgent
								cargo: response.data.container, //"MIEU0029348 / 40HC / 9375кг / MBL:"
								//orderContents
								//weight
								//loadingWarehouseRef
								receiver: response.data.recipient, //"203817977"
								receiverContact: response.data.contact, //"Лора - +359878489328"
								unloadingPoint: response.data.delivery_address, // "БГ СИТИ ФЕШЪН;"
								orderUnloadingDate: moment(response.data.date_delivery, "DD.MM.YYYY").isValid() ? moment(response.data.date_delivery, "DD.MM.YYYY").format() : undefined, //"23.09.2020"
								importCustoms: response.data.receipient_customs, //"BG005100– Аерогара София, Марияна Григорова 0897 978670 Камионът да се яви на терминала"
								importCustomsAgent: response.data.agent, //"ARKAS LOGISTICS S.A. / Thessaloniki Office Tel : +30 2310 519 339 2310 543653 ext: 204 Address : 43 26th October st., 546 27 Thessaloniki GREECE"
								emptiesReturnPoint: response.data.place_empty_container, //"Солун"
								//orderPaymentCurrency
								orderPaymentAmount: response.data.navlo, //"ЕUR 480 / КОНТЕЙНЕР"
								payer: response.data.payer_navlo, //"Бон Марин ООД"
								//paymentDetails
								notes: response.data.remark, //"ПРАЗНИТЕ КОНТЕЙНЕРИ ДА СЕ ВЪРНАТ ВЕДНАГА ОБРАТНО В СОЛУН. ДА СЕ ФАКТУРИРА ДИРЕКТНО КЪМ БОН МАРИН ООД"
								//"conosament":"1."
								//"company_to":"ТРАНС ЛОДЖИК ГРУП ЕООД"
								//"to":"г-н Апостол Стоименов"
								//"subject":"40’HC"
								//"company_from":"Бон Марин ООД"
							}
						this.props.actions.dispatchEditRESTData(this.props.dataComponentPath,importedData);
						console.error("Change this implementation to call report builder");
						this.props.actions.fetchRESTFollow(
								{
									url: `${API_URL}/legalPersons?page=0&size=1&eik=103612886`,
								},
								this.props.componentPath+".orderOrderer",
								(response) => {
									let newData = response.data;
									//transform data to "repository response"-like
									builderDataToProjection(newData,"legalPersons");
									newData = newData._embedded.legalPersons[0];
									//if(newData) {newData._retrieveHref = fetch_url;};
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._links.orderOrderer.self.href",newData._links.self.href);
									if(newData == undefined) {
										newData = new Error(this.props.t("Error.CouldNotFindRecord")+this.props.headerText);
									}
									return newData;
								},
								'EmbedTransportOrderContainer.handleFileUpload',
								{}
							);
						console.error("Change this implementation to call report builder");
						this.props.actions.fetchRESTFollow(
								{
									url: `${API_URL}/legalPersons?page=0&size=1&eik=103612886`,
								},
								this.props.componentPath+".sender",
								(response) => {
									let newData = response.data;
									//transform data to "repository response"-like
									builderDataToProjection(newData,"legalPersons");
									newData = newData._embedded.legalPersons[0];
									//if(newData) {newData._retrieveHref = fetch_url;};
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._links.sender.self.href",newData._links.self.href);
									if(newData == undefined) {
										newData = new Error(this.props.t("Error.CouldNotFindRecord")+this.props.headerText);
									}
									return newData;
								},
								'EmbedTransportOrderContainer.handleFileUpload',
								{}
							);
					} else if(fileType == 3) {
						let importedData = {
							...this.props.data,
							[Constants.PATH_FOR_LOADING]: undefined,
							orderDate: moment(response.data.date, "DD.MM.YYYY").isValid() ? moment(response.data.date, "DD.MM.YYYY").format() : undefined, //"08.01.2021",
							//transporter //"company_to":"TRANS LOGIC COMPANY EOOD",
							//orderOrderer 
							//sender //"company_from":"UNIMASTERS LOGISTICS SCS LTD."
							//senderContact
							//orderRoute
							loadingPoint: response.data.port_discharge, //"Солун",
							//orderLoadingDate: 
							//exportCustoms
							//exportCustomsAgent
							cargo: response.data.type + " " + response.data.container, //"40 HC High Cube" "BMOU5305940"
							orderContents: response.data.goods, //"разно"
							weight: parseFloat(response.data.weight), //"14500",
							//loadingWarehouseRef
							receiver: response.data.recipient, //"Юнимастърс Лоджистикс",
							receiverContact: response.data.contact, //"0887/472210",
							unloadingPoint: response.data.delivery_address, //"ул. Продан Таракчиев 12, склад Юрогейт София",
							orderUnloadingDate: moment(response.data.date_delivery, "DD.MM.YYYY HH:mm").isValid() ? moment(response.data.date_delivery, "DD.MM.YYYY HH:mm").format() : undefined, //"12.01.2021 09:00",
							//importCustoms
							//importCustomsAgent
							emptiesReturnPoint: response.data.place_empty_container, //"Солун",
							//orderPaymentCurrency
							//orderPaymentAmount
							//payer
							//paymentDetails
							notes: response.data.remark //"ПЪЛЕН СЕВЗЕМА ОТ: PORT OFFICE 44- KASIDOPOULOS ПОСЛЕ ПО НАРЯД. ПРАЗНИЯТ ДА СЕ ВЪРНЕ ВЕДНАГА СЛЕД РАЗТОВАРВАНЕ НА TSOUREKAS DEPOT - EVERGREEN, THESSALONIKI! ДА СЕ ПОДПЕЧАТА ЧМР ЗА ПРАЗНИЯТ!!!",

//"container":"BMOU5305940",
//"line":"EVERGREEN LINE",
//"type":"40 HC High Cube",
//"trailer":"E4484EA",
//"driver":"Николай Иванов Игов",
//"schlepp":"CA6386CH",

						};
						this.props.actions.dispatchEditRESTData(this.props.dataComponentPath,importedData);
						console.error("Change this implementation to call report builder");
						this.props.actions.fetchRESTFollow(
								{
									url: `${API_URL}/legalPersons?page=0&size=1&eik=831430171`,
								},
								this.props.componentPath+".orderOrderer",
								(response) => {
									let newData = response.data;
									//transform data to "repository response"-like
									builderDataToProjection(newData,"legalPersons");
									newData = newData._embedded.legalPersons[0];
									//if(newData) {newData._retrieveHref = fetch_url;};
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._links.orderOrderer.self.href",newData._links.self.href);
									if(newData == undefined) {
										newData = new Error(this.props.t("Error.CouldNotFindRecord")+this.props.headerText);
									}
									return newData;
								},
								'EmbedTransportOrderContainer.handleFileUpload',
								{}
							);
						console.error("Change this implementation to call report builder");
						this.props.actions.fetchRESTFollow(
								{
									url: `${API_URL}/legalPersons?page=0&size=1&eik=831430171`,
								},
								this.props.componentPath+".sender",
								(response) => {
									let newData = response.data;
									//transform data to "repository response"-like
									builderDataToProjection(newData,"legalPersons");
									newData = newData._embedded.legalPersons[0];
									//if(newData) {newData._retrieveHref = fetch_url;};
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._links.sender.self.href",newData._links.self.href);
									if(newData == undefined) {
										newData = new Error(this.props.t("Error.CouldNotFindRecord")+this.props.headerText);
									}
									return newData;
								},
								'EmbedTransportOrderContainer.handleFileUpload',
								{}
							);
					} else if(fileType == 10001) {
						let importedData = {
							...this.props.data,
							[Constants.PATH_FOR_LOADING]: undefined,
							orderDate: moment().format(), //"08.01.2021",
							//transporter 
							//orderOrderer 
							//sender "sender":"Medcargo D. Theodorikas Shipping SA",
							senderContact: response.data.contact, //"Mr. Mimis, 00302310542674",
							//orderRoute
							loadingPoint: response.data.port_discharge, //"пристанище Солун, митнически брокер Medcargo",
							orderLoadingDate: moment(response.data.date_departure, "DD/MM/YYYY, HH:mm").isValid() ? moment(response.data.date_departure, "DD/MM/YYYY, HH:mm").format() : undefined, //" 22/04/2020, 09.00 часа ",
							exportCustoms: response.data.export_customs, //" ",
							//exportCustomsAgent
							cargo: response.data.container, //"контейнер CSNU1583638/20’dc",
							orderContents: response.data.goods, //"инструменти",
							weight: parseFloat(response.data.weight), //"19877.20 кг",
							loadingWarehouseRef: response.data.reference_number, //"контейнер CSNU1583638/20’dc",
							receiver: response.data.recipient, //"Дженерал Дистрибюшън АД",
							//receiverContact: 
							unloadingPoint: response.data.delivery_address, //"гр. София, Околовръстен път 454, 1532 Казичене",
							orderUnloadingDate: moment(response.data.date_delivery, "DD/MM/YYYY, HH:mm").isValid() ? moment(response.data.date_delivery, "DD/MM/YYYY, HH:mm").format() : undefined, //"23/04/2020, 09:00 часа",
							importCustoms: response.data.receipient_customs, //"София-Запад / BG005807",
							//importCustomsAgent
							//emptiesReturnPoint: 
							//orderPaymentCurrency
							orderPaymentAmount: response.data.navlo, //"Страните се споразумяха за цена на транспорта: 997,47 лв без ДДС на контейнер"
							//payer 
							paymentDetails: response.data.remark, //"По банков път, 25 работни дни от дата на фактурата за транспорт, издадена в месеца на данъчното събитие, и предоставени оригинални фактура и 2 екземпляра подписани и подпечатани CMR товарителници.",
							//notes: 

							//"container":"контейнер CSNU1583638/20’dc",
							//"company_to":"Транс Лоджик Груп ЕООД - с. Ново Делчево, обл. Благоевград, ул. Славянска №19, Булстат: 204074306, номер от НДР ......................, представлявано от Апостол Стоименов",
							//"type":"1x20’dc ",
							//"vehicle":"Е7585КС/E2271EA - 1 бр. контейнеровоз",
							//"agent":"Mr. Mimis, 00302310542674",
							//"company_from":"\"Орбит\" ЕООД - София, ул. Продан Таракчиев № 16, 1540 София, ИН 121184049, ИН по ЗДДС BG121184049, МОЛ Коциас Зисис, тел. 02/970 6300, факс 02/970 6333, e-mail: truck@orbit.bg (​mailto:truck@orbit.bg​) ",
						};
						this.props.actions.dispatchEditRESTData(this.props.dataComponentPath,importedData);
						console.error("Change this implementation to call report builder");
						this.props.actions.fetchRESTFollow(
								{
									url: `${API_URL}/legalPersons?page=0&size=1&eik=121184049`,
								},
								this.props.componentPath+".orderOrderer",
								(response) => {
									let newData = response.data;
									//transform data to "repository response"-like
									builderDataToProjection(newData,"legalPersons");
									newData = newData._embedded.legalPersons[0];
									//if(newData) {newData._retrieveHref = fetch_url;};
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._links.orderOrderer.self.href",newData._links.self.href);
									if(newData == undefined) {
										newData = new Error(this.props.t("Error.CouldNotFindRecord")+this.props.headerText);
									}
									return newData;
								},
								'EmbedTransportOrderContainer.handleFileUpload',
								{}
							);
						console.error("Change this implementation to call report builder");
						this.props.actions.fetchRESTFollow(
								{
									url: `${API_URL}/legalPersons?page=0&size=1&eik=121184049`,
								},
								this.props.componentPath+".sender",
								(response) => {
									let newData = response.data;
									//transform data to "repository response"-like
									builderDataToProjection(newData,"legalPersons");
									newData = newData._embedded.legalPersons[0];
									//if(newData) {newData._retrieveHref = fetch_url;};
									this.props.actions.dispatchEditRESTData(this.props.componentPath+"._links.sender.self.href",newData._links.self.href);
									if(newData == undefined) {
										newData = new Error(this.props.t("Error.CouldNotFindRecord")+this.props.headerText);
									}
									return newData;
								},
								'EmbedTransportOrderContainer.handleFileUpload',
								{}
							);
					}
				}
			})
			.catch(error => {
				this.props.actions.dispatchEditRESTData(this.props.dataComponentPath,error);
			});
			this.props.actions.dispatchEditRESTData(this.props.dataComponentPath+"."+Constants.PATH_FOR_LOADING,promise);
		}).bind(this);
		reader.readAsArrayBuffer(file);
	}

	render() {
		return (<EmbedEntityContainer
					componentPath={this.props.componentPath}
					retrieve_id={this.props.retrieve_id}
					entityName={this.props.entityName}
					headerText={this.props.t("TransportOrder._className")}
					creatable={true}
					expanded={this.props.expanded}
					loading={this.props.loading}
					onBeforeChange={this.props.onBeforeChange}
					onChange={this.props.onChange}
					placeholderBeforeBody={(props, state) => state.editable && props.retrieve_id == 'add' ?
						<Form>
							<Form.Row>
								<Form.Group className="m-2">
									<label className="btn btn-outline-dark" htmlFor="file1">
										<FontAwesomeIcon icon="upload"/>
										&nbsp;{this.props.t("TransportOrder.Import from Maersk")}
									</label>
									<input type="file" name="file1" id="file1" className="inputfile" onChange={(ev) => {this.handleFileUpload(ev,1); ev.target.value = '';}} />
								</Form.Group>
								<Form.Group className="m-2">
									<label className="btn btn-outline-dark" htmlFor="file2">
										<FontAwesomeIcon icon="upload"/>
										&nbsp;{this.props.t("TransportOrder.Import from Bon Marine")}
									</label>
									<input type="file" name="file2" id="file2" className="inputfile" onChange={(ev) => {this.handleFileUpload(ev,2); ev.target.value = '';}} />
								</Form.Group>
								<Form.Group className="m-2">
									<label className="btn btn-outline-dark" htmlFor="file3">
										<FontAwesomeIcon icon="upload"/>
										&nbsp;{this.props.t("TransportOrder.Import from Unimasters")}
									</label>
									<input type="file" name="file3" id="file3" className="inputfile" onChange={(ev) => {this.handleFileUpload(ev,3); ev.target.value = '';}} />
								</Form.Group>
								<Form.Group className="m-2">
									<label className="btn btn-outline-dark" htmlFor="file10001">
										<FontAwesomeIcon icon="upload"/>
										&nbsp;{this.props.t("TransportOrder.Import from Orbit")}
									</label>
									<input type="file" name="file10001" id="file10001" className="inputfile" onChange={(ev) => {this.handleFileUpload(ev,10001); ev.target.value = '';}} />
								</Form.Group>
							</Form.Row>
						</Form>
						: ''
					}
				/>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = "transportOrders";
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchREST
	const dataComponentPath = ownProps.componentPath+"._embedded."+entityName+".0";
	return {
		//storage
		data: data,
		loading: ownProps.loading != undefined ? ownProps.loading : (data ? data[Constants.PATH_FOR_LOADING] : undefined),
		componentPath: ownProps.componentPath,
		dataComponentPath: dataComponentPath,
		entityName: entityName,
		//data
		retrieve_id: ownProps.retrieve_id,
		onBeforeChange: ownProps.onBeforeChange ? ownProps.onBeforeChange : () => {},
		onChange: ownProps.onChange ? ownProps.onChange : () => {},
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData, dispatchEditRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedTransportOrderContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
