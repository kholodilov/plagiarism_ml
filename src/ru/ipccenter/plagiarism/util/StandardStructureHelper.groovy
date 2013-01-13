package ru.ipccenter.plagiarism.util

import ru.ipccenter.plagiarism.Author
import ru.ipccenter.plagiarism.Solution
import ru.ipccenter.plagiarism.Task

/**
 *
 * @author kholodilov
 */
class StandardStructureHelper
{
    static Solution findSolutionInStandardStructure(File test_data_directory, Task task, Author author)
    {
        def solution_file = new File(test_data_directory,
                author.name + "/ru/ipccenter/deadline1/" + task.name + "/" + task.filename)
        if (!solution_file.exists())
        {
            throw new SolutionNotFoundException("Solution of task ${task} for author ${author} not found")
        }
        return new Solution(task, author, solution_file)
    }

}
