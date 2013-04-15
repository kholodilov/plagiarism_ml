package ru.ipccenter.plagiarism.detectors.impl

import ru.ipccenter.plagiarism.detectors.DetectionResult

/**
 *
 * @author kholodilov
 */
class PlaggieDetectionResult extends DetectionResult
{

    private Map<String, Double> tokenFrequencies
    private String firstSourceWithMatchedLines
    private String secondSourceWithMatchedLines
    private final List<Duplicate> duplicates
    private totalTokensCount

    PlaggieDetectionResult(double similarity, String report,
                           Map<String, Double> tokenFrequencies,
                           List<Duplicate> duplicates, def totalTokensCount)
    {
        super(similarity)

        this.report = report
        this.tokenFrequencies = tokenFrequencies
        this.duplicates = duplicates
        this.totalTokensCount = totalTokensCount

        def reportLines = report.readLines()
        def firstSourceHeaderLine = reportLines.findIndexOf { it == "File A with matched lines:" }
        def secondSourceHeaderLine = reportLines.findIndexOf { it == "File B with matched lines:" }

        firstSourceWithMatchedLines =
            reportLines.subList(firstSourceHeaderLine + 1, secondSourceHeaderLine - 1).join("\n")
        secondSourceWithMatchedLines =
            reportLines.subList(secondSourceHeaderLine + 1, reportLines.size()).join("\n")
    }

    Map<String, Double> getTokenFrequencies()
    {
        return tokenFrequencies
    }

    List<Duplicate> getDuplicates()
    {
        return duplicates
    }

    int getTotalTokensCount()
    {
        return totalTokensCount
    }

    String getFirstSourceWithMatchedLines()
    {
        return firstSourceWithMatchedLines
    }

    String getSecondSourceWithMatchedLines()
    {
        return secondSourceWithMatchedLines
    }
}
