package com.creatoros.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * Social platforms a creator publishes on.
 *
 * <p>
 * The enum constant is what JPA persists; {@link #getLabel()} is what crosses
 * the wire, so the API speaks exactly the strings declared by
 * {@code PlatformType} in the frontend's {@code src/types/creatorOS.ts} and
 * that file needs no changes.
 */
public enum PlatformType {

    YOUTUBE("YouTube"),
    INSTAGRAM("Instagram"),
    LINKEDIN("LinkedIn"),
    X_TWITTER("X (Twitter)"),
    SUBSTACK("Substack"),
    SPOTIFY("Spotify");

    private final String label;

    PlatformType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static PlatformType fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(p -> p.label.equalsIgnoreCase(value) || p.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown platform: " + value));
    }
}
