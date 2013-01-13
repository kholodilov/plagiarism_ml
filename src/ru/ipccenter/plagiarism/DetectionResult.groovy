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

    double getSimilarity()
    {
        return similarity
    }

}
