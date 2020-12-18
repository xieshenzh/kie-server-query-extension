package com.redhat.example.extension;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AuthenticatedQueryComponent implements KieServerApplicationComponentsService {

    private static final String OWNER_EXTENSION = "jBPM";

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
        if ( !OWNER_EXTENSION.equals(extension) ) {
            return Collections.emptyList();
        }
        List<Object> components = new ArrayList<>(1);

        KieServerRegistry context = null;

        for( Object object : services ) {
            if( KieServerRegistry.class.isAssignableFrom(object.getClass()) ) {
                context = (KieServerRegistry) object;
                continue;
            }
        }

        if( SupportedTransports.REST.equals(type) ) {
            components.add(new AuthenticatedQueryResource(context));
        }
        return components;
    }
}
