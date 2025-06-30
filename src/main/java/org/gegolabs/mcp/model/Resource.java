package org.gegolabs.mcp.model;

/**
 * The Model Context Protocol (MCP) provides a standardized way for servers to expose resources to clients. Resources allow servers to share data that provides context to language models, such as files, database schemas, or application-specific information. Each resource is uniquely identified by a URI.
 *
 * Ejemplo:
 * {
 *   "jsonrpc": "2.0",
 *   "id": 1,
 *   "result": {
 *     "resources": [
 *       {
 *         "uri": "file:///project/src/main.rs",
 *         "name": "main.rs",
 *         "description": "Primary application entry point",
 *         "mimeType": "text/x-rust"
 *       }
 *     ],
 *     "nextCursor": "next-page-cursor"
 *   }
 * }
 *
 *
 * Request:
 * {
 *   "jsonrpc": "2.0",
 *   "id": 2,
 *   "method": "resources/read",
 *   "params": {
 *     "uri": "file:///project/src/main.rs"
 *   }
 * }
 * Response:
 * {
 *   "jsonrpc": "2.0",
 *   "id": 2,
 *   "result": {
 *     "contents": [
 *       {
 *         "uri": "file:///project/src/main.rs",
 *         "mimeType": "text/x-rust",
 *         "text": "fn main() {\n    println!(\"Hello world!\");\n}"
 *       }
 *     ]
 *   }
 * }
 *
 *
 */
public class Resource {
}
