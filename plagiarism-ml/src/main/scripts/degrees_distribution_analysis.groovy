import ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveDetector
import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.solutions.impl.AllSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl

import static ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveMode.fuzzySubsequenceOrReverseSubsequence

def dataDirectoryPath = args[0]

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def learningSolutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, dataDirectoryPath)
def allSolutionsPairRepository = new AllSolutionsPairRepository(solutionRepository)

def TASK = "collections2"
def LEARNING_GROUPS = ["L1", "L2"]
def ADAPTIVE_MODE = fuzzySubsequenceOrReverseSubsequence(5)

[taskRepository.find(TASK)].each { task ->

    def allPairs = allSolutionsPairRepository.findFor(task)

    (8..11).each { minMatchLength ->

        println "#### $task, minMatchLength=$minMatchLength"

        def plainDetector = new PlaggieDetector(minMatchLength)

        def originalDegreesDistribution =
                    allPairs.collect { plainDetector.performDetection(it).similarityDegree.value }
                            .inject([:].withDefault {0}) { map, value ->  map[value]++; map }
                            .sort()

        println "Original distribution: $originalDegreesDistribution"

        LEARNING_GROUPS.each { learningGroup ->

            def learningPairs = learningSolutionsPairRepository.findFor(task, learningGroup)

            def detector = new PlaggieAdaptiveDetector(minMatchLength, ADAPTIVE_MODE)
            detector.learnOnPairsWithZeroEstimatedSimilarity(learningPairs)

            def correctedDegreesDistribution =
                        allPairs.collect { detector.performDetection(it).correctedSimilarityDegree.value }
                                .inject([:].withDefault {0}) { map, value ->  map[value]++; map }
                                .sort()

            println "$learningGroup: $correctedDegreesDistribution"

        }

    }
}
