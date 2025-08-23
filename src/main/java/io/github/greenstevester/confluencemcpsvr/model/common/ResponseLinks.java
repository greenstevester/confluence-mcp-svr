package io.github.greenstevester.confluencemcpsvr.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents response links in Confluence API responses
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseLinks(
    String next,
    String base,
    String self,
    String context
) {}