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

package de.vorb.platon.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.annotation.Repeat;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

public class HmacRequestVerifierTest {

    private static final Instant CURRENT_TIME = Instant.now();

    private HmacRequestVerifier requestVerifier;

    private final Clock clock = Clock.fixed(CURRENT_TIME, ZoneOffset.UTC);
    private SecretKeyProvider secretKeyProvider;

    @Before
    public void setUp() throws Exception {

        final SecretKey secretKey =
                KeyGenerator.getInstance(HmacRequestVerifier.HMAC_ALGORITHM.toString()).generateKey();
        secretKeyProvider = Mockito.mock(SecretKeyProvider.class);
        Mockito.when(secretKeyProvider.getSecretKey()).thenReturn(secretKey);

        requestVerifier = new HmacRequestVerifier(secretKeyProvider, clock);

    }

    @Test(expected = SecurityException.class)
    public void testInvalidKey() throws Exception {

        Mockito.when(secretKeyProvider.getSecretKey()).thenReturn(null);

        new HmacRequestVerifier(secretKeyProvider, clock);

    }

    @Test
    @Repeat(10)
    public void testGetSignatureTokenIsRepeatable() throws Exception {

        final String identifier = "comment/1";
        final Instant expirationDate = Instant.now();

        final byte[] firstSignatureToken = requestVerifier.getSignatureToken(identifier, expirationDate);
        final byte[] secondSignatureToken = requestVerifier.getSignatureToken(identifier, expirationDate);

        assertThat(firstSignatureToken).isEqualTo(secondSignatureToken);
    }

    @Test
    public void testTokenExpiration() throws Exception {

        final String identifier = "comment/1";

        final Instant expirationDate = CURRENT_TIME.minusMillis(1); // token expired 1ms ago
        final byte[] signatureToken = requestVerifier.getSignatureToken(identifier, expirationDate);

        final boolean validity = requestVerifier.isRequestValid(identifier, expirationDate, signatureToken);

        assertThat(validity).isFalse();
    }

    @Test
    public void testCannotFakeExpirationDate() throws Exception {

        final String identifier = "comment/1";

        final Instant expirationDate = CURRENT_TIME.minusMillis(1);
        final byte[] signatureToken = requestVerifier.getSignatureToken(identifier, expirationDate);

        // a user attempts to set the expiration date manually (without changing the token)
        final Instant fakedExpirationDate = CURRENT_TIME;

        final boolean validity = requestVerifier.isRequestValid(identifier, fakedExpirationDate, signatureToken);

        assertThat(validity).isFalse();
    }
}
