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

import de.vorb.platon.jooq.tables.pojos.Comment;
import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.web.api.json.CommentJson;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;

@Component
public class CommentConverter {

    private final MessageDigest md5;

    @SneakyThrows
    public CommentConverter() {
        this.md5 = MessageDigest.getInstance("MD5");
    }

    public CommentJson convertPojoToJson(Comment comment) {

        final CommentJson.CommentJsonBuilder json = CommentJson.builder()
                .id(comment.getId())
                .parentId(comment.getParentId())
                .creationDate(comment.getCreationDate())
                .lastModificationDate(comment.getLastModificationDate())
                .status(comment.getStatus())
                .replies(new ArrayList<>());

        if (comment.getStatus() != CommentStatus.DELETED) {

            json.text(comment.getText());
            json.author(comment.getAuthor());
            json.url(comment.getUrl());

            if (comment.getEmailHash() != null) {
                json.emailHash(Base64.getDecoder().decode(comment.getEmailHash()));
            }
        }

        return json.build();
    }

    public Comment convertJsonToPojo(CommentJson json) {
        return new Comment()
                .setId(json.getId())
                .setParentId(json.getParentId())
                .setCreationDate(json.getCreationDate())
                .setLastModificationDate(json.getLastModificationDate())
                .setStatus(json.getStatus())
                .setText(json.getText())
                .setAuthor(json.getAuthor())
                .setEmailHash(calculateEmailHash(json.getEmail()))
                .setUrl(json.getUrl());
    }

    private String calculateEmailHash(String email) {
        if (email == null) {
            return null;
        }

        return ByteArrayConverter.bytesToHexString(md5.digest(email.getBytes(StandardCharsets.UTF_8)));
    }

}
