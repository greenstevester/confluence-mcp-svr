package io.github.greenstevester.confluencemcpsvr.config;

import io.github.greenstevester.confluencemcpsvr.mcp.AIToolCallback;
import io.github.greenstevester.confluencemcpsvr.mcp.AIToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * ToolCallbackProvider implementation that bridges our custom @AITool registry
 * with Spring AI's MCP server system
 */
@Component
public class AIToolCallbackProvider implements ToolCallbackProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(AIToolCallbackProvider.class);
    
    @Autowired
    private AIToolRegistry toolRegistry;
    
    private List<ToolCallback> toolCallbacks;
    
    @PostConstruct
    public void initializeToolCallbacks() {
        logger.info("Initializing ToolCallbackProvider with discovered @AITool methods");
        
        toolCallbacks = new ArrayList<>();
        
        // Convert each AIToolDefinition to a ToolCallback
        for (AIToolRegistry.AIToolDefinition toolDef : toolRegistry.getAllTools()) {
            AIToolCallback callback = new AIToolCallback(toolDef);
            toolCallbacks.add(callback);
            
            logger.info("Created ToolCallback for @AITool: {} - {}", 
                       toolDef.getName(), toolDef.getDescription());
        }
        
        logger.info("ToolCallbackProvider initialized with {} tool callbacks", toolCallbacks.size());
    }
    
    @Override
    public ToolCallback[] getToolCallbacks() {
        if (toolCallbacks == null) {
            logger.warn("ToolCallbacks not yet initialized, returning empty array");
            return new ToolCallback[0];
        }
        
        logger.debug("Providing {} tool callbacks to Spring AI MCP server", toolCallbacks.size());
        return toolCallbacks.toArray(new ToolCallback[0]);
    }
}