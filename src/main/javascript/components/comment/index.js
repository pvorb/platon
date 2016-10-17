/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
