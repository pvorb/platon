var UserInfoService = require('./../../services/user-info-service.js');
var CommentService = require('../../services/comment-service.js');

var template = require('./comment-form.html');

module.exports = {
    props: {
        parentId: {
            type: Number,
            required: false
        }
    },

    render: template.render,
    staticRenderFns: template.staticRenderFns,

    data: function () {
        return {
            comment: UserInfoService.getUserInfo(),
            rememberUser: UserInfoService.getRememberUser()
        };
    },

    methods: {
        postComment: function () {
            var vm = this;

            if (vm.rememberUser) {
                UserInfoService.storeUserInfo({
                    author: vm.comment.author,
                    email: vm.comment.email,
                    url: vm.comment.url
                });
            } else {
                UserInfoService.removeUserInfo();
            }

            vm.comment.parentId = vm.parentId;

            CommentService.postComment(window.location.href, document.title, vm.comment)
                .then(function (newComment) {
                    vm.$emit('posted', newComment);
                })
                .catch(function () {
                    console.log('error', arguments);
                });
        }
    }
};
