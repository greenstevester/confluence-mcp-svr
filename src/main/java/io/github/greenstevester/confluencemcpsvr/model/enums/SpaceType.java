package io.github.greenstevester.confluencemcpsvr.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Space type enumeration for Confluence spaces
 */
public enum SpaceType {
    GLOBAL("global"),
    PERSONAL("personal"),
    COLLABORATION("collaboration"),
    KNOWLEDGE_BASE("knowledge_base");

    private final String value;

    SpaceType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static SpaceType fromValue(String value) {
        for (SpaceType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown space type: " + value);
    }
}