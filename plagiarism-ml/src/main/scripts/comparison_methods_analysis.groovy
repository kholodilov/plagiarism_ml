import org.apache.commons.collections.map.MultiValueMap
import org.apache.commons.io.FileUtils
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.similarity.SimilarityDegree
import ru.ipccenter.plagiarism.solutions.Task
import ru.ipccenter.plagiarism.solutions.impl.AllSolutionsPairRepository
import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl

final TASKS = [
        //new Task("array1", "Array3dImpl.java"),
        new Task("collections2", "WordCounterImpl.java"),
        //new Task("reflection0", "ReflectionsImpl.java")
]

final DETECTORS = [
        "plaggie" : new PlaggieDetector(11),
        //"jccd" : new JCCDDetector()
]

def dataDirectoryPath = args[0]
def work_directory = new File(dataDirectoryPath)
def results_directory = new File(work_directory, "results")
def comparison_results_directory = new File(results_directory, "comparison")

def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)

if (results_directory.exists()) FileUtils.cleanDirectory(results_directory)

def repository = new AllSolutionsPairRepository(solutionRepository)
def task_solution_pairs = repository.loadSolutionsPairs(TASKS)

def nonZeroResults = []
DETECTORS.each { detectorName, detector ->
    task_solution_pairs.each { task, solution_pairs ->
        println "Processing ${task} with ${detectorName}"

        def task_results_directory = new File(comparison_results_directory, task.name)
        task_results_directory.mkdirs()
        solution_pairs.each{ pair ->
            def detectionResult = detector.performDetection(pair)
            pair.addDetectionResult(detectorName, detectionResult)

//            new File(task_results_directory,
//                    "${pair.solution1.author.name}_${pair.solution2.author.name}_${detectorName}.txt")
//            .withOutputStream { out ->
//                out << detectionResult.report
//            }
            if (SimilarityDegree.valueOf(detectionResult.similarity).value > 0)
            //if (detectionResult.similarity >= 0.2)
            {
                nonZeroResults.add(detectionResult)
                //println pair.solution1.author.name + " " + pair.solution2.author.name + " " + detectionResult.similarity
            }
        }
    }
}

nonZeroResults.sort { -it.similarity }.each
{
    println it.pair.solution1.author.name + " " + it.pair.solution2.author.name  + " " + it.similarity
}

task_solution_pairs.each { task, solutionsPairs ->
    println "task: ${task}"
    println(
        solutionsPairs.collectEntries(new MultiValueMap()) { solutionsPair ->
            solutionsPair.detectionResults.collectEntries { method, detectionResult ->
                [method, Math.abs(solutionsPair.estimatedSimilarity - detectionResult.similarity)]
            }
        }
        .collect { method, deltas ->
            //deltas = deltas.findAll { it > 0.20 }
            def statistics = new DescriptiveStatistics(deltas as double[])
            "${method}: ${statistics.mean}±${statistics.standardDeviation} (${deltas.size()})"
        }
    )
/*
    solutionsPairs.findAll { solutionsPair ->
        def resultsPlaggie = solutionsPair.detectionResults["plaggie"]
        def deltaPlaggie = Math.abs(solutionsPair.estimatedSimilarity - resultsPlaggie.similarity)
        def resultsJCCD = solutionsPair.detectionResults["jccd"]
        def deltaJCCD = Math.abs(solutionsPair.estimatedSimilarity - resultsJCCD.similarity)
        deltaPlaggie > 0.20 && deltaJCCD < deltaPlaggie
    }
    .each { println it }
*/
}
