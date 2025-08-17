package io.github.greenstevester.confluence_mcp_svr.service;

import io.github.greenstevester.confluence_mcp_svr.config.ConfluenceProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Configuration validation tests for ConfluenceSpacesService
 * Validates that the service uses the actual configuration from application-dev.properties
 * without making HTTP calls (since Confluence may not be available)
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@DisplayName("ConfluenceSpacesService Configuration Tests")
class ConfluenceSpacesServiceConfigTest {

    @Autowired
    private ConfluenceSpacesService spacesService;

    @Autowired
    private ConfluenceProperties confluenceProperties;

    @Test
    @DisplayName("Should load Confluence configuration from application-dev.properties")
    void testConfigurationLoaded() {
        // Verify configuration is loaded correctly
        assertNotNull(confluenceProperties, "ConfluenceProperties should be autowired");
        assertNotNull(confluenceProperties.api(), "Confluence API config should exist");
        
        // Verify the specific values from application-dev.properties
        assertEquals("http://localhost:8090", confluenceProperties.api().baseUrl());
        assertEquals("steve", confluenceProperties.api().username());
        assertEquals("***REMOVED***", confluenceProperties.api().token());
        
        System.out.println("✓ Configuration loaded from application-dev.properties:");
        System.out.println("  Base URL: " + confluenceProperties.api().baseUrl());
        System.out.println("  Username: " + confluenceProperties.api().username());
        System.out.println("  Token: " + confluenceProperties.api().token().substring(0, 10) + "...");
    }

    @Test
    @DisplayName("Should load API connection settings correctly")
    void testApiConnectionSettings() {
        var api = confluenceProperties.api();
        
        assertEquals(30, api.timeout().getSeconds());
        assertEquals(20, api.maxConnections());
        assertEquals(3, api.retryAttempts());
        
        System.out.println("✓ API connection settings:");
        System.out.println("  Timeout: " + api.timeout());
        System.out.println("  Max connections: " + api.maxConnections());
        System.out.println("  Retry attempts: " + api.retryAttempts());
    }

    @Test
    @DisplayName("Should load default configuration correctly")
    void testDefaultConfiguration() {
        var defaults = confluenceProperties.defaults();
        
        assertEquals(25, defaults.pageSize());
        assertEquals("storage", defaults.bodyFormat());
        assertTrue(defaults.includeLabels());
        assertFalse(defaults.includeProperties());
        assertFalse(defaults.includeWebresources());
        assertFalse(defaults.includeCollaborators());
        assertTrue(defaults.includeVersion());
        
        System.out.println("✓ Default configuration:");
        System.out.println("  Page size: " + defaults.pageSize());
        System.out.println("  Body format: " + defaults.bodyFormat());
        System.out.println("  Include labels: " + defaults.includeLabels());
        System.out.println("  Include version: " + defaults.includeVersion());
    }

    @Test
    @DisplayName("Should have ConfluenceSpacesService properly wired")
    void testServiceWiring() {
        assertNotNull(spacesService, "ConfluenceSpacesService should be autowired");
        System.out.println("✓ ConfluenceSpacesService successfully instantiated and wired");
    }

    @Test
    @DisplayName("Should create service calls without executing them")
    void testServiceCallCreation() {
        // Test that the service can create Mono objects without executing HTTP calls
        var listCall = spacesService.listSpaces(null, null, null, null, null, null);
        assertNotNull(listCall, "List spaces call should return a valid Mono");
        
        var getCall = spacesService.getSpace("test-id");
        assertNotNull(getCall, "Get space call should return a valid Mono");
        
        System.out.println("✓ Service successfully creates reactive calls");
        System.out.println("  - List spaces Mono: " + listCall.getClass().getSimpleName());
        System.out.println("  - Get space Mono: " + getCall.getClass().getSimpleName());
    }

    @Test
    @DisplayName("Should validate configuration values are reasonable")
    void testConfigurationValidation() {
        var api = confluenceProperties.api();
        var defaults = confluenceProperties.defaults();
        
        // URL validation
        assertTrue(api.baseUrl().startsWith("http"), "Base URL should start with http/https");
        assertFalse(api.baseUrl().contains("your-site-name"), "Base URL should not contain placeholder");
        
        // Connection limits validation
        assertTrue(api.maxConnections() > 0 && api.maxConnections() <= 100, 
                  "Max connections should be between 1 and 100");
        assertTrue(api.retryAttempts() >= 0 && api.retryAttempts() <= 10, 
                  "Retry attempts should be between 0 and 10");
        assertTrue(api.timeout().getSeconds() > 0 && api.timeout().getSeconds() <= 300, 
                  "Timeout should be between 1 and 300 seconds");
        
        // Default values validation
        assertTrue(defaults.pageSize() > 0 && defaults.pageSize() <= 100, 
                  "Page size should be between 1 and 100");
        assertTrue("storage".equals(defaults.bodyFormat()) || "view".equals(defaults.bodyFormat()), 
                  "Body format should be 'storage' or 'view'");
        
        System.out.println("✓ All configuration values are within reasonable ranges");
    }

    @Test
    @DisplayName("Should demonstrate dev profile is active")
    void testDevProfileActive() {
        // This test validates that we're running with the dev profile
        // The fact that we got the specific dev config values proves the profile is active
        
        String baseUrl = confluenceProperties.api().baseUrl();
        String username = confluenceProperties.api().username();
        
        // These values are specific to application-dev.properties
        assertEquals("http://localhost:8090", baseUrl);
        assertEquals("steve", username);
        
        System.out.println("✓ Dev profile is active - using application-dev.properties configuration");
        System.out.println("  This confirms tests are using real configuration instead of mocks");
    }
}