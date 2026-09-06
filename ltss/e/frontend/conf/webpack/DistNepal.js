'use strict';

/**
 * DistNepal configuration. Used to build the
 * final output when running npm run distNepal.
 */
const webpack = require('webpack');
const WebpackBaseConfig = require('./Base');

class WebpackDistNepalConfig extends WebpackBaseConfig {

	constructor() {
		super();

		// console.warn('============ DistNepal.js')
		this.config = {
			mode: 'production',
			bail: true,
			cache: false,
			//devtool: 'source-map',
			entry: [
				'./client.js'
			],
			plugins: [
				new webpack.DefinePlugin({
					'process.env.NODE_ENV': '"production"',
					'process.env.BABEL_ENV': '"production"',
					MANAGED_COMPANY_EIK: '"202142790"',
					API_URL: '"https://51.68.172.237/api"',
				}),
				new webpack.optimize.AggressiveMergingPlugin(),
			]
		};

		// Deactivate hot-reloading if we run DistNepal build on the dev server
		this.config.devServer.hot = false;
	}

	/**
	 * Get the environment name
	 * @return {String} The current environment
	 */
	get env() {
		return 'distNepal';
	}
}

module.exports = WebpackDistNepalConfig;
