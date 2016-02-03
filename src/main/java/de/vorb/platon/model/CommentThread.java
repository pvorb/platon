package de.vorb.platon.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "THREADS")
@NamedEntityGraph(name = "CommentThread.detail", attributeNodes = @NamedAttributeNode("comments"))
public class CommentThread {

    private static final int LIMIT_URL = 256;
    private static final int LIMIT_TITLE = 512;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "threads_seq")
    @SequenceGenerator(name = "threads_seq", sequenceName = "SEQ_THREADS")
    private Long id;

    @Column(name = "URL", length = LIMIT_URL)
    private String url;

    @Column(name = "TITLE", length = LIMIT_TITLE)
    private String title;

    @OneToMany(mappedBy = "thread", fetch = FetchType.LAZY)
    private List<Comment> comments;


    protected CommentThread() {}


    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = StringUtils.left(url, LIMIT_URL);
    }

    public String getTitle() {
        return title;
    }

    protected void setTitle(String title) {
        this.title = StringUtils.left(title, LIMIT_TITLE);
    }

    public List<Comment> getComments() {
        return this.comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentThread that = (CommentThread) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(url, that.url) &&
                Objects.equals(title, that.title);
    }

    public boolean equalsById(CommentThread commentThread) {
        if (this == commentThread) return true;
        if (commentThread == null || commentThread.id == null || id == null) return false;
        return Objects.equals(id, commentThread.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, title);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("url", url)
                .toString();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final CommentThread commentThread = new CommentThread();

        private Builder() {
            commentThread.comments = Lists.newArrayList();
        }

        public Builder id(Long id) {
            commentThread.setId(id);
            return this;
        }

        public Builder url(String url) {
            commentThread.setUrl(url);
            return this;
        }

        public Builder title(String title) {
            commentThread.setTitle(title);
            return this;
        }

        public CommentThread build() {
            return commentThread;
        }

    }

}
