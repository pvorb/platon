package de.vorb.platon.persistence;

import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;

@Component
@Converter(autoApply = true)
public class Jsr310InstantTimestampConverter implements AttributeConverter<Instant, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(Instant attribute) {
        return attribute == null ? null : Timestamp.from(attribute);
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp dbData) {
        return dbData == null ? null : dbData.toInstant();
    }

}
