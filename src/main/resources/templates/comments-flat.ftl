<#ftl output_format="HTML"/>
<#include "snippets/base.ftl"/>
<#include "snippets/comment.ftl"/>

<#macro page_title>
    <title>Comments</title>
</#macro>

<#macro page_header>
    Comments for “<a href="https://vorba.ch/2016/fast-spring-boot-development-cycle.html">Fast Front End
    Development Cycle with Spring Boot</a>”
</#macro>

<#macro page_content>
    <div class="comments-flat">
        <header class="my-4">
            <a href="/comments-reply?thread=/bla-url" class="mr-3 btn btn-primary">Leave a comment</a>
        </header>
        <#list comments as id, comment>
            <@page_comment comments comment/>
        </#list>
        <footer class="my-3">
            <a href="/comments-reply?thread=/bla-url" class="mr-3 btn btn-primary">Leave a comment</a>
        </footer>
    </div>
</#macro>

<@page/>
