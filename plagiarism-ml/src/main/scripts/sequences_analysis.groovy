import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveDetector
import ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveMode
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl
import ru.ipccenter.plagiarism.util.Util

import static java.lang.Math.abs

final int MINIMUM_MATCH_LENGTH = 11

def dataDirectoryPath = args[0]

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def solutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, dataDirectoryPath)

def LEARNING_GROUPS = ["L1", "L2", ["L1", "L2"] as String[]]
def CONTROL_GROUPS = ["C_LOW", "C_HIGH"]

taskRepository.findAll().each { task ->

    LEARNING_GROUPS.each { learningGroup ->

        def learningPairs = solutionsPairRepository.findFor(task, learningGroup)

        def detector = new PlaggieAdaptiveDetector(MINIMUM_MATCH_LENGTH,
                        PlaggieAdaptiveMode.fuzzySubsequenceOrReverseSubsequence(4, 3))

        detector.learnOnPairsWithZeroEstimatedSimilarity(learningPairs)

        CONTROL_GROUPS.each { controlGroup ->

            def controlPairs = solutionsPairRepository.findFor(task, controlGroup)

            println "### $task [learning group: $learningGroup (${learningPairs.size()}), control group: $controlGroup(${controlPairs.size()})]"

            def detectionResults = controlPairs.collect { detector.performDetection(it) }

            def improvedResults = detectionResults.findAll { it.correctedQuality > it.quality }
            def degradedResults = detectionResults.findAll { it.correctedQuality < it.quality }
            def sameResultsWithFalseDuplicates =
                detectionResults.findAll { it.falseDuplicatesFound && it.correctedQuality == it.quality }

            def originalDeltas = detectionResults.collect { abs(it.pair.estimatedSimilarity - it.similarity) }
            def correctedDeltas = detectionResults.collect { abs(it.pair.estimatedSimilarity - it.correctedSimilarity) }

            Map<Integer, Integer> originalQualityStatistics =
                detectionResults.collect { it.quality.value }.inject([:].withDefault {0}) {
                    map, value ->  map[value]++; map }
            Map<Integer, Integer> correctedQualityStatistics =
                detectionResults.collect { it.correctedQuality.value }.inject([:].withDefault {0}) {
                    map, value ->  map[value]++; map }
            def originalQualitySum = originalQualityStatistics.collect { quality, count -> abs(quality) * count }.sum()
            def correctedQualitySum = correctedQualityStatistics.collect { quality, count -> abs(quality) * count }.sum()

            println "In control group found " + detectionResults.sum { it.falseDuplicatesCount } +
                    " false duplicates for " +  detectionResults.count { it.falseDuplicatesFound } + " pairs"

            println "Original delta: " + getStatistics(originalDeltas)
            println "Corrected delta: " + getStatistics(correctedDeltas)

            println "Original quality statistics: ${originalQualityStatistics.sort()} (${originalQualitySum})"
            println "Corrected quality statistics: ${correctedQualityStatistics.sort()} (${correctedQualitySum})"

            println "Improved results: " + printSizeAndPercentOfTotal(improvedResults, detectionResults)
            improvedResults.each { println "\t$it" }
            println "Degraded results: " + printSizeAndPercentOfTotal(degradedResults, detectionResults)
            degradedResults.each { println "\t$it" }
            println "Same results with false duplicates: " +
                                           printSizeAndPercentOfTotal(sameResultsWithFalseDuplicates, detectionResults)
            sameResultsWithFalseDuplicates.each { println "\t$it" }
        }
    }
}

private String getStatistics(List<Double> deltas)
{
    def statistics = new DescriptiveStatistics(deltas as double[])
    "${Util.format(statistics.mean)}±${Util.format(statistics.standardDeviation)}"
}

private String printSizeAndPercentOfTotal(List<?> subList, List<?> baseList)
{
    def size = subList.size()
    double percentOfTotal = subList.size() * 100 / baseList.size()
    return "${size} (${Util.format(percentOfTotal)}%)"
}
