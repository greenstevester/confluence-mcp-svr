package io.github.greenstevester.confluencemcpsvr.model.space;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents space icon information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SpaceIcon(
    String path,
    String apiDownloadLink
) {}