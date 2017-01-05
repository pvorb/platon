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

package de.vorb.platon.integration.pages;

import org.assertj.core.util.Preconditions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CommentListPage {

    private final WebDriver webDriver;

    public CommentListPage(WebDriver webDriver) {
        this.webDriver = webDriver;
    }

    public void waitUntilCommentListLoaded() {
        new WebDriverWait(webDriver, 15).until(
                ExpectedConditions.presenceOfElementLocated(By.className("platon-comments")));
    }

    public boolean isCommentFormVisible() {
        return webDriver.findElement(By.className("platon-form")).isDisplayed();
    }

    public boolean isCommentWithIdVisible(long id) {
        final WebElement comment = findCommentById(id);
        return comment.isDisplayed();
    }

    public boolean isCommentWithIdDeleted(long id) {
        final WebElement comment = findCommentById(id);
        final String author = comment.findElement(By.className("platon-author")).getText();
        final String text = comment.findElement(By.className("platon-text")).getText();
        return "[deleted]".equals(author)
                && "[deleted]".equals(text);
    }

    public void replyToComment(long id, String text, String author, String email, String url) {
        final WebElement existingComment = findCommentById(id);
        existingComment.findElement(By.linkText("Reply")).click();

        Preconditions.checkNotNull(text);
        getFirstVisibleChildMatching(existingComment, By.className("platon-form-text")).sendKeys(text);

        if (author != null) {
            getFirstVisibleChildMatching(existingComment, By.className("platon-form-author")).sendKeys(author);
        }

        if (email != null) {
            getFirstVisibleChildMatching(existingComment, By.className("platon-form-email")).sendKeys(email);
        }

        if (url != null) {
            getFirstVisibleChildMatching(existingComment, By.className("platon-form-email")).sendKeys(url);
        }

        getFirstVisibleChildMatching(existingComment, By.cssSelector("form.platon-form")).submit();
    }

    public boolean commentWithIdHasReplies(long id) {

        new WebDriverWait(webDriver, 15).until(
                ExpectedConditions.visibilityOfNestedElementsLocatedBy(findCommentById(id),
                        By.className("platon-comment")));

        return !findCommentById(id).findElements(By.className("platon-comment")).isEmpty();
    }

    private WebElement findCommentById(long id) {
        return webDriver.findElement(By.id("platon-comment-" + id));
    }

    private WebElement getFirstVisibleChildMatching(WebElement parent, By childLocator) {
        return parent.findElements(childLocator).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("No matching element found that is visible"));
    }
}
