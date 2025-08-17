package io.github.greenstevester.confluence_mcp_svr.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents content in a specific format (storage, view, etc.)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContentRepresentation(
    String value,
    String representation
) {}