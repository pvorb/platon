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

package de.vorb.platon.persistence;

import de.vorb.platon.persistence.jooq.tables.pojos.CommentThread;

import java.util.List;
import java.util.Optional;

public interface ThreadRepository {

    CommentThread getById(long id);

    Optional<Long> findThreadIdForUrl(String threadUrl);

    Optional<CommentThread> findThreadForUrl(String threadUrl);

    List<CommentThread> findThreadsForUrlPrefix(String threadUrlPrefix);

    CommentThread insert(CommentThread thread);

    void updateThreadTitle(long id, String newTitle);

}
