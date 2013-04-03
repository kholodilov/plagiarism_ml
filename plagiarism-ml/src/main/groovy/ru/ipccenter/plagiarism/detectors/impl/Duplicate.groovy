package ru.ipccenter.plagiarism.detectors.impl

/**
 *
 * @author dmitry
 */
class Duplicate
{
    private final int index
    private final TokenSequence tokens

    Duplicate(int index, TokenSequence tokens)
    {
        this.index = index
        this.tokens = tokens
    }

    int getIndex()
    {
        return index
    }

    TokenSequence getTokens()
    {
        return tokens
    }

    @Override
    public String toString()
    {
        return "Duplicate{" +
                "index=" + index +
                ", tokens=" + tokens +
                '}';
    }
}
