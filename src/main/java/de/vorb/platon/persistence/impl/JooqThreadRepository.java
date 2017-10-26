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

import de.vorb.platon.jooq.tables.records.ThreadsRecord;
import de.vorb.platon.persistence.ThreadRepository;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static de.vorb.platon.jooq.Tables.THREADS;

@Repository
@RequiredArgsConstructor
public class JooqThreadRepository implements ThreadRepository {

    private final DSLContext dslContext;

    @Override
    public Optional<Long> findThreadIdForUrl(String threadUrl) {
        return Optional.ofNullable(
                dslContext.selectFrom(THREADS)
                        .where(THREADS.URL.eq(threadUrl))
                        .fetchOne(THREADS.ID));
    }

    @Override
    public ThreadsRecord insert(ThreadsRecord thread) {
        return dslContext.insertInto(THREADS)
                .set(thread)
                .returning(THREADS.ID)
                .fetchOne();
    }

}
