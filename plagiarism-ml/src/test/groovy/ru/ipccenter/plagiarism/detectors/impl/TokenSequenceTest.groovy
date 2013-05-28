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
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence = new TokenSequence(["a", "b", "c"])

        assertThat(subsequence.isSubsequenceOf(sequence), is(true))
    }

    @Test
    public void detectsSubsequenceThatSequenceEndsWith()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence = new TokenSequence(["b", "c", "d"])

        assertThat(subsequence.isSubsequenceOf(sequence), is(true))
    }

    @Test
    public void detectsSubsequenceInTheMiddleOfSequence()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence = new TokenSequence(["b", "c"])

        assertThat(subsequence.isSubsequenceOf(sequence), is(true))
    }

    @Test
    public void detectsSequenceAsSubsequenceOfItself()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])

        assertThat(sequence.isSubsequenceOf(sequence), is(true))
    }

    @Test
    public void doesntDetectSubsequenceThatIsLongerThanSequence()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence = new TokenSequence(["a", "b", "c", "d", "e"])

        assertThat(subsequence.isSubsequenceOf(sequence), is(false))
    }

    @Test
    public void doesntDetectSubsequenceThatDiffersInSomeElements()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence = new TokenSequence(["a", "d", "c"])

        assertThat(subsequence.isSubsequenceOf(sequence), is(false))
    }

    // fuzzy subsequence

    @Test
    public void detectsFuzzySubsequenceWhenEquals()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence = new TokenSequence(["a", "b", "c", "d"])

        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 0), is(true))
        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 1), is(true))
        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 2), is(true))
    }

    @Test
    public void detectsFuzzySubsequenceWithMissingLeftMargin()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence1 = new TokenSequence(["b", "c", "d"])
        def subsequence2 = new TokenSequence(["c", "d"])

        assertThat(subsequence1.isFuzzySubsequenceOf(sequence, 1), is(true))
        assertThat(subsequence1.isFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence1.isFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence2.isFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence2.isFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence2.isFuzzySubsequenceOf(sequence, 0), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithMissingRightMargin()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence1 = new TokenSequence(["a", "b", "c"])
        def subsequence2 = new TokenSequence(["a", "b"])

        assertThat(subsequence1.isFuzzySubsequenceOf(sequence, 1), is(true))
        assertThat(subsequence1.isFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence1.isFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence2.isFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence2.isFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence2.isFuzzySubsequenceOf(sequence, 0), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithSingleDifferentToken()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence = new TokenSequence(["a", "x", "c", "d"])

        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 1), is(true))
        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 0), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithDifferentTokenAndMargin()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence1 = new TokenSequence(["b", "x", "d"])
        def subsequence2 = new TokenSequence(["a", "x", "c"])

        assertThat(subsequence1.isFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence1.isFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence1.isFuzzySubsequenceOf(sequence, 0), is(false))

        assertThat(subsequence2.isFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence2.isFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence2.isFuzzySubsequenceOf(sequence, 0), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithTwoConsequentDifferentTokens()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d"])
        def subsequence = new TokenSequence(["a", "x", "x", "d"])

        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 0), is(false))
    }

    @Test
    public void detectsFuzzySubsequenceWithTwoDifferentTokens()
    {
        def sequence = new TokenSequence(["a", "b", "c", "d", "e"])
        def subsequence = new TokenSequence(["a", "x", "c", "x", "e"])

        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 2), is(true))
        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 1), is(false))
        assertThat(subsequence.isFuzzySubsequenceOf(sequence, 0), is(false))
    }

    // utility

    @Test
    public void calculatesDifferentTokensCount()
    {
        assertThat(getDifferentTokensCount(["a", "b"], ["a", "b"]), is(0))
        assertThat(getDifferentTokensCount(["a", "b"], ["x", "b"]), is(1))
        assertThat(getDifferentTokensCount(["a", "b"], ["b", "a"]), is(2))
    }

}
