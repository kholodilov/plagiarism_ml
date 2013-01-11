package ru.ipccenter.plagiarism.util;

/**
 * @author kholodilov
 * @date 1/11/13
 */
public class SolutionNotFoundException extends RuntimeException
{
    public SolutionNotFoundException(String message)
    {
        super(message);
    }
}
