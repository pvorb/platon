var debounce = require('lodash.debounce');
var marked = require('marked');
marked.setOptions({
    gfm: true,
    tables: false,
    breaks: false,
    sanitize: true,
    smartLists: true,
    smartypants: true
});

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
            rememberUser: UserInfoService.getRememberUser(),
            showPreview: false,
            previewStyle: {},
            markdown: ''
        };
    },

    methods: {
        togglePreview: function () {
            this.showPreview = !this.showPreview;
            if (this.showPreview) {
                this.comment.text = marked(this.markdown);
            }
            try {
                this.previewStyle.height = this.$el.getElementsByClassName('platon-form-text')[0].offsetHeight;
            } catch (e) {
                console.error(e.message);
            }
        },
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
            vm.comment.text = marked(vm.markdown);

            CommentService.postComment(window.location.pathname, document.title, vm.comment)
                .then(function (newComment) {
                    vm.$emit('posted', newComment);
                })
                .catch(function () {
                    console.log('error', arguments);
                });
        }
    }
};
