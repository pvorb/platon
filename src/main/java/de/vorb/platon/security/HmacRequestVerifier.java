package de.vorb.platon.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(HmacRequestVerifier.class);

    public static final String MAC_ALGORITHM = "HmacSHA256";

    private Mac mac;

    @Inject
    public HmacRequestVerifier(SecretKeyProvider keyProvider) {
        final SecretKey key = keyProvider.getSecretKey();

        try {
            mac = Mac.getInstance(MAC_ALGORITHM);
            mac.init(key);
        } catch (NoSuchAlgorithmException e) {
            logger.error(String.format("Could not find an implementation of the %s algorithm", MAC_ALGORITHM));
        } catch (InvalidKeyException e) {
            logger.error("The supplied key provider returned an invalid key", e);
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
        final byte[] referenceSignature = getSignatureToken(identifier, expirationDate);

        return Arrays.equals(signatureToken, referenceSignature);
    }

}
