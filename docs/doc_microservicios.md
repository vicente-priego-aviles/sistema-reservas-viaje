# 🛠️ Documentación de Microservicios

Guía detallada de cada microservicio del sistema.

---

## 📋 Resumen de Microservicios

| Servicio | Puerto | Base de Datos | Workers | Endpoints |
|----------|--------|---------------|---------|-----------|
| Clientes | 9080 | clientes_db | 4 | 8 |
| Vuelos | 9081 | vuelos_db | 2 | 6 |
| Hoteles | 9082 | hoteles_db | 2 | 6 |
| Alquiler Coches | 9083 | coches_db | 2 | 6 |
| Pagos | 9084 | pagos_db | 4 | 5 |
| Reservas | 9090 | reservas_db | 0 | 7 |

---

## 👥 servicio-clientes (9080)

### Responsabilidades
- CRUD de clientes
- Validación de existencia
- Validación de tarjetas de crédito
- Gestión de estados del cliente

### Modelo de Dominio

```java
@AggregateRoot
public class Cliente {
    @Identity
    private ClienteId id;
    private Nombre nombre;
    private Email email;
    private TarjetaCredito tarjetaCredito;
    private EstadoCliente estado;
    private List<ReservaId> historicoReservas;
}

@ValueObject
public record TarjetaCredito(
    String numero,
    YearMonth fechaExpiracion,
    String cvv
) {}

public enum EstadoCliente {
    ACTIVO,
    EN_PROCESO_RESERVA,
    RESERVA_CONFIRMADA,
    SUSPENDIDO
}
```

### API REST

#### GET /api/clientes/{id}
Obtener cliente por ID

**Response**:
```json
{
  "id": "CLI-001",
  "nombre": "Vicente Priego",
  "email": "vicente@example.com",
  "estado": "ACTIVO"
}
```

#### POST /api/clientes
Crear nuevo cliente

#### PUT /api/clientes/{id}
Actualizar cliente

#### DELETE /api/clientes/{id}
Eliminar cliente

### Workers de Camunda

#### 1. obtener-datos-cliente
```java
@JobWorker(type = "obtener-datos-cliente")
public Map<String, Object> obtenerDatos(ActivatedJob job) {
    String clienteId = (String) job.getVariablesAsMap().get("clienteId");
    Cliente cliente = repositorio.findById(clienteId)
        .orElseThrow(() -> new ClienteNoEncontradoException(clienteId));
    return Map.of(
        "clienteObtenido", true,
        "clienteNombre", cliente.getNombre()
    );
}
```

#### 2. validar-tarjeta-credito
Valida tarjeta usando algoritmo de Luhn

#### 3. actualizar-estado-cliente
Actualiza estado del cliente

#### 4. revertir-estado-cliente
Revierte estado en caso de error

---

## ✈️ servicio-vuelos (9081)

### Responsabilidades
- Catálogo de vuelos
- Reservas de vuelos
- Cancelaciones (compensación)
- Gestión de asientos

### Modelo de Dominio

```java
@AggregateRoot
public class ReservaVuelo {
    @Identity
    private ReservaVueloId id;
    private VueloId vueloId;
    private ClienteId clienteId;
    private AsientoNumero asiento;
    private LocalDateTime fechaReserva;
    private EstadoReserva estado;
}

@Entity
public class Vuelo {
    private VueloId id;
    private String numeroVuelo;
    private String origen;
    private String destino;
    private LocalDateTime fechaSalida;
    private BigDecimal precio;
    private int asientosDisponibles;
}
```

### API REST

#### GET /api/vuelos
Buscar vuelos disponibles

**Query Params**:
- origen
- destino
- fecha

#### POST /api/vuelos/{id}/reservar
Reservar vuelo

### Workers

#### reservar-vuelo
```java
@JobWorker(type = "reservar-vuelo")
public Map<String, Object> reservar(ActivatedJob job) {
    // Lógica de reserva
    return Map.of(
        "vueloReservaId", reservaId,
        "vueloReservado", true
    );
}
```

#### cancelar-vuelo
Compensación - cancela reserva

---

## 🏨 servicio-hoteles (9082)

### Responsabilidades
- Catálogo de hoteles
- Reservas de habitaciones
- Cancelaciones
- Políticas de cancelación

### Modelo Similar a Vuelos

### API REST

#### GET /api/hoteles
Buscar hoteles

#### POST /api/hoteles/{id}/reservar
Reservar habitación

---

## 🚗 servicio-alquiler-coches (9083)

### Responsabilidades
- Catálogo de vehículos
- Reservas de coches
- Cancelaciones
- Validación de licencia

### Workers

#### reservar-coche
#### cancelar-coche

---

## 💳 servicio-pagos (9084)

### Responsabilidades
- Procesamiento de pagos
- Validación de fondos
- Confirmación de transacciones
- Reembolsos

### Modelo de Dominio

```java
@AggregateRoot
public class Pago {
    @Identity
    private PagoId id;
    private ReservaId reservaId;
    private BigDecimal monto;
    private TarjetaCredito tarjeta;
    private EstadoPago estado;
    private LocalDateTime fechaProcesamiento;
}

public enum EstadoPago {
    PENDIENTE,
    PROCESANDO,
    APROBADO,
    RECHAZADO,
    REEMBOLSADO
}
```

### Workers

#### procesar-pago
```java
@JobWorker(type = "procesar-pago")
public Map<String, Object> procesar(ActivatedJob job) {
    BigDecimal monto = (BigDecimal) job.getVariablesAsMap().get("monto");
    
    if (monto.compareTo(new BigDecimal("10000")) > 0) {
        throw new ZeebeBpmnError("ERROR_PAGO_RECHAZADO", 
            "Monto excede límite");
    }
    
    // Simular procesamiento
    String transaccionId = procesarPagoInterno(monto);
    
    return Map.of(
        "pagoExitoso", true,
        "transaccionId", transaccionId
    );
}
```

#### confirmar-reserva
#### revertir-estado-cliente  
#### marcar-reserva-advertencia

---

## 🎯 servicio-reservas (9090)

### Responsabilidades
- Coordinador principal
- Despliegue de procesos BPMN
- Orquestación
- API Gateway para reservas

### Configuración Especial

```java
@Configuration
public class CamundaConfig {
    
    @PostConstruct
    public void desplegarProcesos() {
        // Auto-deploy de BPMN
        log.info("🚀 Desplegando procesos BPMN...");
    }
}
```

### API REST

#### POST /api/reservas/iniciar
Iniciar proceso de reserva

#### GET /api/reservas/{id}
Consultar estado de reserva

#### POST /api/reservas/{id}/cancelar
Cancelar reserva (publica mensaje)

---

## 🔗 Comunicación entre Servicios

### Patrón Principal: Asíncrono vía Camunda

```
[Cliente] 
  → POST /api/reservas 
    → [servicio-reservas]
      → Inicia proceso BPMN
        → [Zeebe]
          → Job: obtener-datos-cliente
            → [servicio-clientes]
```

### Patrón Secundario: Síncrono REST (solo consultas)

```java
@Component
public class ClienteHttpClient {
    
    private final RestTemplate restTemplate;
    
    public ClienteDTO obtenerCliente(String id) {
        return restTemplate.getForObject(
            "http://servicio-clientes:9080/api/clientes/" + id,
            ClienteDTO.class
        );
    }
}
```

---

## 📊 Health Checks

Todos los servicios exponen:

```bash
# Liveness
GET /actuator/health/liveness

# Readiness
GET /actuator/health/readiness

# Completo
GET /actuator/health
```

---

## 🐳 Configuración Docker

Cada servicio tiene su `Dockerfile`:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 90XX
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

**Última actualización**: Diciembre 2024
