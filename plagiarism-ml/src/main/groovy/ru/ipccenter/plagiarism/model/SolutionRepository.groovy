package ru.ipccenter.plagiarism.model

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
