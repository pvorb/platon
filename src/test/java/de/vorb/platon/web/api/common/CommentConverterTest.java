/*
 * Copyright 2016-2017 the original author or authors.
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

package de.vorb.platon.web.api.common;

import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.web.api.json.CommentJson;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentConverterTest {

    private final CommentConverter commentConverter = new CommentConverter();

    @Test
    public void convertsRegularRecordToEquivalentJson() throws Exception {

        final CommentsRecord record = createCommentRecord(CommentStatus.PUBLIC);

        final CommentJson json = commentConverter.convertRecordToJson(record);

        assertThatMetaFieldsMatchRecord(json, record);
        assertThatContentFieldsMatchRecord(json, record);
    }

    @Test
    public void convertsDeletedRecordToJsonWithoutContent() throws Exception {

        final CommentsRecord record = createCommentRecord(CommentStatus.DELETED);

        final CommentJson json = commentConverter.convertRecordToJson(record);

        assertThatMetaFieldsMatchRecord(json, record);
        assertThatContentFieldsAreNull(json);
        assertThat(json.getReplies()).isEmpty();
    }

    private void assertThatMetaFieldsMatchRecord(CommentJson json, CommentsRecord record) {
        assertThat(json.getId()).isEqualTo(record.getId());
        assertThat(json.getParentId()).isEqualTo(record.getParentId());
        assertThat(json.getCreationDate()).isEqualTo(record.getCreationDate().toInstant());
        assertThat(json.getLastModificationDate()).isEqualTo(record.getLastModificationDate().toInstant());
        assertThat(json.getStatus()).isEqualTo(CommentStatus.valueOf(record.getStatus()));
    }

    private void assertThatContentFieldsMatchRecord(CommentJson json, CommentsRecord record) {
        assertThat(json.getText()).isEqualTo(record.getText());
        assertThat(json.getAuthor()).isEqualTo(record.getAuthor());
        assertThat(json.getEmailHash()).isEqualTo(Base64.getDecoder().decode(record.getEmailHash()));
        assertThat(json.getUrl()).isEqualTo(record.getUrl());
        assertThat(json.getReplies()).isEmpty();
    }

    private void assertThatContentFieldsAreNull(CommentJson json) {
        assertThat(json.getText()).isNull();
        assertThat(json.getAuthor()).isNull();
        assertThat(json.getEmailHash()).isNull();
        assertThat(json.getUrl()).isNull();
    }

    private CommentsRecord createCommentRecord(CommentStatus status) {
        return new CommentsRecord()
                .setId(15L)
                .setParentId(13L)
                .setCreationDate(Timestamp.from(Instant.now().minusSeconds(53)))
                .setLastModificationDate(Timestamp.from(Instant.now()))
                .setStatus(status.toString())
                .setText("Some text")
                .setAuthor("Jane Doe")
                .setEmailHash("DBe/ZuZJBwFncB0tPNcXEQ==")
                .setUrl("https://example.org/~jane.html");
    }

}
