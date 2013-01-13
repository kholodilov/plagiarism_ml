package ru.ipccenter.plagiarism.detectors

import ru.ipccenter.plagiarism.DetectionResult
import ru.ipccenter.plagiarism.SolutionsPair

/**
 *
 * @author kholodilov
 */
public interface Detector
{
    DetectionResult performDetection(SolutionsPair pair)
}