package io.github.greenstevester.confluencemcpsvr.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Page status enumeration for Confluence content
 */
public enum ContentStatus {
    CURRENT("current"),
    TRASHED("trashed"),
    DELETED("deleted"),
    DRAFT("draft"),
    ARCHIVED("archived"),
    HISTORICAL("historical");

    private final String value;

    ContentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ContentStatus fromValue(String value) {
        for (ContentStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown content status: " + value);
    }
}