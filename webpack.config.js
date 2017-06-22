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
				include: sourceDir,
				use: {
					loader: 'babel-loader?cacheDirectory',
					options: {
						presets: ['es2015']
					}
				}
			}, 
			{
				test: /\.css$/,
				include: [ 
					sourceDir,
					path.join(nodeModulesDir, "angular-ui-bootstrap") 
				],
				use: ['style-loader', 'css-loader']
			},
			{
				test: /\.(png|jpg|jpeg|gif|svg)$/,
				include: sourceDir,
				use: {
					loader: 'url-loader'
				}
			},
			{
				test: /\.json$/,
				include: sourceDir,
				use: {
					loader: 'json-loader'
				}
			},
			{
				test: /\.html$/,
				include: sourceDir,
				use: {
					loader: 'raw-loader'
				}
			}
		]
	},
	resolve: {
		modules: [path.resolve(__dirname, "node_modules")]
	}
};

if (env === 'dev') {
	config.plugins.push(new webpack.SourceMapDevToolPlugin({
      exclude: ["coreapps.vendor.js"]
    }));
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