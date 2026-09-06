'use strict';

/* eslint no-console: "off" */
const webpackConfigs = require('./conf/webpack');
const defaultConfig = 'dev';


module.exports = (configObj) => {

	//console.log("Webpack config:",configName);
	let configName;
	if(configObj && configObj.dist) configName = "dist";
	if(configObj && configObj.distBgMashini) configName = "distBgMashini";
	if(configObj && configObj.distNepal) configName = "distNepal";
	if(configObj && configObj.staging) configName = "staging";
	if(configObj && configObj.demo) configName = "demo";
	if(configObj && configObj.testBgMashini) configName = "testBgMashini";
	if(configObj && configObj.testSelfie) configName = "testSelfie";
	if(configObj && configObj.testAnvoice) configName = "testAnvoice";
	if(configObj && configObj.test) configName = "test";
	if(configObj && configObj.dev) configName = "dev";
	// If there was no configuration give, assume default
	const requestedConfig = configName || defaultConfig;

	// Return a new instance of the webpack config
	// or the default one if it cannot be found.
	let LoadedConfig = defaultConfig;

	// console.warn(`============ webpack.config.js ( ${requestedConfig} )`)
	if (webpackConfigs[requestedConfig] !== undefined) {
		LoadedConfig = webpackConfigs[requestedConfig];
	} else {
		console.warn(`
			Provided environment "${configName}" was not found.
			Please use --env option with one of the following ones:
			${Object.keys(webpackConfigs).join(' ')}
		`);
		LoadedConfig = webpackConfigs[defaultConfig];
	}


	const loadedInstance = new LoadedConfig();

	// console.warn(`========== webpack.config.js loadedInstance`)
	// Set the global environment
	process.env.NODE_ENV = loadedInstance.env;

	return loadedInstance.config;
};
