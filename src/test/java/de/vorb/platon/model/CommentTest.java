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
