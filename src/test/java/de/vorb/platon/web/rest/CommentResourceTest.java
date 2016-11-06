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

import com.google.common.truth.Truth;
import com.sun.mail.iap.Argument;
import org.jooq.exception.DataAccessException;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RunWith(MockitoJUnitRunner.class)
public class CommentResourceTest {

    private final ThreadsRecord nonEmptyThread =
            new ThreadsRecord()
                    .setId(1L)
                    .setUrl("http://example.com/article-with-comments")
                    .setTitle("A non-empty comment thread");

    private final List<CommentsRecord> nonEmptyThreadComments = new ArrayList<>();

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private RequestVerifier requestVerifier;

    private final InputSanitizer htmlInputSanitizer = String::trim;

    private CommentResource commentResource;

    private final CommentJson updateComment = new CommentJson(new CommentsRecord().setId(42L).setText("Text"));
    private final String defaultRequestSignature = getSignature("comments/42", Instant.now());

    @Before
    public void setUp() throws Exception {

        final CommentsRecord comment =
                new CommentsRecord()
                        .setId(1L)
                        .setThreadId(nonEmptyThread.getId())
                        .setText("Text")
                        .setAuthor("Author")
                        .setStatus(CommentStatus.PUBLIC.toString());

        nonEmptyThreadComments.add(comment);

        Mockito.when(commentRepository.findByThreadUrl(Mockito.isNull(String.class)))
                .thenReturn(Collections.emptyList());

        Mockito.when(threadRepository.findThreadIdForUrl(Mockito.anyString()))
                .thenReturn(null);
        Mockito.when(commentRepository.findByThreadUrl(Mockito.anyString()))
                .thenReturn(Collections.emptyList());

        Mockito.when(threadRepository.findThreadIdForUrl(Mockito.eq(nonEmptyThread.getUrl())))
                .thenReturn(nonEmptyThread.getId());
        Mockito.when(commentRepository.findByThreadUrl(Mockito.eq(nonEmptyThread.getUrl())))
                .thenReturn(nonEmptyThreadComments);

        final AtomicLong threadIdSequence = new AtomicLong(2);
        Mockito.when(threadRepository.insert(Mockito.any(ThreadsRecord.class))).then(invocation -> {
            final ThreadsRecord newThread = invocation.getArgumentAt(0, ThreadsRecord.class);
            if (newThread.getId() == null) {
                newThread.setId(threadIdSequence.getAndIncrement());
            }
            return newThread;
        });

        final AtomicLong commentIdSequence = new AtomicLong(2);
        Mockito.when(commentRepository.insert(Mockito.any(CommentsRecord.class))).then(invocation -> {
            final CommentsRecord newComment = invocation.getArgumentAt(0, CommentsRecord.class);
            if (newComment.getId() == null) {
                newComment.setId(commentIdSequence.getAndIncrement());
            }
            return newComment;
        });

        Mockito.when(requestVerifier.getSignatureToken(Mockito.any(), Mockito.any())).thenReturn(new byte[0]);
        Mockito.when(requestVerifier.isRequestValid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

        commentResource = new CommentResource(threadRepository, commentRepository, requestVerifier, htmlInputSanitizer,
                new CommentFilters());
    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentsByThreadUrlNull() throws Exception {
        commentResource.findCommentsByThreadUrl(null);
    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentsByUrlOfNonExistingThread() throws Exception {
        commentResource.findCommentsByThreadUrl("http://example.com/article-without-comments");
    }

    @Test
    public void testGetCommentsByThreadUrlNonEmptyThread() throws Exception {

        final CommentListResultJson comments = commentResource.findCommentsByThreadUrl(nonEmptyThread.getUrl());

        Truth.assertThat(comments.getTotalCommentCount()).isGreaterThan(0L);
        Truth.assertThat(comments.getComments()).isNotEmpty();
    }

    @Test
    public void testGetCommentById() throws Exception {

        final CommentsRecord comment = Mockito.mock(CommentsRecord.class);
        Mockito.when(comment.getStatus()).thenReturn(CommentStatus.PUBLIC.toString());
        Mockito.when(commentRepository.findById(Mockito.eq(4711L))).thenReturn(comment);

        Truth.assertThat(commentResource.getComment(4711L)).isEqualTo(new CommentJson(comment));
    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentAwaitingModerationById() throws Exception {

        final CommentsRecord comment = Mockito.mock(CommentsRecord.class);
        Mockito.when(comment.getStatus()).thenReturn(CommentStatus.AWAITING_MODERATION.toString());
        Mockito.when(commentRepository.findById(Mockito.eq(4711L))).thenReturn(comment);

        commentResource.getComment(4711L);
    }

    @Test(expected = NotFoundException.class)
    public void testGetDeletedCommentById() throws Exception {

        final CommentsRecord comment = Mockito.mock(CommentsRecord.class);
        Mockito.when(comment.getStatus()).thenReturn(CommentStatus.DELETED.toString());
        Mockito.when(commentRepository.findById(Mockito.eq(4711L))).thenReturn(comment);

        commentResource.getComment(4711L);
    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentIdNotFound() throws Exception {

        Mockito.when(commentRepository.findById(Mockito.anyLong())).thenReturn(null);

        commentResource.getComment(4711L);
    }

    @Test
    public void testPostCommentToExistingThread() throws Exception {

        final CommentJson newComment = new CommentJson();
        newComment.setText("Text");
        newComment.setAuthor("Author");

        commentResource.postComment(nonEmptyThread.getUrl(), nonEmptyThread.getTitle(), newComment);

        final ArgumentCaptor<CommentsRecord> commentCaptor = ArgumentCaptor.forClass(CommentsRecord.class);

        Mockito.verify(commentRepository).insert(commentCaptor.capture());

        Truth.assertThat(commentCaptor.getValue().getThreadId()).isEqualTo(nonEmptyThread.getId());
    }

    @Test
    public void testPostCommentToNewThread() throws Exception {
        final String threadUrl = "http://example.com/new-article";
        final String threadTitle = "New article";
        final CommentJson newComment =
                Mockito.spy(new CommentJson(new CommentsRecord()
                        .setText("Text")
                        .setAuthor("Author")));

        commentResource.postComment(threadUrl, threadTitle, newComment);

        final ArgumentCaptor<ThreadsRecord> threadCaptor = ArgumentCaptor.forClass(ThreadsRecord.class);
        Mockito.verify(threadRepository).insert(threadCaptor.capture());

        Truth.assertThat(threadCaptor.getValue().getUrl()).isEqualTo(threadUrl);
        Truth.assertThat(threadCaptor.getValue().getTitle()).isEqualTo(threadTitle);

        final ArgumentCaptor<CommentsRecord> commentCaptor = ArgumentCaptor.forClass(CommentsRecord.class);

        Mockito.verify(commentRepository).insert(commentCaptor.capture());

        Truth.assertThat(commentCaptor.getValue().getThreadId()).isEqualTo(threadCaptor.getValue().getId());
    }

    @Test(expected = BadRequestException.class)
    public void testPostCommentWithId() throws Exception {

        final CommentJson comment = new CommentJson(new CommentsRecord().setId(1337L));

        commentResource.postComment("http://example.com/article", "Article", comment);
    }

    @Test
    public void testUpdateComment() throws Exception {

        Mockito.when(commentRepository.findById(Mockito.eq(updateComment.getId())))
                .thenReturn(updateComment.toRecord());

        commentResource.updateComment(defaultRequestSignature, updateComment.getId(), updateComment);

        final ArgumentCaptor<CommentsRecord> commentCaptor = ArgumentCaptor.forClass(CommentsRecord.class);

        Mockito.verify(commentRepository).update(commentCaptor.capture());

        Truth.assertThat(commentCaptor.getValue().getId()).isEqualTo(updateComment.getId());
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateCommentWithMismatchingId() throws Exception {

        commentResource.updateComment(defaultRequestSignature, updateComment.getId() + 1, updateComment);
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateNonExistingComment() throws Exception {

        Mockito.when(commentRepository.findById(Mockito.eq(updateComment.getId()))).thenReturn(null);

        commentResource.updateComment(defaultRequestSignature, updateComment.getId(), updateComment);

    }

    @Test
    public void testDeleteCommentWithValidRequest() throws Exception {

        commentResource.deleteComment(defaultRequestSignature, 42L);

        Mockito.verify(commentRepository).setStatus(Mockito.eq(42L), Mockito.refEq(CommentStatus.DELETED));
    }

    @Test(expected = BadRequestException.class)
    public void testDeleteNonExistingComment() throws Exception {

        Mockito.doThrow(DataAccessException.class)
                .when(commentRepository).setStatus(Mockito.eq(42L), Mockito.refEq(CommentStatus.DELETED));

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
                Mockito.mock(ThreadRepository.class),
                Mockito.mock(CommentRepository.class),
                Mockito.mock(RequestVerifier.class),
                inputSanitizer,
                new CommentFilters()
        );

        final CommentsRecord comment = new CommentsRecord()
                .setText("Some text")
                .setUrl("http://example.com/article?param1=foo&param2=bar")
                .setAuthor("<a href=\"http://example.com/\">Sam</a>");

        Mockito.when(inputSanitizer.sanitize(Mockito.eq(comment.getText()))).thenReturn(comment.getText());

        commentResource.sanitizeComment(comment);

        Mockito.verify(inputSanitizer).sanitize(Mockito.eq(comment.getText()));

        Truth.assertThat(comment.getAuthor()).isEqualTo("Sam");
        Truth.assertThat(comment.getUrl()).isEqualTo("http://example.com/article?param1=foo&amp;param2=bar");
    }

}
