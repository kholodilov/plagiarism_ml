package ru.ipccenter.plagiarism.similarity

import com.madgag.interval.Interval

import static com.madgag.interval.SimpleInterval.interval

/**
 *
 * @author dmitry
 */
class SimilarityDegree implements Comparable<SimilarityDegree>
{
    static SimilarityDegree UNKNOWN = new SimilarityDegree(-1)

    private static int MAX_DEGREE = 4
    private static DEGREES = (0..MAX_DEGREE).collect { degree -> new SimilarityDegree(degree) }

    private final int degree
    Interval<BigDecimal> interval

    private SimilarityDegree(int degree)
    {
        this.degree = degree
        this.interval = interval((degree - 0.5) / MAX_DEGREE, (degree + 0.5) / MAX_DEGREE)
    }

    static SimilarityDegree valueOf(int degree)
    {
        if (degree < 0 || degree > MAX_DEGREE)
        {
            throw new SimilarityCalculationException("Similarity degree is out of bounds: $degree")
        }
        return DEGREES[degree]
    }

    static SimilarityDegree valueOf(double similarity)
    {
        def degree = DEGREES.find { it.equalTo(similarity) }
        if (degree == null)
        {
            throw new SimilarityCalculationException("Similarity is out of bounds: $similarity")
        }
        return degree
    }

    double getSimilarity()
    {
        return degree / MAX_DEGREE
    }

    boolean isZero()
    {
        return degree == 0
    }

    int getValue()
    {
        return degree
    }

    boolean equalTo(double similarity)
    {
        interval.contains(new BigDecimal(similarity))
    }

    @Override
    int compareTo(SimilarityDegree other)
    {
        return degree - other.degree
    }

    @Override
    boolean equals(other)
    {
        if (this.is(other)) return true
        return degree == ((SimilarityDegree) other).degree
    }

    @Override
    int hashCode()
    {
        return degree
    }
}
