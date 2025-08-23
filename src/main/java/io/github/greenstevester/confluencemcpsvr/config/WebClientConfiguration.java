package io.github.greenstevester.confluencemcpsvr.config;

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

    @Bean
    public WebClient confluenceWebClient(ConfluenceProperties confluenceProperties) {
        // Create HttpClient with redirect handling
        HttpClient httpClient = HttpClient.create()
            .followRedirect(true);
        
        // Use Bearer token authentication
        String token = confluenceProperties.api().token();
        
        return WebClient.builder()
            .baseUrl(confluenceProperties.api().baseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "MCP-Confluence-Server/2.0.1")
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}