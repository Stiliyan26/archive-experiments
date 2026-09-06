import i18n from 'i18next';
import * as Constants from '../static/constants';

import { FETCH_REST_SUCCESS, FETCH_REST_FAIL, FETCH_REST_INPROGRESS, REST_PATCH_FAIL, REST_POST_FAIL, CLEAN_REST_DATA, EDIT_REST_DATA, DELETE_REST_FAIL } from '../actions/taskActions'

import { resolveObjectPath, deleteObjectPath, buildObjectPathAndAssign } from './../scripts/dataUtils'

function handleActionError(nextState, action) {
	if(action.error && action.error.response && action.error.response.status == 404) {
		//TODO should we handle 404 somehow when it is not for entities?
	} else {
		if(!(nextState.notifications instanceof Array)) {
			nextState.notifications = [];
		}

		let message = action.error.message;
		if(action.error.response) {
			if(action.error.response.status == 403) {
				message = i18n.t("Error.403")+action.error.message+"; "+JSON.stringify(action.error.response.data)+"; "+action.error.config.method+"; "+action.error.config.url+"; "+action.error.config.data;
			} else if(action.error.response.status == 401) {
				message = i18n.t("Error.401")+action.error.message+"; "+JSON.stringify(action.error.response.data)+"; "+action.error.config.method+"; "+action.error.config.url+"; "+action.error.config.data;
			} else if(action.error.response.status == 400) {
				message = i18n.t("Error.400")+action.error.message+"; "+JSON.stringify(action.error.response.data)+"; "+action.error.config.method+"; "+action.error.config.url+"; "+action.error.config.data;
			}
		}
		
		nextState.notifications.push({timestamp: new Date(), statePath: action.statePath, message: message, error: action.error});
		if(action.error && action.error.response && action.error.response.status == 403) {
			//TODO remove auth? or just the flag and put username in login screen?
		}
	}
}

export function RESTReducer(state = {}, action) {
	switch (action.type) {
		case CLEAN_REST_DATA:
			let nextState = Object.assign({},state); //copy the state
			deleteObjectPath(action.statePath,nextState); //get the referenced attribute
			return nextState;
		case EDIT_REST_DATA:
			nextState = Object.assign({},state); //copy the state
			buildObjectPathAndAssign(action.statePath,nextState,action.value); //get the referenced attribute
			return nextState;
		case FETCH_REST_INPROGRESS:
			//console.log('Reducing: ',JSON.stringify(state));
			nextState = Object.assign({},state); //copy the state
			let loading = resolveObjectPath(action.statePath+"."+Constants.PATH_FOR_LOADING,nextState);
			if(loading instanceof Promise) {
				//TODO cancel with token
				//loading.
			}
			buildObjectPathAndAssign(action.statePath+"."+Constants.PATH_FOR_LOADING,nextState,action.promise); //assign promise to path
			//console.log('Next state: ',JSON.stringify(nextState));
			return nextState;
		case FETCH_REST_SUCCESS:
			if(action.mergeObj instanceof Array) {
				//console.log('Reducing: ',JSON.stringify(state),' with ',action.mergeObj);
				let nextState = Object.assign({},state); //copy the state
				action.mergeObj.forEach((mergeItem) => {
					buildObjectPathAndAssign(mergeItem.path,nextState,mergeItem.value); //get the referenced attribute
				})
				//console.log('Next state: ',JSON.stringify(nextState));
				return nextState;
			}
		case FETCH_REST_FAIL:
			nextState = Object.assign({},state); //copy the state
			buildObjectPathAndAssign(action.statePath,nextState,action.error); //get the referenced attribute
			handleActionError(nextState, action);
			return nextState;
		case REST_PATCH_FAIL:
			nextState = Object.assign({},state); //copy the state
			handleActionError(nextState, action);
			buildObjectPathAndAssign(action.statePath+"."+Constants.PATH_FOR_ERROR,nextState,action.error); //get the referenced attribute
			deleteObjectPath(action.statePath+"."+Constants.PATH_FOR_LOADING,nextState);
			return nextState;
		case REST_POST_FAIL:
			nextState = Object.assign({},state); //copy the state
			handleActionError(nextState, action);
			buildObjectPathAndAssign(action.statePath+"."+Constants.PATH_FOR_ERROR,nextState,action.error); //get the referenced attribute
			deleteObjectPath(action.statePath+"."+Constants.PATH_FOR_LOADING,nextState);
			return nextState;
		case DELETE_REST_FAIL:
			nextState = Object.assign({},state); //copy the state
			handleActionError(nextState, action);
			buildObjectPathAndAssign(action.statePath+"."+Constants.PATH_FOR_ERROR,nextState,action.error); //get the referenced attribute
			deleteObjectPath(action.statePath+"."+Constants.PATH_FOR_LOADING,nextState);
			return nextState;
		default:
			return state;
	}
}