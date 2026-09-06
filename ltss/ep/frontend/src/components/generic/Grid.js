import React from 'react';
import GridRow from './GridRow';

export default class Grid extends React.Component {
	constructor(props){
		super(props)
		this.state = {}
	}
	/*
		this.props:
					headers = the column headers
					data.data = the content data of the table
					gridClass = an html class used for the <table> component
					headerClass = an html class used for the <thead> component
					rowClass = an html class used for each <tr> component
					bodyCellClass = an html class used for each <td> component
					headerCellClass = an html class used for each <th> component in the header of the table

	*/
	render() {

		let rows = this.props.data.data.map((rowData, index) =>{
			return <GridRow 	key={index}
						rowClass={this.props.rowClass}
						cellClass={this.props.bodyCellClass}
						data={rowData} />
		})

		return (
			<table className={this.props.gridClass}>
				<thead className={this.props.headerClass} >
					<GridRow	key={Math.random() * 1000000}
								rowClass={this.props.rowClass}
								cellClass={this.props.headerCellClass}
								data={this.props.data.headers}
								isHeader={true} />
				</thead>
				<tbody>
					{rows}
				</tbody>
			</table>
		);
	}
}

Grid.displayName = 'GenericGrid';
Grid.propTypes = {};
Grid.defaultProps = {};
