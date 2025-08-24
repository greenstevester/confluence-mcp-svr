package io.github.greenstevester.confluencemcpsvr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Configuration for WebClient used to communicate with Confluence API
 */
@Configuration
@EnableConfigurationProperties({ConfluenceProperties.class, McpServerProperties.class})
public class WebClientConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(WebClientConfiguration.class);

    @Bean
    public WebClient confluenceWebClient(ConfluenceProperties confluenceProperties) {
        // Create HttpClient with redirect handling
        HttpClient httpClient = HttpClient.create()
            .followRedirect(true);
        
        // Log MacOS detection - the native DNS library should be automatically picked up if available
        if (isMacOS()) {
            logger.info("Detected MacOS - MacOS native DNS resolver will be used if dependency is available");
        }
        
        // Use Bearer token authentication with validation
        String token = confluenceProperties.api().token();
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalStateException("Confluence API token is required but not configured. Please set CONFLUENCE_API_TOKEN environment variable.");
        }
        
        return WebClient.builder()
            .baseUrl(confluenceProperties.api().baseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token.trim())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "MCP-Confluence-Server/2.0.1")
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
    
    private boolean isMacOS() {
        String osName = System.getProperty("os.name", "").toLowerCase();
        return osName.contains("mac");
    }
}