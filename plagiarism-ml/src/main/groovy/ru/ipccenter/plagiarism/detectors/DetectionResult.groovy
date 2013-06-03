package ru.ipccenter.plagiarism.detectors

import ru.ipccenter.plagiarism.similarity.SimilarityDegree
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

    private DetectionError detectionError

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

    DetectionError getError()
    {
        if (detectionError == null)
        {
            detectionError = new DetectionError(pair.estimatedSimilarityDegree, similarityDegree)
        }
        return detectionError
    }

    SimilarityDegree getSimilarityDegree()
    {
        return SimilarityDegree.valueOf(similarity)
    }

    @Override
    public String toString()
    {
        return "DetectionResult{" +
                "similarity=" + similarity +
                '}';
    }
}
