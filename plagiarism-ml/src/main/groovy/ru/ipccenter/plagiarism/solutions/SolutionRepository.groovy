package ru.ipccenter.plagiarism.solutions

/**
 *
 * @author dmitry
 */
interface SolutionRepository
{
    Solution findSolutionFor(Task task, Author author)
    Solution findRandomSolutionFor(Task task)
    List<Solution> findAllSolutionsFor(Task task)
}
