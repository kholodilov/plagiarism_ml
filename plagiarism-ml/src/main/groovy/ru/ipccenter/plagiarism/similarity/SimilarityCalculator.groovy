package ru.ipccenter.plagiarism.similarity

import com.madgag.interval.Interval

import static com.madgag.interval.SimpleInterval.interval

/**
 *
 * @author dmitry
 */
class SimilarityCalculator
{
    private final int maxDegree
    private final List<Interval<BigDecimal>> intervals

    SimilarityCalculator(int maxDegree)
    {
        this.maxDegree = maxDegree
        this.intervals =
            (0..maxDegree).collect { center ->
                interval((center - 0.5) / maxDegree, (center + 0.5) / maxDegree)
            }
    }

    boolean isZeroDegree(double similarity)
    {
        intervals[0].contains(new BigDecimal(similarity))
    }
}
