# 📝 Changelog

Todos los cambios notables de este proyecto serán documentados en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.0.0/),
y este proyecto se adhiere a [Versionado Semántico](https://semver.org/lang/es/).

---

## [Unreleased]

### 🔄 En Desarrollo (rama: feature/vaadin-ui)

#### Añadido
- Dashboard principal con estadísticas de reservas
- Formulario wizard para nueva reserva
- Vista de gestión de clientes

#### En Progreso
- Integración con Zeebe REST API
- Notificaciones en tiempo real con WebSocket

---

## [1.0.0] - 2024-12-15

### 🎉 Release Inicial - MVP

Primera versión funcional del Sistema de Reservas de Viaje con arquitectura de microservicios y Camunda Platform 8.

#### ✨ Añadido

##### Arquitectura
- Arquitectura de microservicios con 6 servicios independientes
- Arquitectura Hexagonal en cada microservicio
- Domain-Driven Design con anotaciones JMolecules
- Separación clara de capas: Dominio, Aplicación, Infraestructura

##### Microservicios
- **servicio-clientes** (Puerto 9080)
  - Gestión de clientes con validación
  - Validación de tarjetas de crédito
  - Gestión de estados del cliente
  - API REST con OpenAPI
- **servicio-vuelos** (Puerto 9081)
  - Reservas de vuelos
  - Cancelaciones con compensación
  - Job Workers para Camunda
- **servicio-hoteles** (Puerto 9082)
  - Reservas de hoteles
  - Cancelaciones con compensación
  - Integración con Zeebe
- **servicio-alquiler-coches** (Puerto 9083)
  - Reservas de vehículos
  - Cancelaciones con compensación
  - Workers de Camunda
- **servicio-pagos** (Puerto 9084)
  - Procesamiento de pagos
  - Confirmación de reservas
  - Manejo de errores de pago
- **servicio-reservas** (Puerto 9090)
  - Coordinador BPMN principal
  - Despliegue de procesos
  - Orquestación de workflows

##### Procesos BPMN
- Proceso principal de reserva de viaje
- Subproceso: Gestión de Cliente
  - Validación de existencia
  - Validación de tarjeta
  - Actualización de estado
- Subproceso: Proceso de Reserva
  - Reservas paralelas (Gateway paralelo)
  - Boundary events de compensación
  - User tasks de revisión
  - Subproceso de actualización de tarjeta (no interrumpible)
- Subproceso: Proceso de Pago
  - Procesamiento de pago
  - Confirmación de reserva
  - Manejo de errores con compensación

##### Patrón Saga
- Implementación completa de patrón Saga
- Compensaciones automáticas en caso de error
- Boundary events en tareas de reserva
- Subprocesos de error con compensación paralela
- Mensajes de compensación manual

##### Integración Camunda
- Camunda Platform 8.7 (Zeebe, Operate, Tasklist)
- Zeebe Client con Spring Boot Starter
- Job Workers con anotación `@JobWorker`
- Expresiones FEEL en BPMN
- Correlación por `reservaId`

##### Persistencia
- Spring Data JPA
- H2 Database en memoria para desarrollo
- Repositorios JPA
- Entidades JPA separadas del dominio
- DTOs para capa de aplicación

##### APIs REST
- Controllers REST en todos los microservicios
- Validación con Bean Validation
- Mapeo automático con MapStruct
- Documentación OpenAPI/Swagger en cada servicio
- Health checks con Spring Actuator

##### Utilidades
- Lombok para reducir boilerplate
- Apache Commons Lang (StringUtils)
- Logs estructurados con iconos (✅, ❌, 🔍, etc.)
- Manejo global de excepciones

##### DevOps
- Dockerfiles para cada microservicio
- docker-compose-camunda.yml para infraestructura
- docker-compose.yml para microservicios
- Script `start.sh` automatizado
- Separación de infraestructura Camunda

##### Documentación
- README.md completo con arquitectura
- ROADMAP.md con plan de versiones
- CHANGELOG.md (este archivo)
- Documentación de procesos BPMN
- Guía de instalación
- Casos de uso documentados

##### Testing
- Estructura base para tests unitarios
- Configuración de Testcontainers
- Tests de arquitectura con ArchUnit (preparado)

##### Datos de Prueba
- Clientes precargados:
  - Vicente Priego (CLI-001)
  - Verónica Lesmes (CLI-002)
  - Juan Pérez (CLI-003) - tarjeta inválida
- Escenarios de prueba documentados

#### 🔧 Configuración

##### Stack Tecnológico
- Java 21
- Spring Boot 3.5.6
- Camunda Platform 8.7
- Maven 3.9+
- Docker & Docker Compose

##### Dependencias Clave
- spring-boot-starter-camunda-sdk 8.5.10
- jmolecules-ddd 1.10.0
- mapstruct 1.6.3
- lombok 1.18.36
- commons-lang3 3.17.0
- springdoc-openapi 2.7.0

##### Puertos
- 9080: servicio-clientes
- 9081: servicio-vuelos
- 9082: servicio-hoteles
- 9083: servicio-alquiler-coches
- 9084: servicio-pagos
- 9090: servicio-reservas
- 8080: Camunda Operate
- 8081: Camunda Tasklist
- 26500: Zeebe gRPC

#### 📚 Estructura de Paquetes
```
dev.javacadabra.reservasviaje.<dominio>/
├── aplicacion/
│   ├── dto/
│   ├── servicio/
│   └── puerto/
├── dominio/
│   ├── modelo/
│   ├── evento/
│   ├── excepcion/
│   └── servicio/
└── infraestructura/
    ├── adaptador/
    └── configuracion/
```

#### 🐛 Corregido
- IDs duplicados en procesos BPMN
- Posiciones solapadas en diagramas BPMN
- Boundary events mal conectados

#### 🔒 Seguridad
- Validación de entrada en todos los DTOs
- Sanitización de strings con Commons Lang
- Health checks sin información sensible

---

## [0.1.0] - 2024-12-01

### 🚧 Pre-release - Desarrollo Inicial

#### Añadido
- Estructura inicial del proyecto
- Configuración de Maven multi-módulo
- Diagramas BPMN iniciales
- Docker Compose básico

---

## Tipos de Cambios

- **✨ Añadido**: para nuevas características
- **🔧 Cambiado**: para cambios en funcionalidades existentes
- **❌ Obsoleto**: para características que serán eliminadas
- **🗑️ Eliminado**: para características eliminadas
- **🐛 Corregido**: para corrección de errores
- **🔒 Seguridad**: en caso de vulnerabilidades

---

## Enlaces

- [Repositorio](https://github.com/tu-usuario/sistema-reservas-viaje)
- [Issues](https://github.com/tu-usuario/sistema-reservas-viaje/issues)
- [Pull Requests](https://github.com/tu-usuario/sistema-reservas-viaje/pulls)

---

**Mantenido por**: [Tu Nombre](https://github.com/tu-usuario)
