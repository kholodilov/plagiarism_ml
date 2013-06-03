package ru.ipccenter.plagiarism.detectors

import ru.ipccenter.plagiarism.similarity.SimilarityDegree

/**
 *
 * @author dmitry
 */
class DetectionError implements Comparable<DetectionError>
{
    private final int value

    DetectionError(SimilarityDegree estimatedDegree, SimilarityDegree detectedDegree)
    {
        value = detectedDegree.value - estimatedDegree.value
    }

    @Override
    int compareTo(DetectionError other)
    {
        return Math.abs(other.value) - Math.abs(this.value)
    }

    int getValue()
    {
        return value
    }

    @Override
    String toString()
    {
        if (value > 0)
        {
            return "false positive[$value]"
        }
        else if (value < 0)
        {
            return "false negative[$value]"
        }
        else
        {
            return "correctly detected"
        }
    }

    boolean equals(other)
    {
        if (this.is(other)) return true
        return value == ((DetectionError) other).value
    }

    int hashCode()
    {
        return value
    }
}
