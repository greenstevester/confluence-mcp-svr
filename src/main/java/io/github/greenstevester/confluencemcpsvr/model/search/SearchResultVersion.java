package io.github.greenstevester.confluencemcpsvr.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Represents version information in search results
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchResultVersion(
    LocalDateTime when,
    Integer number,
    Boolean minorEdit
) {}