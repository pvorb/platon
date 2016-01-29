package de.vorb.platon.security;

import de.vorb.platon.util.CurrentTimeProvider;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;

@Service
public class HmacRequestVerifier implements RequestVerifier {

    public static final HmacAlgorithms HMAC_ALGORITHM = HmacAlgorithms.HMAC_SHA_256;

    private final CurrentTimeProvider currentTimeProvider;

    private final Mac mac;

    @Inject
    public HmacRequestVerifier(SecretKeyProvider keyProvider, CurrentTimeProvider currentTimeProvider) {

        this.currentTimeProvider = currentTimeProvider;

        final SecretKey key = keyProvider.getSecretKey();

        try {
            mac = Mac.getInstance(HMAC_ALGORITHM.toString());
            mac.init(key);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException(
                    String.format("Could not find an implementation of the %s algorithm", HMAC_ALGORITHM), e);
        } catch (InvalidKeyException e) {
            throw new SecurityException("The supplied key provider returned an invalid key", e);
        }
    }

    @Override
    public byte[] getSignatureToken(String identifier, Instant expirationDate) {
        final byte[] signatureSource = getSignatureSource(identifier, expirationDate);

        return mac.doFinal(signatureSource);
    }

    private byte[] getSignatureSource(String identifier, Instant expirationDate) {
        return String.format("%s|%s", identifier, expirationDate).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean isRequestValid(String identifier, Instant expirationDate, byte[] signatureToken) {
        if (currentTimeProvider.get().isAfter(expirationDate)) {
            return false;
        }

        final byte[] referenceSignature = getSignatureToken(identifier, expirationDate);

        return Arrays.equals(signatureToken, referenceSignature);
    }

}
