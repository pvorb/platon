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

public class CommentControllerDeleteTest extends CommentControllerTest {

//    @Test
//    public void setsCommentStatusToDeletedIfRequestIsValid() {
//
//        final long commentId = 1234;
//        final String signature = "signature";
//        assertThatCode(() -> commentController.deleteComment(commentId, signature))
//                .doesNotThrowAnyException();
//
//        verify(requestValidator).verifyValidRequest(eq(signature), eq("/api/comments/" + commentId));
//        verify(commentRepository).setStatus(eq(commentId), eq(DELETED));
//    }

//    @Test
//    public void throwsBadRequestIfErrorOccursDuringSaving() {
//
//        final long commentId = 1234;
//        final String signature = "signature";
//
//        doThrow(DataAccessException.class)
//                .when(commentRepository).setStatus(eq(commentId), eq(DELETED));
//
//        assertThatExceptionOfType(RequestException.class)
//                .isThrownBy(() -> commentController.deleteComment(commentId, signature))
//                .matches(requestException -> requestException.getHttpStatus() == BAD_REQUEST)
//                .withMessageContaining("Unable to delete comment");
//    }
}
