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
                return Promise.reject(response);
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
        });
    },
    deleteComment: function deleteComment(comment) {
        return Vue.http.delete('/api/comments/' + comment.id);
    },
    canEditComment: function canEditComment(comment) {
        return !!getSignature(comment);
    },
    canDeleteComment: function canDeleteComment(comment) {
        return !!getSignature(comment);
    },
    countComments: function countComments(threadUrls) {
        return Vue.http.get('/api/comments/counts', {
            params: {
                threadUrl: threadUrls
            }
        }).then(function handleSuccess(response) {
            return response.json().then(function (json) {
                return Promise.resolve(json.commentCounts);
            });
        });
    }
};

function storeSignature(comment, signature) {
    localStorage.setItem(getSignatureKey(comment), signature);
}

function getSignature(comment) {
    return localStorage.getItem(getSignatureKey(comment));
}

function getSignatureKey(comment) {
    return 'platon-comment-' + comment.id;
}
