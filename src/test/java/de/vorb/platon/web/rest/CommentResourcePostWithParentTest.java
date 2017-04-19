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
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.util.CommentConverter;
import de.vorb.platon.util.CommentFilters;
import de.vorb.platon.util.InputSanitizer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentResourcePostWithParentTest {

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private CommentRepository commentRepository;

    private final CommentConverter commentConverter = new CommentConverter();

    @Mock
    private RequestVerifier requestVerifier;

    private final InputSanitizer htmlInputSanitizer = String::trim;

    private CommentResource commentResource;


    private ThreadsRecord thread;


    private CommentsRecord existingParentComment;

    private CommentsRecord nonExistingParentComment;

    private CommentsRecord existingParentFromOtherThread;


    @Before
    public void setUp() throws Exception {

        when(requestVerifier.getSignatureToken(Mockito.any(), Mockito.any())).thenReturn(new byte[0]);
        when(requestVerifier.isRequestValid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);


        final long threadId = 42;
        thread =
                Mockito.spy(new ThreadsRecord()
                        .setUrl("http://example.com/article")
                        .setTitle("Article"));

        when(thread.getId()).thenReturn(threadId);

        when(threadRepository.findThreadIdForUrl(Mockito.eq(thread.getUrl()))).thenReturn(threadId);


        final long existingParentId = 4711;
        existingParentComment =
                Mockito.spy(new CommentsRecord()
                        .setThreadId(thread.getId())
                        .setText("Existing parent"));
        when(existingParentComment.getId()).thenReturn(existingParentId);

        when(commentRepository.findById(Mockito.eq(existingParentId))).thenReturn(existingParentComment);


        final long nonExistingParentId = 4712;
        nonExistingParentComment =
                Mockito.spy(new CommentsRecord()
                        .setThreadId(thread.getId())
                        .setText("Non-existing parent"));
        when(nonExistingParentComment.getId()).thenReturn(nonExistingParentId);

        when(commentRepository.findById(Mockito.eq(nonExistingParentId))).thenReturn(null);


        final long otherThreadId = 43;

        final long existingParentFromOtherThreadId = 4713;
        existingParentFromOtherThread =
                Mockito.spy(new CommentsRecord()
                        .setThreadId(otherThreadId)
                        .setText("Existing parent from other thread"));
        when(existingParentFromOtherThread.getId()).thenReturn(existingParentFromOtherThreadId);

        when(commentRepository.findById(Mockito.eq(existingParentFromOtherThreadId)))
                .thenReturn(existingParentFromOtherThread);

        commentResource = new CommentResource(threadRepository, commentRepository, commentConverter, requestVerifier,
                htmlInputSanitizer, new CommentFilters());
    }

    @Test
    public void testWithExistingParent() throws Exception {

        final CommentsRecord newChildComment =
                new CommentsRecord()
                        .setParentId(existingParentComment.getId())
                        .setText("Child");

        when(commentRepository.insert(Mockito.any(CommentsRecord.class))).then(invocation -> {
            CommentsRecord comment = invocation.getArgumentAt(0, CommentsRecord.class);
            comment.setId(4711L);
            return comment;
        });

        final Response response = commentResource.postComment(thread.getUrl(), thread.getTitle(),
                commentConverter.convertRecordToJson(newChildComment));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test(expected = BadRequestException.class)
    public void testWithNonExistingParent() throws Exception {

        final CommentsRecord newChildComment =
                new CommentsRecord()
                        .setParentId(nonExistingParentComment.getId())
                        .setText("Child");

        when(commentRepository.insert(Mockito.eq(newChildComment))).then(invocation -> {
            newChildComment.setId(4711L);
            return newChildComment;
        });

        commentResource.postComment(thread.getUrl(), thread.getTitle(),
                commentConverter.convertRecordToJson(newChildComment));
    }

    @Test(expected = BadRequestException.class)
    public void testWithParentFromOtherThread() throws Exception {

        final CommentsRecord newChildComment =
                new CommentsRecord()
                        .setParentId(existingParentFromOtherThread.getId())
                        .setText("Child");

        commentResource.postComment(thread.getUrl(), thread.getTitle(),
                commentConverter.convertRecordToJson(newChildComment));
    }
}
