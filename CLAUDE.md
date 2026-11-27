# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository. Each microservice has its own `servicio-*/CLAUDE.md` with its port, database, aggregates, workers and endpoints; Claude Code loads it when working inside that module.

## Build & Run Commands

```bash
# Compile all microservices (skipping tests)
mvn clean package -DskipTests

# Compile / test a single microservice
mvn clean package -DskipTests -pl servicio-clientes
mvn test -pl servicio-clientes
mvn test -pl servicio-clientes -Dtest=<Clase>Test

# Run integration tests
mvn verify -pl servicio-clientes

# Build Docker images and start everything
./scripts/build-all.sh

# Start full system (Camunda + microservices)
./scripts/start.sh

# Start only Camunda infrastructure / only the microservices (requires Camunda running)
docker-compose -f docker-compose-camunda.yml up -d
docker-compose up -d

# View logs for a specific service
./scripts/logs.sh clientes   # or vuelos, hoteles, coches, pagos, reservas
```

## Architecture Overview

This is a **Maven multi-module project** (`groupId: dev.javacadabra`, `artifactId: reservasviaje`) with 6 Spring Boot microservices orchestrated by **Camunda Platform 8** (Zeebe engine).

| Module | Port | Role | Details |
|--------|------|------|---------|
| `servicio-reservas` | 9090 | Public API, BPMN deployment, flight/hotel/car booking workers and their compensations | [CLAUDE.md](servicio-reservas/CLAUDE.md) |
| `servicio-clientes` | 9080 | Customer lifecycle & credit card validation | [CLAUDE.md](servicio-clientes/CLAUDE.md) |
| `servicio-pagos` | 9084 | Payment processing, booking confirmation, compensation trigger | [CLAUDE.md](servicio-pagos/CLAUDE.md) |
| `servicio-vuelos` | 9081 | Flight domain model (no Zeebe workers yet) | [CLAUDE.md](servicio-vuelos/CLAUDE.md) |
| `servicio-hoteles` | 9082 | Hotel domain model (no Zeebe workers yet) | [CLAUDE.md](servicio-hoteles/CLAUDE.md) |
| `servicio-alquiler-coches` | 9083 | Car rental domain model (no Zeebe workers yet) | [CLAUDE.md](servicio-alquiler-coches/CLAUDE.md) |

Camunda runs via Docker using the unified `camunda/camunda:8.9.6` image: Zeebe gRPC :26500, REST/Operate/Tasklist :8080 (management :9600). Operate: http://localhost:8080/operate, Tasklist: http://localhost:8080/tasklist.

### Hexagonal Architecture (per microservice)

Every microservice follows the same package structure under `dev.javacadabra.reservasviaje.<domain>/`:

```
dominio/
  modelo/agregado/     — @AggregateRoot (JMolecules)
  modelo/entidad/      — @Entity
  modelo/objetovalor/  — @ValueObject
  repositorio/         — Port interfaces (outbound)
  excepcion/           — Domain exceptions
  evento/              — @DomainEvent

aplicacion/
  servicio/            — Use case implementations
  puerto/entrada/      — Inbound port interfaces (use cases)
  puerto/salida/       — Outbound port interfaces
  dto/entrada/         — Request DTOs
  dto/salida/          — Response DTOs
  mapper/              — MapStruct mappers

infraestructura/
  adaptador/entrada/rest/     — REST controllers
  adaptador/entrada/camunda/  — Zeebe @JobWorker classes
  adaptador/salida/persistencia/ — JPA entities, Spring Data repos, adaptor impls
  configuracion/              — Spring configuration
```

**Dependency rule**: `infraestructura` → `aplicacion` → `dominio`. The domain layer has no framework dependencies.

### Zeebe Workers

Workers are `@Component` classes under `infraestructura/adaptador/entrada/camunda/` annotated with `@JobWorker(type = "job-type-name")`. They receive variables via `ActivatedJob.getVariablesAsMap()` and return `Map<String, Object>` to set process variables. Business errors are thrown as `BpmnError.bpmnError("ERROR_CODE", message, ...)` with an `ERROR_*` code that must match the BPMN boundary event. The BPMN processes and the Saga live in `servicio-reservas` (see its CLAUDE.md).

### Key Configuration

Each service connects to Zeebe via `application.yml`:
```yaml
camunda:
  client:
    mode: self-managed
    zeebe:
      grpc-address: http://127.0.0.1:26500   # local dev
      rest-address: http://127.0.0.1:8080
    auth:
      username: demo
      password: demo
```
In Docker, environment variables `CAMUNDA_CLIENT_ZEEBE_GRPCADDRESS` and `CAMUNDA_CLIENT_ZEEBE_RESTADDRESS` override these to point at the `zeebe` container.

Databases are H2 in-memory (development), one isolated DB per service defined in its own `application.yml`.

### Lombok + MapStruct

The compiler is configured with `lombok-mapstruct-binding` to ensure Lombok processes before MapStruct. Mapper interfaces live in `aplicacion/mapper/` (application layer) and `infraestructura/adaptador/salida/persistencia/mapper/` (entity↔domain mapping).

### Testing

No module has tests yet (every `src/test/java` is empty), although all of them declare `spring-boot-starter-test`, JUnit 5 and `archunit-junit5`. The intended stack: Mockito unit tests for domain and application, ArchUnit rules for the hexagonal boundaries, `@SpringBootTest` with H2 plus Testcontainers (PostgreSQL) for integration, and `@ZeebeSpringTest` for processes.

<!-- mh:inicio -->
## Meta-harness
Brain: brain/   (memoria del proyecto: consulta brain/wiki/index.md antes de decidir; skill `brain`)
Diagramas: docs/diagramas/   (skill `excalidraw`)
Vistas: brain/tableros/   (archivos .base; skill `obsidian-bases`)
- Carriles Responder · FTD · ASDD; constitución en brain/factory/constitucion.md; glosario en brain/wiki/factoria/glosario.md.
- Maven con `node .mh/scripts/mvnq.mjs <objetivos>`; riesgo del cambio con `node .mh/scripts/riesgo.mjs`.
- Arranque y smoke test: `./init.sh`.
<!-- mh:fin -->
