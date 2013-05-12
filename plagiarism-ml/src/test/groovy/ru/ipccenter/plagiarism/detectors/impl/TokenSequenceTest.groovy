package ru.ipccenter.plagiarism.detectors.impl

import org.testng.annotations.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is

/**
 * @author dmitry
 */
public class TokenSequenceTest
{
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
}
