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
