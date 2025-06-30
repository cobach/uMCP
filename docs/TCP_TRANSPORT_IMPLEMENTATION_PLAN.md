# Plan de Implementación: Transporte TCP para uMCP

## Objetivo
Añadir soporte de transporte TCP a uMCP manteniendo la arquitectura actual y permitiendo que los servidores MCP sean accesibles a través de conexiones TCP.

## Estado Actual
- ✅ uMCP funciona correctamente con transporte stdio
- ✅ SDK MCP 0.10.1-SNAPSHOT con fixes de timeout y deserialización
- ✅ Arquitectura modular con abstracciones de capacidades (SyncCapability/AsyncCapability)
- ✅ SSE transport ya implementado como referencia

## Arquitectura Propuesta

### 1. Capa de Abstracción de Transporte

```java
package org.gegolabs.mcp1.transport;

public interface TransportProvider {
    String getName();
    McpAsyncServer createServer(TransportConfig config);
}

public abstract class TransportConfig {
    protected int port;
    protected String host;
    // Configuraciones comunes
}
```

### 2. Implementación TCP

```java
package org.gegolabs.mcp1.transport.tcp;

public class TcpTransportProvider implements TransportProvider {
    @Override
    public McpAsyncServer createServer(TransportConfig config) {
        TcpConfig tcpConfig = (TcpConfig) config;
        return McpAsyncServer.builder()
            .transport(new TcpServerTransportProvider(tcpConfig.getPort()))
            .build();
    }
}

public class TcpServerTransportProvider implements McpServerTransportProvider {
    private final int port;
    private ServerSocket serverSocket;
    private final List<TcpSession> sessions = new CopyOnWriteArrayList<>();
    
    // Implementación del servidor TCP
}
```

### 3. Protocolo TCP para MCP

El protocolo TCP debe manejar:
- **Framing**: Mensajes JSON-RPC delimitados por líneas nuevas
- **Multiplexing**: Múltiples clientes simultáneos
- **Keep-alive**: Detección de conexiones muertas
- **Graceful shutdown**: Cierre limpio de conexiones

```
Cliente                     Servidor TCP
   |                            |
   |-------- CONNECT ---------->|
   |<------- ACCEPTED ----------|
   |                            |
   |-- {"jsonrpc":"2.0"...} -->|  (mensaje delimitado por \n)
   |<-- {"jsonrpc":"2.0"...} --|
   |                            |
   |-------- CLOSE ------------>|
   |<------- CLOSED -----------|
```

## Fases de Implementación

### Fase 1: Infraestructura Base (2-3 días)

1. **Crear estructura de paquetes**
   ```
   src/main/java/org/gegolabs/mcp1/transport/
   ├── TransportProvider.java
   ├── TransportConfig.java
   ├── TransportFactory.java
   └── tcp/
       ├── TcpTransportProvider.java
       ├── TcpServerTransportProvider.java
       ├── TcpSession.java
       └── TcpConfig.java
   ```

2. **Implementar TcpServerTransportProvider**
   - Extender McpServerTransportProvider del SDK
   - Gestión de ServerSocket
   - Aceptar conexiones entrantes
   - Crear sesiones por cliente

3. **Implementar TcpSession**
   - Gestión de Socket individual
   - Lectura/escritura de mensajes JSON-RPC
   - Manejo de errores y desconexiones

### Fase 2: Integración con uMCP (1-2 días)

1. **Modificar MCPServer.java**
   ```java
   public class MCPServer {
       public static Builder builder() {
           return new Builder();
       }
       
       public static class Builder {
           private TransportType transport = TransportType.STDIO;
           private int port = 3000;
           
           public Builder transport(TransportType type) {
               this.transport = type;
               return this;
           }
           
           public Builder port(int port) {
               this.port = port;
               return this;
           }
       }
   }
   ```

2. **Crear aplicaciones de ejemplo**
   - `TcpIntegrationApp.java` - Servidor TCP
   - `TcpClientExample.java` - Cliente de prueba

### Fase 3: Testing y Validación (1-2 días)

1. **Tests unitarios**
   - Test de conexión/desconexión
   - Test de múltiples clientes
   - Test de mensajes grandes
   - Test de errores y timeouts

2. **Tests de integración con Node.js SDK**
   - Crear cliente TCP en Node.js
   - Validar tools/list
   - Validar tools/call
   - Validar manejo de errores

3. **Tests de rendimiento**
   - Múltiples conexiones simultáneas
   - Throughput de mensajes
   - Latencia de respuesta

### Fase 4: Documentación y Ejemplos (1 día)

1. **Documentación técnica**
   - Arquitectura del transporte TCP
   - Protocolo de comunicación
   - Guía de configuración

2. **Ejemplos de uso**
   - Servidor TCP básico
   - Cliente Java
   - Cliente Node.js
   - Configuración con múltiples transportes

## Consideraciones Técnicas

### 1. Gestión de Conexiones
- Pool de threads para manejar múltiples clientes
- Límite configurable de conexiones simultáneas
- Timeout de inactividad configurable

### 2. Seguridad
- Validación de entrada
- Límite de tamaño de mensajes
- Opción para TLS/SSL (futura)

### 3. Compatibilidad
- Mantener compatibilidad con stdio y SSE
- Permitir cambio de transporte sin modificar tools
- Configuración unificada

### 4. Manejo de Errores
- Reconexión automática (cliente)
- Logging detallado
- Métricas de conexión

## Riesgos y Mitigaciones

1. **Riesgo**: Complejidad en el manejo de múltiples sesiones
   - **Mitigación**: Usar ConcurrentHashMap y sincronización apropiada

2. **Riesgo**: Problemas de rendimiento con muchos clientes
   - **Mitigación**: Implementar pool de threads configurable

3. **Riesgo**: Incompatibilidad con clientes existentes
   - **Mitigación**: Seguir estrictamente el protocolo JSON-RPC sobre TCP

## Criterios de Éxito

1. ✅ Servidor TCP acepta conexiones en puerto configurable
2. ✅ Cliente Node.js SDK puede conectarse vía TCP
3. ✅ Tools funcionan correctamente sobre TCP
4. ✅ Soporte para múltiples clientes simultáneos
5. ✅ Tests automatizados pasan
6. ✅ Documentación completa

## Cronograma Estimado

- **Día 1-2**: Infraestructura base
- **Día 3**: Integración con uMCP
- **Día 4**: Testing y validación
- **Día 5**: Documentación y pulido

**Total**: 5 días de desarrollo

## Próximos Pasos

1. Revisar y aprobar este plan
2. Crear branch `feature/tcp-transport`
3. Implementar Fase 1
4. Revisión de código incremental
5. Continuar con fases siguientes

## Notas Adicionales

- El transporte TCP será opcional, stdio seguirá siendo el default
- La implementación debe ser lo más simple posible inicialmente
- Podemos añadir características avanzadas (TLS, compresión) más adelante
- Mantener consistencia con el patrón de diseño actual de uMCP