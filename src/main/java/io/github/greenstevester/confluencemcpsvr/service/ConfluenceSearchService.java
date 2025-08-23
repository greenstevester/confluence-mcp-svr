package io.github.greenstevester.confluencemcpsvr.service;

import io.github.greenstevester.confluencemcpsvr.client.ConfluenceSearchClient;
import io.github.greenstevester.confluencemcpsvr.config.ConfluenceProperties;
import io.github.greenstevester.confluencemcpsvr.model.enums.ExcerptStrategy;
import io.github.greenstevester.confluencemcpsvr.model.search.SearchRequest;
import io.github.greenstevester.confluencemcpsvr.model.search.SearchResponse;
import io.github.greenstevester.confluencemcpsvr.model.search.SearchResult;
import io.github.greenstevester.confluencemcpsvr.util.MarkdownFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for Confluence search operations using CQL
 */
@Service
public class ConfluenceSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfluenceSearchService.class);
    
    private final ConfluenceSearchClient searchClient;
    private final ConfluenceProperties confluenceProperties;
    private final MarkdownFormatter markdownFormatter;
    
    public ConfluenceSearchService(
            ConfluenceSearchClient searchClient,
            ConfluenceProperties confluenceProperties,
            MarkdownFormatter markdownFormatter) {
        this.searchClient = searchClient;
        this.confluenceProperties = confluenceProperties;
        this.markdownFormatter = markdownFormatter;
    }
    
    /**
     * Search Confluence content using CQL
     */
    public Mono<String> search(
            String cql,
            String cqlContext,
            Integer limit,
            Integer start,
            Boolean includeArchivedSpaces,
            ExcerptStrategy excerpt) {
        
        logger.debug("Searching with CQL: {}", cql);
        
        SearchRequest request = new SearchRequest(
            cql,
            cqlContext,
            null, // cursor
            limit != null ? limit : confluenceProperties.defaults().pageSize(),
            start,
            includeArchivedSpaces,
            false, // excludeCurrentSpaces
            excerpt != null ? excerpt : ExcerptStrategy.HIGHLIGHT
        );
        
        return searchClient.search(request)
            .map(this::formatSearchResults)
            .doOnSuccess(result -> logger.debug("Formatted search results"))
            .doOnError(error -> logger.error("Error during search", error))
            .onErrorReturn("Error performing search: Please check your Confluence connection and CQL query.");
    }
    
    /**
     * Format search results for display
     */
    private String formatSearchResults(SearchResponse searchResponse) {
        List<SearchResult> results = searchResponse.results();
        
        if (results == null || results.isEmpty()) {
            return "No results found for your search query.";
        }
        
        StringBuilder result = new StringBuilder();
        
        // Header with search info
        result.append(markdownFormatter.formatHeading("Confluence Search Results", 1))
              .append("\n\n");
        
        if (searchResponse.cqlQuery() != null) {
            result.append(markdownFormatter.formatBold("Query: "))
                  .append(markdownFormatter.formatInlineCode(searchResponse.cqlQuery()))
                  .append("\n\n");
        }
        
        // Search summary
        String summary = String.format("Found %d results", searchResponse.size());
        if (searchResponse.totalSize() != null && searchResponse.totalSize() > searchResponse.size()) {
            summary += String.format(" (showing %d of %d total)", searchResponse.size(), searchResponse.totalSize());
        }
        result.append(markdownFormatter.formatBlockquote(summary))
              .append("\n\n");
        
        // Format each result
        for (int i = 0; i < results.size(); i++) {
            SearchResult searchResult = results.get(i);
            result.append(formatSearchResultItem(searchResult, i + 1));
            
            if (i < results.size() - 1) {
                result.append("\n\n").append(markdownFormatter.formatSeparator());
            }
            result.append("\n\n");
        }
        
        // Search metadata
        if (searchResponse.searchDuration() != null) {
            result.append(markdownFormatter.formatItalic(
                String.format("Search completed in %d ms", searchResponse.searchDuration())))
                  .append("\n");
        }
        
        result.append(markdownFormatter.formatItalic(
            "Results retrieved at " + markdownFormatter.formatDate(LocalDateTime.now())));
        
        return result.toString();
    }
    
    /**
     * Format a single search result item
     */
    private String formatSearchResultItem(SearchResult searchResult, int index) {
        StringBuilder result = new StringBuilder();
        
        // Title with link
        String title = searchResult.title() != null ? searchResult.title() : "Untitled";
        if (searchResult.url() != null) {
            title = markdownFormatter.formatUrl(searchResult.url(), title);
        }
        
        result.append(markdownFormatter.formatHeading(title, 3))
              .append("\n\n");
        
        // Content information
        if (searchResult.content() != null) {
            Map<String, Object> contentInfo = Map.of(
                "Type", searchResult.content().type() != null ? searchResult.content().type() : "Unknown",
                "Status", searchResult.content().status() != null ? searchResult.content().status() : "Unknown",
                "Space", searchResult.content().space() != null ? 
                    String.format("%s (%s)", searchResult.content().space().name(), searchResult.content().space().key()) : "N/A",
                "Last Modified", searchResult.lastModified() != null ? 
                    markdownFormatter.formatDate(searchResult.lastModified()) : "Unknown"
            );
            
            result.append(markdownFormatter.formatBulletList(contentInfo, key -> key))
                  .append("\n\n");
        }
        
        // Excerpt if available
        if (searchResult.excerpt() != null && !searchResult.excerpt().trim().isEmpty()) {
            result.append(markdownFormatter.formatHeading("Excerpt", 4))
                  .append("\n\n");
            result.append(markdownFormatter.formatBlockquote(searchResult.excerpt()))
                  .append("\n\n");
        }
        
        // Score if available
        if (searchResult.score() != null) {
            result.append(markdownFormatter.formatBold("Relevance Score: "))
                  .append(String.format("%.2f", searchResult.score()))
                  .append("\n");
        }
        
        return result.toString();
    }
}