import auth from './auth';

import { RESTReducer } from './taskReducers';

import modal from './modal'
import storage from 'redux-persist/es/storage'
import * as ActionTypes from '../actions/types';
import { persistCombineReducers } from 'redux-persist'

const config = {
	key: 'root',
	storage,
	whitelist: ['auth'] //only these will be persisted
}
const reducers = { auth, rest: RESTReducer, modal };

let persistedReducer = persistCombineReducers(config, reducers);
const rootReducer = (state, action) => {
	if (action.type === ActionTypes.LOGOUT) {
		state.rest = {};
		state.auth = {};
	}
	return persistedReducer(state, action)
};

module.exports = rootReducer
