@Grab(group='ru.ipccenter.plaggie', module='plaggie', version='1.0.1-SNAPSHOT')
import plag.parser.*;
import plag.parser.java.*
import plag.parser.report.*
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

final MINIMUM_MATCH_LENGTH = 8
final MINIMUM_SIMILARITY_VALUE = 0.0
final TASKS = [
                new Task("array1", "Array3dImpl.java"),
                new Task("collections2", "WordCounterImpl.java"),
                new Task("reflection0", "ReflectionsImpl.java")
              ]

final REPORTING = false
final MANUAL_CHECKS = true

final int NUMBER_INTERVALS = 5
final SIMILARITY_INTERVALS =
    (0..NUMBER_INTERVALS-1)
            .collect { center ->
                interval((center - 0.5) / (NUMBER_INTERVALS - 1), (center + 0.5) / (NUMBER_INTERVALS - 1))
            }

final NO_MEAN_VALUE_THRESHOLD = -1.0
final ZERO_MEAN_VALUE_THRESHOLD = 0.0

Stats.newCounter("files_to_parse");
Stats.newCounter("file_comparisons");
Debug.setEnabled(false)

def work_directory = new File(args[0])
def test_data_directory = new File(work_directory, "test_data")
def manual_checks_directory = new File(work_directory, "manual_checks")
def results_directory = new File(work_directory, "results")
def comparison_results_directory = new File(results_directory, "comparison")

if (results_directory.exists()) FileUtils.cleanDirectory(results_directory)

Map<Task, List<SolutionsPair>> task_solution_pairs
if (MANUAL_CHECKS)
{
    task_solution_pairs = loadManualChecksPairs(TASKS, manual_checks_directory, test_data_directory, NUMBER_INTERVALS)
}
else
{
   task_solution_pairs = makeSolutionPairs(findAllSolutions(TASKS, test_data_directory))
}

task_solution_pairs.each { task, solution_pairs ->
    println "Processing ${task}"

    def tokenizer = new JavaTokenizer()
    def checker = new SimpleSubmissionSimilarityChecker(new SimpleTokenSimilarityChecker(MINIMUM_MATCH_LENGTH), tokenizer)
    def task_results_directory = new File(comparison_results_directory, task.name)
    task_results_directory.mkdirs()
    solution_pairs.each{ pair ->
        def submission1 = new SingleFileSubmission(pair.solution1.file)
        def submission2 = new SingleFileSubmission(pair.solution2.file)

        def detectionResult = new SubmissionDetectionResult(submission1, submission2, checker, MINIMUM_SIMILARITY_VALUE)

        def token_counts = [:]
        listAllTokens().each { token_counts.put(it, 0) }

        DetectionResult fileDetectionResult = detectionResult.getFileDetectionResults()[0]
        for (MatchedTile tile : fileDetectionResult.getMatches())
        {
            def tokens_in_match = tile.getTileA().getTokenList().getValueArray()[tile.getTileA().getStartTokenIndex()..tile.getTileA().getEndTokenIndex()].collect {tokenizer.getValueString(it)}
            for (String token : tokens_in_match)
            {
                token_counts.put(token, token_counts[token] + 1)
            }
        }

        pair.setDetectedSimilarity(fileDetectionResult.similarityA)
        def total_tokens = fileDetectionResult.tokensA.size()
        pair.setTokenFrequencies(token_counts.collectEntries { token, count -> [token, count / total_tokens] })

        if (REPORTING)
        {
            def analysis_results = new File(task_results_directory,
                    pair.solution1.author.name + "_" + pair.solution2.author.name + ".txt")

            analysis_results.withOutputStream { out ->
                def repGen = new SimpleTextReportGenerator(new PrintStream(out), true, tokenizer);
                repGen.generateReport(fileDetectionResult)
            }
            println task.name + " " + pair.solution1.author.name + "_" + pair.solution2.author.name +
                    " " + fileDetectionResult.similarityA
        }
    }
}

task_solution_pairs.each { task, solution_pairs ->
    def similarities = solution_pairs.collect { it.detectedSimilarity }
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
            SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.detectedSimilarity)) } ==
            SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.estimatedSimilarity)) }
        }

        def pairs_with_lower_detected_similarity = solution_pairs.findAll { pair ->
            SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.detectedSimilarity)) }
                .is(BEFORE,
                    SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.estimatedSimilarity)) }
                )
        }

        def pairs_with_higher_detected_similarity = solution_pairs.findAll { pair ->
            SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.detectedSimilarity)) }
                .is(AFTER,
                    SIMILARITY_INTERVALS.find { it.contains(new BigDecimal(pair.estimatedSimilarity)) }
                )
        }

        println "# Correctly detected pairs (${correctly_detected_pairs.size()}):"
        correctly_detected_pairs.each { pair ->
            println "${pair.solution1.author} ${pair.solution2.author} ${pair.estimatedSimilarity} ${pair.detectedSimilarity}"
        }


        println "# Pairs with lower detected similarity (${pairs_with_lower_detected_similarity.size()}):"
        pairs_with_lower_detected_similarity.each { pair ->
            println "${pair.solution1.author} ${pair.solution2.author} ${pair.estimatedSimilarity} ${pair.detectedSimilarity}"
        }


        println "# Pairs with higher detected similarity (${pairs_with_higher_detected_similarity.size()}):"
        pairs_with_higher_detected_similarity.each { pair ->
            println "${pair.solution1.author} ${pair.solution2.author} ${pair.estimatedSimilarity} ${pair.detectedSimilarity}"
        }

        generateTokenFrequencyHistogram(task, "correctly_detected", correctly_detected_pairs, results_directory)
        generateTokenFrequencyHistogram(task, "lower_similarity", pairs_with_lower_detected_similarity, results_directory)
        generateTokenFrequencyHistogram(task, "higher_similarity", pairs_with_higher_detected_similarity, results_directory)
        generateAggregateTokenFrequencyHistogramForTask(
                task,
                ["lower_similarity", "correctly_detected", "higher_similarity"],
                [pairs_with_lower_detected_similarity, correctly_detected_pairs, pairs_with_higher_detected_similarity],
                results_directory,
                ZERO_MEAN_VALUE_THRESHOLD
        )
    }
}

// methods

Map<String, StatisticalSummary> calculateTokenStats(List<SolutionsPair> solutionsPairs)
{
    return solutionsPairs
            .collectEntries(new MultiValueMap()) { solutionsPair ->
                solutionsPair.tokenFrequencies
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
        out.println "name ${task.name} ${task.name}_error"
        calculateTokenStats(solutionsPairs).sort().each { token, stats ->
            out.println token + " " + stats.getMean() + " " + stats.getStandardDeviation()
        }
    }
    def gnuplot_script = new File(output_directory, histogram_name + ".gnuplot")
    gnuplot_script.withWriter { out ->
        out << generateGnuplotScript(histogram_name, 1)
    }
}

private generateAggregateTokenFrequencyHistogramForTask(
        Task task, List<String> baseNames, List<List<SolutionsPair>> solutionsPairsSet, File output_directory,
        double meanValueThreshold)
{
    def histogram_name = "${task}_aggregate_histogram"

    def data_file = new File(output_directory, histogram_name + ".txt")
    data_file.withWriter { out ->
        out.println "name " +
                baseNames.collect { baseName -> "${task.name}_${baseName} ${task.name}_${baseName}_error" }.join(" ")

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

def listAllTokens() {
    PlagSym.valueStrings
            .findAll { it != null }
}

Map<Task, List<Solution>> findAllSolutions(ArrayList<Task> tasks, File test_data_directory) {
    Map<Task, List<Solution>> task_solutions = [:].withDefault { [] }

    tasks.each { task ->
        test_data_directory.eachDir { author_dir ->
            try {
                task_solutions[task].add(
                        findSolutionInStandardStructure(test_data_directory, task, new Author(author_dir.name))
                )
            } catch (SolutionNotFoundException e)
            {
                // skip missing solution
            }
        }
    }
    return task_solutions
}

Solution findSolutionInStandardStructure(File test_data_directory, Task task, Author author)
{
    def solution_file = new File(test_data_directory,
            author.name + "/ru/ipccenter/deadline1/" + task.name + "/" + task.filename)
    if (!solution_file.exists())
    {
        throw new SolutionNotFoundException("Solution of task ${task} for author ${author} not found")
    }
    return new Solution(task, author, solution_file)
}

Map<Task, List<SolutionsPair>> makeSolutionPairs(Map<Task, List<Solution>> task_solutions)
{
    Map<Task, List<SolutionsPair>> task_solution_pairs = [:].withDefault { [] }
    task_solutions.each { task, solutions ->
        for (int i = 0; i < solutions.size(); i++)
        {
            for (int j = i + 1; j < solutions.size(); j++)
            {
                task_solution_pairs[task] << new SolutionsPair(solutions[i], solutions[j])
            }
        }
    }
    return task_solution_pairs
}

Map<Task, List<SolutionsPair>> loadManualChecksPairs(
        List<Task> tasks, File manual_checks_directory, File test_data_directory, def numberOfIntervals) {
    Map<Task, List<SolutionsPair>> task_solution_pairs = [:]
    tasks.each { task ->
        def manual_checks_file = new File(manual_checks_directory, task.name + ".txt")
        List<SolutionsPair> solution_pairs = []
        if (manual_checks_file.exists())
        {
            manual_checks_file.eachLine { solutions_pair_line ->
                try {
                    solution_pairs.add(
                            loadSolutionsPair(test_data_directory, task, solutions_pair_line, numberOfIntervals)
                    )
                } catch (ManualCheckParseException e)
                {
                    println e.message
                } catch (SolutionNotFoundException e)
                {
                    println e.message
                }
            }
            task_solution_pairs[task] = solution_pairs
        }
    }
    return task_solution_pairs;
}

SolutionsPair loadSolutionsPair(File test_data_directory, Task task, String solutions_pair_line, int numberOfIntervals)
{
    def matcher = solutions_pair_line =~ /(\S+) (\S+) (\d+)/
    if (!matcher.matches())
    {
        throw new ManualCheckParseException("Failed to parse manual check line: " + solutions_pair_line);
    }

    def author1 = new Author(matcher.group(1))
    def author2 = new Author(matcher.group(2))
    def estimatedSimilarity = Integer.parseInt(matcher.group(3)) / (numberOfIntervals - 1)

    def solution1 = findSolutionInStandardStructure(test_data_directory, task, author1)
    def solution2 = findSolutionInStandardStructure(test_data_directory, task, author2)

    def solutions_pair = new SolutionsPair(solution1, solution2)
    solutions_pair.setEstimatedSimilarity(estimatedSimilarity);

    return solutions_pair;
}