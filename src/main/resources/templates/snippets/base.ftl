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

        .comment:target {
            box-shadow: 0 0 1rem 0 #ddd;
            transition: box-shadow .5s ease-in-out;
        }
    </style>

    <@page_title/>
</head>
<body>
    <div class="container">
        <header class="mt-5 mb-3">
            <h1 style="font-size: 2rem">
                <@page_header/>
            </h1>
        </header>

        <@page_content/>

        <footer class="mt-5 mb-3">
            Powered by <a href="https://github.com/pvorb/platon">Platon</a>
        </footer>
    </div>
</body>
</html>
</#macro>
