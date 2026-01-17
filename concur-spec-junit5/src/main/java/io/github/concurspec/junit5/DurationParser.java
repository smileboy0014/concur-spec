package io.github.concurspec.junit5;

import java.time.Duration;

final class DurationParser {
    private DurationParser() {
    }

    static Duration parse(String s) {
        String v = s.trim().toLowerCase();
        if (v.endsWith("ms")) return Duration.ofMillis(Long.parseLong(v.substring(0, v.length() - 2)));
        if (v.endsWith("s")) return Duration.ofSeconds(Long.parseLong(v.substring(0, v.length() - 1)));
        if (v.endsWith("m")) return Duration.ofMinutes(Long.parseLong(v.substring(0, v.length() - 1)));
        if (v.endsWith("h")) return Duration.ofHours(Long.parseLong(v.substring(0, v.length() - 1)));
        throw new IllegalArgumentException("Unsupported duration format: " + s + " (use ms/s/m/h)");
    }
}
