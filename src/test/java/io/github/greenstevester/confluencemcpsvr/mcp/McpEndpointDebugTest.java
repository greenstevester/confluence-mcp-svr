package io.github.greenstevester.confluencemcpsvr.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Debug test for MCP endpoint to see what's happening
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
class McpEndpointDebugTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void debugMcpEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/mcp/message")
                .header("Accept", "text/event-stream"))
                .andReturn();
                
        System.out.println("=== MCP Endpoint Debug Info ===");
        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Content-Type: " + result.getResponse().getContentType());
        System.out.println("Headers: " + result.getResponse().getHeaderNames());
        System.out.println("Content: " + result.getResponse().getContentAsString());
        System.out.println("===============================");
    }
}