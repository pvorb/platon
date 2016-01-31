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
