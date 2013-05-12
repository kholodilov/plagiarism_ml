package ru.ipccenter.plagiarism.detectors.impl

/**
 *
 * @author dmitry
 */
public enum PlaggieAdaptiveMode
{
    EXACT(
        { duplicate, learnedSequence -> duplicate.tokenSequence == learnedSequence}
    ),
    SUBSEQUENCE(
        { duplicate, learnedSequence -> duplicate.tokenSequence.isSubsequenceOf(learnedSequence) }
    );

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