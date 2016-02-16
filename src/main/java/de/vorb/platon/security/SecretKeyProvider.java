package de.vorb.platon.security;

import javax.crypto.SecretKey;

public interface SecretKeyProvider {

    SecretKey getSecretKey();

}
