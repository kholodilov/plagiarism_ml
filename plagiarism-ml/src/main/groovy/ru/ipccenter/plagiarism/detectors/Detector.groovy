package ru.ipccenter.plagiarism.detectors

import ru.ipccenter.plagiarism.solutions.SolutionsPair

/**
 *
 * @author kholodilov
 */
public interface Detector
{
    DetectionResult performDetection(SolutionsPair pair)
}