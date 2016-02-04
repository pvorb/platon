package de.vorb.platon.web.rest;

import de.vorb.platon.model.Comment;
import de.vorb.platon.model.CommentThread;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.CommentThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.util.InputSanitizer;

import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class CommentResourceTest {

    private static final CommentThread emptyThread =
            CommentThread.builder()
                    .url("http://example.com/article")
                    .title("An empty comment thread")
                    .build();

    private static final CommentThread nonEmptyThread =
            CommentThread.builder()
                    .url("http://example.com/article-with-comments")
                    .title("A non-empty comment thread")
                    .build();

    static {
        final Comment comment =
                Comment.builder()
                        .id(4711L)
                        .thread(nonEmptyThread)
                        .text("Text")
                        .author("Author")
                        .build();

        nonEmptyThread.getComments().add(comment);
    }

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentThreadRepository threadRepository;

    @Mock
    private RequestVerifier requestVerifier;

    private final InputSanitizer htmlInputSanitizer = String::trim;

    private CommentResource commentResource;

    private final Comment updateComment = Comment.builder().id(42L).text("Text").build();
    private final String defaultRequestSignature = getSignature("comments/42", Instant.now());

    @Before
    public void setUp() throws Exception {

        Mockito.when(threadRepository.getByUrl(Mockito.eq(null))).thenReturn(null);

        Mockito.when(threadRepository.getByUrl(Mockito.eq(emptyThread.getUrl()))).thenReturn(emptyThread);

        Mockito.when(threadRepository.getByUrl(Mockito.eq(nonEmptyThread.getUrl()))).thenReturn(nonEmptyThread);

        final Random rng = new Random();
        Mockito.when(commentRepository.save(Mockito.any(Comment.class))).then(invocation -> {
            final Comment comment = invocation.getArgumentAt(0, Comment.class);
            comment.setId(rng.nextLong());
            return comment;
        });

        Mockito.when(commentRepository.findByThread(nonEmptyThread)).thenReturn(nonEmptyThread.getComments());

        Mockito.when(requestVerifier.getSignatureToken(Mockito.any(), Mockito.any())).thenReturn(new byte[0]);
        Mockito.when(requestVerifier.isRequestValid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        commentResource = new CommentResource(threadRepository, commentRepository, requestVerifier, htmlInputSanitizer);
    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentsByThreadUrlNull() throws Exception {
        commentResource.findCommentsByThreadUrl(null);
    }

    @Test
    public void testGetCommentsByThreadUrlEmptyThread() throws Exception {
        Truth.assertThat(commentResource.findCommentsByThreadUrl(emptyThread.getUrl())).isEmpty();
    }

    @Test
    public void testGetCommentsByThreadUrlNonEmptyThread() throws Exception {
        final List<Comment> comments = commentResource.findCommentsByThreadUrl(nonEmptyThread.getUrl());
        Truth.assertThat(comments).isNotEmpty();
    }

    @Test
    public void testGetCommentById() throws Exception {

        final Comment comment = Mockito.mock(Comment.class);
        Mockito.when(comment.getStatus()).thenReturn(Comment.Status.PUBLIC);
        Mockito.when(commentRepository.findOne(Mockito.eq(4711L))).thenReturn(comment);

        Truth.assertThat(commentResource.getComment(4711L)).isSameAs(comment);

    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentAwaitingModerationById() throws Exception {

        final Comment comment = Mockito.mock(Comment.class);
        Mockito.when(comment.getStatus()).thenReturn(Comment.Status.AWAITING_MODERATION);
        Mockito.when(commentRepository.findOne(Mockito.eq(4711L))).thenReturn(comment);

        commentResource.getComment(4711L);

    }

    @Test(expected = NotFoundException.class)
    public void testGetDeletedCommentById() throws Exception {

        final Comment comment = Mockito.mock(Comment.class);
        Mockito.when(comment.getStatus()).thenReturn(Comment.Status.DELETED);
        Mockito.when(commentRepository.findOne(Mockito.eq(4711L))).thenReturn(comment);

        commentResource.getComment(4711L);

    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentIdNotFound() throws Exception {

        Mockito.when(commentRepository.findOne(Mockito.anyLong())).thenReturn(null);
        commentResource.getComment(4711L);

    }

    @Test
    public void testPostCommentToExistingThread() throws Exception {
        final Comment newComment =
                Mockito.spy(Comment.builder()
                        .thread(nonEmptyThread)
                        .text("Text")
                        .author("Author")
                        .build());

        commentResource.postComment(nonEmptyThread.getUrl(), nonEmptyThread.getTitle(), newComment);

        Mockito.verify(commentRepository).save(Mockito.same(newComment));
    }

    @Test
    public void testPostCommentToNewThread() throws Exception {
        final String threadUrl = "http://example.com/new-article";
        final String threadTitle = "New article";
        final Comment newComment =
                Mockito.spy(Comment.builder()
                        .text("Text")
                        .author("Author")
                        .build());

        commentResource.postComment(threadUrl, threadTitle, newComment);

        final ArgumentCaptor<CommentThread> threadCaptor = ArgumentCaptor.forClass(CommentThread.class);
        Mockito.verify(threadRepository).save(threadCaptor.capture());

        Truth.assertThat(threadCaptor.getValue().getUrl()).isEqualTo(threadUrl);
        Truth.assertThat(threadCaptor.getValue().getTitle()).isEqualTo(threadTitle);

        Mockito.verify(commentRepository).save(Mockito.same(newComment));
    }

    @Test
    public void testUpdateComment() throws Exception {

        Mockito.when(commentRepository.exists(Mockito.eq(updateComment.getId()))).thenReturn(true);

        commentResource.updateComment(defaultRequestSignature, updateComment.getId(), updateComment);

        Mockito.verify(commentRepository).save(updateComment);

    }

    @Test(expected = BadRequestException.class)
    public void testUpdateCommentWithMismatchingId() throws Exception {

        commentResource.updateComment(defaultRequestSignature, updateComment.getId() + 1, updateComment);

    }

    @Test(expected = BadRequestException.class)
    public void testUpdateNonExistingComment() throws Exception {

        Mockito.when(commentRepository.exists(Mockito.eq(updateComment.getId()))).thenReturn(false);

        commentResource.updateComment(defaultRequestSignature, updateComment.getId(), updateComment);

    }

    @Test
    public void testDeleteCommentWithValidRequest() throws Exception {

        Mockito.when(commentRepository.exists(42L)).thenReturn(true);

        commentResource.deleteComment(defaultRequestSignature, 42L);

        Mockito.verify(commentRepository).setStatus(Mockito.eq(42L), Mockito.eq(Comment.Status.DELETED));

    }

    @Test(expected = BadRequestException.class)
    public void testDeleteNonExistingComment() throws Exception {

        Mockito.when(commentRepository.exists(42L)).thenReturn(false);

        commentResource.deleteComment(defaultRequestSignature, 42L);

    }

    @Test(expected = BadRequestException.class)
    public void testDeleteCommentWithInvalidRequest() throws Exception {

        final String identifier = "comments/42";
        final Instant expirationDate = Instant.now();

        final String signature = getSignature(identifier, expirationDate);

        Mockito.when(requestVerifier.isRequestValid(Mockito.eq(identifier), Mockito.eq(expirationDate), Mockito.any()))
                .thenReturn(false);

        commentResource.deleteComment(signature, 42L);

    }

    private String getSignature(String identifier, Instant expirationDate) {
        return String.format("%s|%s|%s", identifier, expirationDate,
                Base64.getEncoder().encodeToString("token".getBytes()));
    }

    @Test(expected = BadRequestException.class)
    public void testDeleteCommentWithInvalidNumberOfComponentsInSignature() throws Exception {

        final String signatureWithInvalidNumberOfComponents = "comments/42";
        commentResource.deleteComment(signatureWithInvalidNumberOfComponents, 42L);

    }

    @Test(expected = BadRequestException.class)
    public void testDeleteCommentWithInvalidBase64EncodedToken() throws Exception {

        final String signatureWithBadBase64Encoding = "comments/42|2016-01-01T00:00:00.000Z|SGVsbG8gV29ybGQ==";
        commentResource.deleteComment(signatureWithBadBase64Encoding, 42L);

    }

    @Test(expected = BadRequestException.class)
    public void testDeleteCommentWithNonParseableDateInSignature() throws Exception {

        final String signatureWithNonParseableDate = "comments/42|2016-01-01 00:00:00|SGVsbG8gV29ybGQ=";
        commentResource.deleteComment(signatureWithNonParseableDate, 42L);

    }

    @Test(expected = BadRequestException.class)
    public void testDeleteCommentWithMismatchingIds() throws Exception {

        final long commentId = 42;
        final String signatureWithMismatchingId = "comments/43|2016-01-01T00:00:00.000Z|SGVsbG8gV29ybGQ=";

        commentResource.deleteComment(signatureWithMismatchingId, commentId);

    }
}
