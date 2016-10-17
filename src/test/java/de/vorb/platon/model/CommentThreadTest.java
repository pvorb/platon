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

package de.vorb.platon.model;

import com.google.common.truth.Truth;
import org.junit.Test;

public class CommentThreadTest {

    @Test
    public void testEqualsById() throws Exception {

        final CommentThread thread1 = CommentThread.builder().id(1L).build();

        Truth.assertThat(thread1.equalsById(thread1)).isTrue();

        final CommentThread thread1Copy = CommentThread.builder().id(1L).build();

        Truth.assertThat(thread1.equalsById(thread1Copy)).isTrue();
        Truth.assertThat(thread1Copy.equalsById(thread1)).isTrue();

        final CommentThread thread2 = CommentThread.builder().id(2L).build();

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

        final CommentThread thread1 =
                CommentThread.builder()
                        .id(42L)
                        .url(articleUrl)
                        .title(articleTitle)
                        .build();

        final CommentThread thread2 =
                CommentThread.builder()
                        .id(42L)
                        .url(articleUrl)
                        .title(articleTitle)
                        .build();

        Truth.assertThat(thread1).isEqualTo(thread2);
        Truth.assertThat(thread1.hashCode()).isEqualTo(thread2.hashCode());
    }

}
