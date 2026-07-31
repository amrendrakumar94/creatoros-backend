package com.creatoros.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/** Content format promised to a brand. */
public enum DeliverableType {

    YOUTUBE_DEDICATED("YouTube Dedicated"),
    YOUTUBE_INTEGRATED("YouTube Integrated"),
    INSTAGRAM_REEL("Instagram Reel"),
    INSTAGRAM_STORY("Instagram Story"),
    LINKEDIN_POST("LinkedIn Post"),
    PODCAST_MID_ROLL("Podcast Mid-roll");

    private final String label;

    DeliverableType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static DeliverableType fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(t -> t.label.equalsIgnoreCase(value) || t.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown deliverable type: " + value));
    }
}
