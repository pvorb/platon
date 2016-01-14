package de.vorb.platon.web.rest;

import de.vorb.platon.model.Comment;

import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommentResourceSanitizationTest {

    @Mock
    private CommentResource commentResource;

    @Before
    public void setUp() throws Exception {
        Mockito.doCallRealMethod().when(commentResource).sanitizeComment(Mockito.any());
    }

    @Test
    public void testPreventSimpleXssAttack() throws Exception {
        final String xssString = "Some<script>alert('XSS');</script>text";

        final Comment comment = new Comment(null, null, xssString, xssString, null, xssString);
        commentResource.sanitizeComment(comment);

        Truth.assertThat(comment.getText()).doesNotContain("<script");
        Truth.assertThat(comment.getText()).doesNotContain("</script>");

        Truth.assertThat(comment.getAuthor()).doesNotContain("<script");
        Truth.assertThat(comment.getAuthor()).doesNotContain("</script>");

        Truth.assertThat(comment.getUrl()).doesNotContain("<script");
        Truth.assertThat(comment.getUrl()).doesNotContain("</script>");
    }
}
