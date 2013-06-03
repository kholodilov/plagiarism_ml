package ru.ipccenter.plagiarism.detectors.impl

import ru.ipccenter.plagiarism.detectors.DetectionError
import ru.ipccenter.plagiarism.similarity.SimilarityDegree
import ru.ipccenter.plagiarism.util.Util

/**
 *
 * @author dmitry
 */
class PlaggieAdaptiveDetectionResult extends PlaggieDetectionResult
{
    private final double correctedSimilarity
    private final Collection<TokenSequence> falseDuplicateSequences

    private DetectionError correctedDetectionError

    PlaggieAdaptiveDetectionResult(PlaggieDetectionResult plainResult, double correctedSimilarity,
                                   Collection<TokenSequence> falseDuplicateSequences)
    {
        super(plainResult.pair, plainResult.similarity, plainResult.report, plainResult.tokenFrequencies,
                plainResult.duplicates, plainResult.totalTokensCount)
        this.correctedSimilarity = correctedSimilarity
        this.falseDuplicateSequences = falseDuplicateSequences
    }

    double getCorrectedSimilarity()
    {
        return correctedSimilarity
    }

    DetectionError getCorrectedError()
    {
        if (correctedDetectionError == null)
        {
            correctedDetectionError = new DetectionError(
                    pair.estimatedSimilarityDegree, correctedSimilarityDegree)
        }
        return correctedDetectionError
    }

    SimilarityDegree getCorrectedSimilarityDegree()
    {
        return SimilarityDegree.valueOf(correctedSimilarity)
    }

    boolean isFalseDuplicatesFound()
    {
        return ! falseDuplicateSequences.isEmpty()
    }

    int getFalseDuplicatesCount()
    {
        return falseDuplicateSequences.size()
    }

    @Override
    String toString()
    {
        return "$pair ($error(${Util.format(similarity)}) -> $correctedError(${Util.format(correctedSimilarity)}))"
    }
}
