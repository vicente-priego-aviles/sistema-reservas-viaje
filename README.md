# 🎯 Sistema de Pagos de Viaje con Camunda Platform 8

Sistema de microservicios orquestado por **Camunda Platform 8.7** que implementa un flujo completo de Pagos de viaje incluyendo vuelos, hoteles y coches, con procesamiento de pagos y gestión de clientes.

## 📋 Tabla de Contenidos

- [Arquitectura](#-arquitectura)
- [Tecnologías](#️-tecnologías)
- [Prerequisitos](#-prerequisitos)
- [Inicio Rápido](#-inicio-rápido)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Servicios](#-servicios)
- [Procesos BPMN](#-procesos-bpmn)
- [API REST](#-api-rest)
- [Testing](#-testing)
- [Monitoreo](#-monitoreo)
- [Troubleshooting](#-troubleshooting)

---

## 🏗️ Arquitectura

### Patrón Arquitectónico

- **Arquitectura Hexagonal** (Ports & Adapters)
- **Domain-Driven Design (DDD)** con JMolecules
- **Microservicios** comunicados vía Camunda Platform 8
- **Patrón Saga** para transacciones distribuidas con compensaciones

### Diagrama de Alto Nivel

```
                    ┌─────────────────────┐
                    │  Cliente / Frontend │
                    └──────────┬──────────┘
                               │ HTTP POST
                               ↓
                    ┌─────────────────────┐
                    │ servicio-Pagos   │
                    │   (Orquestador)     │
                    │   Puerto: 9090      │
                    └──────────┬──────────┘
                               │ Inicia Proceso
                               ↓
                    ┌─────────────────────┐
                    │  Camunda Platform 8 │
                    │   - Zeebe (26500)   │
                    │   - Operate (8080)  │
                    │   - Tasklist (8081) │
                    └──────────┬──────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         │                     │                     │
         ↓                     ↓                     ↓
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│  Gestión       │   │  Proceso       │   │  Proceso       │
│  Cliente       │   │  Reserva       │   │  Pago          │
│ (subproceso)   │   │ (subproceso)   │   │ (subproceso)   │
└────────┬───────┘   └────────┬───────┘   └────────┬───────┘
         │                    │                     │
         ↓                    ↓                     ↓
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│ servicio-      │   │ servicio-      │   │ servicio-      │
│ clientes       │   │ vuelos/hoteles/│   │ pagos          │
│ (9080)         │   │ coches         │   │ (9084)         │
│                │   │ (9081/82/83)   │   │                │
└────────────────┘   └────────────────┘   └────────────────┘
     Workers              Workers              Workers
```

---

## 🛠️ Tecnologías

### Backend (Todos los Microservicios)

| Tecnología               | Versión | Propósito                   |
| ------------------------ |---------| --------------------------- |
| **Java**                 | 21      | Lenguaje principal          |
| **Spring Boot**          | 3.5.6   | Framework base              |
| **Camunda Platform**     | 8.7.0   | Orquestación BPMN           |
| **Spring Zeebe**         | 8.8.0   | Integración con Zeebe       |
| **H2 Database**          | 2.3.232 | Base de datos (desarrollo)  |
| **Lombok**               | 1.18.36 | Reducción de boilerplate    |
| **MapStruct**            | 1.6.3   | Mapeo DTO ↔ Entity          |
| **JMolecules**           | 1.10.0  | Anotaciones DDD             |
| **SpringDoc OpenAPI**    | 2.7.0   | Documentación API           |

### Infraestructura

- **Docker** & **Docker Compose**
- **Camunda Platform 8.7** (Zeebe, Operate, Tasklist)
- **Elasticsearch 8.9** (para Camunda)

---

## 📦 Prerequisitos

### Software Requerido

```bash
# Java 21
java --version  # Debe ser 21 o superior

# Maven 3.9+
mvn --version

# Docker & Docker Compose
docker --version
docker-compose --version
```

### Puertos Necesarios

Asegúrate de que estos puertos estén disponibles:

```
Camunda:
  - 26500  (Zeebe gRPC)
  - 8080   (Operate)
  - 8081   (Tasklist)
  - 9200   (Elasticsearch)

Microservicios:
  - 9080   (servicio-clientes)
  - 9081   (servicio-vuelos)
  - 9082   (servicio-hoteles)
  - 9083   (servicio-coches)
  - 9084   (servicio-pagos)
  - 9090   (servicio-Pagos - API Principal)
```

---

## 🚀 Inicio Rápido

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd sistema-Pagos-viaje
```

### 2. Dar Permisos a Scripts

```bash
chmod +x start.sh stop.sh restart.sh logs.sh test-reserva.sh
```

### 3. Iniciar el Sistema Completo

```bash
./start.sh
```

Este script:
- ✅ Compila todos los microservicios
- ✅ Inicia Camunda Platform 8.7
- ✅ Espera a que Camunda esté listo
- ✅ Inicia todos los microservicios
- ✅ Verifica la salud de los servicios

**Tiempo estimado**: 3-5 minutos

### 4. Verificar que Todo Esté Funcionando

```bash
# Verificar Camunda
curl http://localhost:9600/ready

# Verificar microservicios
curl http://localhost:9090/actuator/health
```

### 5. Crear Tu Primera Reserva

```bash
./test-reserva.sh
```

O manualmente:

```bash
curl -X POST http://localhost:9090/api/Pagos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI-001",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2025-12-15",
    "fechaFin": "2025-12-20",
    "monto": 1500.00
  }'
```

### 6. Monitorear la Ejecución

- **Camunda Operate**: http://localhost:8080 (demo/demo)
- **Camunda Tasklist**: http://localhost:8081 (demo/demo)
- **Swagger Pagos**: http://localhost:9090/swagger-ui.html

---

## 📁 Estructura del Proyecto

```
sistema-Pagos-viaje/
├── bpmn/                              # Procesos BPMN
│   ├── proceso-principal.bpmn
│   ├── subproceso-gestion-cliente.bpmn
│   ├── subproceso-proceso-reserva.bpmn
│   └── subproceso-pago.bpmn
│
├── servicio-clientes/                 # Puerto 9080
│   ├── src/main/java/.../cliente/
│   │   ├── dominio/                   # Lógica de negocio
│   │   ├── aplicacion/                # Casos de uso
│   │   └── infraestructura/           # Adaptadores
│   ├── pom.xml
│   └── Dockerfile
│
├── servicio-vuelos/                   # Puerto 9081
├── servicio-hoteles/                  # Puerto 9082
├── servicio-coches/                   # Puerto 9083
├── servicio-pagos/                    # Puerto 9084
│
├── servicio-Pagos/                 # Puerto 9090 (Orquestador)
│   └── Despliega procesos BPMN automáticamente
│
├── docker-compose-camunda.yml         # Infraestructura Camunda
├── docker-compose.yml                 # Microservicios
├── start.sh                           # Iniciar sistema
├── stop.sh                            # Detener sistema
├── restart.sh                         # Reiniciar sistema
├── logs.sh                            # Ver logs
├── test-reserva.sh                    # Prueba rápida
├── Makefile                           # Comandos make
└── README.md                          # Este archivo
```

---

## 🔧 Servicios

### servicio-Pagos (9090) - Orquestador Principal

**Responsabilidad**: 
- Exponer API REST para iniciar Pagos
- Desplegar procesos BPMN automáticamente
- Gestionar el agregado `ReservaViaje`

**Endpoints**:
```
POST /api/Pagos          - Iniciar nueva reserva
GET  /api/Pagos/{id}     - Consultar estado de reserva
```

**NO tiene workers** - Solo coordina el flujo BPMN

---

### servicio-clientes (9080)

**Responsabilidad**:
- Gestión de clientes
- Validación de tarjetas de crédito
- Control de estados del cliente

**Workers Zeebe**:
- `obtener-datos-cliente`
- `validar-tarjeta-credito`
- `actualizar-estado-cliente`
- `revertir-estado-cliente`

**Agregado**: `Cliente`

---

### servicio-vuelos (9081)

**Responsabilidad**:
- Reserva de vuelos
- Cancelación de vuelos (compensación)

**Workers Zeebe**:
- `reservar-vuelo`
- `cancelar-vuelo`

**Agregado**: `ReservaVuelo`

---

### servicio-hoteles (9082)

**Responsabilidad**:
- Reserva de hoteles
- Cancelación de hoteles (compensación)

**Workers Zeebe**:
- `reservar-hotel`
- `cancelar-hotel`

**Agregado**: `ReservaHotel`

---

### servicio-coches (9083)

**Responsabilidad**:
- Reserva de coches de alquiler
- Cancelación de coches (compensación)

**Workers Zeebe**:
- `reservar-coche`
- `cancelar-coche`

**Agregado**: `ReservaCoche`

---

### servicio-pagos (9084)

**Responsabilidad**:
- Procesamiento de pagos
- Confirmación de Pagos
- Reversión de pagos
- Marcado de advertencias

**Workers Zeebe**:
- `procesar-pago`
- `confirmar-reserva`
- `revertir-estado-cliente`
- `marcar-reserva-advertencia`

**Agregado**: `Pago`

---

## 📋 Procesos BPMN

### Proceso Principal

**ID**: `proceso-principal`  
**Descripción**: Flujo orquestador que coordina todo el proceso de reserva

**Flujo**:
```
1. ✅ Validar Datos de Entrada
2. 👤 Gestión de Cliente (Call Activity)
3. 📋 Revisar Datos (User Task)
4. 🎟️ Proceso de Reserva (Call Activity)
5. 💳 Proceso de Pago (Call Activity)
6. ✅ Reserva Completada
```

---

### Subproceso: Gestión de Cliente

**ID**: `subproceso-gestion-cliente`  
**Descripción**: Valida cliente y tarjeta de crédito

**Tareas**:
- Obtener datos del cliente
- Validar cliente encontrado
- Validar tarjeta de crédito
- Actualizar estado: `EN_PROCESO_RESERVA`

**Errores Manejados**:
- `ERROR_CLIENTE_NO_ENCONTRADO`
- `ERROR_TARJETA_INVALIDA`

---

### Subproceso: Proceso de Reserva

**ID**: `subproceso-proceso-reserva`  
**Descripción**: Pagos paralelas con compensaciones automáticas

**Características**:
- ⚡ Pagos paralelas (vuelo, hotel, coche)
- 👤 3 User Tasks de revisión
- 🔁 Compensaciones automáticas en caso de error
- 📝 Eventos no interrumpibles (actualización de tarjeta)

**Flujo Paralelo**:
```
Gateway Split
  ├→ Reservar Vuelo  → Revisar Vuelo
  ├→ Reservar Hotel  → Revisar Hotel
  └→ Reservar Coche  → Revisar Coche
Gateway Join
```

**Compensaciones**:
- Si falla cualquier reserva → Cancela todas automáticamente
- Patrón Saga implementado

---

### Subproceso: Proceso de Pago

**ID**: `subproceso-pago`  
**Descripción**: Procesamiento de pago con rollback

**Happy Path**:
```
1. Procesar Pago
2. Confirmar Reserva Completa
3. Actualizar Estado: CONFIRMADA
4. Fin: Viaje Reservado con Éxito
```

**Path de Error (Pago)**:
```
1. Procesar Pago → ERROR
2. Compensar Reserva (mensaje)
3. Notificar Cliente
4. Fin: Reserva No Completada
```

**Path de Error (Actualización)**:
```
1. Actualizar Estado → ERROR
2. Revertir Estado Cliente
3. Marcar Reserva con Advertencia
4. Fin: Reserva Confirmada con Advertencia
```

---

## 🌐 API REST

### Endpoint Principal: Iniciar Reserva

**URL**: `POST http://localhost:9090/api/Pagos`

**Request Body**:
```json
{
  "clienteId": "CLI-001",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2025-12-15",
  "fechaFin": "2025-12-20",
  "monto": 1500.00
}
```

**Response (201 Created)**:
```json
{
  "reservaId": "550e8400-e29b-41d4-a716-446655440000",
  "processInstanceKey": 2251799813685249,
  "estado": "INICIADA",
  "fechaCreacion": "2025-10-18T10:30:00",
  "mensaje": "Reserva iniciada correctamente. El proceso BPMN está en ejecución."
}
```

**Validaciones**:
- ✅ Todos los campos son obligatorios
- ✅ `fechaInicio` debe ser futura
- ✅ `fechaFin` debe ser posterior a `fechaInicio`
- ✅ `monto` debe ser positivo

---

### Endpoint: Consultar Reserva

**URL**: `GET http://localhost:9090/api/Pagos/{reservaId}`

**Response**:
```json
{
  "reservaId": "550e8400-e29b-41d4-a716-446655440000",
  "estado": "CONFIRMADA",
  "processInstanceKey": 2251799813685249,
  "detalleVuelo": {
    "numeroVuelo": "IB1234",
    "aerolinea": "Iberia",
    "asiento": "12A"
  },
  "detalleHotel": {
    "nombreHotel": "Hotel Barcelona Plaza",
    "numeroHabitacion": "305"
  },
  "detalleCoche": {
    "modelo": "Toyota Corolla",
    "matricula": "1234ABC"
  },
  "numeroConfirmacion": "CONF-1729249800000"
}
```

---

### Documentación Swagger

Cada servicio expone su documentación Swagger:

- **Pagos**: http://localhost:9090/swagger-ui.html
- **Clientes**: http://localhost:9080/swagger-ui.html
- **Vuelos**: http://localhost:9081/swagger-ui.html
- **Hoteles**: http://localhost:9082/swagger-ui.html
- **Coches**: http://localhost:9083/swagger-ui.html
- **Pagos**: http://localhost:9084/swagger-ui.html

---

## 🧪 Testing

### Prueba Manual Rápida

```bash
# Script automatizado
./test-reserva.sh
```

### Pruebas con curl

#### 1. Happy Path (Reserva Exitosa)

```bash
curl -X POST http://localhost:9090/api/Pagos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI-001",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2025-12-15",
    "fechaFin": "2025-12-20",
    "monto": 1500.00
  }'
```

**Resultado Esperado**:
- ✅ Reserva creada con `reservaId` y `processInstanceKey`
- ✅ User Task "Revisar Datos" aparece en Tasklist
- ✅ 3 Pagos (vuelo, hotel, coche) se procesan en paralelo
- ✅ 3 User Tasks de revisión aparecen en Tasklist
- ✅ Pago procesado correctamente
- ✅ Estado final: `CONFIRMADA`

---

#### 2. Error: Monto Alto (> 10000)

```bash
curl -X POST http://localhost:9090/api/Pagos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "CLI-001",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2025-12-15",
    "fechaFin": "2025-12-20",
    "monto": 15000.00
  }'
```

**Resultado Esperado**:
- ✅ Validación y cliente OK
- ✅ Pagos completadas
- ❌ Error en `procesar-pago` (monto excede límite)
- ❌ Compensaciones ejecutadas automáticamente
- ❌ Estado final: Error en pago

---

#### 3. Error: Datos Inválidos

```bash
curl -X POST http://localhost:9090/api/Pagos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "",
    "origen": "Madrid",
    "destino": "Barcelona"
  }'
```

**Resultado Esperado**:
- ❌ Error 400 Bad Request
- ❌ Validación falla antes de iniciar el proceso

---

### Completar User Tasks en Tasklist

1. Abrir http://localhost:8081 (demo/demo)
2. Ver tareas asignadas
3. Completar cada tarea:
   - 📋 Revisar Datos de Entrada
   - ✈️ Revisar Reserva de Vuelo
   - 🏨 Revisar Reserva de Hotel
   - 🚗 Revisar Reserva de Coche

---

## 📊 Monitoreo

### Camunda Operate (http://localhost:8080)

**Credenciales**: demo/demo

**Funcionalidades**:
- Ver instancias de procesos activas/completadas/fallidas
- Navegar por Call Activities jerárquicamente
- Inspeccionar variables de proceso
- Resolver incidents manualmente
- Ver métricas de rendimiento

**Navegación**:
```
1. Processes → proceso-principal
2. Click en instancia activa
3. Click en Call Activity (ej: Gestión de Cliente)
4. "View Called Process Instance"
5. Ver detalle del subproceso
```

---

### Camunda Tasklist (http://localhost:8081)

**Credenciales**: demo/demo

**Funcionalidades**:
- Ver User Tasks pendientes
- Completar tareas asignadas
- Ver formularios asociados
- Filtrar por proceso/estado

---

### Logs de Servicios

```bash
# Ver logs de todos los servicios
./logs.sh

# Ver logs de un servicio específico
./logs.sh clientes
./logs.sh vuelos
./logs.sh hoteles
./logs.sh coches
./logs.sh pagos
./logs.sh Pagos

# Logs de Camunda
./logs.sh zeebe
./logs.sh operate
./logs.sh tasklist
```

O con docker:
```bash
docker logs -f servicio-Pagos
docker logs -f zeebe
```

---

### Health Checks

```bash
# Verificar salud de servicios
curl http://localhost:9090/actuator/health
curl http://localhost:9080/actuator/health
# ... etc

# Verificar Zeebe
curl http://localhost:9600/ready
curl http://localhost:9600/health
```

---

### Consolas H2 (Bases de Datos)

Cada servicio tiene su propia base de datos H2:

```
URL: http://localhost:908X/h2-console

JDBC URL: jdbc:h2:mem:clientes_db   (9080)
          jdbc:h2:mem:vuelos_db     (9081)
          jdbc:h2:mem:hoteles_db    (9082)
          jdbc:h2:mem:coches_db     (9083)
          jdbc:h2:mem:pagos_db      (9084)
          jdbc:h2:mem:Pagos_db   (9090)

Usuario: sa
Password: (vacío)
```

---

## 🐛 Troubleshooting

### Problema: Puertos en Uso

**Síntoma**: Error al iniciar servicios

**Solución**:
```bash
# Ver qué proceso está usando el puerto
lsof -i :9090

# Matar el proceso
kill -9 <PID>
```

---

### Problema: Camunda No Responde

**Síntoma**: `curl http://localhost:9600/ready` falla

**Solución**:
```bash
# Ver logs de Zeebe
docker logs zeebe

# Reiniciar Camunda
docker-compose -f docker-compose-camunda.yml restart

# Si persiste, limpiar y reiniciar
docker-compose -f docker-compose-camunda.yml down -v
./start.sh
```

---

### Problema: Proceso BPMN No Encontrado

**Síntoma**: `Process definition with key 'subproceso-gestion-cliente' not found`

**Solución**:
```bash
# Los procesos BPMN se despliegan automáticamente al iniciar servicio-Pagos
# Verificar logs del servicio-Pagos
docker logs servicio-Pagos | grep "Desplegando"

# Si no están desplegados, reiniciar el servicio
docker-compose restart servicio-Pagos
```

---

### Problema: Workers No Se Registran

**Síntoma**: Jobs quedan pendientes sin procesarse

**Solución**:
```bash
# Verificar que los microservicios están conectados a Zeebe
docker logs servicio-clientes | grep "Zeebe"

# Verificar conectividad
docker exec servicio-clientes ping zeebe

# Reiniciar el servicio problemático
docker-compose restart servicio-clientes
```

---

### Problema: Incident en Proceso

**Síntoma**: Proceso se detiene con incident en Operate

**Solución**:
1. Ir a Operate → Incidents
2. Ver el mensaje de error
3. Corregir el problema (datos, código, etc.)
4. Click en "Retry" en el incident

O desde CLI:
```bash
# Resolver incident manualmente
zbctl resolve incident <INCIDENT_KEY>
```

---

### Problema: User Task No Aparece en Tasklist

**Síntoma**: Proceso se detiene en User Task pero no aparece en Tasklist

**Solución**:
```bash
# Verificar en Operate que el proceso está en el User Task
# Refrescar Tasklist (Ctrl+R)

# Verificar asignación en el BPMN
# Debe tener: <zeebe:assignmentDefinition assignee="demo" />
```

---

## 📝 Comandos Útiles

### Makefile

```bash
# Ver ayuda
make help

# Iniciar sistema
make start

# Detener sistema
make stop

# Ver logs
make logs

# Ver logs de un servicio
make logs-clientes
make logs-Pagos

# Limpiar sistema (elimina volúmenes)
make clean

# Compilar microservicios
make build

# Crear reserva de prueba
make test-reserva
```

---

### Scripts Bash

```bash
# Iniciar sistema completo
./start.sh

# Detener sistema
./stop.sh

# Reiniciar sistema
./restart.sh

# Ver logs
./logs.sh
./logs.sh clientes

# Crear reserva de prueba
./test-reserva.sh
```

---

### Docker Compose

```bash
# Iniciar solo Camunda
docker-compose -f docker-compose-camunda.yml up -d

# Iniciar microservicios
docker-compose up -d

# Ver logs en tiempo real
docker-compose logs -f

# Detener todo
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Reconstruir imágenes
docker-compose up -d --build

# Ver estado de servicios
docker-compose ps
```

---

## 🎓 Buenas Prácticas Implementadas

### Arquitectura

✅ **Separación de Capas** (Dominio, Aplicación, Infraestructura)  
✅ **Inversión de Dependencias** (Puertos y Adaptadores)  
✅ **DDD con JMolecules** (Agregados, Entidades, Value Objects explícitos)  
✅ **Bounded Contexts** (Cada microservicio es un contexto acotado)

### BPMN

✅ **Subprocesos Reutilizables** (Call Activities)  
✅ **Patrón Saga** con compensaciones automáticas  
✅ **Manejo de Errores** con Boundary Events  
✅ **Expresiones FEEL** en lugar de JavaScript  
✅ **IDs legibles en español** (kebab-case)

### Código

✅ **Logs con Iconos** para claridad visual  
✅ **Validación de Datos** con Jakarta Validation  
✅ **MapStruct** para mapeo automático DTOs  
✅ **OpenAPI/Swagger** para documentación  
✅ **Health Checks** en todos los servicios

### DevOps

✅ **Dockerfiles Multi-Stage** optimizados  
✅ **Health Checks** en Docker Compose  
✅ **Scripts de Automatización** (start, stop, logs)  
✅ **Makefile** con comandos útiles  
✅ **Variables de Entorno** centralizadas

---

## 📚 Recursos Adicionales

### Documentación

- [Documentación de Procesos BPMN](docs/doc_procesos_bpmn.md)
- [Camunda Platform 8 Docs](https://docs.camunda.io)
- [Zeebe Docs](https://docs.camunda.io/docs/components/zeebe/zeebe-overview/)

### Comunidad

- [Camunda Forum](https://forum.camunda.io)
- [GitHub Issues](https://github.com/camunda/camunda)

---

## 👥 Equipo y Contribución

### Autor

Sistema desarrollado siguiendo las mejores prácticas de arquitectura de microservicios y DDD.

### Contribuir

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## 📄 Licencia

Este proyecto es un ejemplo educativo de implementación de microservicios con Camunda Platform 8.

---

## 🎉 ¡Gracias por Usar el Sistema!

Si tienes preguntas o encuentras problemas, por favor abre un issue en el repositorio.

**¡Felices Pagos de Viaje!** ✈️🏨🚗
