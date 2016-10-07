var Vue = require('vue');

var template = require('./show.html');

var vm = new Vue({
    el: '#platon-comments',
    render: template.render,
    staticRenderFns: template.staticRenderFns,
    data: {
        comments: [{
            id: 21389043234,
            text: 'some comment'
        }, {
            id: 34789234789,
            text: 'another comment'
        }],
        newComment: {
            text: 'waaaat?',
            author: 'John Doe',
            email: 'john@example.com',
            url: 'http://example.com/joe'
        }
    },
    components: {
        'platon-comment': require('./components/comment'),
        'platon-comment-form': require('./components/comment-form')
    }
});

module.exports = vm;
