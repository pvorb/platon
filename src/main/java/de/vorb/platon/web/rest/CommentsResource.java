package de.vorb.platon.web.rest;

import de.vorb.platon.model.Comment;
import de.vorb.platon.model.CommentThread;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.CommentThreadRepository;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Component
@Path("comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommentsResource {

    private final CommentThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    @Inject
    public CommentsResource(CommentThreadRepository threadRepository, CommentRepository commentRepository) {
        this.threadRepository = threadRepository;
        this.commentRepository = commentRepository;
    }

    @GET
    public List<Comment> getCommentsByThreadUrl(@QueryParam("threadUrl") String threadUrl) {
        if (threadUrl == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        final CommentThread thread = threadRepository.getByUrl(threadUrl);
        if (thread == null) {
            return Collections.emptyList();
        } else {
            return thread.getComments();
        }
    }

    @GET
    @Path("init")
    public String createSampleComment() {
        CommentThread thread = new CommentThread("http://vorba.ch/2013/scala.html", "Scala");
        threadRepository.save(thread);

        Comment comment = new Comment(thread, "A text", "Author", "name@domain.com", "https://github.com/name");
        commentRepository.save(comment);

        comment = new Comment(thread, "A text", "Author", "name@domain.com", "https://github.com/name");
        commentRepository.save(comment);

        thread = new CommentThread("http://vorba.ch/2012/bread.html", "Bread");
        threadRepository.save(thread);

        comment = new Comment(thread, "A text", "Author", "name@domain.com", "https://github.com/name");
        commentRepository.save(comment);

        comment = new Comment(thread, "A text", "Author", "name@domain.com", "https://github.com/name");
        commentRepository.save(comment);

        return "ok";
    }

}
