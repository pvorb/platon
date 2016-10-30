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
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.util.CommentFilters;
import de.vorb.platon.util.InputSanitizer;
import de.vorb.platon.web.rest.json.CommentJson;
import de.vorb.platon.web.rest.json.CommentListResultJson;

import com.google.common.truth.Truth;
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
    private ThreadRepository threadRepository;

    @Mock
    private RequestVerifier requestVerifier;

    @Mock
    private InputSanitizer inputSanitizer;

    @Mock
    private CommentFilters commentFilters;

    @InjectMocks
    private CommentResource commentResource;

    @Test
    public void testSimpleTree() throws Exception {

        final String testThreadUrl = "http://example.org/test";
        final CommentsRecord comment1 = new CommentsRecord().setId(1L).setText("1");
        final CommentsRecord comment2 = new CommentsRecord().setId(2L).setParentId(1L).setText("2");

        Mockito.when(commentFilters.doesCommentCount(Mockito.any())).thenReturn(true);

        Mockito.when(commentRepository.findByThreadUrl(Mockito.eq(testThreadUrl)))
                .thenReturn(Arrays.asList(comment1, comment2));

        CommentListResultJson commentListResult = commentResource.findCommentsByThreadUrl(testThreadUrl);

        Truth.assertThat(commentListResult.getTotalCommentCount()).isEqualTo(2);

        Truth.assertThat(commentListResult.getComments()).hasSize(1);
        Truth.assertThat(commentListResult.getComments().get(0).getId()).isEqualTo(comment1.getId());

        List<CommentJson> replies = commentListResult.getComments().get(0).getReplies();
        Truth.assertThat(replies).hasSize(1);
        Truth.assertThat(replies.get(0).getId()).isEqualTo(comment2.getId());
    }
}
