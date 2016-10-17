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

package de.vorb.platon.persistence;

import de.vorb.platon.model.Comment;

import com.google.common.truth.Truth;
import org.junit.Test;

public class CommentStatusConverterTest {

    private final CommentStatusConverter converter = new CommentStatusConverter();

    @Test
    public void testConvertToDatabaseColumn() throws Exception {
        for (Comment.Status status : Comment.Status.values()) {
            Truth.assertThat(converter.convertToDatabaseColumn(status)).isEqualTo(status.getValue());
        }
    }

    @Test
    public void testConvertToEntityAttribute() throws Exception {
        for (Comment.Status status : Comment.Status.values()) {
            Truth.assertThat(converter.convertToEntityAttribute(status.getValue())).isEqualTo(status);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testConvertNullToDatabaseColumn() {
        converter.convertToDatabaseColumn(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConvertNullToEntityAttribute() {
        converter.convertToEntityAttribute(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertUnknownByteToEntityAttribute() throws Exception {
        converter.convertToEntityAttribute(-1);
    }

}
