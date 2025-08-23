package io.github.greenstevester.confluencemcpsvr.tool;

import io.github.greenstevester.confluencemcpsvr.model.enums.ContentStatus;
import io.github.greenstevester.confluencemcpsvr.model.enums.PageSortOrder;
import io.github.greenstevester.confluencemcpsvr.service.ConfluencePagesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.ai.annotation.Tool;
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
}