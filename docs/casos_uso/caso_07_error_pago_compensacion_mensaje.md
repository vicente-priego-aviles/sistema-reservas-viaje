# Caso 7: Error en Pago con Compensación por Mensaje

← [Índice de Casos de Uso](../doc_casos_uso.md)

## Descripción

Las reservas se completan correctamente pero el pago falla porque el monto total supera el límite del sistema (€10.000). El subproceso de pago lanza `ERROR_PROCESAR_PAGO`, que envía el mensaje `compensar-reserva` al subproceso de reserva. El subproceso de reserva captura el mensaje y ejecuta la compensación manual de las tres reservas.

El error de pago se simula cuando la suma de los precios calculados por los workers supera el límite. Para forzarlo, introducir precios altos al completar los User Tasks de gestión de reserva.

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
1. Validar Datos ✅
2. Gestión de Cliente ✅
3. Revisar Datos 👤
4. Proceso de Reserva (Paralelo)
   - Reservar Vuelo ✅
   - Reservar Hotel ✅
   - Reservar Coche ✅
   - User Tasks completados con precios altos 👤
5. Proceso de Pago
   - Procesar Pago ❌ (monto total > €10.000 → MontoExcedeLimiteException)
   - Boundary Event: ERROR_PROCESAR_PAGO
   - Envía mensaje "compensar-reserva" al subproceso de reserva
   - Notificar Cliente 📧
6. Subproceso de Reserva recibe mensaje de compensación:
   - Compensar Vuelo 🔄
   - Compensar Hotel 🔄
   - Compensar Coche 🔄
   - Actualizar registro cliente 🔄
7. Fin: Reserva No Completada ❌
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

Accede a http://localhost:8081 → pestaña **Processes** → selecciona "Proceso Principal de Reserva de Viaje" → pulsa **Start process**. Se abre el formulario de inicio (`iniciar-reserva`); rellénalo con los datos del caso y envía.

## Respuesta Esperada

```json
{
  "processInstanceKey": 2251799813685257,
  "estado": "INICIADA",
  "mensaje": "Reserva iniciada correctamente"
}
```

## Forzar el error: completar los User Tasks con precios altos

> ⚠️ **Sin este paso el proceso terminará con éxito** (como el Caso 1). El error de pago solo se activa si los precios introducidos en los User Tasks superan el límite de €10.000.

En **Tasklist** (http://localhost:8081) aparecerán tres User Tasks en paralelo una vez avanzado el proceso. Completa cada uno con los precios indicados:

| User Task | Campo de precio | Valor sugerido |
|-----------|----------------|----------------|
| ✈️ Introducir Datos del Vuelo | `precioVuelo` | `4001.0` |
| 🏨 Introducir Datos del Hotel | `precioHotel` | `3000.0` |
| 🚗 Introducir Datos del Coche | `precioCoche` | `3000.0` |

**Total: €10.001** → supera el límite → `MontoExcedeLimiteException` → `ERROR_PROCESAR_PAGO` → compensación por mensaje.

El resto de campos (fechas, destinos, clase de vuelo, etc.) puedes dejarlos con los valores por defecto del formulario.

---

## Logs a consultar

**`./scripts/logs.sh pagos`** — el fallo del pago:
```
🔄 Worker: procesar-pago - Reserva: ... - Monto: 10001.0€
❌ Monto excede límite: 10001.0€
```

**`./scripts/logs.sh reservas`** — las compensaciones que se disparan a continuación:
```
🛑 Iniciando worker de cancelación de vuelo (compensación) - Job Key: ...
❌ Cancelando reserva de vuelo: ... - Motivo: ...
✅ Reserva de vuelo cancelada exitosamente: ...
🛑 Iniciando worker de cancelación de hotel (compensación) - Job Key: ...
❌ Cancelando reserva de hotel: ... - Motivo: ...
✅ Reserva de hotel cancelada exitosamente: ...
🛑 Iniciando worker de cancelación de coche (compensación) - Job Key: ...
❌ Cancelando reserva de coche: ... - Motivo: ...
✅ Reserva de coche cancelada exitosamente: ...
```

## Verificar en Camunda

1. **Operate** (http://localhost:8080): instancia terminada en `fin-reserva-no-completada`; ver el subproceso de compensación manual activado; variables `pagoRealizado=false`
2. **Tasklist** (http://localhost:8081): no hay User Tasks pendientes tras el error
