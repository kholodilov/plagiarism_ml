import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import ru.ipccenter.plagiarism.detectors.DetectionQuality
import ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveDetector
import ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveMode
import ru.ipccenter.plagiarism.similarity.SimilarityCalculator
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl

final int MAXIMUM_SIMILARITY_DEGREE = 4
final int MINIMUM_MATCH_LENGTH = 8

def dataDirectoryPath = args[0]

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def solutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, dataDirectoryPath, MAXIMUM_SIMILARITY_DEGREE)

def detector = new PlaggieAdaptiveDetector(MINIMUM_MATCH_LENGTH, PlaggieAdaptiveMode.SUBSEQUENCE)
def similarityCalculator = new SimilarityCalculator(MAXIMUM_SIMILARITY_DEGREE)

taskRepository.findAll().each { task ->

    def allPairs = solutionsPairRepository.findFor(task)
    def pairsWithZeroEstimatedSimilarity =
        allPairs.findAll { similarityCalculator.isZeroDegree(it.estimatedSimilarity) }
    def learningPairs = pairsWithZeroEstimatedSimilarity.subList(0, (int) (allPairs.size() / 2) + 1)
    def controlPairs = allPairs - learningPairs

    println "### $task (learning group: ${learningPairs.size()}, control group: ${controlPairs.size()})"

    detector.learnOnPairsWithZeroEstimatedSimilarity(learningPairs)

    println "False duplicate found in control group:"
    int totalFalseDuplicatesCount = 0
    int totalPairsCorrected = 0
    def originalDeltas = [] as List<Double>
    def correctedDeltas = [] as List<Double>
    controlPairs.each { pair ->
        def detectionResult = detector.performDetection(pair)
        originalDeltas << Math.abs(pair.estimatedSimilarity - detectionResult.similarity)
        correctedDeltas << Math.abs(pair.estimatedSimilarity - detectionResult.correctedSimilarity)
        totalFalseDuplicatesCount += detectionResult.falseDuplicatesCount
        if (detectionResult.falseDuplicatesFound && !similarityCalculator.isZeroDegree(detectionResult.similarity))
        {
            totalPairsCorrected++
            println "$pair (${estimationQualityAndCorrection(pair.estimatedSimilarity, detectionResult.similarity, detectionResult.correctedSimilarity, similarityCalculator)})"
        }
    }
    println "False duplicates found: $totalFalseDuplicatesCount, pairs corrected: $totalPairsCorrected"
    println "Original statistics: " + getStatistics(originalDeltas)
    println "Corrected statistics: " + getStatistics(correctedDeltas)
}

private String getStatistics(List<Double> deltas)
{
    def statistics = new DescriptiveStatistics(deltas as double[])
    "${format(statistics.mean)}±${format(statistics.standardDeviation)}"
}

String estimationQualityAndCorrection(
        double estimatedSimilarity, double detectedSimilarity, double correctedSimilarity,
        SimilarityCalculator similarityCalculator)
{
    def estimatedDegree = similarityCalculator.degreeOf(estimatedSimilarity)
    def detectedDegree = similarityCalculator.degreeOf(detectedSimilarity)
    def correctedDegree = similarityCalculator.degreeOf(correctedSimilarity)
    return new DetectionQuality(estimatedDegree, detectedDegree).toString() + "[" + format(detectedSimilarity) + "] -> " +
           new DetectionQuality(estimatedDegree, correctedDegree).toString() + "[" + format(correctedSimilarity) + "]"
}

private String format(double v)
{
    String.format('%.2f', v)
}
