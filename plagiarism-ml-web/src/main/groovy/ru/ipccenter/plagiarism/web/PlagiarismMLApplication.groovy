package ru.ipccenter.plagiarism.web

import javax.ws.rs.core.Application

/**
 *
 * @author dmitry
 */
class PlagiarismMLApplication extends Application
{

    @Override
    Set<Object> getSingletons()
    {
        Set<Object> singletons = new HashSet<Object>()
        singletons.add(new FreemarkerViewProcessor())
        singletons.add(new ManualChecker())
        return singletons
    }
}
