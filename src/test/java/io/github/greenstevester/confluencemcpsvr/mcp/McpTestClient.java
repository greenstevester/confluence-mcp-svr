package io.github.greenstevester.confluencemcpsvr.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Test utility for creating and managing MCP clients to test the Confluence MCP server.
 * Provides methods for both STDIO and SSE transport testing.
 */
public class McpTestClient implements AutoCloseable {
    
    private static final Logger logger = LoggerFactory.getLogger(McpTestClient.class);
    
    private final McpSyncClient client;
    private final StdioClientTransport transport;
    private volatile boolean initialized = false;
    
    private McpTestClient(McpSyncClient client, StdioClientTransport transport) {
        this.client = client;
        this.transport = transport;
    }
    
    /**
     * Create a test client using STDIO transport to connect to the server JAR.
     * 
     * @param jarPath Path to the server JAR file
     * @return Configured test client
     */
    public static McpTestClient createStdioClient(String jarPath) {
        Path jar = Paths.get(jarPath);
        if (!jar.toFile().exists()) {
            throw new IllegalArgumentException("Server JAR not found: " + jarPath);
        }
        
        ServerParameters serverParams = ServerParameters.builder("java")
            .args("-Dspring.ai.mcp.server.stdio=true",
                  "-Dspring.main.web-application-type=none", 
                  "-Dspring.main.banner-mode=off",
                  "-Dlogging.pattern.console=",
                  "-jar", 
                  jar.toAbsolutePath().toString())
            .build();
        
        StdioClientTransport transport = new StdioClientTransport(serverParams);
        McpSyncClient client = McpClient.sync(transport)
            .loggingConsumer(notification -> 
                logger.info("Server log [{}]: {}", notification.level(), notification.data()))
            .build();
        
        return new McpTestClient(client, transport);
    }
    
    /**
     * Initialize the client connection and perform capability negotiation.
     * Must be called before using any other methods.
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            logger.info("Initializing MCP client...");
            client.initialize();
            client.setLoggingLevel(LoggingLevel.DEBUG);
            initialized = true;
            logger.info("MCP client initialized successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize MCP client", e);
        }
    }
    
    /**
     * Get server information and capabilities.
     */
    public ServerInfo getServerInfo() {
        ensureInitialized();
        return client.getServerInfo();
    }
    
    /**
     * List all available tools on the server.
     */
    public ListToolsResult listTools() {
        ensureInitialized();
        try {
            return client.listTools(new ListToolsRequest());
        } catch (Exception e) {
            logger.error("Failed to list tools", e);
            throw new RuntimeException("Failed to list tools", e);
        }
    }
    
    /**
     * Call a specific tool with the given arguments.
     */
    public CallToolResult callTool(String toolName, Map<String, Object> arguments) {
        ensureInitialized();
        try {
            CallToolRequest request = new CallToolRequest(toolName, arguments);
            logger.info("Calling tool '{}' with arguments: {}", toolName, arguments);
            CallToolResult result = client.callTool(request);
            logger.info("Tool '{}' completed successfully", toolName);
            return result;
        } catch (Exception e) {
            logger.error("Failed to call tool '{}'", toolName, e);
            throw new RuntimeException("Failed to call tool: " + toolName, e);
        }
    }
    
    /**
     * List all available resources on the server.
     */
    public ListResourcesResult listResources() {
        ensureInitialized();
        try {
            return client.listResources(new ListResourcesRequest());
        } catch (Exception e) {
            logger.error("Failed to list resources", e);
            throw new RuntimeException("Failed to list resources", e);
        }
    }
    
    /**
     * Read a specific resource by URI.
     */
    public ReadResourceResult readResource(String uri) {
        ensureInitialized();
        try {
            ReadResourceRequest request = new ReadResourceRequest(uri);
            logger.info("Reading resource: {}", uri);
            ReadResourceResult result = client.readResource(request);
            logger.info("Resource '{}' read successfully", uri);
            return result;
        } catch (Exception e) {
            logger.error("Failed to read resource '{}'", uri, e);
            throw new RuntimeException("Failed to read resource: " + uri, e);
        }
    }
    
    /**
     * List all available prompts on the server.
     */
    public ListPromptsResult listPrompts() {
        ensureInitialized();
        try {
            return client.listPrompts(new ListPromptsRequest());
        } catch (Exception e) {
            logger.error("Failed to list prompts", e);
            throw new RuntimeException("Failed to list prompts", e);
        }
    }
    
    /**
     * Get a prompt with the specified name and arguments.
     */
    public GetPromptResult getPrompt(String promptName, Map<String, String> arguments) {
        ensureInitialized();
        try {
            GetPromptRequest request = new GetPromptRequest(promptName, arguments);
            logger.info("Getting prompt '{}' with arguments: {}", promptName, arguments);
            GetPromptResult result = client.getPrompt(request);
            logger.info("Prompt '{}' retrieved successfully", promptName);
            return result;
        } catch (Exception e) {
            logger.error("Failed to get prompt '{}'", promptName, e);
            throw new RuntimeException("Failed to get prompt: " + promptName, e);
        }
    }
    
    /**
     * Test server connection and basic functionality.
     * Returns true if all basic operations succeed.
     */
    public boolean testConnection() {
        try {
            // Test server info
            ServerInfo info = getServerInfo();
            logger.info("Server: {} v{}", info.name(), info.version());
            
            // Test capabilities
            if (info.capabilities().tools() != null) {
                ListToolsResult tools = listTools();
                logger.info("Available tools: {}", tools.tools().size());
            }
            
            if (info.capabilities().resources() != null) {
                ListResourcesResult resources = listResources();
                logger.info("Available resources: {}", resources.resources().size());
            }
            
            if (info.capabilities().prompts() != null) {
                ListPromptsResult prompts = listPrompts();
                logger.info("Available prompts: {}", prompts.prompts().size());
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Connection test failed", e);
            return false;
        }
    }
    
    /**
     * Wait for the client to be ready with timeout.
     */
    public boolean waitForReady(long timeout, TimeUnit unit) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = unit.toMillis(timeout);
        
        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                if (testConnection()) {
                    return true;
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                // Continue waiting
            }
        }
        return false;
    }
    
    private void ensureInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Client not initialized. Call initialize() first.");
        }
    }
    
    @Override
    public void close() {
        try {
            if (client != null) {
                client.close();
            }
            if (transport != null) {
                transport.close();
            }
            logger.info("MCP test client closed");
        } catch (Exception e) {
            logger.error("Error closing MCP test client", e);
        }
    }
}