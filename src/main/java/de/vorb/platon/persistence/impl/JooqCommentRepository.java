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

import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.CommentRepository;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.vorb.platon.jooq.Tables.COMMENTS;
import static de.vorb.platon.jooq.Tables.THREADS;
import static org.jooq.impl.DSL.count;

@Repository
public class JooqCommentRepository implements CommentRepository {

    private final DSLContext dslContext;

    @Inject
    public JooqCommentRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    @Override
    public List<CommentsRecord> findByThreadUrl(String threadUrl) {
        return dslContext
                .selectFrom(COMMENTS
                        .join(THREADS).on(COMMENTS.THREAD_ID.eq(THREADS.ID)))
                .where(THREADS.URL.eq(threadUrl))
                .fetchInto(CommentsRecord.class);
    }

    @Override
    public CommentsRecord findById(long id) {
        return dslContext
                .selectFrom(COMMENTS)
                .where(COMMENTS.ID.eq(id))
                .fetchOne();
    }

    @Override
    public CommentsRecord insert(CommentsRecord comment) {
        return dslContext.insertInto(COMMENTS)
                .set(comment)
                .returning()
                .fetchOne();
    }

    @Override
    public Map<String, Integer> countByThreadUrls(Set<String> threadUrls) {

        return dslContext.select(THREADS.URL, count())
                .from(COMMENTS
                        .join(THREADS).on(COMMENTS.THREAD_ID.eq(THREADS.ID)))
                .where(THREADS.URL.in(threadUrls)
                      .and(COMMENTS.STATUS.eq(CommentStatus.PUBLIC.toString())))
                .groupBy(THREADS.URL)
                .fetchMap(THREADS.URL, count());
    }

    @Override
    public void update(CommentsRecord comment) {
        final int affectedRows =
                dslContext.update(COMMENTS)
                        .set(comment)
                        .where(COMMENTS.ID.eq(comment.getId()))
                        .execute();

        if (affectedRows == 0) {
            throw new DataAccessException("Could not update comment");
        }
    }

    @Override
    public void setStatus(long id, CommentStatus status) {
        final int affectedRows =
                dslContext.update(COMMENTS)
                        .set(COMMENTS.STATUS, status.toString())
                        .where(COMMENTS.ID.eq(id))
                        .execute();

        if (affectedRows == 0) {
            throw new DataAccessException("Could not set the comment status");
        }
    }
}
