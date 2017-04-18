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
import de.vorb.platon.util.CommentConverter;
import de.vorb.platon.util.CommentFilters;
import de.vorb.platon.util.InputSanitizer;
import de.vorb.platon.web.rest.json.CommentJson;
import de.vorb.platon.web.rest.json.CommentListResultJson;

import org.jooq.exception.DataAccessException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private final CommentConverter commentConverter = new CommentConverter();

    @Mock
    private RequestVerifier requestVerifier;

    private final InputSanitizer htmlInputSanitizer = String::trim;

    private CommentResource commentResource;

    private final CommentJson updateComment = CommentJson.builder().id(42L).text("Text").build();
    private final String defaultRequestSignature = getSignature("comments/42", Instant.now());

    @Before
    public void setUp() throws Exception {

        final CommentsRecord comment =
                new CommentsRecord()
                        .setId(1L)
                        .setThreadId(nonEmptyThread.getId())
                        .setText("Text")
                        .setAuthor("Author")
                        .setEmailHash(
                                Base64.getEncoder().encodeToString("test@example.org".getBytes(StandardCharsets.UTF_8)))
                        .setStatus(CommentStatus.PUBLIC.toString());

        nonEmptyThreadComments.add(comment);

        when(commentRepository.findByThreadUrl(isNull(String.class)))
                .thenReturn(Collections.emptyList());

        when(threadRepository.findThreadIdForUrl(any()))
                .thenReturn(null);
        when(commentRepository.findByThreadUrl(any()))
                .thenReturn(Collections.emptyList());

        when(threadRepository.findThreadIdForUrl(eq(nonEmptyThread.getUrl())))
                .thenReturn(nonEmptyThread.getId());
        when(commentRepository.findByThreadUrl(eq(nonEmptyThread.getUrl())))
                .thenReturn(nonEmptyThreadComments);

        final AtomicLong threadIdSequence = new AtomicLong(2);
        when(threadRepository.insert(any(ThreadsRecord.class))).then(invocation -> {
            final ThreadsRecord newThread = invocation.getArgumentAt(0, ThreadsRecord.class);
            if (newThread.getId() == null) {
                newThread.setId(threadIdSequence.getAndIncrement());
            }
            return newThread;
        });

        final AtomicLong commentIdSequence = new AtomicLong(2);
        when(commentRepository.insert(any(CommentsRecord.class))).then(invocation -> {
            final CommentsRecord newComment = invocation.getArgumentAt(0, CommentsRecord.class);
            if (newComment.getId() == null) {
                newComment.setId(commentIdSequence.getAndIncrement());
            }
            return newComment;
        });

        when(requestVerifier.getSignatureToken(any(), any())).thenReturn(new byte[0]);
        when(requestVerifier.isRequestValid(any(), any(), any())).thenReturn(true);

        commentResource = new CommentResource(threadRepository, commentRepository, commentConverter, requestVerifier,
                htmlInputSanitizer, new CommentFilters());
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

        assertThat(comments.getTotalCommentCount()).isGreaterThan(0L);
        assertThat(comments.getComments()).isNotEmpty();
    }

    @Test
    public void testGetCommentById() throws Exception {

        final CommentsRecord comment = mock(CommentsRecord.class);
        when(comment.getStatus()).thenReturn(CommentStatus.PUBLIC.toString());
        when(commentRepository.findById(eq(4711L))).thenReturn(comment);

        assertThat(commentResource.getComment(4711L)).isEqualTo(commentConverter.convertRecordToJson(comment));
    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentAwaitingModerationById() throws Exception {

        final CommentsRecord comment = mock(CommentsRecord.class);
        when(comment.getStatus()).thenReturn(CommentStatus.AWAITING_MODERATION.toString());
        when(commentRepository.findById(eq(4711L))).thenReturn(comment);

        commentResource.getComment(4711L);
    }

    @Test(expected = NotFoundException.class)
    public void testGetDeletedCommentById() throws Exception {

        final CommentsRecord comment = mock(CommentsRecord.class);
        when(comment.getStatus()).thenReturn(CommentStatus.DELETED.toString());
        when(commentRepository.findById(eq(4711L))).thenReturn(comment);

        commentResource.getComment(4711L);
    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentIdNotFound() throws Exception {

        when(commentRepository.findById(anyLong())).thenReturn(null);

        commentResource.getComment(4711L);
    }

    @Test
    public void testPostCommentToExistingThread() throws Exception {

        final CommentJson newComment = CommentJson.builder()
                .text("Text")
                .author("Author")
                .build();

        commentResource.postComment(nonEmptyThread.getUrl(), nonEmptyThread.getTitle(), newComment);

        final ArgumentCaptor<CommentsRecord> commentCaptor = ArgumentCaptor.forClass(CommentsRecord.class);

        verify(commentRepository).insert(commentCaptor.capture());

        assertThat(commentCaptor.getValue().getThreadId()).isEqualTo(nonEmptyThread.getId());
    }

    @Test
    public void testPostCommentToNewThread() throws Exception {
        final String threadUrl = "http://example.com/new-article";
        final String threadTitle = "New article";
        final CommentJson newComment =
                spy(CommentJson.builder()
                        .text("Text")
                        .author("Author")
                        .build());

        commentResource.postComment(threadUrl, threadTitle, newComment);

        final ArgumentCaptor<ThreadsRecord> threadCaptor = ArgumentCaptor.forClass(ThreadsRecord.class);
        verify(threadRepository).insert(threadCaptor.capture());

        assertThat(threadCaptor.getValue().getUrl()).isEqualTo(threadUrl);
        assertThat(threadCaptor.getValue().getTitle()).isEqualTo(threadTitle);

        final ArgumentCaptor<CommentsRecord> commentCaptor = ArgumentCaptor.forClass(CommentsRecord.class);

        verify(commentRepository).insert(commentCaptor.capture());

        assertThat(commentCaptor.getValue().getThreadId()).isEqualTo(threadCaptor.getValue().getId());
    }

    @Test(expected = BadRequestException.class)
    public void testPostCommentWithId() throws Exception {

        final CommentJson comment = CommentJson.builder()
                .id(1337L)
                .build();

        commentResource.postComment("http://example.com/article", "Article", comment);
    }

    @Test
    public void testUpdateComment() throws Exception {

        when(commentRepository.findById(eq(updateComment.getId())))
                .thenReturn(commentConverter.convertJsonToRecord(updateComment));

        commentResource.updateComment(defaultRequestSignature, updateComment.getId(), updateComment);

        final ArgumentCaptor<CommentsRecord> commentCaptor = ArgumentCaptor.forClass(CommentsRecord.class);

        verify(commentRepository).update(commentCaptor.capture());

        assertThat(commentCaptor.getValue().getId()).isEqualTo(updateComment.getId());
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateCommentWithMismatchingId() throws Exception {

        commentResource.updateComment(defaultRequestSignature, updateComment.getId() + 1, updateComment);
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateNonExistingComment() throws Exception {

        when(commentRepository.findById(eq(updateComment.getId()))).thenReturn(null);

        commentResource.updateComment(defaultRequestSignature, updateComment.getId(), updateComment);

    }

    @Test
    public void testDeleteCommentWithValidRequest() throws Exception {

        commentResource.deleteComment(defaultRequestSignature, 42L);

        verify(commentRepository).setStatus(eq(42L), refEq(CommentStatus.DELETED));
    }

    @Test(expected = BadRequestException.class)
    public void testDeleteNonExistingComment() throws Exception {

        doThrow(DataAccessException.class)
                .when(commentRepository).setStatus(eq(42L), refEq(CommentStatus.DELETED));

        commentResource.deleteComment(defaultRequestSignature, 42L);
    }

    @Test(expected = BadRequestException.class)
    public void testDeleteCommentWithInvalidRequest() throws Exception {

        final String identifier = "comments/42";
        final Instant expirationDate = Instant.now();

        final String signature = getSignature(identifier, expirationDate);

        when(requestVerifier.isRequestValid(eq(identifier), eq(expirationDate), any()))
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

        final InputSanitizer inputSanitizer = mock(InputSanitizer.class);

        final CommentResource commentResource = new CommentResource(
                mock(ThreadRepository.class),
                mock(CommentRepository.class),
                commentConverter,
                mock(RequestVerifier.class),
                inputSanitizer,
                new CommentFilters()
        );

        final CommentsRecord comment = new CommentsRecord()
                .setText("Some text")
                .setUrl("http://example.com/article?param1=foo&param2=bar")
                .setAuthor("<a href=\"http://example.com/\">Sam</a>");

        when(inputSanitizer.sanitize(eq(comment.getText()))).thenReturn(comment.getText());

        commentResource.sanitizeComment(comment);

        verify(inputSanitizer).sanitize(eq(comment.getText()));

        assertThat(comment.getAuthor()).isEqualTo("Sam");
        assertThat(comment.getUrl()).isEqualTo("http://example.com/article?param1=foo&amp;param2=bar");
    }

    @Test(expected = ClientErrorException.class)
    public void dataAccessException() throws Exception {

        when(commentRepository.findById(eq(updateComment.getId())))
                .thenReturn(commentConverter.convertJsonToRecord(updateComment));

        doThrow(DataAccessException.class).when(commentRepository).update(any());

        commentResource.updateComment(defaultRequestSignature, updateComment.getId(), updateComment);
    }
}
