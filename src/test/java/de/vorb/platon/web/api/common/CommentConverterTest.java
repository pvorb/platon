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

import de.vorb.platon.jooq.tables.pojos.Comment;
import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.web.api.json.CommentJson;

import org.junit.Test;

import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentConverterTest {

    private final CommentConverter commentConverter = new CommentConverter();

    @Test
    public void convertsRegularPojoToEquivalentJson() {

        final Comment comment = prepareComment()
                .setStatus(CommentStatus.PUBLIC);

        final CommentJson json = commentConverter.convertPojoToJson(comment);

        assertThatMetaFieldsMatchPojo(json, comment);
        assertThatContentFieldsMatchPojo(json, comment);
    }

    @Test
    public void convertsMinimalPojoToEquivalentJson() {

        final Comment comment = prepareComment()
                .setCreationDate(null)
                .setLastModificationDate(null)
                .setStatus(null)
                .setAuthor(null)
                .setEmailHash(null)
                .setUrl(null);

        final CommentJson json = commentConverter.convertPojoToJson(comment);

        assertThatMetaFieldsMatchPojo(json, comment);
        assertThatContentFieldsMatchPojo(json, comment);
    }

    @Test
    public void convertsDeletedPojoToJsonWithoutContent() {

        final Comment comment = prepareComment()
                .setStatus(CommentStatus.DELETED);

        final CommentJson json = commentConverter.convertPojoToJson(comment);

        assertThatMetaFieldsMatchPojo(json, comment);
        assertThatContentFieldsAreNull(json);
        assertThat(json.getReplies()).isEmpty();
    }

    private void assertThatMetaFieldsMatchPojo(CommentJson json, Comment comment) {

        assertThat(json.getId()).isEqualTo(comment.getId());
        assertThat(json.getParentId()).isEqualTo(comment.getParentId());

        assertThat(json.getCreationDate()).isEqualTo(
                Optional.ofNullable(comment.getCreationDate())
                        .orElse(null));

        assertThat(json.getLastModificationDate()).isEqualTo(
                Optional.ofNullable(comment.getLastModificationDate())
                        .orElse(null));

        assertThat(json.getStatus()).isEqualTo(
                Optional.ofNullable(comment.getStatus())
                        .orElse(null));
    }

    private void assertThatContentFieldsMatchPojo(CommentJson json, Comment comment) {
        assertThat(json.getText()).isEqualTo(comment.getText());
        assertThat(json.getAuthor()).isEqualTo(comment.getAuthor());

        assertThat(json.getEmailHash())
                .isEqualTo(Optional.ofNullable(comment.getEmailHash())
                        .map(emailHash -> Base64.getDecoder().decode(emailHash))
                        .orElse(null));

        assertThat(json.getUrl()).isEqualTo(comment.getUrl());
        assertThat(json.getReplies()).isEmpty();
    }

    private void assertThatContentFieldsAreNull(CommentJson json) {
        assertThat(json.getText()).isNull();
        assertThat(json.getAuthor()).isNull();
        assertThat(json.getEmailHash()).isNull();
        assertThat(json.getUrl()).isNull();
    }

    private Comment prepareComment() {
        return new Comment()
                .setId(15L)
                .setParentId(13L)
                .setCreationDate(Instant.now().minusSeconds(53))
                .setLastModificationDate(Instant.now())
                .setText("Some text")
                .setAuthor("Jane Doe")
                .setEmailHash("DBe/ZuZJBwFncB0tPNcXEQ==")
                .setUrl("https://example.org/~jane.html");
    }

    @Test
    public void convertsJsonToEquivalentPojo() {

        final CommentJson json = prepareJson().build();

        final Comment comment = commentConverter.convertJsonToPojo(json);

        assertThat(comment.getId()).isEqualTo(json.getId());
        assertThat(comment.getParentId()).isEqualTo(json.getParentId());
        assertThat(comment.getCreationDate()).isEqualTo(json.getCreationDate());
        assertThat(comment.getLastModificationDate()).isEqualTo(json.getLastModificationDate());
        assertThat(comment.getStatus()).isEqualTo(json.getStatus());
        assertThat(comment.getText()).isEqualTo(json.getText());
        assertThat(comment.getAuthor()).isEqualTo(json.getAuthor());
        assertThat(comment.getEmailHash()).isEqualTo("18385ac57d9b171dc3fe83a5a92b68d9");
        assertThat(comment.getUrl()).isEqualTo(json.getUrl());
    }

    @Test
    public void convertsMinimalJsonToEquivalentPojo() {

        final CommentJson json = prepareJson()
                .parentId(null)
                .creationDate(null)
                .lastModificationDate(null)
                .status(null)
                .author(null)
                .email(null)
                .url(null)
                .build();

        final Comment comment = commentConverter.convertJsonToPojo(json);

        assertThat(comment.getId()).isEqualTo(json.getId());
        assertThat(comment.getParentId()).isNull();
        assertThat(comment.getCreationDate()).isNull();
        assertThat(comment.getLastModificationDate()).isNull();
        assertThat(comment.getStatus()).isNull();
        assertThat(comment.getText()).isEqualTo(json.getText());
        assertThat(comment.getAuthor()).isNull();
        assertThat(comment.getEmailHash()).isNull();
        assertThat(comment.getUrl()).isNull();
    }

    @Test
    public void acceptsJsonWithMissingEmailAddress() {

        final CommentJson json = prepareJson()
                .email(null)
                .build();

        final Comment comment = commentConverter.convertJsonToPojo(json);

        assertThat(comment.getEmailHash()).isNull();
    }

    private CommentJson.CommentJsonBuilder prepareJson() {
        return CommentJson.builder()
                .id(15L)
                .parentId(13L)
                .creationDate(Instant.now().minusSeconds(53))
                .lastModificationDate(Instant.now())
                .status(CommentStatus.PUBLIC)
                .text("Some text")
                .author("Jane Doe")
                .email("jane@example.org")
                .url("https://example.org/~jane.html");
    }

}
