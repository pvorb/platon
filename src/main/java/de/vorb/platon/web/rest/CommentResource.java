package de.vorb.platon.web.rest;

import de.vorb.platon.model.Comment;
import de.vorb.platon.model.CommentThread;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.CommentThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.util.InputSanitizer;

import com.google.common.base.Preconditions;
import org.owasp.encoder.Encode;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
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
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@Component
@Path("comments")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommentResource {

    private static final Logger logger = LoggerFactory.getLogger(CommentResource.class);

    private static final String SIGNATURE_HEADER = "X-Signature";

    private static final PolicyFactory NO_HTML_POLICY = new HtmlPolicyBuilder().toFactory();


    private final CommentThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    private final RequestVerifier requestVerifier;
    private final InputSanitizer inputSanitizer;


    @Inject
    public CommentResource(CommentThreadRepository threadRepository, CommentRepository commentRepository,
            RequestVerifier requestVerifier, InputSanitizer inputSanitizer) {

        this.threadRepository = threadRepository;
        this.commentRepository = commentRepository;

        this.requestVerifier = requestVerifier;
        this.inputSanitizer = inputSanitizer;
    }


    @GET
    @Path("{id}")
    @Transactional(readOnly = true)
    public Comment getComment(
            @NotNull @PathParam("id") Long commentId) {

        final Comment comment = commentRepository.findOne(commentId);

        if (comment == null || comment.isDeleted()) {
            throw new NotFoundException(String.format("No comment found with id = %d", commentId));
        } else {
            return comment;
        }
    }


    @GET
    @Transactional(readOnly = true)
    public List<Comment> findCommentsByThreadUrl(
            @NotNull @QueryParam("threadUrl") String threadUrl) {

        final CommentThread thread = threadRepository.getByUrl(threadUrl);
        if (thread == null) {
            throw new NotFoundException(String.format("No thread found with url = '%s'", threadUrl));
        } else {
            return commentRepository.findByThread(thread);
        }
    }


    @POST
    @Transactional
    public Response postComment(
            @NotNull @QueryParam("threadUrl") String threadUrl,
            @NotNull @QueryParam("title") String title,
            @NotNull Comment comment) {

        if (comment.getId() != null) {
            throw new BadRequestException("Comment id is not null");
        }

        CommentThread thread = threadRepository.getByUrl(threadUrl);
        if (thread == null) {

            thread =
                    CommentThread.builder()
                            .url(threadUrl)
                            .title(title)
                            .build();

            threadRepository.save(thread);

            logger.info("Created new {}", thread);
        }

        comment.setThread(thread);
        comment.setCreationDate(Instant.now());
        comment.setModificationDate(comment.getCreationDate());

        assertParentBelongsToSameThread(comment);

        sanitizeComment(comment);

        commentRepository.save(comment);

        logger.info("Posted new comment to {}", thread);

        final URI commentUri = getUriFromId(comment.getId());

        final String identifier = commentUri.toString();
        final Instant expirationDate = comment.getCreationDate().plus(2, ChronoUnit.HOURS);
        final byte[] signature = requestVerifier.getSignatureToken(identifier, expirationDate);

        return Response.created(commentUri)
                .header(SIGNATURE_HEADER, String.format("%s|%s|%s", identifier, expirationDate,
                        Base64.getEncoder().encodeToString(signature)))
                .entity(comment)
                .build();
    }

    @Transactional(readOnly = true)
    protected void assertParentBelongsToSameThread(Comment comment) {

        final Long parentId = comment.getParentId();
        if (parentId == null) {
            return;
        }

        final Comment parentComment = commentRepository.findOne(parentId);

        if (parentComment == null) {
            throw new BadRequestException("Parent comment does not exist");
        }

        final boolean parentBelongsToSameThread = comment.getThread().equalsById(parentComment.getThread());

        if (!parentBelongsToSameThread) {
            throw new BadRequestException("Parent comment does not belong to same thread");
        }

    }


    @PUT
    @Path("{id}")
    @Transactional(noRollbackFor = BadRequestException.class)
    public void updateComment(
            @NotNull @HeaderParam(SIGNATURE_HEADER) String signature,
            @NotNull @PathParam("id") Long commentId,
            @NotNull Comment comment) {

        if (!commentId.equals(comment.getId())) {
            throw new BadRequestException(
                    String.format("Comment ids do not match (%d != %d)", comment.getId(), commentId));
        }

        verifyValidRequest(signature, commentId);

        if (commentRepository.exists(commentId)) {

            sanitizeComment(comment);

            commentRepository.save(comment);

        } else {
            throw new BadRequestException(String.format("Comment with id = %d does not exist", commentId));
        }
    }


    protected void sanitizeComment(Comment comment) {
        comment.setAuthor(NO_HTML_POLICY.sanitize(comment.getAuthor()));
        comment.setUrl(Encode.forHtmlAttribute(comment.getUrl()));

        final String requestText = comment.getText();
        final String sanitizedText = inputSanitizer.sanitize(requestText);
        comment.setText(sanitizedText);
    }


    @DELETE
    @Path("{id}")
    @Transactional
    public void deleteComment(
            @NotNull @HeaderParam(SIGNATURE_HEADER) String signature,
            @NotNull @PathParam("id") Long commentId) {

        verifyValidRequest(signature, commentId);

        if (commentRepository.exists(commentId)) {

            commentRepository.markAsDeleted(commentId);
            logger.info("Deleted comment with id = {}", commentId);

        } else {
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
            final byte[] signatureToken =
                    Base64.getDecoder().decode(signatureComponents[2].getBytes(StandardCharsets.UTF_8));

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
