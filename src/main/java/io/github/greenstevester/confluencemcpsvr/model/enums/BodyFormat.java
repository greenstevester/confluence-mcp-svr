package io.github.greenstevester.confluencemcpsvr.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Body format enumeration for Confluence content
 */
public enum BodyFormat {
    STORAGE("storage"),
    ATLAS_DOC_FORMAT("atlas_doc_format"),
    VIEW("view"),
    EXPORT_VIEW("export_view"),
    ANONYMOUS_EXPORT_VIEW("anonymous_export_view"),
    STYLED_VIEW("styled_view"),
    EDITOR("editor");

    private final String value;

    BodyFormat(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static BodyFormat fromValue(String value) {
        for (BodyFormat format : values()) {
            if (format.value.equals(value)) {
                return format;
            }
        }
        throw new IllegalArgumentException("Unknown body format: " + value);
    }
}