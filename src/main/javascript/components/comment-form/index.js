var UserInfoStore = require('./user-info-store.js');
var CommentService = require('../../services/comment-service.js');

var template = require('./comment-form.html');

module.exports = {
    props: {
        replyTo: {
            type: Number,
            required: false
        }
    },
    render: template.render,
    staticRenderFns: template.staticRenderFns,
    data: function () {
        return {
            comment: UserInfoStore.getUserInfo(),
            rememberUser: UserInfoStore.getRememberUser()
        };
    },
    methods: {
        postComment: function () {
            if (this.rememberUser) {
                UserInfoStore.storeUserInfo({
                    author: this.comment.author,
                    email: this.comment.email,
                    url: this.comment.url
                });
            } else {
                UserInfoStore.removeUserInfo();
            }

            CommentService.postComment(window.location.href, document.title, this.comment)
                .then(function () {
                    console.log('success', arguments);
                })
                .catch(function () {
                    console.log('error', arguments);
                });
        }
    }
};
