package de.vorb.platon.web.mvc.threads;

import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.Tables;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;
import de.vorb.platon.services.markdown.MarkdownRenderer;
import de.vorb.platon.web.mvc.comments.CommentAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import java.util.regex.Pattern;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReplyFormController {

    private static final byte[] EMPTY_STRING_HASH = new byte[20];

    static {
        try {
            final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            final byte[] hash = sha1.digest("".getBytes(StandardCharsets.UTF_8));
            System.arraycopy(hash, 0, EMPTY_STRING_HASH, 0, hash.length);
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-1 not supported");
        }
    }

    private static final String VIEW_NAME = "comment-form";

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final int MAX_TEXT_REFERENCE_LENGTH = Tables.COMMENT.TEXT_REFERENCE.getDataType().length();

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final MarkdownRenderer markdownRenderer;
    private final Clock clock;

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
            CommentFormData formData) {
        byte[] authorHash = null;
        if (formData.isAcceptCookie()) {
            final String sessionId = request.getSession(true).getId();
            try {
                final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                authorHash = sha1.digest(sessionId.getBytes(StandardCharsets.UTF_8));
            } catch (NoSuchAlgorithmException e) {
                log.warn("SHA-1 not supported");
            }
        }
        if (authorHash == null) {
            authorHash = EMPTY_STRING_HASH;
        }

        final LocalDateTime now = LocalDateTime.now(clock);

        final String textHtml = markdownRenderer.renderToHtml(formData.getText());
        final String textWithoutLineBreaks = formData.getText().replace("\n", "").replace("\r", "");
        final String textWithoutMarkup = HTML_TAG_PATTERN.matcher(textWithoutLineBreaks).replaceAll("").trim();
        final String textReference = StringUtils.abbreviate(textWithoutMarkup, "…", MAX_TEXT_REFERENCE_LENGTH);

        return new Comment()
                .setThreadId(threadId)
                .setParentId(parentCommentId)
                .setCreationDate(now)
                .setLastModificationDate(now)
                .setTextSource(formData.getText())
                .setTextHtml(textHtml)
                .setTextReference(textReference)
                .setAuthor(formData.getAuthor())
                .setUrl(formData.getUrl())
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
