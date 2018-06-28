<#ftl output_format="HTML"/>
<#include "snippets/base.ftl"/>

<#macro page_title>
    <title>${status} ${error}</title>
</#macro>

<#macro page_header>
</#macro>

<#macro page_content>
    <div class="jumbotron">
        <h1 class="display-4">${status} ${error}</h1>
        <p class="lead">${message}</p>
        <a href="/" class="btn btn-primary">Go back to homepage</a>
    </div>
</#macro>

<@page/>
