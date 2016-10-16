package de.vorb.platon.model;

import com.google.common.truth.Truth;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.function.Supplier;

public class CommentTest {

    @Test
    public void testSetParentId() throws Exception {

        final Comment comment = new Comment();
        final long parentId = 42;

        comment.setParentId(parentId);

        Truth.assertThat(comment.getParentId()).isEqualTo(parentId);

        comment.setParentId(null);

        Truth.assertThat(comment.getParentId()).isNull();

    }

    @Test
    public void testSetEmailHash() throws Exception {
        new Comment().setEmailHash(new byte[16]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmailHashWithInvalidLength() throws Exception {
        new Comment().setEmailHash(new byte[0]);
    }

    @Test
    public void testEquals() throws Exception {

        final Comment comment = new Comment();
        Truth.assertThat(comment).isNotEqualTo(null);
        Truth.assertThat(comment).isEqualTo(comment);
        Truth.assertThat(comment).isEqualTo(new Comment());

        Truth.assertThat(NEW_COMMENT.get()).isEqualTo(NEW_COMMENT.get());
    }

    @Test
    public void testHashCode() throws Exception {
        Truth.assertThat(NEW_COMMENT.get().hashCode()).isEqualTo(NEW_COMMENT.get().hashCode());
    }

    private static final Supplier<Comment> NEW_COMMENT = () -> {
        final Comment c = new Comment();
        c.setId(2L);
        c.setParentId(1L);
        c.setThread(new CommentThread());
        c.setText("Comment text");
        c.setAuthor("User");
        c.setUrl("http://example.com");
        return c;
    };

}
