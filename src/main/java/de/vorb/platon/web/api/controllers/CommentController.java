/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vorb.platon.web.api.controllers;

import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;
import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;
import de.vorb.platon.security.SignatureCreator;
import de.vorb.platon.web.api.common.CommentFilters;
import de.vorb.platon.web.api.common.CommentSanitizer;
import de.vorb.platon.web.api.common.CommentUriResolver;
import de.vorb.platon.web.api.common.RequestValidator;
import de.vorb.platon.web.api.errors.RequestException;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.time.Clock;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.vorb.platon.model.CommentStatus.PUBLIC;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;


@Controller
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private static final String PATH_LIST = "/comments";
    public static final String PATH_VAR_COMMENT_ID = "commentId";
    public static final String PATH_SINGLE = PATH_LIST + "/{" + PATH_VAR_COMMENT_ID + "}.html";

    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final CommentStatus DEFAULT_STATUS = PUBLIC;

    private static final ZoneId UTC = ZoneId.of("UTC");


    private final Clock clock;

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final SignatureCreator signatureCreator;

    private final CommentUriResolver commentUriResolver;
    private final RequestValidator requestValidator;
    private final CommentFilters commentFilters;
    private final CommentSanitizer commentSanitizer;


    @GetMapping(value = PATH_SINGLE, produces = TEXT_HTML_VALUE)
    public ModelAndView getCommentById(@PathVariable(PATH_VAR_COMMENT_ID) long commentId) {

        final Comment comment = commentRepository.findById(commentId)
                .filter(c -> c.getStatus() == PUBLIC)
                .orElseThrow(() ->
                        RequestException.notFound()
                                .message("No comment found with ID = " + commentId)
                                .build());

        return new ModelAndView("comment-single", Collections.singletonMap("comment", comment));
    }


    @GetMapping(value = PATH_LIST, produces = TEXT_HTML_VALUE)
    public ModelAndView findCommentsByThreadUrl(@RequestParam("threadUrl") String threadUrl) {

        final CommentThread thread = threadRepository.findThreadForUrl(threadUrl)
                .orElseThrow(() -> RequestException.withStatus(HttpStatus.NOT_FOUND)
                        .message("No thread exists for URL “" + threadUrl + '”').build());
        final List<Comment> comments = commentRepository.findByThreadId(thread.getId());
        if (comments.isEmpty()) {
            throw RequestException.notFound()
                    .message("No thread found with url = '" + threadUrl + "'")
                    .build();
        } else {
            final long commentCount = comments.stream().filter(commentFilters::doesCommentCount).count();

            final Map<Long, Comment> commentsById = comments.stream()
                    .collect(Collectors.toMap(Comment::getId, Function.identity(), throwingMerger(),
                            LinkedHashMap::new));

            return new ModelAndView("comments-flat",
                    ImmutableMap.of("commentCount", commentCount, "comments", commentsById));
        }
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (a, b) -> {
            throw new IllegalStateException("Duplicate key " + a);
        };
    }

    @GetMapping(value = "/reply", produces = TEXT_HTML_VALUE, params = "threadTitle")
    public ModelAndView showCommentForm(@RequestParam("threadUrl") String threadUrl,
            @RequestParam("threadTitle") String threadTitle) {
        return new ModelAndView("comment-form", ImmutableMap.of("threadUrl", threadUrl, "threadTitle", threadTitle));
    }

//    @PostMapping(value = PATH_LIST, produces = TEXT_HTML_VALUE)
//    public ModelAndView postComment(
//            @RequestBody MultiValueMap<String, String> commentData) {
//
//        final CommentAction action = Optional.ofNullable(commentData.getFirst("action"))
//                .map(String::toUpperCase)
//                .map(CommentAction::valueOf)
//                .orElse(CommentAction.CREATE);
//
//        final String commentText = commentData.getFirst("commentText");
//        final String commentAuthor = commentData.getFirst("commentAuthor");
//        final String commentUrl = commentData.getFirst("commentUrl");
//
//        final String threadUrl = commentData.getFirst("threadUrl");
//        final String commentTitle = commentData.getFirst("commentTitle");
//
//        final Long threadId = threadRepository.findThreadIdForUrl(threadUrl)
//                .orElseGet(() -> {
//                    final CommentThread thread = new CommentThread()
//                            .setUrl(threadUrl)
//                            .setTitle(commentTitle);
//
//                    final long newThreadId = threadRepository.insert(thread).getId();
//                    log.info("Created new thread for url '{}'", threadUrl);
//                    return newThreadId;
//                });
//
//        if (action == CommentAction.CREATE) {
//
//        }
//    }
//
//    @PostMapping(value = PATH_LIST, consumes = APPLICATION_FORM_URLENCODED_VALUE, produces = TEXT_HTML_VALUE)
//    public ModelAndView postComment(
//            @RequestParam("url") String threadUrl,
//            @RequestParam("threadTitle") String threadTitle,
//            @RequestParam(value = "action", defaultValue = "CREATE") CommentAction action,
//            @RequestBody MultiValueMap<String, String> commentData) {
//
//        if (commentData.getId() != null) {
//            throw RequestException.badRequest()
//                    .message("Comment ID is not null")
//                    .build();
//        }
//
//        final long threadId = threadRepository.findThreadIdForUrl(threadUrl)
//                .orElseGet(() -> {
//                    final CommentThread thread = new CommentThread()
//                            .setUrl(threadUrl)
//                            .setTitle(threadTitle);
//
//                    final long newThreadId = threadRepository.insert(thread).getId();
//
//                    log.info("Created new thread for url '{}'", threadUrl);
//
//                    return newThreadId;
//                });
//
//        commentData.setStatus(DEFAULT_STATUS);
//
//        Comment comment = commentConverter.convertJsonToPojo(commentData);
//
//        comment.setThreadId(threadId);
//        comment.setCreationDate(LocalDateTime.ofInstant(clock.instant(), UTC));
//        comment.setLastModificationDate(comment.getCreationDate());
//
//        assertParentBelongsToSameThread(comment);
//
//        commentSanitizer.sanitizeComment(comment);
//
//        comment = commentRepository.insert(comment);
//
//        log.info("Posted new comment to thread '{}'", threadUrl);
//
//        final URI commentUri = commentUriResolver.createRelativeCommentUriForId(comment.getId());
//        final LocalDateTime expirationTime = comment.getCreationDate().plusDays(1);
//        final SignatureComponents signatureComponents =
//                signatureCreator.createSignatureComponents(commentUri.toString(),
//                        expirationTime.toInstant(ZoneOffset.UTC));
//
//        return ResponseEntity.created(commentUri)
//                .header(SIGNATURE_HEADER, signatureComponents.toString())
//                .body(commentConverter.convertPojoToJson(comment));
//    }
//
//    private void assertParentBelongsToSameThread(Comment comment) {
//
//        final Long parentId = comment.getParentId();
//        if (parentId == null) {
//            return;
//        }
//
//        final Comment parentComment = commentRepository.findById(parentId)
//                .orElseThrow(() ->
//                        RequestException.badRequest()
//                                .message("Parent comment does not exist")
//                                .build());
//
//        if (!comment.getThreadId().equals(parentComment.getThreadId())) {
//            throw RequestException.badRequest()
//                    .message("Parent comment does not belong to same thread")
//                    .build();
//        }
//
//    }
//
//
//    @PutMapping(value = PATH_SINGLE, consumes = APPLICATION_JSON_VALUE)
//    public void updateComment(
//            @PathVariable(PATH_VAR_COMMENT_ID) Long commentId,
//            @RequestHeader(SIGNATURE_HEADER) String signature,
//            @RequestBody CommentJson commentJson) {
//
//        if (!commentId.equals(commentJson.getId())) {
//            throw RequestException.badRequest()
//                    .message(String.format("Comment IDs do not match (%d != %d)", commentJson.getId(), commentId))
//                    .build();
//        }
//
//        final String commentUri = commentUriResolver.createRelativeCommentUriForId(commentId).toString();
//        requestValidator.verifyValidRequest(signature, commentUri);
//
//        final Comment comment = commentRepository.findById(commentId)
//                .orElseThrow(() ->
//                        RequestException.badRequest()
//                                .message(String.format("Comment with ID = %d does not exist", commentId))
//                                .build());
//
//        comment.setText(commentJson.getText());
//        comment.setAuthor(comment.getAuthor());
//        comment.setUrl(comment.getUrl());
//
//        comment.setLastModificationDate(clock.instant());
//
//        commentSanitizer.sanitizeComment(comment);
//
//        try {
//            commentRepository.update(comment);
//        } catch (DataAccessException e) {
//            throw RequestException.withStatus(CONFLICT)
//                    .message(String.format("Conflict on update of comment with ID = %d", commentId))
//                    .cause(e)
//                    .build();
//        }
//    }
//
//
//    @DeleteMapping(PATH_SINGLE)
//    public void deleteComment(
//            @PathVariable(PATH_VAR_COMMENT_ID) Long commentId,
//            @RequestHeader(SIGNATURE_HEADER) String signature) {
//
//        final URI commentUri = commentUriResolver.createRelativeCommentUriForId(commentId);
//
//        requestValidator.verifyValidRequest(signature, commentUri.toString());
//
//        try {
//            commentRepository.setStatus(commentId, DELETED);
//
//            log.info("Marked comment with ID = {} as {}", commentId, DELETED);
//        } catch (DataAccessException e) {
//            throw RequestException.badRequest()
//                    .message(String.format("Unable to delete comment with ID = %d. Does it exist?", commentId))
//                    .cause(e)
//                    .build();
//        }
//    }

}
