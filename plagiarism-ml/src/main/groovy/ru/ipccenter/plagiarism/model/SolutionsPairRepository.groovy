package ru.ipccenter.plagiarism.model

import ru.ipccenter.plagiarism.model.SolutionsPair
import ru.ipccenter.plagiarism.model.Task

/**
 *
 * @author kholodilov
 */
public interface SolutionsPairRepository
{
    Map<Task, List<SolutionsPair>> loadSolutionsPairs()
    List<SolutionsPair> findFor(Task task)
}