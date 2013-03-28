package ru.ipccenter.plagiarism.web

import com.sun.jersey.api.view.Viewable

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam

/**
 * @author dmitry
 */
@Path("/compare")
@Produces("text/html")
class ComparisonService
{

    ComparisonHelper comparisonHelper

    ComparisonService(ComparisonHelper comparisonHelper)
    {
        this.comparisonHelper = comparisonHelper
    }

    @GET
    public Viewable simpleComparisonPage(
            @QueryParam("task") String task,
            @QueryParam("author1") String author1,
            @QueryParam("author2") String author2)
    {
        def comparisonResult = comparisonHelper.simpleComparison(task, author1, author2)

        return makeComparisonPage(comparisonResult)
    }

    @GET
    @Path("plaggie")
    public Viewable plaggieComparisonPage(
            @QueryParam("task") String task,
            @QueryParam("author1") String author1,
            @QueryParam("author2") String author2,
            @QueryParam("minMatch") int minimumMatchLength)
    {
        def comparisonResult = comparisonHelper.plaggieComparison(task, author1, author2, minimumMatchLength)

        return makeComparisonPage(comparisonResult)
    }

    private Viewable makeComparisonPage(ComparisonResult comparisonResult)
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("comparisonResult", comparisonResult);
        return new Viewable("/web/comparison.ftl", model);
    }

}
