package de.vorb.platon.util;

import java.time.Instant;

public class SystemTimeProvider implements TimeProvider {

    @Override
    public Instant getCurrentTime() {
        return Instant.now();
    }

}
