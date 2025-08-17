package io.github.greenstevester.confluence_mcp_svr.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Represents a version of content in Confluence
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Version(
    LocalDateTime createdAt,
    String message,
    int number,
    Boolean minorEdit,
    String authorId
) {}