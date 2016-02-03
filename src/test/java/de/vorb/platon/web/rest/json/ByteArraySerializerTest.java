package de.vorb.platon.web.rest.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;

@RunWith(MockitoJUnitRunner.class)
public class ByteArraySerializerTest {

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private SerializerProvider serializerProvider;

    @Test
    public void testSerialize() throws Exception {

        final String sampleHash = "341be97d9aff90c9978347f66f945b77";
        final byte[] bytes = new BigInteger(sampleHash, 16).toByteArray();

        new ByteArraySerializer().serialize(bytes, jsonGenerator, serializerProvider);

        // verify that the byte array is correctly serialized
        Mockito.verify(jsonGenerator).writeString(Mockito.eq(sampleHash));

    }

}
