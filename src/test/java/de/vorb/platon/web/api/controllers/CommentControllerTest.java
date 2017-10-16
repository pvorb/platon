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
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.ThreadRepository;
import de.vorb.platon.security.RequestVerifier;
import de.vorb.platon.web.api.common.CommentConverter;
import de.vorb.platon.web.api.common.CommentFilters;
import de.vorb.platon.web.api.common.InputSanitizer;
import de.vorb.platon.web.api.errors.RequestException;
import de.vorb.platon.web.api.json.CommentJson;
import de.vorb.platon.web.api.json.CommentListResultJson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentControllerTest {

    private static final String THREAD_URL = "/sample-article.html";

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
    @Mock
    private CommentJson childCommentJson;

    @Before
    public void setUp() throws Exception {
        commentController = new CommentController(clock, threadRepository, commentRepository, commentConverter,
                requestVerifier, inputSanitizer, commentFilters);
    }

    @Test
    public void getCommentByIdReturnsPublicComment() throws Exception {

        final long commentId = 4711;
        final CommentsRecord publicComment =
                new CommentsRecord()
                        .setId(commentId)
                        .setText("Text")
                        .setStatus("PUBLIC");

        when(commentRepository.findById(eq(commentId))).thenReturn(publicComment);
        when(commentConverter.convertRecordToJson(eq(publicComment))).thenReturn(commentJson);

        assertThat(commentController.getCommentById(commentId)).isSameAs(commentJson);
    }

    @Test
    public void getCommentByIdThrowsNotFoundForDeletedComment() throws Exception {

        final long commentId = 1337;
        final CommentsRecord deletedComment =
                new CommentsRecord()
                        .setId(commentId)
                        .setText("Text")
                        .setStatus("DELETED");

        when(commentRepository.findById(eq(commentId))).thenReturn(deletedComment);

        assertThatExceptionOfType(RequestException.class)
                .isThrownBy(() -> commentController.getCommentById(commentId))
                .matches(exception -> exception.getHttpStatus() == HttpStatus.NOT_FOUND)
                .withMessage("No comment found with id = " + commentId);
    }

    @Test
    public void findCommentsByThreadUrlReturnsCommentsAsTree() throws Exception {

        final CommentsRecord comment = new CommentsRecord().setId(4711L);
        final CommentsRecord childComment = new CommentsRecord().setId(4712L).setParentId(comment.getId());

        final List<CommentsRecord> records = Arrays.asList(comment, childComment);

        when(commentRepository.findByThreadUrl(eq(THREAD_URL))).thenReturn(records);
        acceptAllComments();

        when(commentConverter.convertRecordToJson(eq(comment))).thenReturn(commentJson);
        when(commentConverter.convertRecordToJson(eq(childComment))).thenReturn(childCommentJson);

        when(commentJson.getId()).thenReturn(comment.getId());
        when(commentJson.getReplies()).thenReturn(new ArrayList<>());
        when(childCommentJson.getId()).thenReturn(childComment.getId());

        final CommentListResultJson resultJson = commentController.findCommentsByThreadUrl(THREAD_URL);

        assertThat(resultJson.getComments()).isEqualTo(Collections.singletonList(commentJson));
        assertThat(resultJson.getComments().get(0).getReplies()).isEqualTo(Collections.singletonList(childCommentJson));
        assertThat(resultJson.getTotalCommentCount()).isEqualTo(records.size());

        records.forEach(record -> verify(commentFilters).doesCommentCount(eq(record)));
    }

    private void acceptAllComments() {
        when(commentFilters.doesCommentCount(any())).thenReturn(true);
    }

    @Test
    public void findCommentsByThreadUrlThrowsNotFoundIfThreadIsEmpty() throws Exception {

        when(commentRepository.findByThreadUrl(any())).thenReturn(Collections.emptyList());

        assertThatExceptionOfType(RequestException.class)
                .isThrownBy(() -> commentController.findCommentsByThreadUrl(THREAD_URL))
                .matches(exception -> exception.getHttpStatus() == HttpStatus.NOT_FOUND)
                .withMessage("No thread found with url = '" + THREAD_URL + "'");
    }


}
