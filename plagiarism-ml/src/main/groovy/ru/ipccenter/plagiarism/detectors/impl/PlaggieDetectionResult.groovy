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

    PlaggieDetectionResult(double similarity)
    {
        super(similarity)
    }

    PlaggieDetectionResult(double similarity, Map<String, Double> tokenFrequencies, String report)
    {
        this(similarity)
        this.tokenFrequencies = tokenFrequencies
        this.report = report

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

    def getFirstSourceWithMatchedLines()
    {
        return firstSourceWithMatchedLines
    }

    def getSecondSourceWithMatchedLines()
    {
        return secondSourceWithMatchedLines
    }
}
