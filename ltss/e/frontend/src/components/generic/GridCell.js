import React from 'react';

export default class GridCell extends React.Component {

	constructor(props){
		super(props);
		this.state = {};
	}

	render() {
		if(this.props.isHeader){
			return (
					<th className={this.props.cellClass}>
						{this.props.text}
					</th>
			);
			}
		else{
			return (
				<td className={this.props.cellClass}>
					{this.props.text}
				</td>
			);
		}
	}
}

GridCell.displayName = 'GenericGridCell';
GridCell.propTypes = {};
GridCell.defaultProps = {};

