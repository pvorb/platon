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

package de.vorb.platon;

import de.vorb.platon.config.SpringTestConfig;
import de.vorb.platon.util.InputSanitizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = SpringTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InputSanitizerTest {

    @Autowired
    private InputSanitizer inputSanitizer;

    @Test
    public void htmlWithScriptTag() throws Exception {

        final String sanitizedHtml = inputSanitizer.sanitize("<p>Text</p><script>alert('boo!');</script>");

        assertThat(sanitizedHtml).doesNotContain("<script");
        assertThat(sanitizedHtml).doesNotContain("alert(");
    }
}
