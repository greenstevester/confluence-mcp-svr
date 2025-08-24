package io.github.greenstevester.confluencemcpsvr.service;

import io.github.greenstevester.confluencemcpsvr.config.ConfluenceProperties;
import io.github.greenstevester.confluencemcpsvr.model.enums.ContentStatus;
import io.github.greenstevester.confluencemcpsvr.model.enums.PageSortOrder;
import io.github.greenstevester.confluencemcpsvr.util.MarkdownFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Service for comprehensive documentation mining, analysis, and maintenance
 */
@Service
public class DocumentationMiningService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentationMiningService.class);
    
    private final ConfluencePagesService pagesService;
    private final ConfluenceSpacesService spacesService;
    private final ConfluenceSearchService searchService;
    private final ConfluenceProperties confluenceProperties;
    private final MarkdownFormatter markdownFormatter;
    
    // Common technology keywords for documentation mining
    private static final List<String> ARCHITECTURE_KEYWORDS = Arrays.asList(
        "architecture", "system design", "technical specification", "design document",
        "API specification", "service architecture", "microservices", "infrastructure"
    );
    
    private static final List<String> TECHNOLOGY_KEYWORDS = Arrays.asList(
        "Java", "Spring", "Docker", "Kubernetes", "AWS", "microservices",
        "REST API", "database", "Redis", "PostgreSQL", "MongoDB", "Kafka"
    );
    
    private static final List<String> STALENESS_INDICATORS = Arrays.asList(
        "TODO", "FIXME", "outdated", "deprecated", "legacy", "old version",
        "needs update", "under construction", "draft", "placeholder"
    );
    
    public DocumentationMiningService(
            ConfluencePagesService pagesService,
            ConfluenceSpacesService spacesService,
            ConfluenceSearchService searchService,
            ConfluenceProperties confluenceProperties,
            MarkdownFormatter markdownFormatter) {
        this.pagesService = pagesService;
        this.spacesService = spacesService;
        this.searchService = searchService;
        this.confluenceProperties = confluenceProperties;
        this.markdownFormatter = markdownFormatter;
    }
    
    /**
     * Perform comprehensive documentation audit
     */
    public Mono<String> performDocumentationAudit(Integer maxResults) {
        logger.info("Starting comprehensive documentation audit");
        
        return Mono.fromCallable(() -> {
            StringBuilder report = new StringBuilder();
            
            // Header
            report.append(markdownFormatter.formatHeading("📋 Documentation Mining & Audit Report", 1))
                  .append("\n\n")
                  .append(markdownFormatter.formatItalic("Generated at: " + 
                      markdownFormatter.formatDate(LocalDateTime.now())))
                  .append("\n\n");
            
            try {
                // 1. Space Discovery
                String spacesAnalysis = analyzeSpaces().block();
                report.append(spacesAnalysis).append("\n\n");
                
                // 2. Age Analysis
                String ageAnalysis = analyzePageFreshness(maxResults != null ? maxResults : 50).block();
                report.append(ageAnalysis).append("\n\n");
                
                // 3. Content Quality Analysis
                String qualityAnalysis = analyzeContentQuality(maxResults != null ? maxResults : 30).block();
                report.append(qualityAnalysis).append("\n\n");
                
                // 4. Technology Documentation Coverage
                String techAnalysis = analyzeTechnologyCoverage().block();
                report.append(techAnalysis).append("\n\n");
                
                // 5. Recommendations
                report.append(generateRecommendations()).append("\n\n");
                
            } catch (Exception e) {
                logger.error("Error during documentation audit", e);
                report.append("\n\n❌ **Error during audit:** ").append(e.getMessage());
            }
            
            return report.toString();
        });
    }
    
    /**
     * Analyze space organization and structure
     */
    public Mono<String> analyzeSpaces() {
        return spacesService.listSpaces(null, null, null, null, 20, null)
            .map(this::parseSpacesForAnalysis)
            .map(spaces -> {
                StringBuilder result = new StringBuilder();
                result.append(markdownFormatter.formatHeading("🏗️ Documentation Structure Analysis", 2))
                      .append("\n\n");
                
                if (spaces.isEmpty()) {
                    result.append("No spaces found or unable to access spaces.\n");
                } else {
                    result.append("**Total Spaces Found:** ").append(spaces.size()).append("\n\n");
                    result.append("**Space Organization:**\n");
                    
                    for (String space : spaces) {
                        result.append("- ").append(space).append("\n");
                    }
                }
                
                return result.toString();
            });
    }
    
    /**
     * Analyze page freshness and identify outdated content
     */
    public Mono<String> analyzePageFreshness(Integer limit) {
        return pagesService.listPages(null, null, List.of(ContentStatus.CURRENT), 
                PageSortOrder.MODIFIED_DATE, limit, null)
            .map(response -> {
                StringBuilder result = new StringBuilder();
                result.append(markdownFormatter.formatHeading("⏰ Content Freshness Analysis", 2))
                      .append("\n\n");
                
                // Parse the response to identify old pages
                if (response.contains("No Confluence pages found")) {
                    result.append("No pages found for analysis.\n");
                } else {
                    result.append("**Oldest Pages (by last modification):**\n\n");
                    result.append(response);
                    
                    // Add age assessment
                    result.append("\n\n")
                          .append(markdownFormatter.formatBlockquote(
                              "💡 **Age Assessment Criteria:**\\n" +
                              "- 🟥 Critical: >12 months old\\n" + 
                              "- 🟨 Warning: >6 months old\\n" +
                              "- 🟩 Fresh: <6 months old"))
                          .append("\n");
                }
                
                return result.toString();
            });
    }
    
    /**
     * Analyze content quality and identify improvement opportunities
     */
    public Mono<String> analyzeContentQuality(Integer limit) {
        // Build CQL query to find potentially problematic content
        String cql = buildStaleContentQuery();
        
        return searchService.search(cql, null, limit, 0, false, null)
            .map(response -> {
                StringBuilder result = new StringBuilder();
                result.append(markdownFormatter.formatHeading("🔍 Content Quality Analysis", 2))
                      .append("\n\n");
                
                if (response.contains("No results found")) {
                    result.append("✅ No obvious quality issues detected in content.\n");
                } else {
                    result.append("**Potential Quality Issues Found:**\n\n");
                    result.append(response);
                    
                    result.append("\n\n")
                          .append(markdownFormatter.formatBlockquote(
                              "⚠️ **Quality Indicators Searched:**\\n" +
                              "- TODO, FIXME markers\\n" +
                              "- 'Outdated', 'deprecated' mentions\\n" +
                              "- 'Under construction' placeholders\\n" +
                              "- Legacy system references"))
                          .append("\n");
                }
                
                return result.toString();
            });
    }
    
    /**
     * Analyze technology documentation coverage
     */
    public Mono<String> analyzeTechnologyCoverage() {
        // Search for architecture and technology documentation
        String cql = buildTechnologyCoverageQuery();
        
        return searchService.search(cql, null, 50, 0, false, null)
            .map(response -> {
                StringBuilder result = new StringBuilder();
                result.append(markdownFormatter.formatHeading("🛠️ Technology Documentation Coverage", 2))
                      .append("\n\n");
                
                if (response.contains("No results found")) {
                    result.append("⚠️ Limited technology documentation found. Consider adding:\n")
                          .append("- Architecture overviews\n")
                          .append("- API documentation\n") 
                          .append("- System integration guides\n")
                          .append("- Technology stack documentation\n");
                } else {
                    result.append("**Technology Documentation Found:**\n\n");
                    result.append(response);
                }
                
                return result.toString();
            });
    }
    
    /**
     * Generate actionable recommendations
     */
    private String generateRecommendations() {
        StringBuilder result = new StringBuilder();
        result.append(markdownFormatter.formatHeading("💡 Actionable Recommendations", 2))
              .append("\n\n");
        
        result.append("### Immediate Actions:\n")
              .append("1. **Review Oldest Pages** - Update content older than 12 months\n")
              .append("2. **Fix Quality Issues** - Address TODO/FIXME markers found\n")
              .append("3. **Update Architecture Docs** - Ensure current system state is documented\n")
              .append("4. **Add Missing Documentation** - Fill identified technology gaps\n\n");
        
        result.append("### Automation Opportunities:\n")
              .append("- Set up regular documentation review cycles\n")
              .append("- Add 'last-reviewed' metadata to critical pages\n")
              .append("- Create templates for consistent documentation\n")
              .append("- Implement automated staleness alerts\n\n");
        
        result.append("### Maintenance Process:\n")
              .append("1. **Monthly Reviews** - Check pages >6 months old\n")
              .append("2. **Quarterly Audits** - Full documentation structure review\n")
              .append("3. **Release Documentation** - Update docs with each system change\n")
              .append("4. **Owner Assignment** - Assign documentation owners for each area\n");
        
        return result.toString();
    }
    
    /**
     * Build CQL query for finding stale/problematic content
     */
    private String buildStaleContentQuery() {
        String keywords = String.join(" OR ", STALENESS_INDICATORS);
        return String.format("type=page AND text~'%s'", keywords);
    }
    
    /**
     * Build CQL query for technology coverage analysis  
     */
    private String buildTechnologyCoverageQuery() {
        String archKeywords = String.join(" OR ", ARCHITECTURE_KEYWORDS);
        String techKeywords = String.join(" OR ", TECHNOLOGY_KEYWORDS);
        return String.format("type=page AND (text~'%s' OR text~'%s')", archKeywords, techKeywords);
    }
    
    /**
     * Parse spaces list response for analysis
     */
    private List<String> parseSpacesForAnalysis(String spacesResponse) {
        // Simple parsing - in real implementation you might want more sophisticated parsing
        return Arrays.asList(spacesResponse.split("\n"))
            .stream()
            .filter(line -> line.contains("Key:"))
            .limit(10)
            .toList();
    }
}