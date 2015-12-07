package de.vorb.platon.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Objects;


@Entity
@Table(name = "COMMENTS")
public class Comment {

    private static final int LIMIT_AUTHOR = 128;
    private static final int LIMIT_EMAIL = 128;
    private static final int LIMIT_URL = 256;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comments_seq")
    @SequenceGenerator(name = "comments_seq", sequenceName = "SEQ_COMMENTS")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "THREAD_ID", nullable = false)
    @JsonIgnore
    private CommentThread thread;

    @Column(name = "TEXT")
    @Lob
    private String text;

    @Column(name = "AUTHOR", length = LIMIT_AUTHOR)
    private String author;

    @Column(name = "EMAIL", length = LIMIT_EMAIL)
    private String email;

    @Column(name = "URL", length = LIMIT_URL)
    private String url;


    protected Comment() {
    }

    public Comment(CommentThread thread, String text, String author, String email, String url) {
        setThread(thread);
        setText(text);
        setAuthor(author);
        setEmail(email);
        setUrl(url);
    }


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
        this.author = StringUtils.left(author, LIMIT_AUTHOR);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
                Objects.equals(text, comment.text) &&
                Objects.equals(author, comment.author) &&
                Objects.equals(email, comment.email) &&
                Objects.equals(url, comment.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, thread, text, author, email, url);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("text", text)
                .add("author", author)
                .add("email", email)
                .add("url", url)
                .toString();
    }

}
