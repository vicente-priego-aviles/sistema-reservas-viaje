# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Compile all microservices (skipping tests)
mvn clean package -DskipTests

# Compile a single microservice
cd servicio-clientes && mvn clean package -DskipTests

# Run tests for all modules
mvn test

# Run tests for a single module
mvn test -pl servicio-clientes

# Run a specific test class
mvn test -pl servicio-clientes -Dtest=ClienteServicioTest

# Run integration tests
mvn verify -pl servicio-clientes

# Build Docker images and start everything
./scripts/build-all.sh

# Start full system (Camunda + microservices)
./scripts/start.sh

# Start only Camunda infrastructure
docker-compose -f docker-compose-camunda.yml up -d

# Start microservices (requires Camunda already running)
docker-compose up -d

# View logs for a specific service
./scripts/logs.sh clientes   # or vuelos, hoteles, coches, pagos, reservas
```

## Architecture Overview

This is a **Maven multi-module project** (`groupId: dev.javacadabra`, `artifactId: reservasviaje`) with 6 Spring Boot microservices orchestrated by **Camunda Platform 8** (Zeebe engine).

### Modules and Ports

| Module | Port | Role |
|--------|------|------|
| `servicio-reservas` | 9090 | Orchestrator — exposes the public API, deploys BPMN processes, has NO Zeebe workers |
| `servicio-clientes` | 9080 | Customer lifecycle & credit card validation |
| `servicio-vuelos` | 9081 | Flight booking and cancellation |
| `servicio-hoteles` | 9082 | Hotel booking and cancellation |
| `servicio-alquiler-coches` | 9083 | Car rental booking and cancellation |
| `servicio-pagos` | 9084 | Payment processing and confirmation |

Camunda runs via Docker: Zeebe (gRPC :26500, REST :8088), Operate (:8080), Tasklist (:8081).

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

### BPMN Process Flow

Processes are stored in `servicio-reservas/src/main/resources/bpmn/` and deployed automatically at startup by `BpmnDeploymentService`.

There are 4 BPMN processes (all call activities from the main one):
1. `proceso-principal` — Top-level orchestrator
2. `subproceso-gestion-cliente` — Validates client and credit card
3. `subproceso-reserva` — Parallel flight + hotel + car booking with Saga compensations
4. `subproceso-pago` — Payment processing with rollback paths

The `bpmn/` root directory contains modifiable copies for Camunda Modeler. The runtime copies are inside `servicio-reservas/src/main/resources/bpmn/`.

### Zeebe Workers

Workers are `@Component` classes under `infraestructura/adaptador/entrada/camunda/` annotated with `@JobWorker(type = "job-type-name")`. They receive variables via `ActivatedJob.getVariablesAsMap()` and return `Map<String, Object>` to set process variables. Business errors are thrown as `ZeebeBpmnError("ERROR_CODE", message)`.

### Saga / Compensation Pattern

The reservation subprocess uses BPMN compensation boundary events. Each booking task (`reservar-vuelo`, `reservar-hotel`, `reservar-coche`) has an attached compensation boundary event that triggers the matching cancellation task (`cancelar-vuelo`, etc.) if payment fails.

### Key Configuration

Each service connects to Zeebe via `application.yml`:
```yaml
camunda:
  client:
    mode: self-managed
    zeebe:
      grpc-address: http://127.0.0.1:26500   # local dev
      rest-address: http://127.0.0.1:8088
    auth:
      username: demo
      password: demo
```
In Docker, environment variables `CAMUNDA_CLIENT_ZEEBE_GATEWAYADDRESS` and `CAMUNDA_CLIENT_ZEEBE_RESTADDRESS` override these to point at the `zeebe` container.

Databases are H2 in-memory (development). Each service has its own isolated DB (`cliente_db`, `vuelos_db`, etc.) defined in its own `application.yml`.

### Lombok + MapStruct

The compiler is configured with `lombok-mapstruct-binding` to ensure Lombok processes before MapStruct. Mapper interfaces live in `aplicacion/mapper/` (application layer) and `infraestructura/adaptador/salida/persistencia/mapper/` (entity↔domain mapping).

### Testing

- **Unit**: Domain and application layer tests with Mockito (`@ExtendWith(MockitoExtension.class)`)
- **Architecture**: ArchUnit rules enforcing hexagonal layer boundaries
- **Integration**: `@SpringBootTest` with H2; Testcontainers (PostgreSQL) for persistence tests
- **Camunda**: `@ZeebeSpringTest` with embedded Zeebe engine

```bash
# Architecture tests (ArchUnit)
mvn test -pl servicio-clientes -Dtest=ArchitectureTest
```
