# 🚀 Quick Start — Sistema de Reservas de Viaje

Esta guía te ayudará a tener el sistema funcionando desde cero.

---

## 📋 Prerequisitos

Antes de comenzar, asegúrate de tener instalado:

- ☕ **Java 21** ([Descargar OpenJDK](https://openjdk.org/))
- 📦 **Maven 3.9+** ([Descargar Maven](https://maven.apache.org/))
- 🐳 **Docker** y **Docker Compose** ([Descargar Docker](https://www.docker.com/))

```bash
java --version    # Debe ser 21 o superior
mvn --version     # Debe ser 3.9 o superior
docker --version
```

**Puertos que deben estar libres:**

```
8081  — Camunda Operate
8082  — Camunda Tasklist
8088  — Zeebe REST
26500 — Zeebe gRPC
9080  — servicio-clientes
9081  — servicio-vuelos
9082  — servicio-hoteles
9083  — servicio-alquiler-coches
9084  — servicio-pagos
9090  — servicio-reservas (API principal)
```

---

## ⚠️ Paso obligatorio antes del primer arranque

`docker-compose-camunda.yml` requiere el archivo `connector-secrets.txt` para el servicio de Connectors. Este archivo **no está en el repositorio** (está en `.gitignore`). Créalo a partir de la plantilla incluida:

```bash
cp connector-secrets.txt.example connector-secrets.txt
```

Para desarrollo local puedes dejarlo con el contenido por defecto (vacío). Si usas Camunda Connectors con servicios externos (Slack, HTTP, etc.), añade las variables de entorno necesarias en ese archivo.

---

## 🚀 Arrancar el Sistema

### Primera vez (o tras cambios en el código)

```bash
./build-and-run.sh
```

Este script hace todo automáticamente:
1. 🌐 Crea la red Docker de Camunda
2. 🐳 Levanta Camunda Platform 8 (Zeebe, Operate, Tasklist)
3. ⏳ Espera a que Camunda esté listo (health check automático)
4. 🔧 Compila todos los microservicios con Maven
5. 🏗️ Construye las imágenes Docker
6. 🚀 Levanta todos los microservicios

**Tiempo estimado**: 3–5 minutos

### Reinicio rápido (sin recompilar)

```bash
./start.sh
```

Útil cuando el código no ha cambiado y las imágenes Docker ya existen.

> Para una referencia completa de todos los scripts disponibles, consulta [SCRIPTS.md](../SCRIPTS.md).

---

## ✅ Verificar que Todo Funciona

```bash
# Camunda Operate
curl http://localhost:8081/actuator/health

# Microservicios
curl http://localhost:9090/actuator/health   # Reservas (API principal)
curl http://localhost:9080/actuator/health   # Clientes
curl http://localhost:9081/actuator/health   # Vuelos
curl http://localhost:9082/actuator/health   # Hoteles
curl http://localhost:9083/actuator/health   # Coches
curl http://localhost:9084/actuator/health   # Pagos
```

Todos deben devolver `{"status":"UP"}`.

**Interfaces web:**

| Componente | URL | Credenciales |
|-----------|-----|-------------|
| 📊 Camunda Operate | http://localhost:8081 | demo / demo |
| 📋 Camunda Tasklist | http://localhost:8082 | demo / demo |

---

## 🎯 Primera Reserva

> ⚠️ **La capa REST aún no está implementada.** El único punto de entrada disponible actualmente es **Camunda Tasklist**. Los endpoints `/api/Pagos` están planificados como trabajo futuro.

### Iniciar el proceso desde Camunda Tasklist

1. Abre http://localhost:8082 (demo/demo)
2. Click en **"Start Process"**
3. Selecciona **"proceso-principal"**
4. Rellena el formulario con los datos del cliente y haz click en **"Start"**

### Completar el flujo (User Tasks)

Tras iniciar la reserva, el proceso se detiene en varias User Tasks que debes completar en Tasklist:

1. 📋 **Revisar Datos de Entrada** — revisa los datos del cliente
2. ✈️ **Revisar Reserva de Vuelo** — confirma los detalles del vuelo
3. 🏨 **Revisar Reserva de Hotel** — confirma los detalles del hotel
4. 🚗 **Revisar Reserva de Coche** — confirma los detalles del coche

---

## 🧪 Datos de Prueba

El sistema incluye datos precargados para facilitar las pruebas.

### Clientes Disponibles

| ID | Nombre | Tarjeta | Estado |
|----|--------|---------|--------|
| `CLI-001` | Vicente Priego | ✅ Válida | ACTIVO |
| `CLI-002` | Verónica Lesmes | ✅ Válida | ACTIVO |
| `CLI-003` | Juan Pérez | ❌ Inválida | ACTIVO |

### Escenarios por Monto

| Monto | Comportamiento esperado |
|-------|------------------------|
| `< 5000` | ✅ Reserva completada con éxito |
| `5000 – 10000` | ⚠️ Reserva confirmada con advertencia |
| `> 10000` | ❌ Pago rechazado → compensaciones automáticas (cancelación de vuelo, hotel y coche) |

### Escenarios de Error

| Caso | Configuración | Error BPMN | Resultado |
|------|--------------|------------|-----------|
| Cliente no existe | `clienteId: "CLI-999"` | `ERROR_CLIENTE_NO_ENCONTRADO` | Proceso termina en error |
| Tarjeta inválida | `clienteId: "CLI-003"` | `ERROR_TARJETA_INVALIDA` | Proceso termina en error |
| Pago rechazado | `monto: 15000` | `ERROR_PAGO_RECHAZADO` | Compensaciones automáticas |

---

## 📊 Monitorear el Proceso

### Camunda Operate (http://localhost:8081)

Permite ver el progreso visual de cada instancia de proceso:

1. Abre http://localhost:8081 y haz login con demo/demo
2. Ve a **Processes → proceso-principal**
3. Haz click en una instancia activa para ver el flujo en tiempo real
4. Navega a los Call Activities para ver los subprocesos
5. En caso de incident, usa "Retry" para reintentarlo

### Ver Logs de un Microservicio

```bash
# Logs en tiempo real
docker-compose logs -f servicio-clientes
docker-compose logs -f servicio-reservas

# Últimas 100 líneas
docker-compose logs --tail=100 servicio-pagos

# Logs de Camunda
docker-compose -f docker-compose-camunda.yml logs -f zeebe
```

### Consolas H2 (Bases de Datos)

Cada microservicio expone su base de datos en http://localhost:908X/h2-console

| Servicio | URL | JDBC URL |
|---------|-----|----------|
| Clientes | http://localhost:9080/h2-console | `jdbc:h2:mem:cliente_db` |
| Vuelos | http://localhost:9081/h2-console | `jdbc:h2:mem:vuelos_db` |
| Hoteles | http://localhost:9082/h2-console | `jdbc:h2:mem:hoteles_db` |
| Coches | http://localhost:9083/h2-console | `jdbc:h2:mem:coches_db` |
| Pagos | http://localhost:9084/h2-console | `jdbc:h2:mem:pagos_db` |

Usuario: `sa` / Contraseña: (vacía)

---

## 🌐 Documentación API (Swagger)

### Camunda 8 REST API (disponible ahora)

Zeebe expone su propia Swagger UI con la API REST de orquestación de Camunda:

**URL**: http://localhost:8088/swagger-ui/index.html

> ⚠️ **Problema conocido**: el campo `port` viene prerelleno con `8080`. Hay que cambiarlo manualmente a `8088` para que las peticiones funcionen correctamente.

**Pasos para usarla:**
1. Abre http://localhost:8088/swagger-ui/index.html
2. Localiza el campo `port` en la parte superior y cámbialo a `8088`
3. Ya puedes ejecutar peticiones directamente — en la configuración local Self-Managed con perfil `demo`, la autenticación está desactivada y los endpoints funcionan sin necesidad de token.

Esta API permite, entre otras cosas, iniciar instancias de procesos BPMN directamente sin pasar por Tasklist.

> 📖 Referencia oficial (Self-Managed): https://docs.camunda.io/docs/apis-tools/orchestration-cluster-api-rest/orchestration-cluster-api-rest-swagger/

---

### Microservicios (pendiente de implementar)

> ⚠️ Los controladores REST de los microservicios aún no existen, por lo que Swagger muestra "No operations defined in spec!" en todos ellos. Las URLs estarán disponibles una vez se implemente la capa REST.

| Servicio | URL (disponible cuando se implemente la capa REST) |
|---------|-----|
| 🎯 Reservas | http://localhost:9090/swagger-ui.html |
| 👥 Clientes | http://localhost:9080/swagger-ui.html |
| ✈️ Vuelos | http://localhost:9081/swagger-ui.html |
| 🏨 Hoteles | http://localhost:9082/swagger-ui.html |
| 🚗 Coches | http://localhost:9083/swagger-ui.html |
| 💳 Pagos | http://localhost:9084/swagger-ui.html |

---

## 🛑 Parar el Sistema

```bash
./stop-all.sh      # Para todo (microservicios + Camunda)
./stop.sh          # Para solo los microservicios (Camunda sigue activo)
```

---

## 📚 Próximos Pasos

- 🏗️ [Arquitectura Detallada](doc_arquitectura.md)
- 📊 [Procesos BPMN](doc_procesos_bpmn.md)
- 🛠️ [Microservicios](doc_microservicios.md)
- ⚙️ [Configuración](doc_configuracion.md)
- 🧪 [Testing](doc_testing.md)
- 🐳 [Deployment](doc_deployment.md)
- 📜 [Scripts disponibles](../SCRIPTS.md)
