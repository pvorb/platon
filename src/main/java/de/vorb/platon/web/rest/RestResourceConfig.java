package de.vorb.platon.web.rest;

import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/api")
public class RestResourceConfig extends ResourceConfig {

    public RestResourceConfig() {
        register(RequestContextFilter.class);
        register(PoweredByResponseFilter.class);
        register(CommentResource.class);
        register(LoggingFilter.class);
        register(ObjectMapperContextResolver.class);
    }

}
