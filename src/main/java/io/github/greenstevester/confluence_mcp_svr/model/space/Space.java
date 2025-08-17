package io.github.greenstevester.confluence_mcp_svr.model.space;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.greenstevester.confluence_mcp_svr.model.common.ContentRepresentation;
import io.github.greenstevester.confluence_mcp_svr.model.common.ResponseLinks;
import io.github.greenstevester.confluence_mcp_svr.model.enums.SpaceStatus;
import io.github.greenstevester.confluence_mcp_svr.model.enums.SpaceType;

import java.time.LocalDateTime;

/**
 * Represents a Confluence space
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Space(
    String id,
    String key,
    String name,
    SpaceType type,
    SpaceStatus status,
    String authorId,
    LocalDateTime createdAt,
    String homepageId,
    SpaceDescription description,
    SpaceIcon icon,
    @JsonProperty("_links") ResponseLinks links
) {}

