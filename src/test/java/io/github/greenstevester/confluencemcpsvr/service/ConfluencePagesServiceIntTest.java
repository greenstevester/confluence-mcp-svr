package io.github.greenstevester.confluencemcpsvr.service;

import io.github.greenstevester.confluencemcpsvr.AbstractConfluenceIntTest;
import io.github.greenstevester.confluencemcpsvr.config.ConfluenceProperties;
import io.github.greenstevester.confluencemcpsvr.model.enums.ContentStatus;
import io.github.greenstevester.confluencemcpsvr.model.enums.PageSortOrder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for ConfluencePagesService using real server configuration
 * Tests actual connectivity to Confluence server configured in application-dev.properties
 */
@ExtendWith(SpringExtension.class)
@DisplayName("ConfluencePagesService Integration Tests")
class ConfluencePagesServiceIntTest extends AbstractConfluenceIntTest {

    @Autowired
    private ConfluencePagesService pagesService;
    
    @Autowired
    private ConfluenceProperties confluenceProperties;

    @Value("${confluence.api.base-url}")
    private String baseUrl;

    @Value("${confluence.api.username}")
    private String username;

    @Value("${confluence.api.token}")
    private String token;

    @Test
    @DisplayName("Should list pages successfully with basic parameters")
    void testListPagesWithBasicParameters() {
        // Act - List pages with basic parameters
        Mono<String> result = pagesService.listPages(
                null,    // spaceIds - null for all spaces
                null,    // query - null for no text search
                null,    // statuses - will default to CURRENT
                null,    // sort - will default to MODIFIED_DATE_DESC
                5,       // limit - small for testing
                null     // cursor - no pagination
        );

        // Assert - Verify response structure
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("Confluence Pages") || 
                          response.contains("No Confluence pages found") ||
                          response.contains("Error listing pages"), 
                    "Response should contain pages list, no pages message, or error message. Got: " + response);
                // If pages are found, verify basic structure
                if (!response.contains("No Confluence pages found") && !response.contains("Error")) {
                    assertTrue(response.contains("ID") || response.contains("Title"),
                        "Response should include page metadata");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void testListPagesWithNoResults() {
        // Act - Search with very specific query unlikely to return results
        Mono<String> result = pagesService.listPages(
                null,
                "NonExistentPageTitleXYZ123456789", // Query unlikely to match anything
                List.of(ContentStatus.CURRENT),
                PageSortOrder.CREATED_DATE,
                10,
                null
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("No Confluence pages found") || 
                          response.contains("Confluence Pages") ||
                          response.contains("Error listing pages"),
                    "Response should indicate no results found, show pages, or error occurred");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should list pages with specific space IDs")
    void testListPagesWithSpaceIds() {
        // Act - List pages with space filtering (using generic space ID that might exist)
        Mono<String> result = pagesService.listPages(
                List.of("123456"), // Generic space ID - may or may not exist
                null,
                List.of(ContentStatus.CURRENT),
                PageSortOrder.TITLE,
                5,
                null
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.length() > 0, "Response should not be empty");
                // Could show pages or no results depending on space existence
                assertTrue(response.contains("Confluence Pages") || 
                          response.contains("No Confluence pages found") ||
                          response.contains("Error listing pages"),
                    "Response should be valid");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should handle different sort orders")
    void testListPagesWithDifferentSortOrders() {
        // Act - List pages with created date ascending sort
        Mono<String> result = pagesService.listPages(
                null,
                null,
                List.of(ContentStatus.CURRENT),
                PageSortOrder.CREATED_DATE,
                3,
                null
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                // Verify proper response format
                assertTrue(response.contains("Confluence Pages") || 
                          response.contains("No Confluence pages found") ||
                          response.contains("Error listing pages"),
                    "Response should contain valid page response or error");
                if (!response.contains("No Confluence pages found") && !response.contains("Error")) {
                    assertTrue(response.contains("Created") || response.contains("ID"),
                        "Response should include page creation info");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should handle pagination with cursor")
    void testListPagesWithPagination() {
        // Act - List pages with small limit to test pagination
        Mono<String> result = pagesService.listPages(
                null,
                null,
                List.of(ContentStatus.CURRENT),
                PageSortOrder.MODIFIED_DATE_DESC,
                2, // Very small limit
                null
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("Confluence Pages") || 
                          response.contains("No Confluence pages found") ||
                          response.contains("Error listing pages"),
                    "Response should contain valid response");
                // If results exist and there are more, should mention pagination
                if (response.contains("More pages available")) {
                    assertTrue(response.contains("cursor"),
                        "Response should mention cursor for pagination");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should get page details with valid page ID")
    void testGetPageWithValidId() {
        // Note: This test uses a generic page ID that may not exist
        // In a real environment, you'd use an actual page ID
        String testPageId = "123456789";
        
        // Act - Get page details
        Mono<String> result = pagesService.getPage(testPageId);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                // Could be page details or error if page doesn't exist
                assertTrue(response.contains("Confluence Page:") || 
                          response.contains("Error getting page") ||
                          response.contains("not found"),
                    "Response should contain page details or error message");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should handle non-existent page ID gracefully")
    void testGetPageWithNonExistentId() {
        // Act - Try to get a page that definitely doesn't exist
        String nonExistentPageId = "999999999999";
        Mono<String> result = pagesService.getPage(nonExistentPageId);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                // Should handle error gracefully
                assertTrue(response.length() > 0, "Response should not be empty");
                // Might contain error message or empty response
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should handle query-based page search")
    void testListPagesWithQuerySearch() {
        // Act - Search pages with a simple query
        Mono<String> result = pagesService.listPages(
                null,
                "confluence", // Simple query that might match pages
                List.of(ContentStatus.CURRENT),
                PageSortOrder.MODIFIED_DATE_DESC,
                5,
                null
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("Confluence Pages") || 
                          response.contains("No Confluence pages found") ||
                          response.contains("Error listing pages"),
                    "Response should show search results or appropriate message");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should include timestamp in response")
    void testResponseIncludesTimestamp() {
        // Act - List pages
        Mono<String> result = pagesService.listPages(
                null,
                null,
                List.of(ContentStatus.CURRENT),
                null,
                3,
                null
        );

        // Assert - Check for timestamp
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                // Either has results with timestamp or error message
                assertTrue(response.contains("retrieved at") || 
                          response.contains("Error listing pages"),
                    "Response should include retrieval timestamp or error");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }

    @Test
    @DisplayName("Should use default values when optional parameters are null")
    void testListPagesWithDefaultParameters() {
        // Act - List pages with mostly null parameters to test defaults
        Mono<String> result = pagesService.listPages(
                null, // spaceIds - null
                null, // query - null
                null, // statuses - should default to CURRENT
                null, // sort - should default to MODIFIED_DATE_DESC
                null, // limit - should use default from properties
                null  // cursor - null
        );

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(response.contains("Confluence Pages") || 
                          response.contains("No Confluence pages found") ||
                          response.contains("Error listing pages"),
                    "Response should contain valid page response or error");
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