package io.github.greenstevester.confluencemcpsvr.service;

import io.github.greenstevester.confluencemcpsvr.client.ConfluenceSpacesClient;
import io.github.greenstevester.confluencemcpsvr.config.ConfluenceProperties;
import io.github.greenstevester.confluencemcpsvr.model.common.PaginatedResponse;
import io.github.greenstevester.confluencemcpsvr.model.dto.CreateSpaceRequest;
import io.github.greenstevester.confluencemcpsvr.model.dto.UpdateSpaceRequest;
import io.github.greenstevester.confluencemcpsvr.model.enums.SpaceStatus;
import io.github.greenstevester.confluencemcpsvr.model.enums.SpaceType;
import io.github.greenstevester.confluencemcpsvr.model.space.Space;
import io.github.greenstevester.confluencemcpsvr.util.MarkdownFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for Confluence spaces operations
 */
@Service
public class ConfluenceSpacesService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluenceSpacesService.class);
    
    private final ConfluenceSpacesClient spacesClient;
    private final ConfluenceProperties confluenceProperties;
    private final MarkdownFormatter markdownFormatter;
    
    public ConfluenceSpacesService(
            ConfluenceSpacesClient spacesClient,
            ConfluenceProperties confluenceProperties,
            MarkdownFormatter markdownFormatter) {
        this.spacesClient = spacesClient;
        this.confluenceProperties = confluenceProperties;
        this.markdownFormatter = markdownFormatter;
    }
    
    /**
     * List spaces with optional filtering
     */
    public Mono<String> listSpaces(
            List<String> ids,
            List<String> keys,
            List<SpaceType> types,
            List<SpaceStatus> statuses,
            Integer limit,
            String cursor) {
        
        logger.debug("Listing spaces with ids: {}, keys: {}", ids, keys);
        
        List<String> typeStrings = types != null ? 
            types.stream().map(SpaceType::getValue).collect(Collectors.toList()) : null;
        List<String> statusStrings = statuses != null ?
            statuses.stream().map(SpaceStatus::getValue).collect(Collectors.toList()) : null;
        
        return spacesClient.listSpaces(
                ids,
                keys,
                typeStrings,
                statusStrings,
                cursor,
                limit != null ? limit : confluenceProperties.defaults().pageSize())
            .map(this::formatSpacesList)
            .doOnSuccess(result -> logger.debug("Formatted spaces list response"))
            .doOnError(error -> logger.error("Error listing spaces", error));
    }
    
    /**
     * Get detailed information about a specific space
     */
    public Mono<String> getSpace(String spaceId) {
        logger.debug("Getting space details for ID: {}", spaceId);
        
        return spacesClient.getSpace(spaceId)
            .map(this::formatSpaceDetails)
            .doOnSuccess(result -> logger.debug("Formatted space details response"))
            .doOnError(error -> logger.error("Error getting space {}", spaceId, error))
            .onErrorReturn("Error getting space: Please check your Confluence connection, permissions, and space ID.");
    }
    
    /**
     * Format a list of spaces for display
     */
    private String formatSpacesList(PaginatedResponse<Space> spacesResponse) {
        List<Space> spaces = spacesResponse.results();
        
        if (spaces == null || spaces.isEmpty()) {
            return "No Confluence spaces found matching your criteria.";
        }
        
        StringBuilder result = new StringBuilder();
        result.append(markdownFormatter.formatHeading("Confluence Spaces", 1))
              .append("\n\n");
        
        for (int i = 0; i < spaces.size(); i++) {
            Space space = spaces.get(i);
            result.append(formatSpaceListItem(space, i + 1));
            
            if (i < spaces.size() - 1) {
                result.append("\n\n").append(markdownFormatter.formatSeparator());
            }
            result.append("\n\n");
        }
        
        // Add pagination info if available
        if (spacesResponse.links() != null && spacesResponse.links().next() != null) {
            result.append(markdownFormatter.formatItalic("More spaces available. Use cursor for pagination."))
                  .append("\n\n");
        }
        
        result.append(markdownFormatter.formatItalic(
            "Space information retrieved at " + markdownFormatter.formatDate(LocalDateTime.now())));
        
        return result.toString();
    }
    
    /**
     * Format a single space item for list display
     */
    private String formatSpaceListItem(Space space, int index) {
        StringBuilder result = new StringBuilder();
        
        result.append(markdownFormatter.formatHeading(space.name(), 2))
              .append("\n\n");
        
        String baseUrl = confluenceProperties.api().baseUrl();
        String spaceUrl = baseUrl + "/spaces/" + space.key();
        
        Map<String, Object> properties = Map.of(
            "ID", space.id(),
            "Key", markdownFormatter.formatInlineCode(space.key()),
            "Type", space.type().getValue(),
            "Status", space.status().getValue(),
            "Created", space.createdAt() != null ? markdownFormatter.formatDate(space.createdAt()) : "Not available",
            "Author", space.authorId() != null ? space.authorId() : "Unknown",
            "Homepage ID", space.homepageId() != null ? space.homepageId() : "N/A",
            "URL", markdownFormatter.formatUrl(spaceUrl, space.name())
        );
        
        result.append(markdownFormatter.formatBulletList(properties, key -> key));
        
        // Add description if available
        if (space.description() != null && space.description().plain() != null) {
            result.append("\n\n")
                  .append(markdownFormatter.formatHeading("Description", 4))
                  .append("\n\n")
                  .append(space.description().plain().value());
        }
        
        return result.toString();
    }
    
    /**
     * Format detailed space information for display
     */
    private String formatSpaceDetails(Space space) {
        StringBuilder result = new StringBuilder();
        
        // Space title and overview
        result.append(markdownFormatter.formatHeading("Confluence Space: " + space.name(), 1))
              .append("\n\n");
        
        String overview = String.format("A %s space with key `%s` and status `%s`",
            space.type().getValue(), space.key(), space.status().getValue());
        result.append(markdownFormatter.formatBlockquote(overview))
              .append("\n\n");
        
        // Basic Information
        result.append(markdownFormatter.formatHeading("Basic Information", 2))
              .append("\n\n");
        
        Map<String, Object> basicInfo = Map.of(
            "ID", space.id(),
            "Name", space.name(),
            "Key", markdownFormatter.formatInlineCode(space.key()),
            "Type", space.type().getValue(),
            "Status", space.status().getValue(),
            "Created", space.createdAt() != null ? markdownFormatter.formatDate(space.createdAt()) : "Not available",
            "Author ID", space.authorId() != null ? space.authorId() : "Unknown",
            "Homepage ID", space.homepageId() != null ? space.homepageId() : "N/A"
        );
        
        result.append(markdownFormatter.formatBulletList(basicInfo, key -> key))
              .append("\n\n");
        
        // Description
        if (space.description() != null) {
            result.append(markdownFormatter.formatHeading("Description", 2))
                  .append("\n\n");
            
            if (space.description().plain() != null && space.description().plain().value() != null) {
                result.append(space.description().plain().value())
                      .append("\n\n");
            } else {
                result.append("No description available.\n\n");
            }
        }
        
        // Space Links
        if (space.links() != null) {
            String baseUrl = confluenceProperties.api().baseUrl();
            String spaceUrl = baseUrl + "/spaces/" + space.key();
            
            result.append(markdownFormatter.formatHeading("Links", 2))
                  .append("\n\n");
            
            Map<String, Object> links = Map.of(
                "Space URL", markdownFormatter.formatUrl(spaceUrl, "View Space"),
                "Base URL", space.links().base() != null ? space.links().base() : "N/A"
            );
            
            result.append(markdownFormatter.formatBulletList(links, key -> key))
                  .append("\n\n");
        }
        
        // Timestamp
        result.append(markdownFormatter.formatItalic(
            "Space information retrieved at " + markdownFormatter.formatDate(LocalDateTime.now())));
        
        return result.toString();
    }
    
    /**
     * Create a new space in Confluence
     */
    public Mono<String> createSpace(CreateSpaceRequest request) {
        logger.debug("Creating space with key: {}", request.key());
        
        return spacesClient.createSpace(request)
            .map(this::formatSpaceCreationResult)
            .doOnSuccess(result -> logger.debug("Successfully created space"))
            .doOnError(error -> logger.error("Error creating space with key: {}", request.key(), error))
            .onErrorReturn("Error creating space: Please check your Confluence connection, permissions, space key format, and parameters.");
    }
    
    /**
     * Format the result of space creation
     */
    private String formatSpaceCreationResult(Space createdSpace) {
        StringBuilder result = new StringBuilder();
        
        // Success message
        result.append(markdownFormatter.formatHeading("✅ Space Created Successfully", 1))
              .append("\n\n");
        
        String overview = String.format("New %s space created with key `%s`", 
            createdSpace.type().getValue(), createdSpace.key());
        result.append(markdownFormatter.formatBlockquote(overview))
              .append("\n\n");
        
        // Created space details
        result.append(markdownFormatter.formatHeading("Created Space Details", 2))
              .append("\n\n");
        
        Map<String, Object> spaceInfo = Map.of(
            "ID", createdSpace.id(),
            "Name", createdSpace.name(),
            "Key", markdownFormatter.formatInlineCode(createdSpace.key()),
            "Type", createdSpace.type().getValue(),
            "Status", createdSpace.status().getValue(),
            "Homepage ID", createdSpace.homepageId() != null ? createdSpace.homepageId() : "N/A",
            "Created At", createdSpace.createdAt() != null ? 
                markdownFormatter.formatDate(createdSpace.createdAt()) : "N/A"
        );
        
        result.append(markdownFormatter.formatBulletList(spaceInfo, key -> key))
              .append("\n\n");
        
        // Description if provided
        if (createdSpace.description() != null && 
            createdSpace.description().plain() != null && 
            createdSpace.description().plain().value() != null) {
            result.append(markdownFormatter.formatHeading("Description", 2))
                  .append("\n\n")
                  .append(createdSpace.description().plain().value())
                  .append("\n\n");
        }
        
        // Access link if available
        if (createdSpace.links() != null) {
            String baseUrl = confluenceProperties.api().baseUrl();
            String spaceUrl = baseUrl + "/spaces/" + createdSpace.key();
            result.append(markdownFormatter.formatHeading("Quick Access", 2))
                  .append("\n\n")
                  .append("View space: ")
                  .append(markdownFormatter.formatUrl(spaceUrl, "Open in Confluence"))
                  .append("\n\n");
        }
        
        // Timestamp
        result.append(markdownFormatter.formatItalic(
            "Space created at " + markdownFormatter.formatDate(LocalDateTime.now())));
        
        return result.toString();
    }
    
    /**
     * Update an existing space in Confluence
     */
    public Mono<String> updateSpace(UpdateSpaceRequest request) {
        logger.debug("Updating space with key: {}", request.spaceKey());
        
        return spacesClient.updateSpace(request)
            .map(this::formatSpaceUpdateResult)
            .doOnSuccess(result -> logger.debug("Successfully updated space"))
            .doOnError(error -> logger.error("Error updating space with key: {}", request.spaceKey(), error))
            .onErrorReturn("Error updating space: Please check your Confluence connection, permissions, and space key.");
    }
    
    /**
     * Format the result of space update
     */
    private String formatSpaceUpdateResult(Space updatedSpace) {
        StringBuilder result = new StringBuilder();
        
        // Success message
        result.append(markdownFormatter.formatHeading("✅ Space Updated Successfully", 1))
              .append("\n\n");
        
        String overview = String.format("%s space `%s` has been updated", 
            updatedSpace.type().getValue(), updatedSpace.key());
        result.append(markdownFormatter.formatBlockquote(overview))
              .append("\n\n");
        
        // Updated space details
        result.append(markdownFormatter.formatHeading("Updated Space Details", 2))
              .append("\n\n");
        
        Map<String, Object> spaceInfo = Map.of(
            "ID", updatedSpace.id(),
            "Name", updatedSpace.name(),
            "Key", markdownFormatter.formatInlineCode(updatedSpace.key()),
            "Type", updatedSpace.type().getValue(),
            "Status", updatedSpace.status().getValue(),
            "Homepage ID", updatedSpace.homepageId() != null ? updatedSpace.homepageId() : "N/A"
        );
        
        result.append(markdownFormatter.formatBulletList(spaceInfo, key -> key))
              .append("\n\n");
        
        // Description if available
        if (updatedSpace.description() != null && 
            updatedSpace.description().plain() != null && 
            updatedSpace.description().plain().value() != null) {
            result.append(markdownFormatter.formatHeading("Description", 2))
                  .append("\n\n")
                  .append(updatedSpace.description().plain().value())
                  .append("\n\n");
        }
        
        // Access link if available
        if (updatedSpace.links() != null) {
            String baseUrl = confluenceProperties.api().baseUrl();
            String spaceUrl = baseUrl + "/spaces/" + updatedSpace.key();
            result.append(markdownFormatter.formatHeading("Quick Access", 2))
                  .append("\n\n")
                  .append("View space: ")
                  .append(markdownFormatter.formatUrl(spaceUrl, "Open in Confluence"))
                  .append("\n\n");
        }
        
        // Timestamp
        result.append(markdownFormatter.formatItalic(
            "Space updated at " + markdownFormatter.formatDate(LocalDateTime.now())));
        
        return result.toString();
    }
}