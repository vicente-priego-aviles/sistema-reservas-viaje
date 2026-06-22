# 🎟️ Servicio de Pagos - Sistema de Pagos de Viaje

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Camunda](https://img.shields.io/badge/Camunda-8.8.0-blue.svg)](https://camunda.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://github.com/vicente-priego-aviles/sistema-reservas-viaje/blob/main/LICENSE)

> Microservicio de gestión de Pagos (vuelos, hoteles y coches) implementado con **Arquitectura Hexagonal**, **Domain-Driven Design (DDD)** y orquestado por **Camunda Platform 8**.

---

## 📋 Tabla de Contenidos

- [Descripción General](#-descripción-general)
- [Arquitectura](#-arquitectura)
- [Stack Tecnológico](#️-stack-tecnológico)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Patrones de Diseño](#-patrones-de-diseño)
- [Workers de Camunda](#-workers-de-camunda)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Ejecución](#-instalación-y-ejecución)
- [Configuración](#️-configuración)
- [Endpoints API](#-endpoints-api)
- [Procesos BPMN](#-procesos-bpmn)
- [Testing](#-testing)
- [Mejores Prácticas Implementadas](#-mejores-prácticas-implementadas)
- [Roadmap](#-roadmap)
- [Contribución](#-contribución)
- [Licencia](#-licencia)

---

## 🎯 Descripción General

El **Servicio de Pagos** es un microservicio independiente que forma parte de un sistema distribuido de gestión de viajes. Su responsabilidad principal es gestionar la creación, modificación y cancelación de Pagos de:

- ✈️ **Vuelos**
- 🏨 **Hoteles**
- 🚗 **Coches de alquiler**

Este servicio implementa el **patrón Saga** para gestionar transacciones distribuidas con compensaciones automáticas en caso de fallo, garantizando la consistencia eventual del sistema.

### 🎓 Objetivo Didáctico

Este proyecto ha sido diseñado con fines **educativos y de aprendizaje** para desarrolladores backend que deseen profundizar en:

- Arquitectura Hexagonal (Ports & Adapters)
- Domain-Driven Design (DDD)
- Patrón Saga con compensaciones
- Orquestación de procesos con Camunda Platform 8
- Microservicios con Spring Boot
- Buenas prácticas en Java moderno (Java 21)

---

## 🏗️ Arquitectura

### Arquitectura Hexagonal (Ports & Adapters)

El servicio está estructurado siguiendo los principios de la **Arquitectura Hexagonal**, que separa claramente:

```
┌─────────────────────────────────────────────────────────────┐
│                    CAPA DE INFRAESTRUCTURA                  │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Adaptadores de Entrada (Drivers)          │   │
│  │  • REST Controllers (API REST)                      │   │
│  │  • Camunda Workers (Job Workers)                    │   │
│  └─────────────────────────────────────────────────────┘   │
│                            ▼                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              CAPA DE APLICACIÓN                     │   │
│  │  • Casos de Uso (Application Services)             │   │
│  │  • DTOs de Entrada/Salida                          │   │
│  │  • Puertos (Interfaces)                            │   │
│  └─────────────────────────────────────────────────────┘   │
│                            ▼                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              CAPA DE DOMINIO (CORE)                 │   │
│  │  • Agregados (Entities + Value Objects)            │   │
│  │  • Eventos de Dominio                              │   │
│  │  • Servicios de Dominio                            │   │
│  │  • Reglas de Negocio                               │   │
│  └─────────────────────────────────────────────────────┘   │
│                            ▲                                 │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Adaptadores de Salida (Driven)            │   │
│  │  • Repositorios JPA (Persistencia)                  │   │
│  │  • Event Publishers                                 │   │
│  │  • Clientes HTTP (otros microservicios)            │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Domain-Driven Design (DDD)

El diseño del dominio sigue los principios de **DDD** utilizando **JMolecules** para hacer explícitos los conceptos:

- **Agregados** (`@AggregateRoot`): ReservaVuelo, ReservaHotel, ReservaCoche
- **Entidades** (`@Entity`): Pasajero, DetalleReserva
- **Value Objects** (`@ValueObject`): ReservaId, DatosVuelo, DatosHotel, DatosCoche, PrecioReserva
- **Eventos de Dominio** (`@DomainEvent`): ReservaVueloCreadaEvento, ReservaVueloCanceladaEvento, etc.
- **Servicios de Dominio** (`@DomainService`): Lógica de negocio compleja

---

## 🛠️ Stack Tecnológico

### Backend

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21 | Lenguaje base con características modernas |
| **Spring Boot** | 3.5.6 | Framework principal |
| **Spring Web** | - | API REST |
| **Spring Data JPA** | - | Persistencia |
| **Spring Validation** | - | Validación de datos |
| **Spring Actuator** | - | Monitoreo y health checks |
| **Camunda Spring Zeebe** | 8.8.0 | Integración con Camunda Platform 8 |
| **H2 Database** | 2.3.232 | Base de datos en memoria (desarrollo) |
| **PostgreSQL** | - | Base de datos en producción (opcional) |

### Librerías y Herramientas

| Librería | Versión | Propósito |
|----------|---------|-----------|
| **Lombok** | 1.18.36 | Reducción de boilerplate |
| **MapStruct** | 1.6.3   | Mapeo entre DTOs y entidades |
| **Apache Commons Lang** | 3.18.0  | Utilidades (StringUtils, validaciones) |
| **JMolecules** | 1.10.0  | Anotaciones DDD explícitas |
| **SpringDoc OpenAPI** | 2.7.0   | Documentación API automática |

### Testing

| Framework | Versión | Propósito |
|-----------|---------|-----------|
| **JUnit 5** | - | Framework de testing |
| **Mockito** | - | Mocking y stubbing |
| **AssertJ** | - | Assertions fluidas |
| **Testcontainers** | 1.20.4 | Tests de integración con contenedores |
| **ArchUnit** | 1.3.0 | Validación de arquitectura hexagonal |

---

## 📁 Estructura del Proyecto

```
servicio-reservas/
├── src/
│   ├── main/
│   │   ├── java/dev/javacadabra/reservasviaje/reserva/
│   │   │   │
│   │   │   ├── aplicacion/                    # 🔵 CAPA DE APLICACIÓN
│   │   │   │   ├── dto/
│   │   │   │   │   ├── entrada/               # DTOs de entrada
│   │   │   │   │   │   ├── ReservarVueloDTO.java
│   │   │   │   │   │   ├── ReservarHotelDTO.java
│   │   │   │   │   │   └── ReservarCocheDTO.java
│   │   │   │   │   │
│   │   │   │   │   └── salida/                # DTOs de salida
│   │   │   │   │       ├── ReservaVueloRespuestaDTO.java
│   │   │   │   │       ├── ReservaHotelRespuestaDTO.java
│   │   │   │   │       └── ReservaCocheRespuestaDTO.java
│   │   │   │   │
│   │   │   │   ├── servicio/                  # Application Services
│   │   │   │   │   ├── ReservaVueloServicio.java
│   │   │   │   │   ├── ReservaHotelServicio.java
│   │   │   │   │   └── ReservaCocheServicio.java
│   │   │   │   │
│   │   │   │   └── puerto/                    # Interfaces (Ports)
│   │   │   │       ├── entrada/
│   │   │   │       │   ├── ReservarVueloCasoUso.java
│   │   │   │       │   ├── ReservarHotelCasoUso.java
│   │   │   │       │   ├── ReservarCocheCasoUso.java
│   │   │   │       │   ├── CancelarVueloCasoUso.java
│   │   │   │       │   ├── CancelarHotelCasoUso.java
│   │   │   │       │   └── CancelarCocheCasoUso.java
│   │   │   │       │
│   │   │   │       └── salida/
│   │   │   │           ├── ReservaVueloPuerto.java
│   │   │   │           ├── ReservaHotelPuerto.java
│   │   │   │           ├── ReservaCochePuerto.java
│   │   │   │           └── EventoPublicadorPuerto.java
│   │   │   │
│   │   │   ├── dominio/                       # 🟢 CAPA DE DOMINIO (CORE)
│   │   │   │   ├── modelo/
│   │   │   │   │   ├── agregado/              # @AggregateRoot
│   │   │   │   │   │   ├── ReservaVuelo.java
│   │   │   │   │   │   ├── ReservaHotel.java
│   │   │   │   │   │   └── ReservaCoche.java
│   │   │   │   │   │
│   │   │   │   │   ├── entidad/               # @Entity
│   │   │   │   │   │   ├── Pasajero.java
│   │   │   │   │   │   └── DetalleReserva.java
│   │   │   │   │   │
│   │   │   │   │   └── objetovalor/           # @ValueObject
│   │   │   │   │       ├── ReservaId.java
│   │   │   │   │       ├── EstadoReserva.java
│   │   │   │   │       ├── DatosVuelo.java
│   │   │   │   │       ├── DatosHotel.java
│   │   │   │   │       ├── DatosCoche.java
│   │   │   │   │       └── PrecioReserva.java
│   │   │   │   │
│   │   │   │   ├── evento/                    # @DomainEvent
│   │   │   │   │   ├── ReservaVueloCreadaEvento.java
│   │   │   │   │   ├── ReservaHotelCreadaEvento.java
│   │   │   │   │   ├── ReservaCocheCreadaEvento.java
│   │   │   │   │   ├── ReservaVueloCanceladaEvento.java
│   │   │   │   │   ├── ReservaHotelCanceladaEvento.java
│   │   │   │   │   └── ReservaCocheCanceladaEvento.java
│   │   │   │   │
│   │   │   │   ├── excepcion/
│   │   │   │   │   ├── ReservaNoEncontradaException.java
│   │   │   │   │   ├── ReservaDuplicadaException.java
│   │   │   │   │   ├── ReservaInvalidaException.java
│   │   │   │   │   └── CancelacionNoPermitidaException.java
│   │   │   │   │
│   │   │   │   └── servicio/                  # @DomainService
│   │   │   │       └── ValidadorReservaServicio.java
│   │   │   │
│   │   │   └── infraestructura/               # 🟡 CAPA DE INFRAESTRUCTURA
│   │   │       │
│   │   │       ├── adaptador/
│   │   │       │   │
│   │   │       │   ├── entrada/
│   │   │       │   │   ├── rest/              # Controllers REST
│   │   │       │   │   │   ├── ReservaVueloController.java
│   │   │       │   │   │   ├── ReservaHotelController.java
│   │   │       │   │   │   └── ReservaCocheController.java
│   │   │       │   │   │
│   │   │       │   │   └── camunda/           # Job Workers
│   │   │       │   │       ├── ReservaVueloWorker.java
│   │   │       │   │       ├── ReservaHotelWorker.java
│   │   │       │   │       ├── ReservaCocheWorker.java
│   │   │       │   │       ├── CancelarVueloWorker.java
│   │   │       │   │       ├── CancelarHotelWorker.java
│   │   │       │   │       └── CancelarCocheWorker.java
│   │   │       │   │
│   │   │       │   └── salida/
│   │   │       │       ├── persistencia/
│   │   │       │       │   ├── entidad/       # Entidades JPA
│   │   │       │       │   ├── repositorio/   # JpaRepository
│   │   │       │       │   ├── adaptador/     # Implementación de puertos
│   │   │       │       │   └── mapeador/      # MapStruct mappers
│   │   │       │       │
│   │   │       │       └── cliente/
│   │   │       │           └── EventoPublicadorAdaptador.java
│   │   │       │
│   │   │       └── configuracion/
│   │   │           ├── OpenApiConfig.java
│   │   │           ├── JpaConfig.java
│   │   │           ├── CamundaConfig.java
│   │   │           └── BpmnDeploymentService.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── bpmn/                         # Diagramas BPMN (despliegue automático)
│   │       │   ├── subproceso-gestion-cliente.bpmn
│   │       │   ├── subproceso-proceso-reserva.bpmn
│   │       │   ├── subproceso-pago.bpmn
│   │       │   └── proceso-principal.bpmn
│   │       └── data.sql                      # Datos iniciales (opcional)
│   │
│   └── test/
│       └── java/dev/javacadabra/reservasviaje/reserva/
│           ├── arquitectura/
│           │   └── ArquitecturaHexagonalTest.java
│           │
│           ├── unitario/
│           │   ├── dominio/
│           │   └── aplicacion/
│           │
│           └── integracion/
│               ├── repositorio/
│               └── api/
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 🎨 Patrones de Diseño

### 1. **Patrón Saga (Orquestación)**

Implementado para gestionar transacciones distribuidas con compensaciones:

```
┌─────────────────────────────────────────────────────────┐
│                    PROCESO PRINCIPAL                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │   Validar   │→ │   Cliente   │→ │   Pagos  │→   │
│  │   Datos     │  │             │  │             │    │
│  └─────────────┘  └─────────────┘  └─────────────┘    │
│                                           │             │
│                                           ▼             │
│  ┌──────────────────────────────────────────────────┐  │
│  │        SUBPROCESO DE Pagos (PARALELO)        │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐      │  │
│  │  │  Vuelo   │  │  Hotel   │  │  Coche   │      │  │
│  │  └──────────┘  └──────────┘  └──────────┘      │  │
│  │       │              │              │            │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐      │  │
│  │  │ Cancelar │  │ Cancelar │  │ Cancelar │      │  │
│  │  │  Vuelo   │  │  Hotel   │  │  Coche   │      │  │
│  │  └──────────┘  └──────────┘  └──────────┘      │  │
│  │    (Compensación si hay error)                  │  │
│  └──────────────────────────────────────────────────┘  │
│                           │                             │
│                           ▼                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │    Pago     │→ │  Confirmar  │→ │     FIN     │    │
│  │             │  │   Reserva   │  │             │    │
│  └─────────────┘  └─────────────┘  └─────────────┘    │
└─────────────────────────────────────────────────────────┘
```

**Características:**
- ✅ Pagos ejecutadas en **paralelo** para mayor eficiencia
- ✅ **Compensaciones automáticas** si alguna reserva falla
- ✅ **Consistencia eventual** garantizada
- ✅ Manejo robusto de errores con `BpmnError`

### 2. **Repository Pattern**

Separación entre las entidades de dominio y las entidades JPA:

```java
// Puerto (Interface en Dominio)
public interface ReservaVueloPuerto {
    ReservaVuelo guardar(ReservaVuelo reserva);
    Optional<ReservaVuelo> buscarPorId(ReservaId id);
}

// Adaptador (Implementación en Infraestructura)
@Component
public class ReservaVueloAdaptador implements ReservaVueloPuerto {
    private final ReservaVueloJpaRepositorio jpaRepositorio;
    private final ReservaVueloMapeador mapeador;
    
    @Override
    public ReservaVuelo guardar(ReservaVuelo reserva) {
        ReservaVueloEntidad entidad = mapeador.toEntidad(reserva);
        ReservaVueloEntidad guardada = jpaRepositorio.save(entidad);
        return mapeador.toDominio(guardada);
    }
}
```

### 3. **Factory Pattern**

Para creación de agregados con validaciones:

```java
public class ReservaVuelo {
    public static ReservaVuelo crear(
            DatosVuelo datosVuelo,
            List<Pasajero> pasajeros,
            PrecioReserva precio,
            DetalleReserva detalle) {
        
        // Validaciones de negocio
        if (pasajeros.isEmpty()) {
            throw new ReservaInvalidaException("Debe haber al menos un pasajero");
        }
        
        // Crear agregado
        ReservaVuelo reserva = new ReservaVuelo();
        reserva.reservaId = ReservaId.generar();
        reserva.datosVuelo = datosVuelo;
        reserva.pasajeros = pasajeros;
        reserva.precio = precio;
        reserva.detalleReserva = detalle;
        reserva.estado = EstadoReserva.PENDIENTE;
        reserva.fechaCreacion = LocalDateTime.now();
        
        return reserva;
    }
}
```

### 4. **Event Publishing Pattern**

Publicación de eventos de dominio:

```java
@Service
public class ReservaVueloServicio {
    private final EventoPublicadorPuerto eventoPublicador;
    
    public ReservaVueloRespuestaDTO ejecutar(ReservarVueloDTO dto) {
        // ... lógica de creación ...
        
        // Publicar evento
        ReservaVueloCreadaEvento evento = new ReservaVueloCreadaEvento(
            reserva.getReservaId().getValor(),
            dto.clienteId(),
            dto.numeroVuelo(),
            dto.fechaSalida(),
            precio.getMonto()
        );
        eventoPublicador.publicar(evento);
        
        return respuesta;
    }
}
```

---

## 🔧 Workers de Camunda

### Workers de Reserva (Ejecución en Paralelo)

| Worker | Task Type | Descripción |
|--------|-----------|-------------|
| **ReservaVueloWorker** | `reservar-vuelo` | Reserva un vuelo con datos de pasajeros |
| **ReservaHotelWorker** | `reservar-hotel` | Reserva habitaciones de hotel |
| **ReservaCocheWorker** | `reservar-coche` | Reserva un coche de alquiler |

**Variables de entrada comunes:**
- `clienteId` (String)
- Datos específicos de cada tipo de reserva
- `precio` (Double)
- `codigoMoneda` (String, opcional, default: EUR)
- `observaciones` (String, opcional)

**Variables de salida comunes:**
- `reserva[Tipo]Id` (String): ID de la reserva creada
- `reserva[Tipo]Exitosa` (Boolean): Estado de éxito
- `codigoConfirmacion[Tipo]` (String): Código de confirmación
- `precio[Tipo]Final` (Double): Precio final

### Workers de Compensación (Patrón Saga)

| Worker | Task Type | Descripción |
|--------|-----------|-------------|
| **CancelarVueloWorker** | `cancelar-vuelo` | Cancela una reserva de vuelo (compensación) |
| **CancelarHotelWorker** | `cancelar-hotel` | Cancela una reserva de hotel (compensación) |
| **CancelarCocheWorker** | `cancelar-coche` | Cancela una reserva de coche (compensación) |

**Características de los workers de compensación:**
- ✅ Marcados como `isForCompensation=true` en BPMN
- ✅ **NO lanzan excepciones** para no bloquear el flujo
- ✅ Retornan `false` si la cancelación falla
- ✅ Manejan correctamente el caso de que la reserva no exista

**Variables de entrada:**
- `reserva[Tipo]Id` (String): ID de la reserva a cancelar
- `motivoCancelacion` (String, opcional)

**Variables de salida:**
- `[tipo]Cancelado` (Boolean): Estado de la cancelación
- `mensajeCancelacion[Tipo]` (String): Mensaje informativo

---

## 📦 Requisitos Previos

### Software Necesario

- **Java 21** (JDK)
- **Maven 3.9+**
- **Docker** y **Docker Compose**
- **Git**
- **IDE recomendado**: IntelliJ IDEA, Eclipse, VS Code

### Infraestructura Externa

- **Camunda Platform 8** (se levanta con Docker Compose)
  - Zeebe (puerto 26500)
  - Camunda 8.9 unificado: Operate (/operate) + Tasklist (/tasklist) + REST (puerto 8080)

---

## 🚀 Instalación y Ejecución

### 1. Clonar el Repositorio

```bash
git clone https://github.com/vicente-priego-aviles/sistema-reservas-viaje.git
cd sistema-reservas-viaje/servicio-reservas
```

### 2. Levantar Infraestructura de Camunda

```bash
# Desde la raíz del proyecto
docker-compose -f docker-compose-camunda.yml up -d
```

Espera unos 60 segundos hasta que Camunda esté completamente iniciado.

**Verificar que Camunda está funcionando:**
```bash
curl http://localhost:9600/actuator/health/readiness
```

### 3. Compilar el Proyecto

```bash
mvn clean package -DskipTests
```

### 4. Ejecutar el Microservicio

```bash
mvn spring-boot:run
```

O directamente con Java:

```bash
java -jar target/servicio-reservas-1.0.0.jar
```

### 5. Verificar que el Servicio está Funcionando

```bash
# Health check
curl http://localhost:9080/actuator/health

# Swagger UI
open http://localhost:9080/swagger-ui.html
```

---

## ⚙️ Configuración

### application.yml

```yaml
# Configuración del servidor
server:
  port: 9080

# Configuración de Spring
spring:
  application:
    name: servicio-reservas
  
  # Base de datos H2 (desarrollo)
  datasource:
    url: jdbc:h2:mem:Pagosdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  # JPA/Hibernate
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  # H2 Console
  h2:
    console:
      enabled: true
      path: /h2-console

# Configuración de Camunda Platform 8
camunda:
  client:
    zeebe:
      gateway-address: localhost:26500
      rest-address: http://localhost:8080
      prefer-rest-over-grpc: false
    mode: self-managed
    auth:
      username: demo
      password: demo

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

# Logging
logging:
  level:
    dev.javacadabra: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
```

### Variables de Entorno (Producción)

```bash
# Base de datos
DB_URL=jdbc:postgresql://localhost:5432/Pagos
DB_USERNAME=postgres
DB_PASSWORD=secret

# Camunda
CAMUNDA_ZEEBE_ADDRESS=localhost:26500
CAMUNDA_USERNAME=demo
CAMUNDA_PASSWORD=demo

# Servidor
SERVER_PORT=9080
```

---

## 🌐 Endpoints API

### Reserva de Vuelo

**POST** `/api/Pagos/vuelo`

```json
{
  "clienteId": "cliente-123",
  "numeroVuelo": "IB8501",
  "aerolinea": "Iberia",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaSalida": "2025-12-01T10:00:00",
  "fechaLlegada": "2025-12-01T11:30:00",
  "clase": "ECONOMICA",
  "precio": 120.50,
  "codigoMoneda": "EUR",
  "pasajeros": [
    {
      "nombre": "Juan",
      "apellidos": "Pérez García",
      "numeroDocumento": "12345678A",
      "tipoDocumento": "DNI",
      "fechaNacimiento": "1990-05-15",
      "nacionalidad": "ES"
    }
  ],
  "observaciones": "Ventana preferiblemente"
}
```

**Respuesta 201 Created:**
```json
{
  "reservaId": "vuelo-uuid-123",
  "numeroVuelo": "IB8501",
  "aerolinea": "Iberia",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaSalida": "2025-12-01T10:00:00",
  "fechaLlegada": "2025-12-01T11:30:00",
  "clase": "ECONOMICA",
  "precio": 120.50,
  "codigoMoneda": "EUR",
  "estado": "PENDIENTE",
  "pasajeros": [...],
  "clienteId": "cliente-123",
  "codigoConfirmacion": "CONF-ABC123",
  "fechaCreacion": "2025-10-22T14:30:00"
}
```

### Reserva de Hotel

**POST** `/api/Pagos/hotel`

```json
{
  "clienteId": "cliente-123",
  "nombreHotel": "Hotel Gran Vía",
  "ciudad": "Madrid",
  "direccion": "Gran Vía, 45",
  "fechaEntrada": "2025-12-01",
  "fechaSalida": "2025-12-03",
  "tipoHabitacion": "DOBLE",
  "numeroHabitaciones": 1,
  "numeroHuespedes": 2,
  "precio": 180.00,
  "codigoMoneda": "EUR",
  "observaciones": "Habitación no fumadores"
}
```

### Reserva de Coche

**POST** `/api/Pagos/coche`

```json
{
  "clienteId": "cliente-123",
  "empresaAlquiler": "Hertz",
  "modeloCoche": "Seat León",
  "categoriaCoche": "COMPACTO",
  "ubicacionRecogida": "Aeropuerto Madrid T4",
  "ubicacionDevolucion": "Aeropuerto Barcelona",
  "fechaRecogida": "2025-12-01T12:00:00",
  "fechaDevolucion": "2025-12-03T12:00:00",
  "precio": 80.00,
  "codigoMoneda": "EUR",
  "observaciones": "GPS incluido"
}
```

### Consultar Reserva

**GET** `/api/Pagos/{tipo}/{id}`

Donde `{tipo}` puede ser: `vuelo`, `hotel`, o `coche`

### Cancelar Reserva

**DELETE** `/api/Pagos/{tipo}/{id}`

**Body:**
```json
{
  "motivo": "Cambio de planes"
}
```

---

## 📊 Procesos BPMN

### Proceso Principal de Reserva de Viaje

El proceso completo incluye:

1. **Validación de Datos** (`validar-datos-entrada`)
2. **Gestión de Cliente** (Call Activity → subproceso-gestion-cliente)
3. **Revisión Manual** (User Task)
4. **Proceso de Reserva** (Call Activity → subproceso-proceso-reserva)
   - Reserva de Vuelo (Paralelo)
   - Reserva de Hotel (Paralelo)
   - Reserva de Coche (Paralelo)
   - **Compensaciones automáticas** si hay error
5. **Proceso de Pago** (Call Activity → subproceso-pago)
6. **Confirmación Final**

### Subproceso de Pagos (Ejecución Paralela)

```
     ┌────────────────────────────────────────┐
     │      GATEWAY PARALELO (INICIO)         │
     └───┬──────────────┬─────────────────┬───┘
         │              │                 │
         ▼              ▼                 ▼
   ┌─────────┐    ┌─────────┐      ┌─────────┐
   │ Reservar│    │ Reservar│      │ Reservar│
   │  Vuelo  │    │  Hotel  │      │  Coche  │
   └────┬────┘    └────┬────┘      └────┬────┘
        │              │                 │
        │         [Compensación]         │
        │         si hay error           │
        │              │                 │
        ▼              ▼                 ▼
   ┌─────────┐    ┌─────────┐      ┌─────────┐
   │ Cancelar│    │ Cancelar│      │ Cancelar│
   │  Vuelo  │    │  Hotel  │      │  Coche  │
   └─────────┘    └─────────┘      └─────────┘
```

**Ubicación de los BPMN:**
- `src/main/resources/bpmn/proceso-principal.bpmn`
- `src/main/resources/bpmn/subproceso-gestion-cliente.bpmn`
- `src/main/resources/bpmn/subproceso-proceso-reserva.bpmn`
- `src/main/resources/bpmn/subproceso-pago.bpmn`

**Despliegue automático:** Los procesos BPMN se despliegan automáticamente en Camunda al iniciar el microservicio gracias al `BpmnDeploymentService`.

---

## 🧪 Testing

### Ejecutar Tests

```bash
# Todos los tests
mvn test

# Solo tests unitarios
mvn test -Dtest=*Test

# Solo tests de integración
mvn test -Dtest=*IntegrationTest

# Con cobertura (JaCoCo)
mvn clean test jacoco:report
```

### Estructura de Tests

```
src/test/java/
├── arquitectura/
│   └── ArquitecturaHexagonalTest.java    # ArchUnit
│
├── unitario/
│   ├── dominio/
│   │   ├── ReservaVueloTest.java
│   │   ├── ReservaHotelTest.java
│   │   └── ReservaCocheTest.java
│   │
│   └── aplicacion/
│       ├── ReservaVueloServicioTest.java
│       ├── ReservaHotelServicioTest.java
│       └── ReservaCocheServicioTest.java
│
└── integracion/
    ├── ReservaVueloControllerIntegrationTest.java
    └── ReservaVueloRepositorioIntegrationTest.java
```

### Tests de Arquitectura con ArchUnit

```java
@AnalyzeClasses(packages = "dev.javacadabra.reservasviaje.reserva")
public class ArquitecturaHexagonalTest {
    
    @ArchTest
    static final ArchRule capaDominioNoDebeDependerDeInfraestructura =
        noClasses()
            .that().resideInAPackage("..dominio..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infraestructura..", "..aplicacion..")
            .because("El dominio debe ser independiente");
    
    @ArchTest
    static final ArchRule entidadesDominioDebenTenerAnotacionJMolecules =
        classes()
            .that().resideInAPackage("..dominio.modelo..")
            .should().beAnnotatedWith(
                anyOf(AggregateRoot.class, Entity.class, ValueObject.class)
            );
}
```

---

## ✨ Mejores Prácticas Implementadas

### 🏛️ Arquitectura

- ✅ **Arquitectura Hexagonal** - Separación clara de capas
- ✅ **Domain-Driven Design** - Uso de JMolecules
- ✅ **Inversión de Dependencias** - Puertos e interfaces
- ✅ **Separación de Entidades** - Dominio vs JPA
- ✅ **Eventos de Dominio** - Comunicación desacoplada

### 💻 Código

- ✅ **Java 21** - Records, Pattern Matching
- ✅ **Lombok** - Reducción de boilerplate
- ✅ **MapStruct** - Mapeo automático tipo-seguro
- ✅ **Apache Commons Lang** - Utilidades estándar
- ✅ **Validación Bean Validation** - Validaciones declarativas
- ✅ **Logs estructurados** - Con iconos para mejor legibilidad

### 🔧 Operaciones

- ✅ **Spring Actuator** - Health checks y métricas
- ✅ **OpenAPI/Swagger** - Documentación API automática
- ✅ **Docker** - Contenedorización
- ✅ **Despliegue automático BPMN** - Al iniciar el servicio

### 📋 Testing

- ✅ **JUnit 5** - Framework de testing moderno
- ✅ **ArchUnit** - Validación de arquitectura
- ✅ **Testcontainers** - Tests de integración reales
- ✅ **Cobertura JaCoCo** - Medición de cobertura

### 🔄 Camunda

- ✅ **Patrón Saga** - Transacciones distribuidas
- ✅ **Compensaciones** - Rollback automático
- ✅ **Ejecución paralela** - Mayor eficiencia
- ✅ **Manejo robusto de errores** - BpmnError
- ✅ **Reintentos automáticos** - Configurados en BPMN

---

## 🗺️ Roadmap

### ✅ Completado

- [x] Arquitectura hexagonal base
- [x] Modelos de dominio con DDD
- [x] Workers de Camunda (reserva y compensación)
- [x] API REST completa
- [x] Integración con Camunda Platform 8
- [x] Despliegue automático de BPMN
- [x] Logs estructurados con iconos

### 🚧 En Progreso

- [ ] Tests unitarios completos
- [ ] Tests de integración con Testcontainers
- [ ] Tests de arquitectura con ArchUnit

### 📅 Futuro

- [ ] Migrar a PostgreSQL en producción
- [ ] Implementar caché con Redis
- [ ] Circuit Breaker con Resilience4j
- [ ] Distributed Tracing con Micrometer
- [ ] Métricas personalizadas con Prometheus
- [ ] CI/CD con GitHub Actions
- [ ] Documentación adicional (ADRs)
- [ ] Performance testing con JMeter
- [ ] Security testing con OWASP ZAP

---

## 🤝 Contribución

¡Las contribuciones son bienvenidas! Este es un proyecto didáctico y cualquier mejora o sugerencia es apreciada.

### Guía para Contribuir

1. **Fork** el repositorio
2. Crea una **rama feature** (`git checkout -b feature/nueva-funcionalidad`)
3. **Commit** tus cambios (`git commit -m '✨ Agregar nueva funcionalidad'`)
4. **Push** a la rama (`git push origin feature/nueva-funcionalidad`)
5. Abre un **Pull Request**

### Convenciones

- **Commits semánticos**: Usar emojis y mensajes descriptivos
  - ✨ `:sparkles:` Nueva funcionalidad
  - 🐛 `:bug:` Corrección de bug
  - 📝 `:memo:` Documentación
  - ♻️ `:recycle:` Refactorización
  - ✅ `:white_check_mark:` Tests

- **Código en español**: Clases, variables, comentarios
- **Términos técnicos en inglés**: controller, service, worker, etc.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

---

## 📞 Contacto

**Autor:** Vicente Priego  
**Email:** vicentepriegoaviles@gmail.com  
**GitHub:** [@vicente-priego-aviles](https://github.com/vicente-priego-aviles)  

---

## 🙏 Agradecimientos

- **Camunda** - Por su excelente plataforma de orquestación
- **Spring Team** - Por el ecosistema Spring Boot
- **Comunidad DDD** - Por los conceptos de Domain-Driven Design
- **JMolecules** - Por hacer explícitos los conceptos DDD

---

## 📚 Referencias y Recursos

### Documentación Oficial

- [Camunda Platform 8 Documentation](https://docs.camunda.io/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Domain-Driven Design Reference](https://www.domainlanguage.com/ddd/reference/)

### Libros Recomendados

- **"Domain-Driven Design"** - Eric Evans
- **"Implementing Domain-Driven Design"** - Vaughn Vernon
- **"Building Microservices"** - Sam Newman
- **"Clean Architecture"** - Robert C. Martin
- **"BPMN 2.0 by Example"** - Thomas Allweyer

### Artículos y Tutoriales

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Saga Pattern](https://microservices.io/patterns/data/saga.html)
- [Camunda Best Practices](https://camunda.com/best-practices/)

---

<div align="center">

**⭐ Si este proyecto te ha sido útil, considera darle una estrella en GitHub ⭐**

Desarrollado con ❤️ para la comunidad de desarrolladores backend

</div>
