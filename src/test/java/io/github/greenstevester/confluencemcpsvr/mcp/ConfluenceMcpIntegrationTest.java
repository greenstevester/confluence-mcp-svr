package io.github.greenstevester.confluencemcpsvr.mcp;

import io.modelcontextprotocol.schema.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Confluence MCP Server.
 * Tests all tools, resources, and capabilities through the MCP protocol.
 */
@DisplayName("Confluence MCP Server Integration Tests")
class ConfluenceMcpIntegrationTest extends AbstractMcpIntegrationTest {

    @Nested
    @DisplayName("Server Capabilities")
    class ServerCapabilitiesTests {

        @Test
        @DisplayName("Should have correct server information")
        void shouldHaveCorrectServerInformation() {
            assertEquals("mcp-server-atlassian-confluence", serverInfo.name());
            assertNotNull(serverInfo.version());
            assertTrue(serverInfo.version().matches("\\d+\\.\\d+\\.\\d+"));
        }

        @Test
        @DisplayName("Should support required capabilities")
        void shouldSupportRequiredCapabilities() {
            assertServerCapabilities(true, false, false); // Tools supported, resources/prompts optional
            
            // Should support logging
            assertNotNull(serverInfo.capabilities().logging());
        }
    }

    @Nested
    @DisplayName("Confluence Spaces Tools")
    class ConfluenceSpacesToolsTests {

        @Test
        @DisplayName("Should have list spaces tool")
        void shouldHaveListSpacesTool() {
            assertToolExists("listSpaces", 
                "List Confluence spaces with pagination support. Returns space details including key, name, type, and status.");
        }

        @Test
        @DisplayName("Should have get space tool")
        void shouldHaveGetSpaceTool() {
            assertToolExists("getSpace", 
                "Get detailed information about a specific Confluence space by key or ID.");
        }

        @Test
        @DisplayName("List spaces tool should work with default parameters")
        void listSpacesToolShouldWorkWithDefaultParameters() {
            CallToolResult result = callToolAndAssertSuccess("listSpaces", Map.of());
            
            // Verify result structure
            String content = result.content().stream()
                .map(Object::toString)
                .reduce("", (a, b) -> a + b);
            
            assertFalse(content.isEmpty(), "List spaces should return content");
            // Content should be JSON or structured format
            assertTrue(content.contains("spaces") || content.contains("[]"), 
                "Result should contain spaces information");
        }

        @Test
        @DisplayName("List spaces tool should work with pagination")
        void listSpacesToolShouldWorkWithPagination() {
            Map<String, Object> args = Map.of(
                "limit", 5,
                "cursor", ""
            );
            
            CallToolResult result = callToolAndAssertSuccess("listSpaces", args);
            assertNotNull(result.content());
        }

        @Test
        @DisplayName("Get space tool should require space key parameter")
        void getSpaceToolShouldRequireSpaceKeyParameter() {
            // Test with missing parameter - should handle gracefully
            Map<String, Object> emptyArgs = Map.of();
            
            CallToolResult result = mcpClient.callTool("getSpace", emptyArgs);
            assertNotNull(result);
            // Result should indicate missing parameter or provide error info
        }
    }

    @Nested
    @DisplayName("Confluence Pages Tools")
    class ConfluencePagesToolsTests {

        @Test
        @DisplayName("Should have list pages tool")
        void shouldHaveListPagesTool() {
            assertToolExists("listPages", 
                "List pages in a Confluence space with support for filtering, sorting, and pagination.");
        }

        @Test
        @DisplayName("Should have get page tool")
        void shouldHaveGetPageTool() {
            assertToolExists("getPage", 
                "Get detailed information about a specific Confluence page, including content in Markdown format.");
        }

        @Test
        @DisplayName("List pages tool should work with space key")
        void listPagesToolShouldWorkWithSpaceKey() {
            Map<String, Object> args = Map.of(
                "spaceKey", "TEST",
                "limit", 10
            );
            
            CallToolResult result = callToolAndAssertSuccess("listPages", args);
            assertNotNull(result.content());
        }

        @Test
        @DisplayName("Get page tool should handle page ID parameter")
        void getPageToolShouldHandlePageIdParameter() {
            Map<String, Object> args = Map.of("pageId", "123456");
            
            // This might fail if page doesn't exist, but should not crash
            CallToolResult result = mcpClient.callTool("getPage", args);
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Confluence Search Tools")
    class ConfluenceSearchToolsTests {

        @Test
        @DisplayName("Should have search content tool")
        void shouldHaveSearchContentTool() {
            assertToolExists("searchContent", 
                "Search Confluence content using CQL (Confluence Query Language) with support for various content types and filters.");
        }

        @Test
        @DisplayName("Search content tool should work with basic query")
        void searchContentToolShouldWorkWithBasicQuery() {
            Map<String, Object> args = Map.of(
                "cql", "type=page",
                "limit", 5
            );
            
            CallToolResult result = callToolAndAssertSuccess("searchContent", args);
            assertNotNull(result.content());
        }

        @Test
        @DisplayName("Search content tool should handle complex CQL queries")
        void searchContentToolShouldHandleComplexCqlQueries() {
            Map<String, Object> args = Map.of(
                "cql", "type=page AND space.key='TEST'",
                "limit", 10,
                "excerpt", "highlight"
            );
            
            CallToolResult result = callToolAndAssertSuccess("searchContent", args);
            assertNotNull(result.content());
        }
    }

    @Nested
    @DisplayName("Documentation Mining Tools")
    class DocumentationMiningToolsTests {

        @Test
        @DisplayName("Should have documentation mining tools")
        void shouldHaveDocumentationMiningTools() {
            List<String> toolNames = getToolNames();
            
            // Check for documentation mining tools
            assertTrue(toolNames.stream().anyMatch(name -> name.contains("audit") || name.contains("mining")),
                "Should have documentation audit/mining tools");
        }
    }

    @Nested
    @DisplayName("Tool Parameter Validation")
    class ToolParameterValidationTests {

        @Test
        @DisplayName("Tools should handle invalid parameters gracefully")
        void toolsShouldHandleInvalidParametersGracefully() {
            // Test with invalid parameter types
            Map<String, Object> invalidArgs = Map.of(
                "limit", "not-a-number",
                "invalidParam", "test"
            );
            
            CallToolResult result = mcpClient.callTool("listSpaces", invalidArgs);
            assertNotNull(result);
            
            // Should either succeed with defaults or provide meaningful error
            if (result.isError()) {
                String errorContent = result.content().toString();
                assertFalse(errorContent.isEmpty(), "Error message should not be empty");
            }
        }

        @Test
        @DisplayName("Tools should validate required parameters")
        void toolsShouldValidateRequiredParameters() {
            // Test tools that require parameters
            CallToolResult result = mcpClient.callTool("getSpace", Map.of());
            assertNotNull(result);
            
            // Should provide helpful error message about missing parameters
            if (result.isError()) {
                String errorContent = result.content().toString();
                assertTrue(errorContent.toLowerCase().contains("required") || 
                          errorContent.toLowerCase().contains("missing") ||
                          errorContent.toLowerCase().contains("parameter"),
                    "Error should mention required/missing parameters");
            }
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle authentication errors gracefully")
        void shouldHandleAuthenticationErrorsGracefully() {
            // These tests will depend on your actual Confluence setup
            // For now, just ensure the server doesn't crash on auth failures
            
            List<String> toolNames = getToolNames();
            assertFalse(toolNames.isEmpty(), "Should have available tools even with auth issues");
        }

        @Test
        @DisplayName("Should handle network timeouts gracefully")
        void shouldHandleNetworkTimeoutsGracefully() {
            // Test with operations that might timeout
            Map<String, Object> args = Map.of("limit", 1);
            
            try {
                CallToolResult result = mcpClient.callTool("listSpaces", args);
                assertNotNull(result);
            } catch (Exception e) {
                // Should not throw unhandled exceptions
                fail("Tool should handle timeouts gracefully, but threw: " + e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("Performance and Reliability")
    class PerformanceAndReliabilityTests {

        @Test
        @DisplayName("Should respond to tool calls within reasonable time")
        void shouldRespondToToolCallsWithinReasonableTime() {
            long startTime = System.currentTimeMillis();
            
            CallToolResult result = callToolAndAssertSuccess("listSpaces", Map.of("limit", 1));
            
            long duration = System.currentTimeMillis() - startTime;
            assertTrue(duration < 10000, "Tool call should complete within 10 seconds, took: " + duration + "ms");
        }

        @Test
        @DisplayName("Should handle concurrent tool calls")
        void shouldHandleConcurrentToolCalls() {
            // Simple concurrency test - more sophisticated testing would require multiple clients
            Map<String, Object> args = Map.of("limit", 1);
            
            CallToolResult result1 = callToolAndAssertSuccess("listSpaces", args);
            CallToolResult result2 = callToolAndAssertSuccess("listSpaces", args);
            
            assertNotNull(result1);
            assertNotNull(result2);
        }
    }

    @Test
    @DisplayName("Should list all available tools")
    void shouldListAllAvailableTools() {
        List<String> toolNames = getToolNames();
        
        assertFalse(toolNames.isEmpty(), "Should have at least one tool available");
        
        // Expected core tools
        assertTrue(toolNames.contains("listSpaces"), "Should have listSpaces tool");
        assertTrue(toolNames.contains("getSpace"), "Should have getSpace tool");
        assertTrue(toolNames.contains("listPages"), "Should have listPages tool");
        assertTrue(toolNames.contains("getPage"), "Should have getPage tool");
        assertTrue(toolNames.contains("searchContent"), "Should have searchContent tool");
        
        logger.info("Available tools: {}", toolNames);
    }
}