package ru.ipccenter.plagiarism

/**
 * @author dmitry
 * @date 1/11/13
 */
class Solution
{
    final Task task
    final Author author
    final File file

    Solution(Task task, Author author, File file) {
        this.task = task
        this.author = author
        this.file = file
    }
}
