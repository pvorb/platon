var Vue = require('vue');

var template = require('./list.html');

new Vue({
    el: '#platon-comment-thread',
    render: template.render,
    staticRenderFns: template.staticRenderFns,

    data: {
        loading: true,
        comments: [],
        commentDraft: {}
    },

    components: {
        'platon-comment': require('./components/comment'),
        'platon-comment-form': require('./components/comment-form')
    },

    created: function () {
        this.$http.get('/api/comments', {
            params: {
                threadUrl: window.location.href
            }
        }).then(function handleSuccess(response) {
            return response.json();
        }, function handleError(response) {
            if (response.status === 404) {
                return Promise.resolve([]);
            } else {
                return Promise.reject('cannot_load');
            }
        }).then(function updateModel(comments) {
            this.comments = comments;
            this.loading = false;
        }).catch(function displayError(reason) {
            alert(reason);
        });
    }
});
