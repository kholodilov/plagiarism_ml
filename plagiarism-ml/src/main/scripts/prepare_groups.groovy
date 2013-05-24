import ru.ipccenter.plagiarism.solutions.SolutionsPair
import ru.ipccenter.plagiarism.solutions.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl

def dataDirectoryPath = args[0]

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)
def solutionsPairRepository = new ManualChecksSolutionsPairRepository(solutionRepository, dataDirectoryPath)

final LEARNING_GROUP_SIZE = 15
final SIMILARITY_BOUND = 2

taskRepository.findAll().each { task ->

    def allPairs = solutionsPairRepository.findFor(task)

    def zeroSimilarityPairs = allPairs.findAll { it.estimatedSimilarityDegree.isZero() }
    def nonZeroSimilarityPairs = allPairs - zeroSimilarityPairs
    def lowSimilarityPairs = nonZeroSimilarityPairs.findAll { it.estimatedSimilarityDegree.value < SIMILARITY_BOUND }
    def highSimilarityPairs = nonZeroSimilarityPairs - lowSimilarityPairs

    def random = new Random()
    random.setSeed(1369418183142)
    Collections.shuffle(zeroSimilarityPairs, random)

    def learningGroup1 = zeroSimilarityPairs.subList(0, LEARNING_GROUP_SIZE)
    def learningGroup2 = zeroSimilarityPairs.subList(LEARNING_GROUP_SIZE, LEARNING_GROUP_SIZE * 2)

    def lowControlGroup =
        zeroSimilarityPairs.subList(LEARNING_GROUP_SIZE * 2, zeroSimilarityPairs.size()) + lowSimilarityPairs
    def highControlGroup = highSimilarityPairs

    println "\n### $task\n"

    learningGroup1.each { printPair(it, "L1") }
    learningGroup2.each { printPair(it, "L2") }

    lowControlGroup.each { printPair(it, "C_LOW") }
    highControlGroup.each { printPair(it, "C_HIGH") }
}

private void printPair(SolutionsPair pair, String group)
{
    println "$pair.solution1.author $pair.solution2.author $pair.estimatedSimilarityDegree $group"
}