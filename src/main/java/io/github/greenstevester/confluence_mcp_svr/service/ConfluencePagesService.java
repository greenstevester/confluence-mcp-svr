package io.github.greenstevester.confluence_mcp_svr.service;

import io.github.greenstevester.confluence_mcp_svr.client.ConfluencePagesClient;
import io.github.greenstevester.confluence_mcp_svr.config.ConfluenceProperties;
import io.github.greenstevester.confluence_mcp_svr.model.common.PaginatedResponse;
import io.github.greenstevester.confluence_mcp_svr.model.dto.GetPageRequest;
import io.github.greenstevester.confluence_mcp_svr.model.dto.ListPagesRequest;
import io.github.greenstevester.confluence_mcp_svr.model.enums.BodyFormat;
import io.github.greenstevester.confluence_mcp_svr.model.enums.ContentStatus;
import io.github.greenstevester.confluence_mcp_svr.model.enums.PageSortOrder;
import io.github.greenstevester.confluence_mcp_svr.model.page.Page;
import io.github.greenstevester.confluence_mcp_svr.model.page.PageDetailed;
import io.github.greenstevester.confluence_mcp_svr.util.HtmlToMarkdownConverter;
import io.github.greenstevester.confluence_mcp_svr.util.MarkdownFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for managing Confluence pages operations
 */
@Service
public class ConfluencePagesService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluencePagesService.class);
    
    private final ConfluencePagesClient pagesClient;
    private final ConfluenceProperties confluenceProperties;
    private final MarkdownFormatter markdownFormatter;
    private final HtmlToMarkdownConverter htmlToMarkdownConverter;
    
    public ConfluencePagesService(
            ConfluencePagesClient pagesClient,
            ConfluenceProperties confluenceProperties,
            MarkdownFormatter markdownFormatter,
            HtmlToMarkdownConverter htmlToMarkdownConverter) {
        this.pagesClient = pagesClient;
        this.confluenceProperties = confluenceProperties;
        this.markdownFormatter = markdownFormatter;
        this.htmlToMarkdownConverter = htmlToMarkdownConverter;
    }
    
    /**
     * List pages with optional filtering
     */
    public Mono<String> listPages(
            List<String> spaceIds,
            String query,
            List<ContentStatus> statuses,
            PageSortOrder sort,
            Integer limit,
            String cursor) {
        
        logger.debug("Listing pages with spaceIds: {}, query: {}", spaceIds, query);
        
        // Build request with defaults
        ListPagesRequest request = new ListPagesRequest(
            null, // id
            spaceIds,
            null, // parentId
            sort != null ? sort : PageSortOrder.MODIFIED_DATE_DESC,
            statuses != null ? statuses : List.of(ContentStatus.CURRENT),
            null, // title
            query,
            BodyFormat.STORAGE,
            cursor,
            limit != null ? limit : confluenceProperties.defaults().pageSize()
        );
        
        return pagesClient.listPages(request)
            .map(this::formatPagesList)
            .doOnSuccess(result -> logger.debug("Formatted pages list response"))
            .doOnError(error -> logger.error("Error listing pages", error));
    }
    
    /**
     * Get detailed information about a specific page
     */
    public Mono<String> getPage(String pageId) {
        logger.debug("Getting page details for ID: {}", pageId);
        
        GetPageRequest request = new GetPageRequest(
            pageId,
            BodyFormat.STORAGE,
            false, // getDraft
            List.of(ContentStatus.CURRENT),
            null, // version
            confluenceProperties.defaults().includeLabels(),
            confluenceProperties.defaults().includeProperties(),
            false, // includeOperations
            false, // includeLikes
            false, // includeVersions
            confluenceProperties.defaults().includeVersion(),
            false, // includeFavoritedByCurrentUserStatus
            confluenceProperties.defaults().includeWebresources(),
            confluenceProperties.defaults().includeCollaborators()
        );
        
        return pagesClient.getPage(pageId, request)
            .map(this::formatPageDetails)
            .doOnSuccess(result -> logger.debug("Formatted page details response"))
            .doOnError(error -> logger.error("Error getting page {}", pageId, error));
    }
    
    /**
     * Format a list of pages for display
     */
    private String formatPagesList(PaginatedResponse<Page> pagesResponse) {
        List<Page> pages = pagesResponse.results();
        
        if (pages == null || pages.isEmpty()) {
            return "No Confluence pages found matching your criteria.";
        }
        
        StringBuilder result = new StringBuilder();
        result.append(markdownFormatter.formatHeading("Confluence Pages", 1))
              .append("\n\n");
        
        for (int i = 0; i < pages.size(); i++) {
            Page page = pages.get(i);
            result.append(formatPageListItem(page, i + 1));
            
            if (i < pages.size() - 1) {
                result.append("\n\n").append(markdownFormatter.formatSeparator());
            }
            result.append("\n\n");
        }
        
        // Add pagination info if available
        if (pagesResponse.links() != null && pagesResponse.links().next() != null) {
            result.append(markdownFormatter.formatItalic("More pages available. Use cursor for pagination."))
                  .append("\n\n");
        }
        
        result.append(markdownFormatter.formatItalic(
            "Page information retrieved at " + markdownFormatter.formatDate(LocalDateTime.now())));
        
        return result.toString();
    }
    
    /**
     * Format a single page item for list display
     */
    private String formatPageListItem(Page page, int index) {
        StringBuilder result = new StringBuilder();
        
        result.append(markdownFormatter.formatHeading(page.title(), 2))
              .append("\n\n");
        
        String baseUrl = confluenceProperties.api().baseUrl();
        String pageUrl = baseUrl + "/pages/viewpage.action?pageId=" + page.id();
        
        Map<String, Object> properties = Map.of(
            "ID", page.id(),
            "Status", page.status().getValue(),
            "Space ID", page.spaceId() != null ? page.spaceId() : "N/A",
            "Title", page.title(),
            "Created", page.createdAt() != null ? markdownFormatter.formatDate(page.createdAt()) : "Not available",
            "Author", page.authorId() != null ? page.authorId() : "Unknown",
            "Version", page.version() != null ? page.version().number() : "N/A",
            "URL", markdownFormatter.formatUrl(pageUrl, page.title())
        );
        
        result.append(markdownFormatter.formatBulletList(properties, key -> key));
        
        return result.toString();
    }
    
    /**
     * Format detailed page information for display
     */
    private String formatPageDetails(PageDetailed pageData) {
        StringBuilder result = new StringBuilder();
        
        // Page title and overview
        result.append(markdownFormatter.formatHeading("Confluence Page: " + pageData.title(), 1))
              .append("\n\n");
        
        String baseUrl = confluenceProperties.api().baseUrl();
        String overview = String.format("A page in space `%s`", pageData.spaceId());
        result.append(markdownFormatter.formatBlockquote(overview))
              .append("\n\n");
        
        // Basic Information
        result.append(markdownFormatter.formatHeading("Basic Information", 2))
              .append("\n\n");
        
        Map<String, Object> basicInfo = Map.of(
            "ID", pageData.id(),
            "Title", pageData.title(),
            "Space ID", pageData.spaceId() != null ? pageData.spaceId() : "N/A",
            "Parent ID", pageData.parentId() != null ? pageData.parentId() : "None"
        );
        
        result.append(markdownFormatter.formatBulletList(basicInfo, key -> key))
              .append("\n\n");
        
        // Page Content
        if (pageData.body() != null && pageData.body().storage() != null) {
            result.append(markdownFormatter.formatHeading("Page Content", 2))
                  .append("\n\n");
            
            String htmlContent = pageData.body().storage().value();
            String markdownContent = htmlToMarkdownConverter.convert(htmlContent);
            
            result.append(markdownContent)
                  .append("\n\n");
        }
        
        // Labels
        if (pageData.labels() != null && !pageData.labels().isEmpty()) {
            result.append(markdownFormatter.formatHeading("Labels", 2))
                  .append("\n\n");
            
            String labelList = pageData.labels().stream()
                .map(label -> markdownFormatter.formatInlineCode(label.name()))
                .reduce("", (acc, label) -> acc.isEmpty() ? label : acc + ", " + label);
            
            result.append(labelList).append("\n\n");
        }
        
        // Timestamp
        result.append(markdownFormatter.formatItalic(
            "Page information retrieved at " + markdownFormatter.formatDate(LocalDateTime.now())));
        
        return result.toString();
    }
}