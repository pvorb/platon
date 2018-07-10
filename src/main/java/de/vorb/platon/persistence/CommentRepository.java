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

import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.persistence.jooq.tables.pojos.Comment;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Transactional(propagation = Propagation.MANDATORY)
public interface CommentRepository {

    List<Comment> findByThreadId(long threadId);

    List<Comment> findPublicByThreadId(long threadId);

    Optional<Comment> findById(long id);

    Comment insert(Comment comment);

    Map<String, Integer> countByThreadUrls(Set<String> threadUrls);

    void update(Comment comment);

    void setStatus(long id, CommentStatus status);

}
