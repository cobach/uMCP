package org.gegolabs.mcp.model;

/**
 * Resource templates allow servers to expose parameterized resources using URI templates. Arguments may be auto-completed through the completion API.
 *
 * Ejemplo:
 *
 * {
 *   "jsonrpc": "2.0",
 *   "id": 3,
 *   "result": {
 *     "resourceTemplates": [
 *       {
 *         "uriTemplate": "file:///{path}",
 *         "name": "Project Files",
 *         "description": "Access files in the project directory",
 *         "mimeType": "application/octet-stream"
 *       }
 *     ]
 *   }
 * }
 *
 *
 * TYPES:
 * Text Content:
 * {
 *   "uri": "file:///example.txt",
 *   "mimeType": "text/plain",
 *   "text": "Resource content"
 * }
 *  Binary Content:
 *  {
 *   "uri": "file:///example.png",
 *   "mimeType": "image/png",
 *   "blob": "base64-encoded-data"
 * }
 *
 */
public class ResourceTemplate {
}
