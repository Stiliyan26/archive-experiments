import webpack from 'webpack'
import path from 'path'
const fs = require('fs');
const npmBase = path.join(__dirname, './node_modules');
const PropertiesReader = require('properties-reader');
const appProperties = PropertiesReader('./build.properties')._properties;

const srcPathAbsolute = path.resolve('./src');
const includedPackages = [].map(pkg => fs.realpathSync(path.join(npmBase, pkg)));
const cssModulesQuery = {
			modules: true,
			importLoaders: 1,
			localIdentName: '[name]-[local]-[hash:base64:5]'
		};
const TARGET = 'dev'

export default {
	mode: 'development',
	bail: true,
	devtool: 'cheap-module-source-map',

	entry: [
		'react-hot-loader/patch',
        'babel-polyfill',
        'webpack-hot-middleware/client',
        './src/client.js'
      ],

	target: 'web',

	output: {
				path: path.resolve('__dirname', 'src'),
				publicPath: '/',
				filename: 'app.js',
	},
	module: {
				rules: [
				{
						enforce: 'pre',
						test: /\.js?$/,
						include: srcPathAbsolute,
						loader: 'babel-loader',
						query: { presets: ['es2015'] }
					},
					{
						test: /^.((?!cssmodule).)*\.css$/,
						loaders: [	{ loader: 'style-loader' },
								{ loader: 'css-loader' },
								{ loader: 'postcss-loader' }
							]
					},
					{
						test: /\.(png|jpg|gif|mp4|ogg|svg|woff|woff2|eot|ttf|gif|properties)$/,
						loader: 'file-loader'
					},
					{
						test: /^.((?!cssmodule).)*\.(sass|scss)$/,
						loaders: [	{ loader: 'style-loader' },
								{ loader: 'css-loader' },
								{ loader: 'postcss-loader' },
								{ loader: 'sass-loader' }
						]
					},
				 /* { test: /.(png|woff(2)?|eot|ttf|svg)(?[a-z0-9=.]+)?$/, loader: 'url-loader?limit=100000' } ,  */

					{
						test: /^.((?!cssmodule).)*\.less$/,
						loaders: [	{ loader: 'style-loader' },
								{ loader: 'css-loader' },
								{ loader: 'postcss-loader' },
								{ loader: 'less-loader' }
						]
					},
					{
						test: /^.((?!cssmodule).)*\.styl$/,
						loaders: [	{ loader: 'style-loader' },
								{ loader: 'css-loader' },
								{ loader: 'postcss-loader' },
								{ loader: 'stylus-loader' } ]
					},
					{
						test: /\.(js|jsx)$/,
						include: [].concat(includedPackages, [srcPathAbsolute]),
						loaders: [{ loader: 'babel-loader' }]
					},
					{
						test: /\.cssmodule\.(sass|scss)$/,
						loaders: [	{ loader: 'style-loader' },
								{loader: 'css-loader', query: cssModulesQuery },
								{ loader: 'postcss-loader' },
								{ loader: 'sass-loader' }
						]
					},
					{
						test: /\.cssmodule\.css$/,
						loaders: [ 	{ loader: 'style-loader' },
								{ loader: 'css-loader', query: cssModulesQuery },
								{ loader: 'postcss-loader' }
						]
					},
					{ test: /\.cssmodule\.less$/,
						loaders: [	{ loader: 'style-loader' },
								{ loader: 'css-loader', query: cssModulesQuery },
								{ loader: 'postcss-loader' },
								{ loader: 'less-loader' }
						]
					},
					{test: /\.cssmodule\.styl$/,
						loaders: [	{ loader: 'style-loader' },
								{ loader: 'css-loader', query: cssModulesQuery },
								{ loader: 'postcss-loader' },
								{ loader: 'stylus-loader' } ]
					}
				]
	},
	externals: {
				'appProperties': JSON.stringify(appProperties)
	},
	plugins: [
		new webpack.HotModuleReplacementPlugin(),
		new webpack.DefinePlugin({
			MANAGED_COMPANY_EIK: '"202142790"',
			API_URL: '"https://127.0.0.1:8443/api"',
		}),
	]
}


