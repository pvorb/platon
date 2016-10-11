var template = require('./comment.html');

var dateToString = require('./date-to-string.js');

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
            return dateToString(new Date(this.comment.creationDate));
        }
    },
    components: {
        'platon-comment-form': require('../comment-form')
    }
};
