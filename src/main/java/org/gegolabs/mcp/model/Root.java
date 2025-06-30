package org.gegolabs.mcp.model;

/**
 * (Servicio del Cliente)
 * The Model Context Protocol (MCP) provides a standardized way for clients to expose filesystem “roots” to servers. Roots define the boundaries of where servers can operate within the filesystem, allowing them to understand which directories and files they have access to. Servers can request the list of roots from supporting clients and receive notifications when that list changes.
 *
 * Ejemplo:
 *
 * {
 *   "jsonrpc": "2.0",
 *   "id": 1,
 *   "result": {
 *     "roots": [
 *       {
 *         "uri": "file:///home/user/projects/myproject",
 *         "name": "My Project"
 *       }
 *     ]
 *   }
 * }
 *
 * [
 *   {
 *     "uri": "file:///home/user/repos/frontend",
 *     "name": "Frontend Repository"
 *   },
 *   {
 *     "uri": "file:///home/user/repos/backend",
 *     "name": "Backend Repository"
 *   }
 * ]
 *
 * REF: https://modelcontextprotocol.io/specification/2024-11-05/client/roots
 */
public class Root {
}
