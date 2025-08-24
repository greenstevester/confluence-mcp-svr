package io.github.greenstevester.confluencemcpsvr.client;

import io.github.greenstevester.confluencemcpsvr.model.search.SearchRequest;
import io.github.greenstevester.confluencemcpsvr.model.search.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

/**
 * Client for interacting with Confluence Search API
 */
@Component
public class ConfluenceSearchClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluenceSearchClient.class);
    private static final String API_PATH = "/rest/api";
    
    private final WebClient webClient;
    
    public ConfluenceSearchClient(WebClient confluenceWebClient) {
        this.webClient = confluenceWebClient;
    }
    
    /**
     * Search content using CQL (Confluence Query Language)
     */
    public Mono<SearchResponse> search(SearchRequest request) {
        logger.debug("Searching with CQL: {}", request.cql());
        
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        
        queryParams.add("cql", request.cql());
        
        if (request.cqlcontext() != null) {
            queryParams.add("cqlcontext", request.cqlcontext());
        }
        if (request.cursor() != null) {
            queryParams.add("cursor", request.cursor());
        }
        if (request.limit() != null) {
            queryParams.add("limit", request.limit().toString());
        }
        if (request.start() != null) {
            queryParams.add("start", request.start().toString());
        }
        if (request.includeArchivedSpaces() != null) {
            queryParams.add("includeArchivedSpaces", request.includeArchivedSpaces().toString());
        }
        if (request.excludeCurrentSpaces() != null) {
            queryParams.add("excludeCurrentSpaces", request.excludeCurrentSpaces().toString());
        }
        if (request.excerpt() != null) {
            queryParams.add("excerpt", request.excerpt().getValue());
        }
        
        String uri = UriComponentsBuilder.fromPath(API_PATH + "/search")
            .queryParams(queryParams)
            .toUriString();
            
        logger.debug("Making search request to: {}", uri);
        
        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(SearchResponse.class)
            .doOnSuccess(response -> logger.debug("Search completed with {} results", 
                response != null && response.results() != null ? response.results().size() : 0))
            .doOnError(error -> logger.error("Error during search: {}", error.getMessage(), error));
    }
}