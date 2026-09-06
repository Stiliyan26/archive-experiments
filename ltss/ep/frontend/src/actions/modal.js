import * as ActionTypes from './types';



export function showModal(params){
	return {
		type: ActionTypes.MODAL_SHOW,
		title: params.title,
		body: params.body,
		acceptLabel: params.acceptLabel,
		acceptCallback: params.acceptCallback,
		refuseLabel: params.refuseLabel,
		refuseCallback: params.refuseCallback
	}
}
export function hideModal() {
	return{
		type: ActionTypes.MODAL_HIDE,
	}
}