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

package de.vorb.platon.integration;

import de.vorb.platon.config.SpringITConfig;
import de.vorb.platon.integration.pages.CommentCountPage;
import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.jooq.tables.records.ThreadsRecord;
import de.vorb.platon.model.CommentStatus;

import com.google.common.truth.Truth;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.vorb.platon.jooq.Tables.COMMENTS;
import static de.vorb.platon.jooq.Tables.THREADS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringITConfig.class})
public class CommentCountIT {

    private static final Logger logger = LoggerFactory.getLogger(CommentCountIT.class);

    @Autowired
    private WebDriver webDriver;

    @Autowired
    private DSLContext dslContext;

    @Value("http://localhost:${server.port}/comment-count.html")
    private String testUrl;

    private static final String URL_THREAD_1 = "/comment-count-thread-1.html";
    private static final String URL_THREAD_2 = "/comment-count-thread-2.html";

    private Long thread1Id;
    private List<CommentsRecord> commentsThread1 = new ArrayList<>();
    private List<CommentsRecord> commentsThread2 = Collections.emptyList();

    @Before
    public void setUp() throws Exception {
        ThreadsRecord thread = new ThreadsRecord();
        thread.setUrl(URL_THREAD_1);
        thread.setTitle("Thread Count 1");
        thread = dslContext.insertInto(THREADS).set(thread).returning(THREADS.ID).fetchOne();
        thread1Id = thread.getId();

        CommentsRecord comment = new CommentsRecord();
        comment.setThreadId(thread1Id);
        comment.setCreationDate(Timestamp.from(Instant.now()));
        comment.setLastModificationDate(comment.getCreationDate());
        comment.setStatus(CommentStatus.PUBLIC.toString());
        comment.setText("Comment 1");
        comment = dslContext.insertInto(COMMENTS).set(comment).returning(COMMENTS.ID, COMMENTS.STATUS).fetchOne();
        commentsThread1.add(comment);

        comment = new CommentsRecord();
        comment.setThreadId(thread1Id);
        comment.setCreationDate(Timestamp.from(Instant.now()));
        comment.setLastModificationDate(comment.getCreationDate());
        comment.setStatus(CommentStatus.AWAITING_MODERATION.toString());
        comment.setText("Comment 2");
        comment = dslContext.insertInto(COMMENTS).set(comment).returning(COMMENTS.ID, COMMENTS.STATUS).fetchOne();
        commentsThread1.add(comment);

        comment = new CommentsRecord();
        comment.setThreadId(thread1Id);
        comment.setCreationDate(Timestamp.from(Instant.now()));
        comment.setLastModificationDate(comment.getCreationDate());
        comment.setStatus(CommentStatus.DELETED.toString());
        comment.setText("Comment 1");
        comment = dslContext.insertInto(COMMENTS).set(comment).returning(COMMENTS.ID, COMMENTS.STATUS).fetchOne();
        commentsThread1.add(comment);

        comment = new CommentsRecord();
        comment.setThreadId(thread1Id);
        comment.setParentId(comment.getId());
        comment.setCreationDate(Timestamp.from(Instant.now()));
        comment.setLastModificationDate(comment.getCreationDate());
        comment.setStatus(CommentStatus.PUBLIC.toString());
        comment.setText("Comment 1");
        comment = dslContext.insertInto(COMMENTS).set(comment).returning(COMMENTS.ID, COMMENTS.STATUS).fetchOne();
        commentsThread1.add(comment);
    }

    @After
    public void tearDown() throws Exception {
        dslContext.deleteFrom(COMMENTS).execute();
        dslContext.deleteFrom(THREADS).execute();
    }

    @Test
    public void loadComments() throws Exception {

        final CommentCountPage commentPage = new CommentCountPage(webDriver);

        webDriver.get(testUrl);
        commentPage.waitUntilCommentCountsLoaded();

        final Map<String, Set<Long>> commentCountsByThread = commentPage.getCommentCountsByThread();

        commentCountsByThread.values().forEach(commentCounts -> Truth.assertThat(commentCounts).hasSize(1));

        final long countThread1 = commentCountsByThread.get(URL_THREAD_1).iterator().next();
        final long expectedCountThread1 = commentsThread1.stream().filter(
                comment -> CommentStatus.valueOf(comment.getStatus()) == CommentStatus.PUBLIC).count();

        Truth.assertThat(countThread1).isEqualTo(expectedCountThread1);

        final long countThread2 = commentCountsByThread.get(URL_THREAD_2).iterator().next();
        final long expectedCountThread2 = commentsThread2.stream().filter(
                comment -> CommentStatus.valueOf(comment.getStatus()) == CommentStatus.PUBLIC).count();

        Truth.assertThat(countThread2).isEqualTo(expectedCountThread2);
    }
}
