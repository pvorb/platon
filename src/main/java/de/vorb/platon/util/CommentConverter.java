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

package de.vorb.platon.util;

import de.vorb.platon.jooq.tables.records.CommentsRecord;
import de.vorb.platon.model.CommentStatus;
import de.vorb.platon.web.rest.json.CommentJson;

import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Collections;

@Component
public class CommentConverter {

    public CommentJson convertRecordToJson(CommentsRecord record) {
        final CommentJson.CommentJsonBuilder json = CommentJson.builder()
                .id(record.getId())
                .parentId(record.getParentId())
                .creationDate(record.getCreationDate() == null
                        ? null
                        : record.getCreationDate().toInstant())
                .lastModificationDate(record.getLastModificationDate() == null
                        ? null
                        : record.getLastModificationDate().toInstant())
                .status(record.getStatus() == null
                        ? null
                        : Enum.valueOf(CommentStatus.class, record.getStatus()))
                .replies(Collections.emptyList());

        if (Enum.valueOf(CommentStatus.class, record.getStatus()) != CommentStatus.DELETED) {
            json
                    .text(record.getText())
                    .author(record.getAuthor())
                    .url(record.getUrl());
            if (record.getEmailHash() != null) {
                json.emailHash(Base64.getDecoder().decode(record.getEmailHash()));
            }
        }

        return json.build();
    }
}
