var webpack = require("webpack");
var path = require("path");
var pkg = require("./package.json");

var sourceDir = path.join(__dirname, pkg.config.sourceDir);
var targetDir = path.join(__dirname, pkg.config.targetDir);

module.exports = function(env) {
env = env || {};

var config = {
	mode: env.prod ? 'production' : 'development',
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
		new webpack.ProvidePlugin({
			process: 'process/browser',
			$: "jquery",
			jQuery: "jquery",
			"window.jQuery": "jquery"
		})
	],
	optimization: {
		splitChunks: {
			cacheGroups: {
				vendor: {
					test: /[\\/]node_modules[\\/]/,
					name: "vendor",
					chunks: "all"
				}
			}
		}
	},
	module: {
		rules: [
			{
				test: /\.js$/,
				include: sourceDir,
				use: {
					loader: 'babel-loader?cacheDirectory',
					options: {
						presets: ['@babel/preset-env'],
						plugins: ['angularjs-annotate']
					}
				}
			},
			{
				test: /\.css$/,
				use: ['style-loader', 'css-loader']
			},
			{
				test: /\.html$/,
				use: {
					loader: 'raw-loader'
				}
			},
			{
				test: /\.(png|jpg|jpeg|gif|svg)$/,
				use: [
					{
						loader: 'url-loader',
						options: {
							limit: 10000
						}
					},
					{
						loader: 'image-webpack-loader',
						options: {
							mozjpeg: {
							  progressive: true,
							},
							gifsicle: {
								interlaced: false,
							},
							optipng: {
								optimizationLevel: 4,
							},
							pngquant: {
								quality: '75-90',
								speed: 3,
							}
						}
					}
				]
			},
			{
				test: /\.woff(\?v=\d+\.\d+\.\d+)?$/,
				use: {
					loader: 'url-loader',
					options: {
						limit: 10000
					}
				}
			},
			{
				test: /\.woff2(\?v=\d+\.\d+\.\d+)?$/,
				use: {
					loader: 'url-loader',
					options: {
						limit: 10000
					}
				}
			},
			{
				test: /\.ttf(\?v=\d+\.\d+\.\d+)?$/,
				use: {
					loader: 'url-loader',
					options: {
						limit: 10000
					}
				}
			},
			{
				test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
				use: {
					loader: 'url-loader',
					options: {
						limit: 10000
					}
				}
			}
		]
	},
	resolve: {
		alias: {
			'chart.js': path.resolve(__dirname, 'node_modules/chart.js/dist/Chart.js'),
			'chart': path.resolve(__dirname, 'node_modules/chart.js/dist/Chart.js')
		},
		fallback: {
			'assert': require.resolve('assert/')
		},
		modules: [path.resolve(__dirname, "node_modules")]
	}
};

if (env.dev) {
	config.plugins.push(new webpack.SourceMapDevToolPlugin({
      exclude: ["coreapps.vendor.js"]
    }));
} else if (env.prod) {
	config.devtool = 'source-map';
}

return config;
};
