package io.github.greenstevester.confluencemcpsvr.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.greenstevester.confluencemcpsvr.model.common.ResponseLinks;

import java.util.List;

/**
 * Response from Confluence search API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResponse(
    List<SearchResult> results,
    Integer limit,
    Integer size,
    Integer start,
    Integer totalSize,
    String cqlQuery,
    Integer searchDuration,
    @JsonProperty("_links") ResponseLinks links
) {}