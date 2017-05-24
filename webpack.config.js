var webpack = require("webpack");
var ngAnnotatePlugin = require('ng-annotate-webpack-plugin');
var path = require("path");
var pkg = require("./package.json");
var env = require('yargs').argv.mode;
var nodeModulesDir = path.join(__dirname, 'node_modules');

var sourceDir = path.join(__dirname, pkg.config.sourceDir);
var targetDir = path.join(__dirname, pkg.config.targetDir);

var minifiedDependencies = [

];

var config = {
	entry: {
		dashboardwidgets: path.join(sourceDir, "dashboardwidgets/dashboardwidgets")
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
			minChunks: module => /node_modules/.test(module.resource)
		}),
		new ngAnnotatePlugin()
	],
	resolve: {
		root: sourceDir,
		extensions: ['', '.js'],
		alias: {}
	},
	module: {
		loaders: [
			{
				test: /\.js$/,
				exclude: /node_modules/,
				loader: 'babel?presets[]=es2015'
			}, 
			{
				test: /\.css$/,
				loader: 'css'
			}, 
			{
				test: /\.(png|jpg|jpeg|gif|svg)$/,
				loader: 'url'
			},
			{
				test: /\.json$/,
				loader: 'json'
			},
			{
				test: /\.html$/,
				loader: 'raw'
			}, 
			{
				test: /\.scss$/,
				loader: "style!css!sass?outputStyle=expanded&includePaths[]=" 
					+ path.resolve(__dirname, "./node_modules/compass-mixins/lib")
			},
			{test: /\.woff(\?v=\d+\.\d+\.\d+)?$/, loader: 'url'},
			{test: /\.woff2(\?v=\d+\.\d+\.\d+)?$/, loader: 'url'},
			{test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/, loader: 'url'},
			{test: /\.svg(\?v=\d+\.\d+\.\d+)?$/, loader: 'url'},
			{test: /\.eot(\?v=\d+\.\d+\.\d+)?$/, loader: 'url'}
		],
		noParse: []
	}
};

if (env === 'dev') {
	// Run through minifiedDependencies and extract the first part of the path,
	// as that is what you use to require the actual node modules
	// in your code. Then use the complete path to point to the correct
	// file and make sure webpack does not try to parse it
	minifiedDependencies.forEach(function (dep) {
		var depPath = path.resolve(nodeModulesDir, dep);
		var depIndex = 0;
		if (dep.startsWith('@')) {
			depIndex = 1;
		}
		var depAlias = dep.split('/')[depIndex];
		config.resolve.alias[depAlias] = depPath;
		config.module.noParse.push(depPath);
	});
} else if (env === 'prod') {
	config.devtool = 'source-map';
	
	config.plugins.push(new webpack.optimize.DedupePlugin());
	config.plugins.push(new webpack.optimize.UglifyJsPlugin({
		compress: {
			warnings: false
		}
	}));
}

module.exports = config;