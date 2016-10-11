var userInfoStore = require('./user-info-store.js');

var template = require('./comment-form.html');

module.exports = {
    render: template.render,
    staticRenderFns: template.staticRenderFns,
    data: function () {
        return {
            comment: userInfoStore.getUserInfo(),
            rememberUser: userInfoStore.getRememberUser()
        };
    },
    methods: {
        postComment: function () {
            if (this.rememberUser) {
                userInfoStore.storeUserInfo({
                    author: this.comment.author,
                    email: this.comment.email,
                    url: this.comment.url
                });
            } else {
                userInfoStore.removeUserInfo();
            }

            // TODO post request
        }
    }
};
