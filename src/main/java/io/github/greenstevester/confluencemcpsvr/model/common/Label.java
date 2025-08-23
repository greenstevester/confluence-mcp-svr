package io.github.greenstevester.confluencemcpsvr.model.common;

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