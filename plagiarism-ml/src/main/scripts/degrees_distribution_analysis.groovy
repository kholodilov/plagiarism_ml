import ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveDetector
import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.solutions.impl.AllSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl

import static ru.ipccenter.plagiarism.detectors.impl.PlaggieAdaptiveMode.diffOnlyFuzzySubsequenceOrReverseSubsequence

def dataDirectoryPath = args[0]

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def learningSolutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, dataDirectoryPath)
def allSolutionsPairRepository = new AllSolutionsPairRepository(solutionRepository)

def ALLOWED_MANUAL = 25

/*def TASK = "reflection0"
def LEARNING_GROUPS = ["L1", "L2"]
def ADAPTIVE_MODE = fuzzySubsequenceOrReverseSubsequence(5)
def MML_FOR_PLAIN = 11
def MML_FOR_ADAPTIVE = 9..11*/

/*
def TASK = "collections2"
def LEARNING_GROUPS = ["L1", "L2"]
def ADAPTIVE_MODE = fuzzySubsequenceOrReverseSubsequence(4)
def MML_FOR_PLAIN = 10
def MML_FOR_ADAPTIVE = 9..10
*/

def TASK = "array1"
def LEARNING_GROUPS = ["L1", "L2"]
def ADAPTIVE_MODE = diffOnlyFuzzySubsequenceOrReverseSubsequence(4, 3) // fuzzySubsequenceOrReverseSubsequence(3)
def MML_FOR_PLAIN = 11
def MML_FOR_ADAPTIVE = 8..11

def plainDetector = new PlaggieDetector(MML_FOR_PLAIN)

[taskRepository.find(TASK)].each { task ->

    println "#### $task"

    def allPairs = allSolutionsPairRepository.findFor(task)

    def detectionResults = allPairs.collect { plainDetector.performDetection(it) }

    def originalDegreesDistribution =
        detectionResults.collect { it.similarityDegree.value }
                .inject([:].withDefault {0}) { map, value ->  map[value]++; map }
                .sort()

    print "Original distribution: $originalDegreesDistribution (MML=$MML_FOR_PLAIN), "

    println "Similarity threshold " + detectionResults.sort { -it.similarity }.take(ALLOWED_MANUAL).last().similarity

    MML_FOR_ADAPTIVE.each { minMatchLength ->

        LEARNING_GROUPS.each { learningGroup ->

            def learningPairs = learningSolutionsPairRepository.findFor(task, learningGroup).take(10)

            def detector = new PlaggieAdaptiveDetector(minMatchLength, ADAPTIVE_MODE)
            detector.learnOnPairsWithZeroEstimatedSimilarity(learningPairs)

            def correctedDetectionResults = allPairs.collect { detector.performDetection(it) }

            def correctedDegreesDistribution =
                correctedDetectionResults.collect { it.correctedSimilarityDegree.value }
                                .inject([:].withDefault {0}) { map, value ->  map[value]++; map }
                                .sort()

            print "$learningGroup: $correctedDegreesDistribution (MML=$minMatchLength), "

            println "Similarity threshold " +
                    correctedDetectionResults.sort { -it.correctedSimilarity }
                                    .take(ALLOWED_MANUAL - learningPairs.size()).last().correctedSimilarity


        }

    }
}
