'use strict';

/**
 * TestSelfie configuration. Used to build the
 * final output when running npm run TestSelfie-build.
 */
const webpack = require('webpack');
const WebpackBaseConfig = require('./Base');

class WebpackTestSelfieConfig extends WebpackBaseConfig {

	constructor() {
		super();

		console.warn('============ TestSelfie.js')
		this.config = {
			mode: 'production',
			bail: true,
			cache: false,
			devtool: 'source-map',
			entry: [
				'./client.js'
			],
			plugins: [
				new webpack.DefinePlugin({
					'process.env.NODE_ENV': '"production"',
					'process.env.BABEL_ENV': '"production"',
					MANAGED_COMPANY_EIK: '"9999999999"',
					API_URL: '"https://10.144.1.36/api"',
				}),
				new webpack.optimize.AggressiveMergingPlugin(),
			]
		};

		// Deactivate hot-reloading if we run TestSelfie build on the dev server
		this.config.devServer.hot = false;
	}

	/**
	 * Get the environment name
	 * @return {String} The current environment
	 */
	get env() {
		return 'testSelfie';
	}
}

module.exports = WebpackTestSelfieConfig;
