var webpack = require("webpack");
var ngAnnotatePlugin = require('ng-annotate-webpack-plugin');
var path = require("path");
var pkg = require("./package.json");
var env = require('yargs').argv.env;
var nodeModulesDir = path.join(__dirname, 'node_modules');

var sourceDir = path.join(__dirname, pkg.config.sourceDir);
var targetDir = path.join(__dirname, pkg.config.targetDir);

var config = {
	entry: {
		dashboardwidgets: path.join(sourceDir, "dashboardwidgets")
	},
	output: {
		path: targetDir,
		filename: "coreapps.[name].js",
		library: ["coreapps", "[name]"],
		libraryTarget: "umd"
	},
	plugins: [
		new webpack.optimize.CommonsChunkPlugin({
			name: "vendor",
			minChunks: function (module) { return /node_modules/.test(module.resource) }
		}),
		new ngAnnotatePlugin()
	],
	module: {
		rules: [
			{
				test: /\.js$/,
				exclude: /node_modules/,
				use: {
					loader: 'babel-loader?cacheDirectory',
					options: {
						presets: ['es2015']
					}
				}
			}, 
			{
				test: /\.(png|jpg|jpeg|gif|svg)$/,
				use: {
					loader: 'url-loader'
				}
			},
			{
				test: /\.json$/,
				use: {
					loader: 'json-loader'
				}
			},
			{
				test: /\.html$/,
				use: {
					loader: 'raw-loader'
				}
			},
            {
                test: /\.css$/,
                use: {
                	loader: 'css-loader'
                }
            }
		]
	}
};

if (env === 'dev') {

} else if (env === 'prod') {
	config.devtool = 'source-map';
	
	config.plugins.push(new webpack.optimize.UglifyJsPlugin({
		sourceMap: true,
		compress: {
			warnings: false
		}
	}));
}

module.exports = config;