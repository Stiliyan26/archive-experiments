'use strict';

/**
 * Staging configuration. Used to build the
 * final output when running npm run staging.
 */
const webpack = require('webpack');
const WebpackBaseConfig = require('./Base');

class WebpackStagingConfig extends WebpackBaseConfig {

	constructor() {
		super();

		console.warn('============ Staging.js')
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
					MANAGED_COMPANY_EIK: '"204074306"',
					API_URL: '"https://37.157.143.162:41443/api"',
				}),
				new webpack.optimize.AggressiveMergingPlugin(),
			]
		};

		// Deactivate hot-reloading if we run Staging build on the dev server
		this.config.devServer.hot = false;
	}

	/**
	 * Get the environment name
	 * @return {String} The current environment
	 */
	get env() {
		return 'dist';
	}
}

module.exports = WebpackStagingConfig;
