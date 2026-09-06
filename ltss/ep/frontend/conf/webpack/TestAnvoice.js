'use strict';

/**
 * TestAnvoice configuration. Used to build the
 * final output when running npm run TestAnvoice-build.
 */
const webpack = require('webpack');
const WebpackBaseConfig = require('./Base');

class WebpackTestAnvoiceConfig extends WebpackBaseConfig {

	constructor() {
		super();

		console.warn('============ TestAnvoice.js')
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
					API_URL: '"https://10.236.19.27/api"',
				}),
				new webpack.optimize.AggressiveMergingPlugin(),
			]
		};

		// Deactivate hot-reloading if we run TestAnvoice build on the dev server
		this.config.devServer.hot = false;
	}

	/**
	 * Get the environment name
	 * @return {String} The current environment
	 */
	get env() {
		return 'testAnvoice';
	}
}

module.exports = WebpackTestAnvoiceConfig;
