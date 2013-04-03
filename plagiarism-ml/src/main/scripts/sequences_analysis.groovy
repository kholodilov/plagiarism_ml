import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl

import static com.madgag.interval.SimpleInterval.interval

final int NUMBER_INTERVALS = 5
final int MAXIMUM_SIMILARITY_DEGREE = NUMBER_INTERVALS - 1
final SIMILARITY_INTERVALS =
    (0..MAXIMUM_SIMILARITY_DEGREE)
            .collect { center ->
        interval((center - 0.5) / MAXIMUM_SIMILARITY_DEGREE, (center + 0.5) / MAXIMUM_SIMILARITY_DEGREE)
    }

def dataDirectoryPath = args[0]
def manual_checks_directory = new File(dataDirectoryPath, "manual_checks")

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def solutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, manual_checks_directory, dataDirectoryPath, MAXIMUM_SIMILARITY_DEGREE)

def detector = new PlaggieDetector(8)

taskRepository.findAll().each { task ->
    println "###" + task
    solutionsPairRepository
        .findFor(task)
        .findAll { SIMILARITY_INTERVALS[0].contains(new BigDecimal(it.estimatedSimilarity)) }
        .each { solutionsPair ->
            def duplicates = detector.performDetection(solutionsPair).duplicates
            if (!duplicates.empty)
            {
                println duplicates.join("\n")
            }
        }
}