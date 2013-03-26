package ru.ipccenter.plagiarism.detectors

import ru.ipccenter.plagiarism.model.DetectionResult
import ru.ipccenter.plagiarism.model.SolutionsPair

/**
 *
 * @author kholodilov
 */
public interface Detector
{
    DetectionResult performDetection(SolutionsPair pair)
}