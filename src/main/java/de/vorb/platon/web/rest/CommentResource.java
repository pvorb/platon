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

package de.vorb.platon.web.rest;

import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.jooq.tables.records.ThreadsRecord;
import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.util.CommentFilters;
import de.vorb.platon.util.InputSanitizer;
import de.vorb.platon.web.rest.json.CommentJson;
import de.vorb.platon.web.rest.json.CommentListResultJson;

import com.google.common.base.Preconditions;
import org.jooq.exception.DataAccessException;
import org.owasp.encoder.Encode;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Path("comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommentResource {

    private static final Logger logger = LoggerFactory.getLogger(CommentResource.class);

    private static final String SIGNATURE_HEADER = "X-Signature";

    private static final PolicyFactory NO_HTML_POLICY = new HtmlPolicyBuilder().toFactory();


    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    private final RequestVerifier requestVerifier;
    private final InputSanitizer inputSanitizer;
    private final CommentFilters commentFilters;


    @Inject
    public CommentResource(
            ThreadRepository threadRepository,
            CommentRepository commentRepository,
            RequestVerifier requestVerifier,
            InputSanitizer inputSanitizer,
            CommentFilters commentFilters) {

        this.threadRepository = threadRepository;
        this.commentRepository = commentRepository;

        this.requestVerifier = requestVerifier;
        this.inputSanitizer = inputSanitizer;
        this.commentFilters = commentFilters;
    }


    @GET
    @Path("{id}")
    public CommentJson getComment(@NotNull @PathParam("id") long commentId) {

        final CommentsRecord comment = commentRepository.findById(commentId);

        if (comment == null || Enum.valueOf(CommentStatus.class, comment.getStatus()) != CommentStatus.PUBLIC) {
            throw new NotFoundException(String.format("No comment found with id = %d", commentId));
        } else {
            return new CommentJson(comment);
        }
    }


    @GET
    public CommentListResultJson findCommentsByThreadUrl(@NotNull @QueryParam("threadUrl") String threadUrl) {

        final List<CommentsRecord> comments = commentRepository.findByThreadUrl(threadUrl);
        if (comments.isEmpty()) {
            throw new NotFoundException(String.format("No thread found with url = '%s'", threadUrl));
        } else {
            final long totalCommentCount = comments.stream().filter(commentFilters::doesCommentCount).count();
            final List<CommentJson> topLevelComments = transformFlatCommentListToTree(comments);
            return new CommentListResultJson(totalCommentCount, topLevelComments);
        }
    }

    private static List<CommentJson> transformFlatCommentListToTree(List<CommentsRecord> comments) {

        final Map<Long, CommentJson> lookupMap = comments.stream()
                .map(CommentJson::new)
                .collect(Collectors.toMap(CommentJson::getId, Function.identity()));

        final List<CommentJson> topLevelComments = new ArrayList<>();
        comments.forEach(comment -> {
            final List<CommentJson> commentList;
            if (comment.getParentId() == null) {
                commentList = topLevelComments;
            } else {
                commentList = lookupMap.get(comment.getParentId()).getReplies();
            }
            commentList.add(lookupMap.get(comment.getId()));
        });

        return topLevelComments;
    }

    @POST
    public Response postComment(
            @NotNull @QueryParam("threadUrl") String threadUrl,
            @NotNull @QueryParam("threadTitle") String threadTitle,
            @NotNull CommentJson commentJson) {

        if (commentJson.getId() != null) {
            throw new BadRequestException("Comment id is not null");
        }

        Long threadId = threadRepository.findThreadIdForUrl(threadUrl);
        if (threadId == null) {

            final ThreadsRecord thread = new ThreadsRecord()
                    .setUrl(threadUrl)
                    .setTitle(threadTitle);

            threadId = threadRepository.insert(thread).getId();

            logger.info("Created new thread for url '{}'", threadUrl);
        }

        CommentsRecord comment = commentJson.toRecord();

        comment.setThreadId(threadId);
        comment.setCreationDate(Timestamp.from(Instant.now()));
        comment.setLastModificationDate(comment.getCreationDate());

        assertParentBelongsToSameThread(comment);

        sanitizeComment(comment);

        comment = commentRepository.insert(comment);

        logger.info("Posted new comment to thread '{}'", threadUrl);

        final URI commentUri = getUriFromId(comment.getId());

        final String identifier = commentUri.toString();
        final Instant expirationDate = comment.getCreationDate().toInstant().plus(24, ChronoUnit.HOURS);
        final byte[] signature = requestVerifier.getSignatureToken(identifier, expirationDate);

        return Response.created(commentUri)
                .header(SIGNATURE_HEADER, String.format("%s|%s|%s", identifier, expirationDate,
                        Base64.getEncoder().encodeToString(signature)))
                .entity(new CommentJson(comment))
                .build();
    }

    private void assertParentBelongsToSameThread(CommentsRecord comment) {

        final Long parentId = comment.getParentId();
        if (parentId == null) {
            return;
        }

        final CommentsRecord parentComment = commentRepository.findById(parentId);

        if (parentComment == null) {
            throw new BadRequestException("Parent comment does not exist");
        }

        if (!comment.getThreadId().equals(parentComment.getThreadId())) {
            throw new BadRequestException("Parent comment does not belong to same thread");
        }

    }


    @PUT
    @Path("{id}")
    public void updateComment(
            @NotNull @HeaderParam(SIGNATURE_HEADER) String signature,
            @NotNull @PathParam("id") Long commentId,
            @NotNull CommentJson commentJson) {

        if (!commentId.equals(commentJson.getId())) {
            throw new BadRequestException(
                    String.format("Comment ids do not match (%d != %d)", commentJson.getId(), commentId));
        }

        verifyValidRequest(signature, commentId);

        final CommentsRecord comment = commentRepository.findById(commentId);

        if (comment != null) {

            comment.setText(commentJson.getText());
            comment.setAuthor(comment.getAuthor());
            comment.setUrl(comment.getUrl());

            comment.setLastModificationDate(Timestamp.from(Instant.now()));

            sanitizeComment(comment);

            try {
                commentRepository.update(comment);
            } catch (DataAccessException e) {
                throw new ClientErrorException(Response.Status.CONFLICT, e);
            }

        } else {
            throw new BadRequestException(String.format("Comment with id = %d does not exist", commentId));
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


    @DELETE
    @Path("{id}")
    public void deleteComment(
            @NotNull @HeaderParam(SIGNATURE_HEADER) String signature,
            @NotNull @PathParam("id") Long commentId) {

        verifyValidRequest(signature, commentId);

        try {
            commentRepository.setStatus(commentId, CommentStatus.DELETED);

            logger.info("Marked comment with id = {} as DELETED", commentId);
        } catch (DataAccessException e) {
            throw new BadRequestException(String.format("Comment with id = %d does not exist", commentId));
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
                throw new BadRequestException("Authentication signature is invalid or has expired");
            }

        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new BadRequestException("Illegal authentication signature provided");
        }
    }

    private URI getUriFromId(long commentId) {
        return UriBuilder.fromResource(getClass()).segment("{id}").build(commentId);
    }

}
