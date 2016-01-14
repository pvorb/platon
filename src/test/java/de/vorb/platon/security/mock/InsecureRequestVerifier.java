package de.vorb.platon.security.mock;

import de.vorb.platon.security.RequestVerifier;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Arrays;

@Service
public class InsecureRequestVerifier implements RequestVerifier {

    @Override
    public byte[] getSignatureToken(String identifier, Instant expirationDate) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(
                    String.format("%s|%s", identifier, expirationDate).getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isRequestValid(String identifier, Instant expirationDate, byte[] signatureToken) {
        final byte[] referenceSignature = getSignatureToken(identifier, expirationDate);
        return Arrays.equals(referenceSignature, signatureToken);
    }

}
