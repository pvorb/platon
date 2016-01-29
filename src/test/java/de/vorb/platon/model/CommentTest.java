package de.vorb.platon.model;

import com.google.common.truth.Truth;
import org.junit.Test;

public class CommentTest {

    @Test
    public void testSetParentId() throws Exception {

        final Comment comment = new Comment();
        final long parentId = 42;

        comment.setParentId(parentId);

        Truth.assertThat(comment.getParentId()).isEqualTo(parentId);

        comment.setParentId(null);

        Truth.assertThat(comment.getParent()).isNull();

    }
}
