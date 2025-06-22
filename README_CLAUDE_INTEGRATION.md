# Integración de UltraRAG MCP3 con Claude Code

## Pasos para integrar el servidor MCP3 con Claude Code:

### 1. Construcción del JAR
```bash
./gradlew clean jar
```
Esto genera: `build/libs/ultraRAG-1.0-SNAPSHOT.jar`

### 2. Configuración en Claude Code

Abre la configuración de Claude Code:
- En macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- En Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- En Linux: `~/.config/Claude/claude_desktop_config.json`

### 3. Agrega la configuración del servidor

Agrega esta configuración a tu `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "ultrarag": {
      "command": "java",
      "args": [
        "-jar",
        "/Users/cesar/Documents/git-projects/2025/ultraRAG/build/libs/ultraRAG-1.0-SNAPSHOT.jar"
      ]
    }
  }
}
```

**Nota**: Ajusta la ruta del JAR según tu ubicación.

### 4. Reinicia Claude Code

Después de guardar la configuración, reinicia Claude Code para que reconozca el nuevo servidor MCP.

### 5. Verificación

Una vez reiniciado, Claude Code debería mostrar las herramientas disponibles del servidor MCP3:
- `hello` - Saluda con un mensaje personalizado
- `calculator` - Realiza operaciones matemáticas básicas
- `getCurrentDateTime` - Obtiene fecha y hora actual
- `getSystemInfo` - Información del sistema

### Herramientas disponibles en MCP3:

1. **hello**
   - Descripción: Saluda con un mensaje personalizado
   - Parámetros: `name` (string)

2. **calculator**
   - Descripción: Realiza operaciones matemáticas
   - Parámetros: 
     - `operation`: "add", "subtract", "multiply", "divide"
     - `a`: número
     - `b`: número

3. **getCurrentDateTime**
   - Descripción: Obtiene fecha/hora actual
   - Parámetros:
     - `format`: formato de salida (opcional)
     - `timezone`: zona horaria (opcional)

4. **getSystemInfo**
   - Descripción: Información del sistema
   - Sin parámetros requeridos

### Solución de problemas:

Si el servidor no se conecta:
1. Verifica que Java esté instalado: `java -version`
2. Prueba ejecutar el JAR directamente: `java -jar build/libs/ultraRAG-1.0-SNAPSHOT.jar`
3. Revisa los logs de Claude Code
4. Asegúrate de que la ruta del JAR sea absoluta y correcta