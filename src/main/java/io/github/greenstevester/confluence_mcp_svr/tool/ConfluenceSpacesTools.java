package io.github.greenstevester.confluence_mcp_svr.tool;

import io.github.greenstevester.confluence_mcp_svr.model.enums.SpaceStatus;
import io.github.greenstevester.confluence_mcp_svr.model.enums.SpaceType;
import io.github.greenstevester.confluence_mcp_svr.service.ConfluenceSpacesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.ai.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MCP Tools for Confluence Spaces operations
 */
@Service
public class ConfluenceSpacesTools {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluenceSpacesTools.class);
    
    private final ConfluenceSpacesService spacesService;
    
    public ConfluenceSpacesTools(ConfluenceSpacesService spacesService) {
        this.spacesService = spacesService;
    }
    
    /**
     * List Confluence spaces with optional filtering.
     * 
     * List Confluence spaces with optional filtering by IDs, keys, types, or status.
     * 
     * PURPOSE: Discover and browse available Confluence spaces. Provides space metadata 
     * including IDs and keys needed for other operations.
     * 
     * WHEN TO USE:
     * - To discover available spaces in the Confluence instance.
     * - To find space IDs or keys for use with page tools or search.
     * - To get an overview of spaces before drilling down to pages.
     * - To find spaces by type (personal, team, knowledge base, etc.).
     * - To check space status (current vs archived).
     * 
     * WHEN NOT TO USE:
     * - When you already know the specific space ID/key and need details (use 'get_space').
     * - When you need page content (use page tools).
     * - When you need to search content (use 'search' tool).
     * 
     * SPACE TYPES:
     * - GLOBAL: Organization-wide spaces
     * - PERSONAL: Individual user spaces  
     * - COLLABORATION: Team collaboration spaces
     * - KNOWLEDGE_BASE: Documentation and knowledge sharing spaces
     * 
     * RETURNS: Formatted list of spaces including:
     * - Space ID (numeric) and key (string identifier)
     * - Name, type, and status
     * - Creation date and author information
     * - Description and homepage information
     * - Direct links to spaces
     * 
     * EXAMPLES:
     * - List all spaces: { }
     * - List specific spaces by key: { "keys": ["DEV", "PROD", "DOCS"] }
     * - List by type: { "types": ["COLLABORATION", "KNOWLEDGE_BASE"] }
     * - List current spaces only: { "statuses": ["CURRENT"] }
     * - Paginate results: { "limit": 10, "cursor": "some-cursor-value" }
     * - Multiple filters: { "types": ["GLOBAL"], "statuses": ["CURRENT"], "limit": 20 }
     * 
     * ERRORS:
     * - Authentication failures: Check Confluence credentials.
     * - Permission denied: User may not have access to list spaces.
     */
    public String listSpaces(ListSpacesRequest request) {
        logger.debug("list_spaces tool called with: {}", request);
        
        try {
            return spacesService.listSpaces(
                request.ids(),
                request.keys(),
                request.types(),
                request.statuses(),
                request.limit(),
                request.cursor()
            ).block(); // Block for synchronous tool execution
            
        } catch (Exception e) {
            logger.error("Error in list_spaces tool", e);
            return "Error listing spaces: " + e.getMessage();
        }
    }
    
    /**
     * Get detailed information about a specific Confluence space.
     * 
     * Get detailed information about a specific Confluence space by its ID.
     * 
     * PURPOSE: Retrieve comprehensive details about a single Confluence space.
     * 
     * WHEN TO USE:
     * - When you have a specific space ID and need detailed information.
     * - When you need space metadata, description, and configuration details.
     * - When you need to understand space structure before accessing pages.
     * 
     * WHEN NOT TO USE:
     * - When you need to find spaces (use 'list_spaces' instead).
     * - When you need page content from the space (use page tools).
     * - When you need to search within the space (use 'search' tool).
     * 
     * RETURNS: Detailed space information including:
     * - Complete space metadata (ID, key, name, type, status)
     * - Full description and purpose
     * - Creation and authorship information
     * - Homepage and navigation details
     * - Access and permission information
     * - Direct links and URLs
     * 
     * EXAMPLES:
     * - Get space details: { "spaceId": "123456" }
     * 
     * ERRORS:
     * - Space not found: Verify the space ID is correct and exists.
     * - Access denied: Check permissions for the space.
     * - Authentication failures: Check Confluence credentials.
     */
    public String getSpace(GetSpaceRequest request) {
        logger.debug("get_space tool called with: {}", request);
        
        try {
            return spacesService.getSpace(request.spaceId()).block(); // Block for synchronous tool execution
            
        } catch (Exception e) {
            logger.error("Error in get_space tool", e);
            return "Error getting space: " + e.getMessage();
        }
    }
    
    /**
     * Request object for list_spaces tool
     */
    public record ListSpacesRequest(
        List<String> ids,
        List<String> keys,
        List<SpaceType> types,
        List<SpaceStatus> statuses,
        Integer limit,
        String cursor
    ) {}
    
    /**
     * Request object for get_space tool
     */
    public record GetSpaceRequest(
        String spaceId
    ) {}
}