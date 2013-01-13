package ru.ipccenter.plagiarism.util

import ru.ipccenter.plagiarism.Author
import ru.ipccenter.plagiarism.SolutionsPair
import ru.ipccenter.plagiarism.Task

import static ru.ipccenter.plagiarism.util.StandardStructureHelper.findSolutionInStandardStructure

/**
 *
 * @author kholodilov
 */
class ManualChecksSolutionsPairsLoader implements SolutionsPairsLoader
{

    private final List<Task> tasks
    private final File manualChecksDirectory
    private final File testDataDirectory
    private final Object maximumSimilarityDegree

    ManualChecksSolutionsPairsLoader(
            List<Task> tasks,
            File manualChecksDirectory,
            File testDataDirectory,
            def maximumSimilarityDegree)
    {
        this.maximumSimilarityDegree = maximumSimilarityDegree
        this.testDataDirectory = testDataDirectory
        this.manualChecksDirectory = manualChecksDirectory
        this.tasks = tasks
    }

    @Override
    Map<Task, List<SolutionsPair>> loadSolutionsPairs()
    {
        Map<Task, List<SolutionsPair>> taskSolutionPairs = [:]
        tasks.each { task ->
            def manual_checks_file = new File(manualChecksDirectory, task.name + ".txt")
            List<SolutionsPair> solutionsPairs = []
            if (manual_checks_file.exists())
            {
                manual_checks_file.eachLine { solutionsPairLine ->
                    try {
                        solutionsPairs.add(
                                loadSolutionsPair(testDataDirectory, task, solutionsPairLine)
                        )
                    } catch (ManualCheckParseException e)
                    {
                        println e.message
                    } catch (SolutionNotFoundException e)
                    {
                        println e.message
                    }
                }
                taskSolutionPairs[task] = solutionsPairs
            }
        }
        return taskSolutionPairs;
    }

    private SolutionsPair loadSolutionsPair(File test_data_directory, Task task, String solutionsPairLine)
    {
        def matcher = solutionsPairLine =~ /(\S+) (\S+) (\d+)/
        if (!matcher.matches())
        {
            throw new ManualCheckParseException("Failed to parse manual check line: " + solutionsPairLine);
        }

        def author1 = new Author(matcher.group(1))
        def author2 = new Author(matcher.group(2))
        double estimatedSimilarity = Integer.parseInt(matcher.group(3)) / maximumSimilarityDegree

        def solution1 = findSolutionInStandardStructure(test_data_directory, task, author1)
        def solution2 = findSolutionInStandardStructure(test_data_directory, task, author2)

        def solutions_pair = new SolutionsPair(solution1, solution2)
        solutions_pair.setEstimatedSimilarity(estimatedSimilarity);

        return solutions_pair;
    }
}
