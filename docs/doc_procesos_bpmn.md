# 📊 Procesos BPMN

Documentación completa de todos los procesos BPMN del sistema.

---

## 📋 Procesos Implementados

| Proceso | Archivo | Descripción |
|---------|---------|-------------|
| Principal | proceso-principal.bpmn | Flujo completo de reserva |
| Gestión Cliente | subproceso-gestion-cliente.bpmn | Validación de cliente |
| Proceso Reserva | subproceso-proceso-reserva.bpmn | Reservas paralelas |
| Proceso Pago | subproceso-pago.bpmn | Procesamiento de pago |

---

## 🗺️ Proceso Principal

<!--
📸 Insertar imagen aquí:
![Proceso Principal](../images/bpmn/proceso-principal.png)
-->

### Descripción
Proceso maestro que coordina todo el flujo de reserva de viaje.

### Elementos Principales
1. **Start Event**: Solicitud de Reserva
2. **Service Task**: Validar Datos
3. **Call Activity**: Gestión de Cliente
4. **User Task**: Revisar Datos
5. **Call Activity**: Proceso de Reserva
6. **Call Activity**: Proceso de Pago
7. **End Events**: Éxito / Error

### Variables de Proceso

| Variable | Tipo | Origen | Descripción |
|----------|------|--------|-------------|
| `reservaId` | String | Generado | UUID de la reserva |
| `clienteId` | String | Input | ID del cliente |
| `origen` | String | Input | Ciudad origen |
| `destino` | String | Input | Ciudad destino |
| `fechaInicio` | String | Input | Fecha inicio viaje |
| `fechaFin` | String | Input | Fecha fin viaje |
| `monto` | Number | Input | Monto total |

### Expresiones FEEL

```feel
# Generar UUID para reserva
= uuid()

# Validar fechas
= fechaInicio < fechaFin

# Calcular días
= date(fechaFin) - date(fechaInicio)
```

---

## 👤 Subproceso: Gestión de Cliente

<!--
📸 Insertar imagen aquí:
![Gestión de Cliente](../images/bpmn/subproceso-gestion-cliente.png)
-->

### Flujo del Proceso

```
Inicio
  ↓
Obtener Datos Cliente
  ↓
¿Cliente Encontrado? → No → Error: Cliente No Encontrado
  ↓ Sí
Validar Tarjeta Crédito
  ↓
¿Tarjeta Válida? → No → Error: Tarjeta Inválida
  ↓ Sí
Actualizar Estado: En Proceso
  ↓
Fin
```

### Service Tasks

#### 1. obtener-datos-cliente

**Worker Type**: `obtener-datos-cliente`  
**Retries**: 3

**Input**:
```json
{
  "clienteId": "CLI-001"
}
```

**Output**:
```json
{
  "clienteObtenido": true,
  "clienteNombre": "Vicente Priego",
  "clienteEmail": "vicente@example.com"
}
```

#### 2. validar-tarjeta-credito

**Worker Type**: `validar-tarjeta-credito`  
**Retries**: 3

**Validaciones**:
- Algoritmo de Luhn
- Fecha de expiración
- CVV presente

#### 3. actualizar-estado-cliente

**Worker Type**: `actualizar-estado-cliente`

**Input Mapping**:
```xml
<zeebe:input source="=&#34;EN_PROCESO_RESERVA&#34;" target="nuevoEstado" />
```

### Manejo de Errores

| Error | Código | Acción |
|-------|--------|--------|
| Cliente no encontrado | `ERROR_CLIENTE_NO_ENCONTRADO` | End Event con error |
| Tarjeta inválida | `ERROR_TARJETA_INVALIDA` | Boundary event → Error |

---

## 🎟️ Subproceso: Proceso de Reserva

<!--
📸 Insertar imagen aquí:
![Proceso de Reserva](../images/bpmn/subproceso-proceso-reserva.png)
-->

### Características

- ⚡ **Reservas Paralelas**: Vuelo, Hotel y Coche se reservan simultáneamente
- 🔁 **Compensaciones**: Cada reserva tiene su cancelación asociada
- 👤 **User Tasks**: Revisión manual de cada reserva
- 📝 **Eventos No Interrumpibles**: Actualización de tarjeta en paralelo

### Flujo Principal

```
Inicio
  ↓
Gateway Paralelo (Split)
  ├→ Reservar Vuelo → Revisar Vuelo
  ├→ Reservar Hotel → Revisar Hotel
  └→ Reservar Coche → Revisar Coche
  ↓
Gateway Paralelo (Join)
  ↓
Fin
```

### Service Tasks con Compensación

#### Reservar Vuelo
```xml
<bpmn:serviceTask id="reservar-vuelo" name="Reservar Vuelo">
  <bpmn:extensionElements>
    <zeebe:taskDefinition type="reservar-vuelo" retries="3" />
  </bpmn:extensionElements>
</bpmn:serviceTask>

<!-- Boundary Event -->
<bpmn:boundaryEvent id="compensar-vuelo" attachedToRef="reservar-vuelo">
  <bpmn:compensateEventDefinition />
</bpmn:boundaryEvent>

<!-- Tarea de Compensación -->
<bpmn:serviceTask id="cancelar-vuelo" 
                  name="Cancelar Vuelo" 
                  isForCompensation="true">
  <zeebe:taskDefinition type="cancelar-vuelo" />
</bpmn:serviceTask>
```

### User Tasks

#### ✈️ Revisar Reserva de Vuelo

**Task ID**: `revisar-reserva-vuelo`  
**Assignee**: `demo`

**Form Fields**:
- Número de vuelo
- Asiento asignado
- Hora salida/llegada
- Confirmación (checkbox)

#### 🏨 Revisar Reserva de Hotel

**Task ID**: `revisar-reserva-hotel`  
**Assignee**: `demo`

**Form Fields**:
- Nombre del hotel
- Número de habitación
- Tipo de habitación
- Confirmación (checkbox)

#### 🚗 Revisar Reserva de Coche

**Task ID**: `revisar-reserva-coche`  
**Assignee**: `demo`

**Form Fields**:
- Modelo del vehículo
- Punto de recogida
- Punto de devolución
- Confirmación (checkbox)

### Subproceso de Error

Cuando ocurre un error durante las reservas:

```
Error detectado
  ↓
Gateway Paralelo
  ├→ Compensar Vuelo
  ├→ Compensar Hotel
  └→ Compensar Coche
  ↓
Lanzar Error
```

### Subproceso de Compensación Manual

Triggered por mensaje `compensar-reserva`:

```
Mensaje recibido
  ↓
Compensar Vuelo → Compensar Hotel → Compensar Coche
  ↓
Actualizar Registro Cliente
  ↓
Fin
```

### Subproceso No Interrumpible

**Actualizar Información Tarjeta**:
- Triggered por mensaje `tarjeta-proporcionada`
- No interrumpe el flujo principal
- Actualiza info en paralelo

---

## 💳 Subproceso: Proceso de Pago

<!--
📸 Insertar imagen aquí:
![Proceso de Pago](../images/bpmn/subproceso-pago.png)
-->

### Flujo Happy Path

```
Inicio
  ↓
Procesar Pago
  ↓
Confirmar Reserva Completa
  ↓
Actualizar Estado: Confirmado
  ↓
Fin: Viaje Reservado con Éxito
```

### Flujo con Error de Pago

```
Inicio
  ↓
Procesar Pago → ERROR
  ↓
Compensar Reserva (Mensaje)
  ↓
Notificar Cliente
  ↓
Fin: Reserva No Completada
```

### Flujo con Error de Actualización

```
Inicio
  ↓
Procesar Pago → OK
  ↓
Confirmar Reserva → OK
  ↓
Actualizar Estado → ERROR
  ↓
Revertir Estado Cliente
  ↓
Marcar Reserva con Advertencia
  ↓
Fin: Reserva Confirmada con Advertencia
```

### Service Tasks

#### 1. procesar-pago

**Worker Type**: `procesar-pago`

**Lógica**:
```java
if (monto > 10000) {
    throw new ZeebeBpmnError("ERROR_PAGO_RECHAZADO", "Monto excede límite");
}
if (monto > 5000) {
    log.warn("⚠️ Monto alto, requiere revisión");
}
```

#### 2. confirmar-reserva

**Worker Type**: `confirmar-reserva`

**Output**:
```json
{
  "reservaConfirmada": true,
  "numeroConfirmacion": "CONF-12345",
  "fechaConfirmacion": "2025-12-01T10:30:00Z"
}
```

#### 3. revertir-estado-cliente

**Worker Type**: `revertir-estado-cliente`

**Input Mapping**:
```xml
<zeebe:input source="=&#34;ACTIVO&#34;" target="nuevoEstado" />
<zeebe:input source="=&#34;Error al actualizar&#34;" target="motivoReversion" />
```

---

## 🔄 Correlación de Mensajes

### Mensaje: compensar-reserva

```xml
<bpmn:message id="Message_CompensarReserva" name="compensar-reserva">
  <bpmn:extensionElements>
    <zeebe:subscription correlationKey="=reservaId" />
  </bpmn:extensionElements>
</bpmn:message>
```

**Publicar desde código**:
```java
zeebeClient.newPublishMessageCommand()
    .messageName("compensar-reserva")
    .correlationKey(reservaId)
    .variables(Map.of("motivo", "Error en pago"))
    .send()
    .join();
```

---

## 📝 Mejores Prácticas BPMN

### Nomenclatura

✅ **Correcto**:
- IDs: `kebab-case` en español
- Names: Español con iconos
- Flujos: Prefijo por contexto

❌ **Incorrecto**:
- IDs en inglés o camelCase
- Names técnicos sin contexto
- Flujos genéricos

### Compensaciones

✅ **Implementar**:
- Boundary events en tareas de negocio
- Tareas de compensación explícitas
- Asociaciones claras

### Timeouts

```xml
<bpmn:boundaryEvent id="timeout-event" attachedToRef="user-task">
  <bpmn:timerEventDefinition>
    <bpmn:timeDuration>PT1H</bpmn:timeDuration>
  </bpmn:timerEventDefinition>
</bpmn:boundaryEvent>
```

### Expresiones FEEL Útiles

```feel
# Fechas
= date(fechaInicio)
= date and time(fechaInicio + " 00:00:00")
= duration("P5D")  # 5 días

# Números
= monto * 0.21  # IVA
= round(precio, 2)

# Strings
= upper(nombre)
= substring(email, 1, index of(email, "@"))

# Listas
= count(reservas)
= sum(montos)
= contains(["ES", "FR"], pais)

# Condicionales
= if monto > 1000 then "ALTO" else "NORMAL"
```

---

## 🧪 Testing de Procesos

### Test de Proceso Completo

```java
@SpringBootTest
@ZeebeSpringTest
class ProcesoReservaTest {
    
    @Autowired
    private ZeebeClient client;
    
    @Test
    void testReservaExitosa() {
        // Iniciar proceso
        ProcessInstanceEvent process = client
            .newCreateInstanceCommand()
            .bpmnProcessId("TravelBookingScenario")
            .latestVersion()
            .variables(Map.of(
                "clienteId", "CLI-001",
                "origen", "Madrid",
                "destino", "Barcelona",
                "monto", 1500
            ))
            .send()
            .join();
        
        // Verificar que llegó a user task
        await()
            .atMost(Duration.ofSeconds(10))
            .untilAsserted(() -> {
                // Assert task exists
            });
    }
}
```

---

## 📊 Monitoreo de Procesos

### Métricas Clave

| Métrica | Descripción | Objetivo |
|---------|-------------|----------|
| Process Duration | Tiempo total del proceso | < 5 min |
| Active Instances | Procesos en ejecución | < 100 |
| Incidents | Errores sin resolver | 0 |
| Job Success Rate | Tasa de éxito de jobs | > 95% |

### Incidents Comunes

| Incident | Causa | Solución |
|----------|-------|----------|
| No retries left | Worker falla 3 veces | Revisar logs, corregir datos |
| Extract value error | Variable no existe | Validar mapeo de variables |
| Unknown worker | Worker no conectado | Verificar conexión Zeebe |
| Message correlation | Mensaje no correlaciona | Verificar correlation key |

---

**Última actualización**: Diciembre 2024
