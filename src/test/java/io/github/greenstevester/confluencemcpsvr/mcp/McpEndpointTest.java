package io.github.greenstevester.confluencemcpsvr.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        // The MCP endpoint should return SSE content, not a 404 whitelabel error
        mockMvc.perform(get("/mcp/message")
                .header("Accept", "text/event-stream"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/event-stream;charset=UTF-8"));
    }

    @Test  
    void mcpMessage_WithoutSSEHeader_ShouldStillNotBe404() throws Exception {
        // Even without SSE header, should not return 404 or 500
        mockMvc.perform(get("/mcp/message"))
                .andExpect(result -> {
                    int statusCode = result.getResponse().getStatus();
                    if (statusCode == 404 || statusCode == 500) {
                        throw new AssertionError("Expected non-404/500 status but got: " + statusCode);
                    }
                });
    }
}