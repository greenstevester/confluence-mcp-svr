package io.github.greenstevester.confluencemcpsvr.tool;

import io.github.greenstevester.confluencemcpsvr.model.dto.CreatePageRequest;
import io.github.greenstevester.confluencemcpsvr.model.dto.UpdatePageRequest;
import io.github.greenstevester.confluencemcpsvr.model.enums.ContentStatus;
import io.github.greenstevester.confluencemcpsvr.model.enums.PageSortOrder;
import io.github.greenstevester.confluencemcpsvr.service.ConfluencePagesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.greenstevester.confluencemcpsvr.annotation.AITool;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MCP Tools for Confluence Pages operations
 */
@Service
public class ConfluencePagesTools {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluencePagesTools.class);
    
    private final ConfluencePagesService pagesService;
    
    public ConfluencePagesTools(ConfluencePagesService pagesService) {
        this.pagesService = pagesService;
    }
    
    /**
     * List Confluence pages with optional filtering
     */
    @AITool(name = "list-pages", description = "List Confluence pages with optional filtering")
    public String listPages(ListPagesRequest request) {
        logger.debug("list_pages tool called with: {}", request);
        
        try {
            return pagesService.listPages(
                request.spaceIds(),
                request.query(),
                request.statuses(),
                request.sort(),
                request.limit(),
                request.cursor()
            ).block(); // Block for synchronous tool execution
            
        } catch (Exception e) {
            logger.error("Error in list_pages tool", e);
            return "Error listing pages: " + e.getMessage();
        }
    }
    
    /**
     * Get detailed information about a specific Confluence page
     */
    @AITool(name = "get-page", description = "Get detailed information about a specific Confluence page")
    public String getPage(GetPageRequest request) {
        logger.debug("get_page tool called with: {}", request);
        
        try {
            return pagesService.getPage(request.pageId()).block(); // Block for synchronous tool execution
            
        } catch (Exception e) {
            logger.error("Error in get_page tool", e);
            return "Error getting page: " + e.getMessage();
        }
    }
    
    /**
     * Create a new page in Confluence
     */
    @AITool(name = "create-page", description = "Create a new page in Confluence")
    public String createPage(CreatePageToolRequest request) {
        logger.debug("create_page tool called with: {}", request);
        
        try {
            CreatePageRequest createRequest = CreatePageRequest.builder()
                .title(request.title())
                .spaceKey(request.spaceKey())
                .parentId(request.parentId())
                .content(request.content())
                .contentRepresentation(request.contentRepresentation())
                .status(request.status())
                .build();
                
            return pagesService.createPage(createRequest).block(); // Block for synchronous tool execution
            
        } catch (Exception e) {
            logger.error("Error in create_page tool", e);
            return "Error creating page: " + e.getMessage();
        }
    }
    
    /**
     * Update an existing page in Confluence
     */
    @AITool(name = "update-page", description = "Update an existing page in Confluence")
    public String updatePage(UpdatePageToolRequest request) {
        logger.debug("update_page tool called with: {}", request);
        
        try {
            UpdatePageRequest updateRequest = UpdatePageRequest.builder()
                .pageId(request.pageId())
                .title(request.title())
                .content(request.content())
                .contentRepresentation(request.contentRepresentation())
                .status(request.status())
                .version(request.version())
                .parentId(request.parentId())
                .build();
                
            return pagesService.updatePage(updateRequest).block(); // Block for synchronous tool execution
            
        } catch (Exception e) {
            logger.error("Error in update_page tool", e);
            return "Error updating page: " + e.getMessage();
        }
    }
    
    /**
     * Request object for list_pages tool
     */
    public record ListPagesRequest(
        List<String> spaceIds,
        String query,
        List<ContentStatus> statuses,
        PageSortOrder sort,
        Integer limit,
        String cursor
    ) {}
    
    /**
     * Request object for get_page tool
     */
    public record GetPageRequest(
        String pageId
    ) {}
    
    /**
     * Request object for create_page tool
     */
    public record CreatePageToolRequest(
        String title,
        String spaceKey,
        String parentId,
        String content,
        String contentRepresentation,
        ContentStatus status
    ) {}
    
    /**
     * Request object for update_page tool
     */
    public record UpdatePageToolRequest(
        String pageId,
        String title,
        String content,
        String contentRepresentation,
        ContentStatus status,
        Integer version,
        String parentId
    ) {}
}