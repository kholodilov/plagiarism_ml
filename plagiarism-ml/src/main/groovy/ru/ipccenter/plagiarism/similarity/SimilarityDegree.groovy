package ru.ipccenter.plagiarism.similarity

import com.madgag.interval.Interval

import static com.madgag.interval.SimpleInterval.interval

/**
 *
 * @author dmitry
 */
class SimilarityDegree implements Comparable<SimilarityDegree>
{
    private final int degree
    private final int maxDegree
    Interval<BigDecimal> interval

    protected SimilarityDegree(int degree, int maxDegree)
    {
        this.degree = degree
        this.maxDegree = maxDegree
        this.interval = interval((degree - 0.5) / maxDegree, (degree + 0.5) / maxDegree)
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
        if (getClass() != other.class) return false

        return  degree == ((SimilarityDegree) other).degree
    }

    @Override
    int hashCode()
    {
        return degree
    }
}
