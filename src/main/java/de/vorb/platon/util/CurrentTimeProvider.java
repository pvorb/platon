package de.vorb.platon.util;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.function.Supplier;

@FunctionalInterface
public interface CurrentTimeProvider extends Supplier<Instant> {}
