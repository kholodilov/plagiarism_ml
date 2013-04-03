import org.apache.commons.math3.stat.descriptive.StatisticalSummary
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.commons.io.FileUtils
import  org.apache.commons.collections.map.MultiValueMap
import ru.ipccenter.plagiarism.detectors.impl.PlaggieDetector
import ru.ipccenter.plagiarism.solutions.SolutionsPair
import ru.ipccenter.plagiarism.solutions.Task

import static com.madgag.interval.SimpleInterval.interval
import static com.madgag.interval.BeforeOrAfter.AFTER
import static com.madgag.interval.BeforeOrAfter.BEFORE

import ru.ipccenter.plagiarism.solutions.impl.*

final MINIMUM_MATCH_LENGTH = 8

final REPORTING = true
final MANUAL_CHECKS = true
final PLAGGIE_DETECTOR = "plaggie"

final int NUMBER_INTERVALS = 5
final int MAXIMUM_SIMILARITY_DEGREE = NUMBER_INTERVALS - 1
final SIMILARITY_INTERVALS =
    (0..MAXIMUM_SIMILARITY_DEGREE)
            .collect { center ->
                interval((center - 0.5) / MAXIMUM_SIMILARITY_DEGREE, (center + 0.5) / MAXIMUM_SIMILARITY_DEGREE)
            }

final NO_MEAN_VALUE_THRESHOLD = -1.0
final ZERO_MEAN_VALUE_THRESHOLD = 0.0

def dataDirectoryPath = args[0]
def work_directory = new File(dataDirectoryPath)
def test_data_directory = new File(work_directory, "test_data")
def manual_checks_directory = new File(work_directory, "manual_checks")
def results_directory = new File(work_directory, "results")
def comparison_results_directory = new File(results_directory, "comparison")

def taskRepository = new TaskRepositoryFileImpl(dataDirectoryPath)
def tasks = taskRepository.findAll()

def solutionRepository = new SolutionRepositoryFSImpl(dataDirectoryPath)

if (results_directory.exists()) FileUtils.cleanDirectory(results_directory)

Map<Task, List<SolutionsPair>> task_solution_pairs
if (MANUAL_CHECKS)
{
    final ManualChecksSolutionsPairRepository repository = new ManualChecksSolutionsPairRepository(solutionRepository, manual_checks_directory, dataDirectoryPath, MAXIMUM_SIMILARITY_DEGREE)
    task_solution_pairs = repository.loadSolutionsPairs(tasks)
}
else
{
    final AllSolutionsPairRepository repository = new AllSolutionsPairRepository(solutionRepository, test_data_directory)
    task_solution_pairs = repository.loadSolutionsPairs(tasks)
}

def detector = new PlaggieDetector(MINIMUM_MATCH_LENGTH)

task_solution_pairs.each { task, solution_pairs ->
    println "Processing ${task}"

    def task_results_directory = new File(comparison_results_directory, task.name)
    task_results_directory.mkdirs()
    solution_pairs.each{ pair ->
        def detectionResult = detector.performDetection(pair)
        pair.addDetectionResult(PLAGGIE_DETECTOR, detectionResult)

        if (REPORTING)
        {
            def analysis_results = new File(task_results_directory,
                    pair.solution1.author.name + "_" + pair.solution2.author.name + ".txt")

            analysis_results.withOutputStream { out ->
                out << detectionResult.report
            }
        }
    }
}

def false_positive_pairs_lists = []

task_solution_pairs.each { task, solution_pairs ->
    def similarities = solution_pairs.collect { it.detectionResults[PLAGGIE_DETECTOR].similarity }
    def overall_stats = new DescriptiveStatistics(similarities as double[])
    println "### $task (${solution_pairs.size()}, ${format(overall_stats.getMean())}±${format(overall_stats.getStandardDeviation())})"
    def intervals_breakdown = [:]
    SIMILARITY_INTERVALS.each { intervals_breakdown[it] = 0 }
    similarities.each { similarity ->
        SIMILARITY_INTERVALS.each { interval ->
            if (interval.contains(new BigDecimal(similarity))) {
                intervals_breakdown[interval] = intervals_breakdown[interval] + 1
            }
        }
    }
    println intervals_breakdown

    if (MANUAL_CHECKS)
    {
        def correctly_detected_pairs = solution_pairs.findAll { pair ->
            SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.detectionResults[PLAGGIE_DETECTOR].similarity)) } ==
            SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.estimatedSimilarity)) }
        }

        def false_negative_pairs = solution_pairs.findAll { pair ->
            SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.detectionResults[PLAGGIE_DETECTOR].similarity)) }
                .is(BEFORE,
                    SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.estimatedSimilarity)) }
                )
        }

        def false_positive_pairs = solution_pairs.findAll { pair ->
            SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.detectionResults[PLAGGIE_DETECTOR].similarity)) }
                .is(AFTER,
                    SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.estimatedSimilarity)) }
                )
        }

        printPairsInfo(correctly_detected_pairs, "Correctly detected pairs", PLAGGIE_DETECTOR)
        printPairsInfo(false_negative_pairs, "False negative pairs", PLAGGIE_DETECTOR)
        printPairsInfo(false_positive_pairs, "False positive pairs", PLAGGIE_DETECTOR)

//        generateTokenFrequencyHistogram(task, "correctly_detected", correctly_detected_pairs, results_directory)
//        generateTokenFrequencyHistogram(task, "false_negatives", false_negative_pairs, results_directory)
//        generateTokenFrequencyHistogram(task, "false_positives", false_positive_pairs, results_directory)
        generateAggregateTokenFrequencyHistogram(
                task.name,
                ["correctly_detected", "false_positives"],
                [correctly_detected_pairs, false_positive_pairs],
                results_directory,
                ZERO_MEAN_VALUE_THRESHOLD
        )

        false_positive_pairs_lists.add(false_positive_pairs)
    }
}

if (MANUAL_CHECKS)
{
    generateAggregateTokenFrequencyHistogram(
            "false_positives",
            task_solution_pairs.collect { task, _ -> "${task.name}_false_positives" },
            false_positive_pairs_lists,
            results_directory,
            ZERO_MEAN_VALUE_THRESHOLD
    )
}

private void printPairsInfo(ArrayList<SolutionsPair> solutionsPairs, String infoString, String detector)
{
    println "# ${infoString} (${solutionsPairs.size()}):"
    def stats = new DescriptiveStatistics(solutionsPairs.collect { it.detectionResults[detector].similarity } as double[])
    println "${format(stats.getMean())}±${format(stats.getStandardDeviation())}"
    solutionsPairs.each { pair ->
        println "${pair.solution1.author} ${pair.solution2.author} ${format(pair.estimatedSimilarity)} ${format(pair.detectionResults[detector].similarity)}"
    }
}
// methods

Map<String, StatisticalSummary> calculateTokenStats(List<SolutionsPair> solutionsPairs)
{
    return solutionsPairs
            .collectEntries(new MultiValueMap()) { solutionsPair ->
                solutionsPair.detectionResults["plaggie"].tokenFrequencies
            }
            .collectEntries() { token, frequencies ->
                [token, new DescriptiveStatistics(frequencies as double[])]
            }
}

private generateTokenFrequencyHistogram(
        Task task, String baseName, List<SolutionsPair> solutionsPairs, File output_directory)
{
    def histogram_name = "${task}_${baseName}_histogram"
    def data_file = new File(output_directory, histogram_name + ".txt")
    data_file.withWriter { out ->
        out.println "name ${task.name} ${task.name}"
        calculateTokenStats(solutionsPairs).sort().each { token, stats ->
            out.println token + " " + stats.getMean() + " " + stats.getStandardDeviation()
        }
    }
    def gnuplot_script = new File(output_directory, histogram_name + ".gnuplot")
    gnuplot_script.withWriter { out ->
        out << generateGnuplotScript(histogram_name, 1)
    }
}

private generateAggregateTokenFrequencyHistogram(
        String histogramName, List<String> baseNames, List<List<SolutionsPair>> solutionsPairsSet, File output_directory,
        double meanValueThreshold)
{
    def histogram_name = "${histogramName}_aggregate_histogram"

    def data_file = new File(output_directory, histogram_name + ".txt")
    data_file.withWriter { out ->
        out.println "name " +
                baseNames.collect { baseName -> "${baseName} ${baseName}" }.join(" ")

        solutionsPairsSet
            .collect { solutionsPairs ->
                calculateTokenStats(solutionsPairs)
            }
            .collectEntries(new MultiValueMap()) { token_stats ->
                token_stats
            }
            .findAll { token, stats_list ->
                stats_list.find { stats -> stats.getMean() > meanValueThreshold } != null
            }
            .sort()
            .each { token, stats_list ->
                out.println token + " " + stats_list.collect { it.getMean() + " " + it.getStandardDeviation() }.join(" ")
            }
    }

    def gnuplot_script = new File(output_directory, histogram_name + ".gnuplot")
    gnuplot_script.withWriter { out ->
        out << generateGnuplotScript(histogram_name, solutionsPairsSet.size())
    }
}

def generateGnuplotScript(def filename, def datasets_count)
{
    return """\
set terminal postscript eps color "Sans" 8 solid
set output "${filename}.eps"
set style histogram errorbars
set style data histograms
set xtic rotate by -90 scale 0
plot \
""" + (1..datasets_count).collect { i -> "\"${filename}.txt\" using ${i*2}:${i*2 + 1}:xtic(1) title col" }.join(", ")
}

def format(double v)
{
    String.format('%.2f', v)
}