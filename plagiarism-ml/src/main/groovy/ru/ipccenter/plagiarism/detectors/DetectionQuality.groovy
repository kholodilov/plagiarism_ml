package ru.ipccenter.plagiarism.detectors

import ru.ipccenter.plagiarism.similarity.SimilarityDegree

/**
 *
 * @author dmitry
 */
class DetectionQuality implements Comparable<DetectionQuality>
{
    private final int quality

    DetectionQuality(SimilarityDegree estimatedDegree, SimilarityDegree detectedDegree)
    {
        quality = detectedDegree.value - estimatedDegree.value
    }

    @Override
    int compareTo(DetectionQuality other)
    {
        return Math.abs(other.quality) - Math.abs(this.quality)
    }

    @Override
    String toString()
    {
        if (quality > 0)
        {
            return "false positive"
        }
        else if (quality < 0)
        {
            return "false negative"
        }
        else
        {
            return "correctly detected"
        }
    }
}
