package ru.ipccenter.plagiarism.web

import ru.ipccenter.plagiarism.solutions.*

/**
 *
 * @author kholodilov
 */
class ComparisonHelper
{

    private final TaskRepository taskRepository
    private final SolutionRepository solutionRepository

    ComparisonHelper(TaskRepository taskRepository, SolutionRepository solutionRepository)
    {
        this.taskRepository = taskRepository
        this.solutionRepository = solutionRepository
    }

    ComparisonResult simpleComparison(String task_name, String author1_name, String author2_name)
    {
        def task = taskRepository.find(task_name)
        def author1 = new Author(author1_name)
        def author2 = new Author(author2_name)

        def solution1 = findOrGetRandomSolution(task, author1)
        def solution2 = findOrGetRandomSolution(task, author2)

        return new ComparisonResult(
            solution1.file.text,
            solution2.file.text,
            "$solution1.author $solution2.author")
    }

    ComparisonResult plaggieComparison(
            String task_name, String author1_name, String author2_name, int minimumMatchLengthi)
    {
        return new ComparisonResult(
                "",
                "",
                "")
    }

    private Solution findOrGetRandomSolution(Task task, Author author1)
    {
        try {
            return solutionRepository.findSolutionFor(task, author1)
        }
        catch (SolutionNotFoundException e) {
            return solutionRepository.findRandomSolutionFor(task)
        }
    }

}
