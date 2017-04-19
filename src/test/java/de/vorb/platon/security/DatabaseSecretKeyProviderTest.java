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

import de.vorb.platon.persistence.PropertyRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseSecretKeyProviderTest {

    @Mock
    private PropertyRepository propertyRepository;

    private static final String SECRET_KEY = "secret_key";

    @InjectMocks
    private DatabaseSecretKeyProvider secretKeyProvider;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void noSecretKeyAvailable() throws Exception {

        Mockito.when(propertyRepository.findValueByKey(Mockito.eq(SECRET_KEY))).thenReturn(null);

        secretKeyProvider.getSecretKey();

        Mockito.verify(propertyRepository).insertValue(Mockito.eq(SECRET_KEY), Mockito.any());
    }

    @Test
    public void secretKeyExists() throws Exception {

        final SecretKey storedSecretKey = KeyGenerator.getInstance(
                HmacRequestVerifier.HMAC_ALGORITHM.toString()).generateKey();

        Mockito.when(propertyRepository.findValueByKey(Mockito.eq(SECRET_KEY)))
                .thenReturn(Base64.getEncoder().encodeToString(storedSecretKey.getEncoded()));

        final SecretKey secretKeyFromRepo = secretKeyProvider.getSecretKey();

        assertThat(secretKeyFromRepo).isEqualTo(storedSecretKey);
    }
}
