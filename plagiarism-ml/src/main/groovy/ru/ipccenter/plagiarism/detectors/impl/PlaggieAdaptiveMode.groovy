package ru.ipccenter.plagiarism.detectors.impl

/**
 *
 * @author dmitry
 */
public class PlaggieAdaptiveMode
{
    static PlaggieAdaptiveMode EXACT =
        new PlaggieAdaptiveMode({ duplicate, learnedSequence -> duplicate.tokenSequence == learnedSequence })

    static PlaggieAdaptiveMode SUBSEQUENCE =
        new PlaggieAdaptiveMode(
                { duplicate, learnedSequence -> duplicate.tokenSequence.isSubsequenceOf(learnedSequence) })

    static PlaggieAdaptiveMode SUBSEQUENCE_OR_REVERSE_SUBSEQUENCE =
        new PlaggieAdaptiveMode(
            { duplicate, learnedSequence ->
                duplicate.tokenSequence.isSubsequenceOf(learnedSequence) ||
                learnedSequence.isSubsequenceOf(duplicate.tokenSequence)
            })

    static PlaggieAdaptiveMode subsequenceOrReverseSubsequence(int maxReverseDeltaSize)
    {
        new PlaggieAdaptiveMode(
                { duplicate, learnedSequence ->
                    duplicate.tokenSequence.isSubsequenceOf(learnedSequence) ||
                    (
                        duplicate.tokenSequence.size() - learnedSequence.size() <= maxReverseDeltaSize &&
                        learnedSequence.isSubsequenceOf(duplicate.tokenSequence)
                    )
                })
    }

    static PlaggieAdaptiveMode diffOnlyFuzzySubsequenceOrReverseSubsequence(int maxDifferentTokens, int maxSizeDelta = maxDifferentTokens)
    {
        new PlaggieAdaptiveMode(
                { duplicate, learnedSequence ->
                    duplicate.tokenSequence.isDiffOnlyFuzzySubsequenceWithoutSizeLimitOf(learnedSequence, maxDifferentTokens) ||
                    learnedSequence.isDiffOnlyFuzzySubsequenceOf(duplicate.tokenSequence, maxDifferentTokens, maxSizeDelta)
                })
    }

    static PlaggieAdaptiveMode fuzzySubsequenceOrReverseSubsequence(int maxLevenshteinDistance)
    {
        new PlaggieAdaptiveMode(
                { duplicate, learnedSequence ->
                    duplicate.tokenSequence.isSubsequenceOf(learnedSequence) ||
                    learnedSequence.isLevenshteinDistanceNotGreaterThan(duplicate.tokenSequence, maxLevenshteinDistance)
                })
    }

    private final Closure falseDuplicateCondition

    PlaggieAdaptiveMode(Closure falseDuplicateCondition)
    {
        this.falseDuplicateCondition = falseDuplicateCondition
    }

    Closure getFalseDuplicateCondition()
    {
        return falseDuplicateCondition
    }
}