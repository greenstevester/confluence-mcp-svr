package io.github.greenstevester.confluence_mcp_svr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;

/**
 * Configuration properties for Confluence API integration
 */
@ConfigurationProperties(prefix = "confluence")
@Validated
public record ConfluenceProperties(
    @Valid @NotNull Api api,
    @Valid @NotNull Defaults defaults
) {
    
    public record Api(
        @NotBlank String baseUrl,
        @NotBlank String username,
        @NotBlank String token,
        @NotNull Duration timeout,
        @Positive int maxConnections,
        @Positive int retryAttempts
    ) {}
    
    public record Defaults(
        @Positive int pageSize,
        @NotBlank String bodyFormat,
        boolean includeLabels,
        boolean includeProperties,
        boolean includeWebresources,
        boolean includeCollaborators,
        boolean includeVersion
    ) {}
}