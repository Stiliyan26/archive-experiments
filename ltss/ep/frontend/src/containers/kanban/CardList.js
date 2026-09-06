// @flow
import React from 'react';
import { Droppable, Draggable } from 'react-beautiful-dnd';
import CardItem from './CardItem';
import { grid, colors } from './constants';

class InnerCardList extends React.Component {
	shouldComponentUpdate(nextProps) {
		if (nextProps.cards !== this.props.cards || nextProps.updated != this.props.updated) {
			return true;
		}
		return false;
	}

	render() {
		return this.props.cards.map((card, index) => (
			<Draggable 
				key={card.id} 
				draggableId={card.id} 
				index={index}
			>
				{(dragProvided, dragSnapshot) => (
					<CardItem
						key={card.id}
						card={card}
						isDragging={dragSnapshot.isDragging}
						provided={dragProvided}
						componentPath={this.props.componentPath+"."+index} //existing path in redux store where we put data
						updated={this.props.updated}
					/>
				)}
			</Draggable>
		));
	}
}

class InnerList extends React.Component {
	render() {
		const { cards, dropProvided } = this.props;
		const title = this.props.title ? (
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
			>
				{this.props.title}
			</h4>
		) : null;

		return (
			<div>
				{title}
				<div 
					style={{
						/* stop the list collapsing when empty */
						minHeight: "250px",
						/* not relying on the items for a margin-bottom
						as it will collapse when the list is empty */
						marginBottom: grid+"px",
					}} 
					ref={dropProvided.innerRef}
				>
					<InnerCardList
						cards={cards}
						componentPath={this.props.componentPath} //existing path in redux store where we put data
						updated={this.props.updated}
					/>
					{dropProvided.placeholder}
				</div>
			</div>
		);
	}
}

export default class CardList extends React.Component {
	render() {
		const {
			ignoreContainerClipping,
			internalScroll,
			isDropDisabled,
			listId,
			listType,
			style,
			cards,
			title,
		} = this.props;
		
		return (
			<Droppable
				droppableId={listId.toString()}
				type={listType}
				ignoreContainerClipping={ignoreContainerClipping}
				isDropDisabled={isDropDisabled}
			>
				{(dropProvided, dropSnapshot) => (
					<div 
						style={{
							...style,
							backgroundColor: (dropSnapshot.isDraggingOver ? colors.blue.lighter : colors.blue.light),
							display: "flex",
							flexDirection: "column",
							opacity: (isDropDisabled ? 0.5 : 'inherit'),
							padding: grid+"px",
							paddingBottom: "0",
							transition: "background-color 0.1s ease, opacity 0.1s ease",
							userSelect: "none",
							width: "250px",
						}}
						{...dropProvided.droppableProps}
					>
						{internalScroll ? (
							<div 
								style={{
									overflowX: "hidden",
									overflowY: "auto",
									maxHeight: "300px",
								}}>
								<InnerList
									cards={cards}
									title={title}
									dropProvided={dropProvided}
									componentPath={this.props.componentPath} //existing path in redux store where we put data
									updated={this.props.updated}
								/>
							</div>
						) : (
							<InnerList
								cards={cards}
								title={title}
								dropProvided={dropProvided}
								componentPath={this.props.componentPath} //existing path in redux store where we put data
								updated={this.props.updated}
							/>
						)}
					</div>
				)}
			</Droppable>
		);
	}
}
