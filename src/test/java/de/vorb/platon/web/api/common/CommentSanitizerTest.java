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

import de.vorb.platon.persistence.jooq.tables.pojos.Comment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentSanitizerTest {

    @InjectMocks
    private CommentSanitizer commentSanitizer;

//    @Mock
//    private InputSanitizer inputSanitizer;

    private final Comment comment = new Comment();

    @Test
    public void removesHtmlInAuthorName() {

        comment.setAuthor("<i>Jane</i> Doe <script>alert('boo');</script>");

        commentSanitizer.sanitizeComment(comment);

        assertThat(comment.getAuthor()).isEqualTo("Jane Doe");
    }

    @Test
    public void sanitizesAndEncodesUrls() {

        assertThatUrlIsAccepted("http://example.org/article.html");
        assertThatUrlIsAccepted("https://example.org/secure/profile.php");
        assertThatUrlIsAccepted("example.org/article.html");

        assertThatUrlIsNotAccepted("irc://example.org/chat");
        assertThatUrlIsNotAccepted("scp://example.org/file.txt");
        assertThatUrlIsNotAccepted("ftp://example.org/file.txt");
        assertThatUrlIsNotAccepted("<<<invalid url>>>");
    }

    private void assertThatUrlIsAccepted(String url) {
        sanitizeCommentWithUrl(url);
        assertThat(comment.getUrl()).isNotNull();
    }

    private void assertThatUrlIsNotAccepted(String url) {
        sanitizeCommentWithUrl(url);
        assertThat(comment.getUrl()).isNull();
    }

    @Test
    public void encodesSpecialCharactersInUrls() {
        sanitizeCommentWithUrl("https://example.org/a url with spaces");
        assertThat(comment.getUrl()).isEqualTo("https://example.org/a%20url%20with%20spaces");
    }

    @Test
    public void doesNotDoubleEncodeEscapedCharacters() {
        final String url = "https://example.org/a%20url%20with%20spaces";
        sanitizeCommentWithUrl(url);
        assertThat(comment.getUrl()).isEqualTo(url);
    }

    @Test
    public void prependsHttpsToUrlsWithMissingScheme() {
        sanitizeCommentWithUrl("www.example.org");
        assertThat(comment.getUrl()).isEqualTo("https://www.example.org");
    }

    private void sanitizeCommentWithUrl(String url) {
        comment.setUrl(url);
        commentSanitizer.sanitizeComment(comment);
    }

//    @Test
//    public void sanitizesCommentTextUsingInputSanitizer() {
//
//        final String text = "Text";
//        final String sanitizedText = "Sanitized text";
//
//        when(inputSanitizer.sanitize(eq(text))).thenReturn(sanitizedText);
//        comment.setTextSource(text);
//
//        commentSanitizer.sanitizeComment(comment);
//
//        verify(inputSanitizer).sanitize(eq(text));
//        assertThat(comment.getTextSource()).isEqualTo(sanitizedText);
//    }

}
