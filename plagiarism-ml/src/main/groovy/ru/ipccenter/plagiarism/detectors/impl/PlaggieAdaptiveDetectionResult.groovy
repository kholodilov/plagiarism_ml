package ru.ipccenter.plagiarism.detectors.impl

/**
 *
 * @author dmitry
 */
class PlaggieAdaptiveDetectionResult extends PlaggieDetectionResult
{
    private final double correctedSimilarity
    private final Collection<TokenSequence> falseDuplicateSequences

    PlaggieAdaptiveDetectionResult(PlaggieDetectionResult plainResult, double correctedSimilarity,
                                   Collection<TokenSequence> falseDuplicateSequences)
    {
        super(plainResult.similarity, plainResult.report, plainResult.tokenFrequencies,
              plainResult.duplicates, plainResult.totalTokensCount)
        this.falseDuplicateSequences = falseDuplicateSequences
        this.correctedSimilarity = correctedSimilarity
    }

    double getCorrectedSimilarity()
    {
        return correctedSimilarity
    }

    boolean isFalseDuplicatesFound()
    {
        return ! falseDuplicateSequences.isEmpty()
    }

    int getFalseDuplicatesCount()
    {
        return falseDuplicateSequences.size()
    }
}
