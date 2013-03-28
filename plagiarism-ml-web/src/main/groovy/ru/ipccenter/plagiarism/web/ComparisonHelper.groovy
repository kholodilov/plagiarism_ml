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

        return new ComparisonResult(
                solutionsPair.solution1.file.text,
                solutionsPair.solution2.file.text,
                "$solutionsPair.solution1.author $solutionsPair.solution2.author")
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
        def author1 = new Author(author1_name)
        def author2 = new Author(author2_name)

        def solution1 = findOrGetRandomSolution(task, author1)
        def solution2 = findOrGetRandomSolution(task, author2)

        def solutionsPair = new SolutionsPair(solution1, solution2)
        return solutionsPair
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
