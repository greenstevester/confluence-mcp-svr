# Confluence MCP Server

A Model Context Protocol (MCP) Server for Atlassian Confluence, so you can query, scrape and even update Confluence spaces, pages, and content.

## Key Features

Provides AI assistants (Github co-pilot, claude, chatGPT) with tools to:
- **Search** Confluence spaces and pages using powerful CQL queries
- **Read** Confluence page content with full formatting and metadata
- **Create** new Confluence spaces with custom configuration  
- **Create** new Confluence pages with rich content
- **List and browse** spaces and pages with advanced filtering
- **Discover** content structure and relationships

## Quick Start

### Prerequisites

- Java 21 or higher
- Gradle (included via wrapper)
- Access to a target Confluence instance (Cloud or Server)
- Confluence API credentials

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd confluence-mcp-svr
   ```

2. **Configure Confluence credentials:**

   You have three options for configuration:

   **Option A: Using Environment Variables (Recommended for production)**
   ```bash
   export ATLASSIAN_SITE_NAME=your-site-name  # Just the subdomain, not the full URL
   export ATLASSIAN_USER_EMAIL=your-email@example.com
   export ATLASSIAN_API_TOKEN=your-api-token-here
   ```

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

5. **Run the application:**

   **For development with application-dev.properties:**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
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

The server will start on `http://localhost:8081` and be ready to accept MCP client connections.

### Verify Installation

Once the server is running, verify it's working:

**Check health endpoint:**
```bash
curl http://localhost:8081/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

**Check MCP server info:**
```bash
curl http://localhost:8081/mcp/info
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

### Interface Philosophy: Simple Input, Rich Output

This server follows a "Minimal Interface, Maximal Detail" approach:

1.  **Simple Tools:** Ask for only essential identifiers or filters (like `pageId`, `spaceKey`, `cql`).
2.  **Rich Details:** When you ask for a specific item (like `get-page`), the server provides all relevant information by default (content, labels, links, etc.) without needing extra flags.

## Available Tools

This MCP server provides the following tools for your AI assistant:

### List Spaces (`list-spaces`)

**Purpose:** Discover available Confluence spaces and find their 'keys' (unique identifiers).

**Use When:** You need to know which spaces exist, find a space's key, or filter spaces by type/status.

**Conversational Example:** "Show me all the Confluence spaces."

**Parameter Example:** `{}` (no parameters needed for basic list) or `{ type: "global", status: "current" }` (to filter).

### Get Space (`get-space`)

**Purpose:** Retrieve detailed information about a _specific_ space using its key. Includes homepage content snippet.

**Use When:** You know the space key (e.g., "DEV") and need its full details, labels, or homepage overview.

**Conversational Example:** "Tell me about the 'DEV' space in Confluence."

**Parameter Example:** `{ spaceKey: "DEV" }`

### List Pages (`list-pages`)

**Purpose:** List pages within specific spaces (using numeric space IDs) or across the instance, with filtering options.

**Use When:** You need to find pages in a known space (requires numeric ID), filter by status, or do simple text searches on titles/labels.

**Conversational Example:** "Show me current pages in space ID 123456." (Use `list-spaces` first if you only know the key).

**Parameter Example:** `{ spaceId: ["123456"] }` or `{ status: ["archived"], query: "Meeting Notes" }`.

### Get Page (`get-page`)

**Purpose:** Retrieve the full content (in Markdown) and metadata of a _specific_ page using its numeric ID.

**Use When:** You know the numeric page ID (found via `list-pages` or `search`) and need to read, analyze, or summarize its content.

**Conversational Example:** "Get the content of Confluence page ID 12345678."

**Parameter Example:** `{ pageId: "12345678" }`

### Search (`search`)

**Purpose:** Perform powerful searches across Confluence content (pages, blogs, attachments) using CQL (Confluence Query Language).

**Use When:** You need complex searches involving multiple criteria, full-text search, or filtering by labels, dates, contributors, etc.

**Conversational Example:** "Search Confluence for pages labeled 'meeting-notes' created in the last week."

**Parameter Example:** `{ cql: "label = meeting-notes AND created > -7d" }`

### Create Space (`create-space`)

**Purpose:** Create a new Confluence space with specified configuration.

**Use When:** You need to create a new space for organizing content, setting up a new project area, or establishing a dedicated knowledge base.

**Conversational Example:** "Create a new space called 'Product Documentation' with key 'PROD_DOCS'."

**Parameter Example:** `{ "key": "PROD_DOCS", "name": "Product Documentation", "description": "Space for all product documentation and guides", "type": "KNOWLEDGE_BASE" }`

**Required Parameters:**
- `key` - Unique space key (uppercase letters, numbers, underscores only, e.g., "TEAM_DOCS")
- `name` - Display name for the space

**Optional Parameters:**
- `description` - Space description
- `type` - Space type: "GLOBAL" (default), "PERSONAL", "COLLABORATION", "KNOWLEDGE_BASE"
- `status` - Space status: "CURRENT" (default) or "ARCHIVED"

**Returns:** Comprehensive space creation confirmation with:
- Space ID, key, name, and type
- Creation timestamp and author
- Description (if provided)
- Direct access links to the new space

### Create Page (`create-page`)

**Purpose:** Create a new page within an existing Confluence space.

**Use When:** You need to add new content, documentation, or information to a space.

**Conversational Example:** "Create a new page called 'API Documentation' in the DEV space with some getting started content."

**Parameter Example:** `{ "title": "API Documentation", "spaceKey": "DEV", "content": "<h1>Getting Started</h1><p>This page contains our API documentation...</p>" }`

**Required Parameters:**
- `title` - Page title
- `spaceKey` - Space key where the page should be created (e.g., "DEV", "DOCS")
- `content` - Page content in Confluence storage format (HTML-like markup)

**Optional Parameters:**
- `parentId` - ID of parent page (creates as child page)
- `contentRepresentation` - Content format: "storage" (default) or "wiki"
- `status` - Page status: "CURRENT" (default) or "DRAFT"

**Content Format Examples:**
- **Simple text:** `"<p>This is a simple paragraph.</p>"`
- **With headings:** `"<h1>Main Title</h1><h2>Subtitle</h2><p>Content here.</p>"`
- **With lists:** `"<ul><li>Item 1</li><li>Item 2</li></ul>"`
- **With links:** `"<p>See <a href='https://example.com'>example</a> for more info.</p>"`

**Returns:** Detailed page creation confirmation with:
- Page ID, title, and space information
- Parent page relationship (if applicable)
- Direct access links to view the new page

**Important Notes:**
- Space must exist before creating pages (use `list-spaces` to verify)
- User must have page creation permissions in the target space
- Content should be in Confluence storage format (HTML-like markup)
- Page titles must be unique within the space

## Common Usage Workflows

### Setting Up a New Project Space

1. **Create the project space:**
   ```
   "Create a new space for the Alpha project with key 'ALPHA' and description 'Documentation and resources for Project Alpha'"
   ```

2. **Create initial project pages:**
   ```
   "Create a 'Project Overview' page in the ALPHA space with an overview of Project Alpha goals and timeline"
   "Create a 'Meeting Notes' page in the ALPHA space as a child of Project Overview"
   "Create an 'API Documentation' page in the ALPHA space with basic API endpoint information"
   ```

### Content Migration and Organization

1. **Discover existing structure:**
   ```
   "Show me all current spaces to understand the existing organization"
   "List pages in the DEV space to see what documentation already exists"
   ```

2. **Create organized content:**
   ```
   "Create a 'Development Guides' space with key 'DEV_GUIDES' for centralizing all development documentation"
   "Create a 'Getting Started' page in DEV_GUIDES with onboarding information for new developers"
   ```

### Knowledge Base Development

1. **Set up knowledge base:**
   ```
   "Create a knowledge base space called 'Customer Support' with key 'SUPPORT_KB'"
   ```

2. **Structure content hierarchy:**
   ```
   "Create a 'Troubleshooting Guide' page in SUPPORT_KB with common customer issues"
   "Create a 'FAQ' page in SUPPORT_KB with frequently asked questions"
   "Create a 'Product Features' page as a child of the FAQ page with detailed feature explanations"
   ```

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
   ./create-idea-config.sh
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
./create-idea-config.sh
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

All tests pass when run with:
SPRING_PROFILES_ACTIVE=dev ./gradlew test

The tests demonstrate that:
- Configuration is correctly loaded from application-dev.properties
- Service uses real Confluence API settings (not mocks)
- Dev profile is active and working
- Service is properly wired with Spring dependency injection


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