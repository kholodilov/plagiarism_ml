package ru.ipccenter.plagiarism;

/**
 * @author dmitry
 * @date 1/11/13
 */
public class SolutionsPair
{
    final Solution solution1
    final Solution solution2

    double estimatedSimilarity
    double detectedSimilarity
    private Map<String, List<Double>> tokenFrequencies

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

    double getDetectedSimilarity() {
        return detectedSimilarity
    }

    void setDetectedSimilarity(double detectedSimilarity) {
        this.detectedSimilarity = detectedSimilarity
    }

    Map<String, List<Double>> getTokenFrequencies() {
        return tokenFrequencies
    }

    void setTokenFrequencies(Map<String, List<Double>> tokenFrequencies) {
        this.tokenFrequencies = tokenFrequencies
    }
}
