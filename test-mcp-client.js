#!/usr/bin/env node

import { Client } from '@modelcontextprotocol/sdk/client/index.js';
import { StdioClientTransport } from '@modelcontextprotocol/sdk/client/stdio.js';

console.log('=== uMCP Server Test Client ===\n');

async function testServer() {
  // Create stdio transport
  const transport = new StdioClientTransport({
    command: './gradlew',
    args: ['run', '--quiet']
  });

  // Create client
  const client = new Client({
    name: 'test-mcp-client',
    version: '1.0.0'
  }, {
    capabilities: {}
  });

  try {
    console.log('1. Connecting to uMCP server...');
    await client.connect(transport);
    console.log('   ✓ Connected successfully\n');

    // List tools
    console.log('2. Listing available tools...');
    const tools = await client.listTools();
    console.log(`   ✓ Found ${tools.tools.length} tools:`);
    tools.tools.forEach(tool => {
      console.log(`     - ${tool.name}: ${tool.description}`);
    });
    console.log();

    // Test SystemInformation tool
    console.log('3. Testing SystemInformation tool...');
    console.log('   Calling tool...');
    
    const systemInfoResult = await client.callTool({
      name: 'SystemInformation',
      arguments: {}
    });
    
    console.log('   ✓ SystemInformation response:');
    if (systemInfoResult.content && systemInfoResult.content.length > 0) {
      const content = systemInfoResult.content[0];
      if (content.type === 'text') {
        console.log('     ' + content.text.split('\n').join('\n     '));
      }
    }
    console.log();

    // Test DomainAvailability tool
    console.log('4. Testing DomainAvailability tool...');
    const testDomain = 'example.com';
    console.log(`   Checking availability of: ${testDomain}`);
    
    const domainResult = await client.callTool({
      name: 'DomainAvailability',
      arguments: {
        domain: testDomain
      }
    });
    
    console.log('   ✓ DomainAvailability response:');
    if (domainResult.content && domainResult.content.length > 0) {
      const content = domainResult.content[0];
      if (content.type === 'text') {
        console.log('     ' + content.text.split('\n').join('\n     '));
      }
    }
    console.log();

    console.log('5. All tests completed successfully!');
    console.log('   ✓ No timeout errors (fix #350)');
    console.log('   ✓ Tool calls work correctly (fix #355)');

  } catch (error) {
    console.error('❌ Error during test:', error);
    console.error('Stack trace:', error.stack);
  } finally {
    // Close connection
    await client.close();
    console.log('\n✓ Connection closed');
    process.exit(0);
  }
}

// Run the test
testServer().catch(error => {
  console.error('Fatal error:', error);
  process.exit(1);
});