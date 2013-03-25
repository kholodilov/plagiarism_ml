package ru.ipccenter.plagiarism.web

import com.sun.jersey.api.view.Viewable

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType;

/**
 * @author dmitry
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
class ManualChecker
{

    def comparisonHelper = new ComparisonHelper()

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
