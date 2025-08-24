# Confluence MCP Server

A Model Context Protocol (MCP) Server for Atlassian Confluence, so you can query, scrape and even update Confluence spaces, pages, and content.

NOTE: this MCP server is targeted at Confluence v9.2 Server/Data Center.

## Compatibility

see https://developer.atlassian.com/cloud/confluence/rest/v1/intro/#about  
see https://docs.atlassian.com/atlassian-confluence/REST/6.6.0/

### Supported Confluence Versions

- **Confluence Server/Data Center 9.2** - Fully tested and verified
- **Confluence Server/Data Center 7.9+** - Compatible (requires personal access tokens)
- **Earlier versions** - May work but not officially tested

### REST API Endpoints

This MCP server uses the standard Confluence REST API v1 endpoints:

- **Spaces API**: `/rest/api/space`
  - `GET /rest/api/space` - List spaces
  - `GET /rest/api/space/{spaceKey}` - Get specific space
  - `POST /rest/api/space` - Create new space
  - `PUT /rest/api/space/{spaceKey}` - Update space

- **Content API**: `/rest/api/content`
  - `GET /rest/api/content` - List pages/content
  - `GET /rest/api/content/{pageId}` - Get specific page
  - `POST /rest/api/content` - Create new page
  - `PUT /rest/api/content/{pageId}` - Update page

- **Search API**: `/rest/api/search`
  - `GET /rest/api/search` - CQL-based content search

### Authentication

- **Personal Access Tokens (PAT)** - Recommended for Confluence 7.9+
- **Basic Authentication** - Username/password (less secure)
- **Bearer Token Authentication** - API tokens via Authorization header

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
   export CONFLUENCE_API_BASE_URL=https://your-site.atlassian.net  # Full URL to your Confluence instance
   export CONFLUENCE_API_USERNAME=your-email@example.com
   export CONFLUENCE_API_TOKEN=your-api-token-here
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

**Check MCP server endpoint:**
```bash
# The MCP server uses Server-Sent Events at /mcp/message
curl -H "Accept: text/event-stream" http://localhost:8081/mcp/message
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