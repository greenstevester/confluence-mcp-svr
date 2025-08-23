package io.github.greenstevester.confluencemcpsvr.model.dto;

import io.github.greenstevester.confluencemcpsvr.model.enums.BodyFormat;
import io.github.greenstevester.confluencemcpsvr.model.enums.ContentStatus;

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