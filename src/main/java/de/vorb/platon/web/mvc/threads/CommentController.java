package de.vorb.platon.web.mvc.threads;

import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;
import de.vorb.platon.web.mvc.errors.RequestException;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CommentController {

    static final String PATH_LIST_THREADS = "/threads";
    static final String PATH_VAR_THREAD_ID = "threadId";
    static final String PATH_SINGLE_THREAD = PATH_LIST_THREADS + "/{" + PATH_VAR_THREAD_ID + "}/comments";

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    @GetMapping(value = PATH_SINGLE_THREAD, produces = TEXT_HTML_VALUE)
    public ModelAndView findCommentsByThreadUrl(@PathVariable(PATH_VAR_THREAD_ID) long threadId,
            HttpServletRequest request) {

        final CommentThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> RequestException.withStatus(HttpStatus.NOT_FOUND)
                        .message("No thread exists for ID “" + threadId + '”').build());
        final List<Comment> comments = commentRepository.findByThreadId(thread.getId());

        final Map<Long, Comment> commentsById = comments.stream()
                .collect(Collectors.toMap(Comment::getId, Function.identity(), throwingMerger(),
                        LinkedHashMap::new));

        return new ModelAndView("comments-flat",
                ImmutableMap.of("thread", thread, "commentCount", comments.size(), "comments", commentsById));
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (a, b) -> {
            throw new IllegalStateException("Duplicate key " + a);
        };
    }

    static String pathSingleThread(long threadId) {
        return PATH_SINGLE_THREAD.replace('{' + PATH_VAR_THREAD_ID + '}', String.valueOf(threadId));
    }
}
