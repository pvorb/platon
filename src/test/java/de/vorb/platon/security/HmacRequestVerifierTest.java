package de.vorb.platon.security;

import com.google.common.truth.Truth;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.annotation.Repeat;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.time.Instant;

@RunWith(MockitoJUnitRunner.class)
public class HmacRequestVerifierTest {

    @Mock
    private SecretKeyProvider secretKeyProvider;

    private HmacRequestVerifier requestVerifier;

    @Before
    public void setUp() throws Exception {

        // generate a secret key once and always return that secret key
        final SecretKey secretKey = KeyGenerator.getInstance(
                HmacRequestVerifier.HMAC_ALGORITHM.toString()).generateKey();
        Mockito.when(secretKeyProvider.getSecretKey()).thenReturn(secretKey);

        requestVerifier = new HmacRequestVerifier(secretKeyProvider);

    }

    @Test
    @Repeat(1)
    public void testGetSignatureTokenIsRepeatable() throws Exception {

        final String identifier = "comment/1";
        final Instant expirationDate = Instant.now();

        final byte[] firstSignatureToken = requestVerifier.getSignatureToken(identifier, expirationDate);
        final byte[] secondSignatureToken = requestVerifier.getSignatureToken(identifier, expirationDate);

        Truth.assertThat(firstSignatureToken).isEqualTo(secondSignatureToken);
    }
}
