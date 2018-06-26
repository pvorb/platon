<#ftl output_format="HTML"/>
<#include "snippets/base.ftl"/>

<#macro page_title>
    <title>Error ${error.status}</title>
</#macro>

<#macro page_header>
</#macro>

<#macro page_content>
    <div class="jumbotron">
        <h1 class="display-4">Error ${error.status}</h1>
        <p class="lead">${error.message}</p>
        <#if error.cause??>
            <p>${error.cause.message}</p>
        </#if>
        <a href="/" class="btn btn-primary">Go back to homepage</a>
    </div>
</#macro>

<@page/>
