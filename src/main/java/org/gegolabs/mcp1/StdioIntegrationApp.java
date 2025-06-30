package org.gegolabs.mcp1;

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
public class StdioIntegrationApp {

    /**
     * Static initializer block that sets up logging to a file.
     */
    static{
        MiscTools.initializeLogInFile("logs/umcp-stdio.log");
    }

    /**
     * Main entry point for the application.
     * Starts the MCP server and keeps it running until interrupted.
     *
     *
     * Para probar: npx -y @modelcontextprotocol/inspector
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws Exception {
        MCPServer mcpServer = MCPServer.builder()
                .name("ultraRAG")
                .version("0.1.0")
                .transport_Stdio()
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
