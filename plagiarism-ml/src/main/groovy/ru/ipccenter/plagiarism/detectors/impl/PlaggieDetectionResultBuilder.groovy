package ru.ipccenter.plagiarism.detectors.impl

import ru.ipccenter.plagiarism.solutions.SolutionsPair

/**
 *
 * @author dmitry
 */
class PlaggieDetectionResultBuilder
{
    private SolutionsPair solutionsPair
    private double similarity
    private String report
    private Map<String, Double> tokenFrequencies
    private List<Duplicate> duplicates = new ArrayList<Duplicate>()
    private int totalTokensCount

    static PlaggieDetectionResultBuilder aPlaggieDetectionResult()
    {
        return new PlaggieDetectionResultBuilder();
    }

    PlaggieDetectionResultBuilder forSolutionsPair(SolutionsPair solutionsPair)
    {
        this.solutionsPair = solutionsPair
        return this
    }

    PlaggieDetectionResultBuilder withSimilarity(double similarity)
    {
        this.similarity = similarity
        return this
    }

    PlaggieDetectionResultBuilder withTokenFrequencies(Map<String, Double> tokenFrequencies)
    {
        this.tokenFrequencies = tokenFrequencies
        return this
    }

    PlaggieDetectionResultBuilder withReport(String report)
    {
        this.report = report
        return this
    }

    PlaggieDetectionResultBuilder withDuplicate(Duplicate duplicate)
    {
        this.duplicates << duplicate
        return this
    }

    PlaggieDetectionResultBuilder withTotalTokensCount(int totalTokensCount)
    {
        this.totalTokensCount = totalTokensCount
        return this
    }

    PlaggieDetectionResult build()
    {
        return new PlaggieDetectionResult(solutionsPair, similarity, report,
                                          tokenFrequencies, duplicates, totalTokensCount)
    }
}
