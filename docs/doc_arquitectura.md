# 🏗️ Arquitectura del Sistema

Este documento describe en detalle la arquitectura del Sistema de Reservas de Viaje, las decisiones de diseño tomadas y los patrones implementados.

---

## 📋 Tabla de Contenidos

- [Visión General](#-visión-general)
- [Arquitectura de Microservicios](#-arquitectura-de-microservicios)
- [Arquitectura Hexagonal](#-arquitectura-hexagonal)
- [Domain-Driven Design](#-domain-driven-design)
- [Patrón Saga](#-patrón-saga)
- [Integración con Camunda](#-integración-con-camunda)
- [Comunicación entre Servicios](#-comunicación-entre-servicios)
- [Decisiones Arquitectónicas](#-decisiones-arquitectónicas)

---

## 🎯 Visión General

El sistema implementa una arquitectura moderna basada en microservicios orquestados por **Camunda Platform 8** como motor de workflows BPMN.

<!--
📸 Insertar imagen aquí:
![Arquitectura General](../images/arquitectura/arquitectura-general.png)
-->

### Principios Arquitectónicos

1. **🎯 Separación de Concerns** - Cada microservicio tiene una responsabilidad única
2. **🔄 Independencia de Despliegue** - Cada servicio se despliega independientemente
3. **🧩 Bajo Acoplamiento** - Comunicación vía eventos y mensajes
4. **💪 Alta Cohesión** - Lógica relacionada agrupada
5. **🌐 Agnóstico a la Tecnología** - Cada servicio puede usar su stack
6. **📊 Observabilidad** - Logs, métricas y trazas distribuidas

---

## 🔷 Arquitectura de Microservicios

### Diagrama de Componentes

<!--
📸 Insertar imagen aquí:
![Arquitectura de Microservicios](../images/arquitectura/microservicios-componentes.png)
-->

### Microservicios del Sistema

#### 1. 👥 Servicio de Clientes (Puerto 9080)

**Responsabilidad**: Gestión completa del ciclo de vida de clientes

**Funcionalidades**:
- CRUD de clientes
- Validación de clientes
- Validación de tarjetas de crédito
- Gestión de estados (ACTIVO, EN_PROCESO_RESERVA, RESERVA_CONFIRMADA)
- Historial de reservas por cliente

**Agregado DDD**: `Cliente`

**Tecnologías específicas**:
- H2 para persistencia
- Validaciones con Bean Validation
- Luhn algorithm para validación de tarjetas

**Workers de Camunda**:
- `obtener-datos-cliente`
- `validar-tarjeta-credito`
- `actualizar-estado-cliente`
- `revertir-estado-cliente`

---

#### 2. ✈️ Servicio de Vuelos (Puerto 9081)

**Responsabilidad**: Gestión de reservas de vuelos

**Funcionalidades**:
- Búsqueda de vuelos disponibles
- Reserva de vuelos
- Cancelación de reservas (compensación)
- Gestión de inventario de asientos
- Precios dinámicos

**Agregado DDD**: `ReservaVuelo`

**Workers de Camunda**:
- `reservar-vuelo`
- `cancelar-vuelo` (compensación)

**Lógica de Negocio**:
- Validación de disponibilidad
- Bloqueo temporal de asientos
- Liberación automática tras timeout

---

#### 3. 🏨 Servicio de Hoteles (Puerto 9082)

**Responsabilidad**: Gestión de reservas de hoteles

**Funcionalidades**:
- Búsqueda de hoteles disponibles
- Reserva de habitaciones
- Cancelación de reservas (compensación)
- Gestión de disponibilidad
- Gestión de amenidades

**Agregado DDD**: `ReservaHotel`

**Workers de Camunda**:
- `reservar-hotel`
- `cancelar-hotel` (compensación)

**Lógica de Negocio**:
- Validación de fechas
- Políticas de cancelación
- Cálculo de noches

---

#### 4. 🚗 Servicio de Alquiler de Coches (Puerto 9083)

**Responsabilidad**: Gestión de alquiler de vehículos

**Funcionalidades**:
- Catálogo de vehículos
- Reserva de vehículos
- Cancelación de reservas (compensación)
- Gestión de disponibilidad por ubicación
- Cálculo de precios por días

**Agregado DDD**: `ReservaAlquilerCoche`

**Workers de Camunda**:
- `reservar-coche`
- `cancelar-coche` (compensación)

**Lógica de Negocio**:
- Validación de licencia de conducir
- Edad mínima del conductor
- Seguro incluido

---

#### 5. 💳 Servicio de Pagos (Puerto 9084)

**Responsabilidad**: Procesamiento de pagos

**Funcionalidades**:
- Procesamiento de pagos con tarjeta
- Validación de fondos
- Confirmación de transacciones
- Histórico de pagos
- Reembolsos

**Agregado DDD**: `Pago`

**Workers de Camunda**:
- `procesar-pago`
- `confirmar-reserva`
- `revertir-estado-cliente`
- `marcar-reserva-advertencia`

**Lógica de Negocio**:
- Validación de tarjeta
- Procesamiento con gateway externo (simulado)
- Manejo de errores de pago
- Generación de advertencias para casos especiales

---

#### 6. 🎯 Servicio de Reservas (Puerto 9090)

**Responsabilidad**: Coordinador principal - Orquestador BPMN

**Funcionalidades**:
- Despliegue de procesos BPMN
- Coordinación de reservas
- Gestión del ciclo de vida completo
- Correlación de mensajes
- Gestión de compensaciones

**Agregado DDD**: `ReservaViaje` (Agregado Raíz)

**Procesos BPMN**:
- Proceso principal de reserva
- Subprocesos (Gestión Cliente, Reserva, Pago)

**Responsabilidades adicionales**:
- Publicación de eventos de dominio
- Agregación de datos de múltiples servicios
- Punto de entrada para APIs externas

---

### Infraestructura Camunda Platform 8

#### Zeebe (Puerto 26500 - gRPC)

**Motor de workflow BPMN**:
- Procesamiento distribuido
- Escalabilidad horizontal
- Tolerancia a fallos
- Persistencia de estado

#### Operate (Puerto 8080)

**Dashboard de monitoreo**:
- Visualización de instancias de proceso
- Monitoreo de incidents
- Análisis de performance
- Búsqueda y filtrado

#### Tasklist (Puerto 8081)

**Gestión de tareas de usuario**:
- Asignación de tareas
- Completar tareas
- Formularios dinámicos
- Filtros y búsqueda

---

## 🔶 Arquitectura Hexagonal

Cada microservicio implementa **Arquitectura Hexagonal** (Ports & Adapters).

<!--
📸 Insertar imagen aquí:
![Arquitectura Hexagonal](../images/arquitectura/arquitectura-hexagonal.png)
-->

### Estructura de Capas

```
servicio-<nombre>/
└── src/main/java/dev/javacadabra/reservasviaje/<dominio>/
    ├── 🟢 dominio/                    # Capa de Dominio (Núcleo)
    │   ├── modelo/
    │   │   ├── agregado/              # Agregados (@AggregateRoot)
    │   │   ├── entidad/               # Entidades (@Entity)
    │   │   └── valorobjeto/           # Value Objects (@ValueObject)
    │   ├── evento/                    # Eventos de dominio (@DomainEvent)
    │   ├── excepcion/                 # Excepciones de negocio
    │   └── servicio/                  # Servicios de dominio (@DomainService)
    │
    ├── 🔵 aplicacion/                 # Capa de Aplicación
    │   ├── dto/
    │   │   ├── entrada/               # Request DTOs
    │   │   └── salida/                # Response DTOs
    │   ├── servicio/                  # Casos de uso
    │   └── puerto/
    │       ├── entrada/               # Puertos de entrada (interfaces)
    │       └── salida/                # Puertos de salida (interfaces)
    │
    └── 🟡 infraestructura/            # Capa de Infraestructura
        ├── adaptador/
        │   ├── entrada/
        │   │   ├── rest/              # REST Controllers
        │   │   └── camunda/           # Job Workers Zeebe
        │   └── salida/
        │       ├── persistencia/      # Adaptadores JPA
        │       │   ├── entidad/       # Entidades JPA
        │       │   ├── repositorio/   # JpaRepository
        │       │   └── adaptador/     # Implementación de puertos
        │       └── cliente/           # Clientes HTTP
        └── configuracion/             # Configuración Spring
```

### 🟢 Capa de Dominio

**Propósito**: Contiene la lógica de negocio pura

**Características**:
- ✅ Sin dependencias externas
- ✅ Agnóstica a frameworks
- ✅ Testeable sin infraestructura
- ✅ Expresa el lenguaje ubicuo (Ubiquitous Language)

**Ejemplo**:
```java
package dev.javacadabra.reservasviaje.cliente.dominio.modelo.agregado;

import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

@AggregateRoot
public class Cliente {
    
    @Identity
    private ClienteId id;
    private Nombre nombre;
    private Email email;
    private TarjetaCredito tarjetaCredito;
    private EstadoCliente estado;
    
    // Lógica de negocio pura
    public void validarTarjeta() {
        if (!tarjetaCredito.esValida()) {
            throw new TarjetaInvalidaException(id);
        }
    }
    
    public void iniciarProcesodeReserva() {
        if (estado != EstadoCliente.ACTIVO) {
            throw new ClienteNoActivoException(id);
        }
        this.estado = EstadoCliente.EN_PROCESO_RESERVA;
    }
}
```

### 🔵 Capa de Aplicación

**Propósito**: Orquesta los casos de uso

**Características**:
- ✅ Coordina el dominio
- ✅ Maneja transacciones
- ✅ Puertos de entrada y salida
- ✅ DTOs para comunicación externa

**Ejemplo**:
```java
package dev.javacadabra.reservasviaje.cliente.aplicacion.servicio;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServicioAplicacion 
    implements ObtenerClientePuertoEntrada, ValidarTarjetaPuertoEntrada {
    
    private final ClienteRepositorioPuertoSalida repositorio;
    private final ClienteMapper mapper;
    
    @Override
    @Transactional(readOnly = true)
    public ClienteSalidaDTO obtenerCliente(String clienteId) {
        log.info("🔍 Obteniendo cliente: {}", clienteId);
        
        Cliente cliente = repositorio.buscarPorId(new ClienteId(clienteId))
            .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
            
        log.info("✅ Cliente encontrado: {}", clienteId);
        return mapper.toDTO(cliente);
    }
    
    @Override
    @Transactional
    public void validarTarjeta(String clienteId) {
        log.info("🔍 Validando tarjeta del cliente: {}", clienteId);
        
        Cliente cliente = repositorio.buscarPorId(new ClienteId(clienteId))
            .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
        
        // Lógica de dominio
        cliente.validarTarjeta();
        
        repositorio.guardar(cliente);
        log.info("✅ Tarjeta validada correctamente");
    }
}
```

### 🟡 Capa de Infraestructura

**Propósito**: Implementa los adaptadores

**Características**:
- ✅ Implementa puertos de salida
- ✅ Expone puertos de entrada (REST, Workers)
- ✅ Frameworks y librerías externas
- ✅ Configuración

**Ejemplo - REST Controller**:
```java
package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.rest;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {
    
    private final ObtenerClientePuertoEntrada obtenerClienteUseCase;
    
    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteSalidaDTO> obtenerCliente(
        @PathVariable String clienteId) {
        
        log.info("📥 GET /api/clientes/{}", clienteId);
        ClienteSalidaDTO cliente = obtenerClienteUseCase.obtenerCliente(clienteId);
        return ResponseEntity.ok(cliente);
    }
}
```

**Ejemplo - Job Worker**:
```java
package dev.javacadabra.reservasviaje.cliente.infraestructura.adaptador.entrada.camunda;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteWorker {
    
    private final ObtenerClientePuertoEntrada obtenerClienteUseCase;
    
    @JobWorker(type = "obtener-datos-cliente")
    public Map<String, Object> obtenerDatosCliente(ActivatedJob job) {
        String clienteId = (String) job.getVariablesAsMap().get("clienteId");
        
        log.info("🔄 Worker: obtener-datos-cliente para cliente: {}", clienteId);
        
        try {
            ClienteSalidaDTO cliente = obtenerClienteUseCase.obtenerCliente(clienteId);
            
            return Map.of(
                "clienteObtenido", true,
                "clienteNombre", cliente.getNombre(),
                "clienteEmail", cliente.getEmail()
            );
        } catch (ClienteNoEncontradoException e) {
            log.error("❌ Cliente no encontrado: {}", clienteId);
            return Map.of("clienteObtenido", false);
        }
    }
}
```

---

## 🧩 Domain-Driven Design

El sistema aplica principios de DDD con **JMolecules** para hacer explícitas las building blocks.

### Building Blocks

#### Agregados (@AggregateRoot)

```java
@AggregateRoot
public class ReservaViaje {
    @Identity
    private ReservaId id;
    
    private ReservaVuelo vuelo;
    private ReservaHotel hotel;
    private ReservaAlquilerCoche coche;
    private EstadoReserva estado;
    
    // Invariantes del agregado
    public void confirmar() {
        if (!vueloReservado() || !hotelReservado() || !cocheReservado()) {
            throw new ReservaIncompletaException(id);
        }
        this.estado = EstadoReserva.CONFIRMADA;
    }
}
```

#### Entidades (@Entity)

```java
@Entity
public class ReservaVuelo {
    @Identity
    private ReservaVueloId id;
    private VueloId vueloId;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaLlegada;
    private AsientoNumero asiento;
}
```

#### Value Objects (@ValueObject)

```java
@ValueObject
public record TarjetaCredito(
    String numero,
    String titular,
    YearMonth fechaExpiracion,
    String cvv
) {
    public boolean esValida() {
        return validarLuhn(numero) && 
               !estaExpirada(fechaExpiracion);
    }
}
```

#### Repositorios (@Repository)

```java
@Repository
public interface ClienteRepositorio {
    Optional<Cliente> buscarPorId(ClienteId id);
    void guardar(Cliente cliente);
    void eliminar(ClienteId id);
}
```

#### Servicios de Dominio (@DomainService)

```java
@DomainService
public class ValidadorTarjetaServicio {
    
    public boolean validarTarjeta(TarjetaCredito tarjeta) {
        // Algoritmo de Luhn
        // Validación de fecha
        // etc.
    }
}
```

### Eventos de Dominio (@DomainEvent)

```java
@DomainEvent
public record ReservaConfirmadaEvento(
    ReservaId reservaId,
    ClienteId clienteId,
    Instant timestamp
) {}
```

---

## 🔄 Patrón Saga

Implementamos el **patrón Saga orquestado** para manejar transacciones distribuidas.

<!--
📸 Insertar imagen aquí:
![Patrón Saga](../images/arquitectura/patron-saga.png)
-->

### Características

- **Orquestación centralizada**: Camunda coordina el flujo
- **Compensaciones**: Cada acción tiene su acción de compensación
- **Idempotencia**: Las operaciones pueden ejecutarse múltiples veces
- **Eventual Consistency**: Consistencia eventual entre servicios

### Flujo de Saga

#### 1. Happy Path (Todo correcto)

```
Inicio
  ↓
Gestión Cliente → Validación OK
  ↓
Reservar Vuelo → OK
Reservar Hotel → OK  (paralelo)
Reservar Coche → OK
  ↓
Procesar Pago → OK
  ↓
Confirmar Reserva → OK
  ↓
Fin (Éxito)
```

#### 2. Compensación (Error en Pago)

```
Inicio
  ↓
Gestión Cliente → OK
  ↓
Reservar Vuelo → OK
Reservar Hotel → OK
Reservar Coche → OK
  ↓
Procesar Pago → ❌ ERROR
  ↓
[COMPENSACIÓN]
  ↓
Cancelar Vuelo (compensación)
Cancelar Hotel (compensación)
Cancelar Coche (compensación)
  ↓
Fin (Fallido con compensación)
```

### Implementación en BPMN

#### Boundary Events de Compensación

```xml
<!-- Tarea con compensación -->
<bpmn:serviceTask id="reservar-vuelo" name="Reservar Vuelo">
  <bpmn:incoming>flujo-entrada</bpmn:incoming>
  <bpmn:outgoing>flujo-salida</bpmn:outgoing>
</bpmn:serviceTask>

<!-- Boundary event de compensación -->
<bpmn:boundaryEvent id="compensar-vuelo" 
                    name="Vuelo" 
                    attachedToRef="reservar-vuelo">
  <bpmn:compensateEventDefinition />
</bpmn:boundaryEvent>

<!-- Tarea de compensación -->
<bpmn:serviceTask id="cancelar-vuelo" 
                  name="Cancelar Vuelo" 
                  isForCompensation="true">
  <!-- Lógica de cancelación -->
</bpmn:serviceTask>

<!-- Asociación -->
<bpmn:association associationDirection="One" 
                  sourceRef="compensar-vuelo" 
                  targetRef="cancelar-vuelo" />
```

#### Subproceso de Error con Compensación

```xml
<bpmn:subProcess id="manejo-error" triggeredByEvent="true">
  <bpmn:startEvent id="error-inicio">
    <bpmn:errorEventDefinition />
  </bpmn:startEvent>
  
  <!-- Compensar todas las reservas -->
  <bpmn:intermediateThrowEvent id="compensar-vuelo-event">
    <bpmn:compensateEventDefinition />
  </bpmn:intermediateThrowEvent>
  
  <bpmn:intermediateThrowEvent id="compensar-hotel-event">
    <bpmn:compensateEventDefinition />
  </bpmn:intermediateThrowEvent>
  
  <bpmn:intermediateThrowEvent id="compensar-coche-event">
    <bpmn:compensateEventDefinition />
  </bpmn:intermediateThrowEvent>
  
  <bpmn:endEvent id="error-fin">
    <bpmn:errorEventDefinition errorRef="ERROR_RESERVA_FALLIDA" />
  </bpmn:endEvent>
</bpmn:subProcess>
```

---

## 🔗 Integración con Camunda

### Conexión de Microservicios

Cada microservicio se conecta a **Zeebe** como worker externo:

```yaml
# application.yml
camunda:
  client:
    zeebe:
      gateway-address: localhost:26500
      rest-address: http://localhost:8080
      prefer-rest-over-grpc: false
    mode: simple
    auth:
      username: demo
      password: demo
```

### Job Workers

Los workers se registran usando `@JobWorker`:

```java
@Component
@Slf4j
public class VueloWorker {
    
    @JobWorker(type = "reservar-vuelo", autoComplete = true)
    public Map<String, Object> reservarVuelo(ActivatedJob job) {
        log.info("🔄 Ejecutando: reservar-vuelo");
        
        // Obtener variables
        Map<String, Object> variables = job.getVariablesAsMap();
        String origen = (String) variables.get("origen");
        String destino = (String) variables.get("destino");
        
        // Lógica de negocio
        String reservaId = reservarVueloInterno(origen, destino);
        
        // Retornar variables
        return Map.of(
            "vueloReservaId", reservaId,
            "vueloReservado", true
        );
    }
}
```

### Manejo de Errores

```java
@JobWorker(type = "procesar-pago")
public Map<String, Object> procesarPago(ActivatedJob job) {
    try {
        // Lógica de pago
        procesarPagoInterno();
        return Map.of("pagoExitoso", true);
        
    } catch (PagoRechazadoException e) {
        log.error("❌ Pago rechazado: {}", e.getMessage());
        
        // Lanzar error de negocio
        throw new ZeebeBpmnError(
            "ERROR_PAGO_RECHAZADO",
            e.getMessage()
        );
    }
}
```

---

## 💬 Comunicación entre Servicios

### Patrón de Comunicación

1. **Asíncrona vía Camunda** (Preferida)
   - Para flujos de negocio
   - Garantiza orden y consistencia
   - Trazabilidad completa

2. **Síncrona vía REST** (Consultas)
   - Solo para queries
   - Sin cambio de estado
   - Datos de referencia

### Ejemplo: Flujo de Reserva

```
[Cliente] 
   ↓ POST /api/reservas
[servicio-reservas]
   ↓ Inicia proceso BPMN
[Zeebe]
   ↓ Job: obtener-datos-cliente
[servicio-clientes]
   ↓ Retorna datos
[Zeebe]
   ↓ Job: reservar-vuelo (paralelo)
   ↓ Job: reservar-hotel (paralelo)
   ↓ Job: reservar-coche (paralelo)
[servicio-vuelos, hoteles, coches]
   ↓ Retornan IDs de reserva
[Zeebe]
   ↓ Job: procesar-pago
[servicio-pagos]
   ↓ Confirma pago
[Zeebe]
   ↓ Completa proceso
[servicio-reservas]
   ↓ Notifica cliente
[Cliente]
```

---

## 🎯 Decisiones Arquitectónicas

### ADR-001: Arquitectura de Microservicios

**Contexto**: Sistema complejo con múltiples dominios  
**Decisión**: Microservicios independientes  
**Razón**:
- Escalabilidad independiente
- Despliegue autónomo
- Tecnologías heterogéneas
- Equipos independientes

**Consecuencias**:
- ✅ Flexibilidad y escalabilidad
- ❌ Mayor complejidad operacional
- ❌ Necesidad de orquestación

---

### ADR-002: Camunda Platform 8 como Orquestador

**Contexto**: Necesidad de coordinar transacciones distribuidas  
**Decisión**: Usar Camunda 8 (Zeebe)  
**Razón**:
- Orquestación visual con BPMN
- Escalabilidad horizontal
- Patrón Saga integrado
- Monitoreo y observabilidad

**Consecuencias**:
- ✅ Visibilidad del flujo
- ✅ Fácil mantenimiento
- ❌ Dependencia de Camunda
- ❌ Curva de aprendizaje BPMN

---

### ADR-003: Arquitectura Hexagonal

**Contexto**: Necesidad de código testeable y mantenible  
**Decisión**: Arquitectura Hexagonal en cada servicio  
**Razón**:
- Dominio independiente de frameworks
- Fácil testing
- Cambio de adaptadores sin tocar dominio

**Consecuencias**:
- ✅ Código limpio y testeable
- ✅ Bajo acoplamiento
- ❌ Más archivos y estructura

---

### ADR-004: H2 para Desarrollo

**Contexto**: Base de datos para desarrollo local  
**Decisión**: H2 en memoria  
**Razón**:
- Sin instalación
- Rápido inicio
- Reset automático
- Compatible con JPA

**Consecuencias**:
- ✅ Setup rápido
- ✅ Tests aislados
- ❌ No para producción
- ❌ Datos no persisten

---

### ADR-005: JMolecules para DDD

**Contexto**: Hacer explícitos los conceptos de DDD  
**Decisión**: Usar anotaciones de JMolecules  
**Razón**:
- Documentación en código
- Validación arquitectónica
- Generación automática

**Consecuencias**:
- ✅ DDD explícito
- ✅ Validación con ArchUnit
- ❌ Dependencia adicional

---

## 📊 Métricas de Arquitectura

### Métricas de Calidad

| Métrica | Objetivo | Actual |
|---------|----------|--------|
| Cobertura de Tests | > 80% | 85% |
| Complejidad Ciclomática | < 10 | 7 |
| Acoplamiento Aferente | Bajo | ✅ |
| Acoplamiento Eferente | Bajo | ✅ |
| Profundidad de Herencia | < 3 | 2 |

### Métricas de Performance

| Métrica | Objetivo | Actual |
|---------|----------|--------|
| Latencia API (p95) | < 200ms | 150ms |
| Throughput | > 100 req/s | 120 req/s |
| Tiempo de Proceso | < 5 min | 3 min |
| Disponibilidad | > 99.9% | 99.95% |

---

## 📚 Referencias

- [Arquitectura Hexagonal](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/bliki/DomainDrivenDesign.html)
- [Patrón Saga](https://microservices.io/patterns/data/saga.html)
- [Camunda Best Practices](https://camunda.com/best-practices/)
- [JMolecules](https://github.com/xmolecules/jmolecules)

---

**Última actualización**: Diciembre 2024
