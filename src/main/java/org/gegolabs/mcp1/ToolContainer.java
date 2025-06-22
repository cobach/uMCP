package org.gegolabs.mcp1;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.gegolabs.mcp1.protocol.Capability;
import org.gegolabs.mcp1.protocol.CapabilityException;
import org.gegolabs.mcp1.protocol.Description;
import org.gegolabs.mcp1.protocol.Name;
import reactor.core.publisher.Mono;

/**
 * Container for MCP tools that provides conversion to AsyncToolSpecification.
 * This class wraps a Capability and handles the conversion to the format
 * required by the MCP server, including argument handling and execution.
 * 
 * Use the {@link #builder()} method to create a new instance with the builder pattern.
 */
@Slf4j
@Builder
public class ToolContainer{
    /**
     * The capability tool contained in this container.
     * This tool will be converted to an AsyncToolSpecification.
     */
    private Capability tool;

    /**
     * Flag indicating whether the tool has been initialized.
     */
    private boolean initialized = false;

    /**
     * Initializes the capability tool.
     * This method should be called before using the tool.
     *
     * @throws CapabilityException if initialization fails
     */
    public void initialize() throws CapabilityException {
        if (!initialized) {
            log.info("Initializing tool: {}", getToolName());
            tool.initialize();
            initialized = true;
        }
    }

    /**
     * Shuts down the capability tool.
     * This method should be called when the tool is no longer needed.
     *
     * @throws CapabilityException if shutdown fails
     */
    public void shutdown() throws CapabilityException {
        if (initialized) {
            log.info("Shutting down tool: {}", getToolName());
            tool.shutdown();
            initialized = false;
        }
    }

    /**
     * Creates and returns an AsyncToolSpecification for the contained tool.
     * Initializes the tool if it hasn't been initialized yet.
     * 
     * @return the AsyncToolSpecification for the tool, or null if the argument class cannot be determined
     * @throws CapabilityException if tool initialization fails
     */
    public McpServerFeatures.AsyncToolSpecification getAsyncToolSpecification() throws CapabilityException {
        // Initialize the tool if it hasn't been initialized yet
        initialize();

        return createAsyncToolSpecification();
    }

    /**
     * Creates and returns an AsyncToolSpecification for the contained tool without initializing it.
     * This method is used by MCPServer to get the specification before initializing the tool.
     * 
     * @return the AsyncToolSpecification for the tool, or null if the argument class cannot be determined
     * @throws CapabilityException if there is an error creating the tool specification
     */
    public McpServerFeatures.AsyncToolSpecification getUninitializedAsyncToolSpecification() throws CapabilityException {
        return createAsyncToolSpecification();
    }

    /**
     * Internal method to create an AsyncToolSpecification without initializing the tool.
     * 
     * @return the AsyncToolSpecification for the tool, or null if the argument class cannot be determined
     * @throws CapabilityException if there is an error creating the tool specification
     */
    private McpServerFeatures.AsyncToolSpecification createAsyncToolSpecification() throws CapabilityException {
        Class<?> toolExecuteArgumentClass = MiscTools.getToolExecuteArgumentClass(tool);
        if(toolExecuteArgumentClass == null){
            log.error("Could not determine the argument class for tool {}", tool.getClass().getCanonicalName());
            return null;
        }

        // Check if the tool expects a primitive type
        boolean isPrimitive = isPrimitiveOrWrapper(toolExecuteArgumentClass);

        // Generate schema - this will wrap primitives in objects
        McpSchema.JsonSchema schema = MiscTools.generateJsonSchema(toolExecuteArgumentClass);

        log.info("Tool schema: {}", schema);

        McpServerFeatures.AsyncToolSpecification asyncToolSpecification = new McpServerFeatures.AsyncToolSpecification(
                new McpSchema.Tool(getToolName(), getToolDescription(), schema),
                (exchange, arguments) -> {
                    log.info("Tool {} called with arguments: {}", getToolName(), arguments);

                    Object inputObject = null;
                    if (arguments != null) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();

                            // Handle primitive types specially
                            if (isPrimitive) {
                                // Extract the value from the wrapper object
                                // The schema wraps primitives in an object with a "value" property
                                Object value = arguments.get("value");
                                if (value == null) {
                                    // Try with the lowercase class name as property
                                    String propertyName = toolExecuteArgumentClass.getSimpleName().toLowerCase();
                                    value = arguments.get(propertyName);
                                }

                                if (value != null) {
                                    // Convert the value to the expected primitive type
                                    inputObject = objectMapper.convertValue(value, toolExecuteArgumentClass);
                                } else {
                                    log.error("Could not find value property in arguments for primitive type {}", toolExecuteArgumentClass.getName());
                                    return Mono.just(new McpSchema.CallToolResult("Missing value property for primitive type", true));
                                }
                            } else {
                                // For non-primitive types, convert normally
                                inputObject = objectMapper.convertValue(arguments, toolExecuteArgumentClass);
                            }
                        } catch (Exception e) {
                            log.error("Error deserializing arguments to {}: {}", toolExecuteArgumentClass.getName(), e.getMessage());
                            return Mono.just(new McpSchema.CallToolResult("Error en los argumentos: " + e.getMessage(), true));
                        }
                    } else {
                        // Si arguments es null y la clase espera Void, podemos continuar
                        if (toolExecuteArgumentClass == Void.class) {
                            inputObject = null;
                        } else {
                            return Mono.just(new McpSchema.CallToolResult("Se requieren argumentos para esta herramienta", true));
                        }
                    }


                    Object result = null;
                    try {
                        log.info("Executing tool {} with input: {}", getToolName(), inputObject);
                        result = tool.execute(inputObject);
                        log.info("Tool {} result: {}", getToolName(), result);

                        return Mono.just(new McpSchema.CallToolResult(result.toString(), false));

                    } catch (CapabilityException e) {
                        log.error("Tool {} execution failed with CapabilityException: {}", getToolName(), e.getMessage());
                        return Mono.just(new McpSchema.CallToolResult(e.getMessage(), true));
                    } catch (Exception e) {
                        log.error("Tool {} execution failed with unexpected exception", getToolName(), e);
                        return Mono.just(new McpSchema.CallToolResult("Unexpected error: " + e.getMessage(), true));
                    }
                }
        );
        return asyncToolSpecification;
    }

    /**
     * Gets the name of the tool.
     * First tries to get the name from the Name annotation.
     * If not available, falls back to the simple class name.
     * 
     * @return the tool name
     */
    private String getToolName(){
        // Try to get name from Name annotation
        Name name = tool.getClass().getAnnotation(Name.class);
        if (name != null && !name.value().isEmpty()) {
            return name.value();
        }

        // Fallback to simple class name if no annotation is present
        return tool.getClass().getSimpleName();
    }

    /**
     * Gets the description of the tool.
     * First tries to get the description from the Info annotation.
     * If not available, falls back to the canonical class name.
     * 
     * @return the tool description
     */
    private String getToolDescription(){
        // Try to get description from Info annotation
        Description description = tool.getClass().getAnnotation(Description.class);
        if (description != null && !description.value().isEmpty()) {
            return description.value();
        }

        // Fallback to canonical class name if no annotation is present
        return tool.getClass().getCanonicalName();
    }


    /**
     * Sends a logging message through the server exchange.
     *
     * @param exchange the server exchange to send the message through
     * @param message the message to send
     */
    private void sendMessage(McpAsyncServerExchange exchange, String message){
        Mono<Void> loggingNotification = exchange.loggingNotification(
                McpSchema.LoggingMessageNotification.builder()
                        .level(McpSchema.LoggingLevel.INFO)
                        .logger("custom-logger")
                        .data("Hola mundo ultraRAG!")
                        .build()

        );
        loggingNotification.block();
    }

    /**
     * Checks if a class is a primitive type or its wrapper.
     *
     * @param clazz the class to check
     * @return true if the class is a primitive type or its wrapper, false otherwise
     */
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == String.class || 
               clazz == Integer.class || 
               clazz == Long.class || 
               clazz == Double.class || 
               clazz == Float.class || 
               clazz == Boolean.class || 
               clazz == Character.class || 
               clazz == Byte.class || 
               clazz == Short.class;
    }
}
