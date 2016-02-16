package de.vorb.platon.web.rest.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigInteger;

public class ByteArraySerializer extends JsonSerializer<byte[]> {

    @Override
    public void serialize(byte[] bytes, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        final BigInteger bi = new BigInteger(1, bytes);
        gen.writeString(String.format("%0" + (bytes.length << 1) + "x", bi));
    }

}
