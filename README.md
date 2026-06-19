# 🎯 Sistema de Reservas de Viaje con Camunda Platform 8

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Camunda](https://img.shields.io/badge/Camunda-8.7-blue?style=flat-square&logo=camunda)](https://camunda.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

> Sistema empresarial de reservas de viajes construido con **arquitectura de microservicios**, **Camunda Platform 8** como orquestador BPMN, **arquitectura hexagonal**, y **Domain-Driven Design (DDD)**.

---

## 📋 Tabla de Contenidos

- [✨ Características Principales](#-características-principales)
- [🏗️ Arquitectura](#️-arquitectura)
- [📦 Prerequisitos](#-prerequisitos)
- [🚀 Inicio Rápido](#-inicio-rápido)
- [🌐 API REST](#-api-rest)
- [📊 Monitoreo](#-monitoreo)
- [🧪 Testing](#-testing)
- [📁 Estructura del Proyecto](#-estructura-del-proyecto)
- [🐛 Troubleshooting](#-troubleshooting)
- [📚 Documentación Adicional](#-documentación-adicional)
- [🤝 Contribución](#-contribución)

---

## ✨ Características Principales

- 🎯 **Arquitectura Hexagonal** — Separación clara entre dominio, aplicación e infraestructura
- 🧩 **Domain-Driven Design** — Modelado explícito con JMolecules
- 🔄 **Orquestación BPMN** — Camunda Platform 8 como motor de workflows
- ⚡ **Procesamiento Paralelo** — Reservas simultáneas de vuelo, hotel y coche
- 🔁 **Patrón Saga** — Compensaciones automáticas en caso de error
- 📊 **Observabilidad** — Health checks, métricas y logs estructurados
- 🐳 **Containerización** — Docker y Docker Compose listos para producción
- 📖 **OpenAPI** — Documentación automática de APIs REST
- 🧪 **Testing Completo** — Unitarios, integración y arquitectura

---

## 🏗️ Arquitectura

### Diagrama de Alto Nivel

```
                    ┌─────────────────────┐
                    │  Cliente / Frontend │
                    └──────────┬──────────┘
                               │ HTTP POST
                               ↓
                    ┌─────────────────────┐
                    │ servicio-reservas   │
                    │   (Orquestador)     │
                    │   Puerto: 9090      │
                    └──────────┬──────────┘
                               │ Inicia Proceso BPMN
                               ↓
                    ┌─────────────────────┐
                    │  Camunda Platform 8 │
                    │   - Zeebe (26500)   │
                    │   - Operate (8080)  │
                    │   - Tasklist (8081) │
                    └──────────┬──────────┘
                               │
         ┌─────────────────────┼─────────────────────┐
         ↓                     ↓                     ↓
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│  Gestión       │   │  Proceso       │   │  Proceso       │
│  Cliente       │   │  Reserva       │   │  Pago          │
│ (subproceso)   │   │ (subproceso)   │   │ (subproceso)   │
└────────┬───────┘   └────────┬───────┘   └────────┬───────┘
         ↓                    ↓                     ↓
┌────────────────┐   ┌────────────────┐   ┌────────────────┐
│ servicio-      │   │ servicio-      │   │ servicio-      │
│ clientes       │   │ vuelos/hoteles/│   │ pagos          │
│ (9080)         │   │ coches         │   │ (9084)         │
│                │   │ (9081/82/83)   │   │                │
└────────────────┘   └────────────────┘   └────────────────┘
     Workers              Workers              Workers
```

### Stack Tecnológico

| Categoría | Tecnología | Versión | Propósito |
|-----------|-----------|---------|-----------|
| **Lenguaje** | Java | 21 | Desarrollo backend |
| **Framework** | Spring Boot | 3.5.6 | Framework base |
| **Orquestador** | Camunda Platform 8 | 8.7 | Motor de workflows BPMN |
| **Persistencia** | Spring Data JPA | — | Acceso a datos |
| **BD Desarrollo** | H2 Database | 2.3.232 | Base de datos en memoria |
| **Utilidades** | Lombok | 1.18.36 | Reducción de boilerplate |
| **Mapeo** | MapStruct | 1.6.3 | DTO ↔ Entity |
| **Validación** | Jakarta Bean Validation | — | Validación de entrada |
| **DDD** | JMolecules | 1.10.0 | Anotaciones DDD explícitas |
| **API Docs** | SpringDoc OpenAPI | 2.7.0 | Documentación automática |
| **Containerización** | Docker + Compose | — | Despliegue |

### Microservicios

| Puerto | Microservicio | Responsabilidad | Agregado DDD |
|--------|--------------|-----------------|--------------|
| **9080** | 👥 **servicio-clientes** | Gestión de clientes, validación de tarjetas, estados | `Cliente` |
| **9081** | ✈️ **servicio-vuelos** | Reservas y cancelaciones de vuelos | `ReservaVuelo` |
| **9082** | 🏨 **servicio-hoteles** | Reservas y cancelaciones de hoteles | `ReservaHotel` |
| **9083** | 🚗 **servicio-alquiler-coches** | Reservas y cancelaciones de vehículos | `ReservaAlquilerCoche` |
| **9084** | 💳 **servicio-pagos** | Procesamiento de pagos, confirmaciones | `Pago` |
| **9090** | 🎯 **servicio-reservas** | Coordinador BPMN — API pública, despliegue de procesos | `ReservaViaje` |

**Infraestructura Camunda Platform 8:**

| Puerto | Componente | Acceso |
|--------|-----------|--------|
| **8080** | Camunda Operate | `http://localhost:8080` (demo/demo) |
| **8081** | Camunda Tasklist | `http://localhost:8081` (demo/demo) |
| **26500** | Zeebe gRPC | Workers de los microservicios |

### Arquitectura Hexagonal

Cada microservicio implementa Arquitectura Hexagonal con separación estricta de capas:

```
servicio-<nombre>/
├── 🟢 dominio/              # Lógica de negocio pura (sin dependencias externas)
│   ├── modelo/
│   │   ├── agregado/        # @AggregateRoot
│   │   ├── entidad/         # @Entity
│   │   └── objetovalor/     # @ValueObject
│   ├── evento/              # @DomainEvent
│   ├── excepcion/           # Excepciones de negocio
│   └── repositorio/         # Interfaces de puertos de salida
│
├── 🔵 aplicacion/           # Casos de uso
│   ├── dto/entrada/         # Request DTOs
│   ├── dto/salida/          # Response DTOs
│   ├── servicio/            # Application Services
│   ├── mapper/              # MapStruct mappers
│   └── puerto/
│       ├── entrada/         # Interfaces de casos de uso
│       └── salida/          # Interfaces de puertos de salida
│
└── 🟡 infraestructura/      # Adaptadores
    ├── adaptador/
    │   ├── entrada/
    │   │   ├── rest/        # REST Controllers
    │   │   └── camunda/     # @JobWorker (Zeebe)
    │   └── salida/
    │       └── persistencia/ # JPA entities, Spring Data repos
    └── configuracion/       # Spring Configuration
```

**Regla de dependencias**: `infraestructura` → `aplicacion` → `dominio`. El dominio no depende de ningún framework.

### Procesos BPMN

Los procesos residen en `servicio-reservas/src/main/resources/bpmn/` y se despliegan automáticamente al arrancar.

| Proceso | ID | Descripción |
|---------|----|-------------|
| Principal | `proceso-principal` | Flujo orquestador completo |
| Gestión Cliente | `subproceso-gestion-cliente` | Valida cliente y tarjeta de crédito |
| Reserva | `subproceso-reserva` | Reservas paralelas (vuelo+hotel+coche) con compensaciones Saga |
| Pago | `subproceso-pago` | Procesamiento de pago con rollback |

**Flujo del Proceso Principal:**
```
1. Validar Datos de Entrada
2. Gestión de Cliente (Call Activity)
3. Revisar Datos (User Task)
4. Proceso de Reserva → paralelo: vuelo / hotel / coche (Call Activity)
5. Proceso de Pago (Call Activity)
6. Reserva Completada
```

**Patrón Saga en Proceso de Reserva:**
- Cada tarea de reserva tiene un boundary event de compensación asociado
- Si el pago falla → se cancelan vuelo, hotel y coche automáticamente
- Los workers usan `ZeebeBpmnError("ERROR_CODE", message)` para señalizar errores de negocio

---

## 📦 Prerequisitos

```bash
java --version   # Java 21+
mvn --version    # Maven 3.9+
docker --version # Docker con Compose
```

**Puertos necesarios:**

```
Camunda:         8080 (Operate), 8081 (Tasklist), 26500 (Zeebe gRPC)
Microservicios:  9080 (clientes), 9081 (vuelos), 9082 (hoteles),
                 9083 (coches), 9084 (pagos), 9090 (reservas)
```

### ⚠️ Paso obligatorio antes del primer arranque

`docker-compose-camunda.yml` requiere un archivo `connector-secrets.txt` para el servicio de Connectors. Este archivo no está en el repositorio (contiene secretos). Créalo a partir de la plantilla incluida:

```bash
cp connector-secrets.txt.example connector-secrets.txt
```

Para desarrollo local puedes dejarlo con el contenido por defecto (vacío). Si usas Camunda Connectors con servicios externos (Slack, HTTP, etc.), añade ahí las variables de entorno necesarias.

---

## 🚀 Inicio Rápido

```bash
# 1. Crear el archivo de secrets (solo la primera vez)
cp connector-secrets.txt.example connector-secrets.txt

# 2. Compilar y levantar todo el sistema
./build-and-run.sh
```

Para reiniciar sin recompilar: `./start.sh` · Para parar todo: `./stop-all.sh`

> 📖 **Guía completa paso a paso**: [docs/doc_quick_start.md](docs/doc_quick_start.md)  
> 📜 **Referencia de todos los scripts**: [SCRIPTS.md](SCRIPTS.md)

---

## 🌐 API REST

El punto de entrada principal es `servicio-reservas` (puerto 9090):

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/Pagos` | Iniciar una reserva de viaje |
| `GET` | `/api/Pagos/{reservaId}` | Consultar el estado de una reserva |

Cada microservicio expone además su propia API REST documentada en `/swagger-ui.html`.

> 📖 **Ejemplos completos, datos de prueba y escenarios de error**: [docs/doc_quick_start.md](docs/doc_quick_start.md)

---

## 📊 Monitoreo

- **Camunda Operate** (http://localhost:8080) — visualización de instancias de proceso, incidents y variables
- **Camunda Tasklist** (http://localhost:8081) — completar User Tasks del flujo
- **H2 Console** — cada servicio en `http://localhost:908X/h2-console` (usuario `sa`, sin contraseña)

> 📖 **Logs, consolas H2 y monitoreo detallado**: [docs/doc_quick_start.md](docs/doc_quick_start.md)

---

## 🧪 Testing

```bash
# Todos los tests de un módulo
mvn test -pl servicio-clientes

# Test específico
mvn test -pl servicio-clientes -Dtest=ClienteServicioTest

# Tests de arquitectura (ArchUnit)
mvn test -pl servicio-clientes -Dtest=ArchitectureTest

# Tests de integración
mvn verify -pl servicio-clientes
```

**Niveles de testing:**
- **Unitarios** — Dominio y aplicación con Mockito
- **Integración** — `@SpringBootTest` + H2; Testcontainers para PostgreSQL
- **Camunda** — `@ZeebeSpringTest` con Zeebe embebido
- **Arquitectura** — ArchUnit validando que las capas hexagonales no se violen

---

## 📁 Estructura del Proyecto

```
sistema-reservas-viaje/
│
├── 🗂️ bpmn/                               # Copias de BPMN para Camunda Modeler
│   ├── proceso-principal.bpmn
│   ├── subproceso-gestion-cliente.bpmn
│   ├── subproceso-reserva.bpmn
│   └── subproceso-pago.bpmn
│
├── 📚 docs/                               # Documentación detallada
│
├── 🐳 docker-compose-camunda.yml          # Infraestructura Camunda
├── 🐳 docker-compose.yml                  # Microservicios
│
├── 🏗️ servicio-clientes/                  # Puerto 9080
│   ├── src/main/java/dev/javacadabra/reservasviaje/cliente/
│   │   ├── dominio/
│   │   ├── aplicacion/
│   │   └── infraestructura/
│   ├── Dockerfile
│   └── pom.xml
│
├── ✈️ servicio-vuelos/                    # Puerto 9081
├── 🏨 servicio-hoteles/                   # Puerto 9082
├── 🚗 servicio-alquiler-coches/           # Puerto 9083
├── 💳 servicio-pagos/                     # Puerto 9084
│
├── 🎯 servicio-reservas/                  # Puerto 9090 (Orquestador)
│   └── src/main/resources/bpmn/          # Procesos BPMN desplegados al arrancar
│
├── 🔧 build-and-run.sh                    # Inicio completo (compilar + levantar todo)
├── 🔧 start.sh                            # Reinicio rápido (sin recompilar)
├── 🧹 limpieza.sh                         # Limpiar entorno Docker
└── 📦 pom.xml                             # Parent POM (multi-módulo)
```

---

## 🐛 Troubleshooting

### Camunda no responde

```bash
# Ver logs de Zeebe
docker logs zeebe

# Reiniciar Camunda
docker-compose -f docker-compose-camunda.yml restart

# Si persiste, limpiar y volver a levantar
./limpieza.sh
./build-and-run.sh
```

### Proceso BPMN no encontrado

**Síntoma**: `Process definition with key 'subproceso-gestion-cliente' not found`

Los procesos se despliegan automáticamente al iniciar `servicio-reservas`. Verificar logs:
```bash
docker logs servicio-reservas | grep -i "deploy"
docker-compose restart servicio-reservas
```

### Workers no se registran

**Síntoma**: Jobs quedan pendientes sin procesarse

```bash
docker logs servicio-clientes | grep -i "zeebe"
docker-compose restart servicio-clientes
```

### Incident en proceso

1. Abrir Operate → Incidents
2. Leer el mensaje de error
3. Corregir el problema y hacer click en "Retry"

### User Task no aparece en Tasklist

Verificar en Operate que el proceso está detenido en el User Task. Refrescar Tasklist (Ctrl+R). La tarea debe tener `<zeebe:assignmentDefinition assignee="demo" />` en el BPMN.

### Puerto en uso

```bash
lsof -i :9090
kill -9 <PID>
```

---

## 📚 Documentación Adicional

- 🏗️ [Arquitectura Detallada](docs/doc_arquitectura.md)
- 📊 [Procesos BPMN](docs/doc_procesos_bpmn.md)
- 🛠️ [Microservicios](docs/doc_microservicios.md)
- ⚙️ [Configuración](docs/doc_configuracion.md)
- 🧪 [Testing](docs/doc_testing.md)
- 🐳 [Deployment](docs/doc_deployment.md)
- 🚀 [Quick Start](docs/doc_quick_start.md)

---

## 🤝 Contribución

1. 🍴 Fork el proyecto
2. 🔀 Crea una rama para tu feature (`git checkout -b feature/nueva-funcionalidad`)
3. 💾 Commit tus cambios (`git commit -m 'feat: añadir nueva funcionalidad'`)
4. 📤 Push a la rama (`git push origin feature/nueva-funcionalidad`)
5. 🔃 Abre un Pull Request

Ver [CONTRIBUTING.md](CONTRIBUTING.md) para más detalles.

---

## 📄 Licencia

Este proyecto está licenciado bajo la Licencia MIT — ver [LICENSE](LICENSE) para más detalles.
