import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import CompanyLogo from '../icons/CompanyLogo'
import Company2Logo from '../icons/Company2Logo'

import { resolveObjectPath } from './../../scripts/dataUtils';

class OfferTemplate extends React.Component {

	constructor(props){
		super(props)
		this.state = {
			dict: {
				bg: {
					// WATO CODE
					1: {
						NAME: 'ВАТО ООД',
						MOL: 'Мирослав Марков',
						EIK: '201549826',
						VAT: 'BG201549826',
						INVOICE_ADDRESS: 'Казанлък, 6100 Южна Индустриална Зона, п.к. 3',
						DELIVERY_ADDRESS: 'България, Казанлък, 6100 Южна Индустриална Зона, п.к. 3',
					},
					// INDUSTRIAL PARTS CODE
					2: {
						NAME: 'ИНДУСТРИАЛ ПАРТС ООД',
						MOL: 'Мирослав Марков',
						EIK: '123544268',
						VAT: 'BG123544268',
						INVOICE_ADDRESS: 'Казанлък, Южна Индустриална зона, сграда "Индустриал партс", п.к.24',
						DELIVERY_ADDRESS: 'България, Казанлък, Южна Индустриална зона, сграда "Индустриал партс", п.к.24',
					},
					SUPPLIER: 'ДОСТАВЧИК',
					BUYER: 'КУПУВАЧ',
					MOL: 'МОЛ',
					EIK: 'ЕИК (Булстат)',
					VAT: 'ДДС номер',
					INVOICE_ADDRESS: 'Адрес на фактуриране',
					ADDRESS: 'Адрес',
					DELIVERY_ADDRESS: 'Адрес за доставка',
					BIC: 'BIC',
					IBAN: 'IBAN',
					OFFER_TITLE: 'ОФЕРТА',
					REF_NUMBER: 'Референтен номер',
					NO: 'Номер',
					PRODUCT_DESCRIPTION: 'Описание',
					SIZE: 'Размер',
					PN: 'PN',
					MEASURE: 'М-ка',
					QUANTITY: 'Количество',
					UNIT_PRICE: 'Ед. цена',
					DISCOUNT: 'ТО, %',
					DISCOUNT_PRICE: 'Ед. цена след ТО',
					TOTAL_PRICE: 'Обща цена',
					DELIVERY_DEADLINE: 'Срок за доставка',
					DELIVERY_TERMS: '- Условия за доставка',
					GUARANTEE_TERMS: '- Гаранционен срок',
					DISCOUNT_CONDITION: '- Условия за отстъпка',
					PROCESSED_BY: 'Съставител',
					DATE: 'Дата',
					TOTAL_PRICE_WITHOUT_ADDITIONAL_DISCOUNT: 'Цена без доп. отстъпка',
					DISCOUNT_AMOUNT_ADDITIONAL: 'Стойност на доп. отстъпка',
					TOTAL_PRICE_WITH_ADDITIONAL_DISCOUNT: 'Крайна цена без ДДС',
					VAT_AMOUNT: 'ДДС',
					TOTAL_PRICE_WITH_ADDITIONAL_DISCOUNT_AND_VAT: 'Крайна цена с ДДС',
					NOTES: 'Забележки'
				},
				en: {
					// WATO CODE
					1: {
						NAME: 'Wato BG OOD',
						MOL: 'Miroslav Markov',
						EIK: '201549826',
						VAT: 'BG201549826',
						INVOICE_ADDRESS: 'Kazanlak, 6100 South Industrial Zone P.Box 3',
						DELIVERY_ADDRESS: 'Bulgaria, Kazanlak 6100 South Industrial Zone P.Box 3'
					},
					// INDUSTRIAL PARTS CODE
					2: {
						NAME: 'INDUSTRIAL PARTS LTD.',
						MOL: 'Miroslav Markov',
						EIK: '123544268',
						VAT: 'BG123544268',
						INVOICE_ADDRESS: 'Kazanlak, South Industial zone, buld. "Industrial parts", p.c 24',
						DELIVERY_ADDRESS: 'Bulgaria, Kazanlak, South Industial zone, buld. "Industrial parts", p.c 24'
					},
					SUPPLIER: 'SUPPLIER',
					BUYER: 'BUYER',
					EIK: 'Reg. No.',
					VAT: 'VAT number',
					INVOICE_ADDRESS: 'Invoice address',
					ADDRESS: 'Address',
					DELIVERY_ADDRESS: 'Delivery address',
					BIC: 'BIC',
					IBAN: 'IBAN',
					OFFER_TITLE: 'OFFER',
					REF_NUMBER: 'Reference number',
					NO: 'No',
					PRODUCT_DESCRIPTION: 'Product Description',
					SIZE: 'Size/DN',
					PN: 'PN',
					MEASURE: 'Measure',
					QUANTITY: 'Quantity',
					UNIT_PRICE: 'Unit price eur',
					DISCOUNT: 'Discount %',
					DISCOUNT_PRICE: 'Discounted unit price',
					TOTAL_PRICE: 'Total Price eur',
					DELIVERY_DEADLINE: 'Delivery deadline',
					DELIVERY_TERMS: '- Delivery terms',
					GUARANTEE_TERMS: '- Guarantee',
					DISCOUNT_CONDITION: '- Discount terms',
					PROCESSED_BY: 'Processed by',
					DATE: 'Date',
					TOTAL_PRICE_WITHOUT_ADDITIONAL_DISCOUNT: 'Total price without additional discount',
					DISCOUNT_AMOUNT_ADDITIONAL: 'Discount amount',
					TOTAL_PRICE_WITH_ADDITIONAL_DISCOUNT: 'Total price with discount',
					VAT_AMOUNT: 'VAT amount',
					TOTAL_PRICE_WITH_ADDITIONAL_DISCOUNT_AND_VAT: 'Total price with discount and VAT',
					NOTES: 'Notes'
				}
			}
		}
	}

	render() {
		const { offer, lang, offerLines, auth, companyCodeArg } = this.props;
		const { dict } = this.state
		let companyCode = companyCodeArg || 1
		let companyLogo = companyCode == 1 ? <CompanyLogo /> : <Company2Logo />
		let dataRows = []
		if(offer && !(offer instanceof Promise) && offerLines && offerLines != {} && (offerLines instanceof Array)){
			let offerCurrency = offer.currency ? offer.currency.name : ''
			let totalOfferPrice = 0
			dataRows = offerLines.map((offerLine, lineIndex) => {
				let discountedPrice = offerLine.price * (100 - offerLine.discountPercent)/100
				let description = offerLine.description ? offerLine.description : (offerLine.article && offerLine.article.name ? offerLine.article.name : '')
				totalOfferPrice += offerLine.ammount * discountedPrice

				return (
						<tr key={lineIndex + 1}>
							<td className='text-align-center'>{lineIndex + 1}</td>
							<td>{description}</td>
							{/* <td> - </td> */}
							<td className='text-align-center'>{offerLine.article.measureShort}</td>
							<td className='text-align-right'>{offerLine.ammount ? offerLine.ammount.toFixed(2) : ''}</td>
							<td className='text-align-right'>{offerLine.price ? offerLine.price.toFixed(2) : ''} {offerCurrency}</td>
							{this.props.hasShowDiscounts ? <td className='text-align-right'>{offerLine.discountPercent || ''} %</td> : undefined}
							{this.props.hasShowDiscounts ? <td className='text-align-right'>{discountedPrice ? discountedPrice.toFixed(2) : ''} {offerCurrency}</td> : undefined}
							<td className='text-align-right'>{(offerLine.ammount * discountedPrice).toFixed(2)} {offerCurrency}</td>
							{this.props.hasShowDiscounts ? <td className='text-align-right'>{offerLine.deliveryDate}</td> : undefined}
						</tr>
					)
			})
			let discountAmount = totalOfferPrice * (offer.discountPercent/100)
			let totalOfferPriceWithDiscount = (totalOfferPrice * (1 - offer.discountPercent/100))
			let VATAmount = totalOfferPriceWithDiscount * (offer.vatPercent/100)
			if(discountAmount > 0) {
				dataRows.push(
					<tr key={dataRows.length + 1}>
						<td className='no-border'></td>
						<td className='no-border'></td>
						<td className='no-border'></td>
						<td className='no-border'></td>
						{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
						{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
						<td className='no-border'>{dict[lang].TOTAL_PRICE_WITHOUT_ADDITIONAL_DISCOUNT}</td>
						<td className="text-align-right totalOfferPrice">{totalOfferPrice.toFixed(2)} {offerCurrency}</td>
					</tr>
				)
				dataRows.push(
					<tr key={dataRows.length + 2}>
						<td className='no-border'></td>
						<td className='no-border'></td>
						<td className='no-border'></td>
						<td className='no-border'></td>
						{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
						{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
						<td className='no-border'>{dict[lang].DISCOUNT_AMOUNT_ADDITIONAL} ({offer.discountPercent}%) </td>
						<td className="text-align-right totalOfferPrice">{discountAmount.toFixed(2)} {offerCurrency}</td>
					</tr>
				)
			}
			dataRows.push(
				<tr key={dataRows.length + 3}>
					<td className='no-border'></td>
					<td className='no-border'></td>
					<td className='no-border'></td>
					<td className='no-border'></td>
					{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
					{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
					<td className='no-border'>{dict[lang].TOTAL_PRICE_WITH_ADDITIONAL_DISCOUNT}</td>
					<td className="text-align-right totalOfferPrice">{totalOfferPriceWithDiscount.toFixed(2)} {offerCurrency}</td>
				</tr>
			)
			dataRows.push(
				<tr key={dataRows.length + 4}>
					<td className='no-border'></td>
					<td className='no-border'></td>
					<td className='no-border'></td>
					<td className='no-border'></td>
					{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
					{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
					<td className='no-border'>{dict[lang].VAT_AMOUNT}</td>
					<td className="text-align-right totalOfferPrice">{VATAmount.toFixed(2)} {offerCurrency}</td>
				</tr>
			)
			dataRows.push(
				<tr key={dataRows.length + 5}>
					<td className='no-border'></td>
					<td className='no-border'></td>
					<td className='no-border'></td>
					<td className='no-border'></td>
					{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
					{this.props.hasShowDiscounts ? <td className='no-border'></td> : undefined}
					<td className='no-border'>{dict[lang].TOTAL_PRICE_WITH_ADDITIONAL_DISCOUNT_AND_VAT}</td>
					<td className="text-align-right totalOfferPrice">{(totalOfferPriceWithDiscount + VATAmount).toFixed(2)} {offerCurrency}</td>
				</tr>
			)
			return (
				<div className={this.props.className}>
					{companyLogo}
					<br></br> <hr></hr> <br></br>
					<table>
						<thead>
							<tr>
								<th className="header text-align-left">{dict[lang].SUPPLIER}: {dict[lang][companyCode].NAME}</th>
								<th className="header-divider"> &nbsp; </th>
								<th className="header text-align-left">{dict[lang].BUYER}: {offer.person ? offer.person.name : ''}</th>
							</tr>
						</thead>
						<tbody>
							<tr><td> &nbsp; </td></tr>
							<tr>
								<td>
									{dict[lang].MOL+": "}{dict[lang][companyCode].MOL}
								</td>
								<td> &nbsp; </td>
								<td>
									{dict[lang].MOL+": "}{offer.person  ? offer.person.mol : ''}
								</td>
							</tr>
							<tr>
								<td>
									{dict[lang].EIK}: {dict[lang][companyCode].EIK}
								</td>
								<td> &nbsp; </td>
								<td>
									{dict[lang].EIK}: {offer.person  ? offer.person.eik : ''}
								</td>
							</tr>
							<tr>
								<td>
									{dict[lang].VAT}: {dict[lang][companyCode].VAT}
								</td>
								<td> &nbsp; </td>
								<td>
									{dict[lang].VAT}: {offer.person  ? offer.person.vatNumber : ''}
								</td>
							</tr>
							<tr>
								<td>
									{dict[lang].INVOICE_ADDRESS}: {dict[lang][companyCode].INVOICE_ADDRESS}
								</td>
								<td> &nbsp; </td>
								<td>
									{dict[lang].ADDRESS}: {offer.person  ? offer.person.address : ''}
								</td>
							</tr>
							<tr>
								<td>
									{dict[lang].DELIVERY_ADDRESS}: {dict[lang][companyCode].DELIVERY_ADDRESS}
								</td>
								<td> &nbsp; </td>
								<td>
								</td>
							</tr>
							<tr>
								<td>
									{offer.bankAccount  ? offer.bankAccount.bankName : ''}
									<br/>
									{dict[lang].BIC}: {offer.bankAccount  ? offer.bankAccount.bic : ''}
									&nbsp;
									{dict[lang].IBAN}: {offer.bankAccount  ? offer.bankAccount.iban : ''}
								</td>
								<td> &nbsp; </td>
								<td>
								</td>
							</tr>
						</tbody>
					</table>
					<hr/>
					<div className="text-align-center">
						<h1 className="text-align-center">{dict[lang].OFFER_TITLE}</h1>
						<br/>
						{dict[lang].REF_NUMBER}: <b> {offer.code} </b>
						<br/>
						{dict[lang].DATE}: <b> {new Date(offer.lastModifiedDate).toLocaleDateString(lang)} </b>
					</div>
					<br></br> <hr></hr> <br></br>


					<table className="offer-table">
						<thead>
							<tr>
								<th> {dict[lang].NO}</th>
								<th> {dict[lang].PRODUCT_DESCRIPTION}</th>
								{/* <th> {dict[lang].PN}</th> */}
								<th> {dict[lang].MEASURE}</th>
								<th> {dict[lang].QUANTITY}</th>
								<th> {dict[lang].UNIT_PRICE}</th>
								{this.props.hasShowDiscounts ? <th> {dict[lang].DISCOUNT}</th> : undefined}
								{this.props.hasShowDiscounts ? <th> {dict[lang].DISCOUNT_PRICE}</th> : undefined}
								<th> {dict[lang].TOTAL_PRICE}</th>
								{this.props.hasShowDiscounts ? <th> {dict[lang].DELIVERY_DEADLINE}</th> : undefined}
							</tr>
						</thead>
						<tbody>
							{dataRows}
						</tbody>
					</table>

					<br></br> <hr></hr> <br></br>

					<table>
						<tbody>
							<tr>
								<td> 
									{dict[lang].NOTES}: <br/>
									{offer.notes ? <pre>{offer.notes}</pre> : '' }
									{offer.guaranteeTerms ? <pre>{dict[lang].GUARANTEE_TERMS}: {offer.guaranteeTerms}</pre> : '' }
									{offer.discountCondition ? <pre>{dict[lang].DISCOUNT_CONDITION}: {offer.discountCondition}</pre> : '' }
									{offer.deliveryTerms ? <pre>{dict[lang].DELIVERY_TERMS}: {offer.deliveryTerms}</pre> : '' }
								</td>
								<td> {dict[lang].PROCESSED_BY}: {offer.processedBy} <br/>
									{dict[lang].DATE}: {new Date(offer.lastModifiedDate).toLocaleDateString(lang)}
								</td>
							</tr>
						</tbody>
					</table>
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

export default connect(mapStateToProps, mapDispatchToProps)(OfferTemplate);