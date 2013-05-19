import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveDetector
import ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveMode
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl
import ru.ipccenter.plagiarism.util.Util

import static java.lang.Math.abs

final int MINIMUM_MATCH_LENGTH = 8

def dataDirectoryPath = args[0]

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def solutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, dataDirectoryPath)

def detector = new PlaggieAdaptiveDetector(MINIMUM_MATCH_LENGTH, PlaggieAdaptiveMode.SUBSEQUENCE)

taskRepository.findAll().each { task ->

    def allPairs = solutionsPairRepository.findFor(task)
    def pairsWithZeroEstimatedSimilarity = allPairs.findAll { it.estimatedSimilarityDegree.isZero() }
    //Collections.shuffle(pairsWithZeroEstimatedSimilarity)
    def learningPairs = pairsWithZeroEstimatedSimilarity.subList(0, (int) (allPairs.size() / 2) + 1)
    def controlPairs = allPairs - learningPairs

    println "### $task (learning group: ${learningPairs.size()}, control group: ${controlPairs.size()})"

    detector.learnOnPairsWithZeroEstimatedSimilarity(learningPairs)

    def detectionResults = controlPairs.collect { detector.performDetection(it) }

    def improvedResults = detectionResults.findAll { it.correctedQuality > it.quality }
    def degradedResults = detectionResults.findAll { it.correctedQuality < it.quality }
    def sameResultsWithFalseDuplicates =
        detectionResults.findAll { it.falseDuplicatesFound && it.correctedQuality == it.quality }

    def originalDeltas = detectionResults.collect { abs(it.pair.estimatedSimilarity - it.similarity) }
    def correctedDeltas = detectionResults.collect { abs(it.pair.estimatedSimilarity - it.correctedSimilarity) }

    println "In control group found " + detectionResults.sum { it.falseDuplicatesCount } +
            " false duplicates for " +  detectionResults.count { it.falseDuplicatesFound } + " pairs"

    println "Original delta: " + getStatistics(originalDeltas)
    println "Corrected delta: " + getStatistics(correctedDeltas)

    println "Improved results: " + printSizeAndPercentOfTotal(improvedResults, detectionResults)
    improvedResults.each { println "\t$it" }
    println "Degraded results: " + printSizeAndPercentOfTotal(degradedResults, detectionResults)
    degradedResults.each { println "\t$it" }
    println "Same results with false duplicates: " +
                                   printSizeAndPercentOfTotal(sameResultsWithFalseDuplicates, detectionResults)
    sameResultsWithFalseDuplicates.each { println "\t$it" }
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
