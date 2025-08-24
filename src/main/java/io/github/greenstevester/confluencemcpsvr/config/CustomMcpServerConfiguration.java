package io.github.greenstevester.confluencemcpsvr.config;

import io.github.greenstevester.confluencemcpsvr.mcp.AIToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * Custom MCP server configuration using our AITool registry
 * Logs the discovered tools and checks Spring AI integration.
 */
@Configuration
public class CustomMcpServerConfiguration implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomMcpServerConfiguration.class);
    
    @Autowired
    private AIToolRegistry toolRegistry;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("=== Custom MCP Tools Discovery ===");
        logger.info("Found {} @AITool methods:", toolRegistry.getAllTools().size());
        
        toolRegistry.getAllTools().forEach(tool -> {
            logger.info("  - {} ({}): {}", tool.getName(), tool.getMethod().getDeclaringClass().getSimpleName(), tool.getDescription());
        });
        
        // Check Spring AI integration
        logger.info("=== Spring AI MCP Integration Check ===");
        try {
            var toolCallbackProviders = applicationContext.getBeansOfType(ToolCallbackProvider.class);
            logger.info("Found {} ToolCallbackProvider beans:", toolCallbackProviders.size());
            
            toolCallbackProviders.forEach((name, provider) -> {
                logger.info("  - {} ({}): {} tool callbacks", 
                           name, 
                           provider.getClass().getSimpleName(),
                           provider.getToolCallbacks().length);
            });
        } catch (Exception e) {
            logger.warn("Error checking ToolCallbackProvider beans: {}", e.getMessage());
        }
        
        logger.info("=== End MCP Integration Check ===");
    }
}