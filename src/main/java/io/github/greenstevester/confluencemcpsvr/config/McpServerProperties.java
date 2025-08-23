package io.github.greenstevester.confluencemcpsvr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Configuration properties for MCP server metadata
 */
@ConfigurationProperties(prefix = "mcp.server")
@Validated
public record McpServerProperties(
    @NotBlank String name,
    @NotBlank String version,
    @NotBlank String description
) {}