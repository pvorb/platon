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

package de.vorb.platon.web.api.controllers;

import de.vorb.platon.web.api.errors.RequestException;
import de.vorb.platon.web.api.json.CommentJson;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

public class CommentControllerUpdateTest extends CommentControllerTest {

    @Mock
    private CommentJson commentJson;

    @Test
    public void idsMustMatch() throws Exception {

        when(commentJson.getId()).thenReturn(1234L);

        assertThatExceptionOfType(RequestException.class)
                .isThrownBy(() -> commentController.updateComment(1L, "", commentJson))
                .matches(requestException -> requestException.getHttpStatus() == HttpStatus.BAD_REQUEST)
                .withMessageStartingWith("Comment IDs do not match");
    }

}
