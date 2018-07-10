package de.vorb.platon.web.mvc.threads;

import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ThreadRedirectController {

    private static final String THREAD_ID_REPLACEMENT_TARGET = '{' + CommentController.PATH_VAR_THREAD_ID + '}';

    private final ThreadRepository threadRepository;

    @GetMapping(value = CommentController.PATH_LIST_THREADS, params = {"url", "title"})
    @ResponseStatus(HttpStatus.MOVED_PERMANENTLY)
    @Transactional
    public String redirectToComments(
            @RequestParam("url") String threadUrl,
            @RequestParam("title") String threadTitle) {

        final CommentThread thread = threadRepository.findThreadForUrl(threadUrl)
                .orElseGet(() -> createThread(threadUrl, threadTitle));

        return getRedirectForThread(thread);
    }

    @GetMapping(value = CommentController.PATH_LIST_THREADS + '/' + THREAD_ID_REPLACEMENT_TARGET)
    @ResponseStatus(HttpStatus.TEMPORARY_REDIRECT)
    @Transactional(readOnly = true)
    public String redirectToComments(@PathVariable("threadId") long threadId) {

        CommentThread thread = threadRepository.findById(threadId).orElseThrow(IndexOutOfBoundsException::new);

        return getRedirectForThread(thread);
    }

    private CommentThread createThread(String threadUrl, String threadTitle) {

        final CommentThread newThread = threadRepository.insert(
                new CommentThread().setUrl(threadUrl).setTitle(threadTitle));

        log.info("Created new thread: {}", newThread);

        return newThread;
    }

    private String getRedirectForThread(CommentThread commentThread) {
        return "redirect:" + CommentController.PATH_SINGLE_THREAD.replace(THREAD_ID_REPLACEMENT_TARGET,
                String.valueOf(commentThread.getId()));
    }

}
