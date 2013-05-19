package ru.ipccenter.plagiarism.web

import ru.ipccenter.plagiarism.solutions.SolutionsPair

/**
 *
 * @author dmitry
 */
class ComparisonResult
{
    private String leftSource
    private String rightSource
    private String info
    private SolutionsPair pair

    ComparisonResult(SolutionsPair pair, String leftSource, String rightSource, String info)
    {
        this.pair = pair
        this.leftSource = leftSource
        this.rightSource = rightSource
        this.info = info
    }

    SolutionsPair getPair()
    {
        return pair
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
