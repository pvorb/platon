package de.vorb.platon.util;

@FunctionalInterface
public interface InputSanitizer {

    String sanitize(String input);

}
