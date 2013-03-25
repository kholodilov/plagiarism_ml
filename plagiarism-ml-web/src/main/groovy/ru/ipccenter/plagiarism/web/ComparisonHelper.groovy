package ru.ipccenter.plagiarism.web

import ru.ipccenter.plagiarism.Solution
import ru.ipccenter.plagiarism.Task
import ru.ipccenter.plagiarism.util.AllSolutionsPairsLoader

/**
 *
 * @author kholodilov
 */
class ComparisonHelper

{
    private leftSource
    private rightSource
    private info

    private static final TASKS = [
            new Task("array1", "Array3dImpl.java"),
            new Task("collections2", "WordCounterImpl.java"),
            new Task("reflection0", "ReflectionsImpl.java")
    ]
    private static final TEST_DATA_DIRECTORY = new File(System.getProperty("workDirectory") + "/test_data")

    private final AllSolutionsPairsLoader loader

    private final Map<Task, List<Solution>> task_solutions

    private final random = new Random()

    ComparisonHelper()
    {
        loader = new AllSolutionsPairsLoader(TASKS, TEST_DATA_DIRECTORY)
        task_solutions = loader.loadAllSolutions()
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
        def solutions = task_solutions.find { task, _ -> task.name == task_name }?.value
        if (solutions == null)
        {
            renderUnknownTask(task_name)
            return
        }

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

    private renderUnknownTask(String task)
    {
        info = "Unknown task: " + task
        leftSource = ""
        rightSource = ""
    }

    private renderMissingSolution(String task, String author) {
        info = "Cannot find solution of task '$task' for author '$author'"
        leftSource = ""
        rightSource = ""
    }

}
