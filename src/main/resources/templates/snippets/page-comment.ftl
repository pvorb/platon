<#ftl output_format="HTML"/>

<#macro page_comment thread comments comment in_form=false>
    <div id="comment-${comment.id}" class="comment media mb-3 p-3">
        <img class="mr-3" width="64" height="64" style="background: orangered">
        <div class="media-body">
            <header class="meta mb-3">
                <#if comment.author?? && comment.url??>
                    by <b><a href="${comment.url}">${comment.author}</a></b>
                <#elseif comment.author??>
                    by <b>${comment.author}</b>
                <#elseif comment.url??>
                    by <a href="${comment.url}">an anonymous user</a>
                <#else>
                    by an anonymous user
                </#if>
                on
                <time>${comment.creationDate}</time>
                <#if !in_form>
                (<a href="#comment-${comment.id}">Permalink</a>)
                </#if>

                <#if comment.parentId??>
                    <#assign parentComment=comments?api.get(comment.parentId)/>
                    <#assign clippedParentText=parentComment.text?replace('<[^>]+>', '', 'r')[0..*80]/>
                    <#if clippedParentText?length &lt; parentComment.text?length>
                    <div class="small">(In reply to comment
                        <a href="/threads/${thread.id}/comments#comment-${parentComment.id}">“${clippedParentText}…”</a>)</div>
                    <#else>
                    <div class="small">(In reply to comment
                        <a href="/threads/${thread.id}/comments#comment-${parentComment.id}">“${parentComment.text}”</a>)</div>
                    </#if>
                </#if>
            </header>

            <div class="comment-content">
                ${comment.text?no_esc}
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
