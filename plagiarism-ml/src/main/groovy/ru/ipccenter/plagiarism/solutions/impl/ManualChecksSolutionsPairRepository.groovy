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
        List<SolutionsPair> solutionsPairs = []

        def manual_checks_file = new File(dataDirectoryPath + "/manual_checks", task.name + ".txt")
        if (manual_checks_file.exists()) {
            manual_checks_file.eachLine { solutionsPairLine ->
                if (!solutionsPairLine.startsWith("#")) {
                    try {
                        solutionsPairs.add(
                                loadSolutionsPair(task, solutionsPairLine)
                        )
                    } catch (ManualCheckParseException e) {
                        println e.message
                    } catch (SolutionNotFoundException e) {
                        println e.message
                    }
                }
            }
        }
        return solutionsPairs
    }

    private SolutionsPair loadSolutionsPair(Task task, String solutionsPairLine)
    {
        def matcher = solutionsPairLine =~ /(\S+) (\S+) (\d+)/
        if (!matcher.matches())
        {
            throw new ManualCheckParseException("Failed to parse manual check line: " + solutionsPairLine);
        }

        def author1 = new Author(matcher.group(1))
        def author2 = new Author(matcher.group(2))
        def similarityDegree = Integer.parseInt(matcher.group(3))

        def solution1 = solutionRepository.findSolutionFor(task, author1)
        def solution2 = solutionRepository.findSolutionFor(task, author2)

        return new SolutionsPair(solution1, solution2, SimilarityDegree.valueOf(similarityDegree));
    }
}
