package ru.ipccenter.plagiarism.solutions.impl

import ru.ipccenter.plagiarism.solutions.Task
import ru.ipccenter.plagiarism.solutions.TaskNotFoundException
import ru.ipccenter.plagiarism.solutions.TaskRepository

/**
 *
 * @author dmitry
 */
class TaskRepositoryFileImpl implements TaskRepository
{
    private final String dataDirectoryPath

    TaskRepositoryFileImpl(String dataDirectoryPath)
    {
        this.dataDirectoryPath = dataDirectoryPath
    }

    @Override
    Task find(String name)
    {
        def task = findAll().find { it.name == name }
        if (task == null)
        {
            throw new TaskNotFoundException("Task $name not found")
        }
        return task
    }

    @Override
    List<Task> find(List<String> names)
    {
        def tasks = findAll().findAll { it.name in names }
        return tasks
    }

    @Override
    List<Task> findAll()
    {
        def tasks = []
        new File(dataDirectoryPath, "tasks.txt").eachLine { line ->
            if (! line.startsWith("#"))
            {
                Task task = parseTaskLine(line)
                tasks.add(task)
            }
        }
        return tasks
    }

    private Task parseTaskLine(String line)
    {
        def parts = line.split(" ")
        if (parts.length < 2)
        {
            throw new TasksFileFormatException("Unable to parse line: " + line)
        }
        def task = new Task(parts[0], parts[1])
        task
    }
}
