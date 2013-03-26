import org.apache.commons.io.FileUtils
import ru.ipccenter.plagiarism.model.Task
import ru.ipccenter.plagiarism.detectors.JCCDDetector
import ru.ipccenter.plagiarism.detectors.PlaggieDetector
import ru.ipccenter.plagiarism.impl.ManualChecksSolutionsPairRepository
import ru.ipccenter.plagiarism.impl.ManualChecksSolutionsPairRepository
import  org.apache.commons.collections.map.MultiValueMap
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics

final TASKS = [
        new Task("array1", "Array3dImpl.java"),
        new Task("collections2", "WordCounterImpl.java"),
        new Task("reflection0", "ReflectionsImpl.java")
]

final DETECTORS = [
        "plaggie" : new PlaggieDetector(11),
        "jccd" : new JCCDDetector()
]

final int NUMBER_INTERVALS = 5
final int MAXIMUM_SIMILARITY_DEGREE = NUMBER_INTERVALS - 1

def work_directory = new File(args[0])
def test_data_directory = new File(work_directory, "test_data")
def manual_checks_directory = new File(work_directory, "manual_checks")
def results_directory = new File(work_directory, "results")
def comparison_results_directory = new File(results_directory, "comparison")

if (results_directory.exists()) FileUtils.cleanDirectory(results_directory)

def task_solution_pairs =
    new ManualChecksSolutionsPairRepository(TASKS, manual_checks_directory, test_data_directory, MAXIMUM_SIMILARITY_DEGREE)
            .loadSolutionsPairs()

DETECTORS.each { detectorName, detector ->
    task_solution_pairs.each { task, solution_pairs ->
        println "Processing ${task} with ${detectorName}"

        def task_results_directory = new File(comparison_results_directory, task.name)
        task_results_directory.mkdirs()
        solution_pairs.each{ pair ->
            def detectionResult = detector.performDetection(pair)
            pair.addDetectionResult(detectorName, detectionResult)

            new File(task_results_directory,
                    "${pair.solution1.author.name}_${pair.solution2.author.name}_${detectorName}.txt")
            .withOutputStream { out ->
                out << detectionResult.report
            }
            println task.name + " " + pair.solution1.author.name + "_" + pair.solution2.author.name +
                    " " + detectionResult.similarity
        }
    }
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
