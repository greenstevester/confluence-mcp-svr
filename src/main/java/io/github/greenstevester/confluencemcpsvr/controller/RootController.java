package io.github.greenstevester.confluencemcpsvr.controller;

import io.github.greenstevester.confluencemcpsvr.config.McpServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root controller providing basic server information and health status
 */
@RestController
public class RootController {

    private final McpServerProperties mcpServerProperties;
    private final Environment environment;
    private final BuildProperties buildProperties;

    @Autowired
    public RootController(McpServerProperties mcpServerProperties, 
                         Environment environment,
                         BuildProperties buildProperties) {
        this.mcpServerProperties = mcpServerProperties;
        this.environment = environment;
        this.buildProperties = buildProperties;
    }

    /**
     * Root endpoint providing server information and status
     */
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> root() {
        Map<String, Object> response = new LinkedHashMap<>();
        
        // Server information
        response.put("server", "Confluence MCP Server");
        response.put("version", buildProperties.getVersion());
        response.put("buildTime", LocalDateTime.ofInstant(buildProperties.getTime(), ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // MCP Server details
        Map<String, Object> mcpInfo = new LinkedHashMap<>();
        mcpInfo.put("name", mcpServerProperties.name());
        mcpInfo.put("version", mcpServerProperties.version());
        mcpInfo.put("description", mcpServerProperties.description());
        response.put("mcp", mcpInfo);
        
        // Runtime information
        Map<String, Object> runtime = new LinkedHashMap<>();
        runtime.put("port", environment.getProperty("server.port", "8081"));
        runtime.put("profiles", environment.getActiveProfiles());
        runtime.put("javaVersion", System.getProperty("java.version"));
        response.put("runtime", runtime);
        
        // Available endpoints
        Map<String, Object> endpoints = new LinkedHashMap<>();
        endpoints.put("health", "/actuator/health");
        endpoints.put("mcp", "/mcp/message");
        endpoints.put("metrics", "/actuator/metrics");
        response.put("endpoints", endpoints);
        
        response.put("status", "UP");
        
        return response;
    }
}