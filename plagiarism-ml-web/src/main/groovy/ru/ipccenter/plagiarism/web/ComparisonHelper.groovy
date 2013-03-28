package ru.ipccenter.plagiarism.web

import ru.ipccenter.plagiarism.model.*

/**
 *
 * @author kholodilov
 */
class ComparisonHelper

{
    private leftSource
    private rightSource
    private info

    private final TaskRepository taskRepository
    private final SolutionRepository solutionRepository

    ComparisonHelper(TaskRepository taskRepository, SolutionRepository solutionRepository)
    {
        this.taskRepository = taskRepository
        this.solutionRepository = solutionRepository
    }

    public void reload(String task_name, String author1_name, String author2_name)
    {
        def task = taskRepository.find(task_name)
        def author1 = new Author(author1_name)
        def author2 = new Author(author2_name)

        def solution1 = findOrGetRandomSolution(task, author1)
        def solution2 = findOrGetRandomSolution(task, author2)

        info = "$solution1.author $solution2.author"
        leftSource = solution1.file.text
        rightSource = solution2.file.text
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
