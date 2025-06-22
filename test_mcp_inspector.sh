#!/bin/bash

# Create a test script that simulates MCP inspector behavior
(
# Send initialize request
echo '{"method":"initialize","params":{"protocolVersion":"2024-11-05","capabilities":{}},"jsonrpc":"2.0","id":0}'

# Wait for initialization
sleep 1

# List tools
echo '{"method":"tools/list","jsonrpc":"2.0","id":1}'

# Wait for tool list
sleep 1

# Call DomainAvailability tool with digitalmatrix.com
echo '{"method":"tools/call","params":{"name":"DomainAvailability","arguments":{"value":"digitalmatrix.com"}},"jsonrpc":"2.0","id":2}'

# Wait for response
sleep 15

) | timeout 30s java -jar build/libs/ultraRAG-1.0-SNAPSHOT.jar 2>&1