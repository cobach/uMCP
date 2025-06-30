# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project: uMCP (ultraMCP)

uMCP is a lightweight framework for building MCP (Model Context Protocol) servers in Java. It provides a simple abstraction layer over the official MCP Java SDK.

## Build and Development Commands

- **Build the project**: `./gradlew build`
- **Run the application**: `./gradlew run`
- **Run unit tests**: `./gradlew test`
- **Clean build**: `./gradlew clean build`
- **Publish to local Maven**: `./gradlew publishLocal`

## Project Structure

```
src/main/java/org/gegolabs/mcp/    # Note: Changed from mcp1 to mcp
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

- MCP Java SDK: `io.modelcontextprotocol.sdk:mcp:0.10.1-SNAPSHOT` (local build with bug fixes)
- Lombok: For reducing boilerplate code
- Gson: JSON serialization
- Logback: Logging framework
- JSON Schema Generator: Dynamic schema generation

## Known Issues

### MCP Java SDK Bugs (as of 2025-01-30)

1. **Timeout Issue (PR #350)**: The SDK has a bug where responses timeout due to incorrect operator ordering in reactive streams
2. **CallToolRequest Deserialization (PR #355)**: The SDK fails to deserialize tool call parameters correctly

**Status**: Both bugs have been reported and PRs submitted to the official repository. We're using a local build (0.10.1-SNAPSHOT) with these fixes applied until they're merged upstream.

## Next Steps

Once the SDK bugs are fixed:
1. Add TCP transport support
2. Implement more complex tool examples
3. Add resource and prompt support
4. Create comprehensive documentation
5. Publish to Maven Central

## Testing

### Unit Tests

The project includes unit tests for tool implementations. Run with:
```bash
./gradlew test
```

### Integration Testing with Node.js SDK

For comprehensive integration testing, use the Node.js MCP SDK test client:

1. **Navigate to test client directory**:
   ```bash
   cd test-clients/node-sdk
   ```

2. **Run the test client**:
   ```bash
   node test-umcp-with-sdk.js
   ```

This test client:
- Connects to the uMCP server via stdio
- Lists available tools
- Calls each tool and verifies responses
- Validates that SDK bug fixes are working (no timeouts, proper deserialization)

**Important**: Always use this Node.js SDK test client for integration testing instead of manually sending JSON messages. The SDK client properly handles the MCP protocol, connection lifecycle, and response parsing.

### Test Output Example

```
Testing uMCP server with Node.js SDK...

1. Connecting to uMCP server...
✓ Connected successfully

2. Listing available tools...
✓ Tools received: 2
  - domain-availability
  - SystemInformation

3. Testing SystemInformation tool...
✓ SystemInformation result: [system info output]

✅ All tests completed successfully!
✅ No timeout errors (PR #350 fix verified)
✅ Tool calls work correctly (PR #355 fix verified)
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