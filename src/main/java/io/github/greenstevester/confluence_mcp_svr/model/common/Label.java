package io.github.greenstevester.confluence_mcp_svr.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a Confluence label
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Label(
    String id,
    String name,
    String prefix
) {}