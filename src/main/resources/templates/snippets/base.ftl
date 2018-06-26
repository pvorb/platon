<#ftl output_format="HTML"/>

<#macro page_title>
    <!-- page_title -->
</#macro>

<#macro page_header>
    <!-- page_header -->
</#macro>

<#macro page_content>
    <!-- page_content -->
</#macro>

<#macro page_comment comments comment>
    <!-- page_comment -->
</#macro>

<#macro page>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <link rel="stylesheet" href="/webjars/bootstrap/4.1.1/css/bootstrap.min.css"
          integrity="sha384-WskhaSGFgHYWDcbwN70/dfYBj47jz9qbsMId/iRN3ewGhXQFZCSftd1LZCfmhktB">
    <style>
        .container {
            max-width: 50rem;
        }

        .comment {
            border-left: 2px solid white;
            padding-left: 0.5rem;
        }

        .comment:target {
            border-left: 2px solid #048;
            transition: border-left-color 800ms ease-in;
        }
    </style>

    <@page_title/>
</head>
<body>
    <div class="container">
        <header class="my-3">
            <h1>
                <@page_header/>
            </h1>
        </header>

        <@page_content/>
    </div>
</body>
</html>
</#macro>
