package ru.ipccenter.plagiarism.similarity;

import org.testng.annotations.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo;

/**
 * @author dmitry
 */
class SimilarityDegreeTest
{
    @Test
    void correctlyDetectsDegreeOfZeroSimilarity()
    {
        assertThat(SimilarityDegree.valueOf(0.0).value, equalTo(0))
    }

    @Test
    void correctlyDetectsDegreeOfFullSimilarity()
    {
        assertThat(SimilarityDegree.valueOf(1.0).value, equalTo(SimilarityDegree.MAX_DEGREE))
    }
}
