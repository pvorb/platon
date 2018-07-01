<#ftl output_format="HTML"/>

<#macro page_comment thread comments comment in_form=false>
    <div id="comment-${comment.id!"preview"}" class="comment media mb-3 p-3">
        <img class="mr-3" width="64" height="64" src="/avatars/${base64Url(comment.authorHash)}">
        <div class="media-body">
            <header class="meta mb-3">
                <#if comment.url??>
                    by <b><a href="${comment.url}">${comment.author}</a></b>
                <#else>
                    by <b>${comment.author}</b>
                </#if>
                on
                <time>${comment.creationDate}</time>
                <#if !in_form>
                (<a href="#comment-${comment.id}">Permalink</a>)
                </#if>

                <#if comment.parentId??>
                    <#assign parentComment=comments?api.get(comment.parentId)/>
                    <div class="small">(In reply to comment
                        <a href="/threads/${thread.id}/comments#comment-${parentComment.id}">“${parentComment.textReference}”</a>)
                    </div>
                </#if>
            </header>

            <div class="comment-content">
                ${comment.textHtml?no_esc}
            </div>

            <#if !in_form>
            <footer class="mt-3">
                <a href="/threads/${thread.id}/comments/${comment.id}/reply" class="mr-3">Reply</a>
                <a href="/threads/${thread.id}/comments/${comment.id}/edit">Edit</a>
            </footer>
            </#if>
        </div>
    </div>
</#macro>
