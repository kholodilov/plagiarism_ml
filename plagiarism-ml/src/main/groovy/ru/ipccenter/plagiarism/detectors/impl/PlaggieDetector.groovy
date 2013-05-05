package ru.ipccenter.plagiarism.detectors.impl

import plag.parser.*
import plag.parser.java.JavaTokenizer
import plag.parser.java.PlagSym
import plag.parser.report.SimpleTextReportGenerator
import ru.ipccenter.plagiarism.detectors.Detector
import ru.ipccenter.plagiarism.solutions.SolutionsPair

import static ru.ipccenter.plagiarism.detectors.impl.PlaggieDetectionResultBuilder.aPlaggieDetectionResult

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
    PlaggieDetectionResult performDetection(SolutionsPair pair)
    {
        def submission1 = new SingleFileSubmission(pair.solution1.file)
        def submission2 = new SingleFileSubmission(pair.solution2.file)

        def submissionDetectionResult = new SubmissionDetectionResult(submission1, submission2, checker, MINIMUM_SIMILARITY_VALUE)

        DetectionResult plaggieResult = submissionDetectionResult.getFileDetectionResults()[0]
        def detectedSimilarity = plaggieResult.similarityA
        def totalTokensCount = plaggieResult.tokensA.size()

        def ourResult = aPlaggieDetectionResult()
                            .withSimilarity(detectedSimilarity)
                            .withTotalTokensCount(totalTokensCount)
                            .withReport(generateDetectionReport(plaggieResult));

        def tokenFrequenciesCalculator = new TokenFrequenciesCalculator(totalTokensCount)

        plaggieResult.matches.each { MatchedTile matchedTile ->
            def tokensInMatch = getTokens(matchedTile)
            tokenFrequenciesCalculator.addTokens(tokensInMatch)
            ourResult.withDuplicate(new Duplicate(matchedTile.id, new TokenSequence(tokensInMatch)))
        }

        return ourResult
                .withTokenFrequencies(tokenFrequenciesCalculator.calculate())
                .build()
    }

    private List<String> getTokens(MatchedTile tile)
    {
        int[] tokenCodes = tile.getTileA().getTokenList().getValueArray()
        def startIndex = tile.getTileA().getStartTokenIndex()
        def endIndex = tile.getTileA().getEndTokenIndex()
        return tokenCodes[startIndex..endIndex].collect { tokenizer.getValueString(it) }
    }

    private generateDetectionReport(fileDetectionResult)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        def repGen = new SimpleTextReportGenerator(new PrintStream(baos), true, tokenizer);
        repGen.generateReport(fileDetectionResult)
        return baos.toString()
    }

    private static class TokenFrequenciesCalculator
    {
        private final int totalTokensCount
        private final tokenCounts = [:]

        TokenFrequenciesCalculator(int totalTokensCount)
        {
            this.totalTokensCount = totalTokensCount
            listAllTokens().each { tokenCounts.put(it, 0) }
        }

        void addTokens(List<String> tokens)
        {
            tokens.each { token ->
                tokenCounts.put(token, tokenCounts[token] + 1)
            }
        }

        Map<String, Double> calculate()
        {
            return tokenCounts.collectEntries { token, count -> [token, count / totalTokensCount] }
        }

        private static List<String> listAllTokens()
        {
            PlagSym.valueStrings
                    .findAll { it != null }
        }
    }
}
