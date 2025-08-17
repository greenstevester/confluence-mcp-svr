package io.github.greenstevester.confluence_mcp_svr.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Configuration for WebClient used to communicate with Confluence API
 */
@Configuration
@EnableConfigurationProperties({ConfluenceProperties.class, McpServerProperties.class})
public class WebClientConfiguration {

    @Bean
    public WebClient confluenceWebClient(ConfluenceProperties confluenceProperties) {
        String credentials = confluenceProperties.api().username() + ":" + confluenceProperties.api().token();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        
        return WebClient.builder()
            .baseUrl(confluenceProperties.api().baseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "MCP-Confluence-Server/2.0.1")
            .build();
    }
}