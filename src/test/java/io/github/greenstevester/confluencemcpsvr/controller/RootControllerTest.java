package io.github.greenstevester.confluencemcpsvr.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for RootController
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "mcp.server.name=test-mcp-server",
    "mcp.server.version=1.0.0-TEST",
    "mcp.server.description=Test MCP Server",
    "server.port=8081",
    "confluence.api.username=test-user",
    "confluence.api.token=test-token"
})
class RootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void root_ShouldReturnServerInformation() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.server").value("Confluence MCP Server"))
                .andExpect(jsonPath("$.version").exists()) // Build version varies
                .andExpect(jsonPath("$.buildTime").exists())
                .andExpect(jsonPath("$.mcp.name").value("test-mcp-server"))
                .andExpect(jsonPath("$.mcp.version").value("1.0.0-TEST"))
                .andExpect(jsonPath("$.mcp.description").value("Test MCP Server"))
                .andExpect(jsonPath("$.runtime.port").value("8081"))
                .andExpect(jsonPath("$.runtime.profiles").exists())
                .andExpect(jsonPath("$.runtime.javaVersion").exists())
                .andExpect(jsonPath("$.endpoints.health").value("/actuator/health"))
                .andExpect(jsonPath("$.endpoints.mcp").value("/mcp/message"))
                .andExpect(jsonPath("$.endpoints.metrics").value("/actuator/metrics"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void root_ShouldReturnJsonContentType() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/json"));
    }
}