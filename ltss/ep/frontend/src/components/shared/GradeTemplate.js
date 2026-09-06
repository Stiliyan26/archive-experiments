import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { resolveObjectPath } from './../../scripts/dataUtils';

class GradeTemplate extends React.Component {

	constructor(props){
		super(props)
		this.state = { }
	}

	render() {
		const { grade, employeeCompany, employeePosition, employeeDepartment } = this.props;
		let dataRows = []
		if(grade && !(grade instanceof Promise) && grade.employee && !(grade.employee instanceof Promise)  ){
			let employee = grade && grade.employee ? grade.employee : undefined
			return (
				<div className={this.props.className}>
					<div className='text-align-center margin-top-20'> ФОРМУЛЯР </div>
					<div className='text-align-center margin-top-10 margin-bottom-20'>
						за оценка на изпълнението на длъжността от служители
					</div>
					<table className='grade-table margin-top-20 margin-bottom-20'>
						<tbody>
							<tr>
								<td> На: {employee ? employee.name : ''} </td>
								<td> &nbsp; </td>
							</tr>
							<tr className='border-row-dashed'>
								<td className='text-align-center'> <sup> (име, презиме, фамилия) </sup> </td>
								<td> &nbsp; </td>
							</tr>
							<tr className='border-row-dashed'>
								<td colSpan='2'> Длъжност: {employeePosition && employeePosition.name ? employeePosition.name : ''} </td>

							</tr>
							<tr className='border-row-dashed'>
								<td> Отдел: {employeeDepartment && employeeDepartment.name ? employeeDepartment.name : ''} </td>
								<td> Компания: {employeeCompany && employeeCompany.name ? employeeCompany.name : ''}</td>
							</tr>
							<tr className='border-row-dashed'>
								<td> Период за оценяване от: {grade ? new Date(grade.attestationFromDate).toLocaleDateString('bg')  : ''} </td>
								<td> до: {grade ? new Date(grade.attestationToDate).toLocaleDateString('bg')  : ''} </td>
							</tr>
						</tbody>
					</table>
					<div className='text-align-left text-bold margin-top-20'>
						1. ОБОБЩЕНИЕ НА ОСНОВНИТЕ ЦЕЛИ И ЗАДАЧИ НА СЛУЖИТЕЛЯ
					</div>
					<div className='duties-summary margin-top-20 margin-bottom-20'> {grade ? grade.employeeGoals : ''} </div>
					<div className='text-align-left text-bold margin-top-20'>
						2. ОЦЕНКА
					</div>
					<div className='duties-summary margin-top-20 margin-bottom-20'> {grade ? grade.attestationText : ''} </div>
					<div className='text-align-left text-bold margin-top-20'>
						3. МОТИВИ НА ОЦЕНЯВАЩИЯ ЗА ОПРЕДЕЛЯНЕ НА ОЦЕНКАТА
					</div>
					<div className='duties-summary margin-top-20 margin-bottom-20'> {grade ? grade.motivesText : ''} </div>
					<div className='text-align-left text-bold margin-top-20'>
						4. ПОТЕНЦИАЛ ЗА РАЗВИТИЕ НА СЛУЖИТЕЛЯ
					</div>
					<div className='duties-summary margin-top-20 margin-bottom-20'> {grade ? grade.potentialText : ''} </div>
					<div className='text-align-left text-bold margin-top-20'>
						5. КОМЕНТАР НА СЛУЖИТЕЛЯ
					</div>
					<div className='duties-summary margin-top-20 margin-bottom-20'> {grade ? grade.employeeCommentText : ''} </div>
					<div className='text-align-left text-bold margin-top-20'>
						6. КОМЕНТАР НА КОНТРОЛИРАЩИЯ РЪКОВОДИТЕЛ
					</div>
					<div className='duties-summary margin-top-20 margin-bottom-20'> {grade ? grade.controllingOfficerCommentText : ''} </div>


					<div className='text-align-left margin-top-20'> Дата: ...................................г. </div>

					<table className='margin-top-20 margin-bottom-20'>
						<tbody>
							<tr>
								<td>
									Подпис на оценяващия ръководител:
								</td>
								<td >
									Подпис на оценявания:

								</td>
							</tr>
							<tr>
								<td className='text-align-center'>
									/ {grade && grade.certifier ? grade.certifier.name : 'ръководител'} /
								</td>
								<td className='text-align-center'>
									/ {employee ? employee.name : ''} /
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
	return {};
}
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

export default connect(mapStateToProps, mapDispatchToProps)(GradeTemplate);