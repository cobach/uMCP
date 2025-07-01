package org.gegolabs.mcp;

import org.gegolabs.mcp.protocol.SyncCapability;

public class TestServerWithEcho {
    
    // Echo capability
    static class EchoCapability implements SyncCapability<EchoInput, EchoOutput> {
        @Override
        public EchoOutput execute(EchoInput input) {
            return new EchoOutput("Echo: " + input.message);
        }
        
        @Override
        public void initialize() {
            System.out.println("EchoCapability initialized");
        }
        
        @Override
        public void shutdown() {
            System.out.println("EchoCapability shutdown");
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
    
    public static void main(String[] args) throws Exception {
        // Disable fixes to test with pure SDK 0.10.0
        System.setProperty("mcp.bridge.apply.fixes", "false");
        
        System.out.println("Starting test server with SDK fixes DISABLED...");
        
        MCPServer server = MCPServer.builder()
            .name("test-server-echo")
            .version("1.0.0")
            .port(3333)
            .tool(new EchoCapability())
            .build();
            
        server.start();
        
        System.out.println("Server running on port 3333");
        System.out.println("Press Ctrl+C to stop");
        
        // Keep running
        Thread.currentThread().join();
    }
}