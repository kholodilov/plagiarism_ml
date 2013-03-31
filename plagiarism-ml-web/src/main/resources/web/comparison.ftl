<html>
    <head>
        <title>Plagiarism ML manual checker</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
        <form>
            Authors: <input type="text" name="authors" />
            <input type="submit" value="Compare" />
        </form>

        <hr/>

        <div style="width: 100%; font-weight: bold">${model.comparisonResult.info}</div>
        <br/>

        <div style="float: left; width: 50%">
            <textarea style="width: 100%; height: 100%">${model.comparisonResult.leftSource}</textarea>
        </div>
        <div style="float: right; width: 50%">
            <textarea style="width: 100%; height: 100%">${model.comparisonResult.rightSource}</textarea>
        </div>
    </body>
</html>