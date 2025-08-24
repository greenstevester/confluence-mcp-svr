package io.github.greenstevester.confluencemcpsvr.client;

import io.github.greenstevester.confluencemcpsvr.model.common.PaginatedResponse;
import io.github.greenstevester.confluencemcpsvr.model.dto.CreateSpaceRequest;
import io.github.greenstevester.confluencemcpsvr.model.dto.UpdateSpaceRequest;
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
        
        String uri = API_PATH + "/space/" + spaceId;
        
        logger.debug("Making request to: {}", uri);
        
        return webClient.get()
            .uri(uri)
            .retrieve()
            .bodyToMono(Space.class)
            .doOnSuccess(space -> logger.debug("Successfully retrieved space: {}", 
                space != null ? space.name() : "null"))
            .doOnError(error -> logger.error("Error getting space {}", spaceId, error));
    }
    
    /**
     * Create a new space in Confluence
     */
    public Mono<Space> createSpace(CreateSpaceRequest request) {
        logger.debug("Creating space with key: {}", request.key());
        
        // Build the request body for Confluence API
        var requestBody = new java.util.HashMap<String, Object>();
        requestBody.put("key", request.key());
        requestBody.put("name", request.name());
        requestBody.put("type", request.type().getValue());
        requestBody.put("status", request.status().getValue());
        
        // Add description if provided
        if (request.description() != null && !request.description().trim().isEmpty()) {
            var description = new java.util.HashMap<String, Object>();
            var plain = new java.util.HashMap<String, String>();
            plain.put("value", request.description());
            plain.put("representation", "plain");
            description.put("plain", plain);
            requestBody.put("description", description);
        }
        
        String uri = API_PATH + "/space";
        
        logger.debug("Making POST request to: {}", uri);
        
        return webClient.post()
            .uri(uri)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Space.class)
            .doOnSuccess(space -> logger.debug("Successfully created space: {}", 
                space != null ? space.name() : "null"))
            .doOnError(error -> logger.error("Error creating space with key: {}", request.key(), error));
    }
    
    /**
     * Update an existing space in Confluence
     */
    public Mono<Space> updateSpace(UpdateSpaceRequest request) {
        logger.debug("Updating space with key: {}", request.spaceKey());
        
        // Build the request body for Confluence API
        var requestBody = new java.util.HashMap<String, Object>();
        
        // Only include fields that are being updated
        if (request.name() != null) {
            requestBody.put("name", request.name());
        }
        
        if (request.type() != null) {
            requestBody.put("type", request.type().getValue());
        }
        
        if (request.status() != null) {
            requestBody.put("status", request.status().getValue());
        }
        
        // Add description if provided
        if (request.description() != null) {
            var description = new java.util.HashMap<String, Object>();
            var plain = new java.util.HashMap<String, String>();
            plain.put("value", request.description());
            plain.put("representation", "plain");
            description.put("plain", plain);
            requestBody.put("description", description);
        }
        
        String uri = API_PATH + "/space/" + request.spaceKey();
        
        logger.debug("Making PUT request to: {}", uri);
        
        return webClient.put()
            .uri(uri)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Space.class)
            .doOnSuccess(space -> logger.debug("Successfully updated space: {}", 
                space != null ? space.name() : "null"))
            .doOnError(error -> logger.error("Error updating space with key: {}", request.spaceKey(), error));
    }
}