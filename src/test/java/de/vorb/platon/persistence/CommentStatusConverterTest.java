package de.vorb.platon.persistence;

import de.vorb.platon.model.Comment;

import com.google.common.truth.Truth;
import org.junit.Test;

public class CommentStatusConverterTest {

    private final CommentStatusConverter converter = new CommentStatusConverter();

    @Test
    public void testConvertToDatabaseColumn() throws Exception {
        for (Comment.Status status : Comment.Status.values()) {
            Truth.assertThat(converter.convertToDatabaseColumn(status)).isEqualTo(status.getValue());
        }
    }

    @Test
    public void testConvertToEntityAttribute() throws Exception {
        for (Comment.Status status : Comment.Status.values()) {
            Truth.assertThat(converter.convertToEntityAttribute(status.getValue())).isEqualTo(status);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testConvertNullToDatabaseColumn() {
        converter.convertToDatabaseColumn(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConvertNullToEntityAttribute() {
        converter.convertToEntityAttribute(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertUnknownByteToEntityAttribute() throws Exception {
        converter.convertToEntityAttribute(-1);
    }

}
