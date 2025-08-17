package io.github.greenstevester.confluence_mcp_svr.model.dto;

import io.github.greenstevester.confluence_mcp_svr.model.enums.BodyFormat;
import io.github.greenstevester.confluence_mcp_svr.model.enums.ContentStatus;

import java.util.List;

/**
 * Request DTO for getting a specific page
 */
public record GetPageRequest(
    String pageId,
    BodyFormat bodyFormat,
    Boolean getDraft,
    List<ContentStatus> status,
    Integer version,
    Boolean includeLabels,
    Boolean includeProperties,
    Boolean includeOperations,
    Boolean includeLikes,
    Boolean includeVersions,
    Boolean includeVersion,
    Boolean includeFavoritedByCurrentUserStatus,
    Boolean includeWebresources,
    Boolean includeCollaborators
) {}