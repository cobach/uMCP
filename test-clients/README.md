# Test Clients

This directory contains test clients for validating uMCP server functionality.

## Structure

```
test-clients/
├── node-sdk/          # Node.js SDK test client
│   ├── package.json
│   ├── node_modules/
│   └── test-umcp-with-sdk.js
└── README.md
```

## Node.js SDK Test Client

The official MCP Node.js SDK test client validates that uMCP works correctly with the reference implementation.

### Setup

```bash
cd test-clients/node-sdk
npm install
```

### Running Tests

From the `test-clients/node-sdk` directory:

```bash
node test-umcp-with-sdk.js
```

Or using npm:

```bash
npm test
```

### Requirements

- Node.js 18+
- uMCP JAR built (`gradle build` from project root)

## Future Test Clients

This structure allows for additional test clients:
- `python-sdk/` - Python SDK tests
- `tcp-client/` - Direct TCP protocol tests
- `stress-test/` - Performance and load testing