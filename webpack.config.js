var path = require('path');

var jsDir = 'src/main/javascript';

module.exports = {
    entry: {
        platon: path.resolve(jsDir, 'platon.js')
    },
    output: {
        path: path.resolve('src/main/webapp/js'),
        filename: '[name].js'
    },
    module: {
        loaders: [
            {test: /\.html$/, loader: 'vue-template-compiler'},
            {test: /\.css$/, loader: 'style!css'}
        ]
    }
};
