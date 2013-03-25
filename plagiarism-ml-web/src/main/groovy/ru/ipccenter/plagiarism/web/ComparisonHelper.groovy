package ru.ipccenter.plagiarism.web

/**
 *
 * @author kholodilov
 */
class ComparisonHelper

{
    private leftSource
    private rightSource
    private info
    private static final TASKS = [
            "array1" : "Array3dImpl.java",
            "collections2" : "WordCounterImpl.java",
            "reflection0" : "ReflectionsImpl.java"
    ]

    private final test_data_directory = new File(System.getProperty("workDirectory") + "/test_data")
    private final task_solutions = [:].withDefault {[]}
    private final random = new Random()

    ComparisonHelper()
    {
        random.setSeed(125678976)

        TASKS.each { task_name, task_file_name ->
            test_data_directory.eachDir { student_dir ->
                def solution_file = new File(student_dir, "ru/ipccenter/deadline1/" + task_name + "/" + task_file_name)
                if (solution_file.exists())
                {
                    task_solutions[task_name].add(new TaskSolution(student_dir.name, solution_file))
                }
            }
        }
    }

    def getLeftSource() {
        if (leftSource == null) reload();
        return leftSource
    }

    def getRightSource() {
        if (rightSource == null) reload();
        return rightSource
    }

    def getInfo()
    {
        if (info == null) reload();
        return info
    }

    public void reload(String task, String author1, String author2)
    {
        if (!task_solutions.containsKey(task))
        {
            renderUnknownTask(task)
            return
        }
        def solution1
        def solution2
        try {
            solution1 = getSolutionForAuthorOrRandomSolution(task, author1)
            solution2 = getSolutionForAuthorOrRandomSolution(task, author2)
        } catch (MissingSolutionException e) {
            renderMissingSolution(e.task, e.author)
            return
        }
        info = solution1.author + " " + solution2.author
        leftSource = solution1.file.text
        rightSource = solution2.file.text
    }

    private getSolutionForAuthorOrRandomSolution(String task, String author) {
        def solution
        if (author != null) {
            solution = task_solutions[task].find { it.author == author }
            if (solution == null) {
                throw new MissingSolutionException(task, author)
            }
        } else {
            solution = getRandomSolution(task)
        }
        return solution
    }

    private getRandomSolution(String task) {
        final solutions = task_solutions[task]
        return solutions[random.nextInt(solutions.size())]
    }

    private renderUnknownTask(String task) {
        info = "Unknown task: " + task
        leftSource = ""
        rightSource = ""
    }

    private renderMissingSolution(String task, String author) {
        info = "Cannot find solution of task '$task' for author '$author'"
        leftSource = ""
        rightSource = ""
    }

    private static class TaskSolution
    {
        def author
        def file

        public TaskSolution(author, file)
        {
            this.author = author
            this.file = file
        }
    }
    private static class MissingSolutionException extends RuntimeException
    {
        def task
        def author

        public MissingSolutionException(task, author) {
            this.task = task
            this.author = author
        }
    }
}
