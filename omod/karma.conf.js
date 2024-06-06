module.exports = function(config) {
	var pkg = require("./package.json");
	var webpackConfig = require('./webpack.config');
	webpackConfig.devtool = 'inline-source-map';
	
	//Disable CommonsChunkPlugin as it breaks tests.
	var commonsChunkPluginIndex = webpackConfig.plugins.findIndex(function(plugin) { return plugin.chunkNames });
	webpackConfig.plugins.splice(commonsChunkPluginIndex, 1);
	
    var karmaConfig = {
		browsers: ['ChromeHeadless'],
		customLaunchers: {
			ChromeHeadlessDocker: {
				base: 'ChromeHeadless',
				flags: [
					"--disable-gpu",
					"--disable-dev-shm-usage",
					"--disable-setuid-sandbox",
					"--no-sandbox",
				]
			}
		},
        files: [
			{ pattern: 'node_modules/babel-polyfill/browser.js', instrument: false},
            { pattern: pkg.config.sourceDir + '/karma.context.js' }
        ],
		frameworks: ['jasmine'],
        preprocessors: {
            '**/karma.context.js': ['webpack', 'sourcemap']
        },
        webpack: webpackConfig,
		webpackMiddleware: {
			stats: "errors-only"
		},
		reporters: ['progress'],
		port: 9876,
		colors: true,
		logLevel: config.LOG_INFO,
		autoWatch: true,
		concurrency: Infinity,
		singleRun: true
    };
	
	config.set(karmaConfig);
};