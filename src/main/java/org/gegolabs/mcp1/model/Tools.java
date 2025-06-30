package org.gegolabs.mcp1.model;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import lombok.extern.slf4j.Slf4j;
import org.gegolabs.mcp1.MiscTools;
import org.gegolabs.mcp1.impl.SystemInformation;
import reactor.core.publisher.Mono;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Model Context Protocol (MCP) allows servers to expose tools that can be invoked by language models. Tools enable models to interact with external systems, such as querying databases, calling APIs, or performing computations. Each tool is uniquely identified by a name and includes metadata describing its schema.
 * Ejemplo:
 *
 *      {
 *         "name": "get_weather",
 *         "description": "Get current weather information for a location",
 *         "inputSchema": {
 *           "type": "object",
 *           "properties": {
 *             "location": {
 *               "type": "string",
 *               "description": "City name or zip code"
 *             }
 *           },
 *           "required": ["location"]
 *         }
 *       }
 */
@Slf4j
public class Tools {

    /**
     * Default constructor for Tools.
     * Private to prevent instantiation of this utility class.
     */
    private Tools() {
        // Utility class, no instantiation
    }

    /**
     * Gets all available tool specifications.
     * 
     * @return a list of all available AsyncToolSpecification objects
     */
    public static List<McpServerFeatures.AsyncToolSpecification> getAll() {
        return Collections.singletonList(getWorkstationTool());
    }

    /**
     * Gets the default tool specification.
     * 
     * @return the default AsyncToolSpecification object
     */
    public static McpServerFeatures.AsyncToolSpecification getDefault() {
        return getWorkstationTool();
    }

    /**
     * Creates and returns a tool specification for the workstation information tool.
     * This tool provides system information about the user's workstation.
     * 
     * @return an AsyncToolSpecification for the workstation information tool
     */
    public static McpServerFeatures.AsyncToolSpecification getWorkstationTool() {
        // Create JsonSchema for the tool
        JsonSchema schema = new JsonSchema(
                "object",
                new HashMap<>(),
                null,
                null,
                null,
                null
        );

        log.info("Tool schema: {}", schema);

        // Convert schema to JSON string
        String schemaJson;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            schemaJson = mapper.writeValueAsString(schema);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize schema", e);
        }

        McpServerFeatures.AsyncToolSpecification asyncToolSpecification = new McpServerFeatures.AsyncToolSpecification(
                new McpSchema.Tool("userWorkstationInfo", "User workstation information", schemaJson),
                (exchange, arguments) -> {

                    // Send a log message to clients
                    /*Mono<Void> loggingNotification = exchange.loggingNotification(
                            McpSchema.LoggingMessageNotification.builder()
                                    .level(McpSchema.LoggingLevel.INFO)
                                    .logger("custom-logger")
                                    .data("Hola mundo ultraRAG!")
                                    .build()
                            //);
                    );*/
                    //loggingNotification.block();

                    // Tool implementation
                    String result = SystemInformation.getSystemReport();
                    //log.info("Tool result: {}", result);
                    return Mono.just(new McpSchema.CallToolResult(result, false));
                }
        );
        return asyncToolSpecification;
    }

}
