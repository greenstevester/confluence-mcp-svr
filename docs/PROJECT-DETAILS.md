All tests pass when run with:
SPRING_PROFILES_ACTIVE=dev ./gradlew test

The tests demonstrate that:
- Configuration is correctly loaded from application-dev.properties
- Service uses real Confluence API settings (not mocks)
- Dev profile is active and working
- Service is properly wired with Spring dependency injection



**Option B: Using application-dev.properties (For development)**

Copy the example development configuration:
   ```bash
   cp src/main/resources/application-dev.properties src/main/resources/application-local.properties
   ```

Then edit `application-local.properties` with your credentials:
   ```properties
   # Confluence API Configuration
   confluence.api.base-url=https://your-site-name.atlassian.net
   confluence.api.username=your-email@example.com
   confluence.api.token=your-api-token-here
   ```

**Option C: Direct modification (Quick testing only)**

Edit `src/main/resources/application.properties` directly (not recommended for production).

3. **Get your Confluence API Token:**
    - Go to [Atlassian API Tokens](https://id.atlassian.com/manage-profile/security/api-tokens)
    - Click "Create API token"
    - Give it a label (e.g., "MCP Server")
    - Copy the token immediately (you won't be able to see it again)

4. **Build the application:**
   ```bash
   ./gradlew build
   ```

   **For development with custom local properties:**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

   **For production with environment variables:**
   ```bash
   ./gradlew bootRun
   ```

   **Or using the JAR directly:**
   ```bash
   java -jar build/libs/confluence-mcp-svr-0.0.1-SNAPSHOT.jar
   ```

## Configuration

### Confluence API Setup

1. **For Confluence Cloud:**
    - Generate an API token at https://id.atlassian.com/manage-profile/security/api-tokens
    - Use your email as username and the token as password

2. **For Confluence Server:**
    - Use your username and password, or create a dedicated service account

### Environment Variables

The application uses these environment variables:
```bash
export ATLASSIAN_SITE_NAME=your-site-name      
export ATLASSIAN_USER_EMAIL=your-email@domain.com
export ATLASSIAN_API_TOKEN=your-api-token
```

### Advanced Configuration

All configuration options available in `application.properties`:

```properties
# Confluence API Configuration
confluence.api.base-url={ATLASSIAN_SITE_NAME}
confluence.api.username=${ATLASSIAN_USER_EMAIL}
confluence.api.token=${ATLASSIAN_API_TOKEN}
confluence.api.timeout=30s
confluence.api.max-connections=20
confluence.api.retry-attempts=3

# MCP Server Configuration
mcp.server.name=mcp-server-atlassian-confluence
mcp.server.version=2.0.1
mcp.server.description=MCP server for Atlassian Confluence

# Default Settings
confluence.defaults.page-size=25
confluence.defaults.body-format=storage
confluence.defaults.include-labels=true
confluence.defaults.include-version=true
```

**Benefits:**

- **Real-time Access:** Your AI assistant can directly access up-to-date Confluence content.
- **Eliminate Copy/Paste:** No need to manually transfer information between Confluence and your AI assistant.
- **Enhanced AI Capabilities:** Enables AI to search, summarize, analyze, and reference your Confluence documentation contextually.
- **Security:** You control access via an API token. The AI interacts through the server, and sensitive operations remain contained.

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── io/github/greenstevester/confluence_mcp_svr/
│   │       ├── ConfluenceMcpSvrApplication.java    # Main application
│   │       ├── config/                              # Configuration classes
│   │       ├── service/                             # Business logic
│   │       └── tools/                               # MCP tool implementations
│   └── resources/
│       ├── application.properties                   # Configuration
│       └── static/                                  # Static resources
└── test/                                           # Test classes
```

### Adding New Tools

To add new MCP tools for Confluence functionality:

1. Create a new class in the `tools` package
2. Annotate methods with `@Tool` from Spring AI MCP
3. Implement the Confluence API integration in the `service` package

Example:
```java
@Component
public class ConfluenceTools {
    
    @Tool("Search Confluence pages")
    public String searchPages(
        @ToolParameter("Search query") String query,
        @ToolParameter("Space key (optional)") String spaceKey
    ) {
        // Implementation here
    }
}
```

## Architecture

### Technology Stack

- **Framework:** Spring Boot 3.5.4
- **Language:** Java 21
- **Build Tool:** Gradle
- **MCP Integration:** Spring AI MCP Server (WebMVC)
- **Spring AI Version:** 1.0.1

### Key Dependencies

- `spring-boot-starter-web` - REST API capabilities
- `spring-ai-starter-mcp-server-webmvc` - MCP server implementation

### MCP Protocol Implementation

This server implements the Model Context Protocol specification:
- **Tools:** Confluence operations exposed as callable functions
- **Resources:** Access to Confluence pages and spaces
- **Prompts:** Pre-configured templates for common tasks

## Troubleshooting

### Common Issues

**Connection refused when starting:**
- Check if port 8080 is available
- Verify Java 21 is installed: `java --version`

**Confluence authentication errors:**
- Verify API token is correct and not expired
- Check base URL format (include https://)
- Ensure user has appropriate Confluence permissions

**MCP client connection issues:**
- Verify server is running and accessible
- Check MCP client configuration matches server settings
- Review logs for detailed error messages

### Getting Help

1. Check the [Spring AI MCP documentation](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
2. Review Confluence API documentation
3. Enable debug logging: `logging.level.io.github.greenstevester=DEBUG`

## Contributing

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes
4. Add tests for new functionality
5. Run the test suite: `./gradlew test`
6. Commit your changes: `git commit -m "Add your feature"`
7. Push to your fork: `git push origin feature/your-feature`
8. Create a Pull Request

### Code Style

- Follow Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Maintain test coverage above 80%

### Testing

- Write unit tests for all new functionality
- Include integration tests for Confluence API interactions
- Mock external dependencies in tests



### Interface Philosophy: Simple Input, Rich Output

This server follows a "Minimal Interface, Maximal Detail" approach:

1.  **Simple Tools:** Ask for only essential identifiers or filters (like `pageId`, `spaceKey`, `cql`).
2.  **Rich Details:** When you ask for a specific item (like `get-page`), the server provides all relevant information by default (content, labels, links, etc.) without needing extra flags.


### Troubleshooting

**Common Issues:**

1. **Connection refused on port 8081:**
    - Ensure no other service is using port 8081
    - Change the port in application.properties: `server.port=8082`

2. **Authentication errors:**
    - Verify your API token is valid and not expired
    - Ensure you're using the correct site name (just the subdomain)
    - Check that your user has appropriate Confluence permissions

3. **Build failures:**
    - Ensure Java 21 is installed: `java -version`
    - Clear Gradle cache: `./gradlew clean build`

4. **Runtime configuration issues:**
    - Check active profile: Add `-Dspring.profiles.active=dev` for development
    - Verify environment variables are set: `echo $ATLASSIAN_SITE_NAME`

## Running in IntelliJ IDEA

### Environment Variables Setup

For IntelliJ IDEA users, we provide a convenient script to automatically generate run configurations with environment variables from your `.env` file.

1. **Create your `.env` file** (if you haven't already):
   ```bash
   # Copy the values to your .env file
   CONFLUENCE_API_BASE_URL=http://localhost:8090
   CONFLUENCE_API_USERNAME=your-username
   CONFLUENCE_API_TOKEN=your-api-token
   ```

2. **Generate IntelliJ run configuration**:
   ```bash
   ./scripts/create-idea-config.sh
   ```

   This script will:
    - Read environment variables from your `.env` file
    - Create `.idea/runConfigurations/ConfluenceMcpSvrApplication.xml`
    - Generate a proper IntelliJ run configuration with all environment variables loaded

3. **Use the configuration**:
    - Restart IntelliJ IDEA or refresh the project
    - Go to **Run → Edit Configurations**
    - You'll see **ConfluenceMcpSvrApplication** configuration
    - Run the application - it will automatically use your `.env` values

### Benefits of this approach:

- ✅ **Secure**: Sensitive credentials stay in `.env` (git-ignored)
- ✅ **Convenient**: One command to set up IntelliJ configuration
- ✅ **Consistent**: Same environment variables for all team members
- ✅ **Simple**: No manual IntelliJ configuration needed

### Updating environment variables:

When you modify your `.env` file, simply re-run:
```bash
./scripts/create-idea-config.sh
```

The script will update your IntelliJ configuration with the new values.

## Development

### Building the Project

```bash
# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Create bootable JAR
./gradlew bootJar
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "ConfluenceMcpSvrApplicationTests"
```

