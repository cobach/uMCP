# Próximos Pasos para uMCP

## Estado Actual (2025-01-30)

uMCP está funcionalmente completo como framework para crear servidores MCP, pero está bloqueado por dos bugs en el SDK oficial de Java:

1. **Bug de Timeout (PR #350)**: Orden incorrecto de operadores en reactive streams
2. **Bug de Deserialización (PR #355)**: Falla al deserializar parámetros de CallToolRequest

## Acciones Inmediatas

### 1. Esperar resolución de bugs del SDK
- Monitorear PRs #350 y #355 en el repositorio oficial
- Una vez fusionados, actualizar a la nueva versión del SDK

### 2. Mientras tanto, considerar:
- **Opción A**: Usar versión fork del SDK con los fixes aplicados localmente
- **Opción B**: Implementar workarounds temporales en uMCP
- **Opción C**: Documentar las limitaciones y esperar la versión oficial

## Plan de Desarrollo Post-Fix

### Fase 1: Transporte TCP (1-2 semanas)
- Implementar `TcpServerTransportProvider` nativo
- Crear abstracción de transporte en uMCP
- Añadir ejemplos de uso con TCP
- Testing exhaustivo con Node.js SDK

### Fase 2: Funcionalidades MCP Completas (2-3 semanas)
- **Resources**: Sistema de recursos con URIs y templates
- **Prompts**: Gestión de prompts y templates
- **Completions**: Autocompletado inteligente
- **Logging**: Sistema de logging estructurado MCP
- **Roots**: Gestión de directorios raíz

### Fase 3: Herramientas Avanzadas (2-3 semanas)
- Integración con bases de datos
- Herramientas de análisis de código
- Conectores para APIs externas
- Sistema de plugins

### Fase 4: Documentación y Distribución (1 semana)
- Documentación completa con ejemplos
- Guías de inicio rápido
- Publicación en Maven Central
- Sitio web del proyecto

## Arquitectura Propuesta para TCP

```java
// Abstracción de transporte en uMCP
public interface TransportProvider {
    McpAsyncServer createServer(ServerConfig config);
}

// Implementaciones
public class StdioTransportProvider implements TransportProvider { }
public class TcpTransportProvider implements TransportProvider { }
public class SseTransportProvider implements TransportProvider { }

// Uso simplificado
MCPServer.builder()
    .transport(Transport.TCP)
    .port(3000)
    .addTool(new MyTool())
    .build()
    .start();
```

## Mejoras Arquitectónicas

1. **Anotaciones para Tools**:
```java
@Tool(name = "get_weather", description = "Get weather information")
public class WeatherTool implements SyncCapability<WeatherInput, WeatherOutput> {
    // Implementación
}
```

2. **Auto-descubrimiento de Tools**:
```java
MCPServer.builder()
    .scanPackage("com.myapp.tools")
    .build();
```

3. **Configuración declarativa**:
```yaml
mcp:
  server:
    name: "MyMCPServer"
    version: "1.0.0"
    transport: tcp
    port: 3000
  tools:
    scan-packages:
      - com.myapp.tools
      - com.myapp.resources
```

## Consideraciones

- Mantener compatibilidad con la abstracción actual
- Priorizar simplicidad de uso
- Asegurar que los tests cubran todos los transportes
- Documentar claramente las diferencias entre transportes

## Cronograma Estimado

- **Semana 1-2**: Resolución de bugs del SDK (esperando)
- **Semana 3-4**: Implementación TCP
- **Semana 5-7**: Funcionalidades MCP completas
- **Semana 8-10**: Herramientas avanzadas
- **Semana 11**: Documentación y release

Total: ~3 meses desde la resolución de bugs del SDK