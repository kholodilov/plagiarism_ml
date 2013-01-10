package ru.ipccenter.plagiarism

/**
 * Created with IntelliJ IDEA.
 * User: dmitry
 * Date: 1/11/13
 * Time: 2:05 AM
 * To change this template use File | Settings | File Templates.
 */
class Task
{
    final String name
    final String filename

    Task(String name, String filename) {
        this.name = name
        this.filename = filename
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Task task = (Task) o

        if (name != task.name) return false

        return true
    }

    int hashCode() {
        return name.hashCode()
    }
}
