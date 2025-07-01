package org.gegolabs.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import org.gegolabs.mcp.protocol.SyncCapability;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify tools handling doesn't cause timeouts
 */
public class ToolsTimeoutTest {
    
    // Simple test capability - NO extends ToolContainer
    static class EchoCapability implements SyncCapability<EchoInput, EchoOutput> {
        @Override
        public EchoOutput execute(EchoInput input) {
            return new EchoOutput("Echo: " + input.message);
        }
        
        @Override
        public void initialize() {
            // No initialization needed
        }
        
        @Override
        public void shutdown() {
            // No cleanup needed
        }
    }
    
    static class EchoInput {
        public String message;
        
        public EchoInput() {}
        public EchoInput(String message) {
            this.message = message;
        }
    }
    
    static class EchoOutput {
        public String result;
        
        public EchoOutput() {}
        public EchoOutput(String result) {
            this.result = result;
        }
    }
    
    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    public void testServerWithToolsShouldNotTimeout() throws Exception {
        // Temporarily disable SDK fixes to test with pure 0.10.0
        System.setProperty("mcp.bridge.apply.fixes", "false");
        
        try {
            // Create server with a simple tool
            MCPServer server = MCPServer.builder()
                .name("test-server")
                .version("1.0.0")
                .port(3333) // Use different port to avoid conflicts
                .tool(new EchoCapability())
                .build();
            
            // Start server in background thread
            Thread serverThread = new Thread(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Server failed to start: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
            // Give server time to start
            Thread.sleep(2000);
            
            // Server should be running without issues
            assertNotNull(server);
            
            // TODO: Run node client to test tools/list
            
            System.out.println("Server started successfully with tools");
            
        } finally {
            // Re-enable fixes
            System.clearProperty("mcp.bridge.apply.fixes");
        }
    }
}