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

module.exports = {
    getComments: function getComments(threadUrl) {
        return Vue.http.get('/api/comments', {
            params: {
                threadUrl: threadUrl
            }
        }).then(function handleSuccess(response) {
            return response.json();
        }, function handleError(response) {
            if (response.status === 404) {
                return Promise.resolve([]);
            } else {
                return Promise.reject('cannot_load');
            }
        });
    },
    postComment: function postComment(threadUrl, threadTitle, comment) {
        return Vue.http.post('/api/comments', comment, {
            params: {
                threadUrl: threadUrl,
                threadTitle: threadTitle
            }
        }).then(function handleSuccess(response) {
            if (response.status === 201) {
                var commentSignature = response.headers.get('X-Signature');
                return response.json().then(function (newComment) {
                    storeSignature(newComment, commentSignature);
                    return Promise.resolve(newComment);
                });
            }
        }, function handleError(response) {
            return Promise.reject('cannot_post');
        });
    },
    canEditComment: function canEditComment(comment) {
        return getSignature(comment);
    }
};

function storeSignature(comment, signature) {
    localStorage.setItem(getSignatureKey(comment), signature);
}

function getSignature(comment) {
    localStorage.getItem(getSignatureKey(comment));
}

function getSignatureKey(comment) {
    return 'platon-comment-' + comment.id;
}
