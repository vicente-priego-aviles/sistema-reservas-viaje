# Caso 7: Error en Pago con Compensación por Mensaje

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

Las reservas de vuelo, hotel y coche se completan correctamente, pero el pago falla porque el monto total supera el límite del sistema (€10.000). El subproceso de pago lanza `ERROR_PROCESAR_PAGO`, que notifica al cliente y termina con un error end event (`ERROR_PAGO_FALLIDO`) que propaga el fallo al proceso principal.

En `proceso-principal`, el boundary event de error activa una service task (`publicar-compensacion-reserva`) que publica el mensaje `compensar-reserva`. El event subprocess **Manejar Compensación** (`subproceso-compensacion-manual`), registrado al inicio del proceso, recibe el mensaje y ejecuta en paralelo la compensación Saga de las tres reservas, revertiendo además el estado del cliente a `ACTIVO`.

El error de pago se simula cuando `precioVueloFinal + precioHotelFinal + precioCocheFinal` supera €10.000. Para forzarlo, introducir precios altos en los User Tasks de gestión de reserva.

## Datos de Entrada

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

Los datos de inicio son los mismos que el Caso 1. El error se fuerza en los User Tasks de reserva introduciendo precios altos — ver sección [Forzar el error](#forzar-el-error-completar-los-user-tasks-con-precios-altos) más abajo.

## Flujo del Proceso

```
1.  Validar Datos ✅
2.  Gestión de Cliente ✅  →  estado cliente: ACTIVO → EN_PROCESO_RESERVA
3.  Revisar Datos 👤
4.  Proceso de Reserva (Paralelo) ✅
    - Introducir Datos Vuelo 👤  →  Reservar Vuelo ✅
    - Introducir Datos Hotel 👤  →  Reservar Hotel ✅
    - Introducir Datos Coche 👤  →  Reservar Coche ✅
5.  Gateway: ¿Reserva Exitosa? → SÍ → Proceso de Pago
6.  Subproceso de Pago (call activity) → TERMINADO por error
    - Procesar Pago ❌  (monto total > €10.000 → ERROR_PROCESAR_PAGO)
    - Notificar Cliente: Tarjeta No Válida 📧
    - Fin error: ERROR_PAGO_FALLIDO  →  propaga al proceso principal
7.  Boundary event "Error Pago" en proceso principal
    → publicar-compensacion-reserva (publica mensaje "compensar-reserva")
    → Fin: Pago Fallido ❌
8.  Subproceso "Manejar Compensación" activado por mensaje (en paralelo)
    - Compensar Vuelo 🔄  →  cancelar-vuelo ✅
    - Compensar Hotel 🔄  →  cancelar-hotel ✅
    - Compensar Coche 🔄  →  cancelar-coche ✅
    - Revertir Estado del Cliente ✅  →  estado cliente: EN_PROCESO_RESERVA → ACTIVO
    - Fin: Reserva Compensada
9.  Proceso termina cuando ambos tokens completan
    (flujo principal en "Pago Fallido" + subproceso compensación en "Reserva Compensada")
```

## Iniciar el proceso

### Opción A — Camunda REST API (Swagger)

Accede a http://localhost:8088/swagger-ui/index.html, endpoint `POST /v2/process-instances`, con el body:

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

> `processDefinitionId` es el `id` del elemento `<bpmn:process>` — en este caso `proceso-principal`. Lanza siempre la última versión desplegada. No uses `processDefinitionKey` (numérico) a la vez que `processDefinitionId`; son alternativos.

### Opción B — API de `servicio-reservas` (cURL)

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

### Opción C — Tasklist

Accede a http://localhost:8082 → pestaña **Processes** → selecciona "Proceso Principal de Reserva de Viaje" → pulsa **Start process**. Se abre el formulario de inicio (`iniciar-reserva`); rellénalo con los datos del caso y envía.

## Forzar el error: completar los User Tasks con precios altos

> ⚠️ **Sin este paso el proceso terminará con éxito** (como el Caso 1). El error de pago solo se activa si los precios introducidos en los User Tasks superan el límite de €10.000.

En **Tasklist** (http://localhost:8082) aparecerán tres User Tasks en paralelo una vez avanzado el proceso. Completa cada uno con los precios indicados:

| User Task | Campo de precio | Valor sugerido |
|-----------|----------------|----------------|
| ✈️ Introducir Datos del Vuelo | `precioVuelo` | `5000` |
| 🏨 Introducir Datos del Hotel | `precioHotel` | `5000` |
| 🚗 Introducir Datos del Coche | `precioCoche` | `5000` |

**Total: €15.000** → supera el límite → `MontoExcedeLimiteException` → `ERROR_PROCESAR_PAGO` → compensación por mensaje.

El resto de campos (fechas, destinos, clase de vuelo, etc.) puedes dejarlos con los valores por defecto del formulario.

---

## Logs a consultar

**`./scripts/logs.sh pagos`** — el fallo del pago y la publicación del mensaje:
```
🔄 Worker: procesar-pago - Reserva: c1dbefe9-6dc4-11f1-a144-46b5a1ea4ef1 - Monto: 15000.0€
📨 Publicando mensaje compensar-reserva para reserva: c1dbefe9-6dc4-11f1-a144-46b5a1ea4ef1
✅ Mensaje compensar-reserva publicado para reserva: c1dbefe9-6dc4-11f1-a144-46b5a1ea4ef1
```

**`./scripts/logs.sh reservas`** — las compensaciones que se disparan en paralelo:
```
❌ Cancelando reserva de vuelo: 117c5764-cbc4-40c8-829b-91e1efeb196e - Motivo: Compensación por error en el proceso de reserva
✅ Reserva de vuelo cancelada: 117c5764-cbc4-40c8-829b-91e1efeb196e
❌ Cancelando reserva de hotel: b1fb16e8-31b1-4b49-b916-1dcb492e591e - Motivo: Compensación por error en el proceso de reserva
✅ Reserva de hotel cancelada: b1fb16e8-31b1-4b49-b916-1dcb492e591e
🛑 Iniciando worker de cancelación de coche (compensación) - Job Key: ...
❌ Cancelando reserva de coche: d1facf6a-1abb-428f-bfe7-2c1cc919678e - Motivo: Compensación por error en el proceso de reserva
✅ Reserva de coche cancelada exitosamente: d1facf6a-1abb-428f-bfe7-2c1cc919678e
```

**`./scripts/logs.sh clientes`** — la reversión del estado del cliente:
```
🔍 Actualizando estado de cliente: 123e4567-e89b-12d3-a456-426655440000 → Estado nuevo: ACTIVO - Reserva: c1dbefe9-6dc4-11f1-a144-46b5a1ea4ef1
↩️ Proceso de reserva cancelado para cliente: 123e4567-e89b-12d3-a456-426655440000 - EN_PROCESO_RESERVA → ACTIVO
✅ Estado de cliente actualizado correctamente: 123e4567-e89b-12d3-a456-426655440000 - EN_PROCESO_RESERVA → ACTIVO
```

## Verificar en Camunda Operate

Accede a http://localhost:8081 y busca la instancia de `proceso-principal`.

**Estado esperado:**
- Instancia: `COMPLETED`
- End event: `evento-fin-error-pago` (❌ Pago Fallido)
- Actividades completadas en el flujo principal: `publicar-compensar-reserva-task` ✅
- Event subprocess `subproceso-compensacion-manual` (Manejar Compensación): `COMPLETED`
  - `compensar-vuelo-manual`, `compensar-hotel-manual`, `compensar-coche-manual`: `COMPLETED`
  - `cancelar-vuelo`, `cancelar-hotel`, `cancelar-coche`: `COMPLETED`
  - `actualizar-registro-cliente`: `COMPLETED`
- `call-activity-proceso-pago`: `TERMINATED` (terminado por el boundary event de error)
- No quedan User Tasks pendientes en Tasklist

**Variables clave al final:**
- `estadoCliente = "ACTIVO"` (revertido desde `EN_PROCESO_RESERVA`)
- `estadoAnterior = "EN_PROCESO_RESERVA"`
- `reservaVueloId`, `reservaHotelId`, `reservaCocheId`: canceladas en BD

## Instancia de Referencia

Ejecución validada el **2026-06-21**:

| Campo | Valor |
|-------|-------|
| Process Instance Key | `2251799814455506` |
| Versión BPMN | `proceso-principal` v50 |
| Estado final | `COMPLETED` |
| Inicio | 2026-06-21T22:58:45 UTC |
| Fin | 2026-06-21T23:02:38 UTC |
| Duración | ~4 minutos (incluye interacción manual con User Tasks) |
| `reservaId` | `c1dbefe9-6dc4-11f1-a144-46b5a1ea4ef1` |
| `clienteId` | `123e4567-e89b-12d3-a456-426655440000` (Juan Pérez García) |
| Monto total | 15.000€ (5.000 vuelo + 5.000 hotel + 5.000 coche) |
| `reservaVueloId` | `117c5764-cbc4-40c8-829b-91e1efeb196e` → CANCELADA |
| `reservaHotelId` | `b1fb16e8-31b1-4b49-b916-1dcb492e591e` → CANCELADA |
| `reservaCocheId` | `d1facf6a-1abb-428f-bfe7-2c1cc919678e` → CANCELADA |

### Traza de actividades (orden cronológico)

| Timestamp (UTC) | Estado | Tipo | Actividad |
|-----------------|--------|------|-----------|
| 22:58:45 | COMPLETED | START_EVENT | evento-inicio-solicitud-reserva |
| 22:58:45 | COMPLETED | SERVICE_TASK | validar-datos-entrada |
| 22:58:45 | COMPLETED | CALL_ACTIVITY | call-activity-gestion-cliente |
| 22:58:46 | COMPLETED | USER_TASK | revisar-datos-entrada |
| 23:01:22 | COMPLETED | SUB_PROCESS | subprocess-proceso-reserva |
| 23:01:22–23:02:38 | COMPLETED | USER_TASK × 3 | introducir-datos-vuelo / hotel / coche |
| 23:01:58 | COMPLETED | SERVICE_TASK | reservar-coche |
| 23:02:23 | COMPLETED | SERVICE_TASK | reservar-vuelo |
| 23:02:38 | COMPLETED | SERVICE_TASK | reservar-hotel |
| 23:02:38 | COMPLETED | PARALLEL_GATEWAY | gateway-fin-paralelo |
| 23:02:38 | COMPLETED | END_EVENT | fin-reserva-completada (dentro del subprocess) |
| 23:02:38 | COMPLETED | EXCLUSIVE_GATEWAY | gateway-resultado-reserva |
| 23:02:38 | TERMINATED | CALL_ACTIVITY | call-activity-proceso-pago |
| 23:02:38 | COMPLETED | BOUNDARY_EVENT | evento-error-proceso-pago |
| 23:02:38 | COMPLETED | SERVICE_TASK | publicar-compensar-reserva-task |
| 23:02:38 | COMPLETED | END_EVENT | evento-fin-error-pago |
| 23:02:38 | COMPLETED | EVENT_SUB_PROCESS | subproceso-compensacion-manual |
| 23:02:38 | COMPLETED | INTERMEDIATE_THROW × 3 | compensar-vuelo/hotel/coche-manual |
| 23:02:38 | COMPLETED | SERVICE_TASK × 3 | cancelar-vuelo / cancelar-hotel / cancelar-coche |
| 23:02:38 | COMPLETED | SERVICE_TASK | actualizar-registro-cliente (EN_PROCESO_RESERVA → ACTIVO) |
| 23:02:38 | COMPLETED | END_EVENT | compensacion-manual-fin |
