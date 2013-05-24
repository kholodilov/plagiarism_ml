package ru.ipccenter.plagiarism.solutions.impl

import ru.ipccenter.plagiarism.similarity.SimilarityDegree
import ru.ipccenter.plagiarism.solutions.*

/**
 *
 * @author kholodilov
 */
class ManualChecksSolutionsPairRepository implements SolutionsPairRepository
{

    private final String dataDirectoryPath
    private final SolutionRepository solutionRepository

    private Map<Task, List<SolutionsPair>> pairs = [:]

    ManualChecksSolutionsPairRepository(
            SolutionRepository solutionRepository, String dataDirectoryPath)
    {
        this.solutionRepository = solutionRepository
        this.dataDirectoryPath = dataDirectoryPath
    }

    @Override
    Map<Task, List<SolutionsPair>> loadSolutionsPairs(List<Task> listOfTasks)
    {
        Map<Task, List<SolutionsPair>> taskSolutionPairs = [:]
        listOfTasks.each { task ->
            taskSolutionPairs[task] = findFor(task)
        }
        return taskSolutionPairs;
    }

    @Override
    List<SolutionsPair> findFor(Task task)
    {
        if (!pairs.containsKey(task))
        {
            def inputFile = new File(dataDirectoryPath + "/manual_checks", task.name + ".txt")
            def inputLines = inputFile.readLines().findAll { !it.startsWith("#") }
            def pairsForTask = inputLines.collect { loadSolutionsPair(task, it) }
            pairs.put(task, pairsForTask)
        }

        return pairs.get(task)
    }

    @Override
    List<SolutionsPair> findFor(Task task, String ... groups)
    {
        return findFor(task).findAll { it.group in groups }
    }

    private SolutionsPair loadSolutionsPair(Task task, String inputLine)
    {
        def matcher = inputLine =~ /(\S+) (\S+) (\d+) (.+)/
        if (!matcher.matches())
        {
            throw new ManualCheckParseException("Failed to parse manual check line: " + inputLine);
        }

        def author1 = new Author(matcher.group(1))
        def author2 = new Author(matcher.group(2))
        def similarityDegree = Integer.parseInt(matcher.group(3))
        def group = matcher.group(4)

        def solution1 = solutionRepository.findSolutionFor(task, author1)
        def solution2 = solutionRepository.findSolutionFor(task, author2)

        return new SolutionsPair(solution1, solution2, SimilarityDegree.valueOf(similarityDegree), group);
    }
}
