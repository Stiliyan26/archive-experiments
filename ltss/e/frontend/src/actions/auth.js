import * as ActionTypes from './types';
import history from '../scripts/history';
import axios from 'axios'
import CryptoJS from 'crypto-js';
import Base64 from 'crypto-js/enc-base64';
import i18n from 'i18next';

export function logout(value){
	// setTimeout(() => {
		history.push('/login')
		sessionStorage.removeItem("X-AUTH-TOKEN")
	// },0)
	return {
		type: ActionTypes.LOGOUT,
		isAuthenticated: false
	};
}

function loginRequest(credentials){
	return {
		type: ActionTypes.LOGIN_REQUEST,
		isLoggingIn: true,
		isAuthenticated: false,
		message: ""
	}
}
function loginSuccess(response, credentials){
	sessionStorage.removeItem('csrf_token')
	if(response.cstf_token){
		sessionStorage.setItem('csrf_token', response.csrf_token)
	}
	sessionStorage.removeItem('X-AUTH-TOKEN')
	if(response.Authorization){
		sessionStorage.setItem('X-AUTH-TOKEN', response.Authorization)
	}
	return {
		type: ActionTypes.LOGIN_SUCCESS,
		isLoggingIn: false,
		isAuthenticated: true,
		username: credentials.username,
	}
}
function loginFail(errorMessage){
	let errorMsg = errorMessage
	switch(errorMessage){
		case "Failed to fetch":{
			errorMsg = i18n.t("Error.Failed to fetch");
		}
	}
	return {
		type: ActionTypes.LOGIN_FAILURE,
		isLoggingIn: false,
		isAuthenticated: false,
		message: errorMsg
	}
}

export function login(credentials){
	return dispatch => {
		// Init the login process
		dispatch(loginRequest(credentials))
		var myHeaders = new Headers();
		myHeaders.append("Content-Type", "application/x-www-form-urlencoded");
		myHeaders.append("DNT", "0")
		let formData = new FormData();
		formData.append('username', credentials.username);
		formData.append('password', credentials.password);
		axios(API_URL+"/login", {
			method: 'post',
			data: formData,
			credentials: 'include'
		})
		.then((response) =>{
			if(!response.data && !response.data.Authorization){
				throw response;
			}
			dispatch(loginSuccess(response.data, credentials));
		})
		.catch((err) => {
			console.warn('Catch login error: ', err);
			dispatch(loginFail(err.response && err.response.status == 401 ? i18n.t("Error.401") + err.message: err.message));
		});
	}
}