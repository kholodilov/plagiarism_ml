package ru.ipccenter.plagiarism.model

/**
 *
 * @author dmitry
 */
public interface TaskRepository
{
    Task find(String name)
    List<Task> findAll()
}