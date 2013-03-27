package ru.ipccenter.plagiarism.model

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
