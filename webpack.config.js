var path = require('path');

var jsDir = 'src/main/javascript';

module.exports = {
    entry: {
        show: path.resolve(jsDir, 'show.js'),
        count: path.resolve(jsDir, 'count.js')
    },
    output: {
        path: path.resolve('src/main/webapp/js'),
        filename: '[name].js'
    },
    module: {
        loaders: [
            {test: /\.html$/, loader: 'vue-template-compiler'}
        ]
    }
};
