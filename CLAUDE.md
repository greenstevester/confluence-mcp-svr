# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application configured as an MCP (Model Context Protocol) Server for Confluence integration. The project uses Java 21 and Gradle as the build system.

## Build and Development Commands

### Building the project
```bash
./gradlew build
```

### Running the application
```bash
./gradlew bootRun
```

### Running tests
```bash
./gradlew test
```

### Running a specific test class
```bash
./gradlew test --tests "io.github.greenstevester.confluence_mcp_svr.ConfluenceMcpSvrApplicationTests"
```

### Clean build
```bash
./gradlew clean build
```

### Creating a bootable JAR
```bash
./gradlew bootJar
```

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Build Tool**: Gradle
- **MCP Integration**: Spring AI MCP Server (WebMVC variant)
- **Spring AI Version**: 1.0.1

### Package Structure
- Base package: `io.github.greenstevester.confluence_mcp_svr`
- Main application class: `ConfluenceMcpSvrApplication` - Standard Spring Boot application entry point

### Key Dependencies
- `spring-boot-starter-web`: REST API capabilities
- `spring-ai-starter-mcp-server-webmvc`: MCP server implementation using Spring WebMVC

### MCP Server Context
This project is set up as an MCP Server, which means it:
- Exposes tools, resources, and prompts that MCP clients can discover and use
- Handles JSON-RPC communication with MCP clients
- Can be connected to by applications like Claude Desktop or custom MCP clients

The server is currently in initial setup phase with the basic Spring Boot structure in place. Implementation of Confluence-specific MCP capabilities (tools for searching, reading, and managing Confluence content) needs to be added.

### Documentation References
- `/docs/MCP-CLIENT.md`: Contains comprehensive guide for implementing MCP clients in Java Spring Boot (for reference when implementing server-side capabilities that clients will consume)

## Development Notes

When implementing MCP server capabilities for Confluence:
1. Create service classes for Confluence API integration
2. Implement MCP tools using `@Tool` annotations for Confluence operations
3. Configure Confluence API credentials in application.properties
4. Consider implementing resources for Confluence spaces/pages access
5. Add appropriate error handling for Confluence API failures