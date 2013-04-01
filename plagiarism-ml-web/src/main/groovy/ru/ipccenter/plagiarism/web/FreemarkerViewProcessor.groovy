package ru.ipccenter.plagiarism.web

import com.sun.jersey.api.container.ContainerException
import com.sun.jersey.api.view.Viewable
import com.sun.jersey.spi.template.ViewProcessor
import freemarker.ext.beans.BeansWrapper
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateException
import freemarker.template.TemplateExceptionHandler

import javax.servlet.ServletContext
import javax.ws.rs.core.Context
import javax.ws.rs.core.UriInfo
import javax.ws.rs.ext.Provider

@Provider
class FreemarkerViewProcessor implements ViewProcessor<String>
{
    private UriInfo uriInfo;
    private ServletContext servletContext;
    //
    private final Configuration configuration;


    public FreemarkerViewProcessor()
    {
        configuration = new Configuration();
        configuration.setObjectWrapper(new BeansWrapper());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        configuration.setClassForTemplateLoading(this.getClass(), "/");
    }


    @Override
    public String resolve(String path)
    {
        if (uriInfo.getMatchedResources().get(0).getClass().getResource(path) != null) {
            return path;
        }

        return null;
    }


    @Override
    public void writeTo(String resolvedPath, Viewable viewable, OutputStream out) throws IOException
    {
        out.flush();

        try {

            final Template template = configuration.getTemplate(resolvedPath);

            Map<String, Object> model = new HashMap<String, Object>();
            model.put("model", viewable.getModel());
            model.put("uriInfo", uriInfo);
            model.put("servletContext", servletContext);

            template.process(model, new OutputStreamWriter(out));

        } catch (TemplateException te) {
            throw new ContainerException(te);
        }
    }

    @Context
    void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext
    }

    @Context
    void setUriInfo(UriInfo uriInfo)
    {
        this.uriInfo = uriInfo
    }
}
