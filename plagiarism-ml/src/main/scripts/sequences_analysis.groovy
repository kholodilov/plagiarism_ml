import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.detectors.impl.TokenSequence
import ru.ipccenter.plagiarism.similarity.SimilarityCalculator
import ru.ipccenter.plagiarism.solutions.SolutionsPair
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl

final int MAXIMUM_SIMILARITY_DEGREE = 4
final int MINIMUM_MATCH_LENGTH = 11

def dataDirectoryPath = args[0]

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def solutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, dataDirectoryPath, MAXIMUM_SIMILARITY_DEGREE)

def detector = new PlaggieDetector(MINIMUM_MATCH_LENGTH)
def similarityCalculator = new SimilarityCalculator(MAXIMUM_SIMILARITY_DEGREE)

taskRepository.findAll().each { task ->
    println "###" + task

    def allPairs = solutionsPairRepository.findFor(task)
    def pairsWithZeroEstimatedSimilarity =
        allPairs.findAll { similarityCalculator.isZeroDegree(it.estimatedSimilarity) }
    def learningPairs = pairsWithZeroEstimatedSimilarity.subList(0, (int) (allPairs.size() / 2) + 1)
    def controlPairs = allPairs - learningPairs

    def falseDuplicateSequences = new HashMap<TokenSequence, Integer>().withDefault{0}
    learningPairs.each { pair ->
        def duplicates = detector.performDetection(pair).duplicates
        duplicates.collect { it.tokens }.each { tokenSequence ->
            falseDuplicateSequences[tokenSequence] += 1
        }
    }
    println "Total: ${falseDuplicateSequences.values().sum()}, unique: ${falseDuplicateSequences.size()}"

    println "In control group:"
    int totalFound = 0
    controlPairs.each { pair ->
        def detectionResult = detector.performDetection(pair)
        def found = falseDuplicateSequences.keySet().intersect(detectionResult.duplicates.collect { it.tokens })
        if (!found.isEmpty())
        {
            totalFound += found.size()
            println "$pair (${estimationQuality(pair, similarityCalculator)}): $found"
        }
    }
    println "Total: $totalFound"
}

String estimationQuality(SolutionsPair solutionsPair, SimilarityCalculator similarityCalculator)
{
    def estimatedDegree = similarityCalculator.degreeOf(solutionsPair.estimatedSimilarity)
    def detectedDegree = similarityCalculator.degreeOf(solutionsPair.detectedSimilarity)
    if (estimatedDegree < detectedDegree)
    {
        return "false positive"
    }
    else if (estimatedDegree > detectedDegree)
    {
        return "false negative"
    }
    else
    {
        return "correctly detected"
    }
}