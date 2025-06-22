package org.gegolabs.mcp1;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import lombok.Builder;
import lombok.Singular;
import lombok.extern.slf4j.Slf4j;
import org.gegolabs.mcp1.protocol.Capability;
import org.gegolabs.mcp1.protocol.CapabilityException;

import java.util.ArrayList;
import java.util.List;

/**
 * Server implementation for the Model Context Protocol (MCP).
 * This class manages the MCP server lifecycle and tool registration.
 * It provides a builder pattern for easy configuration.
 * 
 * Use the {@link #builder()} method to create a new instance with the builder pattern.
 */
@Slf4j
@Builder
public class MCPServer {
    /**
     * List of capability tools to be registered with the server.
     * Each tool provides a specific functionality to the MCP server.
     */
    @Singular
    private List<Capability> tools;

    /**
     * The transport provider for the MCP server.
     * Handles the communication between the server and clients.
     */
    private McpServerTransportProvider transportProvider;

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
     * Created when getMcpAsyncServer() is called.
     */
    McpAsyncServer mcpAsyncServer;

    /**
     * List of tool containers created for this server.
     * Used to manage the lifecycle of tools.
     */
    private List<ToolContainer> toolContainers = new ArrayList<>();

    /**
     * Starts the MCP asynchronous server. If the server is not already initialized, it sets up
     * the server instance with the provided transport provider, server information, and capabilities.
     * Registers all tools synchronously before returning the server instance.
     *
     * @return the initialized and started {@code McpAsyncServer} instance
     */
    public McpAsyncServer start(){
        if(mcpAsyncServer ==null){
            mcpAsyncServer = McpServer.async(transportProvider)
                    .serverInfo(name, version)
                    .capabilities(McpSchema.ServerCapabilities.builder()
                            .resources(true,true)     // Enable resource support. listChanged indicates whether the server will emit notifications when the list of available tools changes.
                            .tools(true)         // Enable tool support
                            .prompts(true)       // Enable prompt support
                            .logging()           // Enable logging support
                            .build())
                    .build();

            // Register tools synchronously before returning
            for(Capability tool:tools){
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


            /* asyncServer.addResource(asyncResourceSpecification)
                .doOnSuccess(v -> logger.info("Resource registered"))
                .subscribe();

            asyncServer.addPrompt(asyncPromptSpecification)
                    .doOnSuccess(v -> logger.info("Prompt registered"))
                    .subscribe();
            */


        }
        return mcpAsyncServer;
    }

    /**
     * Gracefully closes the MCP server and shuts down all tools.
     */
    public void close(){
        // Shutdown all tools
        for (ToolContainer toolContainer : toolContainers) {
            try {
                toolContainer.shutdown();
            } catch (CapabilityException e) {
                log.error("Error shutting down tool: {}", e.getMessage(), e);
            }
        }

        // Close the server
        mcpAsyncServer.closeGracefully()
                .doOnSuccess(v -> log.info("Server closed"))
                .subscribe();
    }

    /**
     * Converts a Capability to an AsyncToolSpecification.
     *
     * @param toolSpec the capability to convert
     * @return the corresponding AsyncToolSpecification
     * @throws CapabilityException if there is an error creating the tool specification
     */
    private McpServerFeatures.AsyncToolSpecification toolSpecificationToAsyncToolSpecification(Capability toolSpec) throws CapabilityException {
        ToolContainer toolContainer = ToolContainer.builder().tool(toolSpec).build();
        toolContainers.add(toolContainer);
        return toolContainer.getAsyncToolSpecification();
    }
}
