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

    <#if previewComment??>
    <div id="preview-comment" class="mt-3">
        <h4>Preview of your comment</h4>
        <@page_comment thread comments previewComment true/>
    </div>
    </#if>

    <form method="post">
        <div class="form-group">
            <label class="sr-only" for="comment-form-text">Text of the comment</label>
            <textarea id="comment-form-text" name="text" class="form-control" rows="6"
                      placeholder="Your comment goes here">${(previewComment??)?then(previewComment.text, "")}</textarea>
        </div>
        <div class="form-group">
            <label class="sr-only" for="comment-form-author">Name</label>
            <div class="input-group my-2">
                <div class="input-group-prepend">
                    <div class="input-group-text">&#128100;</div>
                </div>
                <input id="comment-form-author" type="text" class="form-control" name="author"
                       placeholder="Name" value="${(previewComment??)?then(previewComment.author, "")}">
            </div>
        </div>
        <div class="form-group">
            <label class="sr-only" for="commentUrl">URL (optional)</label>
            <div class="input-group my-2">
                <div class="input-group-prepend">
                    <div class="input-group-text">&#128279;</div>
                </div>
                <input id="comment-form-url" type="text" class="form-control" name="url"
                       placeholder="URL (optional)" value="${(previewComment??)?then(previewComment.url, "")}">
            </div>
        </div>
        <div class="form-group">
            <div class="form-check">
                <input id="comment-form-accept-cookie" class="form-check-input" type="checkbox"
                       name="acceptCookie">
                <label class="form-check-label" for="comment-form-accept-cookie">
                    I accept the use of a cookie to keep the identicon next to my comments consistent.<br>
                    (If this is not accepted, an anonymized identicon will be visible next to your comment.)
                </label>
            </div>
        </div>
        <div class="form-group">
            <button class="btn btn-primary" name="action" value="create">Create comment</button>
            <button class="btn btn-secondary" name="action" value="preview">Preview comment</button>
        </div>

        <input type="hidden" name="threadId" value="${thread.id}">
        <#if parentComment??>
        <input type="hidden" name="parentCommentId" value="${parentComment.id}">
        </#if>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>
</#macro>

<@page/>
