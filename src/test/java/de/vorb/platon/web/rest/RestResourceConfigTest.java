package de.vorb.platon.web.rest;

import com.google.common.truth.Truth;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.junit.Test;

import java.util.Set;

public class RestResourceConfigTest {

    @Test
    public void testConstructor() throws Exception {

        final RestResourceConfig restResourceConfig = new RestResourceConfig();

        final Set<Class<?>> registeredClasses = restResourceConfig.getClasses();

        // assert that the correct resources and filters are registered
        Truth.assertThat(registeredClasses).containsExactly(
                RequestContextFilter.class,
                PoweredByResponseFilter.class,
                CommentResource.class,
                LoggingFilter.class,
                ObjectMapperContextResolver.class);

    }
}
