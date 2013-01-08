@Grab(group='ru.ipccenter.plaggie', module='plaggie', version='1.0.1-SNAPSHOT')
import plag.parser.*;
import plag.parser.java.*
import plag.parser.report.*

final MINIMUM_MATCH_LENGTH = 8
final MINIMUM_SIMILARITY_VALUE = 0.0
final TASKS = [
                "array1" : "Array3dImpl.java",
                "collections2" : "WordCounterImpl.java",
                "reflection0" : "ReflectionsImpl.java"
              ]
final REPORTING = false

Stats.newCounter("files_to_parse");
Stats.newCounter("file_comparisons");
Debug.setEnabled(false)

def test_data_directory = new File("/Users/kholodilov/Temp/Masters/test_data")
def analysis_results_directory = new File("/Users/kholodilov/Temp/Masters/analysis/tasks/")

def task_average_similarities = [:]
def task_average_token_frequencies = [:]

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

    def token_frequencies_list = []
    def tokenizer = new JavaTokenizer()
    def checker = new SimpleSubmissionSimilarityChecker(new SimpleTokenSimilarityChecker(MINIMUM_MATCH_LENGTH), tokenizer)
    def task_results_directory = new File(analysis_results_directory, task_name)
    task_results_directory.mkdirs()
    double sum_similarities = 0.0
    int comparisons_count = 0
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
            def token_frequencies = token_counts.collectEntries { key, value -> [key, value / total_tokens]}
            token_frequencies_list.add(token_frequencies)

            sum_similarities += fileDetectionResult.similarityA
            comparisons_count++

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
    task_average_similarities[task_name] = sum_similarities / comparisons_count
    def token_frequencies_sums = [:]
    listAllTokens().each { token_frequencies_sums.put(it, 0.0) }
    listAllTokens().each { token ->
        token_frequencies_list.each { token_frequencies ->
            token_frequencies_sums.put(token, token_frequencies_sums[token] + token_frequencies[token])
        }
    }
    task_average_token_frequencies[task_name] =
        token_frequencies_sums.collectEntries { token, frequencies_sum -> [token , frequencies_sum / comparisons_count]}
}

private listAllTokens() {
    PlagSym.valueStrings
            .findAll { it != null }
}

println task_average_similarities
def average_token_frequencies_aggregate = [:]
task_average_token_frequencies.each { task_name, average_token_frequencies ->
    new File("/Users/kholodilov/Temp/Masters/analysis/" + task_name + "_histogram.txt").withWriter { out ->
        out.println "name " + task_name
        average_token_frequencies.each { token, average_frequency ->
            out.println token + " " + average_frequency
            average_token_frequencies_aggregate.get(token, []) << average_frequency
        }
    }
}
new File("/Users/kholodilov/Temp/Masters/analysis/aggregate_histogram.txt").withWriter { out ->
    out.println "name " + TASKS.keySet().join(" ")
    average_token_frequencies_aggregate.each { token, average_frequencies ->
        out.println token + " " + average_frequencies.join(" ")
    }
}