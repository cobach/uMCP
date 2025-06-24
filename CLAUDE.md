# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

- **Build the project**: `./gradlew build`
- **Run the application**: `./gradlew run` or `java -jar build/libs/ultraRAG-1.0-SNAPSHOT.jar`
- **Run tests**: `./gradlew test`
- **Run a single test**: `./gradlew test --tests "ClassName.methodName"`
- **Clean build**: `./gradlew clean build`

## Project Architecture

This is a Java-based MCP (Model Context Protocol) server implementation called **ultraRAG**. The project implements an MCP server with custom tools using the official MCP Java SDK.

### Project Structure

- **src/main/java/org/gegolabs/mcp1/**: Main MCP implementation
  - `Main.java`: Entry point using `McpAsyncServer` with stdio transport
  - `MCPServer.java`: Custom MCP server builder and configuration
  - `MiscTools.java`: Utility class for logging initialization
  - `ToolContainer.java`: Base container for tools
  - `impl/`: Tool implementations
    - `DomainAvailability.java`: Checks domain availability using whois
    - `SystemInformation.java`: Provides system information
  - `model/`: MCP protocol entities (Tools, Resources, Prompts, etc.)
  - `protocol/`: Capability interfaces and abstractions
    - `Capability.java`, `SyncCapability.java`, `AsyncCapability.java`: Tool execution interfaces
    - `CapabilityException.java`: Custom exception for capability failures
- **src/test/java/**: Unit tests
  - `DomainAvailabilityTest.java`: Tests for domain availability tool
  - `SystemInformationTest.java`: Tests for system information tool
- **logs/**: Runtime log files (created automatically)
- **application.log**: Main application log file

### Key Dependencies

- **MCP Java SDK**: `io.modelcontextprotocol.sdk:mcp:0.10.0` - Official MCP protocol implementation
- **Lombok**: `io.freefair.lombok:8.13.1` - Code generation for builders, getters, logging
- **Gson**: `com.google.code.gson:gson:2.10.1` - JSON serialization
- **Logback**: `ch.qos.logback:logback-classic:1.5.13` - Logging framework
- **JSON Schema Generator**: `com.github.victools:jsonschema-generator:4.31.1` - Dynamic schema generation
- **JUnit 5**: Testing framework
- **Mockito**: `org.mockito:mockito-core:5.8.0` - Mocking framework for tests

### Architecture Patterns

- **Builder Pattern**: Used in MCPServer for fluent configuration
- **Interface-based Design**: Tools implement `SyncCapability<Input, Output>` or `AsyncCapability<Input, Output>`
- **Reactive Programming**: Uses Reactor for async operations in the main server
- **Dependency Injection**: Tools are registered with the server through builder pattern
- **Primitive Type Handling**: ToolContainer automatically wraps/unwraps primitive types for MCP protocol compliance

### Implemented Tools

1. **DomainAvailability**
   - Checks if a domain name is available using the whois command
   - Input: Domain name (String)
   - Output: Boolean (true if available, false if taken)

2. **SystemInformation**
   - Provides system information including OS details, memory, and Java runtime
   - Input: Void (no parameters)
   - Output: String (formatted system report)

### Server Configuration

The MCP server is configured in `Main.java`:
- Uses stdio transport for communication
- Server name: "ultraRAG"
- Version: "0.1.0"
- Main class: `org.gegolabs.mcp1.StdioIntegrationApp`

### JVM Arguments Configuration

The project requires specific JVM arguments configured in `build.gradle` to handle reflection and access to internal JDK modules:

```
applicationDefaultJvmArgs = [
    '--add-opens', 'java.base/java.lang=ALL-UNNAMED',
    '--add-opens', 'java.base/java.lang.reflect=ALL-UNNAMED',
    '--add-opens', 'java.base/java.util=ALL-UNNAMED',
    '--add-opens', 'java.base/java.util.concurrent=ALL-UNNAMED',
    '--add-opens', 'java.base/java.io=ALL-UNNAMED',
    '--add-opens', 'java.base/java.net=ALL-UNNAMED',
    '--add-opens', 'java.base/java.nio=ALL-UNNAMED',
    '--add-opens', 'java.base/java.time=ALL-UNNAMED',
    '--add-opens', 'java.base/sun.nio.ch=ALL-UNNAMED',
    '--add-opens', 'java.base/sun.util.calendar=ALL-UNNAMED'
]
```

**Why these are required:**
- The MCP SDK and its dependencies (particularly Reactor and Jackson) use reflection to access private fields and methods
- Java 9+ enforces module encapsulation, blocking reflective access to internal JDK classes
- These `--add-opens` flags explicitly allow the application to access necessary internal APIs
- Without these flags, the application would fail at runtime with `InaccessibleObjectException` errors
- This is a common requirement for frameworks that use deep reflection (like JSON serialization libraries)

### Logging Configuration

- Log file is initialized in `Main.java` static block
- Logs are written to `application.log` in the project root
- Uses Logback through SLF4J facade

### Testing

The project includes unit tests for tool implementations:
- Tests use JUnit 5 and Mockito
- Process builders are mocked to test external command execution
- Run tests with `./gradlew test`

### MCP Integration with Claude Code

To use this MCP server with Claude Code:

1. **Build the JAR**: `./gradlew clean build`
2. **Configure Claude**: Add to `~/.config/claude/mcp_config.json`:
   ```json
   {
     "mcpServers": {
       "ultrarag": {
         "command": "java",
         "args": ["-jar", "/path/to/ultraRAG-1.0-SNAPSHOT.jar"]
       }
     }
   }
   ```
3. **Restart Claude Code** for changes to take effect

Note: The `README_CLAUDE_INTEGRATION.md` file contains additional integration details but may reference features (mcp2, mcp3) that are not yet implemented in the current codebase.

### Known Issues

#### MCP Tool Calls Timeout (as of 2025-01-19)

**Issue**: When using MCP Inspector or Claude Code to call tools, the server receives the requests but doesn't respond, resulting in timeout errors:
- Error: "MCP error -32001: Request timed out"
- Affects: `tools/list` and `tools/call` methods

**Status**:
- ✅ Server initializes correctly
- ✅ Tools are registered successfully (DomainAvailability, SystemInformation)
- ✅ Tool implementations work correctly (verified by unit tests)
- ❌ MCP SDK doesn't dispatch tool requests to handlers
- ❌ No responses are sent back through stdio transport

**Root Cause**: The MCP Java SDK v0.10.0 appears to have a bug where it receives tool-related JSON-RPC requests but fails to:
1. Execute the registered tool handlers
2. Send responses back through the stdio transport

**Workaround**: None currently available. Consider:
- Checking for newer versions of the MCP Java SDK
- Reporting the issue to the MCP SDK maintainers
- Implementing direct JSON-RPC message handling as a fallback