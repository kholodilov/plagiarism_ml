<html>
    <head>
        <title>Plagiarism ML manual checker</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
        <div style="width: 100%">${model.comparisonResult.info}</div>
        <div style="float: left; width: 50%">
            <textarea style="width: 100%; height: 100%">${model.comparisonResult.leftSource}</textarea>
        </div>
        <div style="float: right; width: 50%">
            <textarea style="width: 100%; height: 100%">${model.comparisonResult.rightSource}</textarea>
        </div>
    </body>
</html>