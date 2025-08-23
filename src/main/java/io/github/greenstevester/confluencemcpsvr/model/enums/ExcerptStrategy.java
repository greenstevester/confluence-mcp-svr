package io.github.greenstevester.confluencemcpsvr.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Excerpt strategy enumeration for search results
 */
public enum ExcerptStrategy {
    HIGHLIGHT("highlight"),
    INDEXED("indexed"),
    NONE("none"),
    HIGHLIGHT_UNESCAPED("highlight_unescaped"),
    INDEXED_UNESCAPED("indexed_unescaped");

    private final String value;

    ExcerptStrategy(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ExcerptStrategy fromValue(String value) {
        for (ExcerptStrategy strategy : values()) {
            if (strategy.value.equals(value)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Unknown excerpt strategy: " + value);
    }
}