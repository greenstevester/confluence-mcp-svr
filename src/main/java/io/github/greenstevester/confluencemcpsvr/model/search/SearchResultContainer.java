package io.github.greenstevester.confluencemcpsvr.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents container information in search results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResultContainer(
    String title,
    String displayUrl
) {}