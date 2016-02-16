package de.vorb.platon.web.rest;

import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;

@RunWith(MockitoJUnitRunner.class)
public class PoweredByResponseFilterTest {

    @Mock
    private ContainerRequestContext requestContext;

    @Mock
    private ContainerResponseContext responseContext;

    @Before
    public void setUp() throws Exception {
        Mockito.when(responseContext.getHeaders()).thenReturn(new MultivaluedHashMap<>());
    }

    @Test
    public void testPoweredByHeader() throws Exception {

        new PoweredByResponseFilter().filter(requestContext, responseContext);

        Truth.assertThat(responseContext.getHeaders()).containsKey("X-Powered-By");

    }
}
