package io.github.greenstevester.confluencemcpsvr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Reports the dynamically assigned port when the web server starts
 */
@Component
public class DynamicPortReporter implements ApplicationListener<WebServerInitializedEvent> {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamicPortReporter.class);
    
    // ANSI color codes for console output
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_RESET = "\u001B[0m";
    
    @Autowired
    private Environment environment;
    
    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        int actualPort = event.getWebServer().getPort();
        String serverUrl = "http://localhost:" + actualPort;
        String mcpEndpoint = serverUrl + "/mcp/message";
        
        // Console output with colors
        System.out.println();
        System.out.println(ANSI_BOLD + ANSI_GREEN + "🚀 MCP Server Started Successfully!" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "   Server URL: " + ANSI_YELLOW + serverUrl + ANSI_RESET);
        System.out.println(ANSI_CYAN + "   MCP Endpoint: " + ANSI_YELLOW + mcpEndpoint + ANSI_RESET);
        System.out.println(ANSI_CYAN + "   Dynamically assigned port: " + ANSI_YELLOW + actualPort + ANSI_RESET);
        
        // Check if port was dynamically assigned
        String configuredPort = environment.getProperty("server.port", "8080");
        if ("0".equals(configuredPort)) {
            System.out.println(ANSI_GREEN + "   ✅ Port dynamically assigned (configured as 0)" + ANSI_RESET);
        } else {
            // Check if the configured port is different from the actual port (collision occurred)
            if (!configuredPort.equals(String.valueOf(actualPort))) {
                System.out.println(ANSI_GREEN + "   ⚡ Port collision resolved! Configured: " + configuredPort + ", Used: " + actualPort + ANSI_RESET);
            } else {
                System.out.println(ANSI_YELLOW + "   📝 Using configured port: " + configuredPort + ANSI_RESET);
            }
        }
        
        System.out.println();
        
        // Structured log entry for parsing/monitoring
        logger.info("MCP Server started on dynamically assigned port: {} - URL: {} - MCP endpoint: {}", 
                   actualPort, serverUrl, mcpEndpoint);
    }
}