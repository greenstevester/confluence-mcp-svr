package io.github.greenstevester.confluencemcpsvr.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract base class for MCP integration tests.
 * Sets up an MCP test client and provides common test utilities.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractMcpIntegrationTest {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractMcpIntegrationTest.class);
    
    protected McpTestClient mcpClient;
    protected ServerInfo serverInfo;
    
    @BeforeAll
    void setUpMcpClient() {
        try {
            // Find the built JAR file
            String jarPath = findServerJar();
            logger.info("Using server JAR: {}", jarPath);
            
            // Create and initialize the test client
            mcpClient = McpTestClient.createStdioClient(jarPath);
            mcpClient.initialize();
            
            // Wait for the server to be ready
            assertTrue(mcpClient.waitForReady(30, TimeUnit.SECONDS), 
                "MCP server did not become ready within timeout");
            
            // Get server info for tests
            serverInfo = mcpClient.getServerInfo();
            assertNotNull(serverInfo, "Server info should not be null");
            
            logger.info("MCP integration test setup complete - Server: {} v{}", 
                serverInfo.name(), serverInfo.version());
            
        } catch (Exception e) {
            logger.error("Failed to set up MCP integration test", e);
            throw new RuntimeException("MCP integration test setup failed", e);
        }
    }
    
    @AfterAll
    void tearDownMcpClient() {
        if (mcpClient != null) {
            try {
                mcpClient.close();
                logger.info("MCP integration test teardown complete");
            } catch (Exception e) {
                logger.error("Error during MCP test client cleanup", e);
            }
        }
    }
    
    /**
     * Find the server JAR file in the build directory.
     */
    private String findServerJar() {
        File buildLibs = new File("build/libs");
        if (!buildLibs.exists()) {
            throw new RuntimeException("Build directory not found. Please run './gradlew build' first.");
        }
        
        File[] jarFiles = buildLibs.listFiles((dir, name) -> 
            name.endsWith(".jar") && !name.contains("plain"));
        
        if (jarFiles == null || jarFiles.length == 0) {
            throw new RuntimeException("No JAR files found in build/libs. Please run './gradlew build' first.");
        }
        
        return jarFiles[0].getAbsolutePath();
    }
    
    /**
     * Assert that a tool with the given name exists.
     */
    protected void assertToolExists(String toolName) {
        ListToolsResult tools = mcpClient.listTools();
        boolean toolExists = tools.tools().stream()
            .anyMatch(tool -> toolName.equals(tool.name()));
        assertTrue(toolExists, "Tool '" + toolName + "' should exist");
    }
    
    /**
     * Assert that a tool with the given name exists and has the expected description.
     */
    protected void assertToolExists(String toolName, String expectedDescription) {
        ListToolsResult tools = mcpClient.listTools();
        Tool tool = tools.tools().stream()
            .filter(t -> toolName.equals(t.name()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Tool '" + toolName + "' should exist"));
        
        assertEquals(expectedDescription, tool.description(), 
            "Tool '" + toolName + "' should have expected description");
    }
    
    /**
     * Assert that a resource with the given URI exists.
     */
    protected void assertResourceExists(String resourceUri) {
        ListResourcesResult resources = mcpClient.listResources();
        boolean resourceExists = resources.resources().stream()
            .anyMatch(resource -> resourceUri.equals(resource.uri()));
        assertTrue(resourceExists, "Resource '" + resourceUri + "' should exist");
    }
    
    /**
     * Assert that a prompt with the given name exists.
     */
    protected void assertPromptExists(String promptName) {
        ListPromptsResult prompts = mcpClient.listPrompts();
        boolean promptExists = prompts.prompts().stream()
            .anyMatch(prompt -> promptName.equals(prompt.name()));
        assertTrue(promptExists, "Prompt '" + promptName + "' should exist");
    }
    
    /**
     * Call a tool and assert it succeeds.
     */
    protected CallToolResult callToolAndAssertSuccess(String toolName, Map<String, Object> arguments) {
        CallToolResult result = mcpClient.callTool(toolName, arguments);
        assertNotNull(result, "Tool result should not be null");
        assertFalse(result.isError(), "Tool should not return an error: " + result.content());
        return result;
    }
    
    /**
     * Call a tool and assert it fails with an error.
     */
    protected CallToolResult callToolAndAssertError(String toolName, Map<String, Object> arguments) {
        CallToolResult result = mcpClient.callTool(toolName, arguments);
        assertNotNull(result, "Tool result should not be null");
        assertTrue(result.isError(), "Tool should return an error");
        return result;
    }
    
    /**
     * Read a resource and assert it succeeds.
     */
    protected ReadResourceResult readResourceAndAssertSuccess(String resourceUri) {
        ReadResourceResult result = mcpClient.readResource(resourceUri);
        assertNotNull(result, "Resource result should not be null");
        assertNotNull(result.contents(), "Resource contents should not be null");
        assertFalse(result.contents().isEmpty(), "Resource contents should not be empty");
        return result;
    }
    
    /**
     * Get a prompt and assert it succeeds.
     */
    protected GetPromptResult getPromptAndAssertSuccess(String promptName, Map<String, String> arguments) {
        GetPromptResult result = mcpClient.getPrompt(promptName, arguments);
        assertNotNull(result, "Prompt result should not be null");
        assertNotNull(result.messages(), "Prompt messages should not be null");
        assertFalse(result.messages().isEmpty(), "Prompt messages should not be empty");
        return result;
    }
    
    /**
     * Assert that the server has the expected capabilities.
     */
    protected void assertServerCapabilities(boolean expectsTools, boolean expectsResources, boolean expectsPrompts) {
        ServerCapabilities capabilities = serverInfo.capabilities();
        
        if (expectsTools) {
            assertNotNull(capabilities.tools(), "Server should support tools");
        }
        
        if (expectsResources) {
            assertNotNull(capabilities.resources(), "Server should support resources");
        }
        
        if (expectsPrompts) {
            assertNotNull(capabilities.prompts(), "Server should support prompts");
        }
    }
    
    /**
     * Get all available tool names.
     */
    protected List<String> getToolNames() {
        return mcpClient.listTools().tools().stream()
            .map(Tool::name)
            .toList();
    }
    
    /**
     * Get all available resource URIs.
     */
    protected List<String> getResourceUris() {
        return mcpClient.listResources().resources().stream()
            .map(Resource::uri)
            .toList();
    }
    
    /**
     * Get all available prompt names.
     */
    protected List<String> getPromptNames() {
        return mcpClient.listPrompts().prompts().stream()
            .map(Prompt::name)
            .toList();
    }
}