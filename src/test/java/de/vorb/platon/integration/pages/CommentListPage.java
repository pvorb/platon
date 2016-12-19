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
        final WebElement comment = webDriver.findElement(By.id("platon-comment-" + id));
        return comment.isDisplayed();
    }

    public boolean isCommentWithIdDeleted(long id) {
        final WebElement comment = webDriver.findElement(By.id("platon-comment-" + id));
        final String author = comment.findElement(By.className("platon-author")).getText();
        final String text = comment.findElement(By.className("platon-text")).getText();
        return "[deleted]".equals(author)
                && "[deleted]".equals(text);
    }
}
