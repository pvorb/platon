var userInfoStore = require('./user-info-store.js');

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

            console.log(this.replyTo);
            // TODO post request
        }
    }
};
