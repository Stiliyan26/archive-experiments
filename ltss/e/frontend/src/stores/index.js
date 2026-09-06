import { createStore, applyMiddleware } from 'redux';
import reducers from '../reducers';
import thunkMiddleware from 'redux-thunk'
import { createLogger } from 'redux-logger'
import { persistStore } from 'redux-persist'

function reduxStore(initialState) {

	const loggerMiddleware = createLogger();

	const store = createStore(reducers,
		initialState,
		applyMiddleware(
			thunkMiddleware, // lets us dispatch() functions
			loggerMiddleware // neat middleware that logs actions
		),
		window.devToolsExtension && window.devToolsExtension());

	if (module.hot) {
		// Enable Webpack hot module replacement for reducers
		module.hot.accept('../reducers', () => {
			// We need to require for hot reloading to work properly.
			const nextReducer = require('../reducers');  // eslint-disable-line global-require

			store.replaceReducer(nextReducer);
		});
	}

	let persistor = persistStore(store);

	return {persistor, store};
}

export default reduxStore;
