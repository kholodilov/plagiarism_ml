package ru.ipccenter.plagiarism.detectors

import ru.ipccenter.plagiarism.solutions.SolutionsPair

/**
 *
 * @author kholodilov
 */
class DetectionResult
{
    private final SolutionsPair pair
    private final double similarity
    private final String report

    DetectionResult(SolutionsPair pair, double similarity, String report)
    {
        this.pair = pair
        this.similarity = similarity
        this.report = report
    }

    SolutionsPair getPair()
    {
        return pair
    }

    double getSimilarity()
    {
        return similarity
    }

    String getReport()
    {
        return report
    }

    @Override
    public String toString()
    {
        return "DetectionResult{" +
                "similarity=" + similarity +
                '}';
    }
}
