var template = require('./comment.html');

module.exports = {
    props: {
        comment: {
            type: Object,
            required: true
        }
    },
    render: template.render,
    staticRenderFns: template.staticRenderFns
};
