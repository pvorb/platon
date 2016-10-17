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

import de.vorb.platon.model.Comment;
import de.vorb.platon.model.CommentThread;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.CommentThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.web.rest.json.CommentJson;
import de.vorb.platon.web.rest.json.CommentListResultJson;

import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class CommentResourceHierarchyTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentThreadRepository threadRepository;

    @Mock
    private RequestVerifier requestVerifier;

    @InjectMocks
    private CommentResource commentResource;

    private CommentThread testThread;

    @Before
    public void setUp() throws Exception {
        testThread = CommentThread.builder().id(1L).title("Test Thread").url("http://example.org/test").build();
    }

    @Test
    public void testSimpleTree() throws Exception {

        final Comment comment1 = Comment.builder().id(1L).text("1").build();
        final Comment comment2 = Comment.builder().id(2L).parentId(1L).text("2").build();

        Mockito.when(threadRepository.getByUrl(Mockito.eq(testThread.getUrl()))).thenReturn(testThread);
        Mockito.when(commentRepository.findByThread(Mockito.any(CommentThread.class)))
                .thenReturn(Arrays.asList(comment1, comment2));

        CommentListResultJson commentListResult = commentResource.findCommentsByThreadUrl(testThread.getUrl());

        Truth.assertThat(commentListResult.getTotalCommentCount()).isEqualTo(2);

        Truth.assertThat(commentListResult.getComments()).hasSize(1);
        Truth.assertThat(commentListResult.getComments().get(0).getId()).isEqualTo(comment1.getId());

        List<CommentJson> replies = commentListResult.getComments().get(0).getReplies();
        Truth.assertThat(replies).hasSize(1);
        Truth.assertThat(replies.get(0).getId()).isEqualTo(comment2.getId());
    }
}
