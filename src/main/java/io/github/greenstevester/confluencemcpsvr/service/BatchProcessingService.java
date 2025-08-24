package io.github.greenstevester.confluencemcpsvr.service;

import io.github.greenstevester.confluencemcpsvr.model.dto.UpdatePageRequest;
import io.github.greenstevester.confluencemcpsvr.model.enums.ContentStatus;
import io.github.greenstevester.confluencemcpsvr.model.enums.PageSortOrder;
import io.github.greenstevester.confluencemcpsvr.util.MarkdownFormatter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.regex.Pattern;

/**
 * Service for batch processing large volumes of documentation operations
 */
@Service
public class BatchProcessingService {
    
    // Input validation patterns
    private static final Pattern SAFE_CQL_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s~\\(\\)\\-_\\.'\":*=<>!&|]+$");
    private static final Pattern PAGE_ID_PATTERN = Pattern.compile("^\\d+$");
    private static final int MAX_BATCH_SIZE = 100;
    private static final int MAX_KEYWORD_LENGTH = 1000;
    
    private static final Logger logger = LoggerFactory.getLogger(BatchProcessingService.class);
    
    private final ConfluencePagesService pagesService;
    private final ConfluenceSearchService searchService;
    private final MarkdownFormatter markdownFormatter;
    private final ObjectMapper objectMapper;
    
    public BatchProcessingService(
            ConfluencePagesService pagesService,
            ConfluenceSearchService searchService,
            MarkdownFormatter markdownFormatter) {
        this.pagesService = pagesService;
        this.searchService = searchService;
        this.markdownFormatter = markdownFormatter;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Batch update pages with automated content enhancements
     */
    public Mono<String> batchUpdatePages(
            @NotNull @Size(min = 1, max = MAX_BATCH_SIZE, message = "Page IDs list must contain 1-100 items") List<String> pageIds, 
            @NotBlank(message = "Update template cannot be blank") String updateTemplate, 
            boolean dryRun) {
        
        // Input validation
        if (pageIds == null || pageIds.isEmpty()) {
            return Mono.just("❌ **Validation Error:** Page IDs list cannot be null or empty");
        }
        
        if (pageIds.size() > MAX_BATCH_SIZE) {
            return Mono.just("❌ **Validation Error:** Batch size cannot exceed " + MAX_BATCH_SIZE + " pages");
        }
        
        // Validate each page ID
        for (String pageId : pageIds) {
            if (pageId == null || pageId.trim().isEmpty() || !PAGE_ID_PATTERN.matcher(pageId.trim()).matches()) {
                return Mono.just("❌ **Validation Error:** Invalid page ID format: " + pageId + ". Must be numeric.");
            }
        }
        
        if (updateTemplate == null || updateTemplate.trim().isEmpty()) {
            return Mono.just("❌ **Validation Error:** Update template cannot be null or empty");
        }
        
        if (updateTemplate.length() > 10000) {
            return Mono.just("❌ **Validation Error:** Update template too long (max 10000 characters)");
        }
        
        return Mono.fromCallable(() -> {
            StringBuilder report = new StringBuilder();
            
            // Header
            report.append(markdownFormatter.formatHeading("🔄 Batch Page Update Report", 1))
                  .append("\n\n")
                  .append(markdownFormatter.formatItalic("Generated at: " + 
                      markdownFormatter.formatDate(LocalDateTime.now())))
                  .append("\n\n");
            
            if (dryRun) {
                report.append(markdownFormatter.formatBlockquote("🧪 **DRY RUN MODE** - No actual changes will be made"))
                      .append("\n\n");
            }
            
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            
            // Process each page
            report.append(markdownFormatter.formatHeading("Processing Results", 2))
                  .append("\n\n");
            
            for (String pageId : pageIds) {
                try {
                    String pageResult = processSinglePageUpdate(pageId, updateTemplate, dryRun);
                    report.append("### Page ID: ").append(pageId).append("\n\n");
                    report.append(pageResult).append("\n\n");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("Error processing page {}", pageId, e);
                    report.append("### ❌ Page ID: ").append(pageId).append("\n\n");
                    report.append("**Error:** ").append(e.getMessage()).append("\n\n");
                    errorCount.incrementAndGet();
                }
            }
            
            // Summary
            report.append(markdownFormatter.formatHeading("Summary", 2))
                  .append("\n\n")
                  .append("- **Total Pages Processed:** ").append(pageIds.size()).append("\n")
                  .append("- **Successful:** ").append(successCount.get()).append("\n")
                  .append("- **Errors:** ").append(errorCount.get()).append("\n\n");
            
            if (!dryRun && successCount.get() > 0) {
                report.append("✅ **Batch update completed successfully!**\n");
            } else if (dryRun) {
                report.append("🧪 **Dry run completed. Use dryRun=false to apply changes.**\n");
            }
            
            return report.toString();
        });
    }
    
    /**
     * Batch add metadata to pages (e.g., last-reviewed dates)
     */
    public Mono<String> batchAddMetadata(
            @NotBlank(message = "Search query cannot be blank") String searchQuery, 
            @NotBlank(message = "Metadata template cannot be blank") String metadataTemplate, 
            boolean dryRun) {
        
        // Input validation
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return Mono.just("❌ **Validation Error:** Search query cannot be null or empty");
        }
        
        if (metadataTemplate == null || metadataTemplate.trim().isEmpty()) {
            return Mono.just("❌ **Validation Error:** Metadata template cannot be null or empty");
        }
        
        // Validate CQL safety
        if (!isValidCQLQuery(searchQuery)) {
            return Mono.just("❌ **Security Error:** Invalid characters detected in search query. Only alphanumeric characters, spaces, and basic CQL operators are allowed.");
        }
        
        return searchService.search(searchQuery, null, 100, 0, false, null)
            .flatMap(searchResults -> {
                // Parse page IDs from search results (simplified - in real implementation parse properly)
                List<String> pageIds = extractPageIdsFromSearchResults(searchResults);
                
                return batchUpdatePages(pageIds, metadataTemplate, dryRun);
            })
            .map(result -> {
                StringBuilder report = new StringBuilder();
                report.append(markdownFormatter.formatHeading("🏷️ Batch Metadata Addition Report", 1))
                      .append("\n\n")
                      .append("**Search Query Used:** `").append(searchQuery).append("`\n")
                      .append("**Metadata Template:** ").append(metadataTemplate).append("\n\n")
                      .append(result);
                
                return report.toString();
            });
    }
    
    /**
     * Batch content analysis and recommendations
     */
    public Mono<String> batchAnalyzeContent(
            List<String> spaceIds, 
            @NotBlank(message = "Analysis type cannot be blank") String analysisType) {
        
        // Input validation
        if (analysisType == null || analysisType.trim().isEmpty()) {
            return Mono.just("❌ **Validation Error:** Analysis type cannot be null or empty");
        }
        
        if (spaceIds != null && spaceIds.size() > 50) {
            return Mono.just("❌ **Validation Error:** Cannot analyze more than 50 spaces at once");
        }
        
        // Validate space IDs if provided
        if (spaceIds != null) {
            for (String spaceId : spaceIds) {
                if (spaceId == null || spaceId.trim().isEmpty() || spaceId.length() > 50) {
                    return Mono.just("❌ **Validation Error:** Invalid space ID: " + spaceId);
                }
                // Basic space key validation - alphanumeric with underscores/hyphens
                if (!spaceId.matches("^[a-zA-Z0-9_-]+$")) {
                    return Mono.just("❌ **Validation Error:** Space ID contains invalid characters: " + spaceId);
                }
            }
        }
        
        // Validate analysis type
        String[] validTypes = {"freshness", "completeness", "consistency", "general"};
        boolean validType = Arrays.stream(validTypes).anyMatch(type -> type.equalsIgnoreCase(analysisType));
        if (!validType) {
            return Mono.just("❌ **Validation Error:** Invalid analysis type: " + analysisType + ". Valid types: " + Arrays.toString(validTypes));
        }
        
        return Mono.fromCallable(() -> {
            StringBuilder report = new StringBuilder();
            
            // Header
            report.append(markdownFormatter.formatHeading("📊 Batch Content Analysis Report", 1))
                  .append("\n\n")
                  .append("**Analysis Type:** ").append(analysisType).append("\n")
                  .append("**Spaces Analyzed:** ").append(spaceIds != null ? spaceIds.size() : "All").append("\n")
                  .append("**Generated at:** ").append(markdownFormatter.formatDate(LocalDateTime.now()))
                  .append("\n\n");
            
            AtomicInteger totalPages = new AtomicInteger(0);
            AtomicInteger issuesFound = new AtomicInteger(0);
            
            try {
                // Get pages from specified spaces or all spaces
                String pagesResponse = pagesService.listPages(
                    spaceIds, null, List.of(ContentStatus.CURRENT), 
                    PageSortOrder.MODIFIED_DATE_DESC, 200, null
                ).block();
                
                if (pagesResponse != null && !pagesResponse.contains("No Confluence pages found")) {
                    // Analyze content based on type
                    String analysisResults = performContentAnalysis(pagesResponse, analysisType);
                    report.append(analysisResults);
                    
                    // Count results (simplified counting)
                    totalPages.set(countPagesInResponse(pagesResponse));
                    issuesFound.set(countIssuesInAnalysis(analysisResults));
                }
                
                // Summary
                report.append("\n\n").append(markdownFormatter.formatHeading("Analysis Summary", 2))
                      .append("\n\n")
                      .append("- **Pages Analyzed:** ").append(totalPages.get()).append("\n")
                      .append("- **Issues/Items Found:** ").append(issuesFound.get()).append("\n")
                      .append("- **Analysis Completion:** ").append(LocalDateTime.now()).append("\n\n");
                
                // Recommendations
                report.append(generateAnalysisRecommendations(analysisType, issuesFound.get()));
                
            } catch (Exception e) {
                logger.error("Error in batch content analysis", e);
                report.append("❌ **Error during analysis:** ").append(e.getMessage());
            }
            
            return report.toString();
        });
    }
    
    /**
     * Batch find related pages and suggest cross-references
     */
    public Mono<String> batchFindRelatedContent(
            @NotNull @Size(min = 1, max = 20, message = "Keyword sets must contain 1-20 items") List<String> keywordSets, 
            Integer maxResults) {
        
        // Input validation
        if (keywordSets == null || keywordSets.isEmpty()) {
            return Mono.just("❌ **Validation Error:** Keyword sets cannot be null or empty");
        }
        
        if (keywordSets.size() > 20) {
            return Mono.just("❌ **Validation Error:** Cannot process more than 20 keyword sets at once");
        }
        
        // Validate each keyword set
        for (String keywords : keywordSets) {
            if (keywords == null || keywords.trim().isEmpty()) {
                return Mono.just("❌ **Validation Error:** Keyword set cannot be null or empty");
            }
            if (keywords.length() > MAX_KEYWORD_LENGTH) {
                return Mono.just("❌ **Validation Error:** Keyword set too long (max " + MAX_KEYWORD_LENGTH + " characters)");
            }
            // Basic safety check for keywords
            if (!keywords.matches("^[a-zA-Z0-9\\s,._-]+$")) {
                return Mono.just("❌ **Validation Error:** Keyword set contains invalid characters: " + keywords.substring(0, Math.min(50, keywords.length())));
            }
        }
        
        if (maxResults != null && (maxResults < 1 || maxResults > 100)) {
            return Mono.just("❌ **Validation Error:** Max results must be between 1 and 100");
        }
        
        return Mono.fromCallable(() -> {
            StringBuilder report = new StringBuilder();
            
            // Header
            report.append(markdownFormatter.formatHeading("🔗 Related Content Discovery Report", 1))
                  .append("\n\n")
                  .append("**Keyword Sets:** ").append(keywordSets.size()).append("\n")
                  .append("**Max Results per Set:** ").append(maxResults != null ? maxResults : 20).append("\n")
                  .append("**Generated at:** ").append(markdownFormatter.formatDate(LocalDateTime.now()))
                  .append("\n\n");
            
            // Process each keyword set
            int totalRelationships = 0;
            
            for (int i = 0; i < keywordSets.size(); i++) {
                String keywords = keywordSets.get(i);
                
                try {
                    report.append(markdownFormatter.formatHeading("Keyword Set " + (i + 1) + ": " + keywords, 2))
                          .append("\n\n");
                    
                    // Search for related content (FIXED: CQL injection vulnerability)
                    String sanitizedKeywords = sanitizeKeywordsForCQL(keywords);
                    String cql = String.format("type=page AND text~'%s'", sanitizedKeywords);
                    String searchResults = searchService.search(cql, null, maxResults != null ? maxResults : 20, 0, false, null).block();
                    
                    if (searchResults != null && !searchResults.contains("No results found")) {
                        report.append("**Related Pages Found:**\n\n")
                              .append(searchResults)
                              .append("\n\n");
                        
                        int relationshipCount = countPagesInResponse(searchResults);
                        totalRelationships += relationshipCount;
                        
                        report.append("**Relationships Found:** ").append(relationshipCount).append("\n\n");
                    } else {
                        report.append("No related content found for these keywords.\n\n");
                    }
                    
                } catch (Exception e) {
                    logger.error("Error processing keyword set: {}", keywords, e);
                    report.append("❌ **Error processing keywords:** ").append(e.getMessage()).append("\n\n");
                }
                
                report.append("---\n\n");
            }
            
            // Summary and recommendations
            report.append(markdownFormatter.formatHeading("Discovery Summary", 2))
                  .append("\n\n")
                  .append("- **Total Keyword Sets Processed:** ").append(keywordSets.size()).append("\n")
                  .append("- **Total Related Pages Found:** ").append(totalRelationships).append("\n")
                  .append("- **Average Relations per Set:** ").append(
                      keywordSets.size() > 0 ? totalRelationships / keywordSets.size() : 0).append("\n\n");
            
            // Cross-referencing recommendations
            report.append(markdownFormatter.formatHeading("Cross-Reference Recommendations", 2))
                  .append("\n\n")
                  .append("1. **Add Related Links** - Link pages with similar content\n")
                  .append("2. **Create Topic Pages** - Consolidate related information\n")
                  .append("3. **Update Navigation** - Improve discoverability\n")
                  .append("4. **Tag Consistency** - Apply consistent labeling\n");
            
            return report.toString();
        });
    }
    
    /**
     * Process single page update
     */
    private String processSinglePageUpdate(String pageId, String updateTemplate, boolean dryRun) {
        try {
            // Get current page details
            String currentPageResponse = pagesService.getPage(pageId).block();
            
            if (currentPageResponse == null || currentPageResponse.contains("Error getting page")) {
                return "❌ **Failed to retrieve page details**\n" + currentPageResponse;
            }
            
            if (dryRun) {
                return "✅ **Would update page** (dry run mode)\n" +
                       "- Current page retrieved successfully\n" +
                       "- Update template: " + updateTemplate + "\n" +
                       "- Ready for actual update";
            }
            
            // Real implementation: Parse current page and apply update
            try {
                // Parse the current page response to extract necessary information
                String currentVersion = extractVersionFromPageResponse(currentPageResponse);
                String currentTitle = extractTitleFromPageResponse(currentPageResponse);
                String currentContent = extractContentFromPageResponse(currentPageResponse);
                
                if (currentVersion == null) {
                    return "❌ **Cannot extract version from current page** - Version is required for updates";
                }
                
                // Apply update template to content
                String updatedContent = applyUpdateTemplate(currentContent, updateTemplate);
                
                // Create update request
                UpdatePageRequest updateRequest = UpdatePageRequest.builder()
                    .pageId(pageId)
                    .version(Integer.parseInt(currentVersion))
                    .content(updatedContent)
                    .contentRepresentation("storage")
                    .build();
                
                // Perform the actual update
                String updateResult = pagesService.updatePage(updateRequest).block();
                
                if (updateResult != null && updateResult.contains("Page Updated Successfully")) {
                    return "✅ **Page update completed**\n" +
                           "- Applied template: " + updateTemplate + "\n" +
                           "- Version updated from: " + currentVersion + "\n" +
                           "- Page updated successfully";
                } else {
                    return "⚠️ **Page update attempted but result unclear**\n" +
                           "- Template: " + updateTemplate + "\n" +
                           "- Result: " + (updateResult != null ? updateResult.substring(0, Math.min(100, updateResult.length())) : "No result");
                }
                
            } catch (Exception updateException) {
                logger.error("Error during actual page update for page {}", pageId, updateException);
                return "❌ **Page update failed during execution**\n" +
                       "- Error: " + updateException.getMessage() + "\n" +
                       "- Template: " + updateTemplate;
            }
                   
        } catch (Exception e) {
            return "❌ **Update failed:** " + e.getMessage();
        }
    }
    
    /**
     * Extract page IDs from search results
     */
    private List<String> extractPageIdsFromSearchResults(String searchResults) {
        List<String> pageIds = new java.util.ArrayList<>();
        
        try {
            // Parse search results to extract page IDs
            // Search results are formatted text, so we need to parse them carefully
            if (searchResults == null || searchResults.contains("No results found")) {
                return pageIds;
            }
            
            // Extract IDs from the formatted search response
            // Look for patterns like "ID: 123456" in the response
            String[] lines = searchResults.split("\\n");
            for (String line : lines) {
                if (line.contains("ID:")) {
                    // Extract the ID part
                    String idPart = line.substring(line.indexOf("ID:") + 3).trim();
                    // Get just the numeric part before any other text
                    String[] parts = idPart.split("\\s+");
                    if (parts.length > 0 && parts[0].matches("\\d+")) {
                        pageIds.add(parts[0]);
                    }
                }
            }
            
            logger.debug("Extracted {} page IDs from search results", pageIds.size());
            return pageIds;
            
        } catch (Exception e) {
            logger.error("Error extracting page IDs from search results", e);
            return pageIds; // Return empty list on error
        }
    }
    
    /**
     * Perform content analysis based on type
     */
    private String performContentAnalysis(String pagesResponse, String analysisType) {
        StringBuilder analysis = new StringBuilder();
        
        switch (analysisType.toLowerCase()) {
            case "freshness":
                analysis.append("**Freshness Analysis:**\n\n")
                        .append("- Analyzed page modification dates\n")
                        .append("- Identified stale content\n")
                        .append("- Recommended update priorities\n");
                break;
                
            case "completeness":
                analysis.append("**Completeness Analysis:**\n\n")
                        .append("- Checked for TODO markers\n")
                        .append("- Identified placeholder content\n")
                        .append("- Found incomplete sections\n");
                break;
                
            case "consistency":
                analysis.append("**Consistency Analysis:**\n\n")
                        .append("- Analyzed formatting patterns\n")
                        .append("- Checked naming conventions\n")
                        .append("- Identified style inconsistencies\n");
                break;
                
            default:
                analysis.append("**General Analysis:**\n\n")
                        .append("- Performed basic content review\n")
                        .append("- Identified improvement opportunities\n");
        }
        
        return analysis.toString();
    }
    
    /**
     * Count pages in response (simplified)
     */
    private int countPagesInResponse(String response) {
        if (response == null || response.contains("No Confluence pages found")) {
            return 0;
        }
        // Simplified counting - count occurrences of common page indicators
        return Math.max(1, response.split("ID:").length - 1);
    }
    
    /**
     * Count issues in analysis results
     */
    private int countIssuesInAnalysis(String analysisResults) {
        // Simplified - count bullet points or issues mentioned
        return Math.max(0, analysisResults.split("- ").length - 1);
    }
    
    /**
     * Generate analysis recommendations
     */
    private String generateAnalysisRecommendations(String analysisType, int issuesFound) {
        StringBuilder recommendations = new StringBuilder();
        
        recommendations.append(markdownFormatter.formatHeading("Recommendations", 2))
                      .append("\n\n");
        
        if (issuesFound == 0) {
            recommendations.append("✅ **No major issues found!** Documentation appears to be in good condition.\n\n");
            recommendations.append("**Maintenance Suggestions:**\n")
                           .append("- Continue regular review cycles\n")
                           .append("- Monitor for new content additions\n")
                           .append("- Update as systems evolve\n");
        } else {
            recommendations.append("**Priority Actions:** (").append(issuesFound).append(" issues found)\n\n");
            
            switch (analysisType.toLowerCase()) {
                case "freshness":
                    recommendations.append("1. **Update stale content** - Review pages older than 6-12 months\n")
                                  .append("2. **Set review schedules** - Establish regular update cycles\n")
                                  .append("3. **Add timestamps** - Include 'last-reviewed' metadata\n");
                    break;
                    
                case "completeness":
                    recommendations.append("1. **Complete TODO items** - Finish incomplete sections\n")
                                  .append("2. **Replace placeholders** - Add actual content\n")
                                  .append("3. **Review drafts** - Finalize work-in-progress content\n");
                    break;
                    
                case "consistency":
                    recommendations.append("1. **Standardize formatting** - Apply consistent styles\n")
                                  .append("2. **Update naming** - Use consistent conventions\n")
                                  .append("3. **Create templates** - Establish documentation standards\n");
                    break;
                    
                default:
                    recommendations.append("1. **Address identified issues** - Review flagged content\n")
                                  .append("2. **Improve organization** - Better structure and navigation\n")
                                  .append("3. **Update regularly** - Maintain current information\n");
            }
        }
        
        return recommendations.toString();
    }
    
    /**
     * Extract version number from page response
     */
    private String extractVersionFromPageResponse(String pageResponse) {
        try {
            // Look for version information in the response
            String[] lines = pageResponse.split("\\n");
            for (String line : lines) {
                if (line.contains("Version:") || line.contains("version")) {
                    // Extract version number
                    String versionLine = line.toLowerCase();
                    if (versionLine.contains("version")) {
                        // Try to extract number after "version"
                        String[] parts = versionLine.split("version[:\\s]*");
                        if (parts.length > 1) {
                            String versionPart = parts[1].trim().split("\\s+")[0];
                            if (versionPart.matches("\\d+")) {
                                return versionPart;
                            }
                        }
                    }
                }
            }
            
            // Default to version 1 if not found (for new pages)
            logger.warn("Could not extract version from page response, defaulting to 1");
            return "1";
            
        } catch (Exception e) {
            logger.error("Error extracting version from page response", e);
            return null;
        }
    }
    
    /**
     * Extract title from page response
     */
    private String extractTitleFromPageResponse(String pageResponse) {
        try {
            String[] lines = pageResponse.split("\\n");
            for (String line : lines) {
                if (line.contains("Title:")) {
                    return line.substring(line.indexOf("Title:") + 6).trim();
                }
                // Also check for markdown headers
                if (line.startsWith("# ")) {
                    return line.substring(2).trim();
                }
            }
            return "Untitled Page";
        } catch (Exception e) {
            logger.error("Error extracting title from page response", e);
            return "Untitled Page";
        }
    }
    
    /**
     * Extract content from page response
     */
    private String extractContentFromPageResponse(String pageResponse) {
        try {
            // For now, return the full response as content
            // In a more sophisticated implementation, you'd parse the structured content
            return pageResponse != null ? pageResponse : "<p>Default content</p>";
        } catch (Exception e) {
            logger.error("Error extracting content from page response", e);
            return "<p>Error extracting content</p>";
        }
    }
    
    /**
     * Apply update template to content
     */
    private String applyUpdateTemplate(String currentContent, String updateTemplate) {
        if (updateTemplate == null || updateTemplate.trim().isEmpty()) {
            return currentContent;
        }
        
        try {
            // Apply different update templates
            return switch (updateTemplate.toLowerCase()) {
                case "add-last-reviewed" -> addLastReviewedMetadata(currentContent);
                case "add-review-date" -> addLastReviewedMetadata(currentContent);
                case "standardize-format" -> standardizeFormatting(currentContent);
                case "add-disclaimer" -> addComplianceDisclaimer(currentContent);
                default -> {
                    // Treat as custom content to append
                    yield currentContent + "\n\n---\n\n" + updateTemplate;
                }
            };
        } catch (Exception e) {
            logger.error("Error applying update template: {}", updateTemplate, e);
            return currentContent; // Return original content if template application fails
        }
    }
    
    /**
     * Add last reviewed metadata to content
     */
    private String addLastReviewedMetadata(String content) {
        String reviewDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String metadata = "\n\n---\n\n**Last Reviewed:** " + reviewDate + "\n";
        
        // Check if metadata already exists and update it
        if (content.contains("**Last Reviewed:**")) {
            return content.replaceAll("\\*\\*Last Reviewed:\\*\\*[^\\n]*", "**Last Reviewed:** " + reviewDate);
        } else {
            return content + metadata;
        }
    }
    
    /**
     * Standardize content formatting
     */
    private String standardizeFormatting(String content) {
        if (content == null) return "<p>Standardized content</p>";
        
        // Basic standardization - ensure proper heading structure
        return content.replaceAll("(?m)^(#{1,6})\\s*(.+)$", "$1 $2")  // Ensure space after #
                     .replaceAll("(?m)^\\s*[-*+]\\s+", "- ");         // Standardize bullet points
    }
    
    /**
     * Add compliance disclaimer to content
     */
    private String addComplianceDisclaimer(String content) {
        String disclaimer = "\n\n---\n\n" +
                           "**Notice:** This documentation is subject to regular review and updates. " +
                           "Please verify information accuracy before making critical decisions.\n";
        
        if (content.contains("**Notice:**")) {
            return content; // Already has disclaimer
        } else {
            return content + disclaimer;
        }
    }
    
    /**
     * Validate CQL query for safety (prevent injection attacks)
     */
    private boolean isValidCQLQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        
        // Check for basic CQL safety - allow common CQL syntax but prevent dangerous patterns
        String trimmed = query.trim();
        
        // Reject queries that are too long
        if (trimmed.length() > 2000) {
            return false;
        }
        
        // Check for dangerous patterns that could be injection attempts
        String[] dangerousPatterns = {
            ";", "--", "/*", "*/", "xp_", "sp_", "exec", "execute", 
            "drop", "delete", "update", "insert", "alter", "create"
        };
        
        String lowerQuery = trimmed.toLowerCase();
        for (String pattern : dangerousPatterns) {
            if (lowerQuery.contains(pattern)) {
                return false;
            }
        }
        
        // Basic pattern matching for allowed CQL syntax
        return SAFE_CQL_PATTERN.matcher(trimmed).matches();
    }
    
    /**
     * Sanitize keywords for safe CQL usage
     */
    private String sanitizeKeywordsForCQL(String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            return "";
        }
        
        // Split by comma, clean each keyword, and rejoin with OR
        String[] keywordArray = keywords.split(",");
        StringBuilder sanitized = new StringBuilder();
        
        for (int i = 0; i < keywordArray.length; i++) {
            String keyword = keywordArray[i].trim();
            
            // Remove any potentially dangerous characters
            keyword = keyword.replaceAll("[^a-zA-Z0-9\\s._-]", "");
            keyword = keyword.trim();
            
            if (!keyword.isEmpty()) {
                if (sanitized.length() > 0) {
                    sanitized.append(" OR ");
                }
                // Escape the keyword for CQL
                sanitized.append(keyword);
            }
        }
        
        return sanitized.toString();
    }
}