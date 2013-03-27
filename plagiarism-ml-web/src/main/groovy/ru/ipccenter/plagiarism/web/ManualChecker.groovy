package ru.ipccenter.plagiarism.web

import com.sun.jersey.api.view.Viewable
import ru.ipccenter.plagiarism.impl.SolutionRepositoryFSImpl
import ru.ipccenter.plagiarism.impl.TaskRepositoryFileImpl

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam

/**
 * @author dmitry
 */
@Path("/")
@Produces("text/html")
class ManualChecker
{

    def comparisonHelper

    ManualChecker(ComparisonHelper comparisonHelper)
    {
        this.comparisonHelper = comparisonHelper
    }

    @GET
    public Viewable manualCheckerPage(
            @QueryParam("task") String task,
            @QueryParam("author1") String author1,
            @QueryParam("author2") String author2)
    {
        comparisonHelper.reload(task, author1, author2)

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("comparisonHelper", comparisonHelper);
        return new Viewable("/web/manualchecker.ftl", model);
    }

}
