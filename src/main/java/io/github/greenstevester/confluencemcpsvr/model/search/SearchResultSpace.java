package io.github.greenstevester.confluencemcpsvr.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents space information in search results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResultSpace(
    String key,
    String name,
    String type,
    String status
) {}