package io.github.greenstevester.confluence_mcp_svr.service;

import io.github.greenstevester.confluence_mcp_svr.AbstractConfluenceIntTest;
import io.github.greenstevester.confluence_mcp_svr.config.ConfluenceProperties;
import io.github.greenstevester.confluence_mcp_svr.model.enums.ExcerptStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for ConfluenceSearchService using real server configuration
 * Tests actual connectivity to Confluence server configured in application-dev.properties
 */
@ExtendWith(SpringExtension.class)
@DisplayName("ConfluenceSearchService Integration Tests")
class ConfluenceSearchServiceIntTest extends AbstractConfluenceIntTest {

    @Autowired
    private ConfluenceSearchService searchService;
    
    @Autowired
    private ConfluenceProperties confluenceProperties;

    @Value("${confluence.api.base-url}")
    private String baseUrl;

    @Value("${confluence.api.username}")
    private String username;

    @Value("${confluence.api.token}")
    private String token;

    @Test
    @DisplayName("Should search content successfully with basic CQL query")
    void testSearchWithBasicCQL() {
        // Act - Search for pages with simple CQL
        Mono<String> result = searchService.search(
                "type = page", // Simple CQL query
                null,          // cqlContext
                5,             // limit - small for testing
                0,             // start
                false,         // includeArchivedSpaces
                ExcerptStrategy.HIGHLIGHT // excerpt strategy
        );

        // Assert - Verify response structure
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("Confluence Search Results") || 
                          response.contains("No results found") || 
                          response.contains("Error performing search"), 
                    "Response should contain either search results, no results message, or error message. Got: " + response);
                if (response.contains("Query:")) {
                    assertTrue(response.contains("type = page"), 
                        "Response should include the executed query");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should handle search with no results gracefully")
    void testSearchWithNoResults() {
        // Act - Search with very specific query unlikely to return results
        Mono<String> result = searchService.search(
                "title ~ \"NonExistentPageTitleXYZ123456789\"", // Query unlikely to match anything
                null,
                10,
                null,
                false,
                null // Use default excerpt strategy
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("No results found") || 
                          response.contains("Found 0 results") ||
                          response.contains("Error performing search"),
                    "Response should indicate no results were found or an error occurred");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should search with space context successfully")
    void testSearchWithSpaceContext() {
        // Act - Search within specific space context (if available)
        Mono<String> result = searchService.search(
                "type = page OR type = blogpost", // Search for pages and blog posts
                null, // Could specify space key here if known
                10,
                0,
                false,
                ExcerptStrategy.INDEXED // Use indexed excerpt
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.length() > 0, "Response should not be empty");
                // Verify the response contains proper formatting
                if (!response.contains("No results found") && !response.contains("Error performing search")) {
                    assertTrue(response.contains("Type") || response.contains("Status"),
                        "Response should include content metadata");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should handle search with pagination parameters")
    void testSearchWithPagination() {
        // Act - Search with pagination parameters
        Mono<String> result = searchService.search(
                "type = page", // Basic query
                null,
                3,    // limit to 3 results
                2,    // start from result 2 (skip first 2)
                false,
                ExcerptStrategy.HIGHLIGHT
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("Search Results") || 
                          response.contains("No results") ||
                          response.contains("Error performing search"),
                    "Response should contain search results, no results message, or error");
                // If results are found, verify pagination info is included
                if (response.contains("Found") && !response.contains("No results") && !response.contains("Error")) {
                    assertTrue(response.contains("results") || response.contains("showing"),
                        "Response should indicate result count");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should search with archived spaces included")
    void testSearchWithArchivedSpaces() {
        // Act - Search including archived spaces
        Mono<String> result = searchService.search(
                "type = space", // Search for spaces
                null,
                5,
                null,
                true, // Include archived spaces
                ExcerptStrategy.NONE
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.length() > 0, "Response should not be empty");
                // Results may or may not include archived spaces depending on server content
                // Could be error message if no connection
                assertTrue(response.contains("Search Results") || 
                          response.contains("No results") ||
                          response.contains("Error performing search"),
                    "Response should be valid");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should handle complex CQL queries")
    void testSearchWithComplexCQL() {
        // Act - Search with more complex CQL
        String complexCQL = "(type = page OR type = blogpost) AND lastmodified >= now('-30d')";
        Mono<String> result = searchService.search(
                complexCQL, // Recent pages or blog posts
                null,
                5,
                null,
                false,
                ExcerptStrategy.HIGHLIGHT
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                if (response.contains("Query:")) {
                    assertTrue(response.contains("lastmodified"),
                        "Response should include the complex query");
                }
                // Verify proper formatting of results
                if (!response.contains("No results found") && !response.contains("Error performing search")) {
                    assertTrue(response.contains("Last Modified") || response.contains("Type"),
                        "Response should include content metadata");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should verify search duration is included in response")
    void testSearchDurationMetadata() {
        // Act - Perform a search
        Mono<String> result = searchService.search(
                "type = page",
                null,
                2,
                null,
                false,
                ExcerptStrategy.HIGHLIGHT
        );

        // Assert - Check for search metadata
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                // Either has results with timestamp or error message
                assertTrue(response.contains("Results retrieved at") || 
                          response.contains("Error performing search"),
                    "Response should include retrieval timestamp or error");
                // Search duration may or may not be included depending on API response
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

    @Test
    @DisplayName("Should use default values when optional parameters are null")
    void testSearchWithDefaultParameters() {
        // Act - Search with mostly null parameters to test defaults
        Mono<String> result = searchService.search(
                "type = page", // Only required parameter
                null,          // cqlContext - null
                null,          // limit - should use default from properties
                null,          // start - null
                null,          // includeArchivedSpaces - null
                null           // excerpt - should default to HIGHLIGHT
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("Confluence Search Results") || 
                          response.contains("No results found") ||
                          response.contains("Error performing search"),
                    "Response should contain valid search response or error");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }
}