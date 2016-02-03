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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;

@RunWith(MockitoJUnitRunner.class)
public class CommentResourcePostWithParentTest {

    @Mock
    private CommentThreadRepository threadRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RequestVerifier requestVerifier;

    private final InputSanitizer htmlInputSanitizer = String::trim;

    private CommentResource commentResource;


    private CommentThread thread;


    private Comment existingParentComment;

    private Comment nonExistingParentComment;

    private Comment existingParentFromOtherThread;


    @Before
    public void setUp() throws Exception {

        Mockito.when(requestVerifier.getSignatureToken(Mockito.any(), Mockito.any())).thenReturn(new byte[0]);
        Mockito.when(requestVerifier.isRequestValid(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);


        final long threadId = 42;
        thread =
                Mockito.spy(CommentThread.builder()
                        .url("http://example.com/article")
                        .title("Article")
                        .build());

        Mockito.when(thread.getId()).thenReturn(threadId);

        Mockito.when(threadRepository.getByUrl(Mockito.eq(thread.getUrl()))).thenReturn(thread);


        final long existingParentId = 4711;
        existingParentComment =
                Mockito.spy(Comment.builder()
                        .thread(thread)
                        .text("Existing parent")
                        .build());
        Mockito.when(existingParentComment.getId()).thenReturn(existingParentId);

        Mockito.when(commentRepository.findOne(Mockito.eq(existingParentId))).thenReturn(existingParentComment);


        final long nonExistingParentId = 4712;
        nonExistingParentComment =
                Mockito.spy(Comment.builder()
                        .thread(thread)
                        .text("Non-existing parent")
                        .build());
        Mockito.when(nonExistingParentComment.getId()).thenReturn(nonExistingParentId);

        Mockito.when(commentRepository.findOne(Mockito.eq(nonExistingParentId))).thenReturn(null);


        final long otherThreadId = 43;
        final CommentThread otherThread =
                Mockito.spy(CommentThread.builder()
                        .url("http://example.com/other-article")
                        .title("Other article")
                        .build());
        Mockito.when(otherThread.getId()).thenReturn(otherThreadId);

        final long existingParentFromOtherThreadId = 4713;
        existingParentFromOtherThread =
                Mockito.spy(Comment.builder()
                        .thread(otherThread)
                        .text("Existing parent from other thread")
                        .build());
        Mockito.when(existingParentFromOtherThread.getId()).thenReturn(existingParentFromOtherThreadId);

        Mockito.when(commentRepository.findOne(Mockito.eq(existingParentFromOtherThreadId)))
                .thenReturn(existingParentFromOtherThread);

        commentResource = new CommentResource(threadRepository, commentRepository, requestVerifier, htmlInputSanitizer);
    }

    @Test
    public void testWithExistingParent() throws Exception {

        final Comment newChildComment =
                Comment.builder()
                        .id(4714L)
                        .parent(existingParentComment)
                        .text("Child")
                        .build();

        final Response response = commentResource.postComment(thread.getUrl(), thread.getTitle(), newChildComment);

        Truth.assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

    }

    @Test(expected = BadRequestException.class)
    public void testWithNonExistingParent() throws Exception {

        final Comment newChildComment =
                Comment.builder()
                        .parent(nonExistingParentComment)
                        .text("Child")
                        .build();

        commentResource.postComment(thread.getUrl(), thread.getTitle(), newChildComment);

    }

    @Test(expected = BadRequestException.class)
    public void testWithParentFromOtherThread() throws Exception {

        final Comment newChildComment =
                Comment.builder()
                        .parent(existingParentFromOtherThread)
                        .text("Child")
                        .build();

        commentResource.postComment(thread.getUrl(), thread.getTitle(), newChildComment);

    }

}
