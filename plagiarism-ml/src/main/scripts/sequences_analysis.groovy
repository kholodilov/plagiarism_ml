import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.detectors.impl.TokenSequence
import ru.ipccenter.plagiarism.similarity.SimilarityCalculator
import ru.ipccenter.plagiarism.similarity.SimilarityDegree
import ru.ipccenter.plagiarism.solutions.SolutionsPair
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl

final int MAXIMUM_SIMILARITY_DEGREE = 4
final int MINIMUM_MATCH_LENGTH = 8

def dataDirectoryPath = args[0]

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def solutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, dataDirectoryPath, MAXIMUM_SIMILARITY_DEGREE)

def detector = new PlaggieDetector(MINIMUM_MATCH_LENGTH)
def similarityCalculator = new SimilarityCalculator(MAXIMUM_SIMILARITY_DEGREE)

taskRepository.findAll().each { task ->

    def allPairs = solutionsPairRepository.findFor(task)
    def pairsWithZeroEstimatedSimilarity =
        allPairs.findAll { similarityCalculator.isZeroDegree(it.estimatedSimilarity) }
    def learningPairs = pairsWithZeroEstimatedSimilarity.subList(0, (int) (allPairs.size() / 2) + 1)
    def controlPairs = allPairs - learningPairs

    println "### $task (learning group: ${learningPairs.size()}, control group: ${controlPairs.size()})"

    def falseDuplicateSequences = new HashMap<TokenSequence, Integer>().withDefault{0}
    learningPairs.each { pair ->
        def duplicates = detector.performDetection(pair).duplicates
        duplicates.collect { it.tokens }.each { tokenSequence ->
            falseDuplicateSequences[tokenSequence] += 1
        }
    }
    println "False duplicate sequences: ${falseDuplicateSequences.values().sum()}," +
            " unique: ${falseDuplicateSequences.size()}"

    println "False duplicate found in control group:"
    int totalFalseDuplicatesFound = 0
    int totalPairsCorrected = 0
    controlPairs.each { pair ->
        def detectionResult = detector.performDetection(pair)
        if (!similarityCalculator.isZeroDegree(pair.detectedSimilarity))
        {
            def found = falseDuplicateSequences.keySet().intersect(detectionResult.duplicates.collect { it.tokens })
            if (!found.isEmpty())
            {
                totalFalseDuplicatesFound += found.size()
                totalPairsCorrected++
                double correctedSimilarity =
                    pair.detectedSimilarity - found.collect { it.size() }.sum() / detectionResult.totalTokensCount
                println "$pair (${estimationQualityAndCorrection(pair, correctedSimilarity, similarityCalculator)})" //: $found"
            }
        }
    }
    println "False duplicates found: $totalFalseDuplicatesFound, pairs corrected: $totalPairsCorrected"
}

String estimationQualityAndCorrection(SolutionsPair solutionsPair, double correctedSimilarity,
                                      SimilarityCalculator similarityCalculator)
{
    def estimatedDegree = similarityCalculator.degreeOf(solutionsPair.estimatedSimilarity)
    def detectedDegree = similarityCalculator.degreeOf(solutionsPair.detectedSimilarity)
    def correctedDegree = similarityCalculator.degreeOf(correctedSimilarity)
    return estimationQuality(estimatedDegree, detectedDegree) + " -> " +
           estimationQuality(estimatedDegree, correctedDegree) + "[" + String.format('%.2f', correctedSimilarity) + "]"
}

private String estimationQuality(SimilarityDegree estimatedDegree, SimilarityDegree detectedDegree)
{
    if (estimatedDegree < detectedDegree) {
        return "false positive"
    } else if (estimatedDegree > detectedDegree) {
        return "false negative"
    } else {
        return "correctly detected"
    }
}