import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.detectors.impl.TokenSequence
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
    println falseDuplicateSequences.findAll { _, count -> count > 1}.entrySet().join("\n")
}