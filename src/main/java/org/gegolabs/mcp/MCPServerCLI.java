package org.gegolabs.mcp;

import lombok.extern.slf4j.Slf4j;
import org.gegolabs.mcp.impl.DomainAvailability;
import org.gegolabs.mcp.impl.SystemInformation;

/**
 * Main application class for the uMCP server.
 * This class initializes and starts the MCP server with TCP transport.
 * 
 * The class provides a default constructor.
 */
@Slf4j
public class MCPServerCLI {

    /**
     * Static initializer block that sets up logging to a file.
     */
    static{
        MiscTools.initializeLogInFile("logs/umcp.log");
    }

    /**
     * Main entry point for the application.
     * Starts the MCP server on default TCP port 3000.
     *
     * @param args command line arguments - optionally specify port
     */
    public static void main(String[] args) throws Exception {
        int port = 3000;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                log.warn("Invalid port number: {}, using default 3000", args[0]);
            }
        }
        
        MCPServer mcpServer = MCPServer.builder()
                .name("uMCP")
                .version("1.1.0")
                .port(port)  // TCP transport is now default
                .tool(new DomainAvailability())
                .tool(new SystemInformation())
                .build();
        // Create an async server with custom configuration
        mcpServer.start();

        // Keep the server running
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down MCP server...");
            mcpServer.close();
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
