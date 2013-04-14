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

    double estimatedSimilarity = -1.0
    double detectedSimilarity = -1.0

    private Map<String, DetectionResult> detectionResults = [:]

    SolutionsPair(Solution solution1, Solution solution2)
    {
        this.solution1 = solution1
        this.solution2 = solution2
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
        return "${solution1.author} ${solution2.author}, est. ${format(estimatedSimilarity)}, det. ${format(detectedSimilarity)}";
    }

    private String format(double v)
    {
        String.format('%.2f', v)
    }
}
