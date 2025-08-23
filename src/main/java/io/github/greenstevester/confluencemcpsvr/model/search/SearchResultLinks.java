package io.github.greenstevester.confluencemcpsvr.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents links in search results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResultLinks(
    String webui,
    String self,
    String tinyui
) {}