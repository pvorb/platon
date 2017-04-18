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
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.util.CommentConverter;
import de.vorb.platon.util.CommentFilters;
import de.vorb.platon.util.InputSanitizer;
import de.vorb.platon.web.rest.json.CommentJson;
import de.vorb.platon.web.rest.json.CommentListResultJson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentResourceHierarchyTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ThreadRepository threadRepository;

    @Mock
    private CommentConverter commentConverter;

    @Mock
    private RequestVerifier requestVerifier;

    @Mock
    private InputSanitizer inputSanitizer;

    @Mock
    private CommentFilters commentFilters;

    @InjectMocks
    private CommentResource commentResource;

    @Before
    public void setUp() throws Exception {
        when(commentConverter.convertRecordToJson(any())).thenCallRealMethod();
    }

    @Test
    public void testSimpleTree() throws Exception {

        final String testThreadUrl = "http://example.org/test";
        final CommentsRecord comment1 = new CommentsRecord().setId(1L).setText("1");
        final CommentsRecord comment2 = new CommentsRecord().setId(2L).setParentId(1L).setText("2");

        when(commentFilters.doesCommentCount(any())).thenReturn(true);

        when(commentRepository.findByThreadUrl(eq(testThreadUrl)))
                .thenReturn(Arrays.asList(comment1, comment2));

        final CommentListResultJson commentListResult = commentResource.findCommentsByThreadUrl(testThreadUrl);

        assertThat(commentListResult.getTotalCommentCount()).isEqualTo(2);

        assertThat(commentListResult.getComments()).hasSize(1);
        assertThat(commentListResult.getComments().get(0).getId()).isEqualTo(comment1.getId());

        final List<CommentJson> replies = commentListResult.getComments().get(0).getReplies();
        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).getId()).isEqualTo(comment2.getId());
    }
}
