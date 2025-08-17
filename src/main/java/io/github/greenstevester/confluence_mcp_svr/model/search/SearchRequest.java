package io.github.greenstevester.confluence_mcp_svr.model.search;

import io.github.greenstevester.confluence_mcp_svr.model.enums.ExcerptStrategy;

/**
 * Search request parameters for Confluence CQL search
 */
public record SearchRequest(
    String cql,
    String cqlcontext,
    String cursor,
    Integer limit,
    Integer start,
    Boolean includeArchivedSpaces,
    Boolean excludeCurrentSpaces,
    ExcerptStrategy excerpt
) {}