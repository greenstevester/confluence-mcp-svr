package io.github.greenstevester.confluencemcpsvr.client;

import io.github.greenstevester.confluencemcpsvr.model.common.PaginatedResponse;
import io.github.greenstevester.confluencemcpsvr.model.dto.GetPageRequest;
import io.github.greenstevester.confluencemcpsvr.model.dto.ListPagesRequest;
import io.github.greenstevester.confluencemcpsvr.model.page.Page;
import io.github.greenstevester.confluencemcpsvr.model.page.PageDetailed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Client for interacting with Confluence Pages API
 */
@Component
public class ConfluencePagesClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluencePagesClient.class);
    private static final String API_PATH = "/rest/api";
    
    private final WebClient webClient;
    
    public ConfluencePagesClient(WebClient confluenceWebClient) {
        this.webClient = confluenceWebClient;
    }
    
    /**
     * List pages from Confluence
     */
    public Mono<PaginatedResponse<Page>> listPages(ListPagesRequest request) {
        logger.debug("Listing pages with request: {}", request);
        
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        
        if (request.id() != null && !request.id().isEmpty()) {
            queryParams.put("id", request.id());
        }
        if (request.spaceId() != null && !request.spaceId().isEmpty()) {
            queryParams.put("spaceKey", request.spaceId());
        }
        if (request.parentId() != null) {
            queryParams.add("ancestor", request.parentId());
        }
        if (request.sort() != null) {
            queryParams.add("sort", request.sort().getValue());
        }
        if (request.status() != null && !request.status().isEmpty()) {
            String statusValues = request.status().stream()
                .map(status -> status.getValue())
                .collect(Collectors.joining(","));
            queryParams.add("status", statusValues);
        }
        if (request.title() != null) {
            queryParams.add("title", request.title());
        }
        if (request.bodyFormat() != null) {
            queryParams.add("expand", "body." + request.bodyFormat().getValue());
        }
        if (request.cursor() != null) {
            queryParams.add("cursor", request.cursor());
        }
        if (request.limit() != null) {
            queryParams.add("limit", request.limit().toString());
        }
        
        String uri = UriComponentsBuilder.fromPath(API_PATH + "/content")
            .queryParams(queryParams)
            .toUriString();
            
        logger.debug("Making request to: {}", uri);
        
        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<Page>>() {})
            .doOnSuccess(response -> logger.debug("Successfully retrieved {} pages", 
                response != null ? response.results().size() : 0))
            .doOnError(error -> logger.error("Error listing pages", error));
    }
    
    /**
     * Get a specific page by ID
     */
    public Mono<PageDetailed> getPage(String pageId, GetPageRequest request) {
        logger.debug("Getting page {} with request: {}", pageId, request);
        
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        
        if (request.bodyFormat() != null) {
            queryParams.add("expand", "body." + request.bodyFormat().getValue());
        }
        if (request.getDraft() != null) {
            queryParams.add("status", request.getDraft() ? "draft" : "current");
        }
        if (request.status() != null && !request.status().isEmpty()) {
            String statusValues = request.status().stream()
                .map(status -> status.getValue())
                .collect(Collectors.joining(","));
            queryParams.add("status", statusValues);
        }
        if (request.version() != null) {
            queryParams.add("version", request.version().toString());
        }
        if (request.includeLabels() != null) {
            queryParams.add("include-labels", request.includeLabels().toString());
        }
        if (request.includeProperties() != null) {
            queryParams.add("include-properties", request.includeProperties().toString());
        }
        if (request.includeVersions() != null) {
            queryParams.add("include-versions", request.includeVersions().toString());
        }
        if (request.includeVersion() != null) {
            queryParams.add("include-version", request.includeVersion().toString());
        }
        if (request.includeWebresources() != null) {
            queryParams.add("include-webresources", request.includeWebresources().toString());
        }
        if (request.includeCollaborators() != null) {
            queryParams.add("include-collaborators", request.includeCollaborators().toString());
        }
        
        String uri = UriComponentsBuilder.fromPath(API_PATH + "/content/" + pageId)
            .queryParams(queryParams)
            .toUriString();
            
        logger.debug("Making request to: {}", uri);
        
        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(PageDetailed.class)
            .doOnSuccess(page -> logger.debug("Successfully retrieved page: {}", 
                page != null ? page.title() : "null"))
            .doOnError(error -> logger.error("Error getting page {}", pageId, error));
    }
}