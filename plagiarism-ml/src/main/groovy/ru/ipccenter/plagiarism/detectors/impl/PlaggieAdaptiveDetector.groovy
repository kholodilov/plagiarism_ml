package ru.ipccenter.plagiarism.detectors.impl

import ru.ipccenter.plagiarism.detectors.Detector
import ru.ipccenter.plagiarism.solutions.SolutionsPair

/**
 *
 * @author dmitry
 */
class PlaggieAdaptiveDetector implements Detector
{
    private final learnedFalseDuplicateSequences = new HashMap<TokenSequence, Integer>().withDefault{0}
    private final PlaggieDetector plainDetector

    PlaggieAdaptiveDetector(int minimumMatchLength)
    {
        this.plainDetector = new PlaggieDetector(minimumMatchLength)
    }

    def learnOnPairsWithZeroEstimatedSimilarity(List<SolutionsPair> learningPairs)
    {
        learningPairs.each { pair ->
            def duplicates = plainDetector.performDetection(pair).duplicates
            duplicates.collect { it.tokens }.each { tokenSequence ->
                learnedFalseDuplicateSequences[tokenSequence] += 1
            }
        }
        println "False duplicate sequences: ${learnedFalseDuplicateSequences.values().sum()}," +
                " unique: ${learnedFalseDuplicateSequences.size()}"
    }

    @Override
    PlaggieAdaptiveDetectionResult performDetection(SolutionsPair pair)
    {
        def plainResult = plainDetector.performDetection(pair)
        def falseDuplicateSequences =
            learnedFalseDuplicateSequences.keySet().intersect(plainResult.duplicates.collect { it.tokens })
        double correctedSimilarity =
            plainResult.similarity - falseDuplicateSequences.collect { it.size() }.sum(0) / plainResult.totalTokensCount
        return new PlaggieAdaptiveDetectionResult(plainResult, correctedSimilarity, falseDuplicateSequences)
    }

}
