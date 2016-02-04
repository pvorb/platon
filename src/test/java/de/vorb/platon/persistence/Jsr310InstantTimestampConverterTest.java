package de.vorb.platon.persistence;

import com.google.common.truth.Truth;
import org.junit.Test;

import javax.persistence.AttributeConverter;
import java.sql.Timestamp;
import java.time.Instant;

public class Jsr310InstantTimestampConverterTest {

    final Jsr310InstantTimestampConverter converter = new Jsr310InstantTimestampConverter();

    @Test
    public void testInstantTimestampConversion() throws Exception {

        final Instant instant = Instant.now();

        final Instant converted =
                converter.convertToEntityAttribute(
                        converter.convertToDatabaseColumn(instant));

        Truth.assertThat(converted).isEqualTo(instant);

    }

    @Test
    public void testConversionsWithNull() throws Exception {
        Truth.assertThat(converter.convertToDatabaseColumn(null)).isNull();
        Truth.assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
