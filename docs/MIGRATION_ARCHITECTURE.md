# Confluence MCP Server Migration Architecture

## Current State (TypeScript) vs Target State (Java Spring Boot)

```mermaid
graph TB
    subgraph "TypeScript MCP Server (Source)"
        TS_CLI["CLI Interface<br/>atlassian.*.cli.ts"]
        TS_TOOLS["MCP Tools<br/>atlassian.*.tool.ts"]
        TS_CONTROLLERS["Controllers<br/>atlassian.*.controller.ts"]
        TS_SERVICES["Services<br/>vendor.atlassian.*.service.ts"]
        TS_UTILS["Utils<br/>config, logger, transport, etc."]
        TS_TYPES["Types<br/>*.types.ts"]
        
        TS_CLI --> TS_CONTROLLERS
        TS_TOOLS --> TS_CONTROLLERS
        TS_CONTROLLERS --> TS_SERVICES
        TS_SERVICES --> TS_UTILS
        TS_CONTROLLERS --> TS_UTILS
        TS_TOOLS --> TS_TYPES
        TS_CONTROLLERS --> TS_TYPES
        TS_SERVICES --> TS_TYPES
    end
    
    subgraph "Java Spring Boot MCP Server (Target)"
        JAVA_APP["@SpringBootApplication<br/>ConfluenceMcpSvrApplication"]
        JAVA_TOOLS["MCP Tools<br/>@Tool annotations"]
        JAVA_SERVICES["@Service Components<br/>Business Logic"]
        JAVA_CLIENTS["API Clients<br/>WebClient/RestTemplate"]
        JAVA_CONFIG["@Configuration<br/>Properties & Beans"]
        JAVA_MODELS["POJOs<br/>Records & DTOs"]
        JAVA_UTILS["Utility Classes<br/>Static methods"]
        JAVA_EXCEPTIONS["Custom Exceptions<br/>@ControllerAdvice"]
        
        JAVA_APP --> JAVA_TOOLS
        JAVA_TOOLS --> JAVA_SERVICES
        JAVA_SERVICES --> JAVA_CLIENTS
        JAVA_SERVICES --> JAVA_UTILS
        JAVA_CLIENTS --> JAVA_CONFIG
        JAVA_TOOLS --> JAVA_MODELS
        JAVA_SERVICES --> JAVA_MODELS
        JAVA_CLIENTS --> JAVA_MODELS
        JAVA_SERVICES --> JAVA_EXCEPTIONS
    end
    
    subgraph "External Dependencies"
        CONFLUENCE_API["Confluence REST API v2"]
        MCP_PROTOCOL["MCP JSON-RPC Protocol"]
    end
    
    TS_SERVICES --> CONFLUENCE_API
    TS_TOOLS --> MCP_PROTOCOL
    
    JAVA_CLIENTS --> CONFLUENCE_API
    JAVA_TOOLS --> MCP_PROTOCOL
    
    subgraph "Migration Mapping"
        direction TB
        MAP1["TypeScript Types → Java Records/POJOs"]
        MAP2["Vendor Services → API Clients + @Service"]
        MAP3["Controllers → @Service Components"]
        MAP4["Tools → @Tool Methods"]
        MAP5["Utils → Utility Classes + @Configuration"]
    end
```

## Feature Modules

### 1. Pages Module
**TypeScript Components:**
- `vendor.atlassian.pages.service.ts` → API client
- `atlassian.pages.controller.ts` → Business logic
- `atlassian.pages.tool.ts` → MCP tools
- `atlassian.pages.types.ts` → Data models

**Java Components:**
- `ConfluencePagesClient` → API integration
- `ConfluencePagesService` → Business logic
- `PagesToolsConfiguration` → MCP tool definitions
- `Page`, `PageDetails`, `PagesResponse` → Data models

### 2. Search Module
**TypeScript Components:**
- `vendor.atlassian.search.service.ts` → API client
- `atlassian.search.controller.ts` → Business logic
- `atlassian.search.tool.ts` → MCP tools
- `atlassian.search.types.ts` → Data models

**Java Components:**
- `ConfluenceSearchClient` → API integration
- `ConfluenceSearchService` → Business logic
- `SearchToolsConfiguration` → MCP tool definitions
- `SearchRequest`, `SearchResult` → Data models

### 3. Spaces Module
**TypeScript Components:**
- `vendor.atlassian.spaces.service.ts` → API client
- `atlassian.spaces.controller.ts` → Business logic
- `atlassian.spaces.tool.ts` → MCP tools
- `atlassian.spaces.types.ts` → Data models

**Java Components:**
- `ConfluenceSpacesClient` → API integration
- `ConfluenceSpacesService` → Business logic
- `SpacesToolsConfiguration` → MCP tool definitions
- `Space`, `SpaceDetails` → Data models

## Configuration Strategy

**TypeScript (Current):**
- Environment variables via dotenv
- Global config file at `~/.mcp/configs.json`
- Runtime config loading

**Java (Target):**
- Spring Boot application.properties
- Environment variable binding with @Value
- @ConfigurationProperties for structured config
- Profile-based configuration

## Key Technology Mappings

| TypeScript | Java Spring Boot |
|------------|------------------|
| @modelcontextprotocol/sdk | Spring AI MCP Server WebMVC |
| node-fetch | WebClient / RestTemplate |
| dotenv | @ConfigurationProperties |
| commander (CLI) | Spring Boot CommandLineRunner |
| jest | JUnit 5 + Mockito |
| turndown (HTML→MD) | CommonMark or similar |
| zod validation | Jakarta Validation |
