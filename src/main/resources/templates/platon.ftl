<#ftl output_format="HTML" strip_whitespace=true>
<#import "/spring.ftl" as spring/>
<#macro formValidationClass path field>
    <@spring.bind path/>
    <#if !spring.status.errors.hasErrors()>
        <#assign validationClass=""/>
    <#elseif spring.status.errors.hasFieldErrors(field)>
        <#assign validationClass="is-invalid"/>
    <#else>
        <#assign validationClass="is-valid"/>
    </#if>
</#macro>
