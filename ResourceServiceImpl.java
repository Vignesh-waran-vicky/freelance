package com.aca.aem.core.utils;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.HashMap;
import java.util.Map;

@Component(service = ResourceService.class)
public class ResourceServiceImpl implements ResourceService {

    @Reference ResourceResolverFactory resolverFactory;


    @Override
    public ResourceResolver getResourceResolver() {
        ResourceResolver resolver = null;
        Map<String,Object> param = getServiceParams();

        try {
            resolver = resolverFactory.getResourceResolver(param);
        } catch (LoginException e) {
            e.printStackTrace();
        }
        return resolver;
    }

    public Map<String,Object> getServiceParams(){
        Map<String,Object> param = new HashMap<>();
        param.put(ResourceResolverFactory.SUBSERVICE,"acaadmin");
        return param;
    }
}
