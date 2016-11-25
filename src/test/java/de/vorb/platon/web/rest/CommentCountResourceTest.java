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

import de.vorb.platon.persistence.CommentRepository;

import com.google.common.collect.Sets;
import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class CommentCountResourceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentCountResource commentCountResource;

    private final Set<String> sampleThreadUrls = Sets.newHashSet("/article-a.html", "/2016/11/article-b.html");

    @Before
    public void setUp() throws Exception {

        Mockito.when(commentRepository.countByThreadUrls(Mockito.eq(sampleThreadUrls)))
                .thenReturn(sampleThreadUrls.stream()
                        .collect(Collectors.toMap(Function.identity(), String::length)));
    }

    @Test
    public void getCommentCounts() throws Exception {
        commentCountResource.getCommentCounts(sampleThreadUrls)
                .getCommentCounts()
                .forEach((threadUrl, count) -> {
                    Truth.assertThat(count).isEqualTo(threadUrl.length());
                });
    }
}
