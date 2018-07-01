package de.vorb.platon.web.mvc.threads;

import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;
import de.vorb.platon.web.mvc.comments.CommentAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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

    private static final String VIEW_NAME = "comment-form";

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final Clock clock;
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    @GetMapping(value = {"/threads/{threadId}/reply", "/threads/{threadId}/comments/{parentCommentId}/reply"},
            produces = MediaType.TEXT_HTML_VALUE)
    public String showThreadReplyForm(
            @PathVariable(CommentController.PATH_VAR_THREAD_ID) long threadId,
            @PathVariable(value = "parentCommentId", required = false) Long parentCommentId,
            @ModelAttribute("comment") CommentFormData comment, Model model) {

        applyCommentFormModel(model, threadId, parentCommentId);

        return VIEW_NAME;
    }

    @PostMapping(value = {"/threads/{threadId}/reply", "/threads/{threadId}/comments/{parentCommentId}/reply"},
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public String postComment(HttpServletRequest request,
            @PathVariable(CommentController.PATH_VAR_THREAD_ID) long threadId,
            @PathVariable(value = "parentCommentId", required = false) Long parentCommentId,
            @Valid @ModelAttribute("comment") CommentFormData formData, BindingResult bindingResult, Model model) {

        applyCommentFormModel(model, threadId, parentCommentId);

        if (bindingResult.hasErrors()) {
            return VIEW_NAME;
        } else {
            final Comment comment = createComment(request, threadId, parentCommentId, formData);

            if (formData.getAction() == CommentAction.CREATE) {
                final Comment storedComment = commentRepository.insert(comment);
                return "redirect:" + CommentController.pathSingleThread(threadId) + "#comment-" + storedComment.getId();
            } else {
                model.addAttribute("previewComment", comment);
                return VIEW_NAME;
            }
        }
    }

    private Comment createComment(HttpServletRequest request, long threadId, Long parentCommentId,
            CommentFormData comment) {
        byte[] authorHash = null;
        if (comment.isAcceptCookie()) {
            final String sessionId = request.getSession(true).getId();
            try {
                final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                authorHash = sha1.digest(sessionId.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                log.warn("SHA-1 not supported");
            }
        }
        if (authorHash == null) {
            authorHash = new byte[0];
        }

        final LocalDateTime now = LocalDateTime.now(clock);

        final Node parsedMarkdown = parser.parse(comment.getText());
        final String textHtml = htmlRenderer.render(parsedMarkdown);

        return new Comment()
                .setThreadId(threadId)
                .setParentId(parentCommentId)
                .setCreationDate(now)
                .setLastModificationDate(now)
                .setTextSource(comment.getText())
                .setTextHtml(textHtml)
                .setAuthor(comment.getAuthor())
                .setUrl(comment.getUrl())
                .setAuthorHash(authorHash)
                .setStatus(CommentStatus.PUBLIC);
    }

    private void applyCommentFormModel(Model model, long threadId, Long parentCommentId) {
        final CommentThread thread = threadRepository.findById(threadId).orElseThrow(RuntimeException::new);
        final Optional<Comment> parentComment =
                Optional.ofNullable(parentCommentId).flatMap(commentRepository::findById);

        model.addAttribute("thread", thread);

        final Map<Long, Object> comments = new HashMap<>(2);

        parentComment.ifPresent(parent -> {
            model.addAttribute("parentComment", parent);
            comments.put(parent.getId(), parent);
            addParentOfParent(comments, parent);
        });

        model.addAttribute("comments", comments);
    }

    private void addParentOfParent(Map<Long, Object> comments, Comment parentComment) {
        if (parentComment.getParentId() != null) {
            final Comment parentOfParent =
                    commentRepository.findById(parentComment.getParentId()).orElseThrow(RuntimeException::new);
            comments.put(parentOfParent.getId(), parentOfParent);
        }
    }

}
