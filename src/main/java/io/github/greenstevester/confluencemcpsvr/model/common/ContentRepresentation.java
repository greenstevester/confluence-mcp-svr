package io.github.greenstevester.confluencemcpsvr.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents content in a specific format (storage, view, etc.)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ContentRepresentation(
    String value,
    String representation
) {}