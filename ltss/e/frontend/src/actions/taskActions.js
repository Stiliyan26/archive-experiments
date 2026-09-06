import axios from 'axios';
import lodashIsObject from 'lodash/isObject'
import lodashIsEmpty from 'lodash/isEmpty'

import * as Constants from '../static/constants';
import { buildObjectPathAndAssign, resolveObjectPath } from './../scripts/dataUtils'
import { logout } from './auth'

export const FETCH_REST_INPROGRESS = 'FETCH_REST_INPROGRESS'
export const FETCH_REST_SUCCESS = 'FETCH_REST_SUCCESS'
export const FETCH_REST_FAIL = 'FETCH_REST_FAIL'
export const REST_PATCH_FAIL = 'REST_PATCH_FAIL'
export const REST_POST_FAIL = 'REST_POST_FAIL'
export const CLEAN_REST_DATA = 'CLEAN_REST_DATA'
export const EDIT_REST_DATA = 'EDIT_REST_DATA'
export const DELETE_REST_FAIL = 'DELETE_REST_FAIL'

function fetchRESTInProgress(promise,statePath,origin) {
	return {
		type: FETCH_REST_INPROGRESS,
		statePath,
		promise,
		origin
	};
}

function fetchRESTSuccess(mergeObj,origin) {
	return {
		type: FETCH_REST_SUCCESS,
		mergeObj,
		origin
	};
}

function actionRESTFail(type,error,statePath,origin) {
	return {
		type: type,
		statePath,
		error,
		origin
	};
}

function restFail(dispatch,type,error,statePath,origin) {
	dispatch(actionRESTFail(type,error,statePath,origin));
	if(error && error.response && error.response.status == 401) {
		dispatch(logout());
	}
}

function cleanRESTData(statePath) {
	return {
		type: CLEAN_REST_DATA,
		statePath
	};
}

function editRESTData(statePath,value) {
	return {
		type: EDIT_REST_DATA,
		statePath,
		value
	};
}

export function dispatchCleanRESTData(statePath) {
	return (dispatch) => {
		dispatch(cleanRESTData(statePath));
	}
}

export function dispatchEditRESTData(statePath,value) {
	return (dispatch) => {
		dispatch(editRESTData(statePath,value));
	}
}


//TODO remove debug limiter or add limit restore on user action
var rest_call_limit = 100;

export function resetRESTCallLimit(newLimit) {
	rest_call_limit = newLimit ? newLimit : 100;
}

export function patchRESTMultiData(patches,finalCallback,origin) {
	return (dispatch) => {
		import("moment").then(moment => {
			moment.locale('bg');
			let promises = [];
			//TODO One call for all patches (bulk update)
			patches.forEach((curr) => {
				let {restConfig,statePath,mapping,callback} = curr;
				console.log('PATCH REST request',JSON.stringify(restConfig));
				restConfig.headers = {...restConfig.headers, Authorization: sessionStorage["X-AUTH-TOKEN"]};
				restConfig.data = restConfig.data.map((elem) => {
					let newElem = elem;
					if(moment(newElem.value, moment.ISO_8601).isValid()) {
						newElem.value = moment(newElem.value, moment.ISO_8601).format("YYYY/MM/DD HH:mm:ss ZZ"); //format for java.util.Date.parse
					}
					return newElem;
				});
				const promise = axios(restConfig)
					.then(response => {
						console.log('PATCH REST response',JSON.stringify(response));
						let value = (typeof mapping === 'string' || mapping instanceof String) ?
								resolveObjectPath(mapping,response)
								: mapping(response);
						let mergeObj = ([{
							path: statePath,
							value: value
						}]);
						dispatch(fetchRESTSuccess(mergeObj,origin));
						callback(value);
						return response;
					})
					.catch(error => restFail(dispatch,REST_PATCH_FAIL,error,statePath,origin));
				promises.push(promise);
				dispatch(fetchRESTInProgress(promise,statePath,origin));
			});
			Promise.all(promises)
				.then(responses => {
					finalCallback(responses);
				});
		});
	};
}

export function patchRESTData(restConfig,statePath,mapping,callback,origin,outPromise = {}) {
	return (dispatch) => {
		//console.log('PATCH REST initial request',JSON.stringify(restConfig.data));
		let postData = Object.assign({},restConfig.data);
		for(let key in restConfig.data) {
			if(restConfig.data[key] instanceof Error) {
				//console.log('remove Error object',key);
				delete postData[key];
			}
			if(restConfig.data[key] instanceof Array) {
				restConfig.data[key].forEach((element,index) => {
					if(element._links && element._links.self) { //only if there is link
						postData[key][index] = element._links.self.href;
					}
				})
			} else {
				if(restConfig.data._links) {
					if(restConfig.data._links[key]) { //copy link for linked
						//console.log('key',key);
						if(restConfig.data[key] !== null) { //if not cleared
							if(restConfig.data[key] && restConfig.data[key]._links && restConfig.data[key]._links.self) { //only if there is link
								postData[key] = restConfig.data[key]._links.self.href;
							} else {
								//console.log('reset not edited object',key);
								delete postData[key];
							}
						}
					}
				}
			}
		}
		delete postData._links;
		restConfig.data = postData;
		restConfig.headers = {...restConfig.headers, Authorization: sessionStorage["X-AUTH-TOKEN"]};
		//console.log('PATCH REST request',JSON.stringify(restConfig));
		const promise = axios(restConfig)
		.then(response => {
			//console.log('PATCH REST response',JSON.stringify(response));
			let value = (typeof mapping === 'string' || mapping instanceof String) ?
					resolveObjectPath(mapping,response)
					: mapping(response);
			let mergeObj = ([{
				path: statePath,
				value: value
			}]);
			dispatch(fetchRESTSuccess(mergeObj,origin));
			callback(value);
			return response;
		})
		.catch(error => {
			restFail(dispatch,REST_PATCH_FAIL,error,statePath,origin);
			return error;
		});
		outPromise.promise = promise;
		dispatch(fetchRESTInProgress(promise,statePath,origin));
	}
}

//remove all keys starting with "_" except "_links" and "_embedded"
function deleteExtraKeysRecursive(obj) {
	//console.log("copyWithoutExtraKeysRecursive enter",JSON.stringify(obj));
	if(lodashIsObject(obj)) {
		let newObj = obj;
		for(let key in obj) {
			if(!key.startsWith("_") || key=="_links" || key=="_embedded") {
				let keyValue = deleteExtraKeysRecursive(newObj[key]);
				if(keyValue === undefined) {
					delete newObj[key];
				}
			} else {
				delete newObj[key];
			}
		}
		//console.log("copyWithoutExtraKeysRecursive change",JSON.stringify(newObj));
		if(lodashIsEmpty(newObj)) {
			return undefined;
		}
		return newObj;
	} else {
		return obj;
	}
}

//remove all keys starting with "_" except "_links" and "_embedded"
function deepCloneWithoutExtraKeys(obj) {
	//console.log("deepCloneWithoutExtraKeys enter",JSON.stringify(obj));
	if(lodashIsObject(obj)) {
		//shallow copy
		let newObj;
		if(obj instanceof Array) {
			// console.warn('obj ARRAY deepClone: ', obj)
			newObj = obj.map((elem) => deepCloneWithoutExtraKeys(elem));
		} else {
			// console.warn('obj NOT ARRAY deepClone: ', obj)
			newObj = Object.assign({},obj);
			for(let key in obj) {
				if(!key.startsWith("_") || key=="_links" || key=="_embedded") {
					let keyValue = deepCloneWithoutExtraKeys(newObj[key]);
					if(keyValue === undefined) {
						delete newObj[key];
					} else {
						newObj[key] = keyValue;
					}
				} else {
					delete newObj[key];
				}
			}
			//console.log("deepCloneWithoutExtraKeys change",JSON.stringify(newObj));
			if(lodashIsEmpty(newObj)) {
				return undefined;
			}
		}
		return newObj;
	} else {
		return obj;
	}
}

export function postRESTData(restConfig,statePath,origin,mapping,afterCallback,beforeCallback,outPromise) {
	return (dispatch) => {
		//console.log('POST REST initial request',JSON.stringify(restConfig));
		//deep copy with removing all keys starting with "_" except "_links" and "_embedded"
		let postData = deepCloneWithoutExtraKeys(restConfig.data);
		let origData = restConfig.data;
		for(let key in restConfig.data) {
			//console.log('POST data trim of key: ',key,restConfig.data[key] instanceof Object);
			if(restConfig.data[key] instanceof Error) {
				//console.log('remove Error object',key);
				delete postData[key];
			} else if(restConfig.data[key] instanceof Array) {
				restConfig.data[key].forEach((element,index) => {
					if(element._links && element._links.self) { //only if there is link
						postData[key][index] = element._links.self.href;
					}
				})
			} else {
				//remove sub-objects, because Spring can't handle them
				if(restConfig.data[key] instanceof Object) {
					//console.log('POST Deleting object data key: ',key,postData[key]);
					delete postData[key];
				}
				//exchange sub-objects with their self href link if available
				if(restConfig.data._links) {
					if(restConfig.data._links[key] && restConfig.data[key] != null) { //copy link for linked, ignoring null values
						//console.log('key',key);
						if(restConfig.data[key] && restConfig.data[key]._links && restConfig.data[key]._links.self) { //only if there is link
							postData[key] = restConfig.data[key]._links.self.href;
						} else {
							//console.log('reset not edited object',key);
							postData[key] = restConfig.data._links[key].href;
						}
					}
				}
			}
			//console.log('POST Data key: ',key,postData[key]);
		}
		restConfig.data = postData;
		restConfig.headers = {...restConfig.headers, Authorization: sessionStorage["X-AUTH-TOKEN"]};
		//console.log('POST REST request',JSON.stringify(restConfig));
		const promise = axios(restConfig)
			.then(response => {
				let value = (typeof mapping === 'string' || mapping instanceof String) ?
						resolveObjectPath(mapping,response)
						: mapping ? mapping(response) : response.data;
				//don't change data to avoid unnecessary reload
				//console.log('POST REST data',JSON.stringify(value),JSON.stringify(origData));
				value = Object.assign(origData, value);
				delete value[Constants.PATH_FOR_LOADING];
				delete value[Constants.PATH_FOR_ERROR];
				let mergeObj = ([{
					path: statePath,
					value: value
				}]);
				if(beforeCallback instanceof Function){
					beforeCallback(value);
				}
				dispatch(fetchRESTSuccess(mergeObj,origin));
				if(afterCallback instanceof Function){
					afterCallback(value);
				}
				return response;
			})
			.catch(error => {
				//when calculateOnly, don't report error, but merge the result
				try {
					if(error.response && error.response.data) {
						let parsedError = JSON.parse(error.response.data.message.replace("\\\"","\""));
						let calculateOnlyItem = parsedError.find((item) => (item.error == "calculateOnly.calculateOnly"));
						//console.log("calculateOnly",calculateOnlyItem);

						let transformedValueData = {};
						
						let keys = Object.keys(calculateOnlyItem.value);
						keys.sort();
						keys.forEach((key) => {
							buildObjectPathAndAssign(key, transformedValueData, calculateOnlyItem.value[key]);
						});
						//console.log("calcData transformed",transformedValueData,calculateOnlyItem.value);

						let calcValue = Object.assign({}, origData, transformedValueData[keys[0]]);
						calcValue[Constants.PATH_FOR_ERROR] = error;
						delete calcValue[Constants.PATH_FOR_LOADING];
						delete calcValue.calculateOnly;
						let mergeObj = ([{
							path: statePath,
							value: calcValue
						}]);
						if(beforeCallback instanceof Function){
							beforeCallback(value);
						}
						dispatch(fetchRESTSuccess(mergeObj,origin));
//						restFail(dispatch,FETCH_REST_FAIL,calcValue,statePath,origin);
						if(afterCallback instanceof Function){
							afterCallback(value);
						}
						return error;
					}
				}
				catch(errorJSON) {
					//not JSON error message
				}
				
				restFail(dispatch,REST_POST_FAIL,error,statePath,origin);
				return error;
			});
		if(outPromise) {
			outPromise.promise = promise;
		}
		dispatch(fetchRESTInProgress(promise,statePath,origin));
		return promise
	};
}

export function deleteREST(restConfig,statePath,outPromise = {}) {
	//console.log('deleteREST:',restConfig,statePath);
	return (dispatch) => {
		if(rest_call_limit>0) {
			rest_call_limit--;
			if(rest_call_limit == 0) {
				console.error('Debug REST call limit reached!');
				restFail(dispatch,DELETE_REST_FAIL,new Error('Debug REST call limit reached!'),statePath);
			}
		} else {
			console.error('Debug REST call limit reached!');
			restFail(dispatch,DELETE_REST_FAIL,new Error('Debug REST call limit reached!'),statePath);
			return;
		}
		restConfig.headers = {...restConfig.headers, Authorization: sessionStorage["X-AUTH-TOKEN"]};
		const promise = axios(restConfig)
			.then(response => {
				//console.log('REST response',response);
				dispatch(cleanRESTData(statePath));
				return response;
			})
			.catch(error => {
				restFail(dispatch,DELETE_REST_FAIL,error,statePath);
				return error;
			});
		outPromise.promise = promise;
	};
}

export function fetchRESTFollow(restConfig,statePath,mapping,origin,follow_links = {},outPromise = {}) {

	return (dispatch) => {
		//console.log('fetchRESTFollow:',restConfig,statePath,mapping,follow_links);
		if(rest_call_limit>0) {
			rest_call_limit--;
			if(rest_call_limit == 0) {
				console.log('Debug REST call limit reached!');
				restFail(dispatch,FETCH_REST_FAIL,new Error('Debug REST call limit reached!'),statePath,origin);
			}
		} else {
			return;
		}
		//recursive function for gathering the promise results from each level
		function after([promises,mergeObj]) {
			//console.log('after1',promises,mergeObj);
			if(promises instanceof Array) { //must be true
				if(promises.length>0) {
					//when all promises from this level are done
					return Promise.all(promises)
						.then(mergeObjects => {
							//console.log('all done',mergeObjects);
							let mergeObject = mergeObj;
							let subpromises = [];
							//add the results to the array and all the next level promises
							for(var i = 0; i<mergeObjects.length; i++) {
								if(mergeObjects[i]) { //only for successful calls, failed are undefined
									mergeObject = mergeObject.concat(mergeObjects[i][1]);
									subpromises = subpromises.concat(mergeObjects[i][0]);
								}
							}
							//call for the next level
							return after([subpromises,mergeObject]);
						})
						.catch(error => {
							//console.log('error1',error,mergeObj,promises);
							restFail(dispatch,FETCH_REST_FAIL,error,'',origin);
							throw error;
						});
				} else {
					//no promises for the next level means we're done
					dispatch(fetchRESTSuccess(mergeObj,origin));
					return mergeObj;
				}
			}
			//console.log('after2',mergeObj);
			return mergeObj;
		}

		//start a hierarchy of REST calls and get the promise for the root
		const promise = follow(dispatch,restConfig,statePath,origin,mapping,follow_links)
			.then(after) //when the root promise is done, start gathering the results of the calls by level
			.catch(error => {
				//console.log('error2')
				restFail(dispatch,FETCH_REST_FAIL,error,statePath,origin);
				throw error;
			});
		outPromise.promise = promise;
		dispatch(fetchRESTInProgress(promise,statePath,origin)); //inform about the root promise
	};
}
/*
 * restConfig //axios call parameters
 * statePath //where in the state to put the value (string path)
 * mapping //where to get the value from the response object. Can be string path or function(response)
 * follow_links //settings for following calls with this format:
 * {
 * key, //follow the URL in the _links for this key and put the retrieved 'data' as subkey
 * key: {
 * 			url, params //restConfig is set by these params or by the href in _links for the key
 * 			node //this is used as string path to get the key value from the response, then statePath is calculated for the path till now + node + index(for arrays) + key
 * 			mapping //value for mapping
 * 			children //value for follow_links
 * 		}
 * }
 */
function follow(dispatch,restConfig,statePath,origin,mapping,follow_links) {
	//console.log('follow:',restConfig,statePath,mapping,follow_links);
	//make the call
	restConfig.headers = {...restConfig.headers, Authorization: sessionStorage["X-AUTH-TOKEN"]};
	let promise = axios(restConfig)
		.then(response => {
			//console.log('REST response',response,statePath,mapping,follow_links);
			//get the response to our structure
			let value = (typeof mapping === 'string' || mapping instanceof String) ?
					resolveObjectPath(mapping,response)
					: mapping(response);
			let mergeObj = ([{
				path: statePath,
				value: value
			}]);

			//start the next level of calls
			let promises = [];
			if(follow_links instanceof Object) { //only if there are required sub-calls
				let subpromise = [];
				for(let key in follow_links) { //for each required link
					const node_value = (follow_links[key].node) ? resolveObjectPath(follow_links[key].node,value) : value;
					let handleFollowKey = function(node_value,index) {
						//console.log('handleFollowKey',node_value,key,follow_links[key]);
						if(node_value._links && node_value._links[key] || follow_links[key].url) { //if these are settings for following
							let config = {headers: {Authorization: sessionStorage["X-AUTH-TOKEN"]}};
							let childMapping = "";
							let childLinks = {};
							if(!node_value._links[key] || follow_links[key].url) { //this is not referenced key, try back reference call
								//console.log('handleFollowKey this is not referenced key, try back reference call');
								config.url = ((typeof follow_links[key].url === 'string' || follow_links[key].url instanceof String) ) ?
											follow_links[key].url
											: follow_links[key].url(node_value);
								config.params = follow_links[key].getParams(node_value);
								childMapping = follow_links[key].mapping;
							} else {
								config.url = node_value._links[key].href;
								childMapping = "data";
							}
							childLinks = follow_links[key].children ? follow_links[key].children : follow_links[key];
							const childPath = statePath
								+((follow_links[key].node) ? '.' + follow_links[key].node : '')
								+((index>=0) ? '.'+index : '')
								+'.'+key;
							subpromise = follow(
								dispatch,
								config,
								childPath,
								origin,
								childMapping,
								childLinks,
								m => m
							);
							promises.push(subpromise); //add to the next level promises list
						}
					}
					if(node_value instanceof Array) { //if the current result is array
						for(let i = 0; i < node_value.length; i++) { //call for each array element
							handleFollowKey(node_value[i],i);
						}
					} else { //call the link
						handleFollowKey(node_value,-1);
					}
				}
			}

			//console.log('return',promises,mergeObj);
			//return the next level of promises and the current result
			return [promises,mergeObj];
		})
		.catch(error => {
			//console.log('error3')
			restFail(dispatch,FETCH_REST_FAIL,error,statePath,origin);
			throw error;
		});
	//return the promise of the call
	return promise;
}
