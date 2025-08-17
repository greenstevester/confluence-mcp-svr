package io.github.greenstevester.confluence_mcp_svr.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Space status enumeration for Confluence spaces
 */
public enum SpaceStatus {
    CURRENT("current"),
    ARCHIVED("archived");

    private final String value;

    SpaceStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static SpaceStatus fromValue(String value) {
        for (SpaceStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown space status: " + value);
    }
}