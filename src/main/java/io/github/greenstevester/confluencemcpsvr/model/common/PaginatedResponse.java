package io.github.greenstevester.confluencemcpsvr.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Generic paginated response structure for Confluence API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PaginatedResponse<T>(
    List<T> results,
    @JsonProperty("_links") ResponseLinks links
) {}