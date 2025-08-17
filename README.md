# Confluence MCP Server

A Spring Boot application that serves as a Model Context Protocol (MCP) Server for Confluence integration, enabling AI assistants to interact with Confluence spaces, pages, and content.

## What is this?

This MCP server provides AI assistants (like Claude) with tools to:
- Search Confluence spaces and pages
- Read Confluence page content
- Create and update Confluence pages
- Manage Confluence permissions and metadata

**Model Context Protocol (MCP)** is a standardized protocol that allows AI assistants to securely connect to external data sources and tools. This server exposes Confluence functionality through the MCP protocol.

## Quick Start

### Prerequisites

- Java 21 or higher
- Gradle (included via wrapper)
- Access to a Confluence instance (Cloud or Server)
- Confluence API credentials

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd confluence-mcp-svr
   ```

2. **Configure Confluence credentials:**
   
   Create or update `src/main/resources/application.properties`:
   ```properties
   # Confluence Configuration
   confluence.base-url=https://your-domain.atlassian.net
   confluence.username=your-email@domain.com
   confluence.api-token=your-api-token
   
   # MCP Server Configuration
   server.port=8080
   ```

3. **Build and run:**
   ```bash
   ./gradlew bootRun
   ```

The server will start on `http://localhost:8080` and be ready to accept MCP client connections.

### First Success Test

Verify the server is working by checking the health endpoint:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

## Configuration

### Confluence API Setup

1. **For Confluence Cloud:**
   - Generate an API token at https://id.atlassian.com/manage-profile/security/api-tokens
   - Use your email as username and the token as password

2. **For Confluence Server:**
   - Use your username and password, or create a dedicated service account

### Environment Variables

You can also configure using environment variables:
```bash
export CONFLUENCE_BASE_URL=https://your-domain.atlassian.net
export CONFLUENCE_USERNAME=your-email@domain.com
export CONFLUENCE_API_TOKEN=your-api-token
```

### Advanced Configuration

```properties
# Connection settings
confluence.connection-timeout=30s
confluence.read-timeout=60s

# MCP Server settings
mcp.server.name=confluence-server
mcp.server.version=1.0.0
```

## Usage

### Connecting MCP Clients

This server implements the MCP protocol and can be connected to by any MCP-compatible client.

#### Claude Desktop Configuration

Add to your Claude Desktop MCP configuration:

```json
{
  "mcpServers": {
    "confluence": {
      "command": "java",
      "args": ["-jar", "/path/to/confluence-mcp-svr-0.0.1-SNAPSHOT.jar"],
      "env": {
        "CONFLUENCE_BASE_URL": "https://your-domain.atlassian.net",
        "CONFLUENCE_USERNAME": "your-email@domain.com",
        "CONFLUENCE_API_TOKEN": "your-api-token"
      }
    }
  }
}
```

#### Custom MCP Client

For integrating with your own MCP client, see the [MCP Client Guide](docs/MCP-CLIENT.md).

### Available Tools

Once connected, the following tools will be available to the AI assistant:

- `confluence_search_pages` - Search for pages across Confluence spaces
- `confluence_get_page` - Retrieve page content and metadata
- `confluence_create_page` - Create new pages in Confluence
- `confluence_update_page` - Update existing page content
- `confluence_list_spaces` - List available Confluence spaces

### Example Interactions

**Search for pages:**
```
"Find all pages about 'API documentation' in the Engineering space"
```

**Read page content:**
```
"Show me the content of the 'Getting Started' page"
```

**Create documentation:**
```
"Create a new page in the 'Team Docs' space with meeting notes from today"
```

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

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Built with [Spring AI MCP Server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
- [Model Context Protocol](https://modelcontextprotocol.io/) specification
- [Atlassian Confluence API](https://developer.atlassian.com/cloud/confluence/rest/v2/)

## Status

🚧 **This project is currently in early development.** 

Core MCP server infrastructure is in place, but Confluence-specific tool implementations are still being developed. Contributions welcome!

---

**Need help?** Open an issue or check the [troubleshooting section](#troubleshooting) above.