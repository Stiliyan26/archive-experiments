import React from 'react';
import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import XLSX from 'xlsx'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'
import { resolveObjectPath } from '../../scripts/dataUtils';
import { dispatchEditRESTData } from '../../actions/taskActions';


class LoadXlsxContainer extends React.Component {


	
	to_json(workbook) {
		//if(useworker && workbook.SSF) XLSX.SSF.load_table(workbook.SSF);
		var result = {};
		workbook.SheetNames.forEach(function(sheetName) {
			var roa = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName], {raw:false, header:1});
			if(roa.length > 0) result[sheetName] = roa;
		});
		return result;
	}
	
	//TODO use bank file parser service instead
	handleXLSFileUpload(e) {
		let self = this;
		var rABS = typeof FileReader !== 'undefined' && FileReader.prototype && FileReader.prototype.readAsBinaryString;
		var files = e.target.files;
		var i,f;
		for (i = 0, f = files[i]; i != files.length; ++i) {
			var reader = new FileReader();
			var name = f.name;
			reader.onload = function(e) {
				var data = e.target.result;
				var wb, arr;
				var readtype = {type: rABS ? 'binary' : 'base64' };
				if(!rABS) {
					arr = self.fixdata(data);
					data = btoa(arr);
				}
				try {
					//if(useworker) { sheetjsw(data, process_wb, readtype); return; }
					wb = XLSX.read(data, readtype);
					self.setState({workBook: wb});
					//import specific data extraction
					//EuroBank movements
					let sheet = wb.Sheets[wb.SheetNames[0]];
//					console.log('Sheet: ',sheet);
//					//Check for EuroBank title
//					if(sheet.A1 && sheet.A1.v == "ЮРОБАНК България АД") {
//						console.log('bank name found');
//					}
//					if(sheet.B16 && sheet.B16.v == "Сума") {
//						console.log('Сума found');
//					}
//					if(sheet.C16 && sheet.C16.v == "Валута") {
//						console.log('Валута found');
//					}
//					if(sheet.A16 && sheet.A16.v == "Документ") {
//						console.log('Документ found');
//					}
//					if(sheet.L16 && sheet.L16.v == "Описание") {
//						console.log('Описание found');
//					}
//					if(sheet.H16 && sheet.H16.v == "Вальор") {
//						console.log('Вальор found');
//					}
//					if(sheet.J16 && sheet.J16.v == "Наредител") {
//						console.log('Наредител found');
//					}
//					if(sheet.F16 && sheet.F16.v == "Тип") {
//						console.log('Тип found');
//					}
					let range = XLSX.utils.decode_range(sheet["!ref"]);
					let mappedAndTrimmed = [];
					let bankAccountsPromise = retrieveData("bankAccounts");
					let currenciesPromise = retrieveData("currencies");
					Promise.all([bankAccountsPromise,currenciesPromise]).then((results) => {
						let bankAccountsData = results[0];
						let currenciesData = results[1];
						//now the bank accounts should be loaded
						for(let rowNum = 17; rowNum <= range.e.r; rowNum++) {
							if(sheet["F"+rowNum] && sheet["F"+rowNum].v == "Входящо") {
								//payment detected
								let resultObj = {};
								resultObj.name = (sheet["A"+rowNum] ? sheet["A"+rowNum].v : undefined);
								resultObj.incomeDesc = (sheet["L"+rowNum] ? sheet["L"+rowNum].v : undefined);
								resultObj.incomeDate = new Date(sheet["H"+rowNum] ? sheet["H"+rowNum].w : undefined).toISOString();
								let bankAccount = (sheet["J"+rowNum] && sheet["J"+rowNum].v ? 
										bankAccountsData._embedded.bankAccounts.find((acc) => sheet["J"+rowNum].v.includes(acc.iban))
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
								resultObj.attachableRevenuesAndExpenses.ammount = (sheet["B"+rowNum] ? sheet["B"+rowNum].v : undefined);
								if(sheet["C"+rowNum]) {
									let result = currenciesData._embedded.currencies.find((elem) => (elem.name == sheet["C"+rowNum].v));
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
						};
						//console.log("mappedAndTrimmed",mappedAndTrimmed);
						self.setState({ mapped: mappedAndTrimmed });
						//TODO change to remove pageData._embedded
						self.props.actions.dispatchEditRESTData(self.props.componentPath,{[self.state.selectedImportType]: {pageData: {_embedded: {["incomes"]: self.state.mapped}}}});
					});
					
				} catch(e) {
					console.error(e);
					//opts.errors.failed(e);
				}
			};
			if(rABS) reader.readAsBinaryString(f);
			else reader.readAsArrayBuffer(f);
		}
	}
	
	nepalProduced(e) {
		let self = this;
		var rABS = typeof FileReader !== 'undefined' && FileReader.prototype && FileReader.prototype.readAsBinaryString;
		var files = e.target.files;
		var i,f;
		for (i = 0, f = files[i]; i != files.length; ++i) {
			var reader = new FileReader();
			var name = f.name;
			reader.onload = function(e) {
				var data = e.target.result;
				var wb, arr;
				var readtype = {type: rABS ? 'binary' : 'base64' };
				if(!rABS) {
					arr = self.fixdata(data);
					data = btoa(arr);
				}
				try {
					wb = XLSX.read(data, readtype);
					self.setState({workBook: wb});
					//import specific data extraction
					let sheet = wb.Sheets[wb.SheetNames[0]];
					console.log('Sheet: ',sheet);
					//Check header
					if(sheet.A1 && sheet.A1.v == "DATE") {
						console.log('DATE found');
					}
					if(sheet.B1 && sheet.B1.v == "HOUR") {
						console.log('HOUR found');
					}
					
					let range = XLSX.utils.decode_range(sheet["!ref"]);
					console.log("range",range);
					let mappedAndTrimmed = [];

						//now the bank accounts should be loaded
						for(let rowNum = 2; rowNum <= range.e.r; rowNum++) {
								//payment detected
								let resultObj = {};
								let col = "C";
								let ssfDate = XLSX.SSF.parse_date_code(sheet["A"+rowNum].v);
								console.log("date",ssfDate);
								resultObj.identification = (sheet[col+"1"] ? sheet[col+"1"].v : undefined);
								resultObj.scheduleTimeStart = new Date(ssfDate.y, ssfDate.m, ssfDate.d, sheet["B"+rowNum].v-1).toISOString();
								resultObj.scheduleTimeEnd = new Date(ssfDate.y, ssfDate.m, ssfDate.d, sheet["B"+rowNum].v).toISOString();
								resultObj.quantityKwh = (sheet[col+rowNum] ? sheet[col+rowNum].v : 0);
								//put object for saving
								resultObj._editable = true;
								mappedAndTrimmed.push(resultObj);
						};
						console.log("mappedAndTrimmed",mappedAndTrimmed);
						self.setState({ mapped: mappedAndTrimmed });
						self.props.actions.dispatchEditRESTData(self.props.componentPath, self.state.mapped);
					
				} catch(e) {
					console.error(e);
					//opts.errors.failed(e);
				}
			};
			if(rABS) reader.readAsBinaryString(f);
			else reader.readAsArrayBuffer(f);
		}
	}

	render() {
		return (
				<div>
					<input type="file" name="file" id="file" className="inputfile" onChange={(ev) => {
							switch(this.props.templateName) {
								case "nepalProduced":
									this.nepalProduced(ev);
									break;
								case "":
								default:
									this.handleXLSFileUpload(ev);
							}
						}} />
					<label className="btn btn-success col-sm-12" htmlFor="file">
						<FontAwesomeIcon icon="upload"/>
						{this.props.t("Import.ChooseFileToImport")}
					</label>
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
		data: data,
		templateName: ownProps.templateName,
	};
}

function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(LoadXlsxContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
