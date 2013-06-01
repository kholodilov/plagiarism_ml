package ru.ipccenter.plagiarism.detectors.impl

import org.apache.commons.lang3.StringUtils


/**
 *
 * @author dmitry
 */
class TokenSequence implements CharSequence
{
    static TokenSequence EMPTY = new TokenSequence(new int[0])

    private final int[] tokens

    TokenSequence(int[] tokens)
    {
        this.tokens = tokens
    }

    boolean isSubsequenceOf(TokenSequence otherSequence)
    {
        def sizeDelta = otherSequence.tokens.size() - this.tokens.size()
        for (int shift = 0; shift <= sizeDelta; shift++)
        {
            int[] subsequence = otherSequence.tokens[shift..shift + this.tokens.size() - 1]
            if (this.tokens.equals(subsequence))
            {
                return true
            }
        }
        return false
    }

    boolean isDiffOnlyFuzzySubsequenceOf(TokenSequence otherSequence, int maxDifferentTokens, int maxSizeDelta)
    {
        def sizeDelta = otherSequence.size() - this.size()
        if (sizeDelta < 0 || sizeDelta > maxSizeDelta)
        {
            return false
        }

        for (int shift = 0; shift <= sizeDelta; shift++)
        {
            int[] subsequence = otherSequence.tokens[shift..shift + this.tokens.size() - 1]
            int differentTokensCount = getDifferentTokensCount(this.tokens, subsequence)
            if (differentTokensCount + sizeDelta <= maxDifferentTokens)
            {
                return true
            }
        }
        return false
    }

    boolean isDiffOnlyFuzzySubsequenceWithoutSizeLimitOf(TokenSequence otherSequence, int maxDifferentTokens)
    {
        def sizeDelta = otherSequence.size() - this.size()
        for (int shift = 0; shift <= sizeDelta; shift++)
        {
            int[] subsequence = otherSequence.tokens[shift..shift + this.tokens.size() - 1]
            int differentTokensCount = getDifferentTokensCount(this.tokens, subsequence)
            if (differentTokensCount <= maxDifferentTokens)
            {
                return true
            }
        }
        return false
    }

    boolean isDiffOnlyFuzzySubsequenceOf(TokenSequence otherSequence, int maxDifferentTokens)
    {
        isDiffOnlyFuzzySubsequenceOf(otherSequence, maxDifferentTokens, maxDifferentTokens)
    }

    boolean isLevenshteinDistanceNotGreaterThan(TokenSequence otherSequence, int maxLevenshteinDistance)
    {
        StringUtils.getLevenshteinDistance(this, otherSequence, maxLevenshteinDistance) >= 0
    }

/*
    boolean isSubsequenceWithHolesOf(TokenSequence otherSequence, int maxHolesCount, int maxSizeDelta)
    {
        def sizeDelta = otherSequence.size() - this.size()
        if (sizeDelta < 0 || sizeDelta > maxSizeDelta)
        {
            return false
        }

        for (int shift = 0; shift < sizeDelta; shift++)
        {
            int otherSequencePos = shift
            int thisPos = 0
            while (this.tokens[thisPos] == otherSequence.tokens[otherSequencePos])
            {
                otherSequencePos++
                thisPos++
                if (thisPos == this.size())
                {
                    return true
                }
            }
            int diffTokensCount = 0
            while (diffTokensCount <= maxHolesCount - sizeDelta)
            {

                if ()
                {

                }
            }
        }
        return false
    }
*/

    protected static int getDifferentTokensCount(int[] sequence1, int[] sequence2)
    {
        return [sequence1, sequence2].transpose().count { it[0] != it[1] }
    }

    int size()
    {
        tokens.size()
    }

    @Override
    int length()
    {
        return tokens.size()
    }

/*
    @Override
    String toString()
    {
        return "[" + tokens.join(", ") + "]";
    }
*/

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

    @Override
    char charAt(int index)
    {
        return tokens[index]
    }

    @Override
    CharSequence subSequence(int start, int end)
    {
        int[] subsequence = tokens[start..end - 1]
        return new TokenSequence(subsequence)
    }
}
