package ru.ipccenter.plagiarism.detectors.impl

/**
 *
 * @author dmitry
 */
class TokenSequence implements Iterable<String>
{
    static TokenSequence EMPTY = new TokenSequence([])

    private final List<String> tokens

    TokenSequence(List<String> tokens)
    {
        this.tokens = tokens
    }

    boolean isSubsequenceOf(TokenSequence otherSequence)
    {
        def sizeDelta = otherSequence.tokens.size() - this.tokens.size()
        for (int shift = 0; shift <= sizeDelta; shift++)
        {
            def subsequence = otherSequence.tokens.subList(shift, shift + this.tokens.size())
            if (this.tokens.equals(subsequence))
            {
                return true
            }
        }
        return false
    }

    boolean isFuzzySubsequenceOf(TokenSequence otherSequence, int maxDifferentTokens, int maxSizeDelta)
    {
        def sizeDelta = otherSequence.size() - this.size()
        if (sizeDelta < 0 || sizeDelta > maxSizeDelta)
        {
            return false
        }

        for (int shift = 0; shift <= sizeDelta; shift++)
        {
            def subsequence = otherSequence.tokens.subList(shift, shift + this.tokens.size())
            int differentTokensCount = getDifferentTokensCount(this.tokens, subsequence)
            if (differentTokensCount + sizeDelta <= maxDifferentTokens)
            {
                return true
            }
        }
        return false
    }

    boolean isFuzzySubsequenceWithoutSizeLimitOf(TokenSequence otherSequence, int maxDifferentTokens)
    {
        def sizeDelta = otherSequence.size() - this.size()
        for (int shift = 0; shift <= sizeDelta; shift++)
        {
            def subsequence = otherSequence.tokens.subList(shift, shift + this.tokens.size())
            int differentTokensCount = getDifferentTokensCount(this.tokens, subsequence)
            if (differentTokensCount <= maxDifferentTokens)
            {
                return true
            }
        }
        return false
    }

    boolean isFuzzySubsequenceOf(TokenSequence otherSequence, int maxDifferentTokens)
    {
        isFuzzySubsequenceOf(otherSequence, maxDifferentTokens, maxDifferentTokens)
    }

    protected static int getDifferentTokensCount(List sequence1, List sequence2)
    {
        return [sequence1, sequence2].transpose().count { it[0] != it[1] }
    }

    @Override
    Iterator<String> iterator()
    {
        return tokens.iterator()
    }

    int size()
    {
        tokens.size()
    }

    @Override
    String toString()
    {
        return "[" + tokens.join(", ") + "]";
    }

    @Override
    boolean equals(other)
    {
        if (this.is(other)) return true
        if (getClass() != other.class) return false

        if (tokens != ((TokenSequence) other).tokens) return false

        return true
    }

    @Override
    int hashCode()
    {
        return tokens.hashCode()
    }
}
