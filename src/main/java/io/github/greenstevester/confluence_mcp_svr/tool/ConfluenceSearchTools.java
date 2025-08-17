package io.github.greenstevester.confluence_mcp_svr.tool;

import io.github.greenstevester.confluence_mcp_svr.model.enums.ExcerptStrategy;
import io.github.greenstevester.confluence_mcp_svr.service.ConfluenceSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.ai.annotation.Tool;
import org.springframework.stereotype.Service;

/**
 * MCP Tools for Confluence Search operations using CQL
 */
@Service
public class ConfluenceSearchTools {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluenceSearchTools.class);
    
    private final ConfluenceSearchService searchService;
    
    public ConfluenceSearchTools(ConfluenceSearchService searchService) {
        this.searchService = searchService;
    }
    
    /**
     * Search Confluence content using CQL (Confluence Query Language)
     */
    public String search(SearchRequest request) {
        logger.debug("search tool called with CQL: {}", request.cql());
        
        try {
            return searchService.search(
                request.cql(),
                request.cqlContext(),
                request.limit(),
                request.start(),
                request.includeArchivedSpaces(),
                request.excerpt()
            ).block(); // Block for synchronous tool execution
            
        } catch (Exception e) {
            logger.error("Error in search tool", e);
            return "Error during search: " + e.getMessage();
        }
    }
    
    /**
     * Request object for search tool
     */
    public record SearchRequest(
        String cql,
        String cqlContext,
        Integer limit,
        Integer start,
        Boolean includeArchivedSpaces,
        ExcerptStrategy excerpt
    ) {}
}