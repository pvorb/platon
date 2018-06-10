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

import de.vorb.platon.web.api.common.ByteArrayConverter;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteArrayConverterTest {

    private static final byte[] BYTES = {(byte) 0xFF, (byte) 0x00, (byte) 0xA3};
    private static final String HEX_STRING = "ff00a3";

    @Test
    public void shortBytesToHexString() {
        assertThat(ByteArrayConverter.bytesToHexString(BYTES)).isEqualTo(HEX_STRING);
    }

    @Test
    public void shortHexStringToBytes() {
        assertThat(ByteArrayConverter.hexStringToBytes(HEX_STRING)).isEqualTo(BYTES);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unevenLengthHexToBytes() {
        ByteArrayConverter.hexStringToBytes("ff00a");
    }
}
