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
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.util.CommentFilters;
import de.vorb.platon.util.InputSanitizer;
import de.vorb.platon.web.rest.json.CommentJson;

import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

@RunWith(MockitoJUnitRunner.class)
public class CommentResourcePostWithParentTest {

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private CommentRepository commentRepository;

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

        Mockito.when(requestVerifier.getSignatureToken(Mockito.any(), Mockito.any())).thenReturn(new byte[0]);
        Mockito.when(requestVerifier.isRequestValid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);


        final long threadId = 42;
        thread =
                Mockito.spy(new ThreadsRecord()
                        .setUrl("http://example.com/article")
                        .setTitle("Article"));

        Mockito.when(thread.getId()).thenReturn(threadId);

        Mockito.when(threadRepository.findThreadIdForUrl(Mockito.eq(thread.getUrl()))).thenReturn(threadId);


        final long existingParentId = 4711;
        existingParentComment =
                Mockito.spy(new CommentsRecord()
                        .setThreadId(thread.getId())
                        .setText("Existing parent"));
        Mockito.when(existingParentComment.getId()).thenReturn(existingParentId);

        Mockito.when(commentRepository.findById(Mockito.eq(existingParentId))).thenReturn(existingParentComment);


        final long nonExistingParentId = 4712;
        nonExistingParentComment =
                Mockito.spy(new CommentsRecord()
                        .setThreadId(thread.getId())
                        .setText("Non-existing parent"));
        Mockito.when(nonExistingParentComment.getId()).thenReturn(nonExistingParentId);

        Mockito.when(commentRepository.findById(Mockito.eq(nonExistingParentId))).thenReturn(null);


        final long otherThreadId = 43;

        final long existingParentFromOtherThreadId = 4713;
        existingParentFromOtherThread =
                Mockito.spy(new CommentsRecord()
                        .setThreadId(otherThreadId)
                        .setText("Existing parent from other thread"));
        Mockito.when(existingParentFromOtherThread.getId()).thenReturn(existingParentFromOtherThreadId);

        Mockito.when(commentRepository.findById(Mockito.eq(existingParentFromOtherThreadId)))
                .thenReturn(existingParentFromOtherThread);

        commentResource = new CommentResource(threadRepository, commentRepository, requestVerifier, htmlInputSanitizer,
                new CommentFilters());
    }

    @Test
    public void testWithExistingParent() throws Exception {

        final CommentsRecord newChildComment =
                new CommentsRecord()
                        .setParentId(existingParentComment.getId())
                        .setText("Child");

        Mockito.when(commentRepository.insert(Mockito.any(CommentsRecord.class))).then(invocation -> {
            CommentsRecord comment = invocation.getArgumentAt(0, CommentsRecord.class);
            comment.setId(4711L);
            return comment;
        });

        final Response response = commentResource.postComment(thread.getUrl(), thread.getTitle(),
                new CommentJson(newChildComment));

        Truth.assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());
    }

    @Test(expected = BadRequestException.class)
    public void testWithNonExistingParent() throws Exception {

        final CommentsRecord newChildComment =
                new CommentsRecord()
                        .setParentId(nonExistingParentComment.getId())
                        .setText("Child");

        Mockito.when(commentRepository.insert(Mockito.eq(newChildComment))).then(invocation -> {
            newChildComment.setId(4711L);
            return newChildComment;
        });

        commentResource.postComment(thread.getUrl(), thread.getTitle(), new CommentJson(newChildComment));
    }

    @Test(expected = BadRequestException.class)
    public void testWithParentFromOtherThread() throws Exception {

        final CommentsRecord newChildComment =
                new CommentsRecord()
                        .setParentId(existingParentFromOtherThread.getId())
                        .setText("Child");

        commentResource.postComment(thread.getUrl(), thread.getTitle(), new CommentJson(newChildComment));
    }
}
