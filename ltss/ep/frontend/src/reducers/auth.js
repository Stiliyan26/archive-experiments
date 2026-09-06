/* Define your initial state here.
 *
 * If you change the type from object to something else, do not forget to update
 * src/container/App.js accordingly.
 */
import * as ActionTypes from '../actions/types';

const initialState = {
	userAuthenticated: false,
	isLoggingIn: false,
	message: ""
};

function authReducer(state = initialState, action) {
	/* Keep the reducer clean - do not mutate the original state. */
	// const nextState = Object.assign({}, state);

	switch (action.type) {

		case ActionTypes.LOGIN_REQUEST: {
			return {...state,
				isLoggingIn: action.isLoggingIn,
				message: action.message,
				username: action.username,
			}
		}
		case ActionTypes.LOGIN_SUCCESS: {
			return {...state,
				// userAuthenticated: action.isAuthenticated,
				userAuthenticated: true,
				isLoggingIn: action.isLoggingIn,
				message: "",
				username: action.username,
			};
		}
		case ActionTypes.LOGIN_FAILURE: {
			return {...state,
				// userAuthenticated: action.isAuthenticated,
				userAuthenticated: false,
				isLoggingIn: action.isLoggingIn,
				message: action.message,
				username: action.username,
			};
		}
		// case ActionTypes.LOGOUT: {
		// 	// return {
		// 		// ...state,
		// 		// userAuthenticated: action.isAuthenticated,
		// 		// username: action.username,
		// 	// };
		// }

		default: {
			/* Return original state if no actions were consumed. */
			return state;
		}
	}
}

module.exports = authReducer;