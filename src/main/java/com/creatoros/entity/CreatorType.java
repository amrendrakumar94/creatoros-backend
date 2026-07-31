package com.creatoros.entity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CreatorType {

    TECH_REVIEWER("Tech Reviewer"),
    FINANCE_AND_INVESTING("Finance & Investing"),
    LIFESTYLE_AND_TRAVEL("Lifestyle & Travel"),
    EDUCATIONAL("Educational"),
    GAMING_AND_ESPORTS("Gaming & Esports"),
    FITNESS_AND_HEALTH("Fitness & Health"),
    BEAUTY_AND_FASHION("Beauty & Fashion"),
    PODCAST_HOST("Podcast Host");

    private final String label;

    CreatorType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static CreatorType fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(c -> c.label.equalsIgnoreCase(value) || c.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown creator type: " + value));
    }
}
