import * as ActionTypes from '../actions/types';

const initialState = {
	visible: false,
	title: '',
	body: '',
	acceptLabel: 'Да',
	acceptCallback: undefined,
	refuseLabel: 'Не',
	refuseCallback: undefined
};

function modalReducer(state = initialState, action) {
	switch (action.type) {
		case ActionTypes.MODAL_SHOW:{
			return {
				...state,
				visible: true,
				title: action.title,
				body: action.body,
				acceptLabel: action.acceptLabel || state.acceptLabel,
				acceptCallback: action.acceptCallback,
				refuseLabel: action.refuseLabel || state.refuseLabel,
				refuseCallback: action.refuseCallback
			}
		}
		case ActionTypes.MODAL_HIDE:{
			return {
				...state,
				visible: false,
				title: '',
				body: '',
				acceptLabel: 'Да',
				acceptCallback: undefined,
				refuseLabel: 'Не',
				refuseCallback: undefined
			}
		}
		default: {
			return state;
		}
	}
}

module.exports = modalReducer;
