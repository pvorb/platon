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
import de.vorb.platon.integration.pages.CommentListPage;
import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.jooq.tables.records.ThreadsRecord;
import de.vorb.platon.model.CommentStatus;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;

import static com.google.common.truth.Truth.assertThat;
import static de.vorb.platon.jooq.Tables.COMMENTS;
import static de.vorb.platon.jooq.Tables.THREADS;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringITConfig.class})
public class CommentListIT {

    private static final Logger logger = LoggerFactory.getLogger(CommentListIT.class);

    @Autowired
    private WebDriver webDriver;

    @Autowired
    private DSLContext dslContext;

    private static final String THREAD_URL = "/comment-list.html";

    @Value("http://localhost:${server.port}" + THREAD_URL)
    private String testUrl;

    private ThreadsRecord thread;
    private CommentsRecord topLevelComment;
    private CommentsRecord childComment;
    private CommentsRecord deletedComment;

    @Before
    public void setUp() throws Exception {
        thread = new ThreadsRecord();
        thread.setUrl(THREAD_URL);
        thread.setTitle("Thread");
        thread = dslContext.insertInto(THREADS).set(thread).returning(THREADS.ID).fetchOne();

        topLevelComment = createComment(null, CommentStatus.PUBLIC, "Sample text", "A", "a@example.org",
                "http://example.org");
        deletedComment = createComment(topLevelComment.getId(), CommentStatus.DELETED, "Sample text 2", "B",
                "b@example.com", "http://example.com");
        childComment = createComment(topLevelComment.getId(), CommentStatus.PUBLIC, "Child text", "C", "c@example.com",
                "http://example.com");
    }

    @After
    public void tearDown() throws Exception {
        dslContext.deleteFrom(COMMENTS).execute();
        dslContext.deleteFrom(THREADS).execute();
    }

    private CommentsRecord createComment(Long parentId, CommentStatus status, String text, String author, String email,
            String url) throws NoSuchAlgorithmException {

        CommentsRecord comment = new CommentsRecord();

        comment.setThreadId(thread.getId());
        comment.setParentId(parentId);
        comment.setCreationDate(Timestamp.from(Instant.now()));
        comment.setLastModificationDate(comment.getCreationDate());
        comment.setStatus(status.toString());
        comment.setText(text);
        comment.setAuthor(author);

        MessageDigest md5 = MessageDigest.getInstance(MessageDigestAlgorithms.MD5);
        comment.setEmailHash(Base64.getEncoder().encodeToString(md5.digest(email.getBytes(StandardCharsets.UTF_8))));

        comment.setUrl(url);

        comment = dslContext.insertInto(COMMENTS)
                .set(comment)
                .returning(COMMENTS.ID, COMMENTS.STATUS)
                .fetchOne();

        return comment;
    }

    @Test
    public void loadCommentsAndDisplayComments() throws Exception {

        final CommentListPage commentPage = new CommentListPage(webDriver);

        webDriver.get(testUrl);
        try {
            commentPage.waitUntilCommentListLoaded();
        } catch (TimeoutException e) {
            webDriver.manage().logs().get(LogType.BROWSER).getAll().forEach(
                    logEntry -> logger.info("Browser log: <{}> [{}] {}", logEntry.getLevel(),
                            Instant.ofEpochMilli(logEntry.getTimestamp()), logEntry.getMessage()));
        }

        assertThat(commentPage.isCommentFormVisible()).isTrue();

        assertThat(commentPage.isCommentWithIdVisible(topLevelComment.getId())).isTrue();
        assertThat(commentPage.isCommentWithIdVisible(deletedComment.getId())).isTrue();
        assertThat(commentPage.isCommentWithIdVisible(childComment.getId())).isTrue();

        assertThat(commentPage.isCommentWithIdDeleted(topLevelComment.getId())).isFalse();
        assertThat(commentPage.isCommentWithIdDeleted(deletedComment.getId())).isTrue();
        assertThat(commentPage.isCommentWithIdDeleted(childComment.getId())).isFalse();
    }

    @Test
    public void postNewComment() throws Exception {

        final CommentListPage commentPage = new CommentListPage(webDriver);

        webDriver.get(testUrl);
        try {
            commentPage.waitUntilCommentListLoaded();
        } catch (TimeoutException e) {
            webDriver.manage().logs().get(LogType.BROWSER).getAll().forEach(
                    logEntry -> logger.info("Browser log: <{}> [{}] {}", logEntry.getLevel(),
                            Instant.ofEpochMilli(logEntry.getTimestamp()), logEntry.getMessage()));
        }

        commentPage.replyToComment(
                childComment.getId(),
                "A newly created comment",
                "Selenium",
                "selenium@example.org",
                "http://example.org/selenium");

        assertThat(commentPage.commentWithIdHasReplies(childComment.getId())).isTrue();
    }
}