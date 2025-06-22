package org.gegolabs.mcp1;

import org.gegolabs.mcp1.protocol.Capability;
import org.gegolabs.mcp1.protocol.CapabilityException;
import org.gegolabs.mcp1.protocol.SyncCapability;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the lifecycle management of capabilities.
 */
class CapabilityLifecycleTest {

    /**
     * Test that initialize is called when getAsyncToolSpecification is called.
     */
    @Test
    void testInitializeCalledOnGetAsyncToolSpecification() throws CapabilityException {
        // Create a mock capability that tracks initialization
        MockCapability mockCapability = new MockCapability();
        
        // Create a tool container with the mock capability
        ToolContainer toolContainer = ToolContainer.builder()
                .tool(mockCapability)
                .build();
        
        // Verify that the capability is not initialized yet
        assertFalse(mockCapability.isInitialized(), "Capability should not be initialized before getAsyncToolSpecification is called");
        
        // Get the tool specification which should call initialize
        var toolSpec = toolContainer.getAsyncToolSpecification();
        
        // Verify that the capability is now initialized
        assertTrue(mockCapability.isInitialized(), "Capability should be initialized after getAsyncToolSpecification is called");
    }
    
    /**
     * Test that shutdown is called when the shutdown method is called.
     */
    @Test
    void testShutdownCalledOnShutdown() throws CapabilityException {
        // Create a mock capability that tracks shutdown
        MockCapability mockCapability = new MockCapability();
        
        // Create a tool container with the mock capability
        ToolContainer toolContainer = ToolContainer.builder()
                .tool(mockCapability)
                .build();
        
        // Initialize the capability
        toolContainer.getAsyncToolSpecification();
        
        // Verify that the capability is not shut down yet
        assertFalse(mockCapability.isShutDown(), "Capability should not be shut down before shutdown is called");
        
        // Call shutdown
        toolContainer.shutdown();
        
        // Verify that the capability is now shut down
        assertTrue(mockCapability.isShutDown(), "Capability should be shut down after shutdown is called");
    }
    
    /**
     * Test that initialize is only called once even if getAsyncToolSpecification is called multiple times.
     */
    @Test
    void testInitializeCalledOnlyOnce() throws CapabilityException {
        // Create a mock capability that counts initialization calls
        MockCapability mockCapability = new MockCapability();
        
        // Create a tool container with the mock capability
        ToolContainer toolContainer = ToolContainer.builder()
                .tool(mockCapability)
                .build();
        
        // Get the tool specification multiple times
        toolContainer.getAsyncToolSpecification();
        toolContainer.getAsyncToolSpecification();
        toolContainer.getAsyncToolSpecification();
        
        // Verify that initialize was only called once
        assertEquals(1, mockCapability.getInitializeCount(), "Initialize should only be called once");
    }
    
    /**
     * Test that shutdown is only called once even if shutdown is called multiple times.
     */
    @Test
    void testShutdownCalledOnlyOnce() throws CapabilityException {
        // Create a mock capability that counts shutdown calls
        MockCapability mockCapability = new MockCapability();
        
        // Create a tool container with the mock capability
        ToolContainer toolContainer = ToolContainer.builder()
                .tool(mockCapability)
                .build();
        
        // Initialize the capability
        toolContainer.getAsyncToolSpecification();
        
        // Call shutdown multiple times
        toolContainer.shutdown();
        toolContainer.shutdown();
        toolContainer.shutdown();
        
        // Verify that shutdown was only called once
        assertEquals(1, mockCapability.getShutdownCount(), "Shutdown should only be called once");
    }
    
    /**
     * A mock capability that tracks initialization and shutdown.
     */
    private static class MockCapability implements SyncCapability<String, String> {
        private boolean initialized = false;
        private boolean shutDown = false;
        private int initializeCount = 0;
        private int shutdownCount = 0;
        
        @Override
        public void initialize() throws CapabilityException {
            initialized = true;
            initializeCount++;
        }
        
        @Override
        public void shutdown() throws CapabilityException {
            shutDown = true;
            shutdownCount++;
        }
        
        @Override
        public String execute(String input) throws CapabilityException {
            return "Mock result";
        }
        
        public boolean isInitialized() {
            return initialized;
        }
        
        public boolean isShutDown() {
            return shutDown;
        }
        
        public int getInitializeCount() {
            return initializeCount;
        }
        
        public int getShutdownCount() {
            return shutdownCount;
        }
    }
}