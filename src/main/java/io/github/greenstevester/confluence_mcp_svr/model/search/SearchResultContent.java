package io.github.greenstevester.confluence_mcp_svr.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents search result content details
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResultContent(
    String id,
    String type,
    String status,
    String title,
    SearchResultSpace space,
    SearchResultHistory history,
    SearchResultVersion version,
    SearchResultLinks links
) {}