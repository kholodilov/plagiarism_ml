package ru.ipccenter.plagiarism.detectors.impl

import org.testng.annotations.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static ru.ipccenter.plagiarism.detectors.impl.TokenSequence.getDifferentTokensCount

/**
 * @author dmitry
 */
public class TokenSequenceTest
{

    // precise subsequence

    @Test
    public void detectsSubsequenceThatSequenceStartsWith()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence = new TokenSequence([1, 2, 3] as int[])

        assertThat(subsequence.isSubsequenceOf(sequence), is(true))
    }

    @Test
    public void detectsSubsequenceThatSequenceEndsWith()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence = new TokenSequence([2, 3, 4] as int[])

        assertThat(subsequence.isSubsequenceOf(sequence), is(true))
    }

    @Test
    public void detectsSubsequenceInTheMiddleOfSequence()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence = new TokenSequence([2, 3] as int[])

        assertThat(subsequence.isSubsequenceOf(sequence), is(true))
    }

    @Test
    public void detectsSequenceAsSubsequenceOfItself()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])

        assertThat(sequence.isSubsequenceOf(sequence), is(true))
    }

    @Test
    public void doesntDetectSubsequenceThatIsLongerThanSequence()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence = new TokenSequence([1, 2, 3, 4, 5] as int[])

        assertThat(subsequence.isSubsequenceOf(sequence), is(false))
    }

    @Test
    public void doesntDetectSubsequenceThatDiffersInSomeElements()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence = new TokenSequence([1, 4, 3] as int[])

        assertThat(subsequence.isSubsequenceOf(sequence), is(false))
    }

    // fuzzy subsequence

    @Test
    public void detectsFuzzySubsequenceWhenEquals()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence = new TokenSequence([1, 2, 3, 4] as int[])

        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(true))
        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(true))
        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))

        assertThat(subsequence.isLevenshteinDistanceNotGreaterThan(sequence, 0), is(true))
    }

    @Test
    public void detectsFuzzySubsequenceWithMissingLeftMargin()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence1 = new TokenSequence([2, 3, 4] as int[])
        def subsequence2 = new TokenSequence([3, 4] as int[])

        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(true))
        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 2, 2), is(true))
        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 2, 1), is(false))

        assertThat(subsequence1.isLevenshteinDistanceNotGreaterThan(sequence, 1), is(true))
        assertThat(sequence.isLevenshteinDistanceNotGreaterThan(subsequence1, 1), is(true))
        assertThat(subsequence1.isLevenshteinDistanceNotGreaterThan(sequence, 0), is(false))

        assertThat(subsequence2.isLevenshteinDistanceNotGreaterThan(sequence, 2), is(true))
        assertThat(sequence.isLevenshteinDistanceNotGreaterThan(subsequence2, 2), is(true))
        assertThat(subsequence2.isLevenshteinDistanceNotGreaterThan(sequence, 1), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithMissingRightMargin()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence1 = new TokenSequence([1, 2, 3] as int[])
        def subsequence2 = new TokenSequence([1, 2] as int[])

        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(true))
        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 2, 2), is(true))
        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 2, 1), is(false))

        assertThat(subsequence1.isLevenshteinDistanceNotGreaterThan(sequence, 1), is(true))
        assertThat(sequence.isLevenshteinDistanceNotGreaterThan(subsequence1, 1), is(true))
        assertThat(subsequence1.isLevenshteinDistanceNotGreaterThan(sequence, 0), is(false))

        assertThat(subsequence2.isLevenshteinDistanceNotGreaterThan(sequence, 2), is(true))
        assertThat(sequence.isLevenshteinDistanceNotGreaterThan(subsequence2, 2), is(true))
        assertThat(subsequence2.isLevenshteinDistanceNotGreaterThan(sequence, 1), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithSingleDifferentToken()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence = new TokenSequence([1, 0, 3, 4] as int[])

        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(true))
        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence.isLevenshteinDistanceNotGreaterThan(sequence, 1), is(true))
    }

    @Test
    public void detectsFuzzySubsequenceWithDifferentTokenAndMargin()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence1 = new TokenSequence([2, 0, 4] as int[])
        def subsequence2 = new TokenSequence([1, 0, 3] as int[])

        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence1.isLevenshteinDistanceNotGreaterThan(sequence, 2), is(true))
        assertThat(sequence.isLevenshteinDistanceNotGreaterThan(subsequence1, 2), is(true))
        assertThat(subsequence1.isLevenshteinDistanceNotGreaterThan(sequence, 1), is(false))

        assertThat(subsequence2.isLevenshteinDistanceNotGreaterThan(sequence, 2), is(true))
        assertThat(sequence.isLevenshteinDistanceNotGreaterThan(subsequence2, 2), is(true))
        assertThat(subsequence2.isLevenshteinDistanceNotGreaterThan(sequence, 1), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithTwoConsequentDifferentTokens()
    {
        def sequence = new TokenSequence([1, 2, 3, 4] as int[])
        def subsequence = new TokenSequence([1, 0, 0, 4] as int[])

        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence.isLevenshteinDistanceNotGreaterThan(sequence, 2), is(true))
        assertThat(subsequence.isLevenshteinDistanceNotGreaterThan(sequence, 1), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithTwoDifferentTokens()
    {
        def sequence = new TokenSequence([1, 2, 3, 4, 5] as int[])
        def subsequence = new TokenSequence([1, 0, 3, 0, 5] as int[])

        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence.isDiffOnlyFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence.isLevenshteinDistanceNotGreaterThan(sequence, 2), is(true))
        assertThat(subsequence.isLevenshteinDistanceNotGreaterThan(sequence, 1), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithoutSizeLimit()
    {
        def sequence = new TokenSequence([1, 2, 3, 4, 5] as int[])
        def subsequence1 = new TokenSequence([0, 3, 0] as int[])
        def subsequence2 = new TokenSequence([2, 3, 4] as int[])

        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceWithoutSizeLimitOf(sequence, 2), is(true))
        assertThat(subsequence1.isDiffOnlyFuzzySubsequenceWithoutSizeLimitOf(sequence, 1), is(false))

        assertThat(subsequence2.isDiffOnlyFuzzySubsequenceWithoutSizeLimitOf(sequence, 0), is(true))
    }

    @Test
    public void detectsLevenshteinDistanceWithInsertsAndDeletes()
    {
        def sequence1 = new TokenSequence([1, 2, 3, 4, 5, 6] as int[])
        def sequence2 = new TokenSequence([1, 3, 4, 6] as int[])

        assertThat(sequence1.isLevenshteinDistanceNotGreaterThan(sequence2, 2), is(true))
        assertThat(sequence2.isLevenshteinDistanceNotGreaterThan(sequence1, 2), is(true))
        assertThat(sequence1.isLevenshteinDistanceNotGreaterThan(sequence2, 1), is(false))
        assertThat(sequence2.isLevenshteinDistanceNotGreaterThan(sequence1, 1), is(false))
    }

    // utility

    @Test
    public void calculatesDifferentTokensCount()
    {
        assertThat(getDifferentTokensCount([1, 2] as int[], [1, 2] as int[]), is(0))
        assertThat(getDifferentTokensCount([1, 2] as int[], [0, 2] as int[]), is(1))
        assertThat(getDifferentTokensCount([1, 2] as int[], [2, 1] as int[]), is(2))
    }

}
