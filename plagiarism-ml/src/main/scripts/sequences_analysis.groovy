import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.similarity.SimilarityCalculator
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl

import static com.madgag.interval.SimpleInterval.interval

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
    solutionsPairRepository
        .findFor(task)
        .findAll { similarityCalculator.isZeroDegree(it.estimatedSimilarity) }
        .each { solutionsPair ->
            def duplicates = detector.performDetection(solutionsPair).duplicates
            if (!duplicates.empty)
            {
                println duplicates.join("\n")
            }
        }
}