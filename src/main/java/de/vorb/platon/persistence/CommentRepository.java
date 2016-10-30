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

package de.vorb.platon.persistence;

import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.model.CommentStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommentRepository {

    List<CommentsRecord> findByThreadUrl(String threadUrl);

    CommentsRecord findById(long id);

    CommentsRecord insert(CommentsRecord comment);

    Map<String, Long> countByThreadUrls(Set<String> threadUrls);

    void update(CommentsRecord comment);

    void setStatus(long id, CommentStatus status);

}
