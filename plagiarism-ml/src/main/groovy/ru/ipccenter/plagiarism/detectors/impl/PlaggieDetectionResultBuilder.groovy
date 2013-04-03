package ru.ipccenter.plagiarism.detectors.impl

/**
 *
 * @author dmitry
 */
class PlaggieDetectionResultBuilder
{
    private double similarity
    private String report
    private Map<String, Double> tokenFrequencies
    private List<Duplicate> duplicates = new ArrayList<Duplicate>()

    static PlaggieDetectionResultBuilder aPlaggieDetectionResult()
    {
        return new PlaggieDetectionResultBuilder();
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

    PlaggieDetectionResult build()
    {
        return new PlaggieDetectionResult(similarity, report, tokenFrequencies, duplicates)
    }
}
