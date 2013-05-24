package ru.ipccenter.plagiarism.solutions

import ru.ipccenter.plagiarism.detectors.DetectionResult
import ru.ipccenter.plagiarism.similarity.SimilarityDegree
import ru.ipccenter.plagiarism.util.Util;

/**
 * @author dmitry
 * @date 1/11/13
 */
public class SolutionsPair
{
    final Solution solution1
    final Solution solution2
    private final SimilarityDegree similarityDegree

    private final Map<String, DetectionResult> detectionResults = [:]
    private final String group

    SolutionsPair(Solution solution1, Solution solution2)
    {
        this.solution1 = solution1
        this.solution2 = solution2
        this.similarityDegree = SimilarityDegree.UNKNOWN
    }

    SolutionsPair(Solution solution1, Solution solution2, SimilarityDegree similarityDegree, String group)
    {
        this(solution1, solution2)
        this.group = group
        this.similarityDegree = similarityDegree
    }

    SimilarityDegree getEstimatedSimilarityDegree()
    {
        return similarityDegree
    }

    double getEstimatedSimilarity()
    {
        return similarityDegree.similarity
    }

    String getGroup()
    {
        return group
    }

    def getDetectionResults()
    {
        return detectionResults
    }

    void addDetectionResult(String detector, DetectionResult detectionResult) {
        detectionResults[detector] = detectionResult
    }

    @Override
    public String toString()
    {
        return "${solution1.author} ${solution2.author}, est. ${Util.format(estimatedSimilarity)}";
    }

}
