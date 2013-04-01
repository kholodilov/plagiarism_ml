<html>
    <head>
        <title>Plagiarism ML manual checker</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="stylesheet" href="${servletContext.getContextPath()}/codemirror/codemirror.css" />
        <script src="${servletContext.getContextPath()}/codemirror/codemirror.js"></script>
        <script src="${servletContext.getContextPath()}/codemirror/clike.js"></script>
        <script type="text/javascript">
            conf = {
                lineNumbers: true,
                lineWrapping: true,
                matchBrackets: true,
                mode: "text/x-java",
                readOnly: true
            };
            window.onload = function() {
                CodeMirror.fromTextArea(document.getElementById('codearea_left'), conf);
                CodeMirror.fromTextArea(document.getElementById('codearea_right'), conf);
            }
        </script>
        <style type="text/css">
            .CodeMirror {
                background-color: #eee;
                height: 100%;
            }
        </style>
    </head>
    <body>
        <div style="width: 100%; height: 10%">
            <form>
                Authors: <input type="text" name="authors" value="${model.authors}" />
                <#include "/web/comparison/custom/form/${model.comparisonMethod}.ftl"/>
                <input type="submit" value="Compare" />
            </form>

            <div style="font-weight: bold">${model.comparisonResult.info}</div>
        </div>
        <div style="width: 100%; height: 90%">
            <div style="float: left; width: 50%">
                <textarea id="codearea_left">${model.comparisonResult.leftSource}</textarea>
            </div>
            <div style="float: right; width: 50%">
                <textarea id="codearea_right">${model.comparisonResult.rightSource}</textarea>
            </div>
        </div>
    </body>
</html>