package io.github.greenstevester.confluencemcpsvr.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Integration test for MCP endpoint
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.ai.mcp.server.enabled=true",
    "spring.ai.mcp.server.transport=WEBMVC",
    "spring.ai.mcp.server.sse-message-endpoint=/mcp/message",
    "mcp.server.name=test-mcp-server",
    "mcp.server.version=1.0.0-TEST",
    "mcp.server.description=Test MCP Server",
    "confluence.api.username=test-user",
    "confluence.api.token=test-token"
})
class McpEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void mcpMessage_ShouldNotReturnWhitelabelError() throws Exception {
        // The MCP endpoint should exist and not return 404 whitelabel error
        // MCP uses POST requests, not GET
        String jsonPayload = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{}}";
        
        mockMvc.perform(post("/mcp/message")
                .contentType(APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    // Should not be 404 (endpoint exists) or 500 (server error)
                    // 400 is expected due to session handling
                    if (statusCode == 404 || statusCode == 500) {
                        throw new AssertionError("Expected non-404/500 status but got: " + statusCode + 
                            " - Response: " + result.getResponse().getContentAsString());
                    }
                });
    }

    @Test  
    void mcpMessage_WithoutSSEHeader_ShouldStillNotBe404() throws Exception {
        // Test that the endpoint exists and responds (not 404)
        String jsonPayload = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\",\"params\":{}}";
        
        mockMvc.perform(post("/mcp/message")
                .contentType(APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    // Should not be 404 (endpoint not found)
                    if (statusCode == 404) {
                        throw new AssertionError("MCP endpoint should exist but got 404 - Response: " + 
                            result.getResponse().getContentAsString());
                    }
                });
    }
}