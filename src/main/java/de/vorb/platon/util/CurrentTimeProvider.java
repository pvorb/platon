package de.vorb.platon.util;

import java.time.Instant;
import java.util.function.Supplier;

@FunctionalInterface
public interface CurrentTimeProvider extends Supplier<Instant> {}
