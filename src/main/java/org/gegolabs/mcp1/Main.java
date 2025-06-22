package org.gegolabs.mcp1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import lombok.extern.slf4j.Slf4j;
import org.gegolabs.mcp1.impl.DomainAvailability;
import org.gegolabs.mcp1.impl.SystemInformation;

/**
 * Main application class for the MCP server.
 * This class initializes and starts the MCP server with the configured tools.
 * 
 * The class provides a default constructor.
 */
@Slf4j
public class Main {

    /**
     * Static initializer block that sets up logging to a file.
     */
    static{
        MiscTools.initializeLogInFile("logs/application.log");
    }


    /**
     * Gets the transport provider for the MCP server.
     * 
     * @return the configured transport provider
     */
    public static McpServerTransportProvider getTransportProvider() {
        return new StdioServerTransportProvider(new ObjectMapper());
    }



    /**
     * Main entry point for the application.
     * Starts the MCP server and keeps it running until interrupted.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        MCPServer ultraRAG = MCPServer.builder()
                .name("ultraRAG")
                .version("0.1.0")
                .transportProvider(getTransportProvider())
                .tool(new DomainAvailability())
                .tool(new SystemInformation())
                .build();
        // Create an async server with custom configuration
        McpAsyncServer asyncServer= ultraRAG.start();

        // Keep the server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down MCP server...");
            ultraRAG.close();
        }));

        // Block the main thread to keep the server alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            log.error("Server interrupted", e);
            Thread.currentThread().interrupt();
        }
    }



}
