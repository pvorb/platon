package de.vorb.platon.model;

import com.google.common.truth.Truth;
import org.junit.Test;

public class CommentThreadTest {

    @Test
    public void testEqualsById() throws Exception {

        final CommentThread thread1 = new CommentThread();
        thread1.setId(1L);

        Truth.assertThat(thread1.equalsById(thread1)).isTrue();

        final CommentThread thread1Copy = new CommentThread();
        thread1Copy.setId(1L);

        Truth.assertThat(thread1.equalsById(thread1Copy)).isTrue();
        Truth.assertThat(thread1Copy.equalsById(thread1)).isTrue();

        final CommentThread thread2 = new CommentThread();
        thread2.setId(2L);

        Truth.assertThat(thread1.equalsById(thread2)).isFalse();
        Truth.assertThat(thread2.equalsById(thread1)).isFalse();

        final CommentThread threadWithNullId = new CommentThread();

        Truth.assertThat(thread1.equalsById(threadWithNullId)).isFalse();
        Truth.assertThat(threadWithNullId.equalsById(thread1)).isFalse();

        final CommentThread anotherThreadWithNullId = new CommentThread();

        Truth.assertThat(threadWithNullId.equalsById(anotherThreadWithNullId)).isFalse();
        Truth.assertThat(anotherThreadWithNullId.equalsById(threadWithNullId)).isFalse();

    }

    @Test
    public void testHashCode() throws Exception {
        final String articleUrl = "http://example.com/article";
        final String articleTitle = "Article";

        final CommentThread thread1 = new CommentThread(articleUrl, articleTitle);
        thread1.setId(42L);

        final CommentThread thread2 = new CommentThread(articleUrl, articleTitle);
        thread2.setId(42L);

        Truth.assertThat(thread1).isEqualTo(thread2);
        Truth.assertThat(thread1.hashCode()).isEqualTo(thread2.hashCode());
    }

}
