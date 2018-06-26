<#ftl output_format="HTML"/>

<#macro page_comment comments comment>
    <div id="comment-${comment.id}" class="comment media mb-5">
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
                (<a href="#comment-${comment.id}">Permalink</a>)

                <#if comment.parentId??>
                    <#assign parentComment=comments?api.get(comment.parentId)/>
                    <#assign clippedParentText=parentComment.text?replace('<[^>]+>', '', 'r')[0..*30]/>
                    <div class="small">Reply to comment <a
                            href="#comment-${parentComment.id}">
                        <#if clippedParentText?length < parentComment.text?length>
                            “${clippedParentText}…”
                        <#else>
                            “${parentComment.text}”
                        </#if>
                    </a>
                    </div>
                </#if>
            </header>

            <div class="comment-content">
                ${comment.text?no_esc}
            </div>

            <footer class="mt-3">
                <a href="/comments/${comment.id}/reply" class="mr-3">Reply</a>
                <a href="/comments/${comment.id}/vote" class="mr-3">Vote</a>
                <a href="/comments/${comment.id}/edit">Edit</a>
            </footer>
        </div>
    </div>
</#macro>
