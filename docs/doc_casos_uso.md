# 💡 Casos de Uso

Ejemplos prácticos de uso del sistema con diferentes escenarios.

---

## 📋 Escenarios Disponibles

| # | Escenario | Resultado | Complejidad |
|---|-----------|-----------|-------------|
| 1 | Reserva Exitosa | ✅ Éxito | Básico |
| 2 | Cliente Bloqueado | ❌ Error | Básico |
| 3 | Tarjeta Expirada | ❌ Error | Básico |
| 4 | Error en Pago con Compensación | 🔄 Compensación | Avanzado |
| 5 | Advertencia en Actualización | ⚠️ Advertencia | Avanzado |
| 6 | Actualización de Tarjeta en Paralelo | 🔄 Evento No Interrumpible | Avanzado |

---

## 🧪 Datos de Prueba

### 👥 Clientes Precargados (data.sql)

| UUID | Nombre | Estado | Tarjeta | Usar para |
|------|--------|--------|---------|-----------|
| `123e4567-e89b-12d3-a456-426655440000` | Juan Pérez García | ✅ ACTIVO | VISA válida | Casos exitosos |
| `223e4567-e89b-12d3-a456-426655440001` | María López Martínez | ✅ ACTIVO | MASTERCARD válida | Casos exitosos |
| `323e4567-e89b-12d3-a456-426655440002` | Carlos Rodríguez Sánchez | ✅ ACTIVO | AMEX válida | Casos exitosos |
| `b23e4567-e89b-12d3-a456-426655440010` | Roberto Morales Gil | 🚫 BLOQUEADO | — | Caso 2: cliente bloqueado |
| `g23e4567-e89b-12d3-a456-426655440015` | Raquel Iglesias Márquez | ✅ ACTIVO | ❌ VISA expirada (2023) | Caso 3: tarjeta expirada |

### 💳 Variables del proceso según monto de pago

El pago se calcula como la suma de `precioVueloFinal + precioHotelFinal + precioCocheFinal`, que son los precios que devuelven los workers de reserva (simulados).

---

## 🚀 Cómo iniciar el proceso

Hay dos formas de crear una instancia del proceso `proceso-principal`:

### Opción A: REST API del microservicio (puerto 9090)

```bash
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "123e4567-e89b-12d3-a456-426655440000",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 2,
    "emailContacto": "juan.perez@example.com",
    "telefonoContacto": "+34600123456"
  }'
```

📖 Swagger UI disponible en: http://localhost:9090/swagger-ui.html

### Opción B: Zeebe REST API directamente (puerto 8088)

📖 Swagger UI: http://localhost:8088/swagger-ui/index.html — endpoint `POST /v2/process-instances`

```json
{
  "processDefinitionId": "proceso-principal",
  "variables": {
    "clienteId": "123e4567-e89b-12d3-a456-426655440000",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 2,
    "emailContacto": "juan.perez@example.com",
    "telefonoContacto": "+34600123456"
  }
}
```

> 💡 `processDefinitionId` es el ID del proceso definido en el BPMN (el atributo `id` del elemento `<process>`).
> También puedes usar `processDefinitionKey` con el número que aparece en los logs de arranque de `servicio-reservas`:
> `BpmnDeploymentService - Proceso deployado: proceso-principal | key: <NUMBER>`

---

## ✅ Caso 1: Reserva Exitosa (Happy Path)

### Descripción
Cliente activo con tarjeta válida realiza una reserva completa exitosamente.

### Datos de Entrada

> ⚠️ `fechaInicio` y `fechaFin` deben ser fechas **futuras**. El worker de validación rechazará fechas pasadas con `ERROR_DATOS_INVALIDOS`.

```json
{
  "clienteId": "123e4567-e89b-12d3-a456-426655440000",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2027-06-01",
  "fechaFin": "2027-06-08",
  "numeroPasajeros": 2,
  "emailContacto": "juan.perez@example.com",
  "telefonoContacto": "+34600123456"
}
```

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅ → Juan Pérez García (ACTIVO)
   - Validar tarjeta ✅       → VISA *0366 válida
   - Actualizar estado ✅     → EN_PROCESO_RESERVA
3. Revisar Datos de Entrada (User Task) 👤
4. Proceso de Reserva (Paralelo)
   - Reservar Vuelo ✅  → Revisar Vuelo 👤
   - Reservar Hotel ✅  → Revisar Hotel 👤
   - Reservar Coche ✅  → Revisar Coche 👤
5. Proceso de Pago
   - Procesar Pago ✅
   - Confirmar Reserva ✅
   - Actualizar Estado: RESERVA_CONFIRMADA ✅
6. Fin: Solicitud de Viaje Completada 🎉
```

### Ejecutar con cURL

```bash
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "123e4567-e89b-12d3-a456-426655440000",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 2,
    "emailContacto": "juan.perez@example.com",
    "telefonoContacto": "+34600123456"
  }'
```

### Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685251,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

### Verificar en Camunda

1. Acceder a **Operate**: http://localhost:8080 — ver instancia del proceso en ejecución
2. Acceder a **Tasklist**: http://localhost:8081 — completar los User Tasks para avanzar el flujo

---

## ❌ Caso 2: Cliente Bloqueado

### Descripción
Se intenta reservar con un cliente que está bloqueado en el sistema.

### Datos de Entrada

```json
{
  "clienteId": "b23e4567-e89b-12d3-a456-426655440010",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2025-12-15",
  "fechaFin": "2025-12-22",
  "numeroPasajeros": 1,
  "emailContacto": "roberto.morales@example.com",
  "telefonoContacto": "+34611234567"
}
```

**Nota**: Roberto Morales Gil está 🚫 BLOQUEADO por "Actividad sospechosa detectada".

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ❌ → Roberto Morales Gil (BLOQUEADO)
   - Boundary Event captura el bloqueo
3. Fin: Error en Gestión de Cliente ❌
```

### Logs Esperados

```
🔍 Iniciando gestión de cliente: b23e4567-e89b-12d3-a456-426655440010
🔍 Buscando cliente en base de datos...
🚫 Cliente bloqueado: Roberto Morales Gil — Actividad sospechosa detectada
❌ Error en gestión de cliente: ERROR_CLIENTE_BLOQUEADO
```

---

## ❌ Caso 3: Tarjeta Expirada

### Descripción
Cliente activo pero con tarjeta de crédito expirada.

### Datos de Entrada

```json
{
  "clienteId": "g23e4567-e89b-12d3-a456-426655440015",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2025-12-15",
  "fechaFin": "2025-12-22",
  "numeroPasajeros": 1,
  "emailContacto": "raquel.iglesias@example.com",
  "telefonoContacto": "+34666789012"
}
```

**Nota**: Raquel Iglesias Márquez tiene tarjeta VISA expirada (08/2023).

### Flujo del Proceso

```
1. Validar Datos de Entrada ✅
2. Gestión de Cliente
   - Obtener datos cliente ✅ → Raquel Iglesias Márquez (ACTIVO)
   - Validar tarjeta ❌       → VISA *6474 EXPIRADA (08/2023)
   - Boundary Event: Error Tarjeta Inválida
3. Fin: Error en Gestión de Cliente ❌
```

### Logs Esperados

```
🔍 Validando tarjeta para cliente: g23e4567-e89b-12d3-a456-426655440015
🔍 Comprobando fecha de expiración...
❌ Tarjeta expirada: VISA *6474 — expiró 08/2023
❌ Error: ERROR_TARJETA_INVALIDA
```

---

## 🔄 Caso 4: Error en Pago con Compensación

### Descripción
Las reservas se completan correctamente pero el pago falla, disparando la compensación de todas las reservas.

El error de pago se simula cuando la suma de los precios calculados por los workers supera el límite permitido. Para forzarlo, se puede completar los User Tasks de revisión de vuelo/hotel/coche con precios altos.

### Datos de Entrada

```json
{
  "clienteId": "123e4567-e89b-12d3-a456-426655440000",
  "origen": "Madrid",
  "destino": "Barcelona",
  "fechaInicio": "2025-12-15",
  "fechaFin": "2025-12-22",
  "numeroPasajeros": 2,
  "emailContacto": "juan.perez@example.com",
  "telefonoContacto": "+34600123456"
}
```

### Flujo del Proceso

```
1. Validar Datos ✅
2. Gestión de Cliente ✅
3. Revisar Datos 👤
4. Proceso de Reserva (Paralelo)
   - Reservar Vuelo ✅
   - Reservar Hotel ✅
   - Reservar Coche ✅
   - User Tasks completadas 👤
5. Proceso de Pago
   - Procesar Pago ❌ (monto excede límite)
   - Boundary Event: Error Procesar Pago
   - Mensaje: Compensar Reserva
   - Notificar Cliente 📧
6. Proceso de Reserva recibe mensaje de compensación
   - Compensar Vuelo 🔄
   - Compensar Hotel 🔄
   - Compensar Coche 🔄
7. Fin: Reserva No Completada ❌
```

### Logs Esperados

```
✅ Vuelo reservado
✅ Hotel reservado
✅ Coche reservado
💳 Procesando pago...
❌ Error: Monto excede límite permitido
🔄 Iniciando compensaciones...
🔄 Cancelando vuelo
🔄 Cancelando hotel
🔄 Cancelando coche
✅ Compensaciones completadas
📧 Notificando cliente sobre reserva fallida
```

### Verificar Compensaciones

En **Camunda Operate** verás:
- Subproceso de compensación activado
- Eventos de compensación ejecutados
- Variables actualizadas

---

## ⚠️ Caso 5: Advertencia en Actualización

### Descripción
El pago se procesa correctamente pero falla la actualización del estado del cliente, generando una advertencia.

### Flujo del Proceso

```
1-4. Flujo normal hasta Pago ✅
5. Proceso de Pago
   - Procesar Pago ✅
   - Confirmar Reserva ✅
   - Actualizar Estado ❌
   - Boundary Event: Error Actualización
   - Revertir Estado del Cliente 🔄
   - Marcar Reserva con Advertencia ⚠️
6. Fin: Reserva Confirmada con Advertencia ⚠️
```

### Variables de Salida

```json
{
  "reservaConfirmada": true,
  "estadoReservaFinal": "CONFIRMADA_CON_ADVERTENCIA",
  "requiereIntervencionManual": true,
  "motivo": "Error al actualizar estado tras confirmación"
}
```

---

## 🔄 Caso 6: Actualización de Tarjeta en Paralelo

### Descripción
Durante el proceso de reserva, el cliente actualiza su información de tarjeta de crédito sin interrumpir el flujo principal.

### Paso 1: Iniciar Reserva

```bash
curl -X POST http://localhost:9090/api/reservas/iniciar \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "123e4567-e89b-12d3-a456-426655440000",
    "origen": "Madrid",
    "destino": "Barcelona",
    "fechaInicio": "2027-06-01",
    "fechaFin": "2027-06-08",
    "numeroPasajeros": 2,
    "emailContacto": "juan.perez@example.com",
    "telefonoContacto": "+34600123456"
  }'

# Guardar processInstanceKey de la respuesta
```

### Paso 2: Publicar Mensaje de Actualización de Tarjeta

Con el proceso en ejecución (esperando un User Task), publicar el mensaje desde **Zeebe Swagger** (http://localhost:8088/swagger-ui/index.html), endpoint `POST /v2/messages/publication`:

```json
{
  "messageName": "tarjeta-proporcionada",
  "correlationKey": "<processInstanceKey>",
  "variables": {
    "nuevaTarjeta": "5555-4444-3333-2222",
    "fechaExpiracion": "12/2026"
  }
}
```

### Comportamiento

- ✅ El proceso principal **NO se interrumpe**
- 🔄 Se activa el subproceso "Actualizar Información Tarjeta Crédito"
- 💾 La tarjeta se actualiza en paralelo
- ✅ El proceso principal continúa normalmente

### Logs Esperados

```
🔄 Proceso de reserva en curso...
📥 Mensaje recibido: tarjeta-proporcionada
🔄 Iniciando actualización de tarjeta (no interrumpible)...
💾 Actualizando información de tarjeta...
✅ Tarjeta actualizada correctamente
🔄 Proceso principal continúa...
```

---

## 📚 Recursos

- [Quick Start](doc_quick_start.md) — Guía de arranque del sistema
- [Procesos BPMN](doc_procesos_bpmn.md) — Documentación de workflows
- 📖 Swagger microservicio reservas: http://localhost:9090/swagger-ui.html
- 📖 Swagger Zeebe REST API: http://localhost:8088/swagger-ui/index.html
- 🔍 Camunda Operate: http://localhost:8080
- ✅ Camunda Tasklist: http://localhost:8081

---

**Última actualización**: Junio 2026
