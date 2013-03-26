package ru.ipccenter.plagiarism.impl

import ru.ipccenter.plagiarism.model.Task
import ru.ipccenter.plagiarism.model.TaskRepository

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
