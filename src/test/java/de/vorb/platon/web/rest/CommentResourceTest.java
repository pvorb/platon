package de.vorb.platon.web.rest;

import de.vorb.platon.model.Comment;
import de.vorb.platon.model.CommentThread;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.CommentThreadRepository;
import de.vorb.platon.security.RequestVerifier;

import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class CommentResourceTest {

    private static final CommentThread emptyThread = new CommentThread("http://example.com/article",
            "An empty comment thread");

    private static final CommentThread nonEmptyThread = new CommentThread("http://example.com/article-with-comments",
            "An non-empty comment thread");

    static {
        final Comment comment = new Comment(nonEmptyThread, null, "Text", "Author", null, null);
        comment.setId(4711L);
        nonEmptyThread.getComments().add(comment);
    }

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentThreadRepository threadRepository;

    @Mock
    private RequestVerifier requestVerifier;

    @InjectMocks
    private CommentResource commentResource;

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
        Mockito.when(commentRepository.findOne(Mockito.eq(4711L))).thenReturn(comment);

        Truth.assertThat(commentResource.getComment(4711L)).isSameAs(comment);

    }

    @Test(expected = NotFoundException.class)
    public void testGetCommentIdNotFound() throws Exception {

        Mockito.when(commentRepository.findOne(Mockito.anyLong())).thenReturn(null);
        commentResource.getComment(4711L);

    }

    @Test
    public void testPostCommentToExistingThread() throws Exception {
        final Comment newComment = Mockito.spy(new Comment(nonEmptyThread, null, "Text", "Author", null, null));

        commentResource.postComment(nonEmptyThread.getUrl(), nonEmptyThread.getTitle(), newComment);

        Mockito.verify(commentRepository).save(Mockito.same(newComment));
    }

    @Test
    public void testPostCommentToNewThread() throws Exception {
        final String threadUrl = "http://example.com/new-article";
        final String threadTitle = "New article";
        final Comment newComment = Mockito.spy(new Comment(null, null, "Text", "Author", null, null));

        commentResource.postComment(threadUrl, threadTitle, newComment);

        final ArgumentCaptor<CommentThread> threadCaptor = ArgumentCaptor.forClass(CommentThread.class);
        Mockito.verify(threadRepository).save(threadCaptor.capture());

        Truth.assertThat(threadCaptor.getValue().getUrl()).isEqualTo(threadUrl);
        Truth.assertThat(threadCaptor.getValue().getTitle()).isEqualTo(threadTitle);

        Mockito.verify(commentRepository).save(Mockito.same(newComment));
    }

    @Test(expected = BadRequestException.class)
    public void testUpdateNonExistingComment() throws Exception {
        final Comment newComment = new Comment(null, null, "Text", "Author", null, null);
        newComment.setId(42L);

        commentResource.updateComment(newComment.getId(), newComment);

        Mockito.verifyZeroInteractions(threadRepository, commentRepository);
    }

}
