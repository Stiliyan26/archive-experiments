'use strict';

/**
 * DistBgMashini configuration. Used to build the
 * final output when running npm run distBgMashini.
 */
const webpack = require('webpack');
const WebpackBaseConfig = require('./Base');

class WebpackDistBgMashiniConfig extends WebpackBaseConfig {

	constructor() {
		super();

		// console.warn('============ DistBgMashini.js')
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
					API_URL: '"https://195.201.4.80/api"',
				}),
				new webpack.optimize.AggressiveMergingPlugin(),
			]
		};

		// Deactivate hot-reloading if we run DistBgMashini build on the dev server
		this.config.devServer.hot = false;
	}

	/**
	 * Get the environment name
	 * @return {String} The current environment
	 */
	get env() {
		return 'distBgMashini';
	}
}

module.exports = WebpackDistBgMashiniConfig;
