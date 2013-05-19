package ru.ipccenter.plagiarism.web

import com.sun.jersey.api.view.Viewable

import javax.ws.rs.*
import javax.ws.rs.core.Response

import static javax.ws.rs.core.Response.Status.BAD_REQUEST

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
    @Path("plain/{task}")
    public Viewable plainComparisonPage(
            @PathParam("task") String task,
            @QueryParam("authors") String authors_pair,
            @QueryParam("author1") String author1,
            @QueryParam("author2") String author2)
    {
        String[] authors = getAuthors(author1, author2, authors_pair)

        def comparisonResult = comparisonHelper.simpleComparison(task, authors[0], authors[1])

        return makeComparisonPage("plain", comparisonResult)
    }

    @GET
    @Path("plaggie/{task}")
    public Viewable plaggieComparisonPage(
            @PathParam("task") String task,
            @QueryParam("authors") String authors_pair,
            @QueryParam("author1") String author1,
            @QueryParam("author2") String author2,
            @QueryParam("minMatch") @DefaultValue("8") int minimumMatchLength)
    {
        String[] authors = getAuthors(author1, author2, authors_pair)

        def comparisonResult = comparisonHelper.plaggieComparison(task, authors[0], authors[1], minimumMatchLength)

        return makeComparisonPage("plaggie", comparisonResult, ["minMatch": minimumMatchLength])
    }

    private static String[] getAuthors(String author1, String author2, String authors_pair)
    {
        if (authors_pair != null)
        {
            def authors = authors_pair.split(" ")
            if (authors.length == 2)
            {
                return authors
            }
            else
            {
                throw new WebApplicationException(
                        Response.status(BAD_REQUEST)
                                .entity("Wrong format of 'authors' parameter: " + authors_pair)
                                .build())
            }
        }
        else
        {
            return [author1, author2]
        }
    }

    private static Viewable makeComparisonPage(String method, ComparisonResult result,
                                               Map<String, Object> customParameters = [:])
    {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("comparisonMethod", method);
        model.put("comparisonResult", result);
        model.put("authors", "$result.pair.solution1.author.name $result.pair.solution2.author.name");
        customParameters.each { key, value -> model.put(key, value) }
        return new Viewable("/web/comparison/comparison.ftl", model);
    }

}
