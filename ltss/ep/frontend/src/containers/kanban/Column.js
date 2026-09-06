// @flow
import React from 'react';

import { Button } from 'react-bootstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

import { grid, colors, borderRadius } from './constants';
import { Draggable } from 'react-beautiful-dnd';
import CardList from './CardList';

export default class Column extends React.Component {
	render() {
		const { title, cards, index, columnId } = this.props;
		
		return (
			<Draggable 
				draggableId={columnId} 
				index={index}
			>
				{(provided, snapshot) => {
					const style = {
						margin: grid+"px",
						display: "flex",
						flexDirection: "column",
						...provided.draggableProps.style,
					};
					return <div 
							ref={provided.innerRef}
							{...provided.draggableProps}
							style={style}
						>
							<div 
								style={{
									display: "flex",
									alignItems: "center",
									justifyContent: "center",
									borderTopLeftRadius: borderRadius+"px",
									borderTopRightRadius: borderRadius+"px",
									backgroundColor: (snapshot.isDragging ? colors.blue.lighter : colors.blue.light),
									transition: "background-color 0.1s ease",
									
									//	  &:hover {
									//	    background-color: ${colors.blue.lighter};
									//	  }
								}}
							>
								<h4 
									style={{
										padding: grid+"px",
										transition: "background-color ease 0.2s",
										flexGrow: "1",
										userSelect: "none",
										position: "relative",
										//	  &:focus {
										//	    outline: 2px solid ${colors.purple};
										//	    outline-offset: 2px;
										//	  }
									}}
									{...provided.dragHandleProps}
								>
									{title}
								</h4>
								<Button variant="outline-dark" title={this.props.t("Kanban.SendToBack")} onClick={(e) => {this.props.onSendToBack(columnId);}}
									style={{
										margin: grid+"px",
									}}><FontAwesomeIcon icon="fast-forward"/>
								</Button>
							</div>
							<CardList
								listId={columnId}
								listType="CARD"
								cards={cards}
								componentPath={this.props.componentPath} //existing path in redux store where we put data
								updated={this.props.updated+this.props.totalUpdated}
							/>
						</div>;
				}}
			</Draggable>
		);
	}
}
