package io.github.greenstevester.confluence_mcp_svr.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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