package org.gegolabs.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import org.gegolabs.mcp.bridge.McpBridge;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.gegolabs.mcp.protocol.Capability;
import org.gegolabs.mcp.protocol.CapabilityException;

import java.util.ArrayList;
import java.util.List;

/**
 * Server implementation for the Model Context Protocol (MCP).
 * This class manages the MCP server lifecycle and tool registration.
 * It always uses TCP transport via mcp-java-bridge.
 * 
 * Use the builder() method to create a new instance with the builder pattern.
 */
@Slf4j
@Builder(builderClassName = "Builder")
public class MCPServer {
    
    /**
     * Default TCP port for MCP server
     */
    private static final int DEFAULT_PORT = 3000;
    
    /**
     * TCP port for the server
     */
    @Builder.Default
    private int port = DEFAULT_PORT;
    
    /**
     * Host to bind the server to
     */
    @Builder.Default  
    private String host = "localhost";

    /**
     * List of capability tools to be registered with the server.
     * Each tool provides a specific functionality to the MCP server.
     */
    @Singular
    private List<Capability> tools;

    /**
     * The name of the MCP server.
     * Used for identification in logs and client communications.
     */
    private String name;

    /**
     * The version of the MCP server.
     * Used for compatibility checking and identification.
     */
    private String version;

    /**
     * The async server instance.
     * Created when start() is called.
     */
    private McpAsyncServer mcpAsyncServer;

    /**
     * List of tool containers created for this server.
     * Used to manage the lifecycle of tools.
     */
    @Builder.Default
    private List<ToolContainer> toolContainers = new ArrayList<>();
    
    /**
     * The transport provider - always TCP via bridge
     */
    private McpServerTransportProvider transport;

    /**
     * Starts the MCP asynchronous server. If the server is not already initialized, it sets up
     * the server instance with TCP transport via mcp-java-bridge.
     * Registers all tools synchronously before returning.
     *
     */
    public void start() throws Exception {
        if (mcpAsyncServer == null) {
            // Always use TCP transport via mcp-java-bridge
            transport = McpBridge.tcpTransport(host, port);
            log.info("Starting uMCP server on {}:{} via mcp-java-bridge", host, port);
            
            mcpAsyncServer = McpServer.async(transport)
                    .serverInfo(name, version)
                    .capabilities(McpSchema.ServerCapabilities.builder()
                            .resources(true, true)     // Enable resource support. listChanged indicates whether the server will emit notifications when the list of available tools changes.
                            .tools(true)               // Enable tool support
                            .prompts(true)             // Enable prompt support
                            .logging()                 // Enable logging support
                            .build())
                    .build();

            // Register tools synchronously before returning
            for (Capability tool : tools) {
                try {
                    ToolContainer toolContainer = ToolContainer.builder().tool(tool).build();
                    toolContainers.add(toolContainer);

                    // Get the tool specification without initializing the tool
                    McpServerFeatures.AsyncToolSpecification toolSpec = toolContainer.getUninitializedAsyncToolSpecification();
                    mcpAsyncServer.addTool(toolSpec)
                            .doOnSuccess(v -> {
                                log.info("Tool registered successfully: {}", tool.getClass().getSimpleName());
                                try {
                                    // Initialize the tool after it has been successfully registered
                                    toolContainer.initialize();
                                } catch (CapabilityException e) {
                                    log.error("Failed to initialize tool: {}", tool.getClass().getSimpleName(), e);
                                }
                            })
                            .doOnError(e -> log.error("Failed to register tool: {}", tool.getClass().getSimpleName(), e))
                            .block(); // Block to ensure tool is registered
                } catch (Exception e) {
                    log.error("Exception registering tool: {}", tool.getClass().getSimpleName(), e);
                }
            }
            
            log.info("uMCP server started successfully");
            log.info("To connect with Claude Desktop:");
            log.info("1. Download mcp-java-bridge connector JAR");
            log.info("2. Configure claude_desktop_config.json to use the connector");
            log.info("3. Point the connector to {}:{}", host, port);
        }
    }

    /**
     * Gracefully closes the MCP server and shuts down all tools.
     */
    public void close() {
        // Shutdown all tools
        for (ToolContainer toolContainer : toolContainers) {
            try {
                toolContainer.shutdown();
            } catch (CapabilityException e) {
                log.error("Error shutting down tool: {}", e.getMessage(), e);
            }
        }

        // Close the server
        if (mcpAsyncServer != null) {
            mcpAsyncServer.closeGracefully()
                    .doOnSuccess(v -> log.info("Server closed"))
                    .subscribe();
        }
    }
}