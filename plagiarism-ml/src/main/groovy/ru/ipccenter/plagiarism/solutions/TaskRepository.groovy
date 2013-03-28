package ru.ipccenter.plagiarism.solutions

/**
 *
 * @author dmitry
 */
public interface TaskRepository
{
    Task find(String name)
    List<Task> findAll()
}