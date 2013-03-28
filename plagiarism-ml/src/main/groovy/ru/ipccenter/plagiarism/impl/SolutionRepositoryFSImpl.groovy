package ru.ipccenter.plagiarism.impl

import ru.ipccenter.plagiarism.model.Author
import ru.ipccenter.plagiarism.model.Solution
import ru.ipccenter.plagiarism.model.SolutionNotFoundException
import ru.ipccenter.plagiarism.model.SolutionRepository
import ru.ipccenter.plagiarism.model.Task

/**
 *
 * @author dmitry
 */
class SolutionRepositoryFSImpl implements SolutionRepository
{
    private final String dataDirectoryPath

    private final random = new Random()

    SolutionRepositoryFSImpl(String dataDirectoryPath)
    {
        this.dataDirectoryPath = dataDirectoryPath
    }

    @Override
    List<Solution> findAllSolutionsFor(Task task)
    {
        def solutions = []
        findAllAuthors().each { author ->
            try
            {
                solutions << findSolutionFor(task, author)
            }
            catch (SolutionNotFoundException e) {
                // skip missing solution
            }
        }
        return solutions
    }

    @Override
    Solution findRandomSolutionFor(Task task)
    {
        def solutions = findAllSolutionsFor(task)
        if (solutions.isEmpty())
        {
            throw new SolutionNotFoundException("Task ${task} doesn't have any solutions")
        }
        return solutions[random.nextInt(solutions.size())]
    }

    @Override
    Solution findSolutionFor(Task task, Author author)
    {
        def solution_file = new File(dataDirectoryPath,
                "test_data/" + author.name + "/ru/ipccenter/deadline1/" + task.name + "/" + task.filename)
        if (!solution_file.exists())
        {
            throw new SolutionNotFoundException("Solution of task ${task} for author ${author} not found")
        }
        return new Solution(task, author, solution_file)
    }

    private List<Author> findAllAuthors()
    {
        def authors = []
        new File(dataDirectoryPath, "test_data").eachDir { authorDir ->
            authors << new Author(authorDir.name)
        }
        return authors
    }
}
