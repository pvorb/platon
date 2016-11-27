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

package de.vorb.platon.web.rest.json;

import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.model.CommentStatus;

import com.google.common.truth.Truth;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CommentJsonTest {

    private CommentJson commentJson = new CommentJson();
    private CommentJson sameCommentJson = new CommentJson();

    @Before
    public void setUp() throws Exception {
        commentJson.setText("...");
        sameCommentJson.setText(commentJson.getText());

        commentJson.setAuthor("John Doe");
        sameCommentJson.setAuthor(commentJson.getAuthor());
    }

    @Test
    public void createFromDeletedRecord() throws Exception {
        final CommentsRecord deletedRecord = new CommentsRecord()
                .setText("Comment text")
                .setAuthor("John Doe")
                .setEmailHash("fake hash")
                .setUrl("http://example.org/")
                .setStatus(CommentStatus.DELETED.toString());

        final CommentJson commentJson = new CommentJson(deletedRecord);

        Truth.assertThat(commentJson.getText()).isNull();
        Truth.assertThat(commentJson.getAuthor()).isNull();
        Truth.assertThat(commentJson.getEmailHash()).isNull();
        Truth.assertThat(commentJson.getUrl()).isNull();
    }

    @Test
    public void setEmailNull() throws Exception {
        commentJson.setEmail(null);
        Truth.assertThat(commentJson.getEmailHash()).isEqualTo(null);
    }

    @Test
    public void calculateEmailHash() throws Exception {

        final String email = "test@example.org";

        commentJson.setEmail(email);

        Truth.assertThat(commentJson.getEmailHash())
                .isEqualTo(MessageDigest.getInstance(MessageDigestAlgorithms.MD5)
                        .digest(email.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void equals() throws Exception {
        Truth.assertThat(commentJson).isEqualTo(sameCommentJson);
    }

    @Test
    public void equalsSameInstance() throws Exception {
        Truth.assertThat(commentJson.equals(commentJson)).isTrue();
    }

    @Test
    public void notEqualToNull() throws Exception {
        Truth.assertThat(commentJson).isNotEqualTo(null);
    }

    @Test
    public void sameHashCode() throws Exception {
        Truth.assertThat(commentJson.hashCode()).isEqualTo(sameCommentJson.hashCode());
    }
}
