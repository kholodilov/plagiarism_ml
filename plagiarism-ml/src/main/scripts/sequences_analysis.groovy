import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveDetector
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl
import ru.ipccenter.plagiarism.util.Util

import static java.lang.Math.abs
import static ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveMode.*

def dataDirectoryPath = args[0]

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def solutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, dataDirectoryPath)

def TASK = "collections2"
def LEARNING_GROUPS = ["L1", "L2"]
def CONTROL_GROUPS = ["C_LOW", "C_HIGH"]
def ADAPTIVE_MODES = [
        ["Subsequence", SUBSEQUENCE],
        ["Reverse subsequence (size2)", subsequenceOrReverseSubsequence(2)],
        ["Reverse subsequence (size3)", subsequenceOrReverseSubsequence(3)],
        ["Fuzzy (diff4, size3)", diffOnlyFuzzySubsequenceOrReverseSubsequence(4, 3)],
        ["Fuzzy (diff3, size2)", diffOnlyFuzzySubsequenceOrReverseSubsequence(3, 2)],
        ["Fuzzy (levenshtein4)", fuzzySubsequenceOrReverseSubsequence(4)],
        ["Fuzzy (levenshtein5)", fuzzySubsequenceOrReverseSubsequence(5)],
]

[taskRepository.find(TASK)].each { task ->

    println "### $task\n"

    (8..11).each { minMatchLength ->

        println "### MINIMUM_MATCH_LENGTH=$minMatchLength\n"

        ADAPTIVE_MODES.each { adaptiveModeName, adaptiveMode ->

            LEARNING_GROUPS.each { learningGroup ->

                def learningPairs = solutionsPairRepository.findFor(task, learningGroup)
                def detector = new PlaggieAdaptiveDetector(minMatchLength, adaptiveMode)
                detector.learnOnPairsWithZeroEstimatedSimilarity(learningPairs)

                CONTROL_GROUPS.each { controlGroup ->

                    println "$adaptiveModeName, $learningGroup, $controlGroup"

                    def controlPairs = solutionsPairRepository.findFor(task, controlGroup)
                    def detectionResults = controlPairs.collect { detector.performDetection(it) }

/*
                    def improvedResults = detectionResults.findAll { it.correctedQuality > it.quality }
                    def degradedResults = detectionResults.findAll { it.correctedQuality < it.quality }
                    def sameResultsWithFalseDuplicates =
                        detectionResults.findAll { it.falseDuplicatesFound && it.correctedQuality == it.quality }
*/

/*
                    def originalDeltas = detectionResults.collect { abs(it.pair.estimatedSimilarity - it.similarity) }
                    def correctedDeltas = detectionResults.collect { abs(it.pair.estimatedSimilarity - it.correctedSimilarity) }
*/

                    Map<Integer, Integer> originalQualityStatistics =
                        detectionResults.collect { it.quality.value }.inject([:].withDefault {0}) {
                            map, value ->  map[value]++; map }
                    Map<Integer, Integer> correctedQualityStatistics =
                        detectionResults.collect { it.correctedQuality.value }.inject([:].withDefault {0}) {
                            map, value ->  map[value]++; map }
                    def originalQualitySum = originalQualityStatistics.collect { quality, count -> abs(quality) * count }.sum()
                    def correctedQualitySum = correctedQualityStatistics.collect { quality, count -> abs(quality) * count }.sum()

/*
                    println "In control group found " + detectionResults.sum { it.falseDuplicatesCount } +
                            " false duplicates for " +  detectionResults.count { it.falseDuplicatesFound } + " pairs"
*/

/*
                    println "Original delta: " + getStatistics(originalDeltas)
                    println "Corrected delta: " + getStatistics(correctedDeltas)
*/

                    println "\tOriginal quality statistics: ${originalQualityStatistics.sort()} (${originalQualitySum})"
                    println "\tCorrected quality statistics: ${correctedQualityStatistics.sort()} (${correctedQualitySum})"
                    println ""

/*
                    println "Improved results: " + printSizeAndPercentOfTotal(improvedResults, detectionResults)
                    improvedResults.each { println "\t$it" }
                    println "Degraded results: " + printSizeAndPercentOfTotal(degradedResults, detectionResults)
                    degradedResults.each { println "\t$it" }
                    println "Same results with false duplicates: " +
                                                   printSizeAndPercentOfTotal(sameResultsWithFalseDuplicates, detectionResults)
                    sameResultsWithFalseDuplicates.each { println "\t$it" }
*/
                }
            }
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
