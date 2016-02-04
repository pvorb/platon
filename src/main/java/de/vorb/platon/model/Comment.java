package de.vorb.platon.model;


import de.vorb.platon.web.rest.json.ByteArraySerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;


@Entity
@Table(name = "COMMENTS")
public class Comment {

    private static final Logger logger = LoggerFactory.getLogger(Comment.class);

    private static final int LIMIT_AUTHOR = 128;
    private static final int LIMIT_URL = 256;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comment_id_seq_gen")
    @SequenceGenerator(name = "comment_id_seq_gen", sequenceName = "COMMENT_ID_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "THREAD_ID", nullable = false)
    @JsonIgnore
    private CommentThread thread;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PARENT_ID", nullable = true)
    @JsonIgnore
    private Comment parent;

    @Column(name = "CREATION_DATE", nullable = false)
    @CreatedDate
    private Instant creationDate;

    @Column(name = "LAST_MODIFICATION_DATE", nullable = false)
    @LastModifiedDate
    private Instant lastModificationDate;

    @Column(name = "STATUS", nullable = false)
    private Status status = Status.AWAITING_MODERATION;

    @Column(name = "TEXT", nullable = false)
    @Lob
    private String text;

    @Column(name = "AUTHOR", length = LIMIT_AUTHOR, nullable = true)
    private String author;

    @Column(name = "EMAIL_HASH", columnDefinition = "BINARY(16) NULL")
    @JsonSerialize(using = ByteArraySerializer.class)
    private byte[] emailHash;

    @Column(name = "URL", length = LIMIT_URL, nullable = true)
    private String url;

    protected Comment() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CommentThread getThread() {
        return thread;
    }

    public void setThread(CommentThread thread) {
        this.thread = thread;
    }

    public Comment getParent() {
        return parent;
    }

    protected void setParent(Comment parent) {
        this.parent = parent;
    }

    public Long getParentId() {
        return parent != null ? parent.getId() : null;
    }

    protected void setParentId(Long parentId) {
        if (parentId != null) {
            parent = new Comment();
            parent.setId(parentId);
        } else if (parent != null) {
            parent = null;
        }
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

    public Status getStatus() {
        return status;
    }

    protected void setStatus(Status status) {
        Preconditions.checkNotNull(status);
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        Preconditions.checkNotNull(text);
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = StringUtils.left(author, LIMIT_AUTHOR);
    }

    public byte[] getEmailHash() {
        return emailHash;
    }

    protected void setEmailHash(byte[] emailHash) {
        Preconditions.checkArgument(emailHash.length == 16, "emailHash has invalid length");
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
        this.url = StringUtils.left(url, LIMIT_URL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) &&
                Objects.equals(thread, comment.thread) &&
                Objects.equals(parent, comment.parent) &&
                Objects.equals(status, comment.status) &&
                Objects.equals(text, comment.text) &&
                Objects.equals(author, comment.author) &&
                Arrays.equals(emailHash, comment.emailHash) &&
                Objects.equals(url, comment.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, thread, parent, text, author, url) * 31 + Arrays.hashCode(emailHash);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("text", text)
                .add("author", author)
                .add("emailHash", emailHash)
                .add("url", url)
                .toString();
    }

    public enum Status {
        DELETED(0),
        PUBLIC(1),
        AWAITING_MODERATION(2);

        private final int value;

        Status(int intValue) {
            value = (byte) intValue;
        }

        @JsonValue
        public int getValue() {
            return value;
        }

        public static Status fromValue(int value) {
            for (Status status : Status.values()) {
                if (status.value == value) {
                    return status;
                }
            }

            throw new IllegalArgumentException("Unknown status");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Comment comment = new Comment();

        private Builder() {}

        public Builder id(Long id) {
            comment.setId(id);
            return this;
        }

        public Builder thread(CommentThread thread) {
            comment.setThread(thread);
            return this;
        }

        public Builder parent(Comment parentComment) {
            comment.setParent(parentComment);
            return this;
        }

        public Builder parentId(Long parentId) {
            comment.setParentId(parentId);
            return this;
        }

        public Builder creationDate(Instant creationDate) {
            comment.setCreationDate(creationDate);
            return this;
        }

        public Builder modificationDate(Instant modificationDate) {
            comment.setLastModificationDate(modificationDate);
            return this;
        }

        public Builder text(String text) {
            comment.setText(text);
            return this;
        }

        public Builder author(String author) {
            comment.setAuthor(author);
            return this;
        }

        public Builder emailHash(byte[] emailHash) {
            comment.setEmailHash(emailHash);
            return this;
        }

        public Builder email(String email) {
            comment.setEmail(email);
            return this;
        }

        public Builder url(String url) {
            comment.setUrl(url);
            return this;
        }

        public Comment build() {
            return comment;
        }

    }

}
