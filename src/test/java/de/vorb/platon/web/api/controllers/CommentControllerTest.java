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
import de.vorb.platon.web.api.json.CommentJson;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("WeakerAccess")
public abstract class CommentControllerTest {

    protected static final String SCHEME = "https";
    protected static final String HOST = "example.org";
    protected static final String THREAD_URL = "/sample-article.html";
    protected static final String THREAD_TITLE = "Article";

    protected CommentController commentController;

    protected Clock clock = Clock.systemUTC();

    @Mock
    protected ThreadRepository threadRepository;
    @Mock
    protected CommentRepository commentRepository;

    @Mock
    protected CommentConverter commentConverter;
    @Mock
    protected RequestVerifier requestVerifier;
    @Mock
    protected InputSanitizer inputSanitizer;
    @Mock
    protected CommentFilters commentFilters;

    @Before
    public void setUp() throws Exception {
        commentController = new CommentController(clock, threadRepository, commentRepository, commentConverter,
                requestVerifier, inputSanitizer, commentFilters);
    }

    protected void convertCommentRecordToJson(CommentsRecord comment, CommentJson commentJson) {
        when(commentConverter.convertRecordToJson(eq(comment))).thenReturn(commentJson);
    }

    protected void convertCommentJsonToRecord(CommentJson commentJson, CommentsRecord commentsRecord) {
        when(commentConverter.convertJsonToRecord(eq(commentJson))).thenReturn(commentsRecord);
    }
}
