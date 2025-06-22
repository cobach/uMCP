#!/usr/bin/env python3
"""Test script for MCP server communication"""

import json
import subprocess
import sys
import time

def send_request(proc, request):
    """Send a JSON-RPC request to the MCP server via stdin"""
    request_str = json.dumps(request)
    print(f"Sending: {request_str}")
    proc.stdin.write(request_str + '\n')
    proc.stdin.flush()
    
    # Read response
    response_line = proc.stdout.readline()
    if response_line:
        response = json.loads(response_line)
        print(f"Received: {json.dumps(response, indent=2)}")
        return response
    else:
        print("No response received")
        return None

def main():
    # Start the MCP server
    print("Starting MCP server...")
    proc = subprocess.Popen(
        ['java', '-jar', 'build/libs/ultraRAG-1.0-SNAPSHOT.jar'],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        bufsize=1
    )
    
    # Wait a bit for server to start
    time.sleep(2)
    
    try:
        # Send initialize request
        print("\n1. Sending initialize request...")
        init_request = {
            "jsonrpc": "2.0",
            "method": "initialize",
            "params": {
                "protocolVersion": "2024-11-05",
                "capabilities": {},
                "clientInfo": {
                    "name": "test",
                    "version": "1.0"
                }
            },
            "id": 0
        }
        init_response = send_request(proc, init_request)
        
        # Wait a bit
        time.sleep(1)
        
        # List tools
        print("\n2. Listing tools...")
        list_tools_request = {
            "jsonrpc": "2.0",
            "method": "tools/list",
            "id": 1
        }
        tools_response = send_request(proc, list_tools_request)
        
        # Wait a bit
        time.sleep(1)
        
        # Call SystemInformation tool
        print("\n3. Calling SystemInformation tool...")
        call_tool_request = {
            "jsonrpc": "2.0",
            "method": "tools/call",
            "params": {
                "name": "SystemInformation",
                "arguments": {}
            },
            "id": 2
        }
        tool_response = send_request(proc, call_tool_request)
        
        # Wait a bit
        time.sleep(1)
        
        # Call DomainAvailability tool
        print("\n4. Calling DomainAvailability tool...")
        domain_tool_request = {
            "jsonrpc": "2.0",
            "method": "tools/call",
            "params": {
                "name": "DomainAvailability",
                "arguments": {
                    "value": "digitalmatrix.com"
                }
            },
            "id": 3
        }
        domain_response = send_request(proc, domain_tool_request)
        
        # Wait for any remaining output
        time.sleep(2)
        
        # Check stderr for any errors
        stderr_output = proc.stderr.read()
        if stderr_output:
            print(f"\nStderr output:\n{stderr_output}")
            
    finally:
        # Terminate the process
        print("\nTerminating server...")
        proc.terminate()
        proc.wait()

if __name__ == "__main__":
    main()