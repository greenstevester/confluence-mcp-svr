package io.github.greenstevester.confluencemcpsvr.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents history information in search results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResultHistory(
    Boolean latest
) {}