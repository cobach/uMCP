# uMCP (ultraMCP)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A lightweight framework for building MCP (Model Context Protocol) servers in Java. uMCP provides a simple abstraction layer over the official MCP Java SDK, making it easy to create and deploy MCP servers with TCP transport.

## Features

- **Simple Abstractions**: Clean interfaces (`SyncCapability`, `AsyncCapability`) for implementing MCP tools
- **TCP Transport by Default**: Always uses TCP transport via [mcp-java-bridge](https://github.com/cobach/mcp-java-bridge)
- **Type Safety**: Automatic type handling and JSON schema generation
- **Builder Pattern**: Fluent API for server configuration
- **Minimal Dependencies**: Lightweight framework built on top of the official SDK
- **Claude Desktop Ready**: Works seamlessly with Claude Desktop using the bridge connector

## Quick Start

### Prerequisites

- Java 17 or higher
- Gradle 8.5+ (optional, wrapper included)

### Building

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Create executable JAR
./gradlew jar
```

### Running

```bash
# Run with default port (3000)
./gradlew run

# Run with custom port
./gradlew run --args="8080"

# Or run the JAR directly
java -jar build/libs/uMCP-1.1.0.jar [port]
```

## Creating Tools

Tools in uMCP implement the `SyncCapability` or `AsyncCapability` interface:

```java
@Name("my-tool")
@Description("Description of what my tool does")
public class MyTool implements SyncCapability<MyInput, MyOutput> {
    @Override
    public void initialize() throws CapabilityException {
        // Initialize resources if needed
    }
    
    @Override
    public void shutdown() throws CapabilityException {
        // Clean up resources if needed
    }
    
    @Override
    public MyOutput execute(MyInput input) throws CapabilityException {
        // Tool implementation
        return new MyOutput(result);
    }
}
```

## Server Configuration

uMCP servers always use TCP transport. You can configure the host and port:

```java
// Default: localhost:3000
MCPServer server = MCPServer.builder()
    .name("MyServer")
    .version("1.0.0")
    .tool(new MyTool())
    .build();

// Custom port
MCPServer server = MCPServer.builder()
    .name("MyServer")
    .version("1.0.0")
    .port(8080)
    .tool(new MyTool())
    .build();

// Custom host and port
MCPServer server = MCPServer.builder()
    .name("MyServer")
    .version("1.0.0")
    .host("0.0.0.0")
    .port(8080)
    .tool(new MyTool())
    .tool(new AnotherTool())
    .build();

// Start the server
server.start();
```

## Claude Desktop Configuration

To connect your uMCP server with Claude Desktop, you have several options:

### Option 1: Using the Included Connector Script (Simplest)

After building uMCP, a connector script is created in `install/bin/`:

1. Edit `~/Library/Application Support/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "my-umcp-server": {
      "command": "/path/to/uMCP/install/bin/uMCP-connector",
      "args": ["localhost", "3000"]
    }
  }
}
```

2. Start your uMCP server on the configured port
3. Restart Claude Desktop

### Option 2: Interactive Installation (Recommended)

uMCP includes the mcp-java-bridge JAR which provides an interactive installer:

```bash
# After building uMCP, the bridge JAR is in install/lib/
cd /path/to/uMCP
java -jar install/lib/mcp-java-bridge-1.0.0.jar
```

This will:
- Auto-detect the JAR location
- Prompt for server name (e.g., "my-umcp-server")
- Prompt for host (default: localhost)
- Prompt for port (default: 3000)
- Automatically configure Claude Desktop
- Create a backup of existing configuration

### Option 3: Non-Interactive Installation

For automated setups, use specific parameters:

```bash
java -jar install/lib/mcp-java-bridge-1.0.0.jar install \
  -n "my-umcp-server" \
  -c /path/to/uMCP/install/bin/uMCP-connector \
  -h localhost \
  -p 3000
```

**Arguments:**
- `-n` - Server name in Claude Desktop (required)
- `-c` - Path to the connector script
- `-h` - Server host (default: localhost)
- `-p` - Server port (default: 3000)

### Option 4: Direct JAR Usage

You can also use the bridge JAR directly as the connector:

```json
{
  "mcpServers": {
    "my-umcp-server": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/uMCP/install/lib/mcp-java-bridge-1.0.0.jar",
        "--connector",
        "localhost",
        "3000"
      ]
    }
  }
}
```

### Verifying the Connection

1. **Start your uMCP server** on the configured port
2. **Restart Claude Desktop** to load the new configuration
3. **Check Claude Desktop logs** if the connection doesn't work:
   - On macOS: `~/Library/Logs/Claude/`
   - Look for connection errors or server startup issues

## MCP Bridge JAR Commands

The included mcp-java-bridge JAR is a multi-purpose tool with three modes:

### 1. Interactive Installer (Default)

Running without arguments starts an interactive installer:

```bash
java -jar install/lib/mcp-java-bridge-1.0.0.jar
```

### 2. Connector Mode

Used by Claude Desktop to bridge stdio↔TCP communication:

```bash
# With default settings (localhost:3000)
java -jar install/lib/mcp-java-bridge-1.0.0.jar --connector

# With custom host/port
java -jar install/lib/mcp-java-bridge-1.0.0.jar --connector 192.168.1.100 8080
```

**Note**: This mode is typically not run manually - it's executed by Claude Desktop.

### 3. Install Command

For non-interactive installation:

```bash
java -jar install/lib/mcp-java-bridge-1.0.0.jar install -n <server-name> -c <jar-path> [-h <host>] [-p <port>]
```

### Help Command

Display usage information:

```bash
java -jar install/lib/mcp-java-bridge-1.0.0.jar --help
```

## Example Application

```java
import org.gegolabs.mcp.MCPServer;
import org.gegolabs.mcp.impl.DomainAvailability;
import org.gegolabs.mcp.impl.SystemInformation;

public class MyApp {
    public static void main(String[] args) throws Exception {
        MCPServer server = MCPServer.builder()
            .name("MyMCPServer")
            .version("1.0.0")
            .port(3000)
            .tool(new DomainAvailability())
            .tool(new SystemInformation())
            .build();
            
        server.start();
        
        // Keep running until interrupted
        Thread.currentThread().join();
    }
}
```

## Project Structure

```
uMCP/
├── src/
│   ├── main/java/org/gegolabs/mcp/
│   │   ├── protocol/          # Core abstractions
│   │   ├── impl/              # Tool implementations
│   │   └── MCPServer.java     # Main server class
│   └── test/                  # Unit tests
├── docs/                      # Additional documentation
├── install/                   # Installation directory (after build)
│   ├── bin/
│   │   ├── uMCP              # Server launcher
│   │   └── uMCP-connector    # Bridge connector wrapper
│   └── lib/                   # All JARs including mcp-java-bridge.jar
└── build.gradle               # Build configuration
```

## Dependencies

- MCP Java SDK 0.10.0
- mcp-java-bridge 1.0.0
- Project Reactor (via SDK)
- SLF4J + Logback for logging
- Lombok for reducing boilerplate

## Testing with MCP Inspector

While MCP Inspector expects stdio transport, you can test your uMCP server using the bridge:

1. Start your uMCP server
2. Use the mcp-java-bridge connector as an intermediary
3. Point MCP Inspector to the connector

## Publishing

To publish to local Maven repository:

```bash
./gradlew publishLocal
```

Then use in other projects:

```gradle
dependencies {
    implementation 'org.gegolabs:uMCP:1.1.0'
}
```

## Migration from v1.0.x

If you're upgrading from uMCP 1.0.x:

1. **Transport changes**: Remove any `.transport_Stdio()` or `.transport_Sse()` calls - TCP is now default
2. **Port configuration**: Use `.port(3000)` instead of transport methods
3. **Bridge dependency**: Ensure mcp-java-bridge connector is installed for Claude Desktop

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

[Contributing guidelines to be added]