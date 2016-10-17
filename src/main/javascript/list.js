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

var Vue = require('vue');

var CommentService = require('./services/comment-service.js');

var template = require('./list.html');

new Vue({
    el: '#platon-comment-thread',
    render: template.render,
    staticRenderFns: template.staticRenderFns,

    data: {
        loading: true,
        comments: []
    },

    components: {
        'platon-comment': require('./components/comment'),
        'platon-comment-form': require('./components/comment-form')
    },

    methods: {
        addComment: function(newComment) {
            this.comments.push(newComment);
        }
    },

    created: function () {
        var vm = this;
        CommentService.getComments(window.location.pathname)
            .then(function updateModel(commentsListResult) {
                vm.comments = commentsListResult.comments;
                vm.totalCommentCount = commentsListResult.totalCommentCount;
                vm.loading = false;
            })
            .catch(function displayError(reason) {
                alert(reason);
            });
    }
});
