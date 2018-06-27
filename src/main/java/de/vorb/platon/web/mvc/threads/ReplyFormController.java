package de.vorb.platon.web.mvc.threads;

import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReplyFormController {

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    @GetMapping(value = "/threads/{threadId}/reply", produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView showReplyForm(@PathVariable(CommentController.PATH_VAR_THREAD_ID) long threadId) {

        final CommentThread thread = threadRepository.findById(threadId).orElseThrow(RuntimeException::new);

        return new ModelAndView("comment-form", ImmutableMap.of("thread", thread));
    }

}
