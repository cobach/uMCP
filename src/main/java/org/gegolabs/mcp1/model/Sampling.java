package org.gegolabs.mcp1.model;

/**
 * (Servicio del Cliente)
 *
 * The Model Context Protocol (MCP) provides a standardized way for servers to request LLM sampling (“completions” or “generations”) from language models via clients. This flow allows clients to maintain control over model access, selection, and permissions while enabling servers to leverage AI capabilities—with no server API keys necessary. Servers can request text or image-based interactions and optionally include context from MCP servers in their prompts.
 * Sampling in MCP allows servers to implement agentic behaviors, by enabling LLM calls to occur nested inside other MCP server features.
 * Implementations are free to expose sampling through any interface pattern that suits their needs—the protocol itself does not mandate any specific user interaction model.
 *
 * Ejemplo:
 *
 * Request: (from server)
 *
 * {
 *   "jsonrpc": "2.0",
 *   "id": 1,
 *   "method": "sampling/createMessage",
 *   "params": {
 *     "messages": [
 *       {
 *         "role": "user",
 *         "content": {
 *           "type": "text",
 *           "text": "What is the capital of France?"
 *         }
 *       }
 *     ],
 *     "modelPreferences": {
 *       "hints": [
 *         {
 *           "name": "claude-3-sonnet"
 *         }
 *       ],
 *       "intelligencePriority": 0.8,
 *       "speedPriority": 0.5
 *     },
 *     "systemPrompt": "You are a helpful assistant.",
 *     "maxTokens": 100
 *   }
 * }
 *
 * Response (from client)
 *
 * {
 *   "jsonrpc": "2.0",
 *   "id": 1,
 *   "result": {
 *     "role": "assistant",
 *     "content": {
 *       "type": "text",
 *       "text": "The capital of France is Paris."
 *     },
 *     "model": "claude-3-sonnet-20240307",
 *     "stopReason": "endTurn"
 *   }
 * }
 *
 * Capability Priorities
 *
 * Servers express their needs through three normalized priority values (0-1):
 *
 * costPriority: How important is minimizing costs? Higher values prefer cheaper models.
 * speedPriority: How important is low latency? Higher values prefer faster models.
 * intelligencePriority: How important are advanced capabilities? Higher values prefer more capable models.
 * ​
 * Model Hints
 *
 * While priorities help select models based on characteristics, hints allow servers to suggest specific models or model families:
 *
 * Hints are treated as substrings that can match model names flexibly
 * Multiple hints are evaluated in order of preference
 * Clients MAY map hints to equivalent models from different providers
 * Hints are advisory—clients make final model selection
 *
 * Example:
 * {
 *   "hints": [
 *     { "name": "claude-3-sonnet" }, // Prefer Sonnet-class models
 *     { "name": "claude" } // Fall back to any Claude model
 *   ],
 *   "costPriority": 0.3, // Cost is less important
 *   "speedPriority": 0.8, // Speed is very important
 *   "intelligencePriority": 0.5 // Moderate capability needs
 * }
 */
public class Sampling {
}
