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

package de.vorb.platon.web.rest.json;

import de.vorb.platon.model.CommentStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class CommentJson {

    private final Long id;
    private final Long parentId;

    private final Instant creationDate;
    private final Instant lastModificationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final CommentStatus status;

    private final String text;

    private final String author;

    @JsonSerialize(using = ByteArraySerializer.class)
    @JsonDeserialize(using = ByteArrayDeserializer.class)
    private final byte[] emailHash;

    private final String url;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<CommentJson> replies;

}
