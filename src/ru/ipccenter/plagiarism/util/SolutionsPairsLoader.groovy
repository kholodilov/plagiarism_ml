package ru.ipccenter.plagiarism.util

import ru.ipccenter.plagiarism.SolutionsPair
import ru.ipccenter.plagiarism.Task

/**
 *
 * @author kholodilov
 */
public interface SolutionsPairsLoader
{
    Map<Task, List<SolutionsPair>> loadSolutionsPairs()
}