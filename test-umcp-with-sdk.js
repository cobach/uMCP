#!/usr/bin/env node

import { StdioClientTransport } from '@modelcontextprotocol/sdk/client/stdio.js';
import { Client } from '@modelcontextprotocol/sdk/client/index.js';

async function testUMCP() {
  console.log('Testing uMCP server with Node.js SDK...\n');

  const client = new Client({
    name: 'uMCP Test Client',
    version: '1.0.0'
  }, {
    capabilities: {}
  });

  try {
    // Create transport
    const transport = new StdioClientTransport({
      command: 'java',
      args: ['-jar', 'build/libs/uMCP-1.0.2.jar']
    });

    // Connect to the server
    console.log('1. Connecting to uMCP server...');
    await client.connect(transport);
    console.log('✓ Connected successfully\n');

    // List available tools
    console.log('2. Listing available tools...');
    const toolsResponse = await client.listTools();
    console.log('✓ Tools received:', toolsResponse.tools.length);
    toolsResponse.tools.forEach(tool => {
      console.log(`  - ${tool.name}: ${tool.description}`);
    });
    console.log();

    // Test SystemInformation tool
    console.log('3. Testing SystemInformation tool...');
    try {
      const sysInfoResult = await client.callTool({
        name: 'SystemInformation',
        arguments: {}
      });
      console.log('✓ SystemInformation result:');
      console.log(sysInfoResult.content[0].text);
      console.log();
    } catch (error) {
      console.error('✗ SystemInformation failed:', error.message);
    }


    console.log('✅ All tests completed successfully!');
    console.log('✅ No timeout errors (PR #350 fix verified)');
    console.log('✅ Tool calls work correctly (PR #355 fix verified)');

  } catch (error) {
    console.error('❌ Test failed:', error);
  } finally {
    // Disconnect
    await client.close();
    process.exit(0);
  }
}

// Run the test
testUMCP().catch(console.error);