package ru.ipccenter.plagiarism.web

import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
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
        def solutionsPair = getSolutionsPair(task_name, author1_name, author2_name)

        def info = "$solutionsPair.solution1.author $solutionsPair.solution2.author"

        return new ComparisonResult(
                solutionsPair.solution1.file.text,
                solutionsPair.solution2.file.text,
                info)
    }

    ComparisonResult plaggieComparison(
            String task_name, String author1_name, String author2_name, int minimumMatchLength)
    {
        def solutionsPair = getSolutionsPair(task_name, author1_name, author2_name)

        def detector = new PlaggieDetector(minimumMatchLength)
        def detectionResult = detector.performDetection(solutionsPair)

        def info = "$solutionsPair.solution1.author $solutionsPair.solution2.author // " +
                   "${String.format('%.2f', detectionResult.similarity)}, " +
                   "min match $minimumMatchLength"

        return new ComparisonResult(
                detectionResult.firstSourceWithMatchedLines,
                detectionResult.secondSourceWithMatchedLines,
                info)
    }

    private SolutionsPair getSolutionsPair(String task_name, String author1_name, String author2_name)
    {
        def task = taskRepository.find(task_name)

        def solution1 = findOrGetRandomSolution(task, author1_name)
        def solution2 = findOrGetRandomSolution(task, author2_name)

        def solutionsPair = new SolutionsPair(solution1, solution2)
        return solutionsPair
    }

    private Solution findOrGetRandomSolution(Task task, String author_name)
    {
        if (author_name != null)
        {
            return solutionRepository.findSolutionFor(task, new Author(author_name))
        }
        else
        {
            return solutionRepository.findRandomSolutionFor(task)
        }
    }

}
