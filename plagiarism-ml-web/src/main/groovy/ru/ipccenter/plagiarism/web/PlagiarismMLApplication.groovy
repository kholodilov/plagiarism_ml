package ru.ipccenter.plagiarism.web

import ru.ipccenter.plagiarism.solutions.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.solutions.impl.TaskRepositoryFileImpl

import javax.ws.rs.core.Application

/**
 *
 * @author dmitry
 */
class PlagiarismMLApplication extends Application
{

    private static final String DATA_DIRECTORY_PATH = System.getProperty("dataDirectory")

    @Override
    Set<Object> getSingletons()
    {
        Set<Object> singletons = new HashSet<Object>()

        singletons.add(new FreemarkerViewProcessor())
        singletons.add(new ComparisonService(
                        new ComparisonHelper(
                            new TaskRepositoryFileImpl(DATA_DIRECTORY_PATH),
                            new SolutionRepositoryFSImpl(DATA_DIRECTORY_PATH))
                        ))

        return singletons
    }
}
