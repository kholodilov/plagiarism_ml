package ru.ipccenter.plagiarism.solutions
/**
 *
 * @author kholodilov
 */
public interface SolutionsPairRepository
{
    Map<Task, List<SolutionsPair>> loadSolutionsPairs()
    List<SolutionsPair> findFor(Task task)
}