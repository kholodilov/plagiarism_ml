package ru.ipccenter.plagiarism.impl

import ru.ipccenter.plagiarism.model.Author
import ru.ipccenter.plagiarism.model.Solution
import ru.ipccenter.plagiarism.model.SolutionsPair
import ru.ipccenter.plagiarism.model.SolutionsPairRepository
import ru.ipccenter.plagiarism.model.Task

import static ru.ipccenter.plagiarism.impl.StandardStructureHelper.findSolutionInStandardStructure

/**
 *
 * @author kholodilov
 */
class AllSolutionsPairRepository implements SolutionsPairRepository
{
    private final ArrayList<Task> tasks
    private final File testDataDirectory

    AllSolutionsPairRepository(List<Task> tasks, File testDataDirectory)
    {
        this.tasks = tasks
        this.testDataDirectory = testDataDirectory
    }

    @Override
    Map<Task, List<SolutionsPair>> loadSolutionsPairs()
    {
        return makeSolutionsPairs(loadAllSolutions())
    }

    @Override
    List<SolutionsPair> findFor(Task task)
    {
        return makeSolutionsPairs(loadAllSolutionsFor(task))
    }

    Map<Task, List<Solution>> loadAllSolutions()
    {
        Map<Task, List<Solution>> taskSolutions = [:]

        tasks.each { task ->
            taskSolutions[task] = loadAllSolutionsFor(task)
        }
        return taskSolutions
    }

    private List<Solution> loadAllSolutionsFor(Task task)
    {
        def solutions = []
        testDataDirectory.eachDir { authorDir ->
            try
            {
                solutions << findSolutionInStandardStructure(testDataDirectory, task, new Author(authorDir.name))
            }
            catch (SolutionNotFoundException e) {
                // skip missing solution
            }
        }
        return solutions
    }

    private Map<Task, List<SolutionsPair>> makeSolutionsPairs(Map<Task, List<Solution>> taskSolutions)
    {
        Map<Task, List<SolutionsPair>> taskSolutionsPairs = [:].withDefault { [] }
        taskSolutions.each { task, solutions ->
            taskSolutionsPairs[task].addAll(makeSolutionsPairs(solutions))
        }
        return taskSolutionsPairs
    }
    private List<SolutionsPair> makeSolutionsPairs(List<Solution> solutions)
    {
        List<SolutionsPair> solutionsPairs = []
        for (int i = 0; i < solutions.size(); i++)
        {
            for (int j = i + 1; j < solutions.size(); j++)
            {
                solutionsPairs << new SolutionsPair(solutions[i], solutions[j])
            }
        }
        return solutionsPairs
    }
}
