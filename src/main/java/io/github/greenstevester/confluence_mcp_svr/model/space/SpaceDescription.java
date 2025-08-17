package io.github.greenstevester.confluence_mcp_svr.model.space;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.greenstevester.confluence_mcp_svr.model.common.ContentRepresentation;

/**
 * Represents space description in different formats
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SpaceDescription(
    ContentRepresentation plain,
    ContentRepresentation view
) {}