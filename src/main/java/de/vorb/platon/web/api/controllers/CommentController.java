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

import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.jooq.tables.records.ThreadsRecord;
import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.security.SignatureComponents;
import de.vorb.platon.security.SignatureTokenValidator;
import de.vorb.platon.web.api.common.CommentConverter;
import de.vorb.platon.web.api.common.CommentFilters;
import de.vorb.platon.web.api.common.CommentSanitizer;
import de.vorb.platon.web.api.errors.RequestException;
import de.vorb.platon.web.api.json.CommentJson;
import de.vorb.platon.web.api.json.CommentListResultJson;

import com.google.common.annotations.VisibleForTesting;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    @VisibleForTesting
    private static final String PATH_LIST = "/api/comments";
    private static final String PATH_VAR_COMMENT_ID = "commentId";
    private static final String PATH_SINGLE = PATH_LIST + "/{" + PATH_VAR_COMMENT_ID + "}";

    @VisibleForTesting
    static final String SIGNATURE_HEADER = "X-Signature";
    private static final CommentStatus DEFAULT_STATUS = CommentStatus.PUBLIC;


    private final Clock clock;

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    private final CommentConverter commentConverter;
    private final SignatureTokenValidator signatureTokenValidator;
    private final CommentFilters commentFilters;
    private final CommentSanitizer commentSanitizer;


    @GetMapping(value = PATH_SINGLE, produces = APPLICATION_JSON_UTF8_VALUE)
    public CommentJson getCommentById(@PathVariable(PATH_VAR_COMMENT_ID) long commentId) {

        final CommentsRecord comment = commentRepository.findById(commentId)
                .filter(c ->
                        CommentStatus.valueOf(c.getStatus()) == CommentStatus.PUBLIC)
                .orElseThrow(() ->
                        RequestException.notFound()
                                .message("No comment found with ID = " + commentId)
                                .build());

        return commentConverter.convertRecordToJson(comment);
    }


    @GetMapping(value = PATH_LIST, produces = APPLICATION_JSON_UTF8_VALUE)
    public CommentListResultJson findCommentsByThreadUrl(@RequestParam("threadUrl") String threadUrl) {

        final List<CommentsRecord> comments = commentRepository.findByThreadUrl(threadUrl);
        if (comments.isEmpty()) {
            throw RequestException.notFound()
                    .message(String.format("No thread found with url = '%s'", threadUrl))
                    .build();
        } else {
            final long totalCommentCount = comments.stream().filter(commentFilters::doesCommentCount).count();
            final List<CommentJson> topLevelComments = transformFlatCommentListToTree(comments);

            return CommentListResultJson.builder()
                    .totalCommentCount(totalCommentCount)
                    .comments(topLevelComments)
                    .build();
        }
    }

    private List<CommentJson> transformFlatCommentListToTree(List<CommentsRecord> comments) {

        final Map<Long, CommentJson> lookupMap = comments.stream()
                .map(commentConverter::convertRecordToJson)
                .collect(Collectors.toMap(CommentJson::getId, Function.identity()));

        final List<CommentJson> topLevelComments = new ArrayList<>();
        for (CommentsRecord comment : comments) {
            final List<CommentJson> commentList;
            if (comment.getParentId() == null) {
                commentList = topLevelComments;
            } else {
                commentList = lookupMap.get(comment.getParentId()).getReplies();
            }
            commentList.add(lookupMap.get(comment.getId()));
        }

        return topLevelComments;
    }

    @PostMapping(value = PATH_LIST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommentJson> postComment(
            @RequestParam("threadUrl") String threadUrl,
            @RequestParam("threadTitle") String threadTitle,
            @RequestBody CommentJson commentJson) {

        if (commentJson.getId() != null) {
            throw RequestException.badRequest()
                    .message("Comment ID is not null")
                    .build();
        }

        final long threadId = threadRepository.findThreadIdForUrl(threadUrl)
                .orElseGet(() -> {
                    final ThreadsRecord thread = new ThreadsRecord()
                            .setUrl(threadUrl)
                            .setTitle(threadTitle);

                    final long newThreadId = threadRepository.insert(thread).getId();

                    log.info("Created new thread for url '{}'", threadUrl);

                    return newThreadId;
                });

        commentJson.setStatus(DEFAULT_STATUS);

        CommentsRecord comment = commentConverter.convertJsonToRecord(commentJson);

        comment.setThreadId(threadId);
        comment.setCreationDate(Timestamp.from(clock.instant()));
        comment.setLastModificationDate(comment.getCreationDate());

        assertParentBelongsToSameThread(comment);

        commentSanitizer.sanitizeComment(comment);

        comment = commentRepository.insert(comment);

        log.info("Posted new comment to thread '{}'", threadUrl);

        final URI commentUri = getRelativeCommentUri(comment.getId());
        final Instant expirationTime = comment.getCreationDate().toInstant().plus(24, ChronoUnit.HOURS);
        final byte[] signatureToken = signatureTokenValidator.getSignatureToken(commentUri.toString(), expirationTime);

        return ResponseEntity.created(commentUri)
                .header(SIGNATURE_HEADER,
                        SignatureComponents.of(commentUri.toString(), expirationTime, signatureToken).toString())
                .body(commentConverter.convertRecordToJson(comment));
    }

    private void assertParentBelongsToSameThread(CommentsRecord comment) {

        final Long parentId = comment.getParentId();
        if (parentId == null) {
            return;
        }

        final CommentsRecord parentComment = commentRepository.findById(parentId)
                .orElseThrow(() ->
                        RequestException.badRequest()
                                .message("Parent comment does not exist")
                                .build());

        if (!comment.getThreadId().equals(parentComment.getThreadId())) {
            throw RequestException.badRequest()
                    .message("Parent comment does not belong to same thread")
                    .build();
        }

    }


    @PutMapping(value = PATH_SINGLE, consumes = APPLICATION_JSON_VALUE)
    public void updateComment(
            @PathVariable(PATH_VAR_COMMENT_ID) Long commentId,
            @RequestHeader(SIGNATURE_HEADER) String signature,
            @RequestBody CommentJson commentJson) {

        if (!commentId.equals(commentJson.getId())) {
            throw RequestException.badRequest()
                    .message(String.format("Comment IDs do not match (%d != %d)", commentJson.getId(), commentId))
                    .build();
        }

        verifyValidRequest(signature, commentId);

        final CommentsRecord comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        RequestException.badRequest()
                                .message(String.format("Comment with ID = %d does not exist", commentId))
                                .build());

        comment.setText(commentJson.getText());
        comment.setAuthor(comment.getAuthor());
        comment.setUrl(comment.getUrl());

        comment.setLastModificationDate(Timestamp.from(clock.instant()));

        commentSanitizer.sanitizeComment(comment);

        try {
            commentRepository.update(comment);
        } catch (DataAccessException e) {
            throw RequestException.withStatus(HttpStatus.CONFLICT)
                    .message(String.format("Conflict on update of comment with ID = %d", commentId))
                    .cause(e)
                    .build();
        }
    }

    @DeleteMapping(PATH_SINGLE)
    public void deleteComment(
            @PathVariable(PATH_VAR_COMMENT_ID) Long commentId,
            @RequestHeader(SIGNATURE_HEADER) String signature) {

        verifyValidRequest(signature, commentId);

        try {
            commentRepository.setStatus(commentId, CommentStatus.DELETED);

            log.info("Marked comment with ID = {} as DELETED", commentId);
        } catch (DataAccessException e) {
            throw RequestException.badRequest()
                    .message(String.format("Unable to delete comment with ID = %d. Does it exist?", commentId))
                    .cause(e)
                    .build();
        }
    }

    private void verifyValidRequest(String signature, Long commentId) {

        try {
            final SignatureComponents signatureComponents = SignatureComponents.fromString(signature);

            final String referenceIdentifier = getRelativeCommentUri(commentId).toString();

            checkArgument(signatureComponents.getIdentifier().equals(referenceIdentifier));

            if (!signatureTokenValidator.isSignatureValid(signatureComponents)) {
                throw RequestException.badRequest()
                        .message("Authentication signature is invalid or has expired")
                        .build();
            }

        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw RequestException.badRequest()
                    .message("Illegal authentication signature provided")
                    .cause(e)
                    .build();
        }
    }

    @SneakyThrows
    private URI getRelativeCommentUri(long commentId) {
        return new URI(ServletUriComponentsBuilder.fromCurrentRequest()
                .path(PATH_SINGLE)
                .replaceQuery(null)
                .buildAndExpand(Collections.singletonMap(PATH_VAR_COMMENT_ID, commentId))
                .getPath());
    }
}
