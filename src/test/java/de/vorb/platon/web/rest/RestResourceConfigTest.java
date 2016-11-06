/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                CommentCountResource.class,
                LoggingFilter.class,
                ObjectMapperContextResolver.class);

    }
}
