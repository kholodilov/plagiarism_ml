package ru.ipccenter.plagiarism.detectors.impl

/**
 *
 * @author dmitry
 */
class TokenSequence implements Iterable<String>
{
    private final List<String> tokens

    TokenSequence(List<String> tokens)
    {
        this.tokens = tokens
    }

    @Override
    Iterator<String> iterator()
    {
        return tokens.iterator()
    }

    @Override
    public String toString()
    {
        return "[" + tokens.join(", ") + "]";
    }
}
