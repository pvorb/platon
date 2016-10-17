package de.vorb.platon.web.rest.json;

import java.util.List;

public class CommentListResultJson {

    private long totalCommentCount;
    private List<CommentJson> comments;

    public CommentListResultJson(long totalCommentCount, List<CommentJson> comments) {
        this.totalCommentCount = totalCommentCount;
        this.comments = comments;
    }

    public long getTotalCommentCount() {
        return totalCommentCount;
    }

    public List<CommentJson> getComments() {
        return comments;
    }
}
