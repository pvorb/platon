package de.vorb.platon.security;

import java.time.Instant;

public interface RequestVerifier {

    byte[] getSignatureToken(String identifier, Instant expirationDate);

    boolean isRequestValid(String identifier, Instant expirationDate, byte[] signatureToken);

}
