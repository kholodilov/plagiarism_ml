package ru.ipccenter.plagiarism.solutions

/**
 * @author dmitry
 * @date 1/11/13
 */
class Task
{
    static final Task NULL_TASK

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

    @Override
    public String toString() {
        return name;
    }
}
