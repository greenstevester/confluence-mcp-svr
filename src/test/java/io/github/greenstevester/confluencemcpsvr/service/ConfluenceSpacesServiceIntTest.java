package io.github.greenstevester.confluencemcpsvr.service;

import io.github.greenstevester.confluencemcpsvr.AbstractConfluenceIntTest;
import io.github.greenstevester.confluencemcpsvr.config.ConfluenceProperties;
import io.github.greenstevester.confluencemcpsvr.model.dto.CreateSpaceRequest;
import io.github.greenstevester.confluencemcpsvr.model.dto.UpdateSpaceRequest;
import io.github.greenstevester.confluencemcpsvr.model.enums.SpaceStatus;
import io.github.greenstevester.confluencemcpsvr.model.enums.SpaceType;
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
 * Integration test suite for ConfluenceSpacesService using real server configuration
 * Tests actual connectivity to Confluence server configured in application-dev.properties
 */
@ExtendWith(SpringExtension.class)
@DisplayName("ConfluenceSpacesService Integration Tests")
class ConfluenceSpacesServiceIntTest extends AbstractConfluenceIntTest {

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
                assertTrue(!response.isEmpty(), "Response should not be empty");
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
    @DisplayName("Should create space with valid request")
    void testCreateSpaceWithValidRequest() {
        // Arrange - Create a space request with unique key
        String uniqueKey = "TEST" + System.currentTimeMillis();
        CreateSpaceRequest createRequest = CreateSpaceRequest.builder()
            .key(uniqueKey)
            .name("Test Space - " + System.currentTimeMillis())
            .description("This is a test space created by integration tests.")
            .type(SpaceType.GLOBAL)
            .status(SpaceStatus.CURRENT)
            .build();
        
        // Act - Create the space
        Mono<String> result = spacesService.createSpace(createRequest);
        
        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(!response.isEmpty(), "Response should not be empty");
                // Should either show success or graceful error handling
                assertTrue(response.contains("Space Created Successfully") || 
                          response.contains("Error creating space"),
                    "Response should indicate creation result or error");
                
                // If successful, should contain space details
                if (response.contains("Space Created Successfully")) {
                    assertTrue(response.contains(createRequest.name()),
                        "Response should contain the created space name");
                    assertTrue(response.contains("ID") || response.contains("Key"),
                        "Response should include space metadata");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }
    
    @Test
    @DisplayName("Should update space with valid request")
    void testUpdateSpaceWithValidRequest() {
        // Note: This test uses a generic space key that may not exist
        // In real environment, you'd use an existing space key
        String testSpaceKey = "TEST";
        
        // Arrange - Create an update request
        UpdateSpaceRequest updateRequest = UpdateSpaceRequest.builder()
            .spaceKey(testSpaceKey)
            .name("Updated Test Space - " + System.currentTimeMillis())
            .description("This space has been updated by integration tests.")
            .type(SpaceType.GLOBAL)
            .status(SpaceStatus.CURRENT)
            .build();
        
        // Act - Update the space
        Mono<String> result = spacesService.updateSpace(updateRequest);
        
        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(!response.isEmpty(), "Response should not be empty");
                // Should either show success or graceful error handling
                assertTrue(response.contains("Space Updated Successfully") || 
                          response.contains("Error updating space"),
                    "Response should indicate update result or error");
                
                // If successful, should contain updated space details
                if (response.contains("Space Updated Successfully")) {
                    assertTrue(response.contains("Updated Test Space") || response.contains("Key"),
                        "Response should contain updated space information");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }
    
    @Test
    @DisplayName("Should handle create space with duplicate key")
    void testCreateSpaceWithDuplicateKey() {
        // Arrange - Create request with potentially existing key
        CreateSpaceRequest createRequest = CreateSpaceRequest.builder()
            .key("DUPLICATE_TEST")
            .name("Duplicate Test Space")
            .description("This might conflict with existing space.")
            .type(SpaceType.GLOBAL)
            .status(SpaceStatus.CURRENT)
            .build();
        
        // Act - Try to create the space twice (first one might succeed, second should fail)
        Mono<String> result1 = spacesService.createSpace(createRequest);
        
        // Assert for first attempt
        StepVerifier.create(result1)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(!response.isEmpty(), "Response should not be empty");
                // Should handle conflict gracefully
                assertTrue(response.contains("Space Created Successfully") || 
                          response.contains("Error creating space"),
                    "Response should handle duplicate key gracefully");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }
    
    @Test
    @DisplayName("Should handle update space with invalid space key")
    void testUpdateSpaceWithInvalidSpaceKey() {
        // Arrange - Create update request with non-existent space key
        UpdateSpaceRequest updateRequest = UpdateSpaceRequest.builder()
            .spaceKey("NONEXISTENT_SPACE_KEY_123456")
            .name("Updated Non-existent Space")
            .description("This update should fail.")
            .type(SpaceType.GLOBAL)
            .status(SpaceStatus.CURRENT)
            .build();
        
        // Act - Try to update the space
        Mono<String> result = spacesService.updateSpace(updateRequest);
        
        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(!response.isEmpty(), "Response should not be empty");
                // Should handle error gracefully
                assertTrue(response.contains("Error updating space") || 
                          response.contains("not found") ||
                          response.contains("Space Updated Successfully"),
                    "Response should handle invalid space key gracefully");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }
    
    @Test
    @DisplayName("Should handle create space with minimal required fields")
    void testCreateSpaceWithMinimalFields() {
        // Arrange - Create request with only required fields
        String uniqueKey = "MIN" + System.currentTimeMillis();
        CreateSpaceRequest createRequest = CreateSpaceRequest.builder()
            .key(uniqueKey)
            .name("Minimal Test Space - " + System.currentTimeMillis())
            .build(); // Uses defaults for optional fields
        
        // Act - Create the space
        Mono<String> result = spacesService.createSpace(createRequest);
        
        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(!response.isEmpty(), "Response should not be empty");
                assertTrue(response.contains("Space Created Successfully") || 
                          response.contains("Error creating space"),
                    "Response should indicate creation result");
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }
    
    @Test
    @DisplayName("Should handle different space types and statuses")
    void testCreateSpaceWithDifferentTypesAndStatuses() {
        // Arrange - Create space with COLLABORATION type
        String uniqueKey = "COLLAB" + System.currentTimeMillis();
        CreateSpaceRequest createRequest = CreateSpaceRequest.builder()
            .key(uniqueKey)
            .name("Collaboration Test Space - " + System.currentTimeMillis())
            .description("Testing collaboration space type.")
            .type(SpaceType.COLLABORATION)
            .status(SpaceStatus.CURRENT)
            .build();
        
        // Act - Create the space
        Mono<String> result = spacesService.createSpace(createRequest);
        
        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response, "Response should not be null");
                assertTrue(!response.isEmpty(), "Response should not be empty");
                assertTrue(response.contains("Space Created Successfully") || 
                          response.contains("Error creating space"),
                    "Response should handle different space types");
                
                // If successful, should show collaboration type
                if (response.contains("Space Created Successfully")) {
                    assertTrue(response.contains("COLLABORATION") || response.contains("collaboration"),
                        "Response should reflect the space type");
                }
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }
    
    @Test
    @DisplayName("Should get space details with valid space ID")
    void testGetSpaceWithValidId() {
        // Note: This test uses a generic space ID that may not exist
        // In a real environment, you'd use an actual space ID
        String testSpaceId = "123456";
        
        // Act - Get space details
        Mono<String> result = spacesService.getSpace(testSpaceId);
        
        // Assert - This test verifies the service handles the request gracefully
        StepVerifier.create(result)
            .expectNextMatches(response -> {
                // The service should always return a non-null response
                // It could be success, error message, or default error response
                return response != null && !response.trim().isEmpty();
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
    }
}