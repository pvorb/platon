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

package de.vorb.platon.web.rest;


import de.vorb.platon.model.CommentThread;
import de.vorb.platon.persistence.CommentRepository;
import de.vorb.platon.persistence.CommentThreadRepository;
import de.vorb.platon.web.rest.json.CommentCountsJson;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;


@Component
@Path("comment-count")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CommentCountResource {


    private final CommentThreadRepository threadRepository;
    private final CommentRepository commentRepository;


    @Inject
    public CommentCountResource(CommentThreadRepository threadRepository,
            CommentRepository commentRepository) {
        this.threadRepository = threadRepository;
        this.commentRepository = commentRepository;
    }


    @GET
    @Transactional(readOnly = true)
    public CommentCountsJson getCommentCounts(@NotNull @QueryParam("threadUrl[]") List<String> threadUrls) {

        final CommentCountsJson commentCounts = new CommentCountsJson();

        threadUrls.forEach(threadUrl -> {
            final CommentThread thread = threadRepository.getByUrl(threadUrl);
            final Long threadCommentCount = commentRepository.countCommentsOfThread(thread);
            commentCounts.setCommentCount(threadUrl, threadCommentCount);
        });

        return commentCounts;
    }
}
