package io.github.greenstevester.confluence_mcp_svr.model.dto;

import io.github.greenstevester.confluence_mcp_svr.model.enums.BodyFormat;
import io.github.greenstevester.confluence_mcp_svr.model.enums.ContentStatus;
import io.github.greenstevester.confluence_mcp_svr.model.enums.PageSortOrder;

import java.util.List;

/**
 * Request DTO for listing pages
 */
public record ListPagesRequest(
    List<String> id,
    List<String> spaceId,
    String parentId,
    PageSortOrder sort,
    List<ContentStatus> status,
    String title,
    String query,
    BodyFormat bodyFormat,
    String cursor,
    Integer limit
) {}