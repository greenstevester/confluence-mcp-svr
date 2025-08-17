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
    @DisplayName("Should validate configuration values are reasonable")
    void testConfigurationValidation() {
        var api = confluenceProperties.api();
        var defaults = confluenceProperties.defaults();

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

}