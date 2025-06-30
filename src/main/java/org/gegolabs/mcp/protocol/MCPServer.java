package org.gegolabs.mcp.protocol;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark classes or methods that are part of the MCP server.
 * This annotation is used to identify components that participate in the
 * Model Context Protocol server implementation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface MCPServer {
}
