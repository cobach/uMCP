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

## Installation

### Prerequisites

- Java 17 or higher
- Gradle 8.5+ (for building from source)

### Using as a Dependency

Add uMCP to your project:

#### Maven
```xml
<dependency>
    <groupId>org.gegolabs</groupId>
    <artifactId>uMCP</artifactId>
    <version>1.1.0</version>
</dependency>
```

#### Gradle
```groovy
implementation 'org.gegolabs:uMCP:1.1.0'
```

**Note**: Currently available in Maven Local. Run `./gradlew publishToMavenLocal` after cloning.

### Building from Source

```bash
# Clone the repository
git clone https://github.com/cobach/uMCP.git
cd uMCP

# Build and install to local Maven repository
./gradlew publishToMavenLocal

# Run tests
./gradlew test
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

### Option 1: Direct JAR Configuration

When you build a server using uMCP, configure Claude Desktop to use the bridge JAR:

1. Edit `~/Library/Application Support/Claude/claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "my-server": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/mcp-java-bridge-1.0.0.jar",
        "--connector",
        "localhost",
        "3000"
      ]
    }
  }
}
```

2. Start your MCP server (built with uMCP) on port 3000
3. Restart Claude Desktop

### Option 2: Interactive Installation (Recommended)

Use the mcp-java-bridge JAR installer:

```bash
# Download mcp-java-bridge from https://github.com/cobach/mcp-java-bridge
java -jar mcp-java-bridge-1.0.0.jar
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
java -jar mcp-java-bridge-1.0.0.jar install \
  -n "my-server" \
  -c /path/to/mcp-java-bridge-1.0.0.jar \
  -h localhost \
  -p 3000
```

**Arguments:**
- `-n` - Server name in Claude Desktop (required)
- `-c` - Path to the mcp-java-bridge JAR
- `-h` - Server host (default: localhost)
- `-p` - Server port (default: 3000)

### Option 4: Script Wrapper

Create a simple script to wrap the connector command:

```bash
#!/bin/bash
# save as: my-server-connector.sh
java -jar /path/to/mcp-java-bridge-1.0.0.jar --connector localhost 3000
```

Then configure Claude Desktop:

```json
{
  "mcpServers": {
    "my-server": {
      "command": "/path/to/my-server-connector.sh"
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

## MCP Bridge JAR Usage

The mcp-java-bridge JAR (available at https://github.com/cobach/mcp-java-bridge) is a multi-purpose tool:

### 1. Interactive Installer (Default)

```bash
java -jar mcp-java-bridge-1.0.0.jar
```

### 2. Connector Mode

Used by Claude Desktop to bridge stdio↔TCP:

```bash
# Default settings (localhost:3000)
java -jar mcp-java-bridge-1.0.0.jar --connector

# Custom host/port
java -jar mcp-java-bridge-1.0.0.jar --connector 192.168.1.100 8080
```

### 3. Install Command

```bash
java -jar mcp-java-bridge-1.0.0.jar install -n <server-name> -c <jar-path> [-h <host>] [-p <port>]
```

### 4. Help

```bash
java -jar mcp-java-bridge-1.0.0.jar --help
```

## Creating Your MCP Server

### Step 1: Create a New Project

Create a new Gradle project with the following `build.gradle`:

```gradle
plugins {
    id 'java'
    id 'application'
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation 'org.gegolabs:uMCP:1.1.0'
}

application {
    mainClass = 'com.example.MyMCPServer'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}
```

### Step 2: Create Your Server Application

```java
package com.example;

import org.gegolabs.mcp.MCPServer;
import org.gegolabs.mcp.impl.DomainAvailability;
import org.gegolabs.mcp.impl.SystemInformation;

public class MyMCPServer {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 3000;
        
        MCPServer server = MCPServer.builder()
            .name("MyMCPServer")
            .version("1.0.0")
            .port(port)
            .tool(new DomainAvailability())
            .tool(new SystemInformation())
            .build();
            
        server.start();
        System.out.println("MCP Server running on port " + port);
        
        // Keep running until interrupted
        Thread.currentThread().join();
    }
}
```

### Step 3: Run Your Server

```bash
# Run with Gradle
./gradlew run

# Or build and run the JAR
./gradlew build
java -jar build/libs/your-project.jar 3000
```

## uMCP Library Structure

```
uMCP/
├── src/
│   ├── main/java/org/gegolabs/mcp/
│   │   ├── protocol/          # Core interfaces (SyncCapability, AsyncCapability)
│   │   ├── impl/              # Example tool implementations
│   │   ├── bridge/            # Bridge integration (from mcp-java-bridge)
│   │   └── MCPServer.java     # Main server builder class
│   └── test/                  # Unit tests
├── docs/                      # Additional documentation
└── build.gradle               # Build configuration
```

When you use uMCP in your project, it provides:
- **Core abstractions** for creating MCP tools
- **MCPServer builder** for easy server setup
- **Built-in TCP transport** via mcp-java-bridge
- **Example implementations** to reference

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

## Local Development

When developing with uMCP locally:

```bash
# Clone and install uMCP to local Maven repository
git clone https://github.com/cobach/uMCP.git
cd uMCP
./gradlew publishToMavenLocal
```

This installs uMCP to your local Maven repository (~/.m2/repository), making it available for your projects to use as a dependency.

## Migration from v1.0.x

If you're upgrading from uMCP 1.0.x:

1. **Transport changes**: Remove any `.transport_Stdio()` or `.transport_Sse()` calls - TCP is now default
2. **Port configuration**: Use `.port(3000)` instead of transport methods
3. **Bridge dependency**: Ensure mcp-java-bridge connector is installed for Claude Desktop

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

[Contributing guidelines to be added]