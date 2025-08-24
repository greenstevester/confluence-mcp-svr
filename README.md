# Confluence MCP Server

A Model Context Protocol (MCP) Server for Atlassian Confluence, so you can query, scrape and even update Confluence spaces, pages, and content.

## Key Features

Provides AI assistants (Github co-pilot, claude, chatGPT) with tools to:
- **Search** Confluence spaces and pages using powerful CQL queries
- **Read** Confluence page content with full formatting and metadata
- **Create** new Confluence spaces and pages with content
- **List and browse** spaces and pages with advanced filtering
- **Discover** content structure and relationships

## Quick Start

To get this going, you've got options:

- **Run locally:** Install Java 21 or higher, clone the repository, and run the server.
- **Run in Docker:** Use the pre-built Docker image, or build your own.

## Run locally

### Prerequisites

- (to run locally) Java 21 or higher installed
- Access to a target Confluence instance (Cloud or Server)
- Confluence Personal Access Token (PAT) credentials, details below on how to get that.

### Local Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd confluence-mcp-svr
   ```

2. **Configure Confluence credentials:**

  For configuration:

   **Option A: Using Environment Variables (Recommended for production)**
   ```bash
   export ATLASSIAN_SITE_NAME=your-site-name  # Just the subdomain, not the full URL
   export ATLASSIAN_USER_EMAIL=your-email@example.com
   export ATLASSIAN_API_TOKEN=your-api-token-here
   ```
5. **Run the application:**

   **For development with application-dev.properties:**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
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
```## Usage

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