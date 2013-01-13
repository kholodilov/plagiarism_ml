package ru.ipccenter.plagiarism.util

import ru.ipccenter.plagiarism.Author
import ru.ipccenter.plagiarism.Solution
import ru.ipccenter.plagiarism.SolutionsPair
import ru.ipccenter.plagiarism.Task

import static ru.ipccenter.plagiarism.util.StandardStructureHelper.findSolutionInStandardStructure

/**
 *
 * @author kholodilov
 */
class AllSolutionsPairsLoader implements SolutionsPairsLoader
{
    private final ArrayList<Task> tasks
    private final File testDataDirectory

    AllSolutionsPairsLoader(ArrayList<Task> tasks, File testDataDirectory)
    {
        this.tasks = tasks
        this.testDataDirectory = testDataDirectory
    }

    @Override
    Map<Task, List<SolutionsPair>> loadSolutionsPairs()
    {
        return makeSolutionsPairs(loadAllSolutions())
    }

    Map<Task, List<Solution>> loadAllSolutions()
    {
        Map<Task, List<Solution>> taskSolutions = [:].withDefault { [] }

        tasks.each { task ->
            testDataDirectory.eachDir { authorDir ->
                try {
                    taskSolutions[task].add(
                            findSolutionInStandardStructure(testDataDirectory, task, new Author(authorDir.name))
                    )
                } catch (SolutionNotFoundException e)
                {
                    // skip missing solution
                }
            }
        }
        return taskSolutions
    }

    private Map<Task, List<SolutionsPair>> makeSolutionsPairs(Map<Task, List<Solution>> taskSolutions)
    {
        Map<Task, List<SolutionsPair>> taskSolutionsPairs = [:].withDefault { [] }
        taskSolutions.each { task, solutions ->
            for (int i = 0; i < solutions.size(); i++)
            {
                for (int j = i + 1; j < solutions.size(); j++)
                {
                    taskSolutionsPairs[task] << new SolutionsPair(solutions[i], solutions[j])
                }
            }
        }
        return taskSolutionsPairs
    }
}
