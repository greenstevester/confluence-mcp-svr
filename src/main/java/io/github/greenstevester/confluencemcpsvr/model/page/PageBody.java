package io.github.greenstevester.confluencemcpsvr.model.page;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.greenstevester.confluencemcpsvr.model.common.ContentRepresentation;

/**
 * Represents the body content of a Confluence page in different formats
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PageBody(
    ContentRepresentation storage,
    @JsonProperty("atlas_doc_format") ContentRepresentation atlasDocFormat,
    ContentRepresentation view
) {}