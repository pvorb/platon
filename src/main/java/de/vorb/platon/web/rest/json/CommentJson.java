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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class CommentJson {

    private static final Logger logger = LoggerFactory.getLogger(CommentJson.class);

    private Long id;
    private Long parentId;

    private Instant creationDate;
    private Instant lastModificationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CommentStatus status = CommentStatus.PUBLIC;

    private String text;

    private String author;

    @JsonSerialize(using = ByteArraySerializer.class)
    @JsonDeserialize(using = ByteArrayDeserializer.class)
    private byte[] emailHash;

    private String url;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<CommentJson> replies;

    public CommentJson() {}

    public CommentJson(CommentsRecord comment) {
        id = comment.getId();
        parentId = comment.getParentId();

        creationDate = comment.getCreationDate() == null
                ? null
                : comment.getCreationDate().toInstant();
        lastModificationDate = comment.getLastModificationDate() == null
                ? null
                : comment.getLastModificationDate().toInstant();

        status = comment.getStatus() == null
                ? null
                : Enum.valueOf(CommentStatus.class, comment.getStatus());
        if (status != CommentStatus.DELETED) {
            text = comment.getText();
            author = comment.getAuthor();
            if (comment.getEmailHash() != null) {
                emailHash = Base64.getDecoder().decode(comment.getEmailHash());
            }
            url = comment.getUrl();
        }
        replies = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(Instant lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void setStatus(CommentStatus status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public byte[] getEmailHash() {
        return emailHash;
    }

    public void setEmailHash(byte[] emailHash) {
        this.emailHash = emailHash;
    }

    protected void setEmail(String email) {
        if (email == null) {
            this.emailHash = null;
            return;
        }

        final byte[] stringBytes = email.getBytes(StandardCharsets.UTF_8);
        try {
            emailHash = MessageDigest.getInstance(MessageDigestAlgorithms.MD5).digest(stringBytes);
        } catch (NoSuchAlgorithmException e) {
            logger.error("No implementation of MessageDigest algorithm MD5 available on this platform");
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<CommentJson> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentJson> replies) {
        this.replies = replies;
    }

    public CommentsRecord toRecord() {
        return new CommentsRecord()
                .setId(id)
                .setParentId(parentId)
                .setCreationDate(creationDate == null ? null : Timestamp.from(creationDate))
                .setLastModificationDate(lastModificationDate == null ? null : Timestamp.from(lastModificationDate))
                .setStatus(status == null ? null : status.toString())
                .setText(text)
                .setAuthor(author)
                .setEmailHash(emailHash == null ? null : Base64.getEncoder().encodeToString(emailHash))
                .setUrl(url);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CommentJson that = (CommentJson) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(parentId, that.parentId) &&
                status == that.status &&
                Objects.equals(text, that.text) &&
                Objects.equals(author, that.author) &&
                Arrays.equals(emailHash, that.emailHash) &&
                Objects.equals(url, that.url) &&
                Objects.equals(replies, that.replies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, parentId, status, text, author, emailHash, url, replies);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("parentId", parentId)
                .add("creationDate", creationDate)
                .add("lastModificationDate", lastModificationDate)
                .add("status", status)
                .add("text", text)
                .add("author", author)
                .add("emailHash", emailHash)
                .add("url", url)
                .add("replies", replies)
                .toString();
    }
}
