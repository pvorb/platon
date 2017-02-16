/*
 * Copyright 2016-2017 the original author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.truth.Truth;
import org.junit.Test;

public class ObjectMapperContextResolverTest {

    @Test
    public void testGetContext() throws Exception {

        final int writeDatesAsTimestamps = SerializationFeature.WRITE_DATES_AS_TIMESTAMPS.getMask();

        final ObjectMapper objectMapper = new ObjectMapperContextResolver().getContext(Object.class);
        final int serializationFeatures = objectMapper.getSerializationConfig().getSerializationFeatures();

        // assert that write dates as timestampts is NOT enabled
        Truth.assertThat(serializationFeatures & writeDatesAsTimestamps).isEqualTo(0);

    }

}
