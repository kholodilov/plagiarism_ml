package ru.ipccenter.plagiarism.detectors

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

    @Override
    public String toString()
    {
        return "DetectionResult{" +
                "similarity=" + similarity +
                '}';
    }
}
