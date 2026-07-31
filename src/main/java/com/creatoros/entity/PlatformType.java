package com.creatoros.entity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
