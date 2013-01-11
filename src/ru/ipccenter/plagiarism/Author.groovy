package ru.ipccenter.plagiarism

/**
 * Created with IntelliJ IDEA.
 * User: dmitry
 * Date: 1/11/13
 * Time: 2:04 AM
 * To change this template use File | Settings | File Templates.
 */
class Author
{
    final String name

    Author(String name) {
        this.name = name
    }

    @Override
    public String toString() {
        return name;
    }
}
