package org.gegolabs.mcp.model;

/**
 * The Model Context Protocol (MCP) provides a standardized way for servers to expose prompt templates to clients. Prompts allow servers to provide structured messages and instructions for interacting with language models. Clients can discover available prompts, retrieve their contents, and provide arguments to customize them.
 * Prompts are designed to be user-controlled, meaning they are exposed from servers to clients with the intention of the user being able to explicitly select them for use.
 * Typically, prompts would be triggered through user-initiated commands in the user interface, which allows users to naturally discover and invoke available prompts.
 *
 * Ejemplo:
 *
 * {
 *   "jsonrpc": "2.0",
 *   "id": 1,
 *   "result": {
 *     "prompts": [
 *       {
 *         "name": "code_review",
 *         "description": "Asks the LLM to analyze code quality and suggest improvements",
 *         "arguments": [
 *           {
 *             "name": "code",
 *             "description": "The code to review",
 *             "required": true
 *           }
 *         ]
 *       }
 *     ],
 *     "nextCursor": "next-page-cursor"
 *   }
 * }
 *
 * REF: https://modelcontextprotocol.io/specification/2024-11-05/server/prompts
 *
 */
public class Prompt {
}
