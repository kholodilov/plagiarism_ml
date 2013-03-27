package ru.ipccenter.plagiarism.impl

import ru.ipccenter.plagiarism.model.*

/**
 *
 * @author kholodilov
 */
class AllSolutionsPairRepository implements SolutionsPairRepository
{
    private final SolutionRepository solutionRepository
    private final ArrayList<Task> tasks
    private final File testDataDirectory

    AllSolutionsPairRepository(SolutionRepository solutionRepository, List<Task> tasks, File testDataDirectory)
    {
        this.solutionRepository = solutionRepository
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
        return solutionRepository.findAllSolutionsFor(task)
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
