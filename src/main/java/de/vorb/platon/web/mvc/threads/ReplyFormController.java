package de.vorb.platon.web.mvc.threads;

import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
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

        final CommentThread thread = threadRepository.findById(threadId).orElseThrow(RuntimeException::new);
        final Optional<Comment> parentComment =
                Optional.ofNullable(parentCommentId).flatMap(commentRepository::findById);

        final Map<String, Object> model = new HashMap<>();
        model.put("thread", thread);
        parentComment.ifPresent(parent -> addParent(model, parent));

        return new ModelAndView("comment-form", model);
    }

    private void addParent(Map<String, Object> model, Comment parentComment) {
        model.put("parentComment", parentComment);
        addParentOfParent(model, parentComment);
    }

    private void addParentOfParent(Map<String, Object> model, Comment parentComment) {
        if (parentComment.getParentId() != null) {
            final Comment parentOfParent =
                    commentRepository.findById(parentComment.getParentId()).orElseThrow(RuntimeException::new);
            model.put("comments", Collections.<Long, Object>singletonMap(parentOfParent.getId(), parentOfParent));
        } else {
            model.put("comments", Collections.<Long, Object>emptyMap());
        }
    }

}
