package ru.ipccenter.plagiarism.solutions

/**
 *
 * @author dmitry
 */
class TaskNotFoundException extends RuntimeException
{
    TaskNotFoundException(String msg)
    {
        super(msg)
    }
}
