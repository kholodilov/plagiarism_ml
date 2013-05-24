package ru.ipccenter.plagiarism.solutions
/**
 *
 * @author kholodilov
 */
public interface SolutionsPairRepository
{
    Map<Task, List<SolutionsPair>> loadSolutionsPairs(List<Task> listOfTasks)
    List<SolutionsPair> findFor(Task task)
    List<SolutionsPair> findFor(Task task, String ... groups)
}