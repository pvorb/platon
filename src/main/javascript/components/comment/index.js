var template = require('./comment.html');

module.exports = {
    name: 'platon-comment',
    props: {
        comment: {
            type: Object,
            required: true
        }
    },
    render: template.render,
    staticRenderFns: template.staticRenderFns,
    data: function () {
        return {
            showReplyForm: false,
            commentDraft: {}
        }
    },
    computed: {
        creationDate: function () {
            return new Date(this.comment.creationDate).toLocaleString();
        },
        longCreationDate: function () {
            return new Date(this.comment.creationDate).toISOString();
        }
    },
    components: {
        'platon-comment-form': require('../comment-form')
    },
    methods: {
        addReply: function (newComment) {
            this.comment.replies.push(newComment);
            this.showReplyForm = false;
        }
    }
};
