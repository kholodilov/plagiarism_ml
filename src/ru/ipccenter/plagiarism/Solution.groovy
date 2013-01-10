package ru.ipccenter.plagiarism

/**
 * Created with IntelliJ IDEA.
 * User: dmitry
 * Date: 1/11/13
 * Time: 2:05 AM
 * To change this template use File | Settings | File Templates.
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
