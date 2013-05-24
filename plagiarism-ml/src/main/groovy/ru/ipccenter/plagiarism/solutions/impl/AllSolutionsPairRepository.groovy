package ru.ipccenter.plagiarism.solutions.impl

import ru.ipccenter.plagiarism.solutions.*

/**
 *
 * @author kholodilov
 */
class AllSolutionsPairRepository implements SolutionsPairRepository
{
    private final SolutionRepository solutionRepository

    AllSolutionsPairRepository(SolutionRepository solutionRepository)
    {
        this.solutionRepository = solutionRepository
    }

    @Override
    Map<Task, List<SolutionsPair>> loadSolutionsPairs(List<Task> listOfTasks)
    {
        return makeSolutionsPairs(loadAllSolutions(listOfTasks))
    }

    @Override
    List<SolutionsPair> findFor(Task task)
    {
        return makeSolutionsPairs(solutionRepository.findAllSolutionsFor(task))
    }

    Map<Task, List<Solution>> loadAllSolutions(List<Task> tasks)
    {
        Map<Task, List<Solution>> taskSolutions = [:]

        tasks.each { task ->
            taskSolutions[task] = solutionRepository.findAllSolutionsFor(task)
        }
        return taskSolutions
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
