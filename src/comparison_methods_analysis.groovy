import org.apache.commons.io.FileUtils
@Grab(group='ru.ipccenter.plaggie', module='plaggie', version='1.0.1-SNAPSHOT')
import ru.ipccenter.plagiarism.Task
import ru.ipccenter.plagiarism.detectors.PlaggieDetector
import ru.ipccenter.plagiarism.util.ManualChecksSolutionsPairsLoader

final TASKS = [
        new Task("array1", "Array3dImpl.java"),
        new Task("collections2", "WordCounterImpl.java"),
        new Task("reflection0", "ReflectionsImpl.java")
]

final DETECTORS = [
        "plaggie" : new PlaggieDetector(8)
]

final int NUMBER_INTERVALS = 5
final int MAXIMUM_SIMILARITY_DEGREE = NUMBER_INTERVALS - 1

final PLAGGIE_DETECTOR = "plaggie"

def work_directory = new File(args[0])
def test_data_directory = new File(work_directory, "test_data")
def manual_checks_directory = new File(work_directory, "manual_checks")
def results_directory = new File(work_directory, "results")
def comparison_results_directory = new File(results_directory, "comparison")

if (results_directory.exists()) FileUtils.cleanDirectory(results_directory)

def task_solution_pairs =
    new ManualChecksSolutionsPairsLoader(TASKS, manual_checks_directory, test_data_directory, MAXIMUM_SIMILARITY_DEGREE)
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
        }
    }
}
