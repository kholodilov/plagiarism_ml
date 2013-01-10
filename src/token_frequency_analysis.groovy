@Grab(group='ru.ipccenter.plaggie', module='plaggie', version='1.0.1-SNAPSHOT')
import plag.parser.*;
import plag.parser.java.*
import plag.parser.report.*
@Grab(group='org.apache.commons', module='commons-math3', version='3.1')
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
@Grab(group='com.madgag', module='util-intervals', version='1.33')
import static com.madgag.interval.SimpleInterval.interval

import ru.ipccenter.plagiarism.*

final MINIMUM_MATCH_LENGTH = 8
final MINIMUM_SIMILARITY_VALUE = 0.0
final TASKS = [
                new Task("array1", "Array3dImpl.java"),
                new Task("collections2", "WordCounterImpl.java"),
                new Task("reflection0", "ReflectionsImpl.java")
              ]
final REPORTING = true
final HISTOGRAMS = true

final NUMBER_INTERVALS = 5
final SIMILARITY_INTERVALS =
    (0..NUMBER_INTERVALS-1)
            .collect { center ->
                interval((center - 0.5) / (NUMBER_INTERVALS - 1), (center + 0.5) / (NUMBER_INTERVALS - 1))
            }

Stats.newCounter("files_to_parse");
Stats.newCounter("file_comparisons");
Debug.setEnabled(false)

def work_directory = new File(args[0])
def test_data_directory = new File(work_directory, "test_data")
def results_directory = new File(work_directory, "results")
def comparison_results_directory = new File(results_directory, "comparison")

def task_similarities = [:].withDefault { [] }
def task_token_stats = [:]

Map<Task, List<SolutionsPair>> task_solution_pairs = makeSolutionPairs(findAllSolutions(TASKS, test_data_directory))

TASKS.each { task ->
    println "Processing ${task.name}"

    List<SolutionsPair> solution_pairs = task_solution_pairs[task]
    def token_frequencies = [:].withDefault { [] }
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

        def total_tokens = fileDetectionResult.tokensA.size()
        token_counts.each { token, count -> token_frequencies[token] << count / total_tokens }

        task_similarities[task.name] << fileDetectionResult.similarityA

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
    task_token_stats[task.name] =
        token_frequencies.collectEntries { token, frequencies -> [token , new DescriptiveStatistics(frequencies as double[])]}
}

task_similarities.each { task_name, similarities ->
    def stats = new DescriptiveStatistics(similarities as double[])
    println "${task_name}: ${stats.getMean()}±${stats.getStandardDeviation()}"
    println "total similarities calculated: ${similarities.size()}"
    def intervals_breakdown = [:].withDefault {0}
    similarities.each { similarity ->
        SIMILARITY_INTERVALS.each { interval ->
            if (interval.contains(new BigDecimal(similarity))) {
                intervals_breakdown[interval] = intervals_breakdown[interval] + 1
            }
        }
    }
    println intervals_breakdown
}

if (HISTOGRAMS)
{
    def token_stats_aggregate = [:].withDefault { [] }
    task_token_stats.each { task_name, token_stats ->
        new File(results_directory, task_name + "_histogram.txt").withWriter { out ->
            out.println "name " + task_name
            token_stats.each { token, stats ->
                out.println token + " " + stats.getMean() + " " + stats.getStandardDeviation()
                token_stats_aggregate[token] << stats
            }
        }
    }
    new File(results_directory, "aggregate_histogram.txt").withWriter { out ->
        out.println "name " + TASKS.collect {task -> task.name + " " + task.name + "_error" }.join(" ")
        token_stats_aggregate.each { token, stats_list ->
            out.println token + " " + stats_list.collect { it.getMean() + " " + it.getStandardDeviation() }.join(" ")
        }
    }

    new File(results_directory, "aggregate_histogram.gnuplot").withWriter { out ->
        out << generateGnuplotScript("aggregate_histogram", TASKS.size())
    }
}

private generateGnuplotScript(def filename, def datasets_count)
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

private listAllTokens() {
    PlagSym.valueStrings
            .findAll { it != null }
}

private Map<Task, List<Solution>> findAllSolutions(ArrayList<Task> TASKS, File test_data_directory) {
    Map<Task, List<Solution>> task_solutions = [:].withDefault { [] }

    TASKS.each { task ->
        test_data_directory.eachDir { author_dir ->
            def solution_file = new File(author_dir, "ru/ipccenter/deadline1/" + task.name + "/" + task.filename)
            if (solution_file.exists()) {
                task_solutions[task].add(new Solution(task, new Author(author_dir.name), solution_file))
            }
        }
    }
    return task_solutions
}

private Map<Task, List<SolutionsPair>> makeSolutionPairs(Map<Task, List<Solution>> task_solutions)
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
