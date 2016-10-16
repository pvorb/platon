package de.vorb.platon.web.rest;

import de.vorb.platon.model.Comment;
import de.vorb.platon.model.CommentThread;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.CommentThreadRepository;
import de.vorb.platon.security.RequestVerifier;
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
                        .parentId(existingParentComment.getId())
                        .text("Child")
                        .build();

        Mockito.when(commentRepository.save(Mockito.any(Comment.class))).then(invocation -> {
            Comment comment = invocation.getArgumentAt(0, Comment.class);
            comment.setId(4711L);
            return comment;
        });

        final Response response = commentResource.postComment(thread.getUrl(), thread.getTitle(),
                new CommentJson(newChildComment));

        Truth.assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

    }

    @Test(expected = BadRequestException.class)
    public void testWithNonExistingParent() throws Exception {

        final Comment newChildComment =
                Comment.builder()
                        .parentId(nonExistingParentComment.getId())
                        .text("Child")
                        .build();

        Mockito.when(commentRepository.save(Mockito.eq(newChildComment))).then(invocation -> {
            newChildComment.setId(4711L);
            return newChildComment;
        });

        commentResource.postComment(thread.getUrl(), thread.getTitle(), new CommentJson(newChildComment));

    }

    @Test(expected = BadRequestException.class)
    public void testWithParentFromOtherThread() throws Exception {

        final Comment newChildComment =
                Comment.builder()
                        .parentId(existingParentFromOtherThread.getId())
                        .text("Child")
                        .build();

        commentResource.postComment(thread.getUrl(), thread.getTitle(), new CommentJson(newChildComment));

    }

}
