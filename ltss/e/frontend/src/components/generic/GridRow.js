import React from 'react';
import GridCell from './GridCell';

export default class GridRow extends React.Component {

	constructor(props){
		super(props);
		this.state={};
	}

	render() {
		let cells = [];
		for (var cellProp in this.props.data){
			cells.push( <GridCell 	key={Math.random() * 1000000}
									cellClass={this.props.cellClass}
									text={this.props.data[cellProp]}
									isHeader={this.props.isHeader} />)
		}

		return (
			<tr className={this.props.rowClass}>
				{cells}
			</tr>
		);
	}
}

GridRow.displayName = 'GenericGridRow';
GridRow.propTypes = {};
GridRow.defaultProps = {};
