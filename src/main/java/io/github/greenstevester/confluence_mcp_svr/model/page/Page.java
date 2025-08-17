package io.github.greenstevester.confluence_mcp_svr.model.page;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.greenstevester.confluence_mcp_svr.model.common.ResponseLinks;
import io.github.greenstevester.confluence_mcp_svr.model.common.Version;
import io.github.greenstevester.confluence_mcp_svr.model.enums.ContentStatus;

import java.time.LocalDateTime;

/**
 * Represents a Confluence page
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Page(
    String id,
    ContentStatus status,
    String title,
    String spaceId,
    String parentId,
    String parentType,
    Integer position,
    String authorId,
    String ownerId,
    String lastOwnerId,
    LocalDateTime createdAt,
    Version version,
    PageBody body,
    @JsonProperty("_links") ResponseLinks links
) {}