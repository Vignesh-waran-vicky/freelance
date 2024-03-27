package com.aca.aem.core.servlets;


import com.aca.aem.core.utils.ResourceService;
import com.adobe.cq.dam.cfm.*;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component(
        service= Servlet.class,
        property={
                Constants.SERVICE_DESCRIPTION + "=Solr",
                "sling.servlet.methods={ GET, POST}",
                "sling.servlet.paths=" + "/bin/formcf"
        }
)
public class FomCF extends SlingAllMethodsServlet {


    @Reference
    private ContentFragmentManager fragmentManager;

    @Reference
    ResourceService resourceService;

    @Override
    protected void doPost(final SlingHttpServletRequest req,
                          final SlingHttpServletResponse resp) throws IOException {

        String name = req.getParameter("name");
        String content = req.getParameter("content");
        String date = req.getParameter("date");


        try(ResourceResolver resourceResolver = req.getResourceResolver()) {

            Resource template = resourceResolver.getResource("/conf/aca-aem/settings/dam/cfm/models/article-form");
            Resource path = resourceResolver.getResource("/content/dam/aca-aem/article-form");

            ContentFragment cf = null;

            if(Objects.nonNull(template)){
                FragmentTemplate tpl = template.adaptTo(FragmentTemplate.class);
                if(Objects.nonNull(tpl)){
                    cf = tpl.createFragment(path, name+"cf", name);
                    Resource fragRes = cf.adaptTo(Resource.class);

                    if(Objects.nonNull(cf)){
                        ResourceResolver fragResolver = fragRes.getResourceResolver();
                        ContentElement articleName = cf.getElement("articleName");
                        articleName.setContent(name, "text/plain");

                        ContentElement articleContent = cf.getElement("articleContent");
                        articleContent.setContent(content, "text/plain");

                        ContentElement publishingDate = cf.getElement("publishingDate");
                        publishingDate.setContent(date,"text/plain");

                        fragResolver.commit();
                    }
                }
            }

        } catch (ContentFragmentException e) {
            throw new RuntimeException(e);
        }

        resp.setStatus(200);
        resp.getWriter().write("Content Fragment is created");
    }
}
