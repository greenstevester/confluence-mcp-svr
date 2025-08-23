package io.github.greenstevester.confluencemcpsvr.client;

import io.github.greenstevester.confluencemcpsvr.model.common.PaginatedResponse;
import io.github.greenstevester.confluencemcpsvr.model.space.Space;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Client for interacting with Confluence Spaces API
 */
@Component
public class ConfluenceSpacesClient {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluenceSpacesClient.class);
    private static final String API_PATH = "/rest/api";
    
    private final WebClient webClient;
    
    public ConfluenceSpacesClient(WebClient confluenceWebClient) {
        this.webClient = confluenceWebClient;
    }
    
    /**
     * List spaces from Confluence
     */
    public Mono<PaginatedResponse<Space>> listSpaces(List<String> ids, 
                                                    List<String> keys, 
                                                    List<String> types, 
                                                    List<String> statuses,
                                                    String cursor,
                                                    Integer limit) {
        logger.debug("Listing spaces with parameters");
        
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        
        if (ids != null && !ids.isEmpty()) {
            queryParams.put("ids", ids);
        }
        if (keys != null && !keys.isEmpty()) {
            queryParams.put("keys", keys);
        }
        if (types != null && !types.isEmpty()) {
            queryParams.put("types", types);
        }
        if (statuses != null && !statuses.isEmpty()) {
            queryParams.put("statuses", statuses);
        }
        if (cursor != null) {
            queryParams.add("cursor", cursor);
        }
        if (limit != null) {
            queryParams.add("limit", limit.toString());
        }
        
        String uri = UriComponentsBuilder.fromPath(API_PATH + "/space")
            .queryParams(queryParams)
            .toUriString();
            
        logger.debug("Making request to: {}", uri);
        
        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<Space>>() {})
            .doOnSuccess(response -> logger.debug("Successfully retrieved {} spaces", 
                response != null ? response.results().size() : 0))
            .doOnError(error -> logger.error("Error listing spaces", error));
    }
    
    /**
     * Get a specific space by ID
     */
    public Mono<Space> getSpace(String spaceId) {
        logger.debug("Getting space with ID: {}", spaceId);
        
        String uri = API_PATH + "/spaces/" + spaceId;
        
        logger.debug("Making request to: {}", uri);
        
        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(Space.class)
            .doOnSuccess(space -> logger.debug("Successfully retrieved space: {}", 
                space != null ? space.name() : "null"))
            .doOnError(error -> logger.error("Error getting space {}", spaceId, error));
    }
}