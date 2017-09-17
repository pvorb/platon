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

package de.vorb.platon.config;

import de.vorb.platon.util.InputSanitizer;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class PlatonConfig {

    @Bean
    public WebMvcConfigurerAdapter staticResourceHandlerConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("classpath:/public");
            }
        };
    }

    @Bean
    public InputSanitizer htmlInputSanitizer(@Value("${platon.input.html_elements}") String allowedHtmlElements) {

        final HtmlPolicyBuilder htmlPolicyBuilder = new HtmlPolicyBuilder()
                .allowUrlProtocols("http", "https", "mailto")
                .allowAttributes("href").onElements("a")
                .allowAttributes("src", "width", "height", "alt").onElements("img")
                .allowAttributes("class").onElements("div", "span");

        htmlPolicyBuilder.allowElements(allowedHtmlElements.trim().split("\\s*,\\s*"));

        final PolicyFactory htmlContentPolicy = htmlPolicyBuilder.toFactory();

        return htmlContentPolicy::sanitize;
    }

}
