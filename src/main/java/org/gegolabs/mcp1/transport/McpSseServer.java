package org.gegolabs.mcp1.transport;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.*;
import org.gegolabs.mcp1.MCPServer;
//import org.eclipse.jetty.server.Server;


public class McpSseServer {
    private final Server server;

    public McpSseServer(MCPServer mcpServer) throws Exception {
        server = new Server(8080);
        Connector connector = new ServerConnector(server);
        server.addConnector(connector);

        // Add the CrossOriginHandler to protect from CSRF attacks.
        /*CrossOriginHandler crossOriginHandler = new CrossOriginHandler();
        crossOriginHandler.setAllowedOriginPatterns(Set.of("http://domain.com"));
        crossOriginHandler.setAllowCredentials(true);
        server.setHandler(crossOriginHandler);*/



        // Create a ServletContextHandler with contextPath.
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        // Link the context to the server.
        //crossOriginHandler.setHandler(context);
        server.setHandler(context);



        /*//*****************************************************************
        // Create MCP SSE transport provider
        HttpServletSseServerTransportProvider transportProvider =
                new HttpServletSseServerTransportProvider(new ObjectMapper(), "/", "/sse");


        // Create MCP synchronous server
        McpSyncServer syncServer = McpServer.sync(transportProvider)
                .serverInfo("my-mcp-server", "1.0.0")
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true) // Indicate support for tools
                        .build())
                .build();

        //******************************************************************/

        // Register the MCP servlet with the server
        ServletHolder servletHolder = context.addServlet(mcpServer.getHttpServlet(),"/sse/*");

        // Configure the Servlet with init-parameters.
        //servletHolder.setInitParameter("maxItems", "128");


    }

    public void start() throws Exception {
        server.start();
    }

    public void shutdown() throws Exception {
        server.stop();
    }
}
