package ru.ipccenter.plagiarism.web

import ru.ipccenter.plagiarism.model.Solution
import ru.ipccenter.plagiarism.model.SolutionRepository
import ru.ipccenter.plagiarism.model.TaskRepository

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

    private final random = new Random()

    ComparisonHelper(TaskRepository taskRepository, SolutionRepository solutionRepository)
    {
        this.taskRepository = taskRepository
        this.solutionRepository = solutionRepository
    }

    def getLeftSource()
    {
        return leftSource
    }

    def getRightSource()
    {
        return rightSource
    }

    def getInfo()
    {
        return info
    }

    public void reload(String task_name, String author1, String author2)
    {
        def task = taskRepository.find(task_name)
        def solutions = solutionRepository.findAllSolutionsFor(task)

        def solution1 = getSolutionForAuthorOrRandomSolution(solutions, author1)
        if (solution1 == null)
        {
            renderMissingSolution(task_name, author1)
            return
        }

        def solution2 = getSolutionForAuthorOrRandomSolution(solutions, author2)
        if (solution2 == null)
        {
            renderMissingSolution(task_name, author2)
            return
        }

        info = solution1.author.name + " " + solution2.author.name
        leftSource = solution1.file.text
        rightSource = solution2.file.text
    }

    private Solution getSolutionForAuthorOrRandomSolution(List<Solution> solutions, String author)
    {
        return author != null ?
                solutions.find { it.author.name == author } :
                getRandomSolution(solutions)
    }

    private getRandomSolution(List<Solution> solutions)
    {
        return solutions[random.nextInt(solutions.size())]
    }

    private renderMissingSolution(String task, String author) {
        info = "Cannot find solution of task '$task' for author '$author'"
        leftSource = ""
        rightSource = ""
    }

}
