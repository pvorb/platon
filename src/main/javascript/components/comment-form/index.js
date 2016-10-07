var template = require('./comment-form.html');

module.exports = {
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
            rememberUser: true
        };
    },
    methods: {
        postComment: function () {
            console.log(JSON.stringify(this.comment), this.rememberUser);
        }
    }
};
