package ru.ipccenter.plagiarism.detectors.impl

import ru.ipccenter.plagiarism.detectors.DetectionResult

/**
 *
 * @author kholodilov
 */
class PlaggieDetectionResult extends DetectionResult
{

    private Map<String, Double> tokenFrequencies

    PlaggieDetectionResult(double similarity)
    {
        super(similarity)
    }

    PlaggieDetectionResult(double similarity, Map<String, Double> tokenFrequencies)
    {
        this(similarity)
        this.tokenFrequencies = tokenFrequencies
    }

    Map<String, Double> getTokenFrequencies()
    {
        return tokenFrequencies
    }
}