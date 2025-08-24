package io.github.greenstevester.confluencemcpsvr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Specialized service for systematic documentation auditing with predefined queries
 */
@Service
public class DocumentationAuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentationAuditService.class);
    
    private final ConfluenceSearchService searchService;
    
    public DocumentationAuditService(ConfluenceSearchService searchService) {
        this.searchService = searchService;
    }
    
    /**
     * Predefined audit queries for different documentation aspects
     */
    public enum AuditQuery {
        // Age-based queries - using dynamic date calculation
        VERY_OLD_PAGES("", "Pages older than 2 years"),
        OLD_PAGES("", "Pages older than 1 year"),
        STALE_PAGES("", "Pages older than 6 months"),
        
        // Quality indicators
        TODO_MARKERS("type=page AND text~'TODO OR FIXME OR HACK OR BUG'", "Pages with TODO/FIXME markers"),
        DEPRECATED_CONTENT("type=page AND text~'deprecated OR legacy OR obsolete OR outdated'", "Pages mentioning deprecated/legacy content"),
        PLACEHOLDER_CONTENT("type=page AND text~'placeholder OR under construction OR coming soon OR TBD'", "Placeholder or incomplete pages"),
        DRAFT_CONTENT("type=page AND text~'draft OR work in progress OR WIP OR not final'", "Draft or work-in-progress pages"),
        
        // Architecture documentation
        ARCHITECTURE_DOCS("type=page AND (title~'architecture' OR text~'system design' OR text~'technical specification')", "Architecture and design documentation"),
        API_DOCS("type=page AND (title~'API' OR text~'REST API' OR text~'endpoint' OR text~'swagger')", "API documentation"),
        DATABASE_DOCS("type=page AND text~'database OR schema OR PostgreSQL OR MySQL OR MongoDB OR table'", "Database documentation"),
        DEPLOYMENT_DOCS("type=page AND text~'deployment OR docker OR kubernetes OR AWS OR infrastructure'", "Deployment and infrastructure docs"),
        
        // Technology-specific
        JAVA_DOCS("type=page AND text~'Java OR Spring OR Maven OR Gradle'", "Java/Spring documentation"),
        FRONTEND_DOCS("type=page AND text~'React OR Angular OR Vue OR JavaScript OR TypeScript'", "Frontend documentation"),
        MICROSERVICES_DOCS("type=page AND text~'microservice OR service mesh OR API gateway'", "Microservices documentation"),
        SECURITY_DOCS("type=page AND text~'security OR authentication OR authorization OR OAuth OR JWT'", "Security documentation"),
        
        // Process documentation  
        ONBOARDING_DOCS("type=page AND text~'onboarding OR getting started OR setup OR installation'", "Onboarding and setup docs"),
        TROUBLESHOOTING_DOCS("type=page AND text~'troubleshooting OR FAQ OR common issues OR debugging'", "Troubleshooting documentation"),
        RUNBOOK_DOCS("type=page AND text~'runbook OR operations OR monitoring OR alerts'", "Operational runbooks"),
        
        // Compliance and governance
        COMPLIANCE_DOCS("type=page AND text~'compliance OR GDPR OR security policy OR audit'", "Compliance documentation"),
        PROCESS_DOCS("type=page AND text~'process OR procedure OR workflow OR policy'", "Process documentation");
        
        private final String cql;
        private final String description;
        
        AuditQuery(String cql, String description) {
            this.cql = cql;
            this.description = description;
        }
        
        public String getCql() { 
            // Generate dynamic date queries for age-based queries
            return switch (this) {
                case VERY_OLD_PAGES -> generateDateQuery(24); // 2 years in months
                case OLD_PAGES -> generateDateQuery(12);      // 1 year in months  
                case STALE_PAGES -> generateDateQuery(6);     // 6 months
                default -> cql;
            };
        }
        
        public String getDescription() { return description; }
        
        /**
         * Generate CQL date query for pages older than specified months
         */
        private static String generateDateQuery(int monthsOld) {
            LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(monthsOld);
            String dateString = cutoffDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            return String.format("type=page AND lastModified <= '%s'", dateString);
        }
    }
    
    /**
     * Execute a specific audit query
     */
    public Mono<String> executeAuditQuery(AuditQuery query, Integer limit) {
        logger.debug("Executing audit query: {}", query.name());
        
        return searchService.search(query.getCql(), null, limit != null ? limit : 50, 0, false, null)
            .map(response -> {
                StringBuilder result = new StringBuilder();
                result.append("## ").append(query.getDescription()).append("\n\n");
                result.append("**Query:** `").append(query.getCql()).append("`\n\n");
                
                if (response.contains("No results found")) {
                    result.append("✅ No issues found for this query.\n");
                } else {
                    result.append("**Results:**\n\n").append(response);
                }
                
                return result.toString();
            });
    }
    
    /**
     * Execute systematic audit checklist
     */
    public Mono<String> executeSystematicAudit(List<AuditQuery> queries, Integer limitPerQuery) {
        return Mono.fromCallable(() -> {
            StringBuilder report = new StringBuilder();
            
            // Header
            report.append("# 📋 Systematic Documentation Audit Report\n\n");
            report.append("**Generated:** ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n");
            report.append("**Queries Executed:** ").append(queries.size()).append("\n");
            report.append("**Limit per Query:** ").append(limitPerQuery != null ? limitPerQuery : 50).append("\n\n");
            
            // Execute each query
            for (AuditQuery query : queries) {
                try {
                    String queryResult = executeAuditQuery(query, limitPerQuery).block();
                    report.append(queryResult).append("\n\n---\n\n");
                } catch (Exception e) {
                    logger.error("Error executing query: {}", query.name(), e);
                    report.append("## ❌ Error executing: ").append(query.getDescription()).append("\n");
                    report.append("**Error:** ").append(e.getMessage()).append("\n\n---\n\n");
                }
            }
            
            // Summary and recommendations
            report.append(generateAuditSummary(queries));
            
            return report.toString();
        });
    }
    
    /**
     * Get predefined audit checklists for different scenarios
     */
    public List<AuditQuery> getAuditChecklist(String checklistType) {
        return switch (checklistType.toLowerCase()) {
            case "quality" -> Arrays.asList(
                AuditQuery.TODO_MARKERS,
                AuditQuery.DEPRECATED_CONTENT,
                AuditQuery.PLACEHOLDER_CONTENT,
                AuditQuery.DRAFT_CONTENT
            );
            
            case "freshness" -> Arrays.asList(
                AuditQuery.VERY_OLD_PAGES,
                AuditQuery.OLD_PAGES,
                AuditQuery.STALE_PAGES
            );
            
            case "architecture" -> Arrays.asList(
                AuditQuery.ARCHITECTURE_DOCS,
                AuditQuery.API_DOCS,
                AuditQuery.DATABASE_DOCS,
                AuditQuery.DEPLOYMENT_DOCS,
                AuditQuery.MICROSERVICES_DOCS
            );
            
            case "technology" -> Arrays.asList(
                AuditQuery.JAVA_DOCS,
                AuditQuery.FRONTEND_DOCS,
                AuditQuery.SECURITY_DOCS,
                AuditQuery.DATABASE_DOCS
            );
            
            case "operations" -> Arrays.asList(
                AuditQuery.ONBOARDING_DOCS,
                AuditQuery.TROUBLESHOOTING_DOCS,
                AuditQuery.RUNBOOK_DOCS,
                AuditQuery.DEPLOYMENT_DOCS
            );
            
            case "compliance" -> Arrays.asList(
                AuditQuery.COMPLIANCE_DOCS,
                AuditQuery.PROCESS_DOCS,
                AuditQuery.SECURITY_DOCS
            );
            
            case "full" -> Arrays.asList(AuditQuery.values());
            
            default -> Arrays.asList(
                AuditQuery.TODO_MARKERS,
                AuditQuery.OLD_PAGES,
                AuditQuery.ARCHITECTURE_DOCS,
                AuditQuery.API_DOCS
            );
        };
    }
    
    /**
     * Generate custom CQL queries for domain-specific audits
     */
    public Map<String, String> generateDomainSpecificQueries(List<String> technologies, List<String> businessDomains) {
        Map<String, String> queries = new HashMap<>();
        
        // Technology-specific queries
        if (technologies != null && !technologies.isEmpty()) {
            String techTerms = String.join(" OR ", technologies);
            queries.put("Technology Coverage", String.format("type=page AND text~'%s'", techTerms));
        }
        
        // Business domain queries
        if (businessDomains != null && !businessDomains.isEmpty()) {
            String domainTerms = String.join(" OR ", businessDomains);
            queries.put("Business Domain Coverage", String.format("type=page AND text~'%s'", domainTerms));
        }
        
        // Combination queries
        if (technologies != null && businessDomains != null && !technologies.isEmpty() && !businessDomains.isEmpty()) {
            String techTerms = String.join(" OR ", technologies);
            String domainTerms = String.join(" OR ", businessDomains);
            queries.put("Tech-Domain Integration", 
                String.format("type=page AND (text~'%s') AND (text~'%s')", techTerms, domainTerms));
        }
        
        return queries;
    }
    
    /**
     * Generate audit summary and recommendations
     */
    private String generateAuditSummary(List<AuditQuery> executedQueries) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("# 📊 Audit Summary & Recommendations\n\n");
        
        summary.append("## Queries Executed\n");
        for (AuditQuery query : executedQueries) {
            summary.append("- ").append(query.getDescription()).append("\n");
        }
        summary.append("\n");
        
        summary.append("## 🎯 Priority Actions\n\n");
        summary.append("### High Priority\n");
        summary.append("1. **Address TODO/FIXME markers** - Update or remove temporary content\n");
        summary.append("2. **Update very old pages** - Review pages older than 1-2 years\n");
        summary.append("3. **Complete draft content** - Finish placeholder/WIP documentation\n\n");
        
        summary.append("### Medium Priority\n");
        summary.append("1. **Review deprecated references** - Update or remove legacy content\n");
        summary.append("2. **Enhance architecture docs** - Ensure current system state is documented\n");
        summary.append("3. **Update technology documentation** - Reflect current tech stack\n\n");
        
        summary.append("### Low Priority\n");
        summary.append("1. **Improve organization** - Better categorization and linking\n");
        summary.append("2. **Add missing documentation** - Fill identified gaps\n");
        summary.append("3. **Standardize formatting** - Consistent documentation style\n\n");
        
        summary.append("## 🔄 Recommended Audit Schedule\n\n");
        summary.append("- **Weekly:** Quality checks (TODO/FIXME markers)\n");
        summary.append("- **Monthly:** Freshness review (pages >6 months old)\n");
        summary.append("- **Quarterly:** Full architecture and technology audit\n");
        summary.append("- **Annually:** Complete documentation structure review\n");
        
        return summary.toString();
    }
}