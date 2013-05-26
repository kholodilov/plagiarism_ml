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
    private final PlaggieAdaptiveMode adaptiveMode

    PlaggieAdaptiveDetector(int minimumMatchLength, PlaggieAdaptiveMode adaptiveMode)
    {
        this.plainDetector = new PlaggieDetector(minimumMatchLength)
        this.adaptiveMode = adaptiveMode
    }

    def learnOnPairsWithZeroEstimatedSimilarity(List<SolutionsPair> learningPairs)
    {
        learningPairs.each { pair ->
            def duplicates = plainDetector.performDetection(pair).duplicates
            duplicates.collect { it.tokenSequence }.each { tokenSequence ->
                learnedFalseDuplicateSequences[tokenSequence] += 1
            }
        }
        println "Learned false duplicate sequences: ${learnedFalseDuplicateSequences.values().sum()}," +
                " unique: ${learnedFalseDuplicateSequences.size()}"
    }

    @Override
    PlaggieAdaptiveDetectionResult performDetection(SolutionsPair pair)
    {
        def plainResult = plainDetector.performDetection(pair)
        List<TokenSequence> falseDuplicateSequences =
            findFalseDuplicateSequences(plainResult, adaptiveMode.falseDuplicateCondition)

        double correctedSimilarity =
            (plainResult.duplicateTokensCount - falseDuplicateSequences.collect { it.size() }.sum(0)) / plainResult.totalTokensCount
        return new PlaggieAdaptiveDetectionResult(plainResult, correctedSimilarity, falseDuplicateSequences)
    }

    private List<TokenSequence> findFalseDuplicateSequences(PlaggieDetectionResult plainResult, Closure falseDuplicateCondition)
    {
        def falseDuplicateSequences = []
        plainResult.duplicates.each { duplicate ->
            def bestDuplicateSequence = TokenSequence.EMPTY
            for (learnedSequence in learnedFalseDuplicateSequences.keySet())
            {
                if (falseDuplicateCondition(duplicate, learnedSequence) && learnedSequence.size() > bestDuplicateSequence.size())
                {
                    if (duplicate.tokenSequence.size() <= learnedSequence.size())
                    {
                        bestDuplicateSequence = duplicate.tokenSequence
                        break
                    }
                    else
                    {
                        bestDuplicateSequence = learnedSequence
                    }
                }
            }
            if (bestDuplicateSequence != TokenSequence.EMPTY)
            {
                falseDuplicateSequences.add(bestDuplicateSequence)
            }
        }
        return falseDuplicateSequences
    }

}
