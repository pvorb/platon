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
import de.vorb.platon.security.SignatureComponents;
import de.vorb.platon.web.api.errors.RequestException;
import de.vorb.platon.web.api.json.CommentJson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentControllerPostTest extends CommentControllerTest {

    private static final AtomicLong COMMENT_ID_SEQUENCE = new AtomicLong();

    @Mock
    private CommentJson commentJson;

    @Spy
    private CommentsRecord commentsRecord = new CommentsRecord();

    @Test
    public void createsNewThreadOnDemand() throws Exception {

        insertCommentReturnsCommentWithNextId();

        when(commentJson.getId()).thenReturn(null);
        when(threadRepository.findThreadIdForUrl(any())).thenReturn(Optional.empty());
        when(threadRepository.insert(any())).thenReturn(new ThreadsRecord().setId(1L));
        convertCommentJsonToRecord(commentJson, commentsRecord);
        when(commentsRecord.getParentId()).thenReturn(null);
        when(signatureCreator.createSignatureComponents(any(), any())).thenReturn(mock(SignatureComponents.class));

        when(commentUriResolver.createRelativeCommentUriForId(anyLong()))
                .thenAnswer(invocation -> new URI("/api/comments/" + invocation.getArgumentAt(0, long.class)));

        final ResponseEntity<CommentJson> response =
                commentController.postComment(THREAD_URL, THREAD_TITLE, commentJson);
        verify(threadRepository).insert(eq(new ThreadsRecord(null, THREAD_URL, THREAD_TITLE)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThatLocationHeaderIsCorrect(response.getHeaders().getLocation());
    }

    private void assertThatLocationHeaderIsCorrect(URI location) {
        assertThat(location).hasScheme(null);
        assertThat(location).hasHost(null);
        assertThat(location).hasNoPort();
        assertThat(location).hasPath("/api/comments/" + commentsRecord.getId());
        assertThat(location).hasNoParameters();
    }

    @Test
    public void postCommentWithIdThrowsBadRequestException() throws Exception {
        when(commentJson.getId()).thenReturn(1337L);

        assertThatExceptionOfType(RequestException.class)
                .isThrownBy(() -> commentController.postComment(THREAD_URL, THREAD_TITLE, commentJson))
                .matches(exception -> exception.getHttpStatus() == HttpStatus.BAD_REQUEST)
                .withMessage("Comment ID is not null");
    }

    private void insertCommentReturnsCommentWithNextId() {
        when(commentRepository.insert(any()))
                .then(invocation -> {
                    final CommentsRecord insertedComment = (CommentsRecord) invocation.getArguments()[0];
                    final long nextCommentId = COMMENT_ID_SEQUENCE.incrementAndGet();
                    return insertedComment.setId(nextCommentId);
                });
    }

}
