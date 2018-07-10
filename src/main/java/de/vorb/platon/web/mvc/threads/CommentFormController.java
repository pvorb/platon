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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CommentFormController {

    static final String AUTHOR_ID_COOKIE = "author_id";
    private static final int MAX_COOKIE_AGE = (int) Duration.ofDays(10 * 365).getSeconds();

    static final byte[] EMPTY_STRING_HASH = new byte[20];

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
    private final SecureRandom secureRandom;

    @GetMapping(value = {"/threads/{threadId}/reply", "/threads/{threadId}/comments/{parentCommentId}/reply"},
            produces = MediaType.TEXT_HTML_VALUE)
    @Transactional(readOnly = true)
    public String showReplyForm(
            @PathVariable(CommentController.PATH_VAR_THREAD_ID) long threadId,
            @PathVariable(value = "parentCommentId", required = false) Long parentCommentId,
            @ModelAttribute("comment") CommentFormData comment, Model model) {

        applyCommentFormModel(model, threadId, parentCommentId);

        return VIEW_NAME;
    }

    @PostMapping(value = {"/threads/{threadId}/reply", "/threads/{threadId}/comments/{parentCommentId}/reply"},
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    @Transactional
    public String postNewComment(HttpServletRequest request, HttpServletResponse response,
            @PathVariable(CommentController.PATH_VAR_THREAD_ID) long threadId,
            @PathVariable(value = "parentCommentId", required = false) Long parentCommentId,
            @CookieValue(value = AUTHOR_ID_COOKIE, required = false) String authorId,
            @Valid @ModelAttribute("comment") CommentFormData formData, BindingResult bindingResult, Model model) {

        applyCommentFormModel(model, threadId, parentCommentId);

        if (formData.isAcceptCookie() && authorId == null) {
            final Cookie commentAuthorCookie = new Cookie(AUTHOR_ID_COOKIE, UUID.randomUUID().toString());
            commentAuthorCookie.setMaxAge(MAX_COOKIE_AGE);
            response.addCookie(commentAuthorCookie);
        }

        if (bindingResult.hasErrors()) {
            return VIEW_NAME;
        } else {
            final Comment comment = createComment(request, threadId, parentCommentId, authorId, formData);

            if (formData.getAction() == CommentAction.CREATE) {
                final Comment storedComment = commentRepository.insert(comment);
                return "redirect:" + CommentController.pathSingleThread(threadId) + "#comment-" + storedComment.getId();
            } else if (formData.getAction() == CommentAction.PREVIEW) {
                model.addAttribute("previewComment", comment);
                return VIEW_NAME;
            } else {
                throw new RuntimeException();
            }
        }
    }

    @GetMapping(value = {"/threads/{threadId}/comments/{commentId}/edit"}, produces = MediaType.TEXT_HTML_VALUE)
    @Transactional(readOnly = true)
    public String showEditForm(
            @PathVariable(CommentController.PATH_VAR_THREAD_ID) long threadId,
            @PathVariable(value = "commentId") Long commentId,
            @ModelAttribute("comment") CommentFormData comment, Model model) {

        final CommentThread thread = threadRepository.findById(threadId).orElseThrow(RuntimeException::new);
        final Comment edit = commentRepository.findById(commentId).orElseThrow(RuntimeException::new);

        model.addAttribute("thread", thread);
        comment.setText(edit.getTextSource());
        comment.setAuthor(edit.getAuthor());
        comment.setUrl(edit.getUrl());

        return VIEW_NAME;
    }

    private Comment createComment(HttpServletRequest request, long threadId, Long parentCommentId,
            String authorId, CommentFormData formData) {
        byte[] authorHash = null;

        final String authorOrSessionId = authorId != null ? authorId : request.getSession(true).getId();
        try {
            final MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            authorHash = sha1.digest(authorOrSessionId.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-1 not supported");
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
