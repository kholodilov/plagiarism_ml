@Grab(group='ru.ipccenter.plaggie', module='plaggie', version='1.0.1-SNAPSHOT')
import plag.parser.*;
import plag.parser.java.*
import plag.parser.report.*
@Grab(group='org.apache.commons', module='commons-math3', version='3.1')
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
@Grab(group='com.madgag', module='util-intervals', version='1.33')
import static com.madgag.interval.SimpleInterval.interval

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

TASKS.each { task ->
    println "Processing ${task.name}"

    List<TaskSolution> task_solutions = []
    test_data_directory.eachDir { author_dir ->
        def solution_file = new File(author_dir, "ru/ipccenter/deadline1/" + task.name + "/" + task.filename)
        if (solution_file.exists())
        {
            task_solutions.add(new TaskSolution(task, new Author(author_dir.name), solution_file))
        }
    }

    def token_frequencies = [:].withDefault { [] }
    def tokenizer = new JavaTokenizer()
    def checker = new SimpleSubmissionSimilarityChecker(new SimpleTokenSimilarityChecker(MINIMUM_MATCH_LENGTH), tokenizer)
    def task_results_directory = new File(comparison_results_directory, task.name)
    task_results_directory.mkdirs()
    for (int i = 0; i < task_solutions.size(); i++)
    {
        for (int j = i + 1; j < task_solutions.size(); j++)
        {
            def submission1 = new SingleFileSubmission(task_solutions[i].file)
            def submission2 = new SingleFileSubmission(task_solutions[j].file)

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
                        task_solutions[i].author.name + "_" + task_solutions[j].author.name + ".txt")

                analysis_results.withOutputStream { out ->
                    def repGen = new SimpleTextReportGenerator(new PrintStream(out), true, tokenizer);
                    repGen.generateReport(fileDetectionResult)
                }
                println task.name + " " + task_solutions[i].author.name + "_" + task_solutions[j].author.name +
                        " " + fileDetectionResult.similarityA
            }
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

class Author
{
    final String name

    Author(String name) {
        this.name = name
    }
}

class Task
{
    final String name
    final String filename

    Task(String name, String filename) {
        this.name = name
        this.filename = filename
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Task task = (Task) o

        if (name != task.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}

class TaskSolution
{
    final Task task
    final Author author
    final File file

    TaskSolution(Task task, Author author, File file) {
        this.task = task
        this.author = author
        this.file = file
    }
}