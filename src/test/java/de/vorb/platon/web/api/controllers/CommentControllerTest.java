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

package de.vorb.platon.web.api.controllers;

import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.web.api.common.CommentConverter;
import de.vorb.platon.web.api.common.CommentFilters;
import de.vorb.platon.web.api.common.InputSanitizer;
import de.vorb.platon.web.api.json.CommentJson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentControllerTest {

    private CommentController commentController;

    private Clock clock = Clock.systemUTC();

    @Mock
    private ThreadRepository threadRepository;
    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentConverter commentConverter;
    @Mock
    private RequestVerifier requestVerifier;
    @Mock
    private InputSanitizer inputSanitizer;
    @Mock
    private CommentFilters commentFilters;

    @Mock
    private CommentJson commentJson;

    @Before
    public void setUp() throws Exception {
        commentController = new CommentController(clock, threadRepository, commentRepository, commentConverter,
                requestVerifier, inputSanitizer, commentFilters);
    }

    @Test
    public void getCommentByIdReturnsPublicComment() throws Exception {

        final long commentId = 4711;
        final CommentsRecord publicRecord =
                new CommentsRecord()
                        .setId(commentId)
                        .setText("Text")
                        .setStatus("PUBLIC");

        when(commentRepository.findById(eq(commentId))).thenReturn(publicRecord);
        when(commentConverter.convertRecordToJson(eq(publicRecord))).thenReturn(commentJson);

        assertThat(commentController.getCommentById(commentId)).isSameAs(commentJson);

        verify(commentRepository).findById(eq(commentId));
        verify(commentConverter).convertRecordToJson(eq(publicRecord));
    }
}
