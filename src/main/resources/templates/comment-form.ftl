<#ftl output_format="HTML"/>
<#include "snippets/base.ftl"/>
<#include "snippets/page-comment.ftl"/>

<#macro page_title>
    <title>Comments for “${thread.title}”</title>
</#macro>

<#macro page_header>
    Leave a comment on <a href="${thread.url}">“${thread.title}”</a>
</#macro>

<#macro page_content>
    <div class="my-3">
        <a href="/threads/${thread.id}/comments">Go back to thread</a>
    </div>
    <#if parentComment??>
    <div id="parent-comment" class="mt-3">
        <h4>Replying to comment</h4>
        <@page_comment thread comments parentComment true/>
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

        <input type="hidden" name="threadId" value="${thread.id}">
        <#if parentComment??>
        <input type="hidden" name="parentCommentId" value="${parentComment.id}">
        </#if>
    </form>
</#macro>

<@page/>
