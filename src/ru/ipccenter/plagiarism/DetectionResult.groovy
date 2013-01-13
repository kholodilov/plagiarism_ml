package ru.ipccenter.plagiarism

/**
 *
 * @author kholodilov
 */
class DetectionResult
{
    private final double similarity
    String report

    DetectionResult(double similarity)
    {
        this.similarity = similarity
    }

    DetectionResult(double similarity, String report)
    {
        this.similarity = similarity
        this.report = report
    }

    double getSimilarity()
    {
        return similarity
    }

}
