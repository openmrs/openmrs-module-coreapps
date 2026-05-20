process.env.CHROME_BIN = require('puppeteer').executablePath();

module.exports = function(config) {
	var pkg = require("./package.json");
	var webpackConfig = require('./webpack.config')({});
	webpackConfig.devtool = 'inline-source-map';

	// Disable splitChunks as it breaks tests.
	webpackConfig.optimization = webpackConfig.optimization || {};
	webpackConfig.optimization.splitChunks = false;
	webpackConfig.optimization.runtimeChunk = false;

    var karmaConfig = {
		browsers: ['ChromeHeadlessNoSandbox'],
		customLaunchers: {
			ChromeHeadlessNoSandbox: {
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
			{ pattern: 'node_modules/@babel/polyfill/dist/polyfill.js', instrument: false},
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