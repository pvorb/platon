/*
 * Copyright 2016 the original author or authors.
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

import de.vorb.platon.model.Comment;
import de.vorb.platon.model.CommentThread;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.CommentThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.util.CommentFilters;
import de.vorb.platon.util.InputSanitizer;
import de.vorb.platon.web.rest.json.CommentJson;
import de.vorb.platon.web.rest.json.CommentListResultJson;

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

    private final CommentJson updateComment = new CommentJson(Comment.builder().id(42L).text("Text").build());
    private final String defaultRequestSignature = getSignature("comments/42", Instant.now());

    @Before
    public void setUp() throws Exception {

        Mockito.when(threadRepository.getByUrl(Mockito.eq(null))).thenReturn(null);

        Mockito.when(threadRepository.getByUrl(Mockito.eq(emptyThread.getUrl()))).thenReturn(emptyThread);

        Mockito.when(threadRepository.getByUrl(Mockito.eq(nonEmptyThread.getUrl()))).thenReturn(nonEmptyThread);

        final Random rng = new Random();
        Mockito.when(commentRepository.save(Mockito.any(Comment.class))).then(invocation -> {
            final Comment comment = invocation.getArgumentAt(0, Comment.class);
            if (comment.getId() == null) {
                comment.setId(rng.nextLong());
            }
            return comment;
        });

        Mockito.when(commentRepository.findByThread(nonEmptyThread)).thenReturn(nonEmptyThread.getComments());

        Mockito.when(requestVerifier.getSignatureToken(Mockito.any(), Mockito.any())).thenReturn(new byte[0]);
        Mockito.when(requestVerifier.isRequestValid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        commentResource = new CommentResource(threadRepository, commentRepository, requestVerifier, htmlInputSanitizer,
                new CommentFilters());
    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentsByThreadUrlNull() throws Exception {
        commentResource.findCommentsByThreadUrl(null);
    }

    @Test
    public void testGetCommentsByThreadUrlEmptyThread() throws Exception {
        final CommentListResultJson comments = commentResource.findCommentsByThreadUrl(emptyThread.getUrl());
        Truth.assertThat(comments.getTotalCommentCount()).isEqualTo(0);
        Truth.assertThat(comments.getComments()).isEmpty();
    }

    @Test
    public void testGetCommentsByThreadUrlNonEmptyThread() throws Exception {
        final CommentListResultJson comments = commentResource.findCommentsByThreadUrl(nonEmptyThread.getUrl());
        Truth.assertThat(comments.getTotalCommentCount()).isGreaterThan(0L);
        Truth.assertThat(comments.getComments()).isNotEmpty();
    }

    @Test
    public void testGetCommentById() throws Exception {

        final Comment comment = Mockito.mock(Comment.class);
        Mockito.when(comment.getStatus()).thenReturn(Comment.Status.PUBLIC);
        Mockito.when(commentRepository.findOne(Mockito.eq(4711L))).thenReturn(comment);

        Truth.assertThat(commentResource.getComment(4711L)).isEqualTo(new CommentJson(comment));

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

        commentResource.postComment(nonEmptyThread.getUrl(), nonEmptyThread.getTitle(), new CommentJson(newComment));

        final ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        Mockito.verify(commentRepository).save(commentCaptor.capture());

        Truth.assertThat(commentCaptor.getValue().getThread().getUrl()).isEqualTo(nonEmptyThread.getUrl());
    }

    @Test
    public void testPostCommentToNewThread() throws Exception {
        final String threadUrl = "http://example.com/new-article";
        final String threadTitle = "New article";
        final CommentJson newComment =
                Mockito.spy(new CommentJson(Comment.builder()
                        .text("Text")
                        .author("Author")
                        .build()));

        commentResource.postComment(threadUrl, threadTitle, newComment);

        final ArgumentCaptor<CommentThread> threadCaptor = ArgumentCaptor.forClass(CommentThread.class);
        Mockito.verify(threadRepository).save(threadCaptor.capture());

        Truth.assertThat(threadCaptor.getValue().getUrl()).isEqualTo(threadUrl);
        Truth.assertThat(threadCaptor.getValue().getTitle()).isEqualTo(threadTitle);

        final ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);

        Mockito.verify(commentRepository).save(commentCaptor.capture());

        Truth.assertThat(commentCaptor.getValue().getThread().getUrl()).isEqualTo(threadUrl);
    }

    @Test(expected = BadRequestException.class)
    public void testPostCommentWithId() throws Exception {
        final CommentJson comment = new CommentJson(Comment.builder().id(1337L).build());
        commentResource.postComment("http://example.com/article", "Article", comment);
    }

    @Test
    public void testUpdateComment() throws Exception {

        Mockito.when(commentRepository.exists(Mockito.eq(updateComment.getId()))).thenReturn(true);

        commentResource.updateComment(defaultRequestSignature, updateComment.getId(), updateComment);

        Mockito.verify(commentRepository).save(Mockito.eq(updateComment.toComment()));

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
    public void testDeleteCommentWithNonParsableDateInSignature() throws Exception {

        final String signatureWithNonParsableDate = "comments/42|2016-01-01 00:00:00|SGVsbG8gV29ybGQ=";
        commentResource.deleteComment(signatureWithNonParsableDate, 42L);

    }

    @Test(expected = BadRequestException.class)
    public void testDeleteCommentWithMismatchingIds() throws Exception {

        final long commentId = 42;
        final String signatureWithMismatchingId = "comments/43|2016-01-01T00:00:00.000Z|SGVsbG8gV29ybGQ=";

        commentResource.deleteComment(signatureWithMismatchingId, commentId);

    }

    @Test
    public void testSanitizeComment() throws Exception {

        final InputSanitizer inputSanitizer = Mockito.mock(InputSanitizer.class);

        final CommentResource commentResource = new CommentResource(
                Mockito.mock(CommentThreadRepository.class),
                Mockito.mock(CommentRepository.class),
                Mockito.mock(RequestVerifier.class),
                inputSanitizer,
                new CommentFilters()
        );

        final Comment comment = Comment.builder()
                .text("Some text")
                .url("http://example.com/article?param1=foo&param2=bar")
                .author("<a href=\"http://example.com/\">Sam</a>")
                .build();

        Mockito.when(inputSanitizer.sanitize(Mockito.eq(comment.getText()))).thenReturn(comment.getText());

        commentResource.sanitizeComment(comment);

        Mockito.verify(inputSanitizer).sanitize(Mockito.eq(comment.getText()));

        Truth.assertThat(comment.getAuthor()).isEqualTo("Sam");
        Truth.assertThat(comment.getUrl()).isEqualTo("http://example.com/article?param1=foo&amp;param2=bar");

    }

}
