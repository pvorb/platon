<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css"
          integrity="sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB" crossorigin="anonymous">

    <title>Leave a comment | Platon</title>
</head>
<body>
    <div class="container" style="max-width: 50rem">
        <h1 class="my-3">
            Leave a comment for “<a href="${thread.url}">${thread.title}</a>”
        </h1>

        <#if parentComment??>
        <div id="parent-comment" class="mt-4">
            <h4>Replying to comment</h4>
            <div class="media mb-4">
                <img class="mr-3" width="64" height="64" style="background: limegreen">
                <div class="media-body">
                    <header class="meta mb-3">
                        by <b>Paul Vorbach</b> on
                        <time>2018-06-14 21:44</time>
                    </header>
                    <p>This is the content of our first comment. Please handle with care. This side up!</p>
                </div>
            </div>
        </div>
        </#if>

        <form method="post">
            <div class="form-group">
                <label class="sr-only" for="comment-form-text">Text of the comment</label>
                <textarea id="comment-form-text" name="commentText" class="form-control" rows="8"
                          placeholder="Your comment goes here"></textarea>
            </div>
            <div class="form-group">
                <label class="sr-only" for="comment-form-author">Name (optional)</label>
                <div class="input-group my-2">
                    <div class="input-group-prepend">
                        <div class="input-group-text">&#128100;</div>
                    </div>
                    <input id="comment-form-author" type="text" class="form-control" name="commentAuthor"
                           placeholder="Name (optional)">
                </div>
            </div>
            <div class="form-group">
                <label class="sr-only" for="commentUrl">URL (optional)</label>
                <div class="input-group my-2">
                    <div class="input-group-prepend">
                        <div class="input-group-text">&#128279;</div>
                    </div>
                    <input id="comment-form-url" type="text" class="form-control" name="commentUrl"
                           placeholder="URL (optional)">
                </div>
            </div>
            <div class="form-group">
                <div class="form-check">
                    <input id="comment-form-remember-me" class="form-check-input" type="checkbox"
                           name="commentRememberMe">
                    <label class="form-check-label" for="comment-form-remember-me">
                        Use cookies to remember my details
                    </label>
                </div>
            </div>
            <div class="form-group">
                <button class="btn btn-primary" name="action" value="submit">Post comment</button>
                <button class="btn btn-secondary" name="action" value="preview">Preview comment</button>
            </div>

            <input type="hidden" name="threadUrl" value="${threadUrl}">
            <input type="hidden" name="threadTitle" value="${threadTitle}">
        </form>
    </div>
</body>
</html>
