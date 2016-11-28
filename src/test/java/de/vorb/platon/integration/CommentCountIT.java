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

package de.vorb.platon.integration;

import de.vorb.platon.config.SpringITConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringITConfig.class)
public class CommentCountIT {

    private static final Logger logger = LoggerFactory.getLogger(CommentCountIT.class);

    @Autowired
    private WebDriver webDriver;

    @Value("http://localhost:${server.port}/js/platon.js")
    private String testUrl;

    @Test
    public void navigateToIndex() throws Exception {
        webDriver.get(testUrl);

        logger.info("Navigated to {}", webDriver.getCurrentUrl());
    }
}
