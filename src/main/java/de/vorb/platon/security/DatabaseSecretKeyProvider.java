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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class DatabaseSecretKeyProvider implements SecretKeyProvider {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSecretKeyProvider.class);

    private static final String SECRET_KEY = "secret_key";

    private final PropertyRepository propertyRepository;

    private SecretKey secretKey;

    @Inject
    public DatabaseSecretKeyProvider(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public SecretKey getSecretKey() {

        if (secretKey == null) {
            try {
                initSecretKey();
            } catch (NoSuchAlgorithmException e) {
                logger.error("Unable to generate a secret key for {}", HmacRequestVerifier.HMAC_ALGORITHM, e);
            }
        }

        return secretKey;
    }

    private void initSecretKey() throws NoSuchAlgorithmException {

        final String secretKeyStringValue = propertyRepository.findValueByKey(SECRET_KEY);

        if (secretKeyStringValue == null) {

            secretKey = KeyGenerator.getInstance(HmacRequestVerifier.HMAC_ALGORITHM.toString()).generateKey();

            propertyRepository.insertValue(SECRET_KEY, Base64.getEncoder().encodeToString(secretKey.getEncoded()));

        } else {

            final byte[] secretKeyBytes = Base64.getDecoder().decode(secretKeyStringValue);

            secretKey = new SecretKeySpec(secretKeyBytes, 0, secretKeyBytes.length,
                    HmacRequestVerifier.HMAC_ALGORITHM.toString());

        }
    }

}
