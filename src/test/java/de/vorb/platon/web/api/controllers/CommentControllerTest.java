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
import de.vorb.platon.jooq.tables.records.ThreadsRecord;
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
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static de.vorb.platon.web.api.controllers.CommentController.SIGNATURE_HEADER;
import static java.util.Collections.enumeration;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentControllerTest {

    private static final String SCHEME = "https";
    private static final String HOST = "example.org";

    private static final String THREAD_URL = "/sample-article.html";
    private static final String THREAD_TITLE = "Article";

    private static final AtomicLong COMMENT_ID_SEQUENCE = new AtomicLong();

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

    @Spy
    private CommentsRecord commentsRecord = new CommentsRecord();
    @Mock
    private HttpServletRequest httpServletRequest;

    @Before
    public void setUp() throws Exception {
        commentController = new CommentController(clock, threadRepository, commentRepository, commentConverter,
                requestVerifier, inputSanitizer, commentFilters);

        insertCommentReturnsCommentWithNextId();
    }

    private void insertCommentReturnsCommentWithNextId() {
        when(commentRepository.insert(any()))
                .then(invocation -> {
                    final CommentsRecord insertedComment = (CommentsRecord) invocation.getArguments()[0];
                    final long nextCommentId = COMMENT_ID_SEQUENCE.incrementAndGet();
                    return insertedComment.setId(nextCommentId);
                });
    }

    @Test
    public void getCommentByIdReturnsPublicComment() throws Exception {

        final long commentId = 4711;
        final CommentsRecord publicComment =
                new CommentsRecord()
                        .setId(commentId)
                        .setText("Text")
                        .setStatus("PUBLIC");

        when(commentRepository.findById(eq(commentId))).thenReturn(Optional.of(publicComment));
        convertCommentRecordToJson(publicComment, commentJson);

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

        when(commentRepository.findById(eq(commentId))).thenReturn(Optional.of(deletedComment));

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

        convertCommentRecordToJson(comment, commentJson);
        convertCommentRecordToJson(childComment, childCommentJson);

        when(commentJson.getId()).thenReturn(comment.getId());
        when(commentJson.getReplies()).thenReturn(new ArrayList<>());
        when(childCommentJson.getId()).thenReturn(childComment.getId());

        final CommentListResultJson resultJson = commentController.findCommentsByThreadUrl(THREAD_URL);

        assertThat(resultJson.getComments()).isEqualTo(Collections.singletonList(commentJson));
        assertThat(resultJson.getComments().get(0).getReplies()).isEqualTo(Collections.singletonList(childCommentJson));
        assertThat(resultJson.getTotalCommentCount()).isEqualTo(records.size());

        records.forEach(record -> verify(commentFilters).doesCommentCount(eq(record)));
    }

    private void convertCommentRecordToJson(CommentsRecord comment, CommentJson commentJson) {
        when(commentConverter.convertRecordToJson(eq(comment))).thenReturn(commentJson);
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

    @Test
    public void postCommentCreatesNewThreadOnDemand() throws Exception {

        when(commentJson.getId()).thenReturn(null);
        when(threadRepository.findThreadIdForUrl(any())).thenReturn(Optional.empty());
        when(threadRepository.insert(any())).thenReturn(new ThreadsRecord().setId(1L));
        when(commentConverter.convertJsonToRecord(any())).thenReturn(commentsRecord);
        when(commentsRecord.getParentId()).thenReturn(null);
        when(requestVerifier.getSignatureToken(any(), any())).thenReturn(new byte[0]);
        mockPostCommentRequest();

        final ResponseEntity<CommentJson> response =
                commentController.postComment(THREAD_URL, THREAD_TITLE, commentJson);

        verify(threadRepository).insert(eq(new ThreadsRecord(null, THREAD_URL, THREAD_TITLE)));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThatLocationHeaderIsCorrect(response.getHeaders().getLocation());
        assertThatSignatureHeaderIsCorrect(response.getHeaders());
    }

    private void mockPostCommentRequest() {

        final UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(SCHEME)
                .host(HOST)
                .path("api/comments")
                .queryParam("threadUrl", THREAD_URL)
                .queryParam("threadTitle", THREAD_TITLE)
                .build();

        final String requestUrl = uriComponents.toUriString();
        final String requestQueryString = uriComponents.getQuery();

        when(httpServletRequest.getRequestURL())
                .thenReturn(new StringBuffer(requestUrl));
        when(httpServletRequest.getQueryString())
                .thenReturn(requestQueryString);
        when(httpServletRequest.getHeaderNames())
                .thenReturn(enumeration(singleton(HttpHeaders.CONTENT_TYPE)));
        when(httpServletRequest.getHeaders(eq(HttpHeaders.CONTENT_TYPE)))
                .thenReturn(enumeration(singleton("application/json")));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpServletRequest));
    }

    private void assertThatLocationHeaderIsCorrect(URI location) {
        assertThat(location).hasScheme(SCHEME);
        assertThat(location).hasHost(HOST);
        assertThat(location).hasPath("/api/comments/" + commentsRecord.getId());
        assertThat(location).hasNoParameters();
    }

    private void assertThatSignatureHeaderIsCorrect(HttpHeaders responseHeaders) {

        final List<String> signatureHeader = responseHeaders.get(SIGNATURE_HEADER);
        final URI commentLocation = responseHeaders.getLocation();

        assertThat(signatureHeader).hasSize(1);

        final String[] signatureHeaderComponents = signatureHeader.get(0).split("\\|", -1);
        assertIdentifierIsCommentLocation(signatureHeaderComponents, commentLocation);
        assertExpires24HoursAfterCreation(signatureHeaderComponents);
        assertSignatureIsEmpty(signatureHeaderComponents);
    }

    private void assertIdentifierIsCommentLocation(String[] signatureHeaderComponents, URI commentLocation) {
        assertThat(signatureHeaderComponents[0]).isEqualTo(commentLocation.toString());
    }

    private void assertExpires24HoursAfterCreation(String[] signatureHeader) {
        final Instant expectedExpiration =
                commentsRecord.getCreationDate().toInstant().plus(24, ChronoUnit.HOURS);
        assertThat(Instant.parse(signatureHeader[1])).isEqualTo(expectedExpiration);
    }

    private void assertSignatureIsEmpty(String[] signatureHeader) {
        assertThat(signatureHeader[2]).isEqualTo("");
    }
}
