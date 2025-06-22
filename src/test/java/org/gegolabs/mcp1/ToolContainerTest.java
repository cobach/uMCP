package org.gegolabs.mcp1;

import org.gegolabs.mcp1.impl.DomainAvailability;
import org.gegolabs.mcp1.impl.SystemInformation;
import org.gegolabs.mcp1.protocol.Capability;
import org.gegolabs.mcp1.protocol.CapabilityException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the ToolContainer class.
 */
class ToolContainerTest {

    /**
     * Test that getToolName returns the name from the Name annotation when present.
     */
    @Test
    void testGetToolNameWithNameAnnotation() throws CapabilityException {
        // Create a tool container with a capability that has a Name annotation
        Capability tool = new DomainAvailability();
        ToolContainer toolContainer = ToolContainer.builder()
                .tool(tool)
                .build();

        // Get the tool specification which will call getToolName internally
        var toolSpec = toolContainer.getAsyncToolSpecification();

        // Verify that the tool name is the value from the Name annotation
        assertEquals("domain-availability", toolSpec.tool().name());
    }

    /**
     * Test that getToolName returns the simple class name when no Name annotation is present.
     */
    @Test
    void testGetToolNameWithoutNameAnnotation() throws CapabilityException {
        // Create a tool container with a capability that doesn't have a Name annotation
        Capability tool = new SystemInformation();
        ToolContainer toolContainer = ToolContainer.builder()
                .tool(tool)
                .build();

        // Get the tool specification which will call getToolName internally
        var toolSpec = toolContainer.getAsyncToolSpecification();

        // Verify that the tool name is the simple class name
        assertEquals("SystemInformation", toolSpec.tool().name());
    }
}
