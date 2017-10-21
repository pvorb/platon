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

package de.vorb.platon.web.api.common;

import de.vorb.platon.jooq.tables.records.CommentsRecord;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CommentSanitizerTest {

    @InjectMocks
    private CommentSanitizer commentSanitizer;

    @Mock
    private InputSanitizer inputSanitizer;

    private CommentsRecord comment = new CommentsRecord();

    @Test
    public void escapesHtmlInAuthorName() throws Exception {

        comment.setAuthor("<i>Jane</i> Doe <script>alert('boo');</script>");

        commentSanitizer.sanitizeComment(comment);

        assertThat(comment.getAuthor()).isEqualTo("Jane Doe");
    }

}
