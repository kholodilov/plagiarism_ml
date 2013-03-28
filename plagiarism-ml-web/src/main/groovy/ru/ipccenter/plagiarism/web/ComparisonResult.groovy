package ru.ipccenter.plagiarism.web

/**
 *
 * @author dmitry
 */
class ComparisonResult
{
    private String leftSource
    private String rightSource
    private String info

    ComparisonResult(String leftSource, String rightSource, String info)
    {
        this.leftSource = leftSource
        this.rightSource = rightSource
        this.info = info
    }

    String getLeftSource()
    {
        return leftSource
    }

    String getRightSource()
    {
        return rightSource
    }

    String getInfo()
    {
        return info
    }
}
