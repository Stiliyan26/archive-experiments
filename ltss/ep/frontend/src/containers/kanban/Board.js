// @flow
import React from 'react';
import { DragDropContext, Droppable } from 'react-beautiful-dnd';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import Column from './Column';
import { colors } from './constants';

class Board extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			updatedColumns: this.props.columns.map((column) => (0)),
			updated: 0
		}
		this.onDragEnd = this.onDragEnd.bind(this);
	}
	
	reorder(list, startIndex, endIndex) {
		const result = Array.from(list);
		const [removed] = result.splice(startIndex, 1);
		result.splice(endIndex, 0, removed);

		return result;
	}
	
	reorderCards({columns,source,destination}) {
		const current = [...columns[source.droppableId].cards];
		const next = [...columns[destination.droppableId].cards];
		const target = current[source.index];

		this.setState({updatedColumns: {
				...this.state.updatedColumns,
				[source.droppableId]: this.state.updatedColumns[source.droppableId]+1,
				[destination.droppableId]: this.state.updatedColumns[destination.droppableId]+1,
			},
			updated: this.state.updated+1
		})
		
		// moving to same list
		if (source.droppableId === destination.droppableId) {
			const reordered = this.reorder(current, source.index, destination.index);
			let result = columns;
			result[source.droppableId].cards = reordered;
			return {
				columns: result,
			};
		}

		// moving to different list

		// remove from original
		current.splice(source.index, 1);
		// insert into next
		next.splice(destination.index, 0, target);
		
		let result = columns;
		result[source.droppableId].cards = current;
		result[destination.droppableId].cards = next;

		return {
			columns: result,
		};
	};

	onDragEnd(result) {
		// dropped nowhere
		if (!result.destination) {
			return;
		}
		
		const source = result.source;
		const destination = result.destination;
		
		// did not move anywhere - can bail early
		if (source.droppableId === destination.droppableId &&
			source.index === destination.index) {
			return;
		}
		
		// reordering column
		if (result.type === 'COLUMN') {
			this.props.onReorder(this.reorder(this.props.columns, source.index, destination.index));
			let updatedColumns = this.state.updatedColumns;
			for(let i = Math.min(source.index, destination.index); i <= Math.max(source.index, destination.index); i++) {
				updatedColumns[i] = updatedColumns[i] + 1;
			}
			this.setState({updatedColumns: updatedColumns});
			return;
		}
		
		let data = this.reorderCards({
			columns: this.props.columns,
			source,
			destination,
		});
		
		this.props.onReorder(data.columns);
	}
	
	sendToBack(columnId) {
		this.onDragEnd({source: {droppableId: "board", index: columnId}, destination: {droppableId: "board", index: this.props.columns.length-1}, type: "COLUMN"});
	}

	render() {
		//console.log('render board',this.props);
		const columns = this.props.columns;
		const { containerHeight } = this.props;
		
		const board = (
			<Droppable
				droppableId="board"
				type="COLUMN"
				direction="horizontal"
				ignoreContainerClipping={Boolean(containerHeight)}
				totalUpdated={this.state.updated}
			>
				{(provided) => (
					<div
						ref={provided.innerRef} 
						{...provided.droppableProps} 
						style={{
							minHeight:"100vh",
							/* like display:flex but will allow bleeding over the window width */
							minWidth:"100vw",
							display:"inline-flex",
						}}
					>
						{columns.map((col, index) => {
							return <Column
									key={index.toString()}
									columnId={index}
									index={index}
									title={col.fullName}
									cards={col.cards}
									componentPath={this.props.componentPath+"."+(index)+".cards"} //existing path in redux store where we put data
									updated={this.state.updatedColumns[index]}
									totalUpdated={this.state.updated}
									onSendToBack={(columnId) => this.sendToBack(columnId)}
								/>;
						})}
					</div>
				)}
			</Droppable>
		);
		return (
			<DragDropContext
				onDragEnd={this.onDragEnd}
			>
				{this.props.containerHeight ? (
					<div
						height={containerHeight} 
						style={{
							height: this.props.height,
							overflowX: "hidden",
							overflowY: "auto",
						}}
					>
						{board}
					</div>
				) : (
					board
				)}
			</DragDropContext>
		);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const data = state.rest.kanbanTaskAssigned;
	return {
		data: data,
		columns: ownProps.columns,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(Board);
const WithRouterComponent = withRouter(ConnectComponent);
export default WithRouterComponent;

