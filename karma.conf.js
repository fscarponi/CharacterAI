module.exports = function(config) {
    config.set({
        frameworks: ['mocha', 'webpack'],
        reporters: ['progress', 'mocha'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        browsers: ['ChromeHeadless'],
        autoWatch: false,
        singleRun: true,
        concurrency: Infinity,
        files: [
            { pattern: 'src/wasmJsTest/resources/test.html', watched: false },
            { pattern: 'build/compileSync/wasmJs/test/testDevelopmentExecutable/kotlin/*.js', watched: true }
        ],
        preprocessors: {
            'src/wasmJsTest/resources/test.html': ['html2js'],
            'build/compileSync/wasmJs/test/testDevelopmentExecutable/kotlin/*.js': ['webpack', 'sourcemap']
        },
        webpack: {
            mode: 'development',
            devtool: 'inline-source-map',
            module: {
                rules: [
                    {
                        test: /\.js$/,
                        use: ['source-map-loader'],
                        enforce: 'pre'
                    }
                ]
            }
        },
        client: {
            mocha: {
                timeout: 10000
            }
        },
        mochaReporter: {
            showDiff: true
        }
    });
};
