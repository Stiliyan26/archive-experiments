import React from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import axios from 'axios';
import { CSVReader } from 'react-papaparse'
//import querystring from 'querystring'
import ReactTable from 'react-table-v6'
import classNames from 'classnames'
import { Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import moment from 'moment';
import { withTranslation } from 'react-i18next';

import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import NewHeader from '../../components/generic/NewHeader.js';
import Select from '../OverflowSelect'
import { getCombinedEntityDefinitions, getEntityDefinition, getExpandedColumns, retrieveData } from '../nomenclatures/entityDefinitions.js'
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'
import EmbedEntityListOrTableContainer from '../nomenclatures/EmbedEntityListOrTableContainer'
import { resolveObjectPath, getNomenclatureByCode, getNomenclatureByCriteria } from '../../scripts/dataUtils';
import { dispatchEditRESTData, fetchRESTFollow, resetRESTCallLimit, postRESTData } from '../../actions/taskActions';

import LoadXlsxContainer from './LoadXlsxContainer'

//TODO Error handling
const refCSVReader = React.createRef();

const partnersMapping = [
	{ input: 'Company', accessor: 'name' },
	{ input: 'Country', accessor: 'address' },
	{ input: 'City', accessor: 'address' },
	{ input: 'mails', accessor: 'email'},
	{ input: 'link', accessor: 'webSite'}
]
	// "link",
	// "remark",
	// "Column2",
	// "mails",
	// "in buletin as from"

class PageImportContainer extends React.Component {

	constructor(props){
		super(props);
		this.state = {
			selectedImportType: this.props.match.params.selectedImportType,
			mapped: [],
		};
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}
	
	handleMt940FileUpload({ target }, fileType) {
		const files = target.files;
		const file = files[0];
		let self = this;

		var reader = new FileReader();
		reader.onload = (function(e){
//			let axios_instance = axios.create({
//					headers: {'Accept': '*/*',
//						'Authorization': 'Basic '+"YWRtaW46YWRtaW4=",
//					}
//				});
//			delete axios_instance.defaults.headers.common["X-CSRF"];
			const FormData = require('form-data');
			const form = new FormData();
			form.append('file', file);
			form.append('bankName', "DSK");
//			axios_instance.post("https://services.latona.eu"+":8243/service/bank/v0/bankFile/parse",form).then(response => {
			axios({
				method: 'post',
				url: API_URL+"/service/bankFile",
				headers: {Authorization: sessionStorage["X-AUTH-TOKEN"]},
				data: form
			}).then(response => {
				if(response && response.data) {
					console.log("MT940 response data: ", response.data);
					let importedData = response.data;
					if(importedData instanceof Array) {
						let mappedAndTrimmed = [];
						let bankAccountsPromise = retrieveData("bankAccounts");
						let currenciesPromise = retrieveData("currencies");
						Promise.all([bankAccountsPromise,currenciesPromise]).then((results) => {
							let bankAccountsData = results[0];
							let currenciesData = results[1];
							//now the bank accounts should be loaded
							importedData.forEach(bankRow => {
								if(bankRow.transactionSide == "C") {
									//payment detected
									let resultObj = {};
									resultObj.name = bankRow.uniqueIdentifier;
									resultObj.incomeDesc = bankRow.description;
									resultObj.incomeDate = moment(bankRow.transactionDate, "DD.MM.YYYY").toISOString();
									let bankAccount = (bankRow.corespondentIBAN && bankRow.corespondentIBAN != "" ? 
											bankAccountsData._embedded.bankAccounts.find((acc) => bankRow.corespondentIBAN.includes(acc.iban))
											: undefined);
									if(bankAccount) {
										resultObj.incomePayer = bankAccount.bankAccountOwner;
										resultObj._links = {
												...resultObj._links,
												incomePayer: {
													href: resultObj.incomePayer._links.self.href
												}
											};
									}
									resultObj.attachableRevenuesAndExpenses = {};
									resultObj.attachableRevenuesAndExpenses.ammount = bankRow.amount;
									if(bankRow.ownerAccountCurrency) {
										let result = currenciesData._embedded.currencies.find((elem) => (elem.name == bankRow.ownerAccountCurrency));
										resultObj.attachableRevenuesAndExpenses.article = result;
										resultObj.attachableRevenuesAndExpenses._links = {
											...resultObj.attachableRevenuesAndExpenses._links,
											article: {
												href: resultObj.attachableRevenuesAndExpenses.article._links.self.href
											}
										};
									}
									resultObj._editable = true;
									mappedAndTrimmed.push(resultObj);
								}
							});
							//console.log("mappedAndTrimmed",mappedAndTrimmed);
							self.setState({ mapped: mappedAndTrimmed });
							//TODO change to remove pageData._embedded
							self.props.actions.dispatchEditRESTData(self.props.componentPath,{[self.state.selectedImportType]: {pageData: {_embedded: {["incomes"]: self.state.mapped}}}});
						});
					}
					
					this.props.actions.dispatchEditRESTData(this.props.componentPath+"._importedData",importedData);
				}
			})
			.catch(error => {
				console.warn('REST error',error);
			});
		}).bind(this);
		reader.readAsArrayBuffer(file);
	}
	
	//https://github.com/SheetJS/SheetJS.github.io/blob/master/assets/js/dropsheet.js
	fixdata(data) {
		var o = "", l = 0, w = 10240;
		for(; l<data.byteLength/w; ++l) {
			o+=String.fromCharCode.apply(null,new Uint8Array(data.slice(l*w,l*w+w)));
		}
		o+=String.fromCharCode.apply(null, new Uint8Array(data.slice(o.length)));
		return o;
	}

	async handleOnFileLoad(data, definitions) {
		let self = this;
		console.log('---------------------------');
		console.log(data);
		console.log('---------------------------');

		// File headers
		let inputHeaders = data[0].data;
		let filteredData = data.filter((row,index) => {
			if(index == 0) return false; //header row
			if(row.data.length == 1 && row.data[0] == "") return false; //empty row
			return true;
		});
		let mappedData = []
		// Entity headers as described in the entity definition
		const entityHeaders = getEntityDefinition(self.state.selectedImportType).columns.slice();
		//const entityHeaders = definitions[self.state.selectedImportType].columns.slice();
		
		//console.log(entityHeaders,getExpandedColumns(this.state.selectedImportType, undefined),getEntityDefinition(self.state.selectedImportType).columns);

		let childHeaders = []
		let entityHeadersToFilter = entityHeaders.filter((x) => {
			return x.dataType == 'ENTITY' || x.dataType == 'FILTERED_ENTITY';
		})

		for (var i = 0; i < entityHeadersToFilter.length; i++) {
			//let childDefCols = definitions[entityHeadersToFilter[i].entityType].columns.slice();
			entityHeadersToFilter[i]._definition = getEntityDefinition(entityHeadersToFilter[i].entityType);
			let childDefCols = entityHeadersToFilter[i]._definition.columns.slice();
			for (var k = 0; k < childDefCols.length; k++) {
				let childCol = childDefCols[k]
				if(!entityHeaders.some((col) => { col.accessor == childCol.accessor})){
					entityHeaders.push(childCol)
				}
			}
			// let cols = definitions[i].columns
		}
		
		//console.log("entityHeaders",entityHeaders);

		for (let headerIndex = 0, len = inputHeaders.length; headerIndex < len; headerIndex++) {
			let inputHeader = inputHeaders[headerIndex]
			// look for an exact corresponding header
			let definitionHeader = entityHeaders.filter((entityHeader) => {return entityHeader.Header && inputHeader && entityHeader.Header.toString().trim() == inputHeader.toString().trim()})[0]

			// if no exact corresponding header is found => use a hardcoded custom mapping
			if(!definitionHeader){
				definitionHeader = entityHeaders.filter((entityHeader) => {
						let foundMappingHeader = partnersMapping.filter((mapping) => {
							return mapping.input == inputHeader.trim()
						})
						// if(foundMappingHeader && foundMappingHeader[0]){
						// }
						return (foundMappingHeader && foundMappingHeader[0] && foundMappingHeader[0].accessor == entityHeader.accessor)
					})[0]
			}

			for (var row = 0, dataLen = filteredData.length; row < dataLen; row++) {
				if(!mappedData[row]){
					mappedData[row] = {}
				}
				if(definitionHeader){
					//console.log("definitionHeader",definitionHeader);
					if(definitionHeader.dataType == 'ENTITY' || definitionHeader.dataType == 'FILTERED_ENTITY') {
						mappedData[row][definitionHeader.accessor] = { 
							definition: definitionHeader, 
							value: await getNomenclatureByCriteria(definitionHeader.entityType, (elem) => (elem[definitionHeader._definition && definitionHeader._definition.displayAttr ? definitionHeader._definition.displayAttr : "name"] == filteredData[row].data[headerIndex]), this.props.rest, this.props.actions.fetchRESTFollow, this.props.actions.dispatchEditRESTData)
						};
						//console.log(mappedData[row][definitionHeader.accessor]);
					} else if(definitionHeader.dataType == 'DATE') {
						mappedData[row][definitionHeader.accessor] = { 
							definition: definitionHeader, 
							value: moment(filteredData[row].data[headerIndex],["DD.MM.YYYY г.","DD.MM.YYYY","YYYY-MM-DD","YYYYMMDD","MM/DD/YYYY",moment.ISO_8601,"YYYY/MM/DD HH:mm:ss ZZ","YYYY-MM-DDTHH:mm:ss","DD.MM.YYYY HH:mm"]).format("YYYY-MM-DD")
						};
					} else if(definitionHeader.dataType == 'UNIT') {
						let value = Number.parseFloat(filteredData[row].data[headerIndex].toString().replace(",","."));
						//console.log("value",value,filteredData[row].data[headerIndex]);
						mappedData[row][definitionHeader.accessor] = { 
							definition: definitionHeader, 
							value: value
						};
					} else {
						if(!mappedData[row][definitionHeader.accessor]){
							// set initial value in order to enable the concat afterwards
							mappedData[row][definitionHeader.accessor] = { definition: definitionHeader, value: ''}
						}
						// TODO: update the implementation to use Array.join instead of plain string concat
						let oldValue = mappedData[row][definitionHeader.accessor].value
						let newValue = filteredData[row].data[headerIndex]
						if(oldValue && newValue){
							oldValue += ', '
						}
						mappedData[row][definitionHeader.accessor] = { definition: definitionHeader, value: oldValue + newValue }
					}
				}
			}
		}
		//console.log("mappedData",mappedData);
		// Preparind the entities information for table representation and for the Post calls afterwards
		let mappedAndTrimmed = mappedData.map(
			(row) => {
				let resultObj = {}
				let keys = Object.keys(row)
				for (let key of keys){
					if(!resultObj[key]){
						resultObj[key] = []
					}
					if(row[key] && row[key].value){
						if(row[key].value instanceof Object) {
							resultObj[key] = row[key].value;
							resultObj._links = {...resultObj._links, [key]: {href: row[key].value._links.self.href}};
							continue;
						}
						resultObj[key].push(row[key].value)
					}
					resultObj[key] = resultObj[key].toString();
				}
				resultObj._editable = true;
				return resultObj
			}
		)
		self.setState({ mapped: mappedAndTrimmed });
		self.props.actions.dispatchEditRESTData(self.props.componentPath,{[self.state.selectedImportType]: self.state.mapped});
	};

	handleOnError(err, file, inputElem, reason) {
		console.log('---------------------------');
		console.log(err);
		console.log('---------------------------');
	};

	handleOnRemoveFile(data) {
		this.props.actions.dispatchEditRESTData(this.props.componentPath,{[this.state.selectedImportType]: [{_editable: true}]});
	};

	submitEntity(definitions, definition, endpoint, entity){
		let self = this
		let parentPromise
		let parentGeneratedEntity
		let parentFirstCols = definition.columns.filter((col) => { return col.dataType == 'ENTITY' && col.mappedBy})
		let childFirstCols = definition.columns.filter((col) => { return col.dataType == 'ENTITY' && !col.mappedBy})

		resetRESTCallLimit();

		if(childFirstCols.length > 0){
			for (var i = 0; i < childFirstCols.length; i++) {
				let col = childFirstCols[i]
				let childDefinition = definitions[col.entityType]
				let childEndpoint = childDefinition.endpoint || col.entityType


				let childEntity = {code: Math.floor(Math.random()*10000)}
				let childPromise = self.submitEntity(definitions, childDefinition, childEndpoint, childEntity)

				childPromise.then(({ childData }) => {
					let targetProp = definition.columns.find((col) => { return col.entityType == childEndpoint })

					entity[targetProp.accessor] = `${API_URL}/$${childEndpoint}/${childData.id}`
				})
			}
		}

		if(parentFirstCols.length > 0){
			parentPromise = self.props.actions.postRESTData({
							method: 'post',
							url: `${API_URL}/${endpoint}/`,
							data: entity
						},
						self.props.componentPath,
						'PageImportContainer.submitEntity'
					)
		}
		if(parentPromise){
			parentPromise.then(({ data }) => {
				parentGeneratedEntity = data
				for (var i = 0; i < parentFirstCols.length; i++) {
					let col = parentFirstCols[i]
					let childDefinition = definitions[col.entityType]
					let childEndpoint = childDefinition.endpoint || col.entityType

					let targetProp = childDefinition.columns.find((col) => {
						return col.entityType == endpoint
					})
					let childEntity = {
						[targetProp.accessor] : `${API_URL}/${endpoint}/${parentGeneratedEntity.id}`
					}
					self.submitEntity(definitions, childDefinition, childEndpoint, childEntity)
				}
			})
		}
		else{
			// executed both when there are child first and when there is no references at all
			parentPromise = self.props.actions.postRESTData({
							method: 'post',
							url: `${API_URL}/${endpoint}/`,
							data: entity
						},
						self.props.componentPath,
						'PageImportContainer.submitEntity'
					)
		}
		return parentPromise
	}

	saveGeneralChanges(definitions){
		const { mapped, selectedImportType } = this.state
		const entityDefinition = definitions[selectedImportType]
		let promises = []
		let self = this
		// post every mapped entity separately
		// TODO: enable posting of multiple entities at once
		for (var i = 0; i < mapped.length; i++) {
			let entity = mapped[i]
			let endpoint = entityDefinition.endpoint || selectedImportType
			promises.push( self.submitEntity(definitions, entityDefinition, endpoint, entity) )
			// wait till all promises are resolved, then replace the plain objects in the state with the once returned from the backend
			// the ID prop of the returned objects is crucial for marking them as submitted in the UI table
			Promise.all(promises).then((entities) => {
				this.setState({
					mapped: entities.map((entity) => entity.data)
				})
			})
		}
	}

	saveLegalPersonsPotential(definitions){
		const { mapped, selectedImportType } = this.state
		const { rest, actions } = this.props
		const entityDefinition = definitions[selectedImportType]
		let mappedData = mapped.slice()
		let promises = []
		let self = this
		let defaultValuePromises = []
		let legalStatusesDefault = getNomenclatureByCode('legalStatuses', 6, rest, actions.fetchRESTFollow, actions.dispatchEditRESTData) // Други
		let salesStagesDefault = getNomenclatureByCode('salesStages', 1, rest, actions.fetchRESTFollow, actions.dispatchEditRESTData) // Непознат
		let contactTypesDefault = getNomenclatureByCode('contactTypes', 1, rest, actions.fetchRESTFollow, actions.dispatchEditRESTData) // Основен

		let directionCategoryDefault = getNomenclatureByCode('directionCategories', 3, rest, actions.fetchRESTFollow, actions.dispatchEditRESTData) // Общи
		let areaCategoryDefault = getNomenclatureByCode('areaCategories', 11, rest, actions.fetchRESTFollow, actions.dispatchEditRESTData) // София
		let businessCategoryDefault = getNomenclatureByCode('businessCategories', 32, rest, actions.fetchRESTFollow, actions.dispatchEditRESTData) // Производство
		let generalCategoryDefault = getNomenclatureByCode('generalCategories', 6, rest, actions.fetchRESTFollow, actions.dispatchEditRESTData) // Клиент
		defaultValuePromises.push(legalStatusesDefault)
		defaultValuePromises.push(salesStagesDefault)
		defaultValuePromises.push(contactTypesDefault)
		defaultValuePromises.push(directionCategoryDefault)
		defaultValuePromises.push(areaCategoryDefault)
		defaultValuePromises.push(businessCategoryDefault)
		defaultValuePromises.push(generalCategoryDefault)

		Promise.all(defaultValuePromises).then((values) => {
			legalStatusesDefault = values[0]
			salesStagesDefault = values[1]
			contactTypesDefault = values[2]
			directionCategoryDefault = values[3]
			areaCategoryDefault = values[4]
			businessCategoryDefault = values[5]
			generalCategoryDefault = values[6]

			// post every mapped entity separately
			// TODO: enable posting of multiple entities at once
			for (var i = 0; i < mappedData.length; i++) {
				let entity = Object.assign({}, mappedData[i])

				let cherryPickedLegalPerson = {
					address: entity.address,
					name: entity.name,
					eik: 9999999999,
					country: "test",
					mol: "test",
					direction: `${API_URL}/directionCategories/${directionCategoryDefault.id}`,
					area: `${API_URL}/areaCategories/${areaCategoryDefault.id}`,
					business: `${API_URL}/businessCategories/${businessCategoryDefault.id}`,
					generalCategory: `${API_URL}/generalCategories/${generalCategoryDefault.id}`,
					legalStatus: `${API_URL}/legalStatuses/${legalStatusesDefault.id}`
				}

				let endpoint = entityDefinition.endpoint || selectedImportType
				promises.push(actions.postRESTData({
							method: 'post',
							url: `${API_URL}/${endpoint}/`,
							data: cherryPickedLegalPerson
						},
						self.props.componentPath,
						'PageImportContainer.saveLegalPersonsPotential',
						undefined,
						(data) => {
							if(!data){
								throw new Error(this.props.t("Import.ImportFailed"))
								return ''
							}

							let url = `${API_URL}/contacts/`
							actions.postRESTData({
								method: 'post',
								url: url,
								data: {
									person: `${API_URL}/legalPersons/${data.id}`,
									type: `${API_URL}/contactTypes/${contactTypesDefault.id}`,
									email: entity.email,
									webSite: entity.webSite
								}
							})
						}
					)
				)
				// wait till all promises are resolved, then replace the plain objects in the state with the once returned from the backend
				// the ID prop of the returned objects is crucial for marking them as submitted in the UI table
				Promise.all(promises).then((entities) => {
					for (var i = 0; i < entities.length; i++) {
						if(!entities[i] || !entities[i].data || !entities[i].data.id){
							entities[i] = {data: Object.assign({error: true}, mappedData[i])}
						}
					}

					this.setState({
						mapped: entities.map((ent) => {return ent.data})
					})
				})
			}

		})

	}

	selectImportType(e){
		resetRESTCallLimit(); 
		history.push('/import/'+e.value);
		let importType = e ? e.value : e
		this.setState({
			selectedImportType: importType,
			mapped: []
		});
		// if(importType){
		// 	// fetch all entities of the selected kind for comparison afterwards
		// 	// TODO: decide how to compare the present and imported entities
		// 	this.props.actions.fetchRESTFollow(
		// 		{
		// 			url: `${API_URL}/${importType}`,
		// 			params: {
		// 				// ...filter_param,
		// 				// page: pageIndex,
		// 				projection: 'allRefData',
		// 				size: 150000000,
		// 				sort: 'asc' //TODO implement multi-sort
		// 			},
		// 			paramsSerializer: function(params) {
		// 				//needed for the from-to dates
		// 				return querystring.stringify(params)
		// 			}
		// 		},
		// 		`${importType}Data.data`,
		// 		(response) => ({
		// 			...response.data,
		// 			sort: 'asc',
		// 			filter: undefined,
		// 		}),
		//		'selectImportType',
		// 		{}
		// 	);
		// }
	}

	// used for marking the already submitted entities in the UI table
	getTrProps (state, rowInfo, instance){
		if (rowInfo) {
			let bgColor = 'transparent'
			if(rowInfo.original.error){
				bgColor = '#ff5959' // red
			}
			else if(rowInfo.original.id){
				bgColor = '#4de74d' // green
			}
			return {
				style: {
					background: bgColor,
					color: 'black'
				}
			}
		}
		return {};
	}

	render() {
		const nomenclatures = [];

		let definitions = {}
		definitions['legalPersonsPotential']= {
			//className: "LegalPerson",
			endpoint: 'legalPersons',
			type: 'custom',
			label: this.props.t("Import.PotentialClients"),
			columns: [
						{
							Header: this.props.t("LegalPerson.name"),
							accessor: 'name',
							fluidSize: 2,
							dataType: "TEXT",
						}, {
							Header: this.props.t("LegalPerson.legalStatus"),
							accessor: 'legalStatus',
							dataType: "ENTITY",
							entityType: "legalStatuses",
						}, {
							Header: this.props.t("LegalPerson.egn"),
							accessor: 'egn',
							fluidSize: 2,
							dataType: "TEXT",
						}, {
							Header: this.props.t("LegalPerson.eik"),
							accessor: 'eik',
							fluidSize: 2,
							dataType: "TEXT",
						}, {
							Header: this.props.t("LegalPerson.address"),
							accessor: 'address',
							fluidSize: 2,
							dataType: "TEXT",
						},{
							Header: this.props.t("LegalPerson.contacts"),
							accessor: 'contacts',
							show: false,
							fluidSize: 2,
							dataType: "ENTITY",
							entityType: "contacts",
							mappedBy: "person",
						}
					]
		}
		/*
		//too slow, don't use
		definitions["nepalProduced"] = {
			_importFileType: "xlsx",
			_importEntityDef: "powerPlantProducedSchedules",
			_importTemplateName: "nepalProduced",
			columns: getExpandedColumns("powerPlantProducedSchedules", []),
			label: this.props.t("*PowerPlantProducedSchedule"),
		}
		definitions["eurobank_movements"] = {
			_importFileType: "xlsx",
			_importEntityDef: "incomes",
			columns: getExpandedColumns("incomes", ["attachableRevenuesAndExpenses"]),
			label: this.props.t("Import.EurobankAccountMovements"),
		}
		definitions["mt940"] = {
				_importFileType: "mt940",
				_importEntityDef: "incomes",
				columns: getExpandedColumns("incomes", ["attachableRevenuesAndExpenses"]),
				label: this.props.t("Import.BankFileImport"),
			}
		*/
		definitions = Object.assign(definitions, getCombinedEntityDefinitions())
		for(let key in definitions) {
			let label = definitions[key].label;
			if(definitions[key].className != undefined) {
				label = this.props.t(definitions[key].className+"._className_plural");
			}
			if(label != undefined) {
				nomenclatures.push({label: label, value: key});
			}
		}
		nomenclatures.sort((a, b) => {return (a.label > b.label) ? 1 : ((b.label > a.label) ? -1 : 0);})
		let nomenclature = definitions[this.state.selectedImportType];
		if(definitions[this.state.selectedImportType]) {
			nomenclature.label = this.props.t(definitions[this.state.selectedImportType].className + "._className_plural",definitions[this.state.selectedImportType].label);
		}

		let body;
		if(getEntityDefinition(this.state.selectedImportType) != undefined && nomenclature != undefined) {
			if(this.props.data != undefined) {
				let columns = getExpandedColumns(this.state.selectedImportType, undefined);
				body = <EmbedEntityListOrTableContainer
						componentPath = {this.props.componentPath+"."+this.state.selectedImportType} //redux state path to data array for refresh, new and param to children
						data = {this.props.data} //array with the data
						//loading = {this.props.loading} //Is parent loading? Then wait before retrieving!
						columns = {columns} //columns render function
						retrieveType = {this.state.selectedImportType}
						//nodeType = {this.props.parentAttr} //key to the parent for save purpose
						//nodeHref = {this.props.parentHref} //href of parent for save purpose
						//nodeData = {this.props.parentData} //data of parent for the attachments
						asTable = {true}
						//page={this.state.requiredPageIndex}
						//pages={this.props.pages} // Display the total number of pages
						//pageSize={this.state.requiredPageSize}
						//sortable={this.props.sortable}
						//sorted={this.state.requiredSort}
						//filterable={this.props.filterable}
						//defaultFilter={this.state.requiredFilter}
						//onSortedChange={(newSorted, column, shiftKey) => {this.onSortedChange(newSorted, column, shiftKey);}}
						//onFilteredChange={(filtered, column) => {this.onFilteredChange(filtered, column);}}
						//editable={this.props.editable}
						//aclRestrictable={getEntityDefinition(this.props.retrieveType).aclRestrictable}
						//onChange={()=>{this.props.onChange();}}
						//onCommitChange={(data,index,eventName)=>{this.props.onCommitChange(data,index,eventName);}}
					/>;
			} else {
				body = null;
				//use empty row to make the dropdowns pre-load - otherwise it will try to load it for each imported row in parallel!
				this.props.actions.dispatchEditRESTData(this.props.componentPath,{[this.state.selectedImportType]: [{_editable: true}]});
			}
		} else if(nomenclature && (nomenclature._importFileType == "xlsx" || nomenclature._importFileType == "mt940")) {
			if(this.props.data != undefined) {
				body = <EmbedRetrieveEntityListContainer
						key={this.state.selectedImportType}
						title={nomenclature.label}
						icon={nomenclature.icon}
						columns={nomenclature.columns}
						componentPath={this.props.componentPath+"."+this.state.selectedImportType}
						retrieveType={nomenclature._importEntityDef}
						expanded={true}
						asTable={true}
						creatable={nomenclature.creatable}
						sortable={false}
						filterable={false}
					/>;
				body = <EmbedEntityListOrTableContainer
						componentPath = {this.props.componentPath+"."+this.state.selectedImportType} //redux state path to data array for refresh, new and param to children
						data = {this.props.data} //array with the data
						//loading = {this.props.loading} //Is parent loading? Then wait before retrieving!
						columns = {nomenclature.columns} //columns render function
						retrieveType = {nomenclature._importEntityDef}
						//nodeType = {this.props.parentAttr} //key to the parent for save purpose
						//nodeHref = {this.props.parentHref} //href of parent for save purpose
						//nodeData = {this.props.parentData} //data of parent for the attachments
						asTable = {true}
						//page={this.state.requiredPageIndex}
						//pages={this.props.pages} // Display the total number of pages
						//pageSize={this.state.requiredPageSize}
						//sortable={this.props.sortable}
						//sorted={this.state.requiredSort}
						//filterable={this.props.filterable}
						//defaultFilter={this.state.requiredFilter}
						//onSortedChange={(newSorted, column, shiftKey) => {this.onSortedChange(newSorted, column, shiftKey);}}
						//onFilteredChange={(filtered, column) => {this.onFilteredChange(filtered, column);}}
						//editable={this.props.editable}
						//aclRestrictable={getEntityDefinition(this.props.retrieveType).aclRestrictable}
						//onChange={()=>{this.props.onChange();}}
						//onCommitChange={(data,index,eventName)=>{this.props.onCommitChange(data,index,eventName);}}
					/>;
			} else {
				body = null;
				//use empty row to make the dropdowns pre-load - otherwise it will try to load it for each imported row in parallel!
				this.props.actions.dispatchEditRESTData(this.props.componentPath,{[this.state.selectedImportType]: [{_editable: true}]});
			}
//			} else if(this.state.workBook) {
//				let ws = this.state.workBook.Sheets[this.state.workBook.SheetNames[0]];
//				body = <div dangerouslySetInnerHTML={{__html: XLSX.utils.sheet_to_html(ws)}} />;
//			}
		} else {
			const tableClasses = classNames({
				'clients-table align-center-table -highlight': true,
				'hidden': !nomenclature
			})
			const { mapped, selectedImportType } = this.state
			let submitBtnDisabled = !(mapped && mapped instanceof Array && mapped.length > 0 ) || (mapped.every((x) => x && x.id ))
		
			body = <div className="col-sm-12 form-group">
						<div className='col-sm-3 col-xs-12'>
							<Button disabled={submitBtnDisabled} variant='success' onClick={() => {
								if(selectedImportType == 'legalPersonsPotential')	{
									this.saveLegalPersonsPotential(definitions)
								}
								else{
									this.saveGeneralChanges(definitions)
								}
							}}> {this.props.t("Import.SaveChanges")} </Button>
						</div>
						<div className='col-sm-12'>
							<ReactTable
								className={tableClasses}
								data={ mapped }
								getTrProps={this.getTrProps}
								columns={nomenclature ? nomenclature.columns : []}
								noDataText={this.props.t("Import.YouHaveNotImportedDataYet")}
							/>
						</div>
					</div>
		}
		let uploadAction = null;
		switch(nomenclature ? nomenclature._importFileType : "csv") {
			case "mt940": uploadAction = <div>
					<input type="file" name="file" id="file" className="inputfile" onChange={(ev) => {this.handleMt940FileUpload(ev)}} />
					<label className="btn btn-success col-sm-12" htmlFor="file">
						<FontAwesomeIcon icon="upload"/>
						{this.props.t("Import.ChooseFileToImport")}
					</label>
				</div>;
				break;
			case "xlsx": uploadAction = <LoadXlsxContainer
						templateName={nomenclature._importTemplateName}
						componentPath={this.props.componentPath+"."+this.state.selectedImportType}
					/>;
				break;
			case "csv":
			default: uploadAction = <CSVReader
						ref={refCSVReader}
						onFileLoad={(data) => this.handleOnFileLoad(data, definitions)}
						onError={(data) => this.handleOnError(err, file, inputElem, reason)}
						noClick
						noDrag
						addRemoveButton
						onRemoveFile={(data) => this.handleOnRemoveFile(data)}
					>
						<Button disabled={!this.state.selectedImportType} variant='success'
							onClick={(e) => {
								// Note that the ref is set async, so it might be null at some point
								if (refCSVReader.current) {
									refCSVReader.current.open(e);
								}
							}}
						>{this.props.t("Import.ChooseFileToImport")}</Button>
					</CSVReader>;
		}
		return (
			<div className="page-body-wrapper">
				{/* <Header text={this.props.t("Import.title")} class='page-header text-align-center no-margin' /> */}
				<NewHeader text={this.props.t("Import.title")} auth={this.props.auth.userAuthenticated} />
				<div className='page-body'>
					<div className="col-sm-12 form-group">

						<Select
							className="col-sm-6 col-xs-12"
							name="form-field-name"
							options={nomenclatures}
							onChange={(e) => { this.selectImportType(e) }}
							value={nomenclature}
							placeholder={this.props.t("Import.DataTypeToImport")}
						/>

						<div className='col-sm-3 col-xs-12'>
							{uploadAction}
						</div>
					</div>
					{body}
				</div>
			</div>
		);
	}
}

function mapStateToProps(state,ownProps) {
	const componentPath = ownProps.componentPath ? ownProps.componentPath : "import";
	const containerState = resolveObjectPath(componentPath,state.rest); //rest because of fetchREST
	let data = undefined;
	if(containerState && containerState[ownProps.match.params.selectedImportType]) {
		data = containerState[ownProps.match.params.selectedImportType];
	}
	return {
		auth: state.auth,
		componentPath: componentPath,
		apiEndpoint: ownProps.apiEndpoint,
		rest: state.rest,
		data: data,
	};
}

function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData, fetchRESTFollow, postRESTData }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageImportContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
