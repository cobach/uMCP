package org.gegolabs.mcp1;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;

import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.gegolabs.mcp1.protocol.Capability;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.victools.jsonschema.generator.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class providing miscellaneous tools for the MCP server.
 * Contains methods for logging configuration, tool introspection, and JSON schema generation.
 */
@Slf4j
public class MiscTools {

    /**
     * Default constructor for MiscTools.
     * Private to prevent instantiation of this utility class.
     */
    private MiscTools() {
        // Utility class, no instantiation
    }

    /**
     * Initializes logging to write to a file.
     * 
     * @param logFileName the path to the log file
     */
    public static void initializeLogInFile(String logFileName) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setFile(logFileName);
        fileAppender.setEncoder(encoder);
        fileAppender.start();

        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(fileAppender);

        log.info("Logging configured to write to file: {}", logFileName);
    }

    /**
     * Determines the argument class for a tool's execute method.
     * 
     * @param tool the capability tool to analyze
     * @return the Class object representing the argument type, or null if it cannot be determined
     */
    public static Class<?> getToolExecuteArgumentClass(Capability<?, ?> tool) {
        // Get the generic interfaces implemented by the tool class
        Type[] genericInterfaces = tool.getClass().getGenericInterfaces();

        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericInterface;
                Type rawType = paramType.getRawType();

                // Check if this is the Capability interface or a subinterface
                if (rawType instanceof Class && Capability.class.isAssignableFrom((Class<?>) rawType)) {
                    // The first type parameter is the input type
                    Type inputType = paramType.getActualTypeArguments()[0];

                    if (inputType instanceof Class) {
                        return (Class<?>) inputType;
                    }
                }
            }
        }

        return null; // Could not determine the argument class
    }

    /**
     * Generates a JSON schema for the given class.
     * 
     * @param clazz the class to generate a schema for
     * @return the generated JSON schema, or null if the input class is null
     */
    public static McpSchema.JsonSchema generateJsonSchema(Class clazz){
        if(clazz != null) {
            try {
                // Check if the class is a primitive type, wrapper type, or String
                boolean isPrimitive = clazz.isPrimitive() || 
                                    clazz == String.class || 
                                    clazz == Integer.class || 
                                    clazz == Long.class || 
                                    clazz == Double.class || 
                                    clazz == Float.class || 
                                    clazz == Boolean.class || 
                                    clazz == Character.class || 
                                    clazz == Byte.class || 
                                    clazz == Short.class;

                if (isPrimitive) {
                    // For primitive types, wrap in an object schema with a single property
                    Map<String, Object> properties = new HashMap<>();

                    // Generate schema for the primitive type
                    SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON);
                    configBuilder.with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT);
                    configBuilder.without(Option.NONPUBLIC_NONSTATIC_FIELDS_WITHOUT_GETTERS);
                    configBuilder.without(Option.NONSTATIC_NONVOID_NONGETTER_METHODS);

                    // Add configuration for using camelCase in property names
                    configBuilder.forFields().withPropertyNameOverrideResolver(field -> {
                        String originalName = field.getName();
                        // Convert snake_case to camelCase if necessary
                        if (originalName.contains("_")) {
                            StringBuilder camelCase = new StringBuilder();
                            boolean capitalizeNext = false;
                            for (char c : originalName.toCharArray()) {
                                if (c == '_') {
                                    capitalizeNext = true;
                                } else {
                                    camelCase.append(capitalizeNext ? Character.toUpperCase(c) : c);
                                    capitalizeNext = false;
                                }
                            }
                            return camelCase.toString();
                        }
                        return originalName;
                    });

                    SchemaGeneratorConfig config = configBuilder.build();
                    SchemaGenerator generator = new SchemaGenerator(config);
                    JsonNode primitiveSchema = generator.generateSchema(clazz);

                    // Convert the primitive schema and add it as a property
                    properties.put("value", convertJsonNodeToMcpSchema(primitiveSchema));

                    // Create required array with the single property
                    List<String> required = new ArrayList<>();
                    required.add("value");

                    // Return an object schema with the primitive as a property
                    return new McpSchema.JsonSchema("object", properties, required, false, null, null);
                } else {
                    // For non-primitive types, generate schema normally
                    SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2019_09, OptionPreset.PLAIN_JSON);
                    configBuilder.with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT);
                    configBuilder.without(Option.NONPUBLIC_NONSTATIC_FIELDS_WITHOUT_GETTERS);
                    configBuilder.without(Option.NONSTATIC_NONVOID_NONGETTER_METHODS);

                    // Add configuration for using camelCase in property names
                    configBuilder.forFields().withPropertyNameOverrideResolver(field -> {
                        String originalName = field.getName();
                        // Convert snake_case to camelCase if necessary
                        if (originalName.contains("_")) {
                            StringBuilder camelCase = new StringBuilder();
                            boolean capitalizeNext = false;
                            for (char c : originalName.toCharArray()) {
                                if (c == '_') {
                                    capitalizeNext = true;
                                } else {
                                    camelCase.append(capitalizeNext ? Character.toUpperCase(c) : c);
                                    capitalizeNext = false;
                                }
                            }
                            return camelCase.toString();
                        }
                        return originalName;
                    });

                    SchemaGeneratorConfig config = configBuilder.build();
                    SchemaGenerator generator = new SchemaGenerator(config);
                    JsonNode jsonSchema = generator.generateSchema(clazz);

                    // Convert JsonNode to your McpSchema.JsonSchema format
                    // This would require a conversion method
                    McpSchema.JsonSchema schema = convertJsonNodeToMcpSchema(jsonSchema);

                    // Use the schema as before
                    return schema;
                }
            } catch (Exception e) {
                log.error("Failed to generate JSON schema for class: " + clazz.getName(), e);
                // Return a simple schema as fallback
                Map<String, Object> properties = new HashMap<>();
                return new McpSchema.JsonSchema("object", properties, null, true, null, null);
            }
        }
        return null;
    }

    /**
     * Converts a Jackson JsonNode (from victools) to a McpSchema.JsonSchema object.
     * This method handles the conversion of JSON Schema elements like type, properties,
     * required fields, etc.
     *
     * @param jsonNode The JsonNode to convert
     * @return A McpSchema.JsonSchema object representing the same schema
     */
    private static McpSchema.JsonSchema convertJsonNodeToMcpSchema(JsonNode jsonNode) {
        // Extract the type (default to "object" if not specified)
        String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "object";

        // Extract properties
        Map<String, Object> properties = new HashMap<>();
        if (jsonNode.has("properties") && jsonNode.get("properties").isObject()) {
            JsonNode propsNode = jsonNode.get("properties");
            propsNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode propNode = propsNode.get(fieldName);
                properties.put(fieldName, convertJsonNodeToMcpSchema(propNode));
            });
        }

        // Extract required fields
        List<String> required = null;
        if (jsonNode.has("required") && jsonNode.get("required").isArray()) {
            required = new ArrayList<>();
            JsonNode requiredNode = jsonNode.get("required");
            for (int i = 0; i < requiredNode.size(); i++) {
                required.add(requiredNode.get(i).asText());
            }
            // If the list is empty, set it to null
            if (required.isEmpty()) {
                required = null;
            }
        }

        // Extract definitions and other schema attributes into maps
        Map<String, Object> definitions = new HashMap<>();
        if (jsonNode.has("description")) {
            definitions.put("description", jsonNode.get("description").asText());
        }

        // Extract enum values and other schema attributes into maps
        Map<String, Object> schemaAttributes = new HashMap<>();
        if (jsonNode.has("enum") && jsonNode.get("enum").isArray()) {
            List<String> enumValues = new ArrayList<>();
            JsonNode enumNode = jsonNode.get("enum");
            for (int i = 0; i < enumNode.size(); i++) {
                enumValues.add(enumNode.get(i).asText());
            }
            if (!enumValues.isEmpty()) {
                schemaAttributes.put("enum", enumValues);
            }
        }

        // Extract additionalProperties
        // This is a simplification - in a real implementation, you might want to handle
        // the case where additionalProperties is a schema itself
        Boolean additionalProps = null;
        if (jsonNode.has("additionalProperties")) {
            JsonNode additionalPropsNode = jsonNode.get("additionalProperties");
            if (additionalPropsNode.isBoolean()) {
                additionalProps = additionalPropsNode.asBoolean();
            } else if (additionalPropsNode.isObject()) {
                // If it's an object, we'll just set additionalProps to true
                // since we can't pass a schema object as additionalProps
                additionalProps = true;

                // Optionally, you could log a warning here
                log.warn("Complex additionalProperties schema found but not supported - defaulting to true");
            }
        }

        // Create and return the McpSchema.JsonSchema
        return new McpSchema.JsonSchema(
                type,
                properties,
                required,
                additionalProps,
                definitions,
                schemaAttributes
        );
    }



}
