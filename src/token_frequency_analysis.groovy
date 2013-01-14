@Grab(group='ru.ipccenter.plaggie', module='plaggie', version='1.0.1-SNAPSHOT')
@Grab(group='org.apache.commons', module='commons-math3', version='3.1')
import org.apache.commons.math3.stat.descriptive.StatisticalSummary
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
@Grab(group='commons-io', module='commons-io', version='2.4')
import org.apache.commons.io.FileUtils
@Grab(group='commons-collections', module='commons-collections', version='3.2.1')
import  org.apache.commons.collections.map.MultiValueMap
@Grab(group='com.madgag', module='util-intervals', version='1.33')
import static com.madgag.interval.SimpleInterval.interval
import static com.madgag.interval.BeforeOrAfter.AFTER
import static com.madgag.interval.BeforeOrAfter.BEFORE

import ru.ipccenter.plagiarism.*
import ru.ipccenter.plagiarism.util.*
import ru.ipccenter.plagiarism.detectors.*

final MINIMUM_MATCH_LENGTH = 8
final TASKS = [
                new Task("array1", "Array3dImpl.java"),
                new Task("collections2", "WordCounterImpl.java"),
                new Task("reflection0", "ReflectionsImpl.java")
              ]

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

def work_directory = new File(args[0])
def test_data_directory = new File(work_directory, "test_data")
def manual_checks_directory = new File(work_directory, "manual_checks")
def results_directory = new File(work_directory, "results")
def comparison_results_directory = new File(results_directory, "comparison")

if (results_directory.exists()) FileUtils.cleanDirectory(results_directory)

Map<Task, List<SolutionsPair>> task_solution_pairs
if (MANUAL_CHECKS)
{
    task_solution_pairs =
        new ManualChecksSolutionsPairsLoader(TASKS, manual_checks_directory, test_data_directory, MAXIMUM_SIMILARITY_DEGREE)
            .loadSolutionsPairs()
}
else
{
   task_solution_pairs = new AllSolutionsPairsLoader(TASKS, test_data_directory)
                            .loadSolutionsPairs()
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
    println "${task.name}: ${overall_stats.getMean()}±${overall_stats.getStandardDeviation()}"
    println "total pairs compared: ${solution_pairs.size()}"
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

        def pairs_with_lower_detected_similarity = solution_pairs.findAll { pair ->
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

        println "# Correctly detected pairs (${correctly_detected_pairs.size()}):"
        correctly_detected_pairs.each { pair ->
            println "${pair.solution1.author} ${pair.solution2.author} \
                        ${pair.estimatedSimilarity} ${pair.detectionResults[PLAGGIE_DETECTOR].similarity}"
        }


        println "# Pairs with lower detected similarity (${pairs_with_lower_detected_similarity.size()}):"
        pairs_with_lower_detected_similarity.each { pair ->
            println "${pair.solution1.author} ${pair.solution2.author} \
                        ${pair.estimatedSimilarity} ${pair.detectionResults[PLAGGIE_DETECTOR].similarity}"
        }


        println "# Pairs with higher detected similarity (${false_positive_pairs.size()}):"
        false_positive_pairs.each { pair ->
            println "${pair.solution1.author} ${pair.solution2.author} \
                        ${pair.estimatedSimilarity} ${pair.detectionResults[PLAGGIE_DETECTOR].similarity}"
        }

//        generateTokenFrequencyHistogram(task, "correctly_detected", correctly_detected_pairs, results_directory)
//        generateTokenFrequencyHistogram(task, "lower_similarity", pairs_with_lower_detected_similarity, results_directory)
//        generateTokenFrequencyHistogram(task, "higher_similarity", false_positive_pairs, results_directory)
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
