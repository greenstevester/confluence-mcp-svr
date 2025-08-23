package io.github.greenstevester.confluencemcpsvr.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Sort order enumeration for Confluence pages
 */
public enum PageSortOrder {
    ID("id"),
    ID_DESC("-id"),
    CREATED_DATE("created-date"),
    CREATED_DATE_DESC("-created-date"),
    MODIFIED_DATE("modified-date"),
    MODIFIED_DATE_DESC("-modified-date"),
    TITLE("title"),
    TITLE_DESC("-title");

    private final String value;

    PageSortOrder(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static PageSortOrder fromValue(String value) {
        for (PageSortOrder order : values()) {
            if (order.value.equals(value)) {
                return order;
            }
        }
        throw new IllegalArgumentException("Unknown sort order: " + value);
    }
}