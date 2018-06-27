<#ftl output_format="HTML"/>
<#include "snippets/base.ftl"/>
<#include "snippets/comment.ftl"/>

<#macro page_title>
    <title>Comments for “${thread.title}” | Platon</title>
</#macro>

<#macro page_header>
    Comments for “<a href="${thread.url}">${thread.title}</a>”
</#macro>

<#macro page_content>
    <div class="comments-flat">
        <#if commentCount &gt; 3>
        <header class="my-4">
            <a href="/threads/${thread.id}/comments/reply" class="mr-3 btn btn-primary">Leave a comment</a>
        </header>
        </#if>
        <#list comments as id, comment>
            <@page_comment thread comments comment/>
        </#list>
        <footer class="my-3">
            <a href="/threads/${thread.id}/comments/reply" class="mr-3 btn btn-primary">Leave a comment</a>
        </footer>
    </div>
</#macro>

<@page/>
