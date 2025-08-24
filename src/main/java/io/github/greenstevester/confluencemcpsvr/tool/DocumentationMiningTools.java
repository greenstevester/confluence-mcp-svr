package io.github.greenstevester.confluencemcpsvr.tool;

import io.github.greenstevester.confluencemcpsvr.service.DocumentationMiningService;
import io.github.greenstevester.confluencemcpsvr.service.DocumentationAuditService;
import io.github.greenstevester.confluencemcpsvr.service.BatchProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * MCP Tools for Documentation Mining and Analysis
 */
@Service
public class DocumentationMiningTools {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentationMiningTools.class);
    
    private final DocumentationMiningService miningService;
    private final DocumentationAuditService auditService;
    private final BatchProcessingService batchProcessingService;
    
    public DocumentationMiningTools(DocumentationMiningService miningService, 
                                   DocumentationAuditService auditService,
                                   BatchProcessingService batchProcessingService) {
        this.miningService = miningService;
        this.auditService = auditService;
        this.batchProcessingService = batchProcessingService;
    }
    
    /**
     * Perform comprehensive documentation audit and analysis
     * 
     * Analyzes your entire Confluence instance to identify documentation quality,
     * freshness, coverage gaps, and maintenance needs.
     * 
     * PURPOSE: Comprehensive documentation health assessment for system maintenance.
     * 
     * WHEN TO USE:
     * - Monthly/quarterly documentation reviews
     * - Before major system updates or releases
     * - When onboarding new team members who need current documentation
     * - To identify outdated or missing critical documentation
     * - For compliance and documentation governance
     * 
     * ANALYSIS INCLUDES:
     * - Space organization and structure assessment
     * - Page freshness analysis (identifies old/stale content)
     * - Content quality issues (TODO/FIXME markers, deprecated content)
     * - Technology documentation coverage gaps
     * - Actionable recommendations for improvement
     * 
     * RETURNS: Comprehensive markdown report with:
     * - Documentation structure overview
     * - List of oldest/most stale pages needing review
     * - Content quality issues requiring attention
     * - Technology coverage analysis
     * - Prioritized recommendations for documentation maintenance
     * 
     * EXAMPLES:
     * - Full audit: { "maxResults": 100 }
     * - Quick assessment: { "maxResults": 25 }
     * - Default scope: { }
     * 
     * BEST PRACTICES:
     * - Run monthly for active documentation maintenance
     * - Use results to prioritize documentation update tasks
     * - Track improvements over time by comparing reports
     * - Share results with team leads and documentation owners
     */
    public String auditDocumentation(DocumentationAuditRequest request) {
        logger.debug("audit_documentation tool called with: {}", request);
        
        try {
            return miningService.performDocumentationAudit(request.maxResults()).block();
            
        } catch (Exception e) {
            logger.error("Error in audit_documentation tool", e);
            return "Error performing documentation audit: " + e.getMessage();
        }
    }
    
    /**
     * Analyze space organization and documentation structure
     * 
     * Provides overview of how your documentation is organized across Confluence spaces.
     * 
     * PURPOSE: Understand documentation architecture and identify organizational improvements.
     * 
     * WHEN TO USE:
     * - Planning documentation reorganization
     * - Understanding current documentation landscape
     * - Identifying redundant or misplaced content
     * - Before implementing new documentation standards
     * 
     * RETURNS: Analysis of:
     * - Available spaces and their purposes
     * - Space types and organization patterns
     * - Recommendations for structural improvements
     * 
     * EXAMPLE: { }
     */
    public String analyzeSpaceStructure() {
        logger.debug("analyze_space_structure tool called");
        
        try {
            return miningService.analyzeSpaces().block();
            
        } catch (Exception e) {
            logger.error("Error in analyze_space_structure tool", e);
            return "Error analyzing space structure: " + e.getMessage();
        }
    }
    
    /**
     * Analyze page freshness and identify outdated content
     * 
     * Identifies pages that haven't been updated recently and may need review.
     * 
     * PURPOSE: Find potentially stale documentation requiring updates.
     * 
     * WHEN TO USE:
     * - Regular documentation maintenance cycles
     * - Before major system releases
     * - When validating documentation accuracy
     * - To prioritize documentation update efforts
     * 
     * RETURNS: Analysis including:
     * - Oldest pages by last modification date
     * - Age assessment criteria and recommendations
     * - Priority levels for update efforts
     * 
     * EXAMPLES:
     * - Review 50 oldest pages: { "maxResults": 50 }
     * - Quick check of 10 oldest: { "maxResults": 10 }
     * - Default analysis: { }
     */
    public String analyzeFreshness(PageFreshnessRequest request) {
        logger.debug("analyze_freshness tool called with: {}", request);
        
        try {
            return miningService.analyzePageFreshness(request.maxResults()).block();
            
        } catch (Exception e) {
            logger.error("Error in analyze_freshness tool", e);
            return "Error analyzing page freshness: " + e.getMessage();
        }
    }
    
    /**
     * Analyze technology documentation coverage
     * 
     * Identifies gaps in technical documentation for your technology stack.
     * 
     * PURPOSE: Ensure comprehensive coverage of your technical architecture and systems.
     * 
     * WHEN TO USE:
     * - After adopting new technologies
     * - During architecture reviews
     * - When onboarding new developers
     * - For technical documentation audits
     * 
     * RETURNS: Analysis of:
     * - Existing technology documentation
     * - Coverage gaps for key technologies
     * - Recommendations for missing documentation
     * 
     * COVERS TECHNOLOGIES:
     * - Architecture patterns (microservices, APIs)
     * - Programming languages (Java, Spring)
     * - Infrastructure (Docker, Kubernetes, AWS)
     * - Databases and data systems
     * 
     * EXAMPLE: { }
     */
    public String analyzeTechnologyCoverage() {
        logger.debug("analyze_technology_coverage tool called");
        
        try {
            return miningService.analyzeTechnologyCoverage().block();
            
        } catch (Exception e) {
            logger.error("Error in analyze_technology_coverage tool", e);
            return "Error analyzing technology coverage: " + e.getMessage();
        }
    }
    
    /**
     * Execute systematic documentation audit with predefined checklists
     * 
     * Runs comprehensive audit using predefined query sets for specific documentation aspects.
     * 
     * PURPOSE: Systematic evaluation of documentation quality and completeness using proven checklists.
     * 
     * WHEN TO USE:
     * - Regular scheduled audits (monthly/quarterly)
     * - Before major releases or compliance reviews
     * - When establishing documentation governance
     * - For systematic improvement of documentation quality
     * 
     * CHECKLIST TYPES:
     * - "quality" - TODO markers, deprecated content, drafts, placeholders
     * - "freshness" - Age-based analysis (very old, old, stale pages)
     * - "architecture" - Architecture docs, API docs, database docs, deployment
     * - "technology" - Java, frontend, security, database documentation
     * - "operations" - Onboarding, troubleshooting, runbooks, deployment
     * - "compliance" - Compliance, process, security policy documentation
     * - "full" - Complete audit using all available queries
     * 
     * RETURNS: Detailed report with:
     * - Results for each checklist query
     * - Priority-based action recommendations
     * - Suggested audit schedule for ongoing maintenance
     * 
     * EXAMPLES:
     * - Quality audit: { "checklistType": "quality", "limitPerQuery": 25 }
     * - Architecture review: { "checklistType": "architecture", "limitPerQuery": 50 }
     * - Full comprehensive audit: { "checklistType": "full", "limitPerQuery": 30 }
     */
    public String executeSystematicAudit(SystematicAuditRequest request) {
        logger.debug("execute_systematic_audit tool called with: {}", request);
        
        try {
            List<DocumentationAuditService.AuditQuery> queries = 
                auditService.getAuditChecklist(request.checklistType() != null ? request.checklistType() : "quality");
            
            return auditService.executeSystematicAudit(queries, request.limitPerQuery()).block();
            
        } catch (Exception e) {
            logger.error("Error in execute_systematic_audit tool", e);
            return "Error executing systematic audit: " + e.getMessage();
        }
    }
    
    /**
     * Execute specific audit query for targeted analysis
     * 
     * Runs a single predefined audit query for focused analysis of specific documentation aspects.
     * 
     * PURPOSE: Targeted analysis of specific documentation quality or coverage areas.
     * 
     * WHEN TO USE:
     * - Investigating specific quality issues
     * - Monitoring particular documentation categories
     * - Quick checks between full audits
     * - Validating fixes or improvements
     * 
     * AVAILABLE QUERIES:
     * - Age-based: VERY_OLD_PAGES, OLD_PAGES, STALE_PAGES
     * - Quality: TODO_MARKERS, DEPRECATED_CONTENT, PLACEHOLDER_CONTENT, DRAFT_CONTENT
     * - Architecture: ARCHITECTURE_DOCS, API_DOCS, DATABASE_DOCS, DEPLOYMENT_DOCS
     * - Technology: JAVA_DOCS, FRONTEND_DOCS, MICROSERVICES_DOCS, SECURITY_DOCS
     * - Operations: ONBOARDING_DOCS, TROUBLESHOOTING_DOCS, RUNBOOK_DOCS
     * - Compliance: COMPLIANCE_DOCS, PROCESS_DOCS
     * 
     * RETURNS: Focused results for the specific query with recommendations.
     * 
     * EXAMPLES:
     * - Find TODO markers: { "queryName": "TODO_MARKERS", "limit": 20 }
     * - Check old pages: { "queryName": "OLD_PAGES", "limit": 30 }
     * - Review API docs: { "queryName": "API_DOCS", "limit": 15 }
     */
    public String executeSpecificAudit(SpecificAuditRequest request) {
        logger.debug("execute_specific_audit tool called with: {}", request);
        
        try {
            DocumentationAuditService.AuditQuery query = 
                DocumentationAuditService.AuditQuery.valueOf(request.queryName().toUpperCase());
            
            return auditService.executeAuditQuery(query, request.limit()).block();
            
        } catch (IllegalArgumentException e) {
            return "Invalid query name: " + request.queryName() + ". Available queries: " + 
                   String.join(", ", getAvailableQueryNames());
        } catch (Exception e) {
            logger.error("Error in execute_specific_audit tool", e);
            return "Error executing specific audit: " + e.getMessage();
        }
    }
    
    /**
     * Get available audit queries and checklists
     * 
     * Lists all available predefined queries and checklist types for reference.
     * 
     * PURPOSE: Reference guide for available audit capabilities.
     * 
     * RETURNS: Complete list of available queries and checklist types with descriptions.
     * 
     * EXAMPLE: { }
     */
    public String getAvailableAudits() {
        logger.debug("get_available_audits tool called");
        
        StringBuilder result = new StringBuilder();
        result.append("# 📋 Available Documentation Audit Tools\n\n");
        
        // Checklist types
        result.append("## Systematic Audit Checklists\n\n");
        result.append("- **quality** - Content quality issues (TODO, deprecated, drafts)\n");
        result.append("- **freshness** - Age-based analysis (old and stale pages)\n");
        result.append("- **architecture** - Architecture and design documentation\n");
        result.append("- **technology** - Technology-specific documentation coverage\n");
        result.append("- **operations** - Operational and process documentation\n");
        result.append("- **compliance** - Compliance and governance documentation\n");
        result.append("- **full** - Complete audit using all available queries\n\n");
        
        // Individual queries
        result.append("## Specific Audit Queries\n\n");
        for (DocumentationAuditService.AuditQuery query : DocumentationAuditService.AuditQuery.values()) {
            result.append("- **").append(query.name()).append("** - ").append(query.getDescription()).append("\n");
        }
        
        result.append("\n## Usage Examples\n\n");
        result.append("```json\n");
        result.append("// Systematic quality audit\n");
        result.append("{\n");
        result.append("  \"tool\": \"execute_systematic_audit\",\n");
        result.append("  \"params\": {\n");
        result.append("    \"checklistType\": \"quality\",\n");
        result.append("    \"limitPerQuery\": 25\n");
        result.append("  }\n");
        result.append("}\n\n");
        result.append("// Specific query for TODO markers\n");
        result.append("{\n");
        result.append("  \"tool\": \"execute_specific_audit\",\n");
        result.append("  \"params\": {\n");
        result.append("    \"queryName\": \"TODO_MARKERS\",\n");
        result.append("    \"limit\": 20\n");
        result.append("  }\n");
        result.append("}\n");
        result.append("```\n");
        
        return result.toString();
    }
    
    /**
     * Helper method to get available query names
     */
    private String[] getAvailableQueryNames() {
        DocumentationAuditService.AuditQuery[] queries = DocumentationAuditService.AuditQuery.values();
        String[] names = new String[queries.length];
        for (int i = 0; i < queries.length; i++) {
            names[i] = queries[i].name();
        }
        return names;
    }
    
    /**
     * Batch update multiple pages with automated enhancements
     * 
     * Updates multiple pages simultaneously with consistent improvements or metadata additions.
     * 
     * PURPOSE: Efficiently apply consistent updates across multiple pages for maintenance tasks.
     * 
     * WHEN TO USE:
     * - Adding "last-reviewed" metadata to multiple pages
     * - Standardizing formatting across documentation
     * - Bulk corrections or improvements
     * - Adding consistent headers, footers, or disclaimers
     * 
     * UPDATE TEMPLATES:
     * - Metadata additions (dates, ownership, review status)
     * - Content standardization (formatting, structure)
     * - Automated corrections (links, references)
     * - Compliance additions (disclaimers, policies)
     * 
     * RETURNS: Detailed report with:
     * - Individual page update results
     * - Success/failure statistics
     * - Error details for troubleshooting
     * 
     * EXAMPLES:
     * - Add review metadata: { "pageIds": ["123", "456"], "updateTemplate": "last-reviewed: 2024-08-24", "dryRun": true }
     * - Apply changes: { "pageIds": ["123", "456"], "updateTemplate": "standardization", "dryRun": false }
     * 
     * SAFETY: Always use dryRun=true first to preview changes before applying.
     */
    public String batchUpdatePages(BatchUpdateRequest request) {
        logger.debug("batch_update_pages tool called with: {}", request);
        
        try {
            return batchProcessingService.batchUpdatePages(
                request.pageIds(), 
                request.updateTemplate(), 
                request.dryRun() != null ? request.dryRun() : true
            ).block();
            
        } catch (Exception e) {
            logger.error("Error in batch_update_pages tool", e);
            return "Error in batch page update: " + e.getMessage();
        }
    }
    
    /**
     * Batch analyze content across multiple spaces or pages
     * 
     * Performs large-scale content analysis to identify patterns, issues, and opportunities.
     * 
     * PURPOSE: Systematic analysis of documentation quality and consistency across large content sets.
     * 
     * WHEN TO USE:
     * - Quarterly documentation health checks
     * - Before major system changes or releases
     * - When establishing new documentation standards
     * - For compliance or governance reporting
     * 
     * ANALYSIS TYPES:
     * - "freshness" - Identifies outdated content by modification dates
     * - "completeness" - Finds TODO markers, placeholders, incomplete sections
     * - "consistency" - Checks formatting, naming, and style consistency
     * - "coverage" - Analyzes documentation coverage for topics/technologies
     * 
     * RETURNS: Comprehensive analysis report with:
     * - Content quality metrics and statistics
     * - Identified issues and improvement opportunities  
     * - Priority-based recommendations for action
     * - Batch processing summary and completion status
     * 
     * EXAMPLES:
     * - Analyze freshness in specific spaces: { "spaceIds": ["DEV", "OPS"], "analysisType": "freshness" }
     * - Check completeness across all spaces: { "spaceIds": null, "analysisType": "completeness" }
     * - Consistency review: { "spaceIds": ["DOCS"], "analysisType": "consistency" }
     */
    public String batchAnalyzeContent(BatchAnalysisRequest request) {
        logger.debug("batch_analyze_content tool called with: {}", request);
        
        try {
            return batchProcessingService.batchAnalyzeContent(
                request.spaceIds(), 
                request.analysisType() != null ? request.analysisType() : "freshness"
            ).block();
            
        } catch (Exception e) {
            logger.error("Error in batch_analyze_content tool", e);
            return "Error in batch content analysis: " + e.getMessage();
        }
    }
    
    /**
     * Find and map related content across documentation
     * 
     * Discovers relationships between pages and suggests cross-referencing opportunities.
     * 
     * PURPOSE: Improve documentation discoverability and create better information architecture.
     * 
     * WHEN TO USE:
     * - Creating topic-based navigation and linking
     * - Identifying documentation silos and integration opportunities
     * - Building knowledge maps and content relationships
     * - Improving user journey through related information
     * 
     * RELATIONSHIP DISCOVERY:
     * - Keyword-based content correlation
     * - Topic clustering and similarity analysis
     * - Cross-reference opportunity identification
     * - Navigation improvement suggestions
     * 
     * RETURNS: Relationship analysis report with:
     * - Related content groups organized by keywords
     * - Cross-referencing opportunities and recommendations
     * - Content relationship statistics and insights
     * - Actionable suggestions for improving content connections
     * 
     * EXAMPLES:
     * - Find API-related content: { "keywordSets": ["API,REST,endpoint", "authentication,OAuth"], "maxResults": 25 }
     * - Map architecture topics: { "keywordSets": ["microservices", "database", "deployment"], "maxResults": 20 }
     */
    public String batchFindRelatedContent(RelatedContentRequest request) {
        logger.debug("batch_find_related_content tool called with: {}", request);
        
        try {
            return batchProcessingService.batchFindRelatedContent(
                request.keywordSets(), 
                request.maxResults()
            ).block();
            
        } catch (Exception e) {
            logger.error("Error in batch_find_related_content tool", e);
            return "Error finding related content: " + e.getMessage();
        }
    }
    
    // Request objects for all tools
    
    /**
     * Request object for documentation audit tool
     */
    public record DocumentationAuditRequest(
        Integer maxResults
    ) {}
    
    /**
     * Request object for page freshness analysis
     */
    public record PageFreshnessRequest(
        Integer maxResults
    ) {}
    
    /**
     * Request object for systematic audit
     */
    public record SystematicAuditRequest(
        String checklistType,
        Integer limitPerQuery
    ) {}
    
    /**
     * Request object for specific audit query
     */
    public record SpecificAuditRequest(
        String queryName,
        Integer limit
    ) {}
    
    /**
     * Request object for batch update operations
     */
    public record BatchUpdateRequest(
        List<String> pageIds,
        String updateTemplate,
        Boolean dryRun
    ) {}
    
    /**
     * Request object for batch analysis operations
     */
    public record BatchAnalysisRequest(
        List<String> spaceIds,
        String analysisType
    ) {}
    
    /**
     * Request object for related content discovery
     */
    public record RelatedContentRequest(
        List<String> keywordSets,
        Integer maxResults
    ) {}
}