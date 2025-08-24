package io.github.greenstevester.confluencemcpsvr.client;

import io.github.greenstevester.confluencemcpsvr.model.common.PaginatedResponse;
import io.github.greenstevester.confluencemcpsvr.model.dto.CreatePageRequest;
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
    
    /**
     * Create a new page in Confluence
     */
    public Mono<PageDetailed> createPage(CreatePageRequest request) {
        logger.debug("Creating page with title: {}", request.title());
        
        // Build the request body for Confluence API
        var requestBody = new java.util.HashMap<String, Object>();
        requestBody.put("type", "page");
        requestBody.put("title", request.title());
        
        // Space information
        var space = new java.util.HashMap<String, String>();
        space.put("key", request.spaceKey());
        requestBody.put("space", space);
        
        // Parent page if specified
        if (request.parentId() != null) {
            var ancestors = new java.util.ArrayList<>();
            var parent = new java.util.HashMap<String, String>();
            parent.put("id", request.parentId());
            ancestors.add(parent);
            requestBody.put("ancestors", ancestors);
        }
        
        // Page body content
        var body = new java.util.HashMap<String, Object>();
        var storage = new java.util.HashMap<String, String>();
        storage.put("value", request.content());
        storage.put("representation", request.contentRepresentation());
        body.put("storage", storage);
        requestBody.put("body", body);
        
        // Status
        requestBody.put("status", request.status().getValue());
        
        String uri = API_PATH + "/content";
        
        logger.debug("Making POST request to: {}", uri);
        
        return webClient.post()
            .uri(uri)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(PageDetailed.class)
            .doOnSuccess(page -> logger.debug("Successfully created page: {}", 
                page != null ? page.title() : "null"))
            .doOnError(error -> logger.error("Error creating page with title: {}", request.title(), error));
    }
}