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
                "array1" : "Array3dImpl.java",
                "collections2" : "WordCounterImpl.java",
                "reflection0" : "ReflectionsImpl.java"
              ]
final REPORTING = false
final HISTOGRAMS = false

final NUMBER_INTERVALS = 7
final SIMILARITY_INTERVALS =
    (0..NUMBER_INTERVALS-1)
            .collect { center ->
                interval((center - 0.5) / (NUMBER_INTERVALS - 1), (center + 0.5) / (NUMBER_INTERVALS - 1))
            }

Stats.newCounter("files_to_parse");
Stats.newCounter("file_comparisons");
Debug.setEnabled(false)

def test_data_directory = new File("/Users/kholodilov/Temp/Masters/test_data")
def analysis_results_directory = new File("/Users/kholodilov/Temp/Masters/analysis/tasks/")

def task_similarities = [:].withDefault { [] }
def task_token_stats = [:]

TASKS.each { task_name, task_file_name ->
    println "Processing ${task_name}"
    def task_files = []
    def task_authors = []
    test_data_directory.eachDir { student_dir ->
        def task_file = new File(student_dir, "ru/ipccenter/deadline1/" + task_name + "/" + task_file_name)
        if (task_file.exists())
        {
            task_files.add(task_file)
            task_authors.add(student_dir.name)
        }
    }

    def token_frequencies = [:].withDefault { [] }
    def tokenizer = new JavaTokenizer()
    def checker = new SimpleSubmissionSimilarityChecker(new SimpleTokenSimilarityChecker(MINIMUM_MATCH_LENGTH), tokenizer)
    def task_results_directory = new File(analysis_results_directory, task_name)
    task_results_directory.mkdirs()
    for (int i = 0; i < task_files.size(); i++)
    {
        for (int j = i + 1; j < task_files.size(); j++)
        {
            def submission1 = new SingleFileSubmission(task_files[i])
            def submission2 = new SingleFileSubmission(task_files[j])

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

            task_similarities[task_name] << fileDetectionResult.similarityA

            if (REPORTING)
            {
                def analysis_results =
                    new File(task_results_directory, task_authors[i] + "_" + task_authors[j] + ".txt")

                analysis_results.withOutputStream { out ->
                    def repGen = new SimpleTextReportGenerator(new PrintStream(out), true, tokenizer);
                    repGen.generateReport(fileDetectionResult)
                }
                println task_name + " " + task_authors[i] + " " + task_authors[j] + " " + fileDetectionResult.similarityA
            }
        }
    }
    task_token_stats[task_name] =
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
        new File("/Users/kholodilov/Temp/Masters/analysis/" + task_name + "_histogram.txt").withWriter { out ->
            out.println "name " + task_name
            token_stats.each { token, stats ->
                out.println token + " " + stats.getMean() + " " + stats.getStandardDeviation()
                token_stats_aggregate[token] << stats
            }
        }
    }
    new File("/Users/kholodilov/Temp/Masters/analysis/aggregate_histogram.txt").withWriter { out ->
        out.println "name " + TASKS.keySet().collect { it + " " + it + "_error" }.join(" ")
        token_stats_aggregate.each { token, stats_list ->
            out.println token + " " + stats_list.collect { it.getMean() + " " + it.getStandardDeviation() }.join(" ")
        }
    }

    new File("/Users/kholodilov/Temp/Masters/analysis/aggregate_histogram.gnuplot").withWriter { out ->
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