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
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.util.CommentConverter;
import de.vorb.platon.util.CommentFilters;
import de.vorb.platon.util.InputSanitizer;
import de.vorb.platon.web.api.errors.RequestException;
import de.vorb.platon.web.api.json.CommentJson;
import de.vorb.platon.web.api.json.CommentListResultJson;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.owasp.encoder.Encode;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Slf4j
public class CommentController {

    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final CommentStatus DEFAULT_STATUS = CommentStatus.PUBLIC;

    private static final PolicyFactory NO_HTML_POLICY = new HtmlPolicyBuilder().toFactory();


    private final Clock clock;

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    private final CommentConverter commentConverter;
    private final RequestVerifier requestVerifier;
    private final InputSanitizer inputSanitizer;
    private final CommentFilters commentFilters;


    @Autowired
    public CommentController(
            Clock clock,
            ThreadRepository threadRepository,
            CommentRepository commentRepository,
            CommentConverter commentConverter,
            RequestVerifier requestVerifier,
            InputSanitizer inputSanitizer,
            CommentFilters commentFilters) {

        this.clock = clock;

        this.threadRepository = threadRepository;
        this.commentRepository = commentRepository;

        this.commentConverter = commentConverter;
        this.requestVerifier = requestVerifier;
        this.inputSanitizer = inputSanitizer;
        this.commentFilters = commentFilters;
    }


    @GetMapping(value = "/comments/{id}", produces = APPLICATION_JSON_UTF8_VALUE)
    public CommentJson getCommentById(@PathVariable("id") long commentId) {

        final CommentsRecord comment = commentRepository.findById(commentId);

        if (comment == null || Enum.valueOf(CommentStatus.class, comment.getStatus()) != CommentStatus.PUBLIC) {
            throw RequestException.notFound()
                    .message(String.format("No comment found with id = %d", commentId))
                    .build();
        } else {
            return commentConverter.convertRecordToJson(comment);
        }
    }


    @GetMapping(value = "/comments", produces = APPLICATION_JSON_UTF8_VALUE)
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

    @PostMapping(value = "/comments", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CommentJson> postComment(
            @RequestParam("threadUrl") String threadUrl,
            @RequestParam("threadTitle") String threadTitle,
            @RequestBody CommentJson commentJson) {

        if (commentJson.getId() != null) {
            throw RequestException.badRequest()
                    .message("Comment id is not null")
                    .build();
        }

        Long threadId = threadRepository.findThreadIdForUrl(threadUrl);
        if (threadId == null) {

            final ThreadsRecord thread = new ThreadsRecord()
                    .setUrl(threadUrl)
                    .setTitle(threadTitle);

            threadId = threadRepository.insert(thread).getId();

            logger.info("Created new thread for url '{}'", threadUrl);
        }

        commentJson.setStatus(DEFAULT_STATUS);

        CommentsRecord comment = commentConverter.convertJsonToRecord(commentJson);

        comment.setThreadId(threadId);
        comment.setCreationDate(Timestamp.from(clock.instant()));
        comment.setLastModificationDate(comment.getCreationDate());

        assertParentBelongsToSameThread(comment);

        sanitizeComment(comment);

        comment = commentRepository.insert(comment);

        logger.info("Posted new comment to thread '{}'", threadUrl);

        final URI commentUri = getUriFromId(comment.getId());

        final String identifier = commentUri.toString();
        final Instant expirationDate = comment.getCreationDate().toInstant().plus(24, ChronoUnit.HOURS);
        final byte[] signature = requestVerifier.getSignatureToken(identifier, expirationDate);

        return ResponseEntity.created(commentUri)
                .header(SIGNATURE_HEADER, String.format("%s|%s|%s", identifier, expirationDate,
                        Base64.getEncoder().encodeToString(signature)))
                .body(commentConverter.convertRecordToJson(comment));
    }

    private void assertParentBelongsToSameThread(CommentsRecord comment) {

        final Long parentId = comment.getParentId();
        if (parentId == null) {
            return;
        }

        final CommentsRecord parentComment = commentRepository.findById(parentId);

        if (parentComment == null) {
            throw RequestException.badRequest()
                    .message("Parent comment does not exist")
                    .build();
        }

        if (!comment.getThreadId().equals(parentComment.getThreadId())) {
            throw RequestException.badRequest()
                    .message("Parent comment does not belong to same thread")
                    .build();
        }

    }


    @PutMapping(value = "/comments/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_UTF8_VALUE)
    public void updateComment(
            @PathVariable("id") Long commentId,
            @RequestHeader(SIGNATURE_HEADER) String signature,
            @RequestBody CommentJson commentJson) {

        if (!commentId.equals(commentJson.getId())) {
            throw RequestException.badRequest()
                    .message(String.format("Comment ids do not match (%d != %d)", commentJson.getId(), commentId))
                    .build();
        }

        verifyValidRequest(signature, commentId);

        final CommentsRecord comment = commentRepository.findById(commentId);

        if (comment != null) {

            comment.setText(commentJson.getText());
            comment.setAuthor(comment.getAuthor());
            comment.setUrl(comment.getUrl());

            comment.setLastModificationDate(Timestamp.from(clock.instant()));

            sanitizeComment(comment);

            try {
                commentRepository.update(comment);
            } catch (DataAccessException e) {
                throw RequestException.withStatus(HttpStatus.CONFLICT)
                        .message(String.format("Conflict on update of comment with id = %d", commentId))
                        .cause(e)
                        .build();
            }

        } else {
            throw RequestException.badRequest()
                    .message(String.format("Comment with id = %d does not exist", commentId))
                    .build();
        }
    }


    void sanitizeComment(CommentsRecord comment) {
        if (comment.getAuthor() != null) {
            comment.setAuthor(NO_HTML_POLICY.sanitize(comment.getAuthor()));
        }

        if (comment.getUrl() != null) {
            comment.setUrl(Encode.forHtmlAttribute(comment.getUrl()));
        }

        final String requestText = comment.getText();
        final String sanitizedText = inputSanitizer.sanitize(requestText);
        comment.setText(sanitizedText);
    }


    @DeleteMapping("/comments/{id}")
    public void deleteComment(
            @PathVariable("id") Long commentId,
            @RequestHeader(SIGNATURE_HEADER) String signature) {

        verifyValidRequest(signature, commentId);

        try {
            commentRepository.setStatus(commentId, CommentStatus.DELETED);

            logger.info("Marked comment with id = {} as DELETED", commentId);
        } catch (DataAccessException e) {
            throw RequestException.badRequest()
                    .message(String.format("Comment with id = %d does not exist", commentId))
                    .cause(e)
                    .build();
        }
    }

    private void verifyValidRequest(String signature, Long commentId) {

        final String[] signatureComponents = signature.split("\\|");

        try {

            Preconditions.checkArgument(signatureComponents.length == 3);

            final String identifier = signatureComponents[0];
            final String referenceIdentifier = getUriFromId(commentId).toString();

            Preconditions.checkArgument(identifier.equals(referenceIdentifier));

            final Instant expirationDate = Instant.parse(signatureComponents[1]);
            final byte[] signatureToken = Base64.getDecoder().decode(signatureComponents[2]);

            if (!requestVerifier.isRequestValid(identifier, expirationDate, signatureToken)) {
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

    private URI getUriFromId(long commentId) {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .pathSegment("{id}")
                .buildAndExpand(Collections.singletonMap("id", commentId)).toUri();
    }
}
