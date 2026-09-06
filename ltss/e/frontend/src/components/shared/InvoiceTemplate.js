import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { convertNumbersToBgnCurrency, convertNumbersToEnglish } from './../../scripts/dataUtils';

class InvoiceTemplate extends React.Component {

	constructor(props){
		super(props)
		this.state = {
			dict: {
				bg: {
					TITLE: 'Фактура',
					ORIGINAL: 'Оригинал',
					COPY: 'Копие',
					DATE: 'Дата',
					NOT_ISSUED: 'Не е издадена',
					SUPPLIER: 'Доставчик',
					SUPPLIER_NAME: 'Транс Лоджик Груп ЕООД',
					BUYER: 'Получател',
					MOL: 'МОЛ',
					SUPPLIER_MOL: 'Апостол Стоименов',
					EIK: 'Идент. №',
					SUPPLIER_EIK: '204074306',
					VAT: 'ДДС №',
					SUPPLIER_VAT: 'BG204074306',
					CITY: 'Град',
					SUPPLIER_CITY: 'Ново Делчево',
					ADDRESS: 'Адрес',
					SUPPLIER_ADDRESS: 'ул. Славянска 19',
					BIC: 'Банков код',
					IBAN: 'IBAN',
					REF_NUMBER: 'Референтен номер',
					NO: 'Номер',
					PRODUCT_DESCRIPTION: 'Описание',
					SIZE: 'Размер',
					PN: 'PN',
					MEASURE: 'М-ка',
					QUANTITY: 'Кол-во',
					UNIT_PRICE: 'Ед. цена',
					DISCOUNT: 'ТО, %',
					DISCOUNT_PRICE: 'Ед. цена след ТО',
					TOTAL_PRICE: 'Обща цена',
					DELIVERY_DEADLINE: 'Срок за доставка',
					DELIVERY_TERMS: '- Условия за доставка',
					GUARANTEE_TERMS: '- Гаранционен срок',
					DISCOUNT_CONDITION: '- Условия за отстъпка',
					PROCESSED_BY: 'Съставил',
					RECEIVED_BY: 'Получил',
					VAT_AMOUNT: 'ДДС',
					NOTES: 'Забележки',
					TAX_DATE: 'Дата на данъчно събитие',
					PAYMENT: 'Плащане',
					VAT_EXEMPTION_REASON: 'Основание за ненач. на ДДС',
					DEAL_DESCRIPTION: 'Описание на сделката',
					BANK: 'Банка',
					DEAL_PLACE: 'Място на сделката',
					STAMP_DISCLAIMER: "Съгласно чл.7, ал.1 от Закона за счетоводството, чл.114 от ЗДДС и чл.78 от ППЗДДС печатът и подписът не са задължителни реквизити на фактурата",
					TAX_BASE_AMOUNT: 'Данъчна основа',
					TAX_AMOUNT: 'ДДС 20%',
					TOTAL_AMOUNT: 'Сума за плащане',
					CURRENCY: 'Валута',
					WITH_WORDS: 'Словом',
					TRANSPORT: 'Транспорт',
					CONTAINER: 'контейнер',
					TRUCK: 'товарен автомобил',
				},
				en: {
					TITLE: 'INVOICE',
					ORIGINAL: 'ORIGINAL',
					COPY: 'COPY',
					NOT_ISSUED: 'Not issued',
					DATE: 'Date of document',
					SUPPLIER: 'Supplier',
					SUPPLIER_NAME: 'Trans Logic Group LTD',
					BUYER: 'Client',
					MOL: 'Attention to',
					SUPPLIER_MOL: 'Apostol Stoimenov',
					EIK: 'ID. №',
					SUPPLIER_EIK: '204074306',
					VAT: 'VAT №',
					SUPPLIER_VAT: 'BG204074306',
					CITY: 'Address',
					SUPPLIER_CITY: 'Novo Delchevo',
					ADDRESS: 'Address',
					SUPPLIER_ADDRESS: '19 Slavianska str',
					BIC: 'BIC',
					IBAN: 'IBAN',
					REF_NUMBER: 'Референтен номер',
					NO: '№',
					PRODUCT_DESCRIPTION: 'Name/Description',
					SIZE: 'Размер',
					PN: 'PN',
					MEASURE: 'М-ка',
					QUANTITY: 'Quantity',
					UNIT_PRICE: 'Price',
					TOTAL_PRICE: 'Amount',
					DELIVERY_DEADLINE: 'Срок за доставка',
					DELIVERY_TERMS: '- Условия за доставка',
					GUARANTEE_TERMS: '- Гаранционен срок',
					DISCOUNT_CONDITION: '- Условия за отстъпка',
					PROCESSED_BY: 'Compiler (name)',
					RECEIVED_BY: 'Получил',
					VAT_AMOUNT: 'ДДС',
					NOTES: 'Забележки',
					TAX_DATE: 'Date of tax event',
					PAYMENT: 'Payment',
					VAT_EXEMPTION_REASON: 'VAT not applicable with zero stakes – reason',
					DEAL_DESCRIPTION: 'Описание на сделката',
					BANK: 'Bank',
					DEAL_PLACE: 'Място на сделката',
					STAMP_DISCLAIMER: '',
					TAX_BASE_AMOUNT: 'Tax base',
					TAX_AMOUNT: 'Tax amount',
					TOTAL_AMOUNT: 'Amount to pay',
					CURRENCY: 'Currency',
					WITH_WORDS: 'In words',
					TRANSPORT: 'Transport',
					CONTAINER: 'container',
					TRUCK: 'truck',
				}
			}
		}
	}

	render() {
		const { formData, originalOrCopy, lang, formDetailsData, auth } = this.props;
		const { dict } = this.state;
		let dataRows = []
		if(formData && !(formData instanceof Promise) && formDetailsData && formDetailsData != {} && (formDetailsData instanceof Array)){
			let offerCurrency = formData.invoiceCurrency ? formData.invoiceCurrency.name : '';
			dataRows = formDetailsData.map((detailsRow, lineIndex) => {
				return (
						<tr key={lineIndex + 1}>
							<td className='text-align-center'>{lineIndex + 1}</td>
							<td>{(detailsRow.transport && detailsRow.transport.tractorUnit && detailsRow.transport.trailer) ?
									dict[lang].TRANSPORT + ": " + detailsRow.transport.route + ", " + dict[lang].CONTAINER + " " + detailsRow.transport.containerNumber + ", " + dict[lang].TRUCK + " " + detailsRow.transport.tractorUnit.licensePlate + "/" + detailsRow.transport.trailer.licensePlate
									: detailsRow.article.name + ": " + detailsRow.invoiceRowDescription
								}
							</td>
							<td className='text-align-right'>{detailsRow.quantity}</td>
							<td className='text-align-right'>{detailsRow.priceRate ? detailsRow.priceRate.toFixed(2) : ''}</td>
							<td className='text-align-right'>{(detailsRow.quantity * detailsRow.priceRate).toFixed(2)}</td>
						</tr>
					)
			})
			dataRows.push(
				<tr key={dataRows.length + 1}>
					<td className='no-border text-align-right' colSpan="4">{dict[lang].TAX_BASE_AMOUNT}: </td>
					<td className="text-align-right totalOfferPrice">{this.props.formData.taxBaseAmount ? this.props.formData.taxBaseAmount.toFixed(2) : 0}</td>
				</tr>
			)
			dataRows.push(
				<tr key={dataRows.length + 2}>
					<td className='no-border text-align-right' colSpan="4">{dict[lang].TAX_AMOUNT}: </td>
					<td className="text-align-right totalOfferPrice">{this.props.formData.taxAmount ? this.props.formData.taxAmount.toFixed(2) : 0}</td>
				</tr>
			)
			dataRows.push(
				<tr key={dataRows.length + 3}>
					<td className='no-border text-align-right' colSpan="4">{dict[lang].TOTAL_AMOUNT}: </td>
					<td className="text-align-right totalOfferPrice">{this.props.formData.totalAmount ? this.props.formData.totalAmount.toFixed(2) : 0}</td>
				</tr>
			)
			dataRows.push(
				<tr key={dataRows.length + 4}>
					<td className='no-border text-align-right' colSpan="4">{dict[lang].CURRENCY}: </td>
					<td className="text-align-right totalOfferPrice">{offerCurrency}</td>
				</tr>
			)
			dataRows.push(
				<tr key={dataRows.length + 5}>
					<td className='no-border text-align-right' colSpan="5">{dict[lang].WITH_WORDS}: {
						(lang == "bg") ?
						convertNumbersToBgnCurrency(Math.floor(this.props.formData.totalAmount))+" "+(offerCurrency == "BGN" ? "лева" : offerCurrency)+" и "+convertNumbersToBgnCurrency(Math.floor((this.props.formData.totalAmount - Math.floor(this.props.formData.totalAmount))*100))+" "+(offerCurrency == "BGN" ? "стотинки" : "")
						: convertNumbersToEnglish(this.props.formData.totalAmount) + " " + offerCurrency
					}</td>
				</tr>
			)
			return (
				<div className={this.props.className}>
					<hr></hr>
					<h1 className="text-align-center">{dict[lang].TITLE}</h1>
					<h4 className="text-align-center">{originalOrCopy == 'orig' ? dict[lang].ORIGINAL : dict[lang].COPY}</h4>
					{dict[lang].NO}: {this.props.formData.invoiceNum ? String(this.props.formData.invoiceNum).padStart(10, '0') : dict[lang].NOT_ISSUED}<br/>
					{dict[lang].DATE}: {new Date(this.props.formData.invoiceDate).toLocaleDateString(lang)}
					<hr></hr>
					<table>
						<tbody>
							<tr>
								<td>{dict[lang].BUYER}: </td>
								<td>{this.props.formData.invoiceCounterParty.name}</td>
								<td> &nbsp; </td>
								<td>{dict[lang].SUPPLIER}: </td>
								<td>{dict[lang].SUPPLIER_NAME}</td>
							</tr>
							<tr>
								<td>{dict[lang].VAT}: </td>
								<td>{this.props.formData.invoiceCounterParty.vatNumber}</td>
								<td> &nbsp; </td>
								<td>{dict[lang].VAT}: </td>
								<td>{dict[lang].SUPPLIER_VAT}</td>
							</tr>
								<tr>
								<td>{dict[lang].EIK}: </td>
								<td>{this.props.formData.invoiceCounterParty.eik}</td>
								<td> &nbsp; </td>
								<td>{dict[lang].EIK}: </td>
								<td>{dict[lang].SUPPLIER_VAT}</td>
							</tr>
							<tr>
								<td>{dict[lang].CITY}: </td>
								<td>{this.props.formData.invoiceCounterParty.city}</td>
								<td> &nbsp; </td>
								<td>{dict[lang].CITY}: </td>
								<td>{dict[lang].SUPPLIER_CITY}</td>
							</tr>
							<tr>
								<td>{dict[lang].ADDRESS}: </td>
								<td>{this.props.formData.invoiceCounterParty.address}</td>
								<td> &nbsp; </td>
								<td>{dict[lang].ADDRESS}: </td>
								<td>{dict[lang].SUPPLIER_ADDRESS}</td>
							</tr>
							<tr>
								<td>{dict[lang].MOL}: </td>
								<td>{this.props.formData.invoiceCounterParty.mol}</td>
								<td> &nbsp; </td>
								<td>{dict[lang].MOL}: </td>
								<td>{dict[lang].SUPPLIER_MOL}</td>
							</tr>
						</tbody>
					</table>
					<hr></hr>
					<table className="offer-table">
						<thead>
							<tr>
								<th> {dict[lang].NO}</th>
								<th> {dict[lang].PRODUCT_DESCRIPTION}</th>
								<th> {dict[lang].QUANTITY}</th>
								<th> {dict[lang].UNIT_PRICE}</th>
								<th> {dict[lang].TOTAL_PRICE}</th>
							</tr>
						</thead>
						<tbody>
							{dataRows}
						</tbody>
					</table>
					<hr></hr>
					<table>
						<tbody>
							<tr>
								<td>{dict[lang].TAX_DATE}: </td>
								<td>{new Date(this.props.formData.invoiceDate).toLocaleDateString(lang)}</td>
								<td> &nbsp; </td>
								<td>{dict[lang].PAYMENT}: </td>
								<td>{this.props.formData.paymentType.listOptionItemName}</td>
							</tr>
							<tr>
								<td>{dict[lang].VAT_EXEMPTION_REASON}: </td>
								<td>{this.props.formData.vatExemptionReason.listOptionItemName}</td>
								<td> &nbsp; </td>
								<td>{dict[lang].IBAN}: </td>
								<td>{this.props.formData.bankAccount ? this.props.formData.bankAccount.iban : ""}</td>
							</tr>
							<tr>
								<td></td>
								<td></td>
								<td> &nbsp; </td>
								<td>{dict[lang].BANK}: </td>
								<td>{this.props.formData.bankAccount ? this.props.formData.bankAccount.bankName : ""}</td>
							</tr>
							<tr>
								<td></td>
								<td></td>
								<td> &nbsp; </td>
								<td>{dict[lang].BIC}: </td>
								<td>{this.props.formData.bankAccount ? this.props.formData.bankAccount.bic : ""}</td>
							</tr>
							<tr>
								<td></td>
								<td></td>
								<td> &nbsp; </td>
								<td>{dict[lang].PROCESSED_BY}: </td>
								<td>{this.props.formData.issuedBy ? this.props.formData.issuedBy.fullName : ""}</td>
							</tr>
						</tbody>
					</table>
					<hr></hr> 
					{dict[lang].STAMP_DISCLAIMER}
					<hr></hr>
				</div>
			);
		}
		else{
			return (<div> </div>)
		}
	}
}

function mapStateToProps(state,ownProps) {
	return {
		auth: state.auth,
		hasShowDiscounts: ownProps.hasShowDiscounts,
	};
}
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

export default connect(mapStateToProps, mapDispatchToProps)(InvoiceTemplate);