package io.github.greenstevester.confluence_mcp_svr.service;

import io.github.greenstevester.confluence_mcp_svr.client.ConfluenceSpacesClient;
import io.github.greenstevester.confluence_mcp_svr.config.ConfluenceProperties;
import io.github.greenstevester.confluence_mcp_svr.model.common.PaginatedResponse;
import io.github.greenstevester.confluence_mcp_svr.model.common.ResponseLinks;
import io.github.greenstevester.confluence_mcp_svr.model.common.ContentRepresentation;
import io.github.greenstevester.confluence_mcp_svr.model.enums.SpaceStatus;
import io.github.greenstevester.confluence_mcp_svr.model.enums.SpaceType;
import io.github.greenstevester.confluence_mcp_svr.model.space.Space;
import io.github.greenstevester.confluence_mcp_svr.model.space.SpaceDescription;
import io.github.greenstevester.confluence_mcp_svr.model.space.SpaceIcon;
import io.github.greenstevester.confluence_mcp_svr.util.MarkdownFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ConfluenceSpacesService using native mocks
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("dev")
@DisplayName("ConfluenceSpacesService Tests")
class ConfluenceSpacesServiceTest {

    private ConfluenceSpacesService spacesService;
    private MockConfluenceSpacesClient mockSpacesClient;
    private ConfluenceProperties mockProperties;
    private MarkdownFormatter markdownFormatter;

    @BeforeEach
    void setUp() {
        mockSpacesClient = new MockConfluenceSpacesClient();
        mockProperties = createMockProperties();
        markdownFormatter = new MarkdownFormatter();
        
        spacesService = new ConfluenceSpacesService(
            mockSpacesClient,
            mockProperties,
            markdownFormatter
        );
    }
    
    private ConfluenceProperties createMockProperties() {
        ConfluenceProperties.Api api = new ConfluenceProperties.Api(
            "http://localhost:8090",
            "test-user",
            "test-token",
            Duration.ofSeconds(30),
            20,
            3
        );
        
        ConfluenceProperties.Defaults defaults = new ConfluenceProperties.Defaults(
            25,
            "storage",
            true,
            false,
            false,
            false,
            true
        );
        
        return new ConfluenceProperties(api, defaults);
    }

    @Test
    @DisplayName("Should list spaces successfully with default parameters")
    void testListSpacesDefault() {
        // Arrange
        List<Space> expectedSpaces = createTestSpaces();
        PaginatedResponse<Space> paginatedResponse = new PaginatedResponse<>(
            expectedSpaces,
            null
        );
        mockSpacesClient.setListSpacesResponse(paginatedResponse);

        // Act
        Mono<String> result = spacesService.listSpaces(null, null, null, null, null, null);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.contains("Confluence Spaces"));
                assertTrue(response.contains("Test Space 1"));
                assertTrue(response.contains("TEST1"));
                assertTrue(response.contains("Test Space 2"));
                assertTrue(response.contains("TEST2"));
                assertFalse(response.contains("No Confluence spaces found"));
            })
            .verifyComplete();

        // Verify client was called with correct parameters
        assertEquals(25, mockSpacesClient.getLastLimit());
        assertNull(mockSpacesClient.getLastCursor());
    }

    @Test
    @DisplayName("Should list spaces with filters")
    void testListSpacesWithFilters() {
        // Arrange
        List<Space> filteredSpaces = List.of(createTestSpace("1", "FILTERED", "Filtered Space"));
        PaginatedResponse<Space> paginatedResponse = new PaginatedResponse<>(
            filteredSpaces,
            null
        );
        mockSpacesClient.setListSpacesResponse(paginatedResponse);

        List<String> ids = List.of("1", "2");
        List<String> keys = List.of("TEST1", "TEST2");
        List<SpaceType> types = List.of(SpaceType.GLOBAL);
        List<SpaceStatus> statuses = List.of(SpaceStatus.CURRENT);

        // Act
        Mono<String> result = spacesService.listSpaces(ids, keys, types, statuses, 10, "cursor123");

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.contains("Filtered Space"));
                assertTrue(response.contains("FILTERED"));
            })
            .verifyComplete();

        // Verify parameters passed to client
        assertEquals(ids, mockSpacesClient.getLastIds());
        assertEquals(keys, mockSpacesClient.getLastKeys());
        assertEquals(10, mockSpacesClient.getLastLimit());
        assertEquals("cursor123", mockSpacesClient.getLastCursor());
    }

    @Test
    @DisplayName("Should handle empty spaces list")
    void testListSpacesEmpty() {
        // Arrange
        PaginatedResponse<Space> emptyResponse = new PaginatedResponse<>(
            new ArrayList<>(),
            null
        );
        mockSpacesClient.setListSpacesResponse(emptyResponse);

        // Act
        Mono<String> result = spacesService.listSpaces(null, null, null, null, null, null);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.contains("No Confluence spaces found matching your criteria"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should include pagination info when next link is present")
    void testListSpacesWithPagination() {
        // Arrange
        List<Space> spaces = createTestSpaces();
        ResponseLinks links = new ResponseLinks("base", "/next", null, null);
        PaginatedResponse<Space> paginatedResponse = new PaginatedResponse<>(
            spaces,
            links
        );
        mockSpacesClient.setListSpacesResponse(paginatedResponse);

        // Act
        Mono<String> result = spacesService.listSpaces(null, null, null, null, null, null);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.contains("More spaces available"));
                assertTrue(response.contains("Use cursor for pagination"));
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("Should get space details successfully")
    void testGetSpaceDetails() {
        // Arrange
        Space testSpace = createDetailedTestSpace();
        mockSpacesClient.setGetSpaceResponse(testSpace);

        // Act
        Mono<String> result = spacesService.getSpace("space123");

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertTrue(response.contains("Confluence Space: Detailed Test Space"));
                assertTrue(response.contains("DETAIL"));
                assertTrue(response.contains("global"));
                assertTrue(response.contains("current"));
                assertTrue(response.contains("This is a detailed test space with comprehensive information"));
                assertTrue(response.contains("Basic Information"));
                assertTrue(response.contains("Description"));
            })
            .verifyComplete();

        // Verify correct space ID was requested
        assertEquals("space123", mockSpacesClient.getLastRequestedSpaceId());
    }

    @Test
    @DisplayName("Should handle error when listing spaces")
    void testListSpacesError() {
        // Arrange
        mockSpacesClient.setError(new RuntimeException("Connection failed"));

        // Act
        Mono<String> result = spacesService.listSpaces(null, null, null, null, null, null);

        // Assert
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    @DisplayName("Should handle error when getting space details")
    void testGetSpaceError() {
        // Arrange
        mockSpacesClient.setError(new RuntimeException("Space not found"));

        // Act
        Mono<String> result = spacesService.getSpace("invalid");

        // Assert
        StepVerifier.create(result)
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    @DisplayName("Should format space with description correctly")
    void testSpaceWithDescription() {
        // Arrange
        ContentRepresentation plainText = new ContentRepresentation("This is a test description", "plain");
        SpaceDescription description = new SpaceDescription(plainText, null);
        
        Space spaceWithDesc = new Space(
            "1",
            "TEST",
            "Test Space",
            SpaceType.GLOBAL,
            SpaceStatus.CURRENT,
            "author123",
            LocalDateTime.now(),
            "home123",
            description,
            null,
            null
        );
        
        PaginatedResponse<Space> response = new PaginatedResponse<>(
            List.of(spaceWithDesc),
            null
        );
        mockSpacesClient.setListSpacesResponse(response);

        // Act
        Mono<String> result = spacesService.listSpaces(null, null, null, null, null, null);

        // Assert
        StepVerifier.create(result)
            .assertNext(formatted -> {
                assertTrue(formatted.contains("Description"));
                assertTrue(formatted.contains("This is a test description"));
            })
            .verifyComplete();
    }

    // Helper methods to create test data
    private List<Space> createTestSpaces() {
        return List.of(
            createTestSpace("1", "TEST1", "Test Space 1"),
            createTestSpace("2", "TEST2", "Test Space 2")
        );
    }

    private Space createTestSpace(String id, String key, String name) {
        return new Space(
            id,
            key,
            name,
            SpaceType.GLOBAL,
            SpaceStatus.CURRENT,
            "author123",
            LocalDateTime.now(),
            "homepage" + id,
            null,
            null,
            null
        );
    }

    private Space createDetailedTestSpace() {
        ContentRepresentation plainText = new ContentRepresentation(
            "This is a detailed test space with comprehensive information",
            "plain"
        );
        SpaceDescription description = new SpaceDescription(plainText, null);
        ResponseLinks links = new ResponseLinks("http://base.url", null, null, null);
        
        return new Space(
            "detail123",
            "DETAIL",
            "Detailed Test Space",
            SpaceType.GLOBAL,
            SpaceStatus.CURRENT,
            "author456",
            LocalDateTime.of(2024, 1, 15, 10, 30),
            "homepage789",
            description,
            null,
            links
        );
    }

    /**
     * Native mock implementation of ConfluenceSpacesClient
     */
    private static class MockConfluenceSpacesClient extends ConfluenceSpacesClient {
        private PaginatedResponse<Space> listSpacesResponse;
        private Space getSpaceResponse;
        private RuntimeException error;
        
        // Capture parameters for verification
        private List<String> lastIds;
        private List<String> lastKeys;
        private List<String> lastTypes;
        private List<String> lastStatuses;
        private String lastCursor;
        private Integer lastLimit;
        private String lastRequestedSpaceId;

        public MockConfluenceSpacesClient() {
            super(null); // No actual WebClient needed
        }

        public void setListSpacesResponse(PaginatedResponse<Space> response) {
            this.listSpacesResponse = response;
        }

        public void setGetSpaceResponse(Space response) {
            this.getSpaceResponse = response;
        }

        public void setError(RuntimeException error) {
            this.error = error;
        }

        @Override
        public Mono<PaginatedResponse<Space>> listSpaces(List<String> ids, 
                                                        List<String> keys, 
                                                        List<String> types, 
                                                        List<String> statuses,
                                                        String cursor,
                                                        Integer limit) {
            // Capture parameters
            this.lastIds = ids;
            this.lastKeys = keys;
            this.lastTypes = types;
            this.lastStatuses = statuses;
            this.lastCursor = cursor;
            this.lastLimit = limit;

            if (error != null) {
                return Mono.error(error);
            }
            return Mono.just(listSpacesResponse);
        }

        @Override
        public Mono<Space> getSpace(String spaceId) {
            this.lastRequestedSpaceId = spaceId;
            
            if (error != null) {
                return Mono.error(error);
            }
            return Mono.just(getSpaceResponse);
        }

        // Getters for verification
        public List<String> getLastIds() { return lastIds; }
        public List<String> getLastKeys() { return lastKeys; }
        public List<String> getLastTypes() { return lastTypes; }
        public List<String> getLastStatuses() { return lastStatuses; }
        public String getLastCursor() { return lastCursor; }
        public Integer getLastLimit() { return lastLimit; }
        public String getLastRequestedSpaceId() { return lastRequestedSpaceId; }
    }

}