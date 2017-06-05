module.exports = function(config) {
	var pkg = require("./package.json");
	var webpackConfig = require('./webpack.config');
	webpackConfig.devtool = 'inline-source-map';
	
	//Disable CommonsChunkPlugin as it breaks tests.
	var commonsChunkPluginIndex = webpackConfig.plugins.findIndex(function(plugin) { return plugin.chunkNames });
	webpackConfig.plugins.splice(commonsChunkPluginIndex, 1);
	
    var karmaConfig = {
        browsers: ['PhantomJS'],
		customLaunchers: {
			ChromeWithoutSecurity: {
				base: 'Chrome',
				flags: ['--disable-web-security']
			},
			FirefoxNoSandbox: {
				base: 'Firefox',
				flags: ['--no-sandbox']
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
	
	if (process.env.TRAVIS) {
		karmaConfig.browsers = ['FirefoxNoSandbox'];
	}
	
	config.set(karmaConfig);
};