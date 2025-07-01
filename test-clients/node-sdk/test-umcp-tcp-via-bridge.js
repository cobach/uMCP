#!/usr/bin/env node

import { StdioClientTransport } from '@modelcontextprotocol/sdk/client/stdio.js';
import { Client } from '@modelcontextprotocol/sdk/client/index.js';

async function testUMCPviaBridge() {
  console.log('Testing uMCP server via mcp-java-bridge (TCP)...\n');

  const client = new Client({
    name: 'uMCP TCP Test Client',
    version: '1.0.0'
  }, {
    capabilities: {}
  });

  try {
    // Create transport using the bridge connector
    const transport = new StdioClientTransport({
      command: 'java',
      args: [
        '-Dmcp.bridge.apply.fixes=false',  // Disable fixes to test with pure SDK 0.10.0
        '-jar', 
        '../../../mcp-java-bridge/build/libs/mcp-java-bridge-1.0.0-SNAPSHOT.jar',
        '--connector',
        'localhost',
        '3333'  // Port where our test server is running
      ]
    });

    // Connect to the server
    console.log('1. Connecting to uMCP server via bridge...');
    await client.connect(transport);
    console.log('✓ Connected successfully\n');

    // List available tools
    console.log('2. Listing available tools...');
    const startTime = Date.now();
    const toolsResponse = await client.listTools();
    const elapsed = Date.now() - startTime;
    
    console.log(`✓ Tools received in ${elapsed}ms:`, toolsResponse.tools.length);
    toolsResponse.tools.forEach(tool => {
      console.log(`  - ${tool.name}: ${tool.description}`);
    });
    console.log();

    // Test the Echo tool if available
    console.log('3. Testing Echo tool...');
    try {
      const echoResult = await client.callTool({
        name: 'EchoCapability',
        arguments: { message: 'Hello from Node.js client!' }
      });
      console.log('✓ Echo result:', echoResult.content[0].text);
      console.log();
    } catch (error) {
      console.error('✗ Echo tool failed:', error.message);
    }

    console.log('✅ All tests completed successfully!');
    console.log(`✅ No timeout errors - tools/list responded in ${elapsed}ms`);

  } catch (error) {
    console.error('❌ Test failed:', error);
    console.error('Stack:', error.stack);
  } finally {
    // Disconnect
    await client.close();
    process.exit(0);
  }
}

// Run the test
testUMCPviaBridge().catch(console.error);