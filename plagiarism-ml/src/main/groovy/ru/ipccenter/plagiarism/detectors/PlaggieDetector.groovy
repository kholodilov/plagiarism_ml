package ru.ipccenter.plagiarism.detectors

import plag.parser.CodeTokenizer
import plag.parser.Debug
import plag.parser.MatchedTile
import plag.parser.SimpleSubmissionSimilarityChecker
import plag.parser.SimpleTokenSimilarityChecker
import plag.parser.SingleFileSubmission
import plag.parser.Stats
import plag.parser.SubmissionDetectionResult
import plag.parser.SubmissionSimilarityChecker
import plag.parser.java.JavaTokenizer
import plag.parser.java.PlagSym
import plag.parser.report.SimpleTextReportGenerator
import ru.ipccenter.plagiarism.model.DetectionResult
import ru.ipccenter.plagiarism.model.SolutionsPair

/**
 *
 * @author kholodilov
 */
class PlaggieDetector implements Detector
{
    static {
        Stats.newCounter("files_to_parse");
        Stats.newCounter("file_comparisons");
        Debug.setEnabled(false)
    }

    private static final MINIMUM_SIMILARITY_VALUE = 0.0

    private final CodeTokenizer tokenizer
    private SubmissionSimilarityChecker checker

    PlaggieDetector(int minimumMatchLength)
    {
        tokenizer = new JavaTokenizer()
        checker = new SimpleSubmissionSimilarityChecker(new SimpleTokenSimilarityChecker(minimumMatchLength), tokenizer)
    }

    @Override
    DetectionResult performDetection(SolutionsPair pair)
    {
        def submission1 = new SingleFileSubmission(pair.solution1.file)
        def submission2 = new SingleFileSubmission(pair.solution2.file)

        def submissionDetectionResult = new SubmissionDetectionResult(submission1, submission2, checker, MINIMUM_SIMILARITY_VALUE)

        def token_counts = [:]
        listAllTokens().each { token_counts.put(it, 0) }

        def fileDetectionResult = submissionDetectionResult.getFileDetectionResults()[0]
        def tokens_in_matches = 0
        for (MatchedTile tile : fileDetectionResult.getMatches())
        {
            def tokens_in_match = tile.getTileA().getTokenList().getValueArray()[tile.getTileA().getStartTokenIndex()..tile.getTileA().getEndTokenIndex()].collect {tokenizer.getValueString(it)}
            for (String token : tokens_in_match)
            {
                token_counts.put(token, token_counts[token] + 1)
            }
            tokens_in_matches += tokens_in_match.size()
        }

        def total_tokens = fileDetectionResult.tokensA.size()
        def tokenFrequencies = token_counts.collectEntries { token, count -> [token, count / total_tokens] }

        def detectionResult = new PlaggieDetectionResult(fileDetectionResult.similarityA, tokenFrequencies)
        detectionResult.report = generateDetectionReport(fileDetectionResult)

        return detectionResult

    }

    private generateDetectionReport(fileDetectionResult)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        def repGen = new SimpleTextReportGenerator(new PrintStream(baos), true, tokenizer);
        repGen.generateReport(fileDetectionResult)
        return baos.toString()
    }

    private static List<String> listAllTokens()
    {
        PlagSym.valueStrings
                .findAll { it != null }
    }
}
