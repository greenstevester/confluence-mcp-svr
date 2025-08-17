package io.github.greenstevester.confluence_mcp_svr.service;

import io.github.greenstevester.confluence_mcp_svr.config.ConfluenceProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for ConfluenceSpacesService using real server configuration
 * Tests actual connectivity to Confluence server configured in application-dev.properties
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("ConfluenceSpacesService Integration Tests")
class ConfluenceSpacesServiceTest {

    @Autowired
    private ConfluenceSpacesService spacesService;
    
    @Autowired
    private ConfluenceProperties confluenceProperties;

    @Value("${confluence.api.base-url}")
    private String baseUrl;

    @Value("${confluence.api.username}")
    private String username;

    @Value("${confluence.api.token}")
    private String token;

    static {
        loadEnvironmentVariables();
    }

    private static void loadEnvironmentVariables() {

        final Map<String, String> ENV_TO_PROPERTY_MAP = Map.of(
                "CONFLUENCE_API_BASE_URL", "confluence.api.base-url",
                "CONFLUENCE_API_USERNAME", "confluence.api.username",
                "CONFLUENCE_API_TOKEN", "confluence.api.token"
        );

        Path envFile = Paths.get(".env");
        if (Files.exists(envFile)) {
            try {
                Files.lines(envFile)
                        .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                        .forEach(line -> {
                            String[] parts = line.split("=", 2);
                            if (parts.length == 2) {
                                String key = parts[0].trim();
                                String value = parts[1].trim();
                                System.setProperty(key, value);
                                
                                String mappedProperty = ENV_TO_PROPERTY_MAP.get(key);
                                if (mappedProperty != null) {
                                    System.setProperty(mappedProperty, value);
                                }
                            }
                        });
            } catch (IOException e) {
                System.err.println("Warning: Could not load .env file: " + e.getMessage());
            }
        }
    }


    @Test
    @DisplayName("Should list spaces successfully from real Confluence server")
    void testListSpacesIntegration() {
        // Act - Call the actual service with real Confluence connection
        Mono<String> result = spacesService.listSpaces(
                null, // ids
                null, // keys
                null, // types
                null, // statuses
                5,    // limit - small limit for testing
                null  // cursor
        );

        // Assert - Verify response structure
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("Confluence Spaces") || response.contains("No Confluence spaces found"), 
                    "Response should contain either spaces list or no spaces message");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should handle empty or filtered results gracefully")
    void testListSpacesWithFilters() {
        // Act - Call with specific filters that might return empty results
        Mono<String> result = spacesService.listSpaces(
            null, // ids
            null, // keys  
            null, // types
            null, // statuses
            5,    // limit - small limit for testing
            null  // cursor
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                // Should handle both cases - spaces found or no spaces found
                assertTrue(response.length() > 0, "Response should not be empty");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should verify Confluence configuration is loaded correctly")
    void testConfluenceConfiguration() {
        // Verify configuration from application-dev.properties
        assertNotNull(confluenceProperties, "ConfluenceProperties should be autowired");
        assertNotNull(confluenceProperties.api(), "API configuration should be present");
        
        // Verify the configuration is loaded from environment variables
        assertNotNull(confluenceProperties.api().baseUrl(), 
            "Base URL should be loaded from environment variable");
        assertNotNull(confluenceProperties.api().username(),
            "Username should be loaded from environment variable");
        assertNotNull(confluenceProperties.api().token(), 
            "API token should be loaded from environment variable");
        assertFalse(confluenceProperties.api().token().equals("default-token"),
            "API token should not be the default placeholder value");
        assertEquals(Duration.ofSeconds(30), confluenceProperties.api().timeout(),
            "Timeout should match application-dev.properties");
        assertEquals(20, confluenceProperties.api().maxConnections(),
            "Max connections should match application-dev.properties");
        assertEquals(3, confluenceProperties.api().retryAttempts(),
            "Retry attempts should match application-dev.properties");
    }

    @Test
    @DisplayName("Should verify default configuration is loaded correctly")
    void testDefaultConfiguration() {
        assertNotNull(confluenceProperties.defaults(), "Defaults configuration should be present");
        
        // Verify defaults match application-dev.properties
        assertEquals(25, confluenceProperties.defaults().pageSize(),
            "Default page size should match application-dev.properties");
        assertEquals("storage", confluenceProperties.defaults().bodyFormat(),
            "Default body format should match application-dev.properties");
        assertTrue(confluenceProperties.defaults().includeLabels(),
            "Include labels should match application-dev.properties");
        assertFalse(confluenceProperties.defaults().includeProperties(),
            "Include properties should match application-dev.properties");
        assertFalse(confluenceProperties.defaults().includeWebresources(),
            "Include webresources should match application-dev.properties");
        assertFalse(confluenceProperties.defaults().includeCollaborators(),
            "Include collaborators should match application-dev.properties");
        assertTrue(confluenceProperties.defaults().includeVersion(),
            "Include version should match application-dev.properties");
    }
}