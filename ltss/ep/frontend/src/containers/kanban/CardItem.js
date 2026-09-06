// @flow
import React from 'react';
import ReactDOM from 'react-dom';
//import styled from 'styled-components';
import { borderRadius, colors, grid } from './constants';
import FieldNomenclatureSelectContainer from '../fields/FieldNomenclatureSelectContainer';
import FieldEntitySearchContainer from '../fields/FieldEntitySearchContainer'

// Previously this extended React.Component
// That was a good thing, because using React.PureComponent can hide
// issues with the selectors. However, moving it over does can considerable
// performance improvements when reordering big lists (400ms => 200ms)
// Need to be super sure we are not relying on PureComponent here for
// things we should be doing in the selector as we do not know if consumers
// will be using PureComponent
export default class CardItem extends React.PureComponent {
	render() {
		const { card, isDragging, provided } = this.props;
		
		const style = {
			borderRadius: borderRadius+"px",
			border: "1px solid grey",
			backgroundColor: (isDragging ? colors.green : colors.white),
			
			boxShadow: (isDragging ? `2px 2px 1px ${colors.shadow}` : 'none'),
			padding: grid+"px",
			minHeight: "40px",
			margin: "0 0 "+grid+"px 0",
			userSelect: "none",
			transition: "background-color 0.1s ease",
			
			/* anchor overrides */
			color: colors.black,
			
			//		&:hover {
			//		  color: ${colors.black};
			//		  text-decoration: none;
			//		}
			//		&:focus {
			//		  outline: 2px solid ${colors.purple};
			//		  box-shadow: none;
			//		}
			
			/* flexbox */
			display: "flex",
			alignItems: "center",
			...provided.draggableProps.style,
		};
		return (
			<div 
				ref={provided.innerRef}
				{...provided.draggableProps}
				{...provided.dragHandleProps}
				style={style}
			>
				{/*<img 
					style={{
						width: "40px",
						height: "40px",
						borderRadius: "50%",
						marginRight: grid+"px",
						flexShrink: "0",
						flexGrow: "0",
					}} 
					src={card.assigned ? card.assigned.avatarUrl : null} 
					alt={card.assigned ? card.assigned.name : null}
				/>*/}
				<div 
					style={{
						/* flex child */
						flexGrow: "1",
						
						/* Needed to wrap text in ie11 */
						/* https://stackoverflow.com/questions/35111090/why-ie11-doesnt-wrap-the-text-in-flexbox */
						flexBasis: "100%",
						
						/* flex parent */
						display: "flex",
						flexDirection: "column",
					}}
				>
					<div
						//		&::before {
						//		  content: open-quote;
						//		}
						//	
						//		&::after {
						//		  content: close-quote;
						//		}
					>
						<FieldEntitySearchContainer 
							href={undefined}
							componentPath={this.props.componentPath} //existing path in redux store where we put data
							entityType={"tasks"}
							onChange={(href) => {}}
						/>
					</div>
					<div 
						style={{
							display: "flex",
							marginTop: grid+"px",
						}}
					>
						<small 
							style={{
								flexGrow: "0",
								margin: "0",
							}}
						>
							{/*card.assigned ? card.assigned.name : null*/}
						</small>
						<small 
							style={{
								margin: "0",
								marginLeft: grid+"px",
								textAlign: "right",
								flexGrow: "1",
							}}
						>
							{card.status ? card.status.name : null}
						</small>
					</div>
					<div 
						style={{
							display: "flex",
							marginTop: grid+"px",
						}}
					>
						<small 
							style={{
								flexGrow: "0",
								margin: "0",
							}}
						>
							(id: {card.id})
						</small>
						<small 
							style={{
								margin: "0",
								marginLeft: grid+"px",
								textAlign: "right",
								flexGrow: "1",
							}}
						>
							{/*card.assigned ? card.assigned.name : null*/}
							<FieldNomenclatureSelectContainer 
								href={card._links && card._links.assigned ? card._links.assigned.href : undefined} //REST URI to load from
								componentPath={this.props.componentPath+".assigned"} //existing path in redux store where we put data
								nomenclatureKey={"secUsers"}
								displayAttr={"fullName"}
							/>
						</small>
					</div>
				</div>
			</div>
		);
	}
}
