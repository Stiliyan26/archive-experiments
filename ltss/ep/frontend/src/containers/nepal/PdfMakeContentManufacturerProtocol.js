
import pdfMake from "pdfmake/build/pdfmake";
import pdfFonts from "pdfmake/build/vfs_fonts";
import JSZip from "jszip";

const fInvestLogo = require('../../images/companyLogo.svg');

function formatDateParameters(date) {
		return date < 10 ? "0" + date : date;
	}

function getCurrentDate() {
		let day = formatDateParameters(new Date().getDate());
		let month = formatDateParameters(new Date().getMonth() + 1);
		let year = new Date().getFullYear()

		return day + "." + month + "." + year;
	}

function getRangeDate(fromDate,toDate) {
		let fromDay = formatDateParameters(new Date(fromDate).getDate())
		let toDay = formatDateParameters(new Date(toDate).getDate())
		let month = ""

		switch (new Date(fromDate).getMonth()) {
			case 0:
				month = "ЯНУАРИ"
				break;
			case 1:
				month = "ФЕВРУАРИ"
				break;
			case 2:
				month = "МАРТ"
				break;
			case 3:
				month = "АПРИЛ"
				break;
			case 4:
				month = "МАЙ"
				break;
			case 5:
				month = "ЮНИ"
				break;
			case 6:
				month = "ЮЛИ"
				break;
			case 7:
				month = "АВГУСТ"
				break;
			case 8:
				month = "СЕПТЕМВРИ"
				break;
			case 9:
				month = "ОКТОМВРИ"
				break;
			case 10:
				month = "НОЕМВРИ"
				break;
			case 11:
				month = "ДЕКЕМВРИ"
				break;
			default:
		}

		if (new Date(fromDate).getDate() == 1 && new Date(toDate).getDate() >= 30) {
			return month
		}

		return fromDay + "-" + toDay + ". " + month
	}

function getYear(fromDate) {
		return new Date(fromDate).getFullYear();
	}


function getContent(result,numberOfRows,logo) {
		let year = getYear(result.fromDate);
		let rangeDate = getRangeDate(result.fromDate,result.toDate);
		let currentDate = result.protocolCreationDate
		
	pdfMake.vfs = pdfFonts.pdfMake.vfs;
		
	return pdfMake.createPdf({
		pageMargins: [20,50,20,50],
		content: [
			{
				table: {
					body: [
						[{
							layout: 'noBorders',
							margin: [20,20,20,0],border: [true, true, true, false],
							table: {
								widths: [200, '*', 200],
								body: [[
									{image: logo, width: 150, height: 150}, 
									{text: 'Ф Инвест ЕООД', alignment: 'center', bold: true},
									''
								]]
							}
						}],
						[{text: 'ПРОТОКОЛ',bold: true, alignment: 'center', margin: [20,20,20,0], border: [true, false, true, false]}], 
						[{text: 'за продадена електрическа енергия по Договор за покупко-продажба на електрическа енергия за енергиен обект ' + result.name
							, alignment: 'center', margin: [20,20,20,0], border: [true, false, true, false]
						}],
						[{
							margin: [20,50,0,0], border: [true, false, true, false],
							layout: 'noBorders',
							table: {
								widths: ['*','*', '*'],
								body: [[
									{text: 'за месец', alignment: 'center'},
									{text: rangeDate, alignment: 'center', bold: true},
									{text: year+' г.', alignment: 'center'},
								]]
							}
						}],
						[{text: 'Днес, '+currentDate+' г.,', margin: [40,50,0,0], border: [true, false, true, false]}],
						[{text: 'Се състави настоящият протокол за продадената електрическа енергия на Ф Инвест ЕООД', margin: [20,20,20,0], border: [true, false, true, false]}],
						[{
							margin: [20,20,20,0], border: [true, false, true, false],
							table: {
								widths: ['*','*','*'],
								body: [
									[
										{text: 'ИТН', alignment: 'center', bold: true},
										{text: result.itn, alignment: 'center', colSpan: 2}, 
										{}
									],
									[
										{text: numberOfRows == 3 ? 'Енергия по график, MWh' : 'Енергия за фактуриране, MWh', alignment: 'center', bold: true},
										{text: 'Единична цена, лв./MWh', alignment: 'center', bold: true},
										{text: 'Сума без ДДС, лв.', alignment: 'center', bold: true}
									],
									[
										{text: numberOfRows == 3 ? result.scheduledEnergy : result.energyForInvoice, alignment: 'center'},
										{text: result.pricePerMwh+' лв.', alignment: 'center'},
										{text: numberOfRows == 3 ? result.scheduledPriceWithoutVat : result.priceWithoutVat+' лв.', alignment: 'center'}
									]
								].concat(
									numberOfRows == 3 ? 
									[
										[
											{text: 'Балансираща енергия, MWh излишък/недостиг', alignment: 'center', bold: true},
											{text: 'Единична цена, лв./MWh', alignment: 'center', bold: true},
											{text: 'Сума без ДДС, лв.', alignment: 'center', bold: true}
										],
										[
											{text: result.balancedEnergy, alignment: 'center'},
											{text: result.pricePerMwh+' лв.', alignment: 'center'},
											{text: result.balancedPriceWithoutVat+' лв.', alignment: 'center'}
										],[
											{text: 'Обща енергия за фактуриране, MWh', alignment: 'center', bold: true, fillColor: '#B0C4DE'},
											{text: 'Единична цена, лв./MWh', alignment: 'center', bold: true, fillColor: '#B0C4DE'},
											{text: 'Обща сума без ДДС, лв.', alignment: 'center', bold: true, fillColor: '#B0C4DE'}
										],
										[
											{text: result.totalEnergyForInvoice, alignment: 'center'},
											{text: result.pricePerMwh+' лв.', alignment: 'center'},
											{text: result.totalPriceWithoutVat+' лв.', alignment: 'center'}
										]
									]
									: []
								)
							},
							layout: {
								hLineColor: '#4488FF',
								vLineColor: '#4488FF',
							}
						}],
						[{
							border: [true, false, true, true],
							layout: 'noBorders',
							table: {
								widths: ['*','*'],
								body: [[
									{text: 'Подпис за Производителя:', alignment: 'center', bold: true, margin: [0,20,0,50]},
									{text: 'Подпис за Ф Инвест ЕООД :', alignment: 'center', bold: true, margin: [0,20,0,50]}
								]]
							}
						}]
					]
				},
				layout: {
					hLineWidth: function (i) {
						return 2;
					},
					vLineWidth: function (i) {
						return 2;
					},
					hLineColor: '#4488FF',
					vLineColor: '#4488FF',
				}
			}
		]
	});
}

export function getPdfBlob(result,numberOfRows) {
	return new Promise((resolve) => {
		var xhr = new XMLHttpRequest();
		xhr.onload = function() {
			var reader = new FileReader();
			reader.onloadend = function() {
				const pdf = getContent(result,numberOfRows,reader.result);
				pdf.getBlob((blob) => resolve(blob));
			}
			reader.readAsDataURL(xhr.response);
		};
		xhr.open('GET', fInvestLogo);
		xhr.responseType = 'blob';
		xhr.send();
	});
}

export function getPdfBase64(result,numberOfRows) {
	return new Promise((resolve) => {
		var xhr = new XMLHttpRequest();
		xhr.onload = function() {
			var reader = new FileReader();
			reader.onloadend = function() {
				const pdf = getContent(result,numberOfRows,reader.result);
				pdf.getBase64((base64String) => resolve(base64String));
			}
			reader.readAsDataURL(xhr.response);
		};
		xhr.open('GET', fInvestLogo);
		xhr.responseType = 'blob';
		xhr.send();
	});
}

export function openPdf(result,numberOfRows) {
	var xhr = new XMLHttpRequest();
	xhr.onload = function() {
		var reader = new FileReader();
		reader.onloadend = function() {
			console.log(result,numberOfRows,reader.result)
			const pdf = getContent(result,numberOfRows,reader.result);
			pdf.open();
			//pdf.download('protocol.pdf');
		}
		reader.readAsDataURL(xhr.response);
	};
	xhr.open('GET', fInvestLogo);
	xhr.responseType = 'blob';
	xhr.send();
}

export function downloadPdf(result,numberOfRows) {
	var xhr = new XMLHttpRequest();
	xhr.onload = function() {
		var reader = new FileReader();
		reader.onloadend = function() {
			const pdf = getContent(result,numberOfRows,reader.result);
			//pdf.open();
			pdf.download('protocol.pdf');
		}
		reader.readAsDataURL(xhr.response);
	};
	xhr.open('GET', fInvestLogo);
	xhr.responseType = 'blob';
	xhr.send();
}

export function downloadPdfZip(protocols) {
	var zip = new JSZip();
	protocols.forEach((protocol) => {
		zip.file(protocol.fileName, protocol.blob);
	});
	zip.generateAsync({type:"blob"})
	.then(function (content) {
		var csvURL = window.URL.createObjectURL(content);
		var tempLink = document.createElement('a');
		tempLink.href = csvURL;
		tempLink.setAttribute('download', 'protocols.zip');
		tempLink.click();
	});
}

export default getContent;
