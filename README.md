# uMCP (ultraMCP)

A lightweight framework for building MCP (Model Context Protocol) servers in Java. uMCP provides a simple abstraction layer over the official MCP Java SDK, making it easy to create and deploy MCP servers with minimal boilerplate.

## Features

- **Simple Abstractions**: Clean interfaces (`SyncCapability`, `AsyncCapability`) for implementing MCP tools
- **Multiple Transports**: Built-in support for stdio and SSE transports
- **Type Safety**: Automatic type handling and JSON schema generation
- **Builder Pattern**: Fluent API for server configuration
- **Minimal Dependencies**: Lightweight framework built on top of the official SDK

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
# Run with Gradle
./gradlew run

# Or run the JAR directly
java -jar build/libs/uMCP-1.0.2.jar
```

### Testing with MCP Inspector

```bash
# Run the server
./gradlew run

# In another terminal, start the MCP Inspector
npx @modelcontextprotocol/inspector java -jar build/libs/uMCP-1.0.2.jar
```

## Creating Tools

Tools in uMCP implement the `SyncCapability` or `AsyncCapability` interface:

```java
public class MyTool extends ToolContainer implements SyncCapability<MyInput, MyOutput> {
    @Override
    public MyOutput execute(MyInput input) {
        // Tool implementation
        return new MyOutput(result);
    }
}
```

## Project Structure

```
uMCP/
├── src/
│   ├── main/java/org/gegolabs/mcp1/
│   │   ├── protocol/          # Core abstractions
│   │   ├── impl/              # Tool implementations
│   │   └── transport/         # Transport providers
│   └── test/                  # Unit tests
├── test-clients/              # Test clients for validation
│   └── node-sdk/              # Node.js SDK test client
├── docs/                      # Additional documentation
└── build.gradle               # Build configuration
```

## Documentation

- [CLAUDE.md](docs/CLAUDE.md) - Development guidelines for Claude Code
- [TCP Transport Plan](docs/TCP_TRANSPORT_IMPLEMENTATION_PLAN.md) - Upcoming TCP transport implementation
- [Next Steps](docs/NEXT_STEPS.md) - Project roadmap

## Testing

The project includes test clients to validate MCP server functionality:

```bash
cd test-clients/node-sdk
npm install
npm test
```

## Publishing

To publish to local Maven repository:

```bash
./gradlew publishLocal
```

## License

[License information to be added]

## Contributing

[Contributing guidelines to be added]