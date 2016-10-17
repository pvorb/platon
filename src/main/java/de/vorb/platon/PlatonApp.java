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

package de.vorb.platon;

import de.vorb.platon.security.HmacRequestVerifier;
import de.vorb.platon.security.SecretKeyProvider;
import de.vorb.platon.util.CurrentTimeProvider;
import de.vorb.platon.util.InputSanitizer;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

@SpringBootApplication
public class PlatonApp {

    private static final Logger logger = LoggerFactory.getLogger(PlatonApp.class);

    public static void main(String... args) {
        SpringApplication.run(PlatonApp.class, args);
    }

    private final SecretKey secretKey;

    public PlatonApp() throws NoSuchAlgorithmException {
        secretKey = KeyGenerator.getInstance(HmacRequestVerifier.HMAC_ALGORITHM.toString()).generateKey();

        logger.info("Secret key: {}", Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }

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
    public SecretKeyProvider secretKeyProvider() {
        return () -> secretKey;
    }

    @Bean
    public CurrentTimeProvider timeProvider() {
        return () -> Instant.now();
    }

    private static final HtmlPolicyBuilder htmlContentPolicyBuilder = new HtmlPolicyBuilder()
            .allowUrlProtocols("http", "https", "mailto")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src", "width", "height", "alt").onElements("img")
            .allowAttributes("class").onElements("div", "span");

    @Value("${platon.input.html_elements}")
    private String htmlElements;

    @Bean
    public InputSanitizer htmlInputSanitizer() {
        final String[] htmlElementList = htmlElements.split("\\s*,\\s*");

        htmlContentPolicyBuilder.allowElements(htmlElementList);

        final PolicyFactory htmlContentPolicy = htmlContentPolicyBuilder.toFactory();

        return htmlContentPolicy::sanitize;
    }

}
