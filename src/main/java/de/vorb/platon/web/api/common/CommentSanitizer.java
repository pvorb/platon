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

import com.google.common.collect.ImmutableSet;
import org.owasp.encoder.Encode;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

@Component
public class CommentSanitizer {

    private static final Set<String> ACCEPTED_URL_SCHEMES = ImmutableSet.of("http", "https", "mailto");

    private static final PolicyFactory NO_HTML_POLICY = new HtmlPolicyBuilder().toFactory();

    private final InputSanitizer inputSanitizer;

    @Autowired
    public CommentSanitizer(InputSanitizer inputSanitizer) {
        this.inputSanitizer = inputSanitizer;
    }

    public void sanitizeComment(CommentsRecord comment) {

        if (comment.getAuthor() != null) {
            comment.setAuthor(NO_HTML_POLICY.sanitize(comment.getAuthor()).trim());
        }

        if (comment.getUrl() != null) {
            String url = comment.getUrl();
            if (!url.contains("://")) {
                url = "https://" + url;
            }

            if (isUrlSchemeAccepted(url)) {
                comment.setUrl(Encode.forHtmlAttribute(url));
            } else {
                comment.setUrl(null);
            }
        }

        final String requestText = comment.getText();
        final String sanitizedText = inputSanitizer.sanitize(requestText);
        comment.setText(sanitizedText);
    }

    private boolean isUrlSchemeAccepted(String url) {
        try {
            final String scheme = new URI(url).getScheme();
            return ACCEPTED_URL_SCHEMES.contains(scheme);
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
