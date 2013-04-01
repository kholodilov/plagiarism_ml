<html>
    <head>
        <title>Plagiarism ML manual checker</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
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
                <textarea style="width: 100%; height: 100%">${model.comparisonResult.leftSource}</textarea>
            </div>
            <div style="float: right; width: 50%">
                <textarea style="width: 100%; height: 100%">${model.comparisonResult.rightSource}</textarea>
            </div>
        </div>
    </body>
</html>