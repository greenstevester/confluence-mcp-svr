package io.github.greenstevester.confluence_mcp_svr.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a search result from Confluence
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResult(
    SearchResultContent content,
    String title,
    String excerpt,
    String url,
    String entityType,
    String iconCssClass,
    LocalDateTime lastModified,
    String friendlyLastModified,
    Double score,
    SearchResultContainer resultGlobalContainer,
    List<Object> breadcrumbs
) {}

