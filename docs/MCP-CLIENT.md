Based on the documentation, here's a comprehensive guide for writing an MCP client in Java Spring Boot:

MCP Client Implementation Guide - Java Spring Boot

Key Components

1. Dependencies (pom.xml)
   <dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-starter-mcp-client</artifactId>
   </dependency>
   <dependency>
   <groupId>org.springframework.ai</groupId>
   <artifactId>spring-ai-starter-model-anthropic</artifactId>
   </dependency>

2. Configuration (application.yml)
   spring:
   ai:
   mcp:
   client:
   enabled: true
   name: your-client-name
   version: 1.0.0
   type: SYNC  # or ASYNC
   request-timeout: 20s
   stdio:
   root-change-notification: true
   servers-configuration: classpath:/mcp-servers-config.json
   toolcallback:
   enabled: true

3. MCP Server Configuration (mcp-servers-config.json)
   {
   "mcpServers": {
   "server-name": {
   "command": "npx",
   "args": ["-y", "@modelcontextprotocol/server-name"],
   "env": {
   "API_KEY": "your-api-key"
   }
   }
   }
   }

4. Client Implementation Options

Option A: Using Spring AI Auto-Configuration
- Automatically creates McpClient based on configuration
- Integrates with Spring AI's ChatClient
- Tool callbacks automatically registered

Option B: Manual Client Creation
// Create transport
ServerParameters params = ServerParameters.builder("npx")
.args("-y", "@modelcontextprotocol/server-name")
.build();
McpTransport transport = new StdioClientTransport(params);

// Create sync client
McpSyncClient client = McpClient.sync(transport)
.requestTimeout(Duration.ofSeconds(10))
.capabilities(ClientCapabilities.builder()
.roots(true)
.sampling()
.build())
.build();

// Initialize
client.initialize();

// Use tools
ListToolsResult tools = client.listTools();
CallToolResult result = client.callTool(
new CallToolRequest("tool-name", Map.of("param", "value"))
);

5. Transport Options
- STDIO: Process-based communication
- SSE (Server-Sent Events): HTTP-based, recommended for production
- For WebFlux: Use spring-ai-mcp-client-webflux-spring-boot-starter

6. Client Capabilities
- Roots: Filesystem access boundaries
- Sampling: LLM interaction support
- Logging: Server log message handling
- Tools: Function execution
- Resources: Data access
- Prompts: Template interactions

7. Integration with Spring AI ChatClient
   var chatClient = chatClientBuilder
   .defaultSystem("System prompt")
   .defaultToolCallbacks((Object[]) mcpToolAdapter.toolCallbacks())
   .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
   .build();

The MCP client acts as a bridge between your Spring Boot application and MCP servers, handling protocol negotiation, message transport, and capability management automatically
through Spring's dependency injection and auto-configuration features.