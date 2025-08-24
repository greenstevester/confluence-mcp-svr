package io.github.greenstevester.confluencemcpsvr.config;

import io.github.greenstevester.confluencemcpsvr.client.ConfluenceSpacesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Startup health checker that validates Confluence connectivity and displays endpoint information
 */
@Component
public class StartupHealthChecker implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupHealthChecker.class);
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";

    private final ConfluenceSpacesClient confluenceClient;
    private final Environment environment;
    private final McpServerProperties mcpServerProperties;
    
    @Value("${server.port:8081}")
    private String serverPort;

    @Value("${spring.ai.mcp.server.enabled:false}")
    private boolean mcpServerEnabled;

    @Value("${spring.ai.mcp.server.sse-message-endpoint:/mcp/message}")
    private String mcpEndpoint;

    @Autowired
    public StartupHealthChecker(ConfluenceSpacesClient confluenceClient,
                               Environment environment,
                               McpServerProperties mcpServerProperties) {
        this.confluenceClient = confluenceClient;
        this.environment = environment;
        this.mcpServerProperties = mcpServerProperties;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Skip console output in STDIO mode to prevent interference with MCP protocol
        if (!isStdioMode()) {
            printStartupBanner();
            checkConfluenceConnection();
            displayEndpointInformation();
            printFooter();
        }
    }
    
    private boolean isStdioMode() {
        return environment.matchesProfiles("stdio") || 
               Boolean.parseBoolean(environment.getProperty("spring.ai.mcp.server.stdio", "false"));
    }

    private void printStartupBanner() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println();
        System.out.println(ANSI_CYAN + "╔════════════════════════════════════════════════════════════╗" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "║" + ANSI_BOLD + "           Confluence MCP Server - Health Check             " + ANSI_RESET + ANSI_CYAN + "║" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "║" + String.format("                    %s                     ", timestamp) + "║" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "╚════════════════════════════════════════════════════════════╝" + ANSI_RESET);
        System.out.println();
    }

    private void checkConfluenceConnection() {
        System.out.println(ANSI_BLUE + "🔗 Confluence Connection Check" + ANSI_RESET);
        System.out.println("   ├─ Testing connection to Confluence API...");
        
        try {
            // Try to fetch a small number of spaces to test connectivity (non-blocking)
            var spaces = confluenceClient.listSpaces(null, null, null, null, null, 1)
                .block(java.time.Duration.ofSeconds(5)); // Block for max 5 seconds
            
            System.out.println("   ├─ " + ANSI_GREEN + "✓ Connection successful!" + ANSI_RESET);
            System.out.println("   ├─ " + ANSI_GREEN + "✓ Authentication verified" + ANSI_RESET);
            
            if (spaces != null && spaces.results() != null) {
                System.out.println("   └─ " + ANSI_GREEN + "✓ Found " + spaces.results().size() + " space(s) accessible" + ANSI_RESET);
            } else {
                System.out.println("   └─ " + ANSI_YELLOW + "⚠ Connection works but no spaces returned" + ANSI_RESET);
            }
            
        } catch (Exception e) {
            System.out.println("   ├─ " + ANSI_RED + "✗ Connection failed: " + e.getMessage() + ANSI_RESET);
            System.out.println("   └─ " + ANSI_RED + "✗ Please check your Confluence credentials and network connectivity" + ANSI_RESET);
        }
        System.out.println();
    }

    private void displayEndpointInformation() {
        String baseUrl = "http://localhost:" + serverPort;
        
        // MCP Endpoints
        System.out.println(ANSI_BLUE + "🔌 MCP Server Endpoints" + ANSI_RESET);
        if (mcpServerEnabled) {
            System.out.println("   ├─ " + ANSI_GREEN + "✓ MCP Server: ENABLED" + ANSI_RESET);
            System.out.println("   ├─ Name: " + mcpServerProperties.name());
            System.out.println("   ├─ Version: " + mcpServerProperties.version());
            System.out.println("   ├─ " + ANSI_BOLD + "SSE Endpoint: " + baseUrl + mcpEndpoint + ANSI_RESET);
            System.out.println("   └─ " + ANSI_CYAN + "Test: curl -H \"Accept: text/event-stream\" " + baseUrl + mcpEndpoint + ANSI_RESET);
        } else {
            System.out.println("   └─ " + ANSI_YELLOW + "⚠ MCP Server: DISABLED" + ANSI_RESET);
        }
        System.out.println();
        
        // API Endpoints
        System.out.println(ANSI_BLUE + "🌐 API Endpoints" + ANSI_RESET);
        System.out.println("   ├─ " + ANSI_BOLD + "Root API: " + baseUrl + "/" + ANSI_RESET);
        System.out.println("   └─ " + ANSI_CYAN + "Test: curl " + baseUrl + "/" + ANSI_RESET);
        System.out.println();
        
        // Health & Monitoring Endpoints  
        System.out.println(ANSI_BLUE + "🏥 Health & Monitoring Endpoints" + ANSI_RESET);
        
        List<String> healthEndpoints = getHealthEndpoints(baseUrl);
        for (int i = 0; i < healthEndpoints.size(); i++) {
            boolean isLast = (i == healthEndpoints.size() - 1);
            String prefix = isLast ? "   └─ " : "   ├─ ";
            System.out.println(prefix + healthEndpoints.get(i));
        }
        System.out.println();
    }

    private List<String> getHealthEndpoints(String baseUrl) {
        List<String> endpoints = new ArrayList<>();
        
        // Standard actuator endpoints that are typically available
        endpoints.add(ANSI_BOLD + "Health: " + baseUrl + "/actuator/health" + ANSI_RESET);
        endpoints.add(ANSI_BOLD + "Info: " + baseUrl + "/actuator/info" + ANSI_RESET);
        endpoints.add(ANSI_BOLD + "Metrics: " + baseUrl + "/actuator/metrics" + ANSI_RESET);
        
        // Add test commands
        endpoints.add(ANSI_CYAN + "Test Health: curl " + baseUrl + "/actuator/health" + ANSI_RESET);
        endpoints.add(ANSI_CYAN + "Test Info: curl " + baseUrl + "/actuator/info" + ANSI_RESET);
        
        return endpoints;
    }

    private void printFooter() {
        String[] profiles = environment.getActiveProfiles();
        String profileStr = profiles.length > 0 ? String.join(", ", profiles) : "default";
        
        System.out.println(ANSI_BLUE + "📋 Runtime Information" + ANSI_RESET);
        System.out.println("   ├─ Active Profiles: " + ANSI_YELLOW + profileStr + ANSI_RESET);
        System.out.println("   ├─ Server Port: " + ANSI_YELLOW + serverPort + ANSI_RESET);
        System.out.println("   ├─ Java Version: " + ANSI_YELLOW + System.getProperty("java.version") + ANSI_RESET);
        System.out.println("   └─ Spring Boot: " + ANSI_YELLOW + org.springframework.boot.SpringBootVersion.getVersion() + ANSI_RESET);
        System.out.println();
        
        System.out.println(ANSI_GREEN + ANSI_BOLD + "🚀 Confluence MCP Server is ready for connections!" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "   Use the endpoints above for integration and health checks" + ANSI_RESET);
        System.out.println();
    }
}