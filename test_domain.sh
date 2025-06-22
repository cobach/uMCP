#!/bin/bash

# Start the MCP server and send test requests
(
# Send initialize request
echo '{"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{},"clientInfo":{"name":"test","version":"1.0"}},"jsonrpc":"2.0","id":0}'

# Wait for initialization
sleep 1

# Send tool call request for DomainAvailability
echo '{"method":"tools/call","params":{"name":"DomainAvailability","arguments":{"value":"digitalmatrix.com"}},"jsonrpc":"2.0","id":1}'

# Wait for response
sleep 2

# Exit
) | java -jar build/libs/ultraRAG-1.0-SNAPSHOT.jar