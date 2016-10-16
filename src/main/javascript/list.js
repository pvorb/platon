var Vue = require('vue');

var CommentService = require('./services/comment-service.js');

var template = require('./list.html');

new Vue({
    el: '#platon-comment-thread',
    render: template.render,
    staticRenderFns: template.staticRenderFns,

    data: {
        loading: true,
        comments: []
    },

    components: {
        'platon-comment': require('./components/comment'),
        'platon-comment-form': require('./components/comment-form')
    },

    methods: {
        addComment: function(newComment) {
            this.comments.push(newComment);
        }
    },

    created: function () {
        var vm = this;
        CommentService.getComments(window.location.pathname)
            .then(function updateModel(comments) {
                vm.comments = comments;
                vm.loading = false;
            })
            .catch(function displayError(reason) {
                alert(reason);
            });
    }
});
