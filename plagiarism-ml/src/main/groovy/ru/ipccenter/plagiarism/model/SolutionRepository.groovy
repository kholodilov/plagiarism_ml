package ru.ipccenter.plagiarism.model

/**
 *
 * @author dmitry
 */
interface SolutionRepository
{
    Solution findSolutionFor(Task task, Author author)
    List<Solution> findAllSolutionsFor(Task task)
}
