# MCP Test Harness

This directory contains a comprehensive test harness for the Confluence MCP Server, following canonical MCP testing patterns and best practices.

## Overview

The MCP test harness provides multiple testing approaches:

1. **Manual Interactive Testing** - Using MCP Inspector
2. **Automated Integration Testing** - Using Java MCP client
3. **Performance Testing** - Load and response time testing
4. **Protocol Compliance Testing** - MCP specification compliance

## Directory Structure

```
test-harness/
├── scripts/
│   ├── test-with-inspector.sh      # Manual testing with MCP Inspector
│   └── run-integration-tests.sh    # Automated integration tests
├── config/
│   └── (test configurations)
└── docs/
    └── MCP-TEST-HARNESS.md         # This file
```

## Test Types

### 1. Manual Interactive Testing

Use the MCP Inspector for interactive testing and debugging:

```bash
# Test with STDIO transport (recommended)
./test-harness/scripts/test-with-inspector.sh

# Select option 1 for STDIO mode
```

The MCP Inspector provides:
- **Server Connection Pane** - Transport configuration and connection testing
- **Tools Tab** - Interactive tool testing with custom inputs
- **Resources Tab** - Resource listing and content inspection (if supported)
- **Prompts Tab** - Prompt template testing (if supported)  
- **Notifications Pane** - Server logs and notifications

#### Inspector Workflow
1. Start Inspector with your server
2. Verify basic connectivity and capability negotiation
3. Test each tool with various inputs
4. Monitor server logs and error responses
5. Test edge cases and error conditions

### 2. Automated Integration Testing

Comprehensive automated tests covering all MCP functionality:

```bash
# Run all MCP integration tests
./test-harness/scripts/run-integration-tests.sh

# Run with options
./test-harness/scripts/run-integration-tests.sh --verbose
./test-harness/scripts/run-integration-tests.sh --pattern "ConfluenceSpaces"
./test-harness/scripts/run-integration-tests.sh --skip-build
```

#### Test Categories

**Server Capabilities Tests**
- Server information validation
- Capability negotiation testing
- Protocol version compatibility

**Confluence Spaces Tools Tests**
- `listSpaces` tool functionality
- `getSpace` tool functionality
- Parameter validation
- Pagination testing

**Confluence Pages Tools Tests**  
- `listPages` tool functionality
- `getPage` tool functionality
- Content format validation (Markdown)
- Filtering and sorting

**Confluence Search Tools Tests**
- `searchContent` tool functionality
- CQL query validation
- Search result formatting
- Excerpt generation

**Error Handling Tests**
- Authentication error handling
- Network timeout handling
- Invalid parameter handling
- Graceful degradation

**Performance Tests**
- Response time validation
- Concurrent request handling
- Resource usage monitoring

### 3. Test Configuration

#### Test Profiles

The test harness uses Spring profiles for different test scenarios:

- **`test`** - Unit tests with mocked dependencies
- **`integration`** - Integration tests with real MCP protocol
- **`dev`** - Development testing with local Confluence instance

#### Environment Variables

For integration testing with a real Confluence instance:

```bash
# Create .env file with your Confluence credentials
CONFLUENCE_API_BASE_URL=https://your-site.atlassian.net
CONFLUENCE_API_USERNAME=your-email@example.com
CONFLUENCE_API_TOKEN=your-api-token-here
```

## Test Utilities

### McpTestClient

Java utility class for creating MCP clients in tests:

```java
// Create STDIO client
McpTestClient client = McpTestClient.createStdioClient("path/to/server.jar");
client.initialize();

// Test server capabilities
assertTrue(client.testConnection());

// Call tools
CallToolResult result = client.callTool("listSpaces", Map.of("limit", 10));

// Clean up
client.close();
```

### AbstractMcpIntegrationTest

Base class for integration tests with common utilities:

```java
@ExtendWith(McpIntegrationTest.class)
class MyMcpTest extends AbstractMcpIntegrationTest {
    
    @Test
    void shouldListSpaces() {
        // Test utilities available
        assertToolExists("listSpaces");
        CallToolResult result = callToolAndAssertSuccess("listSpaces", Map.of());
        // ... assertions
    }
}
```

## Best Practices

### Test Development Workflow

1. **Start with Inspector** - Use interactive testing to understand tool behavior
2. **Write Integration Tests** - Automate the scenarios you validated manually
3. **Test Edge Cases** - Invalid inputs, network failures, auth errors
4. **Performance Testing** - Ensure reasonable response times
5. **Continuous Integration** - Run tests on every commit

### Test Data Management

- Use test-specific Confluence spaces when possible
- Mock external dependencies for unit tests
- Use real Confluence instance for integration tests
- Clean up test data after test runs

### Error Testing

Always test error conditions:
- Missing required parameters
- Invalid parameter types
- Authentication failures
- Network timeouts
- Malformed responses

## Debugging Test Failures

### Common Issues

**Build Failures**
```bash
# Ensure project builds correctly
./gradlew clean build -x test
```

**Server Startup Issues**
```bash
# Check server logs in test output
./test-harness/scripts/run-integration-tests.sh --verbose
```

**Authentication Issues**
```bash
# Verify .env file configuration
cat .env
# Test credentials manually with curl
curl -u "$CONFLUENCE_API_USERNAME:$CONFLUENCE_API_TOKEN" "$CONFLUENCE_API_BASE_URL/rest/api/space"
```

**Test Timeouts**
```bash
# Increase timeout in test configuration
# Check server performance and network connectivity
```

### Test Reports

Test reports are generated at:
- `build/reports/tests/test/index.html` - Main test report
- `build/test-results/test/` - JUnit XML results
- Console output - Real-time test progress

## Integration with CI/CD

### GitHub Actions Example

```yaml
name: MCP Tests
on: [push, pull_request]

jobs:
  mcp-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '21'
      - name: Run MCP Integration Tests
        run: ./test-harness/scripts/run-integration-tests.sh
        env:
          CONFLUENCE_API_BASE_URL: ${{ secrets.CONFLUENCE_API_BASE_URL }}
          CONFLUENCE_API_USERNAME: ${{ secrets.CONFLUENCE_API_USERNAME }}
          CONFLUENCE_API_TOKEN: ${{ secrets.CONFLUENCE_API_TOKEN }}
```

## Advanced Testing

### Performance Testing

```bash
# Run performance-focused tests
./gradlew test --tests "*Performance*"

# Monitor response times
./gradlew test --tests "*ConfluenceMcp*" -Dtest.performance.monitoring=true
```

### Protocol Compliance Testing

The test harness validates MCP protocol compliance:
- JSON-RPC message format
- Capability negotiation
- Error response formats
- Transport layer behavior

### Custom Test Scenarios

Create custom test scenarios for your specific use cases:

```java
@Test
void shouldHandleCustomWorkflow() {
    // List spaces
    CallToolResult spaces = callToolAndAssertSuccess("listSpaces", Map.of());
    
    // Extract space key from result
    String spaceKey = extractSpaceKeyFromResult(spaces);
    
    // List pages in that space
    CallToolResult pages = callToolAndAssertSuccess("listPages", 
        Map.of("spaceKey", spaceKey));
    
    // Get specific page content
    String pageId = extractPageIdFromResult(pages);
    CallToolResult page = callToolAndAssertSuccess("getPage", 
        Map.of("pageId", pageId));
        
    // Validate content format
    assertContentIsMarkdown(page);
}
```

## Troubleshooting

### Common Test Issues

1. **JAR not found** - Run `./gradlew build` first
2. **Port conflicts** - Ensure port 8081 is available for SSE tests
3. **Confluence connectivity** - Verify .env configuration and network access
4. **Test timeouts** - Check server startup time and increase timeouts if needed

### Getting Help

- Check test logs for detailed error messages
- Use `--verbose` flag for additional debugging output
- Run individual tests to isolate issues
- Review MCP protocol documentation for compliance requirements

## Contributing

When adding new features to the MCP server:

1. Add corresponding test cases to `ConfluenceMcpIntegrationTest`
2. Test manually with MCP Inspector
3. Update this documentation if adding new test utilities
4. Ensure all tests pass in CI/CD pipeline

This test harness ensures the Confluence MCP Server provides a reliable, compliant, and performant MCP implementation.