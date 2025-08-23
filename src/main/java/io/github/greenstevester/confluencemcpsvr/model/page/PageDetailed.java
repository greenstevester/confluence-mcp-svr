package io.github.greenstevester.confluencemcpsvr.model.page;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.greenstevester.confluencemcpsvr.model.common.Label;
import io.github.greenstevester.confluencemcpsvr.model.common.Version;

import java.util.List;

/**
 * Extended page object with additional optional fields
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PageDetailed(
    String id,
    String title,
    String spaceId,
    String parentId,
    PageBody body,
    List<Label> labels,
    List<Version> versions,
    Boolean isFavoritedByCurrentUser
) {}