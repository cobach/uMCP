# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project: uMCP (ultraMCP)

uMCP is a lightweight framework for building MCP (Model Context Protocol) servers in Java. It provides a simple abstraction layer over the official MCP Java SDK.

## Build and Development Commands

- **Build the project**: `./gradlew build`
- **Run the application**: `./gradlew run`
- **Run tests**: `./gradlew test`
- **Clean build**: `./gradlew clean build`
- **Publish to local Maven**: `./gradlew publishLocal`

## Project Structure

```
src/main/java/org/gegolabs/mcp1/
├── StdioIntegrationApp.java   # Main entry point for stdio transport
├── SseIntegrationApp.java     # SSE transport example
├── MCPServer.java             # Core server builder and configuration
├── MiscTools.java             # Utility functions
├── ToolContainer.java         # Base class for tool implementations
├── protocol/                  # Core abstractions
│   ├── Capability.java        # Base capability interface
│   ├── SyncCapability.java    # Synchronous tool interface
│   ├── AsyncCapability.java   # Asynchronous tool interface
│   └── CapabilityException.java
├── impl/                      # Tool implementations
│   ├── DomainAvailability.java
│   └── SystemInformation.java
└── transport/                 # Transport implementations
    └── McpSseServer.java      # SSE server transport
```

## Architecture Patterns

1. **Capability-based Design**: Tools implement `SyncCapability<Input, Output>` or `AsyncCapability<Input, Output>`
2. **Builder Pattern**: MCPServer uses fluent builder for configuration
3. **Automatic Type Handling**: ToolContainer handles primitive type wrapping/unwrapping
4. **Transport Abstraction**: Supports stdio and SSE transports

## Key Dependencies

- MCP Java SDK: `io.modelcontextprotocol.sdk:mcp:0.10.0`
- Lombok: For reducing boilerplate code
- Gson: JSON serialization
- Logback: Logging framework
- JSON Schema Generator: Dynamic schema generation

## Known Issues

### MCP Java SDK Bugs (as of 2025-01-30)

1. **Timeout Issue (PR #350)**: The SDK has a bug where responses timeout due to incorrect operator ordering in reactive streams
2. **CallToolRequest Deserialization (PR #355)**: The SDK fails to deserialize tool call parameters correctly

**Status**: Both bugs have been reported and PRs submitted to the official repository. Until merged, the framework works correctly with stdio transport for basic operations.

## Next Steps

Once the SDK bugs are fixed:
1. Add TCP transport support
2. Implement more complex tool examples
3. Add resource and prompt support
4. Create comprehensive documentation
5. Publish to Maven Central

## Testing

The project includes unit tests for tool implementations. Run with:
```bash
./gradlew test
```

## Integration with Claude

To use uMCP servers with Claude Desktop:

1. Build the JAR: `./gradlew build`
2. Add to MCP configuration:
```json
{
  "mcpServers": {
    "umcp": {
      "command": "java",
      "args": ["-jar", "/path/to/uMCP-1.0.2.jar"]
    }
  }
}
```