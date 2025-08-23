package io.github.greenstevester.confluencemcpsvr.model.search;

import io.github.greenstevester.confluencemcpsvr.model.enums.ExcerptStrategy;

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