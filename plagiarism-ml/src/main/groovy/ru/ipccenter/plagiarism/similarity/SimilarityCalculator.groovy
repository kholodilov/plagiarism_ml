package ru.ipccenter.plagiarism.similarity

/**
 *
 * @author dmitry
 */
class SimilarityCalculator
{
    private final int maxDegree
    private final List<SimilarityDegree> degrees

    SimilarityCalculator(int maxDegree)
    {
        this.maxDegree = maxDegree
        this.degrees = (0..maxDegree).collect { degree -> new SimilarityDegree(degree, maxDegree) }
    }

    boolean isZeroDegree(double similarity)
    {
        degrees[0].equalTo(similarity)
    }

    SimilarityDegree degreeOf(double similarity)
    {
        def degree = degrees.find { it.equalTo(similarity) }
        if (degree == null)
        {
            throw new SimilarityCalculatorException("Similarity is out of bounds: $similarity")
        }
        degree
    }
}
