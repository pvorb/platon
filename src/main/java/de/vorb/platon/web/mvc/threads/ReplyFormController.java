package de.vorb.platon.web.mvc.threads;

import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;
import de.vorb.platon.view.Base64UrlMethod;
import de.vorb.platon.web.mvc.comments.CommentAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReplyFormController {

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    @GetMapping(value = {"/threads/{threadId}/reply", "/threads/{threadId}/comments/{parentCommentId}/reply"},
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView showThreadReplyForm(
            @PathVariable(CommentController.PATH_VAR_THREAD_ID) long threadId,
            @PathVariable(value = "parentCommentId", required = false) Long parentCommentId) {

        final Map<String, Object> model = getModelForCommentForm(threadId, parentCommentId, null);

        return new ModelAndView("comment-form", model);
    }

    @PostMapping(value = {"/threads/{threadId}/reply", "/threads/{threadId}/comments/{parentCommentId}/reply"},
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView postComment(HttpServletRequest request, HttpServletResponse response,
            @PathVariable(CommentController.PATH_VAR_THREAD_ID) long threadId,
            @PathVariable(value = "parentCommentId", required = false) Long parentCommentId,
            @RequestBody MultiValueMap<String, String> formValues) {

        final CommentAction action = CommentAction.valueOf(formValues.getFirst("action").toUpperCase());
        final String text = formValues.getFirst("text");
        final String author = formValues.getFirst("author");
        final String url = formValues.getFirst("url");
        final boolean acceptCookie = "checked".equalsIgnoreCase(formValues.getFirst("acceptCookie"));

        byte[] authorHash = null;
        if (acceptCookie) {
            final String sessionId = request.getSession(true).getId();
            try {
                final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                authorHash = sha1.digest(sessionId.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                log.warn("SHA-1 not supported");
            }
        }
        if (authorHash == null) {
            authorHash = new byte[1];
        }

        final Comment previewComment = new Comment()
                .setParentId(parentCommentId)
                .setCreationDate(LocalDateTime.now(Clock.systemUTC()))
                .setLastModificationDate(LocalDateTime.now(Clock.systemUTC()))
                .setText(text)
                .setAuthor(author)
                .setUrl(url)
                .setAuthorHash(authorHash)
                .setStatus(CommentStatus.AWAITING_MODERATION);

        final Map<String, Object> model = getModelForCommentForm(threadId, parentCommentId, previewComment);
        model.put("previewComment", previewComment);

        return new ModelAndView("comment-form", model);
    }

    private Map<String, Object> getModelForCommentForm(long threadId, Long parentCommentId, Comment previewComment) {
        final CommentThread thread = threadRepository.findById(threadId).orElseThrow(RuntimeException::new);
        final Optional<Comment> parentComment =
                Optional.ofNullable(parentCommentId).flatMap(commentRepository::findById);

        final Map<String, Object> model = new HashMap<>();
        model.put("thread", thread);
        model.put("base64Url", Base64UrlMethod.INSTANCE);

        final Map<Long, Object> comments = new HashMap<>(2);

        parentComment.ifPresent(parent -> {
            model.put("parentComment", parent);
            comments.put(parent.getId(), parent);
            addParentOfParent(comments, parent);
        });

        if (previewComment != null) {
            model.put("previewComment", previewComment);
        }

        model.put("comments", comments);

        return model;
    }

    private void addParentOfParent(Map<Long, Object> comments, Comment parentComment) {
        if (parentComment.getParentId() != null) {
            final Comment parentOfParent =
                    commentRepository.findById(parentComment.getParentId()).orElseThrow(RuntimeException::new);
            comments.put(parentOfParent.getId(), parentOfParent);
        }
    }

}
