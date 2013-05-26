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

    private DetectionQuality detectionQuality

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

    DetectionQuality getQuality()
    {
        if (detectionQuality == null)
        {
            detectionQuality = new DetectionQuality(pair.estimatedSimilarityDegree, similarityDegree)
        }
        return detectionQuality
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
