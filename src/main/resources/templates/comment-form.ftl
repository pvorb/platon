<#ftl output_format="HTML"/>
<#import "/spring.ftl" as spring/>
<#import "platon.ftl" as platon/>
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

    <form method="post" action="#preview-comment">
        <div class="form-group">
            <label class="sr-only" for="text">Text of the comment</label>
            <@platon.formValidationClass "comment" "text"/>
            <@spring.formTextarea "comment.text" 'class="form-control ${platon.validationClass}" rows="6" placeholder="Your comment goes here"'/>
            <div class="invalid-feedback">
                <@spring.showErrors ", "/>
            </div>
            <small class="text-muted">
                You can use <a href="http://commonmark.org/help/" target="_blank">Markdown</a> to mark up your comment.
            </small>
        </div>
        <div class="form-group">
            <label class="sr-only" for="author">Name</label>
            <div class="input-group">
                <div class="input-group-prepend">
                    <div class="input-group-text">&#128100;</div>
                </div>
                <@platon.formValidationClass "comment" "author"/>
                <@spring.formInput "comment.author" 'class="form-control rounded-right ${platon.validationClass}" placeholder="Name"'/>
                <div class="invalid-feedback">
                    <@spring.showErrors ", "/>
                </div>
            </div>
            <small class="text-muted">
                Your name. Feel free to enter a pseudonym if you don’t want to give your real name.
            </small>
        </div>
        <div class="form-group">
            <label class="sr-only" for="commentUrl">URL (optional)</label>
            <div class="input-group">
                <div class="input-group-prepend">
                    <div class="input-group-text">&#128279;</div>
                </div>
                <@platon.formValidationClass "comment" "url"/>
                <@spring.formInput "comment.url" 'class="form-control rounded-right ${platon.validationClass}" placeholder="URL (optional)"'/>
                <div class="invalid-feedback">
                    <@spring.showErrors ", "/>
                </div>
            </div>
            <small class="text-muted">
                An HTTP(S) URL that you want to link your name to. This might be a personal blog, Twitter account, etc.
            </small>
        </div>
        <div class="form-group">
            <div class="form-check">
                <@spring.formCheckbox "comment.acceptCookie" 'class="form-check-input"'/>
                <label class="form-check-label" for="acceptCookie">
                    I accept the use of a cookie in order to be able to edit my comments for a long period of time
                    and to keep the identicon next to my comments consistent. (optional)
                </label>
            </div>
            <small class="text-muted">
                A randomly generated identicon will be visible next to your comment, if you don't give consent. Please
                be aware, that you can withdraw your given consent at any time by clicking the “delete cookie” link
                next to any of your comments.
            </small>
        </div>
        <div class="form-group">
            <button class="btn btn-primary" name="action" value="CREATE">Create comment</button>
            <button class="btn btn-secondary" name="action" value="PREVIEW">Preview comment</button>
        </div>

        <input type="hidden" name="threadId" value="${thread.id}">
        <#if parentComment??>
        <input type="hidden" name="parentCommentId" value="${parentComment.id}">
        </#if>
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
    </form>
</#macro>

<@page/>
