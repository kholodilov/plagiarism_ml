package ru.ipccenter.plagiarism.solutions

import ru.ipccenter.plagiarism.detectors.DetectionResult;

/**
 * @author dmitry
 * @date 1/11/13
 */
public class SolutionsPair
{
    final Solution solution1
    final Solution solution2

    double estimatedSimilarity
    private Map<String, DetectionResult> detectionResults = [:]

    SolutionsPair(Solution solution1, Solution solution2)
    {
        this.solution1 = solution1
        this.solution2 = solution2
    }

    double getEstimatedSimilarity() {
        return estimatedSimilarity
    }

    void setEstimatedSimilarity(double estimatedSimilarity) {
        this.estimatedSimilarity = estimatedSimilarity
    }

    def getDetectionResults() {
        return detectionResults
    }

    void addDetectionResult(String detector, DetectionResult detectionResult) {
        detectionResults[detector] = detectionResult
    }

    @Override
    public String toString()
    {
        return "SolutionsPair{" +
                "author1=" + solution1.author +
                ", author2=" + solution2.author +
                ", estimatedSimilarity=" + estimatedSimilarity +
                ", detectionResults=" + detectionResults +
                '}';
    }
}
