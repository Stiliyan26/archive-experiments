import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { convertNumbersToBgnCurrency, convertNumbersToEnglish } from './../../scripts/dataUtils';

class OrderTemplate extends React.Component {

	constructor(props){
		super(props)
		this.state = {
		}
	}

	render() {
		const { formData, originalOrCopy, lang, formDetailsData, ownCompanyData, auth } = this.props;
		let dataRows = []
		if(formData && !(formData instanceof Promise) && formDetailsData && formDetailsData != {} && (formDetailsData instanceof Array)){
			let offerCurrency = formData.currency ? formData.currency.name : '';
			dataRows = formDetailsData.map((detailsRow, lineIndex) => {
				return (
						<tr key={lineIndex + 1}>
							<td className='text-align-center'>{lineIndex + 1}</td>
							<td className='text-align-right'>{detailsRow.godId && detailsRow.godId.code ? detailsRow.godId.code : ''}</td>
							<td className='text-align-right'>{detailsRow.godId && detailsRow.godId.nameBg ? detailsRow.godId.nameBg : ''}</td>
							<td className='text-align-right'>{detailsRow.meeId && detailsRow.meeId.name ? detailsRow.meeId.name : ''}</td>
							<td className='text-align-right'>{detailsRow.quantityConfirm ? detailsRow.quantityConfirm.toFixed(2) : ''}</td>
							<td className='text-align-right'>{detailsRow.priceConfirm ? detailsRow.priceConfirm.toFixed(2) : ''}</td>
							<td className='text-align-right'>{(detailsRow.quantityConfirm * detailsRow.priceConfirm).toFixed(2)}</td>
						</tr>
					)
			})
			dataRows.push(
				<tr key={dataRows.length + 3}>
					<td className='no-border text-align-right' colSpan="6">Сума за плащане: </td>
					<td className="text-align-right totalOfferPrice">{this.props.formData.total ? this.props.formData.total.toFixed(2) : 0}</td>
				</tr>
			)
			dataRows.push(
				<tr key={dataRows.length + 5}>
					<td className='no-border text-align-right' colSpan="5">Сума за плащане словом: {
						convertNumbersToBgnCurrency(Math.floor(this.props.formData.total))+" "+offerCurrency+" и "+convertNumbersToBgnCurrency(Math.floor((this.props.formData.total - Math.floor(this.props.formData.total))*100))
					}</td>
				</tr>
			)
			return (
				<div className={this.props.className}>
					<hr></hr>
					<table>
						<tbody>
							<tr>
								<td>ЗАЯВИТЕЛ</td>
								<td> &nbsp; </td>
								<td> &nbsp; </td>
								<td>ДОСТАВЧИК</td>
								<td> &nbsp; </td>
							</tr>
							<tr>
								<td>име:</td>
								<td>{ownCompanyData.name}</td>
								<td> &nbsp; </td>
								<td>име:</td>
								<td>{this.props.formData.parId.name}</td>
							</tr>
							<tr>
								<td>Адрес:</td>
								<td>{ownCompanyData.address}</td>
								<td> &nbsp; </td>
								<td>Адрес:</td>
								<td>{this.props.formData.parId.address}</td>
							</tr>
							<tr>
								<td>Ид. Номер:</td>
								<td>{ownCompanyData.bulstat}</td>
								<td> &nbsp; </td>
								<td>Ид. Номер:</td>
								<td>{this.props.formData.parId.bulstat}</td>
							</tr>
							<tr>
								<td>ИН по ЗДДС:</td>
								<td>{ownCompanyData.vatNo}</td>
								<td> &nbsp; </td>
								<td>ИН по ЗДДС:</td>
								<td>{this.props.formData.parId.vatNo}</td>
							</tr>
							<tr>
								<td>МОЛ:</td>
								<td>{ownCompanyData.mol}</td>
								<td> &nbsp; </td>
								<td>МОЛ:</td>
								<td>{this.props.formData.parId.mol}</td>
							</tr>
						</tbody>
					</table>
					<hr></hr>
					<h1 className="text-align-center">Поръчка към доставчик</h1>
					Номер: {String(this.props.formData.orderNum).padStart(10, '0')}<br/>
					Дата: {new Date(this.props.formData.dateOrr).toLocaleDateString(lang)}
					<hr></hr>
					<table className="offer-table">
						<thead>
							<tr>
								<th>№</th>
								<th>Кат. Номер</th>
								<th>Описание</th>
								<th>М. ед.</th>
								<th>Кол.</th>
								<th>Ед. цена</th>
								<th>Сума</th>
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
								<td></td>
								<td></td>
								<td> &nbsp; </td>
								<td></td>
								<td></td>
							</tr>
							<tr>
								<td>Изпратил:</td>
								<td></td>
								<td> &nbsp; </td>
								<td>Получил:</td>
								<td></td>
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
	};
}
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

export default connect(mapStateToProps, mapDispatchToProps)(OrderTemplate);