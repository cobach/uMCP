# Plan de Implementación: Transporte TCP Transparente para uMCP usando mcp-bridge

## Objetivo
Modernizar uMCP para que escuche en un puerto TCP embebiendo la librería mcp-bridge, mientras que los clientes MCP continúan usando stdio sin darse cuenta del cambio. El protocolo TCP queda completamente encapsulado y transparente.

## Concepto Clave: TCP Transparente

**Punto fundamental**: Al incrustar mcp-bridge en uMCP, el cliente stdio del bridge se convierte en la nueva forma estándar de que los clientes MCP interactúen con uMCP. Desde la perspectiva del cliente MCP:
- Sigue usando stdio como siempre
- No necesita saber que hay TCP de por medio
- El protocolo TCP queda completamente encapsulado
- La experiencia es idéntica a conectarse a un servidor stdio tradicional

## Estado Actual
- ✅ uMCP funciona correctamente con transporte stdio directo
- ✅ SDK MCP 0.10.1-SNAPSHOT con fixes de timeout y deserialización
- ✅ Arquitectura modular con abstracciones de capacidades (SyncCapability/AsyncCapability)
- ✅ mcp-bridge disponible como librería con soporte para servidores embebidos

## Descubrimiento Importante: StreamProvider en mcp-bridge

mcp-bridge ya incluye la infraestructura necesaria para embeber servidores MCP:
- **StreamProvider Interface**: Permite integrar servidores MCP in-process
- **Modo EMBEDDED**: Configuración de mcp-bridge para usar StreamProvider en lugar de procesos externos
- **No se necesita TcpTransportProvider**: mcp-bridge maneja todo el transporte TCP

## Arquitectura de Integración

### Flujo de Comunicación (TCP Encapsulado)
```
┌─────────────┐         ┌─────────────────┐  TCP   ┌─────────────────┐         ┌──────────────┐
│ MCP Client  │  stdio  │  mcp-bridge     │◄─────►│  mcp-bridge     │   In-   │    uMCP      │
│ (Claude,    │◄───────►│  Client (Stub)  │       │ Server(Skeleton)│ Process │(StreamProvider│
│  etc.)      │         │                 │       │    EMBEDDED     │ Streams │   embedded)  │
└─────────────┘         └─────────────────┘       └─────────────────┘◄───────►└──────────────┘
                   ↑                                                                      ↑
                   └──────────────────────────────────────────────────────────────────────┘
                         El cliente ve stdio puro, TCP es completamente transparente

Detalle del protocolo:
- Cliente ↔ Stub: stdio (JSON-RPC)
- Stub ↔ Skeleton: TCP con framing (length-prefixed messages)
- Skeleton ↔ uMCP: In-process streams (PipedInputStream/OutputStream)
```

### Componentes
1. **MCP Client**: Sin cambios - usa stdio como siempre
2. **mcp-bridge Client (Stub)**: Proxy que traduce stdio ↔ TCP
   - Recibe JSON-RPC por stdin
   - Envía por TCP al servidor
   - Recibe respuestas TCP y las envía por stdout
3. **mcp-bridge Server (Skeleton)**: Servidor TCP con uMCP embebido
   - Escucha en puerto TCP
   - Maneja múltiples conexiones concurrentes
   - Delega a uMCP via StreamProvider
4. **uMCP (StreamProvider)**: Lógica MCP embebida en el skeleton
   - Procesa comandos MCP
   - Retorna respuestas
   - Completamente aislado del transporte

## Fases de Implementación

### Fase 1: Implementación de StreamProvider en uMCP

1. **Agregar dependencia a mcp-bridge**
   ```gradle
   dependencies {
       implementation 'org.gegolabs.mcp.bridge:mcp-bridge:1.0.0-SNAPSHOT'
       // ... otras dependencias
   }
   ```

2. **Crear UmcpTransportProvider que implementa ambas interfaces**
   ```java
   package org.gegolabs.mcp.bridge;
   
   import org.gegolabs.mcp.bridge.api.StreamProvider;
   import org.gegolabs.mcp.bridge.api.StreamPair;
   import io.modelcontextprotocol.sdk.McpServerTransportProvider;
   import io.modelcontextprotocol.sdk.shared.Transport;
   import java.net.Socket;
   import java.io.*;
   
   /**
    * Transport provider that bridges between MCP SDK and mcp-bridge.
    * Implements both interfaces to work with both systems.
    */
   @Slf4j
   public class UmcpTransportProvider implements McpServerTransportProvider, StreamProvider {
       private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();
       private final McpAsyncServer mcpServer;
       
       public UmcpTransportProvider() {
           // Crear servidor MCP con todas las capabilities
           this.mcpServer = McpAsyncServer.builder()
               .name("uMCP Server")
               .version("1.0.2")
               .build();
           
           // Registrar capabilities
           registerCapabilities();
       }
       
       private void registerCapabilities() {
           // Registrar tools disponibles
           var domainAvailability = new DomainAvailability();
           mcpServer.tool(
               domainAvailability.name(),
               domainAvailability.description(),
               domainAvailability.parameters(),
               domainAvailability::call
           );
           
           var systemInfo = new SystemInformation();
           mcpServer.tool(
               systemInfo.name(),
               systemInfo.description(),
               systemInfo.parameters(),
               systemInfo::call
           );
       }
       
       // Implementación de StreamProvider para mcp-bridge
       @Override
       public StreamPair createStreams(String sessionId, Socket clientSocket) 
               throws IOException {
           // Crear pipes para conectar TCP con MCP
           PipedInputStream fromClient = new PipedInputStream();
           PipedOutputStream toMcp = new PipedOutputStream(fromClient);
           
           PipedInputStream fromMcp = new PipedInputStream();
           PipedOutputStream toClient = new PipedOutputStream(fromMcp);
           
           // Crear transport para esta sesión
           Transport transport = new StreamTransport(fromClient, toClient);
           
           // Guardar contexto de la sesión
           SessionContext context = new SessionContext(
               sessionId, transport, toMcp, fromMcp
           );
           sessions.put(sessionId, context);
           
           // Conectar el transport al servidor MCP
           mcpServer.connect(transport);
           
           // Retornar streams para que mcp-bridge los conecte al socket TCP
           return new StreamPair(fromMcp, toMcp);
       }
       
       @Override
       public void sessionClosed(String sessionId) {
           log.info("Session closed: {}", sessionId);
           SessionContext context = sessions.remove(sessionId);
           if (context != null) {
               context.close();
           }
       }
       
       // Implementación de McpServerTransportProvider
       @Override
       public Transport create() {
           // Este método se usa para transporte directo stdio
           // En nuestro caso, los transports se crean en createStreams()
           throw new UnsupportedOperationException(
               "Use createStreams() for TCP transport"
           );
       }
       
       // Clase auxiliar para mantener el contexto de cada sesión
       @Data
       private static class SessionContext implements Closeable {
           private final String sessionId;
           private final Transport transport;
           private final OutputStream toMcp;
           private final InputStream fromMcp;
           
           @Override
           public void close() {
               try {
                   transport.close();
                   toMcp.close();
                   fromMcp.close();
               } catch (IOException e) {
                   log.error("Error closing session {}: {}", sessionId, e.getMessage());
               }
           }
       }
   }
   ```

3. **Crear StreamTransport para conectar streams arbitrarios**
   ```java
   package org.gegolabs.mcp.transport;
   
   import io.modelcontextprotocol.sdk.shared.Transport;
   import reactor.core.publisher.Flux;
   import reactor.core.publisher.Mono;
   
   /**
    * Transport implementation that works with arbitrary input/output streams.
    * Used to connect MCP SDK with mcp-bridge's stream pairs.
    */
   public class StreamTransport implements Transport {
       private final InputStream input;
       private final OutputStream output;
       private final ObjectMapper mapper = new ObjectMapper();
       
       public StreamTransport(InputStream input, OutputStream output) {
           this.input = input;
           this.output = output;
       }
       
       @Override
       public Flux<JsonNode> messages() {
           // Leer mensajes JSON del input stream
           return Flux.create(sink -> {
               BufferedReader reader = new BufferedReader(
                   new InputStreamReader(input)
               );
               
               Thread readerThread = new Thread(() -> {
                   try {
                       String line;
                       while ((line = reader.readLine()) != null) {
                           JsonNode message = mapper.readTree(line);
                           sink.next(message);
                       }
                       sink.complete();
                   } catch (IOException e) {
                       sink.error(e);
                   }
               });
               
               readerThread.setDaemon(true);
               readerThread.start();
           });
       }
       
       @Override
       public Mono<Void> send(JsonNode message) {
           // Escribir mensaje JSON al output stream
           return Mono.fromRunnable(() -> {
               try {
                   String json = mapper.writeValueAsString(message);
                   output.write(json.getBytes());
                   output.write('\n');
                   output.flush();
               } catch (IOException e) {
                   throw new RuntimeException(e);
               }
           });
       }
       
       @Override
       public Mono<Void> close() {
           return Mono.fromRunnable(() -> {
               try {
                   input.close();
                   output.close();
               } catch (IOException e) {
                   log.error("Error closing transport: {}", e.getMessage());
               }
           });
       }
   }
   ```

### Fase 2: Aplicación Principal con Bridge Embebido

1. **Crear UmcpServerApp**
   ```java
   package org.gegolabs.mcp;
   
   import org.gegolabs.mcp.bridge.BridgeServerBuilder;
   import org.gegolabs.mcp.bridge.api.BridgeServerApi;
   import org.gegolabs.mcp.bridge.config.ServerMode;
   
   /**
    * uMCP Server - TCP server with embedded MCP and transparent stdio access
    * 
    * This server listens on TCP port 7777 by default.
    * Clients connect using stdio through mcp-bridge client:
    * 
    * java -jar mcp-bridge.jar client localhost:7777
    */
   @Slf4j
   public class UmcpServerApp {
       public static void main(String[] args) throws Exception {
           int port = args.length > 0 ? Integer.parseInt(args[0]) : 7777;
           
           log.info("Starting uMCP Server on TCP port {}", port);
           log.info("Connect using: java -jar mcp-bridge.jar client localhost:{}", port);
           
           // Crear TransportProvider que implementa StreamProvider
           UmcpTransportProvider transportProvider = new UmcpTransportProvider();
           
           // Configurar y arrancar bridge server en modo EMBEDDED
           BridgeServerApi server = BridgeServerBuilder.create()
               .port(port)
               .serverMode(ServerMode.EMBEDDED)
               .streamProvider(transportProvider)
               .maxClients(20)
               .onConnectionAccepted(event -> 
                   log.info("Client connected from: {}", event.getRemoteAddress()))
               .onSessionClosed(event -> 
                   log.info("Client disconnected: {}", event.getSessionId()))
               .build();
           
           // Iniciar servidor
           server.start().join();
           
           // Agregar shutdown hook
           Runtime.getRuntime().addShutdownHook(new Thread(() -> {
               log.info("Shutting down uMCP Server...");
               server.stop().join();
           }));
           
           // Mantener el main thread vivo
           Thread.currentThread().join();
       }
   }
   ```

2. **Eliminar clases main antiguas**
   - Eliminar `StdioIntegrationApp.java`
   - Eliminar `SseIntegrationApp.java`

3. **Actualizar build.gradle**
   ```gradle
   application {
       mainClass = 'org.gegolabs.mcp.UmcpServerApp'
   }
   ```

### Fase 3: Comando de Instalación Automática del Stub

1. **Comando interactivo simple**
   ```bash
   # Comando principal - totalmente interactivo
   java -jar mcp-bridge.jar client install
   
   # O con alias más simple
   bridge-cli client install
   ```

2. **Flujo interactivo de instalación**
   ```
   $ bridge-cli client install
   
   🔍 Detecting installed AI agents...
   
   Found the following AI agents:
   1. Claude Desktop (/Users/user/Library/Application Support/Claude/claude_desktop_config.json)
   2. Claude Code (~/.config/claude-code/settings.json)
   
   Select an agent to configure (1-2) or 'q' to quit: 1
   
   📋 Enter MCP server details:
   Server name [umcp]: my-server
   Host [localhost]: 
   Port [7777]: 8080
   
   🔧 Configuration preview:
   {
     "mcpServers": {
       "my-server": {
         "command": "java",
         "args": ["-jar", "/usr/local/bin/mcp-bridge.jar", "client", "localhost:8080"]
       }
     }
   }
   
   ⚠️  Server 'my-server' already exists in Claude Desktop. Overwrite? (y/n): y
   
   💾 Creating backup: claude_desktop_config.json.backup-2025-01-30T15:30:00
   ✅ Successfully installed 'my-server' for Claude Desktop
   
   🚀 Please restart Claude Desktop to activate the new MCP server.
   
   Would you like to configure another agent? (y/n): n
   ```

3. **Comandos adicionales (no interactivos)**
   ```bash
   # Instalación directa (sin interacción)
   bridge-cli client install --agent claude-desktop --server umcp --host localhost --port 7777
   
   # Listar agentes disponibles
   bridge-cli client install --list
   
   # Verificar configuración existente
   bridge-cli client install --check
   
   # Desinstalar
   bridge-cli client uninstall
   ```

4. **Implementar InteractiveInstaller en mcp-bridge**
   ```java
   package org.gegolabs.mcp.bridge.cli;
   
   @Slf4j
   public class InteractiveInstaller {
       private final Scanner scanner = new Scanner(System.in);
       private final AgentDetector detector = new AgentDetector();
       
       public void runInteractive() {
           System.out.println("\n🔍 Detecting installed AI agents...\n");
           
           List<DetectedAgent> agents = detector.detectInstalledAgents();
           
           if (agents.isEmpty()) {
               System.out.println("❌ No supported AI agents found.");
               System.out.println("\nSupported agents:");
               System.out.println("- Claude Desktop: https://claude.ai/desktop");
               System.out.println("- Claude Code: https://github.com/anthropics/claude-code");
               return;
           }
           
           // Mostrar agentes encontrados
           System.out.println("Found the following AI agents:");
           for (int i = 0; i < agents.size(); i++) {
               DetectedAgent agent = agents.get(i);
               System.out.printf("%d. %s (%s)\n", i + 1, agent.displayName, agent.configPath);
           }
           
           // Selección de agente
           System.out.print("\nSelect an agent to configure (1-" + agents.size() + ") or 'q' to quit: ");
           String choice = scanner.nextLine();
           
           if ("q".equalsIgnoreCase(choice)) {
               return;
           }
           
           int index = Integer.parseInt(choice) - 1;
           DetectedAgent selectedAgent = agents.get(index);
           
           // Solicitar detalles del servidor
           System.out.println("\n📋 Enter MCP server details:");
           
           System.out.print("Server name [umcp]: ");
           String serverName = scanner.nextLine();
           if (serverName.isEmpty()) serverName = "umcp";
           
           System.out.print("Host [localhost]: ");
           String host = scanner.nextLine();
           if (host.isEmpty()) host = "localhost";
           
           System.out.print("Port [7777]: ");
           String portStr = scanner.nextLine();
           int port = portStr.isEmpty() ? 7777 : Integer.parseInt(portStr);
           
           // Mostrar preview
           System.out.println("\n🔧 Configuration preview:");
           String configJson = generateConfigJson(serverName, host, port);
           System.out.println(configJson);
           
           // Instalar con confirmación
           installWithConfirmation(selectedAgent, serverName, host, port);
           
           // Preguntar si configurar otro
           System.out.print("\nWould you like to configure another agent? (y/n): ");
           if ("y".equalsIgnoreCase(scanner.nextLine())) {
               runInteractive();
           }
       }
   }
   ```

5. **Implementar InstallCommand con opciones no interactivas**
   ```java
   @Slf4j
   public class InstallCommand {
       private static final Map<String, AgentConfig> SUPPORTED_AGENTS = Map.of(
           "claude-desktop", new AgentConfig(
               "Claude Desktop",
               System.getProperty("user.home") + "/Library/Application Support/Claude/claude_desktop_config.json",
               "mcpServers"
           ),
           "claude-code", new AgentConfig(
               "Claude Code", 
               System.getProperty("user.home") + "/.config/claude-code/settings.json",
               "mcpServers"
           )
       );
       
       public void install(String agent, String serverName, String host, int port) {
           AgentConfig config = SUPPORTED_AGENTS.get(agent);
           if (config == null) {
               throw new IllegalArgumentException("Unknown agent: " + agent);
           }
           
           // Leer configuración existente
           JsonObject existingConfig = readConfig(config.configPath);
           
           // Obtener sección mcpServers
           JsonObject mcpServers = existingConfig.getAsJsonObject(config.serversKey);
           if (mcpServers == null) {
               mcpServers = new JsonObject();
               existingConfig.add(config.serversKey, mcpServers);
           }
           
           // Verificar si ya existe
           if (mcpServers.has(serverName)) {
               log.warn("Server '{}' already configured in {}", serverName, config.name);
               if (!promptOverwrite()) {
                   return;
               }
           }
           
           // Crear configuración del servidor
           JsonObject serverConfig = new JsonObject();
           serverConfig.addProperty("command", "java");
           JsonArray args = new JsonArray();
           args.add("-jar");
           args.add(getCurrentJarPath()); // Path al mcp-bridge.jar
           args.add("client");
           args.add(host + ":" + port);
           serverConfig.add("args", args);
           
           // Agregar a la configuración
           mcpServers.add(serverName, serverConfig);
           
           // Guardar configuración
           saveConfig(config.configPath, existingConfig);
           
           log.info("Successfully installed '{}' stub for {} at {}:{}", 
                    serverName, config.name, host, port);
           log.info("Restart {} to activate the new MCP server", config.name);
       }
       
       private void createBackup(String configPath) {
           // Crear backup antes de modificar
           String backupPath = configPath + ".backup-" + 
                              LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
           Files.copy(Paths.get(configPath), Paths.get(backupPath));
           log.info("Created backup at: {}", backupPath);
       }
   }
   ```

3. **Agregar detección inteligente de configuración**
   ```java
   public class AgentDetector {
       public List<DetectedAgent> detectInstalledAgents() {
           List<DetectedAgent> agents = new ArrayList<>();
           
           // Detectar Claude Desktop
           if (Files.exists(Paths.get(CLAUDE_DESKTOP_CONFIG))) {
               agents.add(new DetectedAgent("claude-desktop", "Claude Desktop", true));
           }
           
           // Detectar Claude Code
           if (Files.exists(Paths.get(CLAUDE_CODE_CONFIG))) {
               agents.add(new DetectedAgent("claude-code", "Claude Code", true));
           }
           
           return agents;
       }
       
       public void suggestInstallation() {
           List<DetectedAgent> agents = detectInstalledAgents();
           
           if (agents.isEmpty()) {
               log.info("No supported AI agents detected. Supported agents:");
               log.info("- Claude Desktop: https://claude.ai/desktop");
               log.info("- Claude Code: https://github.com/anthropics/claude-code");
               return;
           }
           
           log.info("Detected AI agents:");
           for (DetectedAgent agent : agents) {
               log.info("- {} ({})", agent.displayName, agent.id);
           }
           
           log.info("\nTo install uMCP for an agent, run:");
           log.info("java -jar mcp-bridge.jar install --agent <agent-id> --server umcp --host localhost --port 7777");
       }
   }
   ```

4. **Comando de desinstalación**
   ```bash
   # Remover stub de un agente
   java -jar mcp-bridge.jar uninstall --agent claude-desktop --server umcp
   
   # Remover con confirmación
   java -jar mcp-bridge.jar uninstall --agent claude-code --server umcp --confirm
   ```

### Fase 4: Testing y Validación

1. **Test de integración básico**
   ```bash
   # Terminal 1: Iniciar uMCP Server
   java -jar build/libs/uMCP-1.0.2.jar
   
   # Terminal 2: Conectar cliente
   echo '{"jsonrpc":"2.0","method":"tools/list","id":1}' | \
     java -jar mcp-bridge.jar client localhost:7777
   ```

2. **Test con múltiples clientes**
   ```bash
   # Conectar varios clientes simultáneamente
   for i in {1..5}; do
     (echo '{"jsonrpc":"2.0","method":"tools/call","params":{"name":"domain-availability","arguments":{"domain":"test'$i'.com"}},"id":'$i'}' | \
       java -jar mcp-bridge.jar client localhost:7777) &
   done
   ```

3. **Test con Claude Desktop**
   - Configurar Claude Desktop con la configuración proporcionada
   - Verificar que las herramientas aparecen y funcionan correctamente

### Fase 4: Documentación

1. **Actualizar README de uMCP**
   ```markdown
   # uMCP - Ultra Model Context Protocol Server
   
   A lightweight, modular MCP server framework with built-in TCP support.
   
   ## Quick Start
   
   ### Running the Server
   
   uMCP runs as a TCP server on port 7777 by default:
   
   ```bash
   java -jar uMCP-1.0.2.jar [port]
   ```
   
   ### Connecting Clients
   
   #### Interactive Installation (Recommended)
   
   ```bash
   # Simple interactive installer
   bridge-cli client install
   
   # Or using the JAR directly
   java -jar mcp-bridge.jar client install
   ```
   
   This will:
   - Automatically detect installed AI agents (Claude Desktop, Claude Code)
   - Let you select which agent to configure
   - Guide you through server configuration
   - Create backups before modifying configs
   - Offer to configure multiple agents
   
   #### Direct Installation (Advanced)
   
   ```bash
   # Install for specific agent without interaction
   bridge-cli client install --agent claude-desktop --server umcp --host localhost --port 7777
   ```
   
   #### Manual Testing
   
   ```bash
   # Direct connection for testing
   java -jar mcp-bridge.jar client localhost:7777
   ```
   
   #### Manual Configuration
   
   If automatic installation doesn't work, add to Claude Desktop config manually:
   ```json
   {
     "mcpServers": {
       "umcp": {
         "command": "java",
         "args": ["-jar", "path/to/mcp-bridge.jar", "client", "localhost:7777"]
       }
     }
   }
   ```
   
   ## Architecture
   
   uMCP uses mcp-bridge in embedded mode to provide transparent TCP connectivity:
   - Clients use stdio (no changes needed)
   - TCP transport is completely transparent
   - Supports multiple concurrent clients
   - Full session isolation
   
   ## Alternative Transports (Development)
   
   For development and testing, uMCP supports additional transports:
   
   ### Direct STDIO
   ```java
   MCPServer server = MCPServer.builder()
       .transport_Stdio()
       .build();
   ```
   
   ### Server-Sent Events (SSE)
   ```java
   MCPServer server = MCPServer.builder()
       .transport_Sse()
       .ssePort(8080)
       .build();
   ```
   ```

### Fase 5: Empaquetado y Distribución

1. **Crear distribución con dependencias**
   ```gradle
   // En build.gradle
   task fatJar(type: Jar) {
       manifest {
           attributes 'Main-Class': 'org.gegolabs.mcp.UmcpServerApp'
       }
       archiveClassifier = 'all'
       from {
           configurations.runtimeClasspath.collect { 
               it.isDirectory() ? it : zipTree(it) 
           }
       }
       with jar
   }
   ```

2. **Bundle de distribución**
   ```
   umcp-dist/
   ├── lib/
   │   ├── uMCP-1.0.2-all.jar      # Fat JAR con todas las dependencias
   │   └── mcp-bridge-client.jar    # Cliente bridge para conexiones
   ├── bin/
   │   ├── start-server.sh          # Script para iniciar servidor
   │   ├── bridge-cli               # Alias para mcp-bridge.jar
   │   └── connect-client.sh        # Script para conectar cliente
   ├── config/
   │   └── claude-desktop.json      # Ejemplo de configuración
   └── README.md
   ```

3. **Script bridge-cli para facilitar el uso**
   ```bash
   #!/bin/bash
   # bridge-cli - Wrapper script for mcp-bridge.jar
   
   BRIDGE_JAR="$(dirname "$0")/../lib/mcp-bridge-client.jar"
   
   if [ ! -f "$BRIDGE_JAR" ]; then
       echo "Error: mcp-bridge-client.jar not found at $BRIDGE_JAR"
       exit 1
   fi
   
   java -jar "$BRIDGE_JAR" "$@"
   ```

4. **Instalación del alias global (opcional)**
   ```bash
   # En el instalador del bundle
   echo "Installing bridge-cli to /usr/local/bin..."
   sudo cp bin/bridge-cli /usr/local/bin/
   sudo chmod +x /usr/local/bin/bridge-cli
   
   echo "✅ bridge-cli installed successfully!"
   echo "You can now use: bridge-cli client install"
   ```

## Ventajas de esta Arquitectura

1. **Simplicidad**: No hay que implementar transporte TCP desde cero
2. **Reutilización**: Aprovecha toda la infraestructura robusta de mcp-bridge
3. **Transparencia**: Los clientes no saben que hay TCP de por medio
4. **Escalabilidad**: Soporte nativo para múltiples clientes
5. **Mantenibilidad**: Un solo punto de integración (StreamProvider)

## Criterios de Éxito

1. ✅ uMCP funciona embebido en mcp-bridge
2. ✅ Servidor escucha en puerto TCP configurable
3. ✅ Clientes se conectan transparentemente via stdio
4. ✅ Soporte para múltiples clientes simultáneos
5. ✅ Todas las capabilities funcionan correctamente
6. ✅ Documentación clara y ejemplos funcionando

## Próximos Pasos

1. Implementar UmcpStreamProvider
2. Modificar MCPServer para soportar modo IN_PROCESS
3. Crear UmcpServerApp con bridge embebido
4. Probar integración completa
5. Actualizar documentación y crear distribución

## Notas Adicionales

### Diferencias con el Plan Original

- **Plan original**: Crear TcpTransportProvider desde cero
- **Plan actualizado**: Usar StreamProvider de mcp-bridge
- **Beneficio**: Menos código, más robusto, mejor integración

### Consideraciones de Diseño

- StreamProvider es la interfaz clave para la integración
- El modo EMBEDDED de mcp-bridge está diseñado exactamente para este caso
- La arquitectura mantiene la separación de responsabilidades:
  - uMCP: Lógica MCP y capabilities
  - mcp-bridge: Transporte TCP y gestión de conexiones