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

package de.vorb.platon.persistence.impl;

import de.vorb.platon.jooq.tables.pojos.Comment;
import de.vorb.platon.jooq.tables.records.CommentRecord;
import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.CommentRepository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static de.vorb.platon.jooq.Tables.COMMENT;
import static de.vorb.platon.jooq.Tables.COMMENT_THREAD;
import static org.jooq.impl.DSL.count;

@Repository
@RequiredArgsConstructor
public class JooqCommentRepository implements CommentRepository {

    private final DSLContext dslContext;

    @Override
    public List<Comment> findByThreadUrl(String threadUrl) {
        return dslContext
                .selectFrom(COMMENT
                        .join(COMMENT_THREAD).on(COMMENT.THREAD_ID.eq(COMMENT_THREAD.ID)))
                .where(COMMENT_THREAD.URL.eq(threadUrl))
                .fetchInto(Comment.class);
    }

    @Override
    public Optional<Comment> findById(long id) {
        return Optional.ofNullable(
                dslContext
                        .selectFrom(COMMENT)
                        .where(COMMENT.ID.eq(id))
                        .fetchOneInto(Comment.class));
    }

    @Override
    public Comment insert(Comment comment) {
        return dslContext.insertInto(COMMENT)
                .set(convertCommentToRecord(comment))
                .returning()
                .fetchOne()
                .into(Comment.class);
    }

    @Override
    public Map<String, Integer> countByThreadUrls(Set<String> threadUrls) {

        return dslContext.select(COMMENT_THREAD.URL, count())
                .from(COMMENT
                        .join(COMMENT_THREAD).on(COMMENT.THREAD_ID.eq(COMMENT_THREAD.ID)))
                .where(COMMENT_THREAD.URL.in(threadUrls)
                        .and(COMMENT.STATUS.eq(CommentStatus.PUBLIC)))
                .groupBy(COMMENT_THREAD.URL)
                .fetchMap(COMMENT_THREAD.URL, count());
    }

    @Override
    public void update(Comment comment) {
        final int affectedRows =
                dslContext.update(COMMENT)
                        .set(convertCommentToRecord(comment))
                        .where(COMMENT.ID.eq(comment.getId()))
                        .execute();

        if (affectedRows == 0) {
            throw new DataAccessException("Could not update comment");
        }
    }

    @Override
    public void setStatus(long id, CommentStatus status) {
        final int affectedRows =
                dslContext.update(COMMENT)
                        .set(COMMENT.STATUS, status)
                        .where(COMMENT.ID.eq(id))
                        .execute();

        if (affectedRows == 0) {
            throw new DataAccessException("Could not set the comment withStatus");
        }
    }

    private CommentRecord convertCommentToRecord(Comment comment) {
        return dslContext.newRecord(COMMENT, comment);
    }

}
