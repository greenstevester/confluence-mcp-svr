# Confluence MCP Server

Connect your AI assistant directly to Atlassian Confluence to search, read, create, and manage documentation without manual copy-paste. This Model Context Protocol (MCP) server enables AI assistants like Claude, ChatGPT, and GitHub Copilot to interact with your Confluence spaces and pages in real-time.

> **Status**: Early development - Core MCP infrastructure complete, Confluence tool implementations in progress

## Why Use This MCP Server?

- **Eliminate Manual Work**: No more copying content between Confluence and your AI assistant
- **Real-Time Access**: AI gets up-to-date information directly from your Confluence instance
- **Enhanced AI Capabilities**: Enable AI to search, summarize, and analyze your documentation contextually
- **Secure Integration**: You control access via API tokens - AI interactions remain contained

## Compatibility

**Primary Target**: Confluence Server/Data Center 9.2 (fully tested)
**Also Compatible**: Confluence Server/Data Center 7.9+ (requires personal access tokens)
**Earlier Versions**: May work but not officially tested

See [Confluence REST API documentation](https://developer.atlassian.com/cloud/confluence/rest/v1/intro/#about) for full compatibility details.


## Available AI Tools

Once connected, your AI assistant gains access to these Confluence capabilities:

- **Search Content** - Powerful CQL-based search across all spaces and pages
- **Read Pages** - Get full page content converted to Markdown with metadata
- **List & Browse** - Discover spaces, pages, and content structure
- **Create Content** - Create new spaces and pages with rich content
- **Manage Structure** - Organize content with parent-child relationships

## Quick Start

### Prerequisites

- **Java 21+** installed on your system
- **Confluence instance** (Cloud or Server 7.9+) with admin/API access
- **Personal Access Token** from Confluence (see setup below)

### Installation Options

**Option A: Run Locally (Recommended for development)**
```bash
# 1. Clone and navigate
git clone https://github.com/greenstevester/confluence-mcp-svr.git
cd confluence-mcp-svr

# 2. Configure credentials (create .env file)
cp .env.example .env
# Edit .env with your Confluence details

# 3. Build and run
./gradlew bootRun --args='--spring.profiles.active=dev'
```

**Option B: Run with Docker**
```bash
# Quick start with interactive setup
./docker/docker-quickstart.sh
```

### Get Your Confluence API Token

1. Go to [Atlassian API Tokens](https://id.atlassian.com/manage-profile/security/api-tokens)
2. Click "Create API token"
3. Name it "MCP Server" 
4. Copy the token immediately (you won't see it again)
5. Add to your `.env` file:
   ```env
   CONFLUENCE_API_BASE_URL=https://your-site.atlassian.net
   CONFLUENCE_API_USERNAME=your-email@example.com
   CONFLUENCE_API_TOKEN=your-copied-token
   ```

### Verify Installation

Once running, test your server:

```bash
# Check server health
curl http://localhost:8081/actuator/health
# Expected: {"status":"UP"}

# Verify MCP endpoint
curl -H "Accept: text/event-stream" http://localhost:8081/mcp/message
# Should establish SSE connection
```

## Usage

### Connecting to AI Assistants

#### Claude Desktop

Add this configuration to your Claude Desktop MCP settings:

```json
{
  "mcpServers": {
    "confluence": {
      "command": "java",
      "args": ["-jar", "confluence-mcp-svr-0.0.1-SNAPSHOT.jar"],
      "env": {
        "CONFLUENCE_API_BASE_URL": "https://your-site.atlassian.net",
        "CONFLUENCE_API_USERNAME": "your-email@example.com",
        "CONFLUENCE_API_TOKEN": "your-api-token"
      }
    }
  }
}
```

#### Other MCP Clients

This server works with any MCP-compatible client. For custom integrations, see our [MCP Client Implementation Guide](docs/MCP-CLIENT.md).

### Example Interactions

Once connected, you can interact with your Confluence instance using natural language:

**Search for content:**
> "Find all pages about 'API documentation' in the Engineering space"

**Read documentation:**
> "Show me the content of the 'Getting Started' page in the DEV space"

**Create new content:**
> "Create a new page called 'Sprint Planning' in the TEAM space with our planning template"

**Discover structure:**
> "List all spaces and show me what's in the DOCS space"

**Analyze content:**
> "Summarize all the troubleshooting guides in our support documentation"

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

## Common Workflows

### Setting Up Documentation Spaces

**Create a new project space:**
> "Create a space called 'Project Alpha' with key 'ALPHA' for our new product documentation"

**Add initial structure:**
> "In the ALPHA space, create a 'Getting Started' page with setup instructions and a 'Meeting Notes' page for team updates"

### Content Discovery and Organization

**Explore existing content:**
> "Show me all spaces and their purposes, then list the most recently updated pages"

**Organize information:**
> "Find all API documentation across our spaces and create a summary page linking to each guide"

### Knowledge Management

**Create structured knowledge base:**
> "Create a 'Support KB' space, then add pages for FAQ, troubleshooting, and product features with proper hierarchy"

**Content analysis:**
> "Analyze our documentation for gaps - what topics are mentioned but don't have dedicated pages?"




## Development & Contributing

This project welcomes contributions! See our comprehensive documentation:

- **[Development Setup](docs/PROJECT-DETAILS.md)** - Build, test, and run locally
- **[Docker Guide](docs/DOCKER.md)** - Container deployment and debugging  
- **[Test Harness](test-harness/docs/MCP-TEST-HARNESS.md)** - Comprehensive testing tools
- **[MCP Client Guide](docs/MCP-CLIENT.md)** - Integrate with custom applications

### Quick Development Setup

```bash
# Build and test
./gradlew clean build

# Run with development profile
./gradlew bootRun --args='--spring.profiles.active=dev'

# Run comprehensive tests
./test-harness/scripts/run-integration-tests.sh
```

## Troubleshooting

### Common Issues

**Server won't start:**
```bash
# Check Java version (needs 21+)
java --version

# Verify port 8081 is available
lsof -i :8081
```

**Authentication errors:**
- Verify API token is correct and not expired
- Check base URL format (include `https://`)
- Ensure user has Confluence permissions
- Test credentials manually:
  ```bash
  curl -u "$CONFLUENCE_API_USERNAME:$CONFLUENCE_API_TOKEN" \
       "$CONFLUENCE_API_BASE_URL/rest/api/space"
  ```

**Connection issues:**
- Check server logs: `./docker/logs.sh` (Docker) or application console output
- Verify firewall/network access to Confluence instance
- Test health endpoint: `curl http://localhost:8081/actuator/health`

### Getting Help

- **Issues**: [GitHub Issues](https://github.com/greenstevester/confluence-mcp-svr/issues)
- **Discussions**: [GitHub Discussions](https://github.com/greenstevester/confluence-mcp-svr/discussions)
- **Documentation**: Comprehensive guides in the [docs/](docs) directory

---

**Built with [Spring AI MCP Server](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html) • [Model Context Protocol](https://modelcontextprotocol.io/) • [Confluence REST API](https://developer.atlassian.com/cloud/confluence/rest/v2/)**