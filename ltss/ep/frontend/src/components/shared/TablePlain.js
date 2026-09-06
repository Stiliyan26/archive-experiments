import React from 'react';

export default class TablePlain extends React.Component {

	constructor(props){
		super(props)
	}

	render() {
		const { headers, data, hidden, id } = this.props;
		let dataHeaders = headers.map((header, index) => {
			return <th key={index+"."+header}>{header}</th>
		})
		let dataRows = data.map((rowData, trIndex) => {
			return <tr key={trIndex+"."+rowData}>{
				rowData.map((cell, tdIndex) => {
					return <td key={tdIndex+"."+cell}>{cell}</td>
				})
			}</tr>
		})

		return (
			<table className={this.props.className} id={id}>
				<thead>
					<tr>
						{dataHeaders}
					</tr>
				</thead>
				<tbody>
					{dataRows}
				</tbody>
			</table>
		);
	}
}

TablePlain.displayName = 'TablePlain';
TablePlain.propTypes = {};
TablePlain.defaultProps = {};
